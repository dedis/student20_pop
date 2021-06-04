package ch.epfl.pop.model.network

case class JsonRpcResponse(
                           jsonrpc: String,
                           result: Option[ResultObject],
                           error: Option[ErrorObject],
                           id: Option[Int]
                         ) extends JsonRpcMessage {
  def isPositive: Boolean = result.isDefined
}

object JsonRpcResponse extends Parsable {
  def apply(
             jsonrpc: String,
             result: Option[ResultObject],
             error: Option[ErrorObject],
             id: Option[Int]
           ): JsonRpcResponse = {
    new JsonRpcResponse(jsonrpc, result, error, id)
  }

  override def buildFromJson(payload: String): JsonRpcResponse = ???
}
