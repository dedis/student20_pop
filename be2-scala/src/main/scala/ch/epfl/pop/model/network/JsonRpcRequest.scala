package ch.epfl.pop.model.network
import ch.epfl.pop.model.network.method.Params
import ch.epfl.pop.model.network.method.message.data.MessageData
import spray.json._
import DefaultJsonProtocol._
import ch.epfl.pop.json.Protocol.jsonRpcRequestFormat

case class JsonRpcRequest(
                           jsonrpc: String,
                           method: Method.Method,
                           params: Params,
                           id: Option[Int]
                         ) extends JsonRpcMessage

object JsonRpcRequest extends Parsable {
  def apply(
             jsonrpc: String,
             method: Method.Method,
             params: Params,
             id: Option[Int]
           ): JsonRpcRequest = {
    // FIXME add checks
    new JsonRpcRequest(jsonrpc, method, params, id)
  }

  override def buildFromJson(messageData: MessageData, payload: String): JsonRpcRequest =
  // TODO exception handling
    payload.parseJson.asJsObject.convertTo[JsonRpcRequest]
}
