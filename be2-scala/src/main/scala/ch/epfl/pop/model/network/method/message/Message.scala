package ch.epfl.pop.model.network.method.message

import ch.epfl.pop.model.network.method.message.data.{MessageData, Parsable}
import ch.epfl.pop.model.objects.{Base64Data, Hash, PublicKey, Signature, WitnessSignaturePair}

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

  override def buildFromJson(messageData: MessageData, payload: String): Any = ???
}
