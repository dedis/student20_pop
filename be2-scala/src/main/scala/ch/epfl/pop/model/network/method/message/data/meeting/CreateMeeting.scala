package ch.epfl.pop.model.network.method.message.data.meeting

import ch.epfl.pop.json.Protocol.createMeetingFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Timestamp}
import spray.json._

case class CreateMeeting(
                          id: Hash,
                          name: String,
                          creation: Timestamp,
                          location: Option[String],
                          start: Timestamp,
                          end: Option[Timestamp],
                          extra: Option[Any]
                        ) extends MessageData {
  override def _object: ObjectType = ObjectType.MEETING
  override def action: ActionType = ActionType.CREATE
}

object CreateMeeting extends Parsable {
  def apply(
             id: Hash,
             name: String,
             creation: Timestamp,
             location: Option[String],
             start: Timestamp,
             end: Option[Timestamp],
             extra: Option[Any]
           ): CreateMeeting = {
    // FIXME add checks
    new CreateMeeting(id, name, creation, location, start, end, extra)
  }

  override def buildFromJson(messageData: MessageData, payload: String): CreateMeeting =
    // TODO exception handling
    payload.parseJson.asJsObject.convertTo[CreateMeeting]
}
