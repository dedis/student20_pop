package ch.epfl.pop.model.network.method

import ch.epfl.pop.json.HighLevelProtocol._
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.objects.Channel
import spray.json._

case class Catchup(override val channel: Channel) extends Params(channel)

object Catchup extends Parsable {
  def apply(channel: Channel): Catchup = {
    new Catchup(channel)
  }

  override def buildFromJson(payload: String): Catchup = payload.parseJson.asJsObject.convertTo[Catchup]
}
