package ch.epfl.pop.model.network.method.message.data.rollCall

import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType, Parsable}
import ch.epfl.pop.model.objects.{Hash, Timestamp}

case class CreateRollCall(
                          id: Hash,
                          name: String,
                          creation: Timestamp,
                          start: Option[Timestamp],
                          scheduled: Option[Timestamp],
                          location: String,
                          roll_call_description: Option[String]
                        ) extends MessageData {
  override val _object: ObjectType = ObjectType.ROLL_CALL
  override val action: ActionType = ActionType.CREATE
}

object CreateRollCall extends Parsable {
  def apply(
             id: Hash,
             name: String,
             creation: Timestamp,
             start: Option[Timestamp],
             scheduled: Option[Timestamp],
             location: String,
             roll_call_description: Option[String]
           ): CreateRollCall = {
    // FIXME add checks
    new CreateRollCall(id, name, creation, start, scheduled, location, roll_call_description)
  }

  override def buildFromJson(messageData: MessageData, payload: String): CreateRollCall = ???
}
