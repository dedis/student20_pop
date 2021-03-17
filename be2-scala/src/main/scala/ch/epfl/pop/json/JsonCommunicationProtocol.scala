package ch.epfl.pop.json

import ch.epfl.pop.json.Actions.Actions
import ch.epfl.pop.json.JsonMessages._
import ch.epfl.pop.json.JsonUtils._
import ch.epfl.pop.json.Methods.Methods
import ch.epfl.pop.json.Objects.Objects
import spray.json._

import java.nio.charset.StandardCharsets
import scala.collection.immutable
import scala.util.{Failure, Success, Try}


/**
 * Custom Json communication protocol
 */
object JsonCommunicationProtocol extends DefaultJsonProtocol {

  /* implicit used to parse/serialize a Methods enumeration value */
  implicit object JsonEnumMethodsFormat extends RootJsonFormat[Methods] {
    @throws(classOf[DeserializationException])
    override def read(json: JsValue): Methods = Methods.unapply(json.convertTo[String]) match {
      case Some(v) => v
      case _ => throw DeserializationException("invalid \"method\" field : unrecognized")
    }
    override def write(obj: Methods): JsValue = JsString(obj.toString)
  }

  /* implicit used to parse/serialize a Objects enumeration value */
  implicit object JsonEnumObjectsFormat extends RootJsonFormat[Objects] {
    @throws(classOf[DeserializationException])
    override def read(json: JsValue): Objects = Objects.unapply(json.convertTo[String]) match {
      case Some(v) => v
      case _ => throw DeserializationException("invalid \"object\" field : unrecognized")
    }
    override def write(obj: Objects): JsValue = JsString(obj.toString)
  }

  /* implicit used to parse/serialize a Actions enumeration value */
  implicit object JsonEnumActionsFormat extends RootJsonFormat[Actions] {
    @throws(classOf[DeserializationException])
    override def read(json: JsValue): Actions = Actions.unapply(json.convertTo[String]) match {
      case Some(v) => v
      case _ => throw DeserializationException("invalid \"action\" field : unrecognized")
    }
    override def write(obj: Actions): JsValue = JsString(obj.toString)
  }

  /* implicit used to parse/serialize a String encoded in Base64 */
  implicit object ByteArrayFormat extends RootJsonFormat[ByteArray] {
    @throws(classOf[IllegalArgumentException])
    override def read(json: JsValue): ByteArray =
      JsonUtils.DECODER.decode(json.convertTo[String].getBytes(StandardCharsets.UTF_8))
    override def write(obj: ByteArray): JsValue = JsString(JsonUtils.ENCODER.encode(obj).map(_.toChar).mkString)
  }

  implicit val keySignPairFormat: RootJsonFormat[KeySignPair] = jsonFormat2(KeySignPair)


  /* ------------------- ADMIN MESSAGES CLIENT ------------------- */

