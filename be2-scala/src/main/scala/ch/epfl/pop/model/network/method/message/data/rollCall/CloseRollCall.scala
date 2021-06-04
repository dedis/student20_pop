package ch.epfl.pop.model.network.method.message.data.rollCall

import ch.epfl.pop.json.MessageDataProtocol._
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, PublicKey, Timestamp}
import spray.json._

case class CloseRollCall(
                           update_id: Hash,
                           closes: Hash,
                           end: Timestamp,
                           attendees: List[PublicKey]
                         ) extends MessageData {
  override val _object: ObjectType = ObjectType.ROLL_CALL
  override val action: ActionType = ActionType.CLOSE
}

object CloseRollCall extends Parsable {
  def apply(
             update_id: Hash,
             closes: Hash,
             end: Timestamp,
             attendees: List[PublicKey]
           ): CloseRollCall = {
    new CloseRollCall(update_id, closes, end, attendees)
  }

  override def buildFromJson(payload: String): CloseRollCall = payload.parseJson.asJsObject.convertTo[CloseRollCall]
}



