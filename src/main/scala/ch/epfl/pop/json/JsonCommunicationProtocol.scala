package ch.epfl.pop.json

import java.util.Base64

import ch.epfl.pop.json.Actions.Actions
import ch.epfl.pop.json.JsonMessages._
import ch.epfl.pop.json.JsonUtils._
import ch.epfl.pop.json.Methods.Methods
import ch.epfl.pop.json.Objects.Objects
import spray.json._

import scala.collection.immutable


/**
 * Custom Json communication protocol
 */
object JsonCommunicationProtocol extends DefaultJsonProtocol {

  implicit object jsonEnumMethodsFormat extends RootJsonFormat[Methods] {
    override def read(json: JsValue): Methods = Methods.withName(json.convertTo[String]) // TODO may throw NoSuchElementException
    override def write(obj: Methods): JsValue = JsString(obj.toString)
  }

  implicit object jsonEnumObjectsFormat extends RootJsonFormat[Objects] {
    override def read(json: JsValue): Objects = Objects.withName(json.convertTo[String]) // TODO may throw NoSuchElementException
    override def write(obj: Objects): JsValue = JsString(obj.toString)
  }

  implicit object jsonEnumActionsFormat extends RootJsonFormat[Actions] {
    override def read(json: JsValue): Actions = Actions.withName(json.convertTo[String]) // TODO may throw NoSuchElementException
    override def write(obj: Actions): JsValue = JsString(obj.toString)
  }


  implicit object DecodedBase64StringFormat extends RootJsonFormat[DecodedBase64String] {
    override def read(json: JsValue): DecodedBase64String = JsonUtils.DECODER.decode(json.convertTo[String])
    override def write(obj: DecodedBase64String): JsValue = JsString(JsonUtils.ENCODER.encode(obj).map(_.toChar).mkString)
  }


  /* ------------------- ADMIN MESSAGES CLIENT ------------------- */

