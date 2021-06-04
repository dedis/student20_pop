package ch.epfl.pop.pubsub.graph.validators

import ch.epfl.pop.pubsub.graph.{ErrorCodes, PipelineError}

trait ContentValidator {
  /**
   * Creates a validation error message for reason <reason> that happened in
   * validator module <validator> with optional error code <errorCode>
   *
   * @param reason the reason of the validation error
   * @param validator validator module where the error occurred
   * @param errorCode error code related to the error
   * @return a description of the error and where it occurred
   */
  def validationError(reason: String, validator: String, errorCode: ErrorCodes.ErrorCodes = ErrorCodes.INVALID_DATA): PipelineError =
    PipelineError(errorCode.id, s"$validator content validation failed: $reason")
}
