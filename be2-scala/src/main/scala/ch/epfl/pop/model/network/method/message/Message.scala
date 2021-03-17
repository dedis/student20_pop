package ch.epfl.pop.model.network.method.message

import ch.epfl.pop.json.HighLevelProtocol.messageFormat
import ch.epfl.pop.model.network.Parsable
import ch.epfl.pop.model.network.method.message.data.MessageData
import ch.epfl.pop.model.objects.{Base64Data, Hash, PublicKey, Signature, WitnessSignaturePair}
import spray.json._

case class Message(
                    data: Base64Data,
                    sender: PublicKey,
                    signature: Signature,
                    message_id: Hash,
                    witness_signatures: List[WitnessSignaturePair],
                    decodedData: MessageData
                  )

object Message extends Parsable {
  def apply(
             data: Base64Data,
             sender: PublicKey,
             signature: Signature,
             message_id: Hash,
             witness_signatures: List[WitnessSignaturePair]
           ): Message = {
    // FIXME add checks
    new Message(data, sender, signature, message_id, witness_signatures, ???)
  }

  override def buildFromJson(messageData: MessageData, payload: String): Message =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[Message]
}
