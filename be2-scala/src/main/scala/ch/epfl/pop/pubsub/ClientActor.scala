package ch.epfl.pop.pubsub

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import ch.epfl.pop.model.objects.Channel
import ch.epfl.pop.pubsub.ClientActor._
import ch.epfl.pop.pubsub.PubSubMediator._
import ch.epfl.pop.pubsub.graph.GraphMessage

import scala.collection.mutable
import scala.util.Failure


case class ClientActor(mediator: ActorRef) extends Actor with ActorLogging {

  private var wsHandle: Option[ActorRef] = None
  private val subscribedChannels: mutable.Set[Channel] = mutable.Set.empty

  // called just after actor creation
  // override def preStart(): Unit = mediator ! Subscribe("topic", self) // FIXME topic

  private def messageWsHandle(event: ClientActorMessage): Unit = event match {
    case ClientAnswer(graphMessage) => wsHandle.fold(())(_ ! graphMessage)
  }

  override def receive: Receive = LoggingReceive {
    case message: Event => message match {
      case ConnectWsHandle(wsClient: ActorRef) =>
        log.info(s"Connecting wsHandle $wsClient to actor ${this.self}")
        wsHandle = Some(wsClient)
      case DisconnectWsHandle => subscribedChannels.foreach(channel => mediator ! UnsubscribeFrom(channel))
      case SubscribeTo(channel) => mediator ! SubscribeTo(channel)
      case UnsubscribeFrom(channel) => mediator ! UnsubscribeFrom(channel)
    }
    case message: PubSubMediatorMessage => message match {
      case SubscribeToAck(channel) =>
        log.info(s"Actor $self received ACK mediator $mediator for the subscribe to channel '$channel' request")
        subscribedChannels += channel
      case UnsubscribeFromAck(channel) =>
        log.info(s"Actor $self received ACK mediator $mediator for the unsubscribe from channel '$channel' request")
        subscribedChannels -= channel
      case SubscribeToNAck(channel, reason) =>
        log.info(s"Actor $self received NACK mediator $mediator for the subscribe to channel '$channel' request for reason: $reason")
      case UnsubscribeFromNAck(channel, reason) =>
        log.info(s"Actor $self received NACK mediator $mediator for the unsubscribe from channel '$channel' request for reason: $reason")
    }
    case clientAnswer@ClientAnswer(_) =>
      log.info(s"Sending an answer back to client $wsHandle: $clientAnswer")
      messageWsHandle(clientAnswer)

    case m@_ => m match {
      case Failure(exception: Exception) => println("FAILURE : " + m + exception.getMessage)
      case _ => println("UNKNOWN MESSAGE TO CLIENT ACTOR: " + m)
    }
  }
}

object ClientActor {
  def props(mediator: ActorRef): Props = Props(new ClientActor(mediator))

  sealed trait ClientActorMessage
  // answer to be sent to the client represented by the client actor
  final case class ClientAnswer(graphMessage: GraphMessage) extends ClientActorMessage


  sealed trait Event
  // connect the client actor with the front-end
  final case class ConnectWsHandle(wsClient: ActorRef) extends Event
  // unsubscribe from all channels
  final case object DisconnectWsHandle extends Event
  // subscribe to a particular channel
  final case class SubscribeTo(channel: Channel) extends Event
  // unsubscribe from a particular channel
  final case class UnsubscribeFrom(channel: Channel) extends Event
}

