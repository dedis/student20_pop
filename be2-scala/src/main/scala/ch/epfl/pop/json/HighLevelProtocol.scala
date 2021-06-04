package ch.epfl.pop.json

import ch.epfl.pop.json.ObjectProtocol._
import ch.epfl.pop.model.network.MethodType.MethodType
import ch.epfl.pop.model.network.method._
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network._
import ch.epfl.pop.model.objects._
import spray.json._

import scala.collection.immutable.ListMap

object HighLevelProtocol extends DefaultJsonProtocol {

  // ----------------------------------- ENUM FORMATTERS ----------------------------------- //
  implicit object methodTypeFormat extends RootJsonFormat[MethodType] {
    override def read(json: JsValue): MethodType = json match {
      case JsString(method) => MethodType.unapply(method).getOrElse(MethodType.INVALID)
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a MethodType")
    }

    override def write(obj: MethodType): JsValue = JsString(obj.toString)
  }


  // ----------------------------------- HIGH-LEVEL FORMAT ----------------------------------- //
  implicit object messageFormat extends RootJsonFormat[Message] {
    final private val PARAM_DATA: String = "data"
    final private val PARAM_SENDER: String = "sender"
    final private val PARAM_SIGNATURE: String = "signature"
    final private val PARAM_MESSAGE_ID: String = "message_id"
    final private val PARAM_WITNESS_SIG: String = "witness_signatures"

    override def read(json: JsValue): Message =
      json.asJsObject.getFields(PARAM_DATA, PARAM_SENDER, PARAM_SIGNATURE, PARAM_MESSAGE_ID, PARAM_WITNESS_SIG) match {
        case Seq(data@JsString(_), sender@JsString(_), signature@JsString(_), messageId@JsString(_), JsArray(witnessSig)) =>
          Message(
            data.convertTo[Base64Data],
            sender.convertTo[PublicKey],
            signature.convertTo[Signature],
            messageId.convertTo[Hash],
            witnessSig.map(_.convertTo[WitnessSignaturePair]).toList
          )
        case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Message object")
      }

    override def write(obj: Message): JsValue = JsObject(
      PARAM_DATA -> obj.data.toJson,
      PARAM_SENDER -> obj.sender.toJson,
      PARAM_SIGNATURE -> obj.signature.toJson,
      PARAM_MESSAGE_ID -> obj.message_id.toJson,
      PARAM_WITNESS_SIG -> obj.witness_signatures.toJson // FIXME does this work? Otherwise use "JsArray(obj.witness_signatures.map(_.toJson).toVector))"
    )
  }

  implicit object ParamsFormat extends RootJsonFormat[Params] {
    final private val PARAM_CHANNEL: String = "channel"
    final private val OPTIONAL_PARAM_MESSAGE: String = "message"

    override def read(json: JsValue): Params = json.asJsObject.getFields(PARAM_CHANNEL) match {
      case Seq(channel@JsString(_)) => json.asJsObject.getFields(OPTIONAL_PARAM_MESSAGE) match {
        case Seq(message@JsObject(_)) => new ParamsWithMessage(channel.convertTo[Channel], message.convertTo[Message])
        case Seq(_) => throw new IllegalArgumentException(s"Unrecognizable '$OPTIONAL_PARAM_MESSAGE' value in $json")
        case _ => new Params(channel.convertTo[Channel])
      }
      case _ => throw new IllegalArgumentException(s"Unrecognizable '$PARAM_CHANNEL' value in $json")
    }

    override def write(obj: Params): JsValue = {
      var jsObjectContent: ListMap[String, JsValue] = ListMap[String, JsValue](PARAM_CHANNEL -> obj.channel.toJson)

      obj match {
        case params: ParamsWithMessage => jsObjectContent += (OPTIONAL_PARAM_MESSAGE -> params.message.toJson)
        case _ =>
      }

      JsObject(jsObjectContent)
    }
  }

  implicit object BroadcastFormat extends RootJsonFormat[Broadcast] {
    override def read(json: JsValue): Broadcast = {
      val params: ParamsWithMessage = json.convertTo[Params].asInstanceOf[ParamsWithMessage]
      Broadcast(params.channel, params.message)
    }
    override def write(obj: Broadcast): JsValue = obj.toJson(ParamsFormat.write)
  }

  implicit object CatchupFormat extends RootJsonFormat[Catchup] {
    override def read(json: JsValue): Catchup = Catchup(json.convertTo[Params].channel)
    override def write(obj: Catchup): JsValue = obj.toJson(ParamsFormat.write)
  }

  implicit object PublishFormat extends RootJsonFormat[Publish] {
    override def read(json: JsValue): Publish = {
      val params: ParamsWithMessage = json.convertTo[Params].asInstanceOf[ParamsWithMessage]
      Publish(params.channel, params.message)
    }
    override def write(obj: Publish): JsValue = obj.toJson(ParamsFormat.write)
  }

  implicit object SubscribeFormat extends RootJsonFormat[Subscribe] {
    override def read(json: JsValue): Subscribe = Subscribe(json.convertTo[Params].channel)
    override def write(obj: Subscribe): JsValue = obj.toJson(ParamsFormat.write)
  }

