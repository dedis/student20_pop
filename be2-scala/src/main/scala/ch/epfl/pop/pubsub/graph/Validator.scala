package ch.epfl.pop.pubsub.graph

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.model.network.method._
import ch.epfl.pop.model.network.requests.lao.{JsonRpcRequestCreateLao, JsonRpcRequestStateLao, JsonRpcRequestUpdateLao}
import ch.epfl.pop.model.network.requests.meeting.{JsonRpcRequestCreateMeeting, JsonRpcRequestStateMeeting}
import ch.epfl.pop.model.network.requests.rollCall.{JsonRpcRequestCloseRollCall, JsonRpcRequestCreateRollCall, JsonRpcRequestOpenRollCall, JsonRpcRequestReopenRollCall}
import ch.epfl.pop.model.network.requests.witness.JsonRpcRequestWitnessMessage
import ch.epfl.pop.model.network.{JsonRpcRequest, JsonRpcResponse}
import ch.epfl.pop.pubsub.graph.validators.LaoValidator._
import ch.epfl.pop.pubsub.graph.validators.MeetingValidator._
import ch.epfl.pop.pubsub.graph.validators.MessageValidator._
import ch.epfl.pop.pubsub.graph.validators.ParamsValidator._
import ch.epfl.pop.pubsub.graph.validators.RollCallValidator._
import ch.epfl.pop.pubsub.graph.validators.RpcValidator._
import ch.epfl.pop.pubsub.graph.validators.WitnessValidator._

object Validator {

  private val VALIDATOR_ERROR: PipelineError = PipelineError(
    ErrorCodes.INVALID_ACTION.id,
    "Unsupported action: Validator was given a message it could not recognize"
  )

  // FIXME implement schema
  def validateSchema(jsonString: JsonString): Either[JsonString, PipelineError] = Left(jsonString)

  private def validateJsonRpcContent(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequest) => validateRpcRequest(message)
      case message@(_: JsonRpcResponse) => validateRpcResponse(message)
      case _ => Right(VALIDATOR_ERROR)
    }
    case graphMessage@_ => graphMessage
  }

  private def validateMethodContent(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(jsonRpcRequest: JsonRpcRequest) => jsonRpcRequest.getParams match {
      case _: Broadcast => validateBroadcast(jsonRpcRequest)
      case _: Catchup => validateCatchup(jsonRpcRequest)
      case _: Publish => validatePublish(jsonRpcRequest)
      case _: Subscribe => validateSubscribe(jsonRpcRequest)
      case _: Unsubscribe => validateUnsubscribe(jsonRpcRequest)
      case _ => Right(VALIDATOR_ERROR)
    }
    case Left(_) => Right(PipelineError(
      ErrorCodes.SERVER_ERROR.id,
      "Unsupported action: MethodValidator was given a response message"
    ))
    case graphMessage@_ => graphMessage
  }

  private def validateMessageContent(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(jsonRpcRequest: JsonRpcRequest) => jsonRpcRequest.getParams match {
      case _: Broadcast => validateMessage(jsonRpcRequest)
      case _: Catchup => graphMessage
      case _: Publish => validateMessage(jsonRpcRequest)
      case _: Subscribe => graphMessage
      case _: Unsubscribe => graphMessage
      case _ => Right(VALIDATOR_ERROR)
    }
    case graphMessage@_ => graphMessage
  }

  def validateHighLevelMessage(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(_) => validateJsonRpcContent(graphMessage) match {
      case Left(_) => validateMethodContent(graphMessage) match {
        case Left(_) => validateMessageContent(graphMessage) match {
          case Left(_) => graphMessage
          case graphMessage@_ => graphMessage
        }
        case graphMessage@_ => graphMessage
      }
      case graphMessage@_ => graphMessage
    }
    case graphMessage@_ => graphMessage
  }

  def validateMessageDataContent(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(jsonRpcMessage) => jsonRpcMessage match {
      case message@(_: JsonRpcRequestCreateLao) => validateCreateLao(message)
      case message@(_: JsonRpcRequestStateLao) => validateStateLao(message)
      case message@(_: JsonRpcRequestUpdateLao) => validateUpdateLao(message)
      case message@(_: JsonRpcRequestCreateMeeting) => validateCreateMeeting(message)
      case message@(_: JsonRpcRequestStateMeeting) => validateStateMeeting(message)
      case message@(_: JsonRpcRequestCreateRollCall) => validateCreateRollCall(message)
      case message@(_: JsonRpcRequestOpenRollCall) => validateOpenRollCall(message)
      case message@(_: JsonRpcRequestReopenRollCall) => validateReopenRollCall(message)
      case message@(_: JsonRpcRequestCloseRollCall) => validateCloseRollCall(message)
      case message@(_: JsonRpcRequestWitnessMessage) => validateWitnessMessage(message)
      case _ => Right(VALIDATOR_ERROR)
    }
    case graphMessage@_ => graphMessage
  }


  // takes a string (json) input and compares it with the JsonSchema
  // /!\ Json Schema plugin?
  val schemaValidator: Flow[JsonString, Either[JsonString, PipelineError], NotUsed] = Flow[JsonString].map(validateSchema)

  // takes a JsonRpcMessage and validates input until Message layer
  val jsonRpcContentValidator: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map(validateHighLevelMessage)

  // validation from Message layer
  val messageContentValidator: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map(validateMessageDataContent)
}
