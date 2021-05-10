package ch.epfl.pop.model.network

import ch.epfl.pop.model.network.method.message.data.MessageData

case class JsonRpcResponse(
                           jsonrpc: String,
                           result: Option[Any], // FIXME
                           error: Option[ErrorObject],
                           id: Int
                         ) extends JsonRpcMessage

object JsonRpcResponse extends Parsable {
  def apply(
             jsonrpc: String,
             result: Option[Any], // FIXME
             error: Option[ErrorObject],
             id: Int
           ): JsonRpcResponse = {
    // FIXME add checks
    new JsonRpcResponse(jsonrpc, result, error, id)
  }

  override def buildFromJson(messageData: MessageData, payload: String): JsonRpcResponse = ???
}