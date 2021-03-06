package coinffeine.peer.api.impl

import coinffeine.peer.bitcoin.platform.DefaultBitcoinPlatform
import coinffeine.peer.config.user.UserFileConfigComponent
import coinffeine.peer.exchange.DefaultExchangeActor
import coinffeine.peer.market.orders.archive.h2.H2OrderArchive
import coinffeine.peer.{CoinffeinePeerActor, ProtocolConstants}
import coinffeine.protocol.gateway.overlay.OverlayMessageGateway
import coinffeine.protocol.serialization.protobuf.ProtobufProtocolSerializationComponent

trait ProductionCoinffeineComponent
  extends DefaultCoinffeineApp.Component
    with CoinffeinePeerActor.Component
    with ProtocolConstants.DefaultComponent
    with DefaultExchangeActor.Component
    with DefaultBitcoinPlatform.Component
    with OverlayMessageGateway.Component
    with ProtobufProtocolSerializationComponent
    with UserFileConfigComponent
    with H2OrderArchive.Component
