package ch.epfl.pop.model.network.method.message.data.meeting

import ch.epfl.pop.json.HighLevelProtocol.stateMeetingFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Timestamp, WitnessSignaturePair}
import spray.json._

case class StateMeeting(
                         id: Hash,
                         name: String,
                         creation: Timestamp,
                         last_modified: Timestamp,
                         location: Option[String],
                         start: Timestamp,
                         end: Option[Timestamp],
                         extra: Option[Any],
                         modification_id: Hash,
                         modification_signatures: List[WitnessSignaturePair]
                       ) extends MessageData {
  override def _object: ObjectType = ObjectType.MEETING
  override def action: ActionType = ActionType.STATE
}

object StateMeeting extends Parsable {
  def apply(
             id: Hash,
             name: String,
             creation: Timestamp,
             last_modified: Timestamp,
             location: Option[String],
             start: Timestamp,
             end: Option[Timestamp],
             extra: Option[Any],
             modification_id: Hash,
             modification_signatures: List[WitnessSignaturePair]
           ): StateMeeting = {
    // FIXME add checks
    new StateMeeting(id, name, creation, last_modified, location, start, end, extra, modification_id, modification_signatures)
  }

  override def buildFromJson(messageData: MessageData, payload: String): StateMeeting =
    // TODO exception handling
    payload.parseJson.asJsObject.convertTo[StateMeeting]
}
