package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol.catchupFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.MessageData
import spray.json._

case class Catchup(channel: Channel) extends Params

object Catchup extends Parsable {
  def apply(channel: Channel): Catchup = {
    // FIXME add checks
    new Catchup(channel)
  }

  override def buildFromJson(messageData: MessageData, payload: String): Catchup =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[Catchup]
}
