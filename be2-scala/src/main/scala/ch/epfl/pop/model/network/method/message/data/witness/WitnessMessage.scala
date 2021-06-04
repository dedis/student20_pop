package ch.epfl.pop.model.network.method.message.data.witness

import ch.epfl.pop.json.MessageDataProtocol._
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
  override val _object: ObjectType = ObjectType.MESSAGE
  override val action: ActionType = ActionType.WITNESS
}

object WitnessMessage extends Parsable {
  def apply(
             message_id: Hash,
             signature: Signature,
           ): WitnessMessage = {
    new WitnessMessage(message_id, signature)
  }

  override def buildFromJson(payload: String): WitnessMessage = payload.parseJson.asJsObject.convertTo[WitnessMessage]
}
