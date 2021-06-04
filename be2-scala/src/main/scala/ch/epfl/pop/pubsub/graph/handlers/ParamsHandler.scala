package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.actor.ActorRef
import akka.stream.FlowShape
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import ch.epfl.pop.model.network.method.{Catchup, Subscribe, Unsubscribe}
import ch.epfl.pop.pubsub.ClientActor
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}

object ParamsHandler {
  def graph(clientActorRef: ActorRef): Flow[GraphMessage, GraphMessage, NotUsed] = Flow.fromGraph(GraphDSL.create() {
    implicit builder: GraphDSL.Builder[NotUsed] => {
      import GraphDSL.Implicits._

      /* partitioner port numbers */
      val portPipelineError = 0
      val portSubscribe = 1
      val portUnsubscribe = 2
      val portCatchup = 3
      val totalPorts = 4

      /* building blocks */
      val handlerPartitioner = builder.add(Partition[GraphMessage](totalPorts, {
        case Left(jsonRpcMessage) => jsonRpcMessage match {
          case _: Subscribe => portSubscribe
          case _: Unsubscribe => portUnsubscribe
          case _: Catchup => portCatchup
        }
        case _ => portPipelineError // Pipeline error goes directly in handlerMerger
      }))

      val subscribeHandler = builder.add(ParamsHandler.subscribeHandler(clientActorRef))
      val unsubscribeHandler = builder.add(ParamsHandler.unsubscribeHandler(clientActorRef))
      val catchupHandler = builder.add(ParamsHandler.catchupHandler(clientActorRef))

      val handlerMerger = builder.add(Merge[GraphMessage](totalPorts))

      /* glue the components together */
      handlerPartitioner.out(portPipelineError) ~> handlerMerger
      handlerPartitioner.out(portSubscribe) ~> subscribeHandler ~> handlerMerger
      handlerPartitioner.out(portUnsubscribe) ~> unsubscribeHandler ~> handlerMerger
      handlerPartitioner.out(portCatchup) ~> catchupHandler ~> handlerMerger

      /* close the shape */
      FlowShape(handlerPartitioner.in, handlerMerger.out)
    }
  })

  case class Asking(g: GraphMessage, replyTo: ActorRef)

  def subscribeHandler(clientActorRef: ActorRef): Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage: Subscribe) =>
      // ActorFlow.ask(clientActorRef)(makeMessage = (el, replyTo: ActorRef) => SubscribeTo(channel))
      clientActorRef ! ClientActor.SubscribeTo(jsonRpcMessage.channel)
      Right(PipelineError(-100, "FIXME: should use akka ask pattern to create an ActorFlow"))
    case graphMessage@_ => graphMessage
  }

  def unsubscribeHandler(clientActorRef: ActorRef): Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage: Unsubscribe) =>
      clientActorRef ! ClientActor.UnsubscribeFrom(jsonRpcMessage.channel)
      Right(PipelineError(-100, "FIXME: should use akka ask pattern to create an ActorFlow"))
    case graphMessage@_ => graphMessage
  }

  def catchupHandler(clientActorRef: ActorRef): Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    // case Left(jsonRpcMessage: Catchup) => clientActorRef ! ClientActor.CatchupChannel(jsonRpcMessage.channel)
    case graphMessage@_ => graphMessage // FIXME catchup
  }
}