  implicit object MessageContentDataFormat extends RootJsonFormat[MessageContentData] {
    override def read(json: JsValue): MessageContentData = {
      val jsonObject: JsObject = json.asJsObject
      jsonObject.getFields("object").head.convertTo[String] match {

        /* create LAO, update LAO's properties and broadcast LAO's state */
        case Objects.Lao() =>
          jsonObject.getFields("action", "id", "name", "creation", "organizer", "witnesses") match {

            // create LAO and broadcast LAO's state
            case Seq(a@JsString(_), id@JsString(_), JsString(n), c@JsNumber(_), orgKey@JsString(_), JsArray(w)) =>
              val action: Actions = a.convertTo[Actions]
              val mcb = new MessageContentDataBuilder()
                .setHeader(Objects.Lao, action)
                .setId(id.convertTo[ByteArray])
                .setName(n)
                .setCreation(c.convertTo[TimeStamp])
                .setOrganizer(orgKey.convertTo[Key])
                .setWitnesses(w.map(_.convertTo[Key]).toList)


              action match {
                case Actions.UpdateProperties =>
                  jsonObject.getFields("last_modified") match {
                    case Seq(lm@JsNumber(_)) => mcb.setLastModified(lm.convertTo[TimeStamp])
                    case _ => throw JsonMessageParserException(
                      s"""invalid "$action" query : field "last_modified" missing or wrongly formatted"""
                    )
                  }
                case Actions.State =>
                  jsonObject.getFields("last_modified", "modification_id", "modification_signatures") match {
                    case Seq(lm@JsNumber(_), mid@JsString(_), JsArray(ms)) =>
                      mcb.setLastModified(lm.convertTo[TimeStamp])
                        .setModificationId(mid.convertTo[ByteArray])
                        .setModificationSignatures(ms.map(_.convertTo[KeySignPair]).toList)

                    case _ => throw JsonMessageParserException(
                      "invalid \"stateBroadcastLao\" query : fields (\"modification_id\" and/or " +
                      "\"modification_signatures\" and/or \"last_modified\") missing or wrongly formatted"
                    )
                  }

                case _ =>
              }

              mcb.build()


            // update LAO's properties
            case Seq(action@JsString(_), id@JsString(_), JsString(name), JsArray(witnesses)) =>
              val mcb = new MessageContentDataBuilder()
                .setHeader(Objects.Lao, action.convertTo[Actions])
                .setId(id.convertTo[ByteArray])
                .setName(name)
                .setWitnesses(witnesses.map(_.convertTo[Key]).toList)

              jsonObject.getFields("last_modified") match {
                case Seq(lm@JsNumber(_)) => mcb.setLastModified(lm.convertTo[TimeStamp])
                case _ => throw JsonMessageParserException(
                  """invalid "updateLaoProperties" query : field "last_modified" missing or wrongly formatted"""
                )
              }

              mcb.build()


            // parsing error : invalid message content data fields
            case _ => throw JsonMessageParserException("invalid \"MessageContentData\" : fields missing or wrongly formatted")
        }

        /* witness a message */
        case Objects.Message() =>
          jsonObject.getFields("action", "message_id", "signature") match {
            case Seq(action@JsString(_), mid@JsString(_), signature@JsString(_)) =>
              new MessageContentDataBuilder()
                .setHeader(Objects.Message, action.convertTo[Actions])
                .setMessageId(mid.convertTo[Base64String])
                .setSignature(signature.convertTo[SignatureJson])
                .build()

            // parsing error : invalid message content data fields
            case _ => throw JsonMessageParserException("invalid \"MessageContentData\" : fields missing or wrongly formatted")
          }

        /* create meeting and broadcast meeting's state */
        case Objects.Meeting() =>
          jsonObject.getFields("action", "id", "name", "creation", "start") match {
            case Seq(a@JsString(_), id@JsString(_), JsString(n), c@JsNumber(_), st@JsNumber(_)) =>
              val action: Actions = a.convertTo[Actions]

              val location: Seq[JsValue] = jsonObject.getFields("location")
              val end: Seq[JsValue] = jsonObject.getFields("end")
              val extra: Seq[JsValue] = jsonObject.getFields("extra")

              val mcd = new MessageContentDataBuilder()
                .setHeader(Objects.Meeting, action)
                .setId(id.convertTo[ByteArray])
                .setName(n)
                .setCreation(c.convertTo[TimeStamp])
                .setStart(st.convertTo[TimeStamp])

              action match {
                case Actions.State =>
                  jsonObject.getFields("last_modified", "modification_id", "modification_signatures") match {
                    case Seq(lm@JsNumber(_), mid@JsString(_), JsArray(ms)) =>
                      mcd.setLastModified(lm.convertTo[TimeStamp])
                        .setModificationId(mid.convertTo[ByteArray])
                        .setModificationSignatures(ms.map(_.convertTo[KeySignPair]).toList)
                    case _ => throw JsonMessageParserException(
                      "invalid \"stateBroadcastMeeting\" query : fields (\"modification_id\" and/or " +
                        "\"modification_signatures\" and/or \"last_modified\") missing or wrongly formatted"
                    )
                  }
                case _ =>
              }

              location match {
                case Seq(JsString(l)) => mcd.setLocation(l)
                case _ =>
              }
              end match {
                case Seq(end@JsNumber(_)) => mcd.setEnd(end.convertTo[TimeStamp])
                case _ =>
              }
              extra match { case _ => mcd.setExtra("extra: [unknown extra type?]") }

              mcd.build()

            // parsing error : invalid message content data fields
            case _ => throw JsonMessageParserException("invalid \"MessageContentData\" : fields missing or wrongly formatted")
          }

        /* create/open/reopen/close roll call */
        case Objects.RollCall() =>
          jsonObject.getFields("action", "id") match {
            case Seq(a@JsString(_), id@JsString(_)) =>
              val action: Actions = a.convertTo[Actions]

              val mcd = new MessageContentDataBuilder()
                .setHeader(Objects.RollCall, action)
                .setId(id.convertTo[ByteArray])

              action match {
                case Actions.Create =>
                  jsonObject.getFields("name", "creation", "location") match {
                    case Seq(JsString(name), creation@JsNumber(_), JsString(location)) =>
                      mcd.setName(name)
                        .setCreation(creation.convertTo[TimeStamp])
                        .setLocation(location)

                      jsonObject.getFields("start") match {
                        case Seq(s@JsNumber(_)) => mcd.setStart(s.convertTo[TimeStamp])
                        case _ =>
                      }
                      jsonObject.getFields("scheduled") match {
                        case Seq(s@JsNumber(_)) => mcd.setScheduled(s.convertTo[TimeStamp])
                        case _ =>
                      }
                      jsonObject.getFields("roll_call_description") match {
                        case Seq(JsString(description)) => mcd.setRollCallDescription(description)
                        case _ =>
                      }

                    case _ => throw JsonMessageParserException(
                      "invalid \"CreateRollCall\" query : fields (\"name\" and/or " +
                        "\"creation\" and/or \"location\") missing or wrongly formatted"
                    )
                  }

                case Actions.Open | Actions.Reopen =>
                  jsonObject.getFields("start") match {
                    case Seq(start@JsNumber(_)) =>
                      mcd.setStart(start.convertTo[TimeStamp])

                    case _ => throw JsonMessageParserException(
                      "invalid \"OpenRollCall/ReopenRollCall\" query : field (\"start\") missing or wrongly formatted"
                    )
                  }

                case Actions.Close =>
                  jsonObject.getFields("start", "end", "attendees") match {
                    case Seq(start@JsNumber(_), end@JsNumber(_), JsArray(attendees)) =>
                      mcd.setStart(start.convertTo[TimeStamp])
                        .setEnd(end.convertTo[TimeStamp])
                        .setAttendees(attendees.map(_.convertTo[Key]).toList)

                    case _ => throw JsonMessageParserException(
                      "invalid \"CloseRollCall\" query : fields (\"start\" and/or " +
                        "\"end\" and/or \"attendees\") missing or wrongly formatted"
                    )
                  }
                case _ => throw JsonMessageParserException(
                  s"""invalid roll call query : action "${action.toString}" is unrecognizable"""
                )
              }

              mcd.build()


            // parsing error : invalid message content data fields
            case _ => throw JsonMessageParserException("invalid \"MessageContentData\" : fields missing or wrongly formatted")
          }

        /* parsing error : object field not recognized */
        case _ => throw JsonMessageParserException("invalid \"MessageContentData\" : invalid \"object\" field (unrecognized)")
      }
    }

    override def write(obj: MessageContentData): JsValue = {

      var jsObjectContent: immutable.ListMap[String, JsValue] = immutable.ListMap[String, JsValue]()

      jsObjectContent += ("object" -> JsString(obj._object.toString))
      jsObjectContent += ("action" -> JsString(obj.action.toString))

      if (!obj.id.isEmpty) jsObjectContent += ("id" -> obj.id.toJson)
      if (obj.name != "") jsObjectContent += ("name" -> obj.name.toJson)
      if (obj.creation != -1L) jsObjectContent += ("creation" -> obj.creation.toJson)
      if (obj.last_modified != -1L) jsObjectContent += ("last_modified" -> obj.last_modified.toJson)
      if (!obj.organizer.isEmpty) jsObjectContent += ("organizer" -> obj.organizer.toJson)
      if (obj._object == Objects.Lao) jsObjectContent += ("witnesses" -> JsArray(obj.witnesses.map(w => w.toJson).toVector))
      if (!obj.modification_id.isEmpty) jsObjectContent += ("modification_id" -> obj.modification_id.toJson)
      if (obj._object == Objects.Lao && obj.action == Actions.State)
        jsObjectContent += ("modification_signatures" -> JsArray(obj.modification_signatures.map(s => s.toJson).toVector))
      if (!obj.message_id.isEmpty) jsObjectContent += ("message_id" -> obj.message_id.toJson)
      if (!obj.signature.isEmpty) jsObjectContent += ("signature" -> obj.signature.toJson)
      if (obj.location != "") jsObjectContent += ("location" -> obj.location.toJson)
      if (obj.start != -1L) jsObjectContent += ("start" -> obj.start.toJson)
      if (obj.end != -1L) jsObjectContent += ("end" -> obj.end.toJson)
      if (obj.extra != "") jsObjectContent += ("extra" -> obj.extra.toJson)
      if (obj.scheduled != -1L) jsObjectContent += ("scheduled" -> obj.scheduled.toJson)
      if (obj.roll_call_description != "") jsObjectContent += ("roll_call_description" -> obj.end.toJson)
      if (obj._object == Objects.RollCall && obj.action == Actions.Close)
        jsObjectContent += ("attendees" -> JsArray(obj.attendees.map(a => a.toJson).toVector))

      JsObject(jsObjectContent)
    }
  }

