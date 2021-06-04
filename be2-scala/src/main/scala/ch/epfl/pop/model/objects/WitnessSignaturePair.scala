package ch.epfl.pop.model.objects

case class WitnessSignaturePair(witness: PublicKey, signature: Signature) {
  def verify(messageId: Hash): Boolean = signature.verify(witness, messageId.asInstanceOf[Base64Data])
}
