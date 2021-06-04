package ch.epfl.pop.pubsub.graph

final case class PipelineError(code: Int, description: String)

object ErrorCodes extends Enumeration {
  type ErrorCodes = Value

  // invalid action
  val INVALID_ACTION: ErrorCodes = Value(-1, "invalid action")
  // invalid resource (e.g. channel does not exist, channel was not subscribed to, etc.)
  val INVALID_RESOURCE: ErrorCodes = Value(-2, "invalid resource")
  // resource already exists (e.g. lao already exists, channel already exists, etc.)
  val ALREADY_EXISTS: ErrorCodes = Value(-3, "resource already exists")
  // request data is invalid (e.g. message is invalid)
  val INVALID_DATA: ErrorCodes = Value(-4, "request data is invalid")
  // access denied (e.g. subscribing to a “restricted” channel)
  val ACCESS_DENIED: ErrorCodes = Value(-5, "access denied")
  // internal server error (e.g. crashed while processing, database unavailable, etc.)
  val SERVER_ERROR: ErrorCodes = Value(-6, "internal server error")
}