  implicit object MessageContentFormat extends RootJsonFormat[MessageContent] {
    override def read(json: JsValue): MessageContent =
      json.asJsObject.getFields("data", "sender", "signature", "message_id", "witness_signatures") match {
        case Seq(data, sender, signature, message_id, JsArray(witnesses)) =>
          val string64Data: Base64String = data.convertTo[Base64String]
          val decodedData: String = JsonUtils.DECODER.decode(string64Data).map(_.toChar).mkString

          MessageContent(
            string64Data,
            decodedData.parseJson.convertTo[MessageContentData],
            sender.convertTo[Key],
            signature.convertTo[SignatureJson],
            message_id.convertTo[HashJson],
            witnesses.map(_.convertTo[KeySignPair]).toList
          )

        // parsing error : invalid message content fields
        case _ => throw JsonMessageParserException("invalid \"MessageContent\" : fields missing or wrongly formatted")
      }

    override def write(obj: MessageContent): JsValue =
      JsObject(
        "data" -> obj.encodedData.toJson,
        "sender" -> obj.sender.toJson,
        "signature" -> obj.signature.toJson,
        "message_id" -> obj.message_id.toJson,
        "witness_signatures" -> JsArray(obj.witness_signatures.map(_.toJson).toVector)
      )
  }

  implicit val messageParametersFormat: RootJsonFormat[MessageParameters] = jsonFormat2(MessageParameters)

