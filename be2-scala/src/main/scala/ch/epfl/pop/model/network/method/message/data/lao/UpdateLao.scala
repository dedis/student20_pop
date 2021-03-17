package ch.epfl.pop.model.network.method.message.data.lao

import ch.epfl.pop.json.HighLevelProtocol.updateLaoFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, PublicKey, Timestamp}
import spray.json._

case class UpdateLao(id: Hash, name: String, last_modified: Timestamp, witnesses: List[PublicKey]) extends MessageData {
  override def _object: ObjectType = ObjectType.LAO
  override def action: ActionType = ActionType.UPDATE_PROPERTIES
}

object UpdateLao extends Parsable {
  def apply(id: Hash, name: String, last_modified: Timestamp, witnesses: List[PublicKey]): UpdateLao = {
    // FIXME add checks
    new UpdateLao(id, name, last_modified, witnesses)
  }

  override def buildFromJson(messageData: MessageData, payload: String): UpdateLao =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[UpdateLao]
}
