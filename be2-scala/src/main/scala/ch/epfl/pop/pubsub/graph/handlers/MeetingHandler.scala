package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.data.meeting.StateMeeting
import ch.epfl.pop.model.network.requests.meeting.{JsonRpcRequestCreateMeeting, JsonRpcRequestStateMeeting}
import ch.epfl.pop.model.objects.Hash
import ch.epfl.pop.pubsub.graph.{DbActor, ErrorCodes, GraphMessage, PipelineError}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

case object MeetingHandler extends MessageHandler {

  override val handler: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequestCreateMeeting) => handleCreateMeeting(message)
      case message@(_: JsonRpcRequestStateMeeting) => handleStateMeeting(message)
      case _ => Right(PipelineError(
        ErrorCodes.SERVER_ERROR.id,
        "Internal server fault: MeetingHandler was given a message it could not recognize"
      ))
    }
    case graphMessage@_ => graphMessage
  }

  def handleCreateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    rpcMessage.getParamsChannel.decodeSubChannel match {
      case Some(_) => dbAskWritePropagate(rpcMessage)
      case _ => Right(PipelineError(
        ErrorCodes.INVALID_DATA.id,
        s"Unable to create meeting: invalid encoded laoId '${rpcMessage.getParamsChannel}'"
      ))
    }
  }

  def handleStateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    val modificationId: Hash = rpcMessage.getDecodedData.asInstanceOf[StateMeeting].modification_id
    val ask = dbActor.ask(ref => DbActor.Read(rpcMessage.getParamsChannel, modificationId, ref)).map {
      case Some(_) => dbAskWritePropagate(rpcMessage)
      // TODO careful about asynchrony and the fact that the network may reorder some messages
      case _ => Right(PipelineError(
        ErrorCodes.INVALID_DATA.id,
        s"Unable to request meeting state: invalid modification_id '$modificationId' (no message associated to this id)"
      ))
    }
    Await.result(ask, DbActor.getDuration)
  }
}
