package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}

object ParamsValidator extends MethodContentValidator {

  final def validationError(reason: String): PipelineError = super.validationError(reason, "MethodValidator")

  private def validateGeneralParams(rpcMessage: JsonRpcRequest): GraphMessage = {
    if (!validateChannel(rpcMessage.getParamsChannel)) {
      Right(ParamsValidator.validationError("Channel validation failed"))
    } else {
      Left(rpcMessage)
    }
  }

  def validateBroadcast(rpcMessage: JsonRpcRequest): GraphMessage = validateGeneralParams(rpcMessage)
  def validateCatchup(rpcMessage: JsonRpcRequest): GraphMessage = validateGeneralParams(rpcMessage)
  def validatePublish(rpcMessage: JsonRpcRequest): GraphMessage = validateGeneralParams(rpcMessage)
  def validateSubscribe(rpcMessage: JsonRpcRequest): GraphMessage = validateGeneralParams(rpcMessage)
  def validateUnsubscribe(rpcMessage: JsonRpcRequest): GraphMessage = validateGeneralParams(rpcMessage)
}
