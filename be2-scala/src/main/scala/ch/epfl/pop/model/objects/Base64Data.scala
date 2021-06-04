package ch.epfl.pop.model.objects

import java.nio.charset.StandardCharsets
import java.util.Base64

case class Base64Data(data: String) {
  val DECODER: Base64.Decoder = Base64.getDecoder // FIXME getUrlDecoder

  def decode(): String = DECODER.decode(this.getBytes).map(_.toChar).mkString

  def getBytes: Array[Byte] = data.getBytes(StandardCharsets.UTF_8)

  def equals(that: Base64Data): Boolean = data == that.data
}

object Base64Data {
  val ENCODER: Base64.Encoder = Base64.getEncoder // FIXME getUrlEncoder

  def encode(data: String): Base64Data = Base64Data(ENCODER.encode(data.getBytes(StandardCharsets.UTF_8)).map(_.toChar).mkString)
}
