package ch.epfl.pop.crypto

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import ch.epfl.pop.json.{Base64String, HashJson, Key, SignatureJson}

import java.util.Base64

object Hash {

  /**
   * Compute the id of the message, which is the Hash(msg||signature)
   * @param msg the message in base64
   * @param signature the signature of the message
   * @return the id of the message
   */
  def computeMessageId(msg: Base64String, signature: SignatureJson): HashJson = {
    val data = arrayRepr(msg, Base64.getEncoder.encodeToString(signature))
    val id = getMessageDigest().digest(data.getBytes(StandardCharsets.UTF_8))
    id
  }

  /**
   * Compute the LAO id, which is Hash(organizer||creation||name)
   * @param organizer the organizer of the LAO
   * @param creation the creation timestamp of the LAO
   * @param name the name of the LAO
   * @return the id of the LAO
   */
  def computeLAOId(organizer: Key, creation: Long, name: String): HashJson = {
    val data = arrayRepr(Base64.getEncoder.encodeToString(organizer), creation.toString, name)
    val id = getMessageDigest().digest(data.getBytes(StandardCharsets.UTF_8))
    id
  }


  /**
   * Compute the meeting id, which is Hash('M'||laoId||creation||name)
   * @param laoID the id of the LAO
   * @param creation the creation timestamp of the meeting
   * @param name the name of the meeting
   * @return the id of the meeting
   */
  def computeMeetingId(laoID: HashJson, creation: Long, name: String): HashJson = computeGenericId("M", laoID, creation, name)

  /**
   * Compute the roll-call id, which is Hash('R'||laoId||creation||name)
   * @param laoID the id of the LAO
   * @param creation the creation timestamp of the roll-call
   * @param name the name of the roll-call
   * @return the id of the roll-call
   */
  def computeRollCallId(laoID: HashJson, creation: Long, name: String): HashJson = computeGenericId("R", laoID, creation, name)


  private def computeGenericId(typ: String, a: Array[Byte], l: Long, s: String): HashJson = {
    val data = arrayRepr(typ, Base64.getEncoder.encodeToString(a), l.toString, s)
    val id = getMessageDigest().digest(data.getBytes(StandardCharsets.UTF_8))
    id
  }

  private def arrayRepr(strings: String*): String =
    "[" + strings.tail.foldLeft(escapeAndQuote(strings.head))((acc, s) => acc + "," + escapeAndQuote(s)) + "]"

  private def escapeAndQuote(s: String): String = '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"'

  private def getMessageDigest() = MessageDigest.getInstance("SHA-256")

}
