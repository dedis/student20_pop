package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol.{broadcastFormat}
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.MessageData
import spray.json._

case class Broadcast(channel: Channel, message: Message) extends ParamsWithMessage

object Broadcast extends Parsable {
  def apply(channel: Channel, message: Message): Broadcast = {
    // FIXME add checks
    new Broadcast(channel, message)
  }

  override def buildFromJson(messageData: MessageData, payload: String): Broadcast =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[Broadcast]
}
