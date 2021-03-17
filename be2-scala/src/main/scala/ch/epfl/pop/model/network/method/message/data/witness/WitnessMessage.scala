package ch.epfl.pop.model.network.method.message.data.witness

import ch.epfl.pop.json.HighLevelProtocol.witnessMessageFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType}
import ch.epfl.pop.model.objects.{Hash, Signature}
import spray.json._

case class WitnessMessage(
                           message_id: Hash,
                           signature: Signature,
                         ) extends MessageData {
  override def _object: ObjectType = ObjectType.MESSAGE
  override def action: ActionType = ActionType.WITNESS
}

object WitnessMessage extends Parsable {
  def apply(
             message_id: Hash,
             signature: Signature,
           ): WitnessMessage = {
    // FIXME add checks
    new WitnessMessage(message_id, signature)
  }

  override def buildFromJson(messageData: MessageData, payload: String): WitnessMessage =
    // TODO exception handling
    payload.parseJson.asJsObject.convertTo[WitnessMessage]
}