  implicit object MessageContentDataFormat extends RootJsonFormat[MessageContentData] {
    override def read(json: JsValue): MessageContentData = {
      val jsonObject: JsObject = json.asJsObject
      jsonObject.getFields("object").head.convertTo[String] match {

        /* create LAO, update LAO's properties and broadcast LAO's state */
        case Objects.Lao() =>
          jsonObject.getFields("action", "id", "name", "creation", "last_modified", "organizer", "witnesses") match {

            // create LAO and broadcast LAO's state
            case Seq(a@JsString(_), id@JsString(_), JsString(n), c@JsNumber(_), lm@JsNumber(_), orgKey@JsString(_), JsArray(w)) =>
              new MessageContentDataBuilder()
                .setHeader(Objects.Lao, a.convertTo[Actions])
                .setId(id.convertTo[DecodedBase64String])
                .setName(n)
                .setCreation(c.convertTo[TimeStamp])
                .setLastModified(lm.convertTo[TimeStamp])
                .setOrganizer(orgKey.convertTo[Key])
                .setWitnesses(w.map(_.convertTo[Key]).toList)
                .build()

            // update LAO's properties
            case Seq(action@JsString(_), JsString(name), lastModified@JsNumber(_), JsArray(witnesses)) =>
              new MessageContentDataBuilder()
                .setHeader(Objects.Lao, action.convertTo[Actions])
                .setName(name)
                .setLastModified(lastModified.convertTo[TimeStamp])
                .setWitnesses(witnesses.map(_.convertTo[Key]).toList)
                .build()


            // parsing error : invalid message content data fields
            case _ => throw DeserializationException("invalid MessageContentData : fields missing or wrongly formatted")
        }

        /* witness a message */
        case Objects.Message() =>
          jsonObject.getFields("action", "message_id", "signature") match {
            case Seq(action@JsString(_), mid@JsString(_), signature@JsString(_)) =>
              new MessageContentDataBuilder()
                .setHeader(Objects.Message, action.convertTo[Actions])
                .setMessageId(mid.convertTo[DecodedBase64String])
                .setSignature(signature.convertTo[Signature])
                .build()

            // parsing error : invalid message content data fields
            case _ => throw DeserializationException("invalid MessageContentData : fields missing or wrongly formatted")
          }

        /* create meeting and broadcast meeting's state */
        case Objects.Meeting() =>
          jsonObject.getFields("action", "id", "name", "creation", "last_modified", "start") match {
            case Seq(a@JsString(_), id@JsString(_), JsString(n), c@JsNumber(_), lm@JsNumber(_), st@JsNumber(_)) =>

              val location: Seq[JsValue] = jsonObject.getFields("location")
              val end: Seq[JsValue] = jsonObject.getFields("end")
              val extra: Seq[JsValue] = jsonObject.getFields("extra")

              val mcd = new MessageContentDataBuilder()
                .setHeader(Objects.Meeting, a.convertTo[Actions])
                .setId(id.convertTo[DecodedBase64String])
                .setName(n)
                .setCreation(c.convertTo[TimeStamp])
                .setLastModified(lm.convertTo[TimeStamp])
                .setStart(st.convertTo[TimeStamp])

              location match {
                case Seq(JsString(l)) => mcd.setLocation(l)
                case _ =>
              }
              end match {
                case Seq(end@JsNumber(_)) => mcd.setEnd(end.convertTo[TimeStamp])
                case _ =>
              }
              extra match { case _ => mcd.setExtra("extra: TODO in JsonCommunicationProtocol") }

              mcd.build()

            // parsing error : invalid message content data fields
            case _ => throw DeserializationException("invalid MessageContentData : fields missing or wrongly formatted")
          }

        /* parsing error : object field not recognized */
        case _ => throw DeserializationException("invalid MessageContentData : invalid object field")
      }
    }

    override def write(obj: MessageContentData): JsValue = {

      var jsObjectContent: immutable.ListMap[String, JsValue] = immutable.ListMap[String, JsValue]()

      jsObjectContent += ("object" -> JsString(obj._object.toString))
      jsObjectContent += ("action" -> JsString(obj.action.toString))

      if (!obj.id.isEmpty) jsObjectContent += ("id" -> obj.id.toJson)
      if (obj.name != "") jsObjectContent += ("name" -> obj.name.toJson)
      if (obj.creation != -1) jsObjectContent += ("creation" -> obj.creation.toJson)
      if (obj.last_modified != -1) jsObjectContent += ("last_modified" -> obj.last_modified.toJson)
      if (!obj.organizer.isEmpty) jsObjectContent += ("organizer" -> obj.organizer.toJson)
      if (obj._object == Objects.Lao) jsObjectContent += ("witnesses" -> JsArray(obj.witnesses.map(w => w.toJson).toVector))
      if (!obj.message_id.isEmpty) jsObjectContent += ("message_id" -> obj.message_id.toJson)
      if (!obj.signature.isEmpty) jsObjectContent += ("signature" -> obj.signature.toJson)
      if (obj.location != "") jsObjectContent += ("location" -> obj.location.toJson)
      if (obj.start != -1) jsObjectContent += ("start" -> obj.start.toJson)
      if (obj.end != -1) jsObjectContent += ("end" -> obj.end.toJson)
      if (obj.extra != "") jsObjectContent += ("extra" -> obj.extra.toJson) // TODO modify extra's type

      JsObject(jsObjectContent)
    }
  }

  implicit object MessageContentFormat extends RootJsonFormat[MessageContent] {
    override def read(json: JsValue): MessageContent =
      json.asJsObject.getFields("data", "sender", "signature", "message_id", "witness_signatures") match {
        case Seq(data, sender, signature, message_id, JsArray(witnesses)) =>
          val decodedData: String = Base64.getDecoder.decode(data.convertTo[String]).map(_.toChar).mkString

          MessageContent(
            decodedData.parseJson.convertTo[MessageContentData],
            sender.convertTo[Key],
            signature.convertTo[Signature],
            message_id.convertTo[Hash],
            witnesses.map(_.convertTo[Key]).toList
          )

        // parsing error : invalid message content fields
        case _ => throw DeserializationException("invalid MessageContent : fields missing or wrongly formatted")
      }

    override def write(obj: MessageContent): JsValue = {

      val encodedData: Array[Byte] = Base64.getEncoder.encode(obj.data.toJson.compactPrint.getBytes)
      val encodedString: String = encodedData.map(_.toChar).mkString

      JsObject(
        "data" -> JsString(encodedString),
        "sender" -> obj.sender.toJson,
        "signature" -> obj.signature.toJson,
        "message_id" -> obj.message_id.toJson,
        "witness_signatures" -> JsArray(obj.witness_signatures.map(_.toJson).toVector)
      )
    }
  }

