package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.meeting.{CreateMeeting, StateMeeting}
import ch.epfl.pop.model.objects.Hash
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}


case object MeetingValidator extends MessageDataContentValidator {
  def validateCreateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CreateMeeting")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CreateMeeting = message.decodedData.get.asInstanceOf[CreateMeeting]
        val expectedHash: Hash = Hash.fromStrings() // FIXME get id from db

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateTimestampStaleness(data.start)) {
          Right(validationError(s"stale 'start' timestamp (${data.start})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.creation, data.end.get)) {
          Right(validationError(s"'end' (${data.end.get}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  def validateStateMeeting(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "StateMeeting")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: StateMeeting = message.decodedData.get.asInstanceOf[StateMeeting]
        val expectedHash: Hash = Hash.fromStrings() // FIXME get id from db

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateTimestampOrder(data.creation, data.last_modified)) {
          Right(validationError(s"'last_modified' (${data.last_modified}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (!validateTimestampStaleness(data.start)) {
          Right(validationError(s"stale 'start' timestamp (${data.start})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.creation, data.end.get)) {
          Right(validationError(s"'end' (${data.end.get}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (data.end.isDefined && !validateTimestampOrder(data.start, data.end.get)) {
          Right(validationError(s"'end' (${data.end.get}) timestamp is younger than 'start' (${data.start})"))
        } else if (!validateWitnessSignatures(data.modification_signatures, data.modification_id)) {
          Right(validationError("witness key-signature pairs are not valid for the given modification_id"))
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }
}