  implicit object JsonMessagePublishClientFormat extends RootJsonFormat[JsonMessagePublishClient] {
    override def read(json: JsValue): JsonMessagePublishClient = {
      json.asJsObject.getFields("jsonrpc", "method", "params", "id") match {
        case Seq(JsString(version), method@JsString(_), params@JsObject(_), JsNumber(id)) =>

          val parsed: Try[JsonMessagePublishClient] = Try {
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

                /* CreateRollCallMessageClient, OpenRollCallMessageClient and CloseRollCallMessageClient */
                case (Objects.RollCall, action) => action match {
                  // CreateRollCallMessageClient
                  case Actions.Create => CreateRollCallMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                  // OpenRollCallMessageClient
                  case Actions.Open | Actions.Reopen => OpenRollCallMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                  // CloseRollCallMessageClient
                  case Actions.Close => CloseRollCallMessageClient(parsedParams, id.toInt, method.convertTo[Methods], version)
                }

                /* parsing error : invalid object/action pair */
                case _ => throw JsonMessageParserException(
                  s"invalid message : invalid (object = ${messageContent.data._object}, action = ${messageContent.data.action}) pair",
                  Some(id.toInt)
                )
              }

              // Should never happen missing MessageContent in MessageParameters
              case _ => throw JsonMessageParserException(
                "missing MessageContent in MessageParameter for JsonMessagePublishClient", Some(id.toInt)
              )
            }
          }

          parsed match {
            case Success(mpc) => mpc
            case Failure(s) => s match {
              case DeserializationException(msg, _, _) => throw JsonMessageParserException(msg, Some(id.toInt))
              case _ => throw s
            }
          }

        // parsing error : invalid message parameters fields
        case _ =>
          val msg: String = "invalid MessageParameters : fields missing or wrongly formatted"
          json.asJsObject.getFields("id") match {
            case Seq(JsNumber(id)) => throw JsonMessageParserException(msg, Some(id.toInt))
            case _ => throw JsonMessageParserException(msg)
          }
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

  implicit val messageErrorContentFormat: RootJsonFormat[MessageErrorContent] = jsonFormat2(MessageErrorContent)

  implicit val propagateMessageServerFormat: RootJsonFormat[PropagateMessageServer] = jsonFormat3(PropagateMessageServer)

  implicit val answerResultIntMessageServerFormat: RootJsonFormat[AnswerResultIntMessageServer] = jsonFormat3(AnswerResultIntMessageServer)
  implicit val answerResultArrayMessageServerFormat: RootJsonFormat[AnswerResultArrayMessageServer] = jsonFormat3(AnswerResultArrayMessageServer)

  implicit object AnswerErrorMessageServerFormat extends RootJsonFormat[AnswerErrorMessageServer] {
    override def read(json: JsValue): AnswerErrorMessageServer = {
      json.asJsObject.getFields("jsonrpc", "error") match {
        case Seq(JsString(version), err@JsObject(_)) =>
          json.asJsObject.getFields("id") match {
            case Seq(JsNumber(id)) => AnswerErrorMessageServer(Some(id.toInt), err.convertTo[MessageErrorContent], version)
            case Seq(JsNull) => AnswerErrorMessageServer(None, err.convertTo[MessageErrorContent], version)
            case _ => throw JsonMessageParserException("invalid \"AnswerErrorMessageServer\" : id field wrongly formatted")
          }
        case _ => throw JsonMessageParserException("invalid \"AnswerErrorMessageServer\" : fields missing or wrongly formatted")
      }
    }

    override def write(obj: AnswerErrorMessageServer): JsValue = {
      val optId: JsValue = obj.id match {
        case Some(idx) => idx.toJson
        case _ => JsNull
      }

      JsObject(
        "jsonrpc" -> obj.jsonrpc.toJson,
        "error" -> obj.error.toJson,
        "id" -> optId
      )
    }
  }


  /* ------------------- PUBSUB MESSAGES CLIENT ------------------- */

  implicit val subscribeMessageClientFormat: RootJsonFormat[SubscribeMessageClient] = jsonFormat4(SubscribeMessageClient)
  implicit val unsubscribeMessageClientFormat: RootJsonFormat[UnsubscribeMessageClient] = jsonFormat4(UnsubscribeMessageClient)
  implicit val catchupMessageClientFormat: RootJsonFormat[CatchupMessageClient] = jsonFormat4(CatchupMessageClient)
}
