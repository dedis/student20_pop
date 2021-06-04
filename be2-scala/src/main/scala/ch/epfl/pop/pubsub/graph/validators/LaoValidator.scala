package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.model.network.JsonRpcRequest
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.lao.{CreateLao, StateLao, UpdateLao}
import ch.epfl.pop.model.objects.Hash
import ch.epfl.pop.pubsub.graph.{GraphMessage, PipelineError}


case object LaoValidator extends MessageDataContentValidator {
  def validateCreateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "CreateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: CreateLao = message.decodedData.get.asInstanceOf[CreateLao]
        val expectedHash: Hash = Hash.fromStrings(data.organizer.base64Data.decode(), data.creation.toString, data.name)

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateWitnesses(data.witnesses)) {
          Right(validationError("duplicate witnesses keys"))
        /* FIXME hash issues
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        */
        } else if (data.organizer != message.sender) {
          Right(validationError("unexpected organizer public key"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }

  def validateStateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "StateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: StateLao = message.decodedData.get.asInstanceOf[StateLao]
        val expectedHash: Hash = Hash.fromStrings(data.organizer.base64Data.decode(), data.creation.toString, data.name)

        if (!validateTimestampStaleness(data.creation)) {
          Right(validationError(s"stale 'creation' timestamp (${data.creation})"))
        } else if (!validateTimestampOrder(data.creation, data.last_modified)) {
          Right(validationError(s"'last_modified' (${data.last_modified}) timestamp is younger than 'creation' (${data.creation})"))
        } else if (!validateWitnesses(data.witnesses)) {
          Right(validationError("duplicate witnesses keys"))
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

  def validateUpdateLao(rpcMessage: JsonRpcRequest): GraphMessage = {
    def validationError(reason: String): PipelineError = super.validationError(reason, "UpdateLao")

    rpcMessage.getParamsMessage match {
      case Some(message: Message) =>
        val data: UpdateLao = message.decodedData.get.asInstanceOf[UpdateLao]
        val expectedHash: Hash = Hash.fromStrings() // FIXME get id from db

        if (!validateTimestampStaleness(data.last_modified)) {
          Right(validationError(s"stale 'last_modified' timestamp (${data.last_modified})"))
        } else if (!validateWitnesses(data.witnesses)) {
          Right(validationError("duplicate witnesses keys"))
        } else if (expectedHash != data.id) {
          Right(validationError("unexpected id"))
        } else {
          Left(rpcMessage)
        }
      case _ => Right(validationErrorNoMessage)
    }
  }
}
