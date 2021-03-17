package ch.epfl.pop.model.network.method.message.data.lao

import ch.epfl.pop.json.HighLevelProtocol.stateLaoFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, PublicKey, Timestamp, WitnessSignaturePair}
import spray.json._

case class StateLao(
                     id: Hash,
                     name: String,
                     creation: Timestamp,
                     last_modified: Timestamp,
                     organizer: PublicKey,
                     witnesses: List[PublicKey],
                     modification_id: Hash,
                     modification_signatures: List[WitnessSignaturePair]
                   ) extends MessageData {
  override def _object: ObjectType = ObjectType.LAO
  override def action: ActionType = ActionType.STATE
}

object StateLao extends Parsable {
  def apply(
             id: Hash,
             name: String,
             creation: Timestamp,
             last_modified: Timestamp,
             organizer: PublicKey,
             witnesses: List[PublicKey],
             modification_id: Hash,
             modification_signatures: List[WitnessSignaturePair]
           ): StateLao = {
    // FIXME add checks
    new StateLao(id, name, creation, last_modified, organizer, witnesses, modification_id, modification_signatures)
  }

  override def buildFromJson(messageData: MessageData, payload: String): StateLao =
    // TODO exception handling
    payload.parseJson.asJsObject.convertTo[StateLao]
}
