package ch.epfl.pop.model.network

trait JsonRpcMessage {
  val jsonrpc: String
  // Note: Parsable enforced in companion objects
}
