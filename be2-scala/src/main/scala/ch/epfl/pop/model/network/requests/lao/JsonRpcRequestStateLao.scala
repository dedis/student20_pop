package ch.epfl.pop.model.network.requests.lao

import ch.epfl.pop.model.network.{JsonRpcRequest, MethodType}
import ch.epfl.pop.model.network.method.Params

final case class JsonRpcRequestStateLao(
                                         override val jsonrpc: String,
                                         override val method: MethodType.MethodType,
                                         override val params: Params,
                                         override val id: Option[Int]
                                        ) extends JsonRpcRequest(jsonrpc, method, params, id)