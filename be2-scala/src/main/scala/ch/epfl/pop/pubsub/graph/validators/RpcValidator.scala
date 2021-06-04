package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.{JsonRpcRequest, JsonRpcResponse}
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}

object RpcValidator extends ContentValidator {
  final val JSON_RPC_VERSION: String = "2.0"

  def validateRpcRequest(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "RpcRequest")

    if (rpcMessage.jsonrpc != JSON_RPC_VERSION) {
      Right(validationError(s"json-rpc version should be '$JSON_RPC_VERSION' but is ${rpcMessage.jsonrpc}"))
    } else {
      Left(rpcMessage)
    }
  }

  def validateRpcResponse(rpcMessage: JsonRpcResponse): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "RpcResponse")

    if (rpcMessage.jsonrpc != JSON_RPC_VERSION) {
      Right(validationError(s"json-rpc version should be '$JSON_RPC_VERSION' but is ${rpcMessage.jsonrpc}"))
    } else {
      Left(rpcMessage)
    }
  }
}
