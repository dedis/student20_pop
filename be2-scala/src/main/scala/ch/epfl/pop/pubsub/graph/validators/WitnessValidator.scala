package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}


case object WitnessValidator extends MessageDataContentValidator {
  def validateWitnessMessage(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "WitnessMessage")

    rpcMessage.getParamsMessage match {
      case Some(_) => Left(rpcMessage)
      case _ => Right(validationErrorNoMessage)
    }
  }
}
