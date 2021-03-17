package ch.epfl.pop.model.network.method.message.data.lao

import ch.epfl.pop.json.HighLevelProtocol.createLaoFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, PublicKey, Timestamp}
import spray.json._

case class CreateLao(
                      id: Hash,
                      name: String,
                      creation: Timestamp,
                      organizer: PublicKey,
                      witnesses: List[PublicKey]
                    ) extends MessageData {
  override def _object: ObjectType = ObjectType.LAO
  override def action: ActionType = ActionType.CREATE
}

object CreateLao extends Parsable {
  def apply(id: Hash, name: String, creation: Timestamp, organizer: PublicKey, witnesses: List[PublicKey]): CreateLao = {
    // FIXME add checks
    new CreateLao(id, name, creation, organizer, witnesses)
  }

  override def buildFromJson(messageData: MessageData, payload: String): CreateLao = {
    // TODO exception handling
    payload.parseJson.asJsObject.convertTo[CreateLao]
  }
}
