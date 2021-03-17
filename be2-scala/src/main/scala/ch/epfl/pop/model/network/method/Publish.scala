package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol.publishFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.MessageData
import spray.json._

case class Publish(channel: Channel, message: Message) extends ParamsWithMessage

object Publish extends Parsable {
  def apply(channel: Channel, message: Message): Publish = {
    // FIXME add checks
    new Publish(channel, message)
  }

  override def buildFromJson(messageData: MessageData, payload: String): Publish =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[Publish]
}
