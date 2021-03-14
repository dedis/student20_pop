package ch.epfl.pop.model.network.method.message.data.witness

import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.{ActionType, MessageData, ObjectType, Parsable}
import ch.epfl.pop.model.objects.{Hash, Signature}

case class WitnessMessage(
                           message_id: Hash,
                           signature: Signature,
                         ) extends MessageData {
  override val _object: ObjectType = ObjectType.MESSAGE
  override val action: ActionType = ActionType.WITNESS
}

object WitnessMessage extends Parsable {
  def apply(
             message_id: Hash,
             signature: Signature,
           ): WitnessMessage = {
    // FIXME add checks
    new WitnessMessage(message_id, signature)
  }

  override def buildFromJson(messageData: MessageData, payload: String): WitnessMessage = ???
}
