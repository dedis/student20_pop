package ch.epfl.pop.pubsub.graph

import akka.NotUsed
import akka.stream.scaladsl.Flow
import ch.epfl.pop.json.HighLevelProtocol._
import ch.epfl.pop.json.MessageDataProtocol._
import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data._
import ch.epfl.pop.model.network.requests.lao.{JsonRpcRequestCreateLao, JsonRpcRequestStateLao, JsonRpcRequestUpdateLao}
import ch.epfl.pop.model.network.requests.meeting.{JsonRpcRequestCreateMeeting, JsonRpcRequestStateMeeting}
import ch.epfl.pop.model.network.requests.rollCall.{JsonRpcRequestCloseRollCall, JsonRpcRequestCreateRollCall, JsonRpcRequestOpenRollCall, JsonRpcRequestReopenRollCall}
import ch.epfl.pop.model.network.requests.witness.JsonRpcRequestWitnessMessage
import ch.epfl.pop.model.network.{JsonRpcRequest, JsonRpcResponse}
import spray.json._

import scala.util.{Failure, Success, Try}

object MessageDecoder {

  /**
   * Graph component: takes a string as input and parses it into a JsonRpcMessage
   * (stored into a GraphMessage)
   */
  val jsonRpcParser: Flow[Either[JsonString, PipelineError], GraphMessage, NotUsed] = Flow[Either[JsonString, PipelineError]].map {
    case Left(jsonString) => Try(jsonString.parseJson.asJsObject) match {
      case Success(obj) =>
        val fields: Set[String] = obj.fields.keySet

        if (fields.contains("method")) {
          Left(obj.convertTo[JsonRpcRequest])
        } else {
          Left(obj.convertTo[JsonRpcResponse])
        }
      case _ => Right(PipelineError(
        ErrorCodes.INVALID_DATA.id,
        "MessageDecoder parsing failed : input json is not correctly formatted"
      ))
    }

    case Right(pipelineError) => Right(pipelineError) // implicit typecasting
  }

  /**
   * Graph component: takes a GraphMessage and parses the 'data' field of any JsonRpcRequest
   * query containing a message. The result is stored in the input JsonRpcRequest's Message's
   * decodedData field
   */
  val dataParser: Flow[GraphMessage, GraphMessage, NotUsed] = Flow[GraphMessage].map(parseData)


  /**
   * Decodes the JsonRpcRequest's Message 'data' field
   *
   * @param _object object of the targeted MessageData
   * @param action action of the targeted MessageData
   * @return the decoded 'data' field as a subclass of MessageData
   */
  @throws(classOf[ProtocolException])
  private def parseMessageData(dataString: String, _object: ObjectType, action: ActionType): MessageData =
    DataBuilder.buildData(_object, action, dataString)

  /**
   * Type casts a general JsonRpcRequest <request> into a typed request (model/network/requests)
   *
   * @param request JsonRpcRequest to be type casted
   * @throws java.lang.IllegalArgumentException if the data header (object and action) is either missing
   *                                            or is an invalid combination
   * @return a type casted JsonRpcRequest
   */
  @throws(classOf[IllegalArgumentException])
  private def typeCastRequest(request: JsonRpcRequest): JsonRpcRequest = request.getDecodedData match {
    case Some(data) => (data._object, data.action) match {
      case (ObjectType.LAO, ActionType.CREATE) => request.toTypedRequest(JsonRpcRequestCreateLao)
      case (ObjectType.LAO, ActionType.STATE) => request.toTypedRequest(JsonRpcRequestStateLao)
      case (ObjectType.LAO, ActionType.UPDATE_PROPERTIES) => request.toTypedRequest(JsonRpcRequestUpdateLao)
      case (ObjectType.MEETING, ActionType.CREATE) => request.toTypedRequest(JsonRpcRequestCreateMeeting)
      case (ObjectType.MEETING, ActionType.STATE) => request.toTypedRequest(JsonRpcRequestStateMeeting)
      case (ObjectType.ROLL_CALL, ActionType.CLOSE) => request.toTypedRequest(JsonRpcRequestCloseRollCall)
      case (ObjectType.ROLL_CALL, ActionType.CREATE) => request.toTypedRequest(JsonRpcRequestCreateRollCall)
      case (ObjectType.ROLL_CALL, ActionType.OPEN) => request.toTypedRequest(JsonRpcRequestOpenRollCall)
      case (ObjectType.ROLL_CALL, ActionType.REOPEN) => request.toTypedRequest(JsonRpcRequestReopenRollCall)
      case (ObjectType.MESSAGE, ActionType.WITNESS) => request.toTypedRequest(JsonRpcRequestWitnessMessage)
      case _ => throw new IllegalArgumentException(s"Illegal ('object'/'action') = (${data._object}/${data.action}) combination")
    }
    case _ => throw new IllegalArgumentException(s"Unable to infer type of JsonRpcRequest (decoded 'data' field is missing)")
  }


  /**
   * Decides whether a graph message 'data' field should be decoded or not.
   * If yes, the 'data' field is decoded
   *
   * @param graphMessage graph message that should be further decoded
   * @return the upgraded graph message (with 'data' field decoded) or an error
   */
  def parseData(graphMessage: GraphMessage): GraphMessage = graphMessage match {
    case Left(jsonRpcRequest: JsonRpcRequest) => jsonRpcRequest.getDecodedData match {
      case Some(_) => graphMessage // do nothing if 'data' already decoded
      case _ if !jsonRpcRequest.hasParamsMessage => graphMessage // do nothing if rpc-message doesn't contain any message
      case _ =>
        // json string representation of the 'data' field
        val jsonString: String = jsonRpcRequest.getEncodedData.get.decode()

        // Try to extract data header from the json string
        Try(jsonString.parseJson.asJsObject.getFields("object", "action")) match {

          // if the header is correct (both 'object' and 'action' are present and both strings)
          case Success(Seq(objectString@JsString(_), actionString@JsString(_))) =>
            var typedRequest = jsonRpcRequest // filler for the typed request

            Try {
              val messageData = parseMessageData(jsonString, objectString.convertTo[ObjectType], actionString.convertTo[ActionType])
              jsonRpcRequest.setDecodedData(messageData)
              typedRequest = typeCastRequest(jsonRpcRequest)
            } match {
              case Success(_) => Left(typedRequest) // everything worked at expected, 'decodedData' field was populated
              case Failure(exception) => Right(PipelineError(ErrorCodes.INVALID_DATA.id, s"Invalid data: $exception"))
            }

          case Success(_) => Right(PipelineError(
            ErrorCodes.INVALID_DATA.id,
            "Invalid data: Unable to parse 'data' field: 'object' or 'action' field is missing/wrongly formatted"
          ))
          case _ => Right(PipelineError(
            ErrorCodes.INVALID_DATA.id,
            "Invalid data: Unable to parse 'data' field: 'data' is not a valid json string"
          ))
        }
    }
    case graphMessage@_ => graphMessage
  }
}
