package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol._
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.objects.Channel
import spray.json._

case class Unsubscribe(override val channel: Channel) extends Params(channel)

object Unsubscribe extends Parsable {
  def apply(channel: Channel): Unsubscribe = {
    new Unsubscribe(channel)
  }

  override def buildFromJson(payload: String): Unsubscribe = payload.parseJson.asJsObject.convertTo[Unsubscribe]
}
