package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.pattern.AskableActorRef
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

trait MessageHandler {
  implicit lazy val dbActor: AskableActorRef = DbActor.getInstance
  implicit lazy val timeout: Timeout = DbActor.getTimeout

  val handler: Flow[GraphMessage, GraphMessage, NotUsed]

  def dbAskWritePropagate(rpcMessage: JsonRpcRequest): GraphMessage = {
    val paramsMessage: Option[Message] = rpcMessage.getParamsMessage
    paramsMessage match {
      case Some(message) =>
        val ask = dbActor.ask(ref => DbActor.Write(rpcMessage.getParamsChannel, message, ref)).map {
          case true =>
            // FIXME propagate
            Left(rpcMessage)
          case _ => Right(PipelineError(-10, "")) // FIXME add DbActor "answers" with error description if failed
        }
        Await.result(ask, DbActor.getDuration)
      case _ => Right(PipelineError(ErrorCodes.INVALID_DATA.id, s"RPC-params does not contain any message"))
    }
  }
}