  implicit val messageParametersFormat: RootJsonFormat[MessageParameters] = jsonFormat2(MessageParameters)

  implicit object JsonMessagePublishClientFormat extends RootJsonFormat[JsonMessagePublishClient] {
    override def read(json: JsValue): JsonMessagePublishClient = {
      json.asJsObject.getFields("jsonrpc", "method", "params", "id") match {
        case Seq(JsString(version), method@JsString(_), params@JsObject(_), JsNumber(id)) =>

          val parsedParams: MessageParameters = params.convertTo[MessageParameters]

          parsedParams.message match {
            case Some(messageContent) => (messageContent.data._object, messageContent.data.action) match {

              /* CreateLaoMessageClient, UpdateLaoMessageClient and BroadcastLaoMessageClient */
              case (Objects.Lao, action) => action match {
                // CreateLaoMessageClient
                case Actions.Create => CreateLaoMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                // UpdateLaoMessageClient
                case Actions.UpdateProperties => UpdateLaoMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                // BroadcastLaoMessageClient
                case Actions.State => BroadcastLaoMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
              }

              /* WitnessMessageMessageClient */
              case (Objects.Message, Actions.Witness) => WitnessMessageMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)

              /* CreateMeetingMessageClient and BroadcastMeetingMessageClient */
              case (Objects.Meeting, action) => action match {
                // CreateMeetingMessageClient
                case Actions.Create => CreateMeetingMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                // BroadcastMeetingMessageClient
                case Actions.State => BroadcastMeetingMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
              }

              /* parsing error : invalid object/action pair */
              case _ => throw DeserializationException(
                s"invalid message : invalid (object = ${messageContent.data._object}, action = ${messageContent.data.action}) pair"
              )
            }

            // Should never happen missing MessageContent in MessageParameters
            case _ =>
              println("parsing error : missing MessageContent in MessageParameter for JsonMessagePublishClient. Returning null")
              null
          }

        // parsing error : invalid message parameters fields
        case _ => throw DeserializationException("invalid MessageParameters : fields missing or wrongly formatted")
      }
    }


    override def write(obj: JsonMessagePublishClient): JsValue =
      JsObject(
        "jsonrpc" -> obj.jsonrpc.toJson,
        "method" -> obj.method.toJson,
        "params" -> obj.params.toJson,
        "id" -> obj.id.toJson
      )
  }


  /* ------------------- ANSWER MESSAGES SERVER ------------------- */

  implicit val channelMessagesFormat: RootJsonFormat[ChannelMessages] = jsonFormat1(ChannelMessages)
  implicit val messageErrorContentFormat: RootJsonFormat[MessageErrorContent] = jsonFormat2(MessageErrorContent)

  implicit val answerResultIntMessageServerFormat: RootJsonFormat[AnswerResultIntMessageServer] = jsonFormat3(AnswerResultIntMessageServer)
  implicit val answerResultArrayMessageServerFormat: RootJsonFormat[AnswerResultArrayMessageServer] = jsonFormat3(AnswerResultArrayMessageServer)
  implicit val answerErrorMessageServerFormat: RootJsonFormat[AnswerErrorMessageServer] = jsonFormat3(AnswerErrorMessageServer)


  /* ------------------- PUBSUB MESSAGES CLIENT ------------------- */

  implicit val subscribeMessageClientFormat: RootJsonFormat[SubscribeMessageClient] = jsonFormat4(SubscribeMessageClient)
  implicit val unsubscribeMessageClientFormat: RootJsonFormat[UnsubscribeMessageClient] = jsonFormat4(UnsubscribeMessageClient)
  implicit val propagateMessageServerFormat: RootJsonFormat[PropagateMessageServer] = jsonFormat3(PropagateMessageServer) // TODO move
  implicit val catchupMessageClientFormat: RootJsonFormat[CatchupMessageClient] = jsonFormat4(CatchupMessageClient)
}
