package ch.epfl.pop.model.network.method.message.data.rollCall

import ch.epfl.pop.json.Protocol.createRollCallFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Timestamp}
import spray.json._

case class CreateRollCall(
                          id: Hash,
                          name: String,
                          creation: Timestamp,
                          start: Option[Timestamp],
                          scheduled: Option[Timestamp],
                          location: String,
                          roll_call_description: Option[String]
                        ) extends MessageData {
  override def _object: ObjectType = ObjectType.ROLL_CALL
  override def action: ActionType = ActionType.CREATE
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

  override def buildFromJson(messageData: MessageData, payload: String): CreateRollCall =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[CreateRollCall]
}
