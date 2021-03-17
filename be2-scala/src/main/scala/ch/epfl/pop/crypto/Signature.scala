package ch.epfl.pop.crypto

import ch.epfl.pop.json.{Base64String, Key, SignatureJson}
import com.google.crypto.tink.subtle.Ed25519Verify

import java.security.GeneralSecurityException
import java.util.Base64
import java.nio.charset.StandardCharsets.UTF_8

object Signature {

  /**
   * Verify if a signature is correct.
   *
   * @param msg       the message to verify, encoded in base64
   * @param signature the signature corresponding to the message
   * @param key       the public key used for verification
   * @return whether the signature is correct
   */
  def verify(msg: Base64String, signature: SignatureJson, key: Key): Boolean = {
    val ed = new Ed25519Verify(key)
    val msgDecoded = Base64.getDecoder.decode(msg.getBytes(UTF_8))

    try {
      ed.verify(signature, msgDecoded)
      true
    }
    catch {
      case _ : GeneralSecurityException => false
    }
  }
}
