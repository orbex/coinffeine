package coinffeine.peer.exchange.protocol.impl

import coinffeine.model.Both
import coinffeine.model.bitcoin.{ImmutableTransaction, Network, PublicKey}
import coinffeine.model.exchange.ActiveExchange.Amounts
import coinffeine.model.exchange._
import coinffeine.peer.exchange.protocol._

object DefaultExchangeProtocol extends ExchangeProtocol {

  override def createHandshake(
      exchange: DepositPendingExchange,
      deposit: ImmutableTransaction): Handshake = {
    requireValidDeposit(exchange, deposit)
    new DefaultHandshake(exchange, deposit)
  }

  private def requireValidDeposit(exchange: DepositPendingExchange,
                                                     deposit: ImmutableTransaction): Unit = {
    val validator = new DepositValidator(
      exchange.amounts, exchange.participants.map(_.bitcoinKey), exchange.parameters.network)
    val validation = exchange.role match {
      case BuyerRole => validator.requireValidBuyerFunds _
      case SellerRole => validator.requireValidSellerFunds _
    }
    validation(deposit).swap.foreach { errors =>
      throw new IllegalArgumentException(
        s"""Our own deposit is invalid: ${errors.list.mkString(", ")}
           |Deposit: $deposit
           |Exchange: $exchange
         """.stripMargin)
    }
  }

  override def createMicroPaymentChannel(exchange: RunningExchange) =
    new DefaultMicroPaymentChannel(exchange)

  override def validateDeposits(transactions: Both[ImmutableTransaction],
                                amounts: Amounts,
                                requiredSignatures: Both[PublicKey],
                                network: Network) =
    new DepositValidator(amounts, requiredSignatures, network).validate(transactions)
}
