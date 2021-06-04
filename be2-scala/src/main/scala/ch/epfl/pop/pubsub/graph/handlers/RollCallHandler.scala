package ch.epfl.pop.pubsub.graph.handlers

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.requests.rollCall.{JsonRpcRequestCloseRollCall, JsonRpcRequestCreateRollCall, JsonRpcRequestOpenRollCall, JsonRpcRequestReopenRollCall}
import ch.epfl.pop.pubsub.graph.{ErrorCodes, GraphMessage, PipelineError}

case object RollCallHandler extends MessageHandler {

  override val handler: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequestCreateRollCall) => handleCreateRollCall(message)
      case message@(_: JsonRpcRequestOpenRollCall) => handleOpenRollCall(message)
      case message@(_: JsonRpcRequestReopenRollCall) => handleReopenRollCall(message)
      case message@(_: JsonRpcRequestCloseRollCall) => handleCloseRollCall(message)
      case _ => Right(PipelineError(
        ErrorCodes.SERVER_ERROR.id,
        "Internal server fault: RollCallHandler was given a message it could not recognize"
      ))
    }
    case graphMessage@_ => graphMessage
  }

  def handleCreateRollCall(rpcMessage: JsonRpcRequest): GraphMessage =
    rpcMessage.getParamsChannel.decodeSubChannel match {
      case Some(_) => dbAskWritePropagate(rpcMessage)
      case _ => Right(PipelineError(
        ErrorCodes.INVALID_DATA.id,
        s"Unable to create meeting: invalid encoded laoId '${rpcMessage.getParamsChannel}'"
      ))
    }
  def handleOpenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
  def handleReopenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
  def handleCloseRollCall(rpcMessage: JsonRpcRequest): GraphMessage = dbAskWritePropagate(rpcMessage)
}