  implicit object UnsubscribeFormat extends RootJsonFormat[Unsubscribe] {
    override def read(json: JsValue): Unsubscribe = Unsubscribe(json.convertTo[Params].channel)
    override def write(obj: Unsubscribe): JsValue = obj.toJson(ParamsFormat.write)
  }


  implicit val errorObjectFormat: JsonFormat[ErrorObject] = jsonFormat2(ErrorObject.apply)
  implicit object ResultObjectFormat extends RootJsonFormat[ResultObject] {
    override def read(json: JsValue): ResultObject = json match {
      case JsNumber(resultInt) => new ResultObject(resultInt.toInt)
      case JsArray(resultArray) => new ResultObject(resultArray.map(_.convertTo[Message]).toList)
      case _ => throw new IllegalArgumentException(s"Unrecognizable channel value in $json")
    }
    override def write(obj: ResultObject): JsValue = {
      if (obj.isIntResult) {
        JsNumber(obj.resultInt.get)
      } else {
        JsArray(obj.resultMessages.get.map(m => m.toJson).toVector)
      }
    }
  }

  implicit object jsonRpcRequestFormat extends RootJsonFormat[JsonRpcRequest] {
    final private val PARAM_JSON_RPC: String = "jsonrpc"
    final private val PARAM_METHOD: String = "method"
    final private val PARAM_PARAMS: String = "params"
    final private val PARAM_ID: String = "id"

    override def read(json: JsValue): JsonRpcRequest = json.asJsObject.getFields(PARAM_JSON_RPC, PARAM_METHOD, PARAM_PARAMS, PARAM_ID) match {
      case Seq(JsString(version), methodJsString@JsString(_), paramsJsObject@JsObject(_), optId) => {

        val method: MethodType = methodJsString.convertTo[MethodType]
        val params: Params = method match {
          case MethodType.BROADCAST => paramsJsObject.convertTo[Broadcast]
          case MethodType.PUBLISH => paramsJsObject.convertTo[Publish]
          case MethodType.SUBSCRIBE => paramsJsObject.convertTo[Subscribe]
          case MethodType.UNSUBSCRIBE => paramsJsObject.convertTo[Unsubscribe]
          case MethodType.CATCHUP => paramsJsObject.convertTo[Catchup]
        }

        val id: Option[Int] = optId match {
          case JsNumber(id) => Some(id.toInt)
          case JsNull => None
          case _ => throw new IllegalArgumentException(s"Can't parse json value $optId to an id (number or null)")
        }

        JsonRpcRequest(version, method, params, id)
      }
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a JsonRpcRequest object")
    }

    override def write(obj: JsonRpcRequest): JsValue = {
      val optId: JsValue = obj.id match {
        case Some(idx) => idx.toJson
        case _ => JsNull
      }

      JsObject(
        PARAM_JSON_RPC -> obj.jsonrpc.toJson,
        PARAM_METHOD -> obj.method.toJson,
        PARAM_PARAMS -> obj.params.toJson,
        PARAM_ID -> optId
      )
    }
  }

  implicit object jsonRpcResponseFormat extends RootJsonFormat[JsonRpcResponse] {
    final private val PARAM_JSON_RPC: String = "jsonrpc"
    final private val PARAM_RESULT: String = "result"
    final private val PARAM_ERROR: String = "error"
    final private val PARAM_ID: String = "id"

    override def read(json: JsValue): JsonRpcResponse = json.asJsObject.getFields(PARAM_JSON_RPC, PARAM_ID) match {
      case Seq(JsString(version), id) =>
        val idOpt: Option[Int] = id match {
          case JsNumber(idx) => Some(idx.toInt)
          case JsNull => None
          case _ => throw new IllegalArgumentException(s"Unable to parse json value $id to an id (number or null)")
        }

        val (resultOpt, errorOpt): (Option[ResultObject], Option[ErrorObject]) = json.asJsObject.getFields(PARAM_RESULT) match {
          case Seq(result) => (Some(result.convertTo[ResultObject]), None)
          case _ => json.asJsObject.getFields(PARAM_ERROR) match {
            case Seq(error) => (None, Some(error.convertTo[ErrorObject]))
            case _ => throw new IllegalArgumentException(
              s"Unable to parse json answer $json to a JsonRpcResponse object: 'result' and 'error' fields are missing or wrongly formatted"
            )
          }
        }

        JsonRpcResponse(version, resultOpt, errorOpt, idOpt)
      case _ => throw new IllegalArgumentException(
        s"Unable to parse json answer $json to a JsonRpcResponse object: 'jsonrpc' or 'id' field missing or wrongly formatted"
      )
    }

    override def write(obj: JsonRpcResponse): JsValue = {
      var jsObjectContent: ListMap[String, JsValue] = ListMap[String, JsValue](PARAM_JSON_RPC -> obj.jsonrpc.toJson)

      obj.id match {
        case Some(idx) => jsObjectContent += (PARAM_ID -> idx.toJson)
        case _ => jsObjectContent += (PARAM_ID -> JsNull)
      }

      if (obj.isPositive) {
        jsObjectContent += (PARAM_RESULT -> obj.result.get.toJson)
      } else {
        jsObjectContent += (PARAM_ERROR -> obj.error.get.toJson)
      }

      JsObject(jsObjectContent)
    }
  }
}
