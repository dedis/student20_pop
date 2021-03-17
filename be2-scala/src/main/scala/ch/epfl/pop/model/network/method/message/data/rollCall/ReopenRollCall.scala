package ch.epfl.pop.model.network.method.message.data.rollCall

import ch.epfl.pop.json.Protocol.reopenRollCallFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Timestamp}
import spray.json._

case class ReopenRollCall(
                           update_id: Hash,
                           opens: Hash,
                           start: Timestamp
                         ) extends MessageData {
  override def _object: ObjectType = ObjectType.ROLL_CALL
  override def action: ActionType = ActionType.REOPEN
}

object ReopenRollCall extends Parsable {
  def apply(
             update_id: Hash,
             opens: Hash,
             start: Timestamp
           ): ReopenRollCall = {
    // FIXME add checks
    new ReopenRollCall(update_id, opens, start)
  }

  override def buildFromJson(messageData: MessageData, payload: String): ReopenRollCall =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[ReopenRollCall]
}

