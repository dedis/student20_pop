package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.witness.WitnessMessage
import ch.epfl.pop.model.network.requests.witness.JsonRpcRequestWitnessMessage
import ch.epfl.pop.model.objects.{Hash, WitnessSignaturePair}
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

case object WitnessHandler extends MessageHandler {

  override val handler: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequestWitnessMessage) => handleWitnessMessage(message)
      case _ => Right(PipelineError(
        ErrorCodes.SERVER_ERROR.id,
        "Internal server fault: WitnessHandler was given a message it could not recognize"
      ))
    }
    case graphMessage@_ => graphMessage
  }

  def handleWitnessMessage(rpcMessage: JsonRpcRequest): GraphMessage = {
    val decodedData: WitnessMessage = rpcMessage.getDecodedData.get.asInstanceOf[WitnessMessage]
    val messageId: Hash = decodedData.message_id
    val ask = dbActor.ask(ref => DbActor.Read(rpcMessage.getParamsChannel, messageId, ref)).map {
      case Some(message: Message) =>
        val askWrite = dbActor.ask(ref => DbActor.Write(
          rpcMessage.getParamsChannel,
          message.addWitnessSignature(WitnessSignaturePair(rpcMessage.getParamsMessage.get.sender, decodedData.signature)),
          ref
        )).map {
          case true =>
            // FIXME propagate
            Left(rpcMessage)
          case _ => Right(PipelineError(-10, "")) // FIXME add DbActor "answers" with error description if failed
        }
        Await.result(askWrite, DbActor.getDuration)
      case None => Right(PipelineError(-10, "")) // FIXME add DbActor "answers" with error description if failed
    }
    Await.result(ask, DbActor.getDuration)
  }
}
