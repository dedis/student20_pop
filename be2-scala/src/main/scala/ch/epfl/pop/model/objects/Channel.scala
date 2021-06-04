package ch.epfl.pop.model.objects

import java.util.Base64

import scala.util.{Success, Try}

final case class Channel(channel: String) {

  /**
   * Extract the sub-channel (laoId) from a channel name
   *
   * @return an array of bytes corresponding to the decoded sub-channel name or None if an error occurred
   */
  def decodeSubChannel: Option[Array[Byte]] = channel match {
    case _ if channel.startsWith(Channel.rootChannelPrefix) =>
      Try(Base64.getUrlDecoder.decode(channel.substring(Channel.rootChannelPrefix.length).getBytes)) match {
        case Success(value) => Some(value)
        case _ => None
      }
    case _ => None
  }

  def isRootChannel: Boolean = channel == Channel.rootChannel

  def isSubChannel: Boolean = channel.startsWith(Channel.rootChannelPrefix)

  override def equals(that: Any): Boolean = that match {
    case that: Channel => channel == that.channel
    case _ => false
  }
}

object Channel {
  val rootChannel: String = "/root"
  val rootChannelPrefix: String = "/root/"
}
