package ch.epfl.pop.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import ch.epfl.pop.model.objects.Channel
import ch.epfl.pop.pubsub.ClientActor.{SubscribeTo, UnsubscribeFrom}
import ch.epfl.pop.pubsub.PubSubMediator.{SubscribeToAck, SubscribeToNAck, UnsubscribeFromAck, UnsubscribeFromNAck}

import scala.collection.mutable

class PubSubMediator extends Actor with ActorLogging {

  // Map from channels to the collection of actors subscribed to the channel
  private var channelMap: mutable.Map[Channel, mutable.Set[ActorRef]] = mutable.Map.empty

  override def receive: Receive = LoggingReceive {
    case SubscribeTo(channel) => channelMap.get(channel) match {
      case Some(set) =>
        if (set.contains(sender)) {
          val reason: String = s"Actor $sender was already subscribed to channel '$channel'"
          log.info(reason)
          sender ! SubscribeToNAck(channel, reason)
        } else {
          log.info(s"Subscribing $sender to channel '$channel'")
          set += sender
          sender ! SubscribeToAck(channel)
        }
      case _ =>
        log.info(s"Subscribing $sender to channel '$channel'")
        channelMap = channelMap + (channel -> mutable.Set(sender))
        sender ! SubscribeToAck(channel)
    }

    case UnsubscribeFrom(channel) => channelMap.get(channel) match {
      case Some(set) if set.contains(sender) =>
        log.info(s"Unsubscribing $sender from channel '$channel'")
        set -= sender
        sender ! UnsubscribeFromAck(channel)
      case Some(_) =>
        sender ! UnsubscribeFromNAck(channel, s"Actor $sender is not subscribed to channel '$channel'")
      case _ =>
        sender ! UnsubscribeFromNAck(channel, s"Channel '$channel' does not exist in the system")
    }
  }
}

object PubSubMediator {
  def props: Props = Props(new PubSubMediator())

  sealed trait PubSubMediatorMessage
  // subscribe confirmation (sender successfully subscribed to channel channel)
  final case class SubscribeToAck(channel: Channel) extends PubSubMediatorMessage
  // subscribe confirmation (sender successfully unsubscribed from channel channel)
  final case class UnsubscribeFromAck(channel: Channel) extends PubSubMediatorMessage
  // unsubscribe infirmation (sender failed to subscribe to channel channel)
  final case class SubscribeToNAck(channel: Channel, reason: String) extends PubSubMediatorMessage
  // unsubscribe infirmation (sender failed to unsubscribe from channel channel)
  final case class UnsubscribeFromNAck(channel: Channel, reason: String) extends PubSubMediatorMessage
}
