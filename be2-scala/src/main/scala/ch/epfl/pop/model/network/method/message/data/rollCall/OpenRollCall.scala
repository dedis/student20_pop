package ch.epfl.pop.model.network.method.message.data.rollCall

import ch.epfl.pop.json.MessageDataProtocol._
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Timestamp}
import spray.json._

case class OpenRollCall(
                         update_id: Hash,
                         opens: Hash,
                         start: Timestamp
                       ) extends MessageData {
  override val _object: ObjectType = ObjectType.ROLL_CALL
  override val action: ActionType = ActionType.OPEN
}

object OpenRollCall extends Parsable {
  def apply(
             update_id: Hash,
             opens: Hash,
             start: Timestamp
           ): OpenRollCall = {
    new OpenRollCall(update_id, opens, start)
  }

  override def buildFromJson(payload: String): OpenRollCall = payload.parseJson.asJsObject.convertTo[OpenRollCall]
}
