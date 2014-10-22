package coinffeine.peer.market

import scala.concurrent.duration._

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout

import coinffeine.model.currency.FiatCurrency
import coinffeine.model.market.{Market, OrderBookEntry, OrderId}
import coinffeine.peer.ProtocolConstants
import coinffeine.peer.market.SubmissionSupervisor.{KeepSubmitting, StopSubmitting}

private class SubmissionSupervisor(gateway: ActorRef, protocolConstants: ProtocolConstants)
  extends Actor with ActorLogging {

  private implicit val timeout = Timeout(1.second)
  private var delegatesByMarket = Map.empty[Market[_ <: FiatCurrency], ActorRef]

  override def receive = {
    case message@KeepSubmitting(order) =>
      getOrCreateDelegate(marketOf(order)) forward message

    case message@StopSubmitting(order) =>
      delegatesByMarket.values.foreach(_ forward message)
  }

  private def marketOf(order: OrderBookEntry[_ <: FiatCurrency]) =
    Market(currency = order.price.currency)

  private def getOrCreateDelegate(market: Market[_ <: FiatCurrency]): ActorRef =
    delegatesByMarket.getOrElse(market, createDelegate(market))

  private def createDelegate(market: Market[_ <: FiatCurrency]): ActorRef = {
    log.info(s"Start submitting to $market")
    val newDelegate = context.actorOf(
      MarketSubmissionActor.props(market, gateway, protocolConstants),
      market.currency.toString
    )
    delegatesByMarket += market -> newDelegate
    newDelegate
  }
}

object SubmissionSupervisor {

  case class KeepSubmitting(order: OrderBookEntry[_ <: FiatCurrency])

  case class StopSubmitting(orderId: OrderId)

  case class InMarket(order: OrderBookEntry[_ <: FiatCurrency])

  case class Offline(order: OrderBookEntry[_ <: FiatCurrency])

  def props(gateway: ActorRef, constants: ProtocolConstants) =
    Props(new SubmissionSupervisor(gateway, constants))
}
