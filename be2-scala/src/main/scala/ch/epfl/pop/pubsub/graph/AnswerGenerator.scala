package ch.epfl.pop.pubsub.graph

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network._
import ch.epfl.pop.pubsub.graph.validators.RpcValidator

object AnswerGenerator {

  def generateAnswer(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    // Note: the output message (if successful) is an answer
    // The standard output is always a JsonMessage (pipeline errors are transformed into negative answers)
    case Left(rpcRequest: JsonRpcRequest) => rpcRequest.method match {
      // FIXME catchup has non int result
      case MethodType.CATCHUP => Left(JsonRpcResponse(
        RpcValidator.JSON_RPC_VERSION, ???, None, rpcRequest.id
      ));
      // FIXME propagate has no answer
      case MethodType.BROADCAST => ???
      // Standard answer. Result == 0
      case _ => Left(JsonRpcResponse(
        RpcValidator.JSON_RPC_VERSION, Some(new ResultObject(0)), None, rpcRequest.id
      ))
    }

    case Right(pipelineError: PipelineError) => Left(JsonRpcResponse(
      RpcValidator.JSON_RPC_VERSION,
      None,
      Some(ErrorObject(pipelineError.code, pipelineError.description)),
      ???
    ))

    // /!\ If something is outputted as Right(...), then there's a mistake somewhere in the graph!
    case _ => Right(PipelineError(
      ErrorCodes.SERVER_ERROR.id,
      s"Internal server error: unknown reason. The MessageEncoder could not decide what to do with input $graphMessage"
    ))
  }

  val generator: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map(generateAnswer)
}
