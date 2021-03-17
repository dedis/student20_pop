package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.Protocol.subscribeFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.MessageData
import spray.json._

case class Subscribe(channel: Channel) extends Params

object Subscribe extends Parsable {
  def apply(channel: Channel): Subscribe = {
    // FIXME add checks
    new Subscribe(channel)
  }

  override def buildFromJson(messageData: MessageData, payload: String): Subscribe =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[Subscribe]
}
