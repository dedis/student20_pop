package ch.epfl.pop.model.objects

import com.google.crypto.tink.subtle.Ed25519Verify

import scala.util.{Success, Try}

case class Signature(signature: Base64Data) {
  def verify(key: PublicKey, message: Base64Data): Boolean = {
    val ed = new Ed25519Verify(key.getBytes)

    Try(ed.verify(this.getBytes, message.getBytes)) match {
      case Success(_) => true
      case _ => false
    }
  }

  def getBytes: Array[Byte] = signature.data.getBytes
}
