package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol._
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.objects.Channel
import spray.json._

case class Subscribe(override val channel: Channel) extends Params(channel)

object Subscribe extends Parsable {
  def apply(channel: Channel): Subscribe = {
    new Subscribe(channel)
  }

  override def buildFromJson(payload: String): Subscribe = payload.parseJson.asJsObject.convertTo[Subscribe]
}
