package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.rollCall.{CloseRollCall, CreateRollCall, OpenRollCall, ReopenRollCall}
import ch.epfl.pop.model.objects.Hash
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}


case object RollCallValidator extends MessageDataContentValidator {
  // TODO the roll call validator checks with the old roll call specs!
  def validateCreateRollCall(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CreateRollCall")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CreateRollCall = message.decodedData.get.asInstanceOf[CreateRollCall]
        val expectedHash: Hash = Hash.fromStrings() // FIXME get id from db

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if ((data.start.isDefined && data.scheduled.isDefined) || (data.start.isEmpty && data.scheduled.isEmpty)) {
          Right(validationError(s"the message should include either 'start' or 'scheduled' values, but not both"))
        } else if (data.start.isDefined && !validateTimestampOrder(data.creation, data.start.get)) {
          Right(validationError(s"'start' (${data.start.get}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (data.scheduled.isDefined && !validateTimestampOrder(data.creation, data.scheduled.get)) {
          Right(validationError(s"'scheduled' (${data.scheduled.get}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  // TODO check that this is correct (correct validations)
  def validateOpenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "OpenRollCall")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: OpenRollCall = message.decodedData.get.asInstanceOf[OpenRollCall]

        if (!validateTimestampStaleness(data.start)) {
          Right(validationError(s"stale 'start' timestamp (${data.start})"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  // TODO check that this is correct (correct validations)
  def validateReopenRollCall(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "ReopenRollCall")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: ReopenRollCall = message.decodedData.get.asInstanceOf[ReopenRollCall]

        if (!validateTimestampStaleness(data.start)) {
          Right(validationError(s"stale 'start' timestamp (${data.start})"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  // TODO check that this is correct (correct validations)
  def validateCloseRollCall(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CloseRollCall")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CloseRollCall = message.decodedData.get.asInstanceOf[CloseRollCall]

        if (!validateTimestampStaleness(data.end)) {
          Right(validationError(s"stale 'end' timestamp (${data.end})"))
        } else if (data.attendees.size != data.attendees.toSet.size) {
          Right(validationError("duplicate attendees keys"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }
}
