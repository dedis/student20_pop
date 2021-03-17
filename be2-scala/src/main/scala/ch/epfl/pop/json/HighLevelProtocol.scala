package ch.epfl.pop.json

import ch.epfl.pop.model.network.method.{Broadcast, Catchup, Publish, Subscribe, Unsubscribe}
import ch.epfl.pop.model.network.method.message.Message
import ch.epfl.pop.model.network.method.message.data.lao.{CreateLao, StateLao, UpdateLao}
import ch.epfl.pop.model.network.method.message.data.meeting.{CreateMeeting, StateMeeting}
import ch.epfl.pop.model.network.method.message.data.rollCall.{CloseRollCall, CreateRollCall, OpenRollCall, ReopenRollCall}
import ch.epfl.pop.model.network.method.message.data.witness.WitnessMessage
import ch.epfl.pop.model.objects._
import spray.json.{DefaultJsonProtocol, JsNull, JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat, deserializationError, enrichAny}


object HighLevelProtocol extends DefaultJsonProtocol {

  implicit object Base64DataFormat extends JsonFormat[Base64Data] {
    override def read(json: JsValue): Base64Data = json match {
      case JsString(data) => Base64Data(data)
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a Base64Data object")
    }

    override def write(obj: Base64Data): JsValue = JsString(obj.data)
  }

  implicit object HashFormat extends JsonFormat[Hash] {
    override def read(json: JsValue): Hash = json match {
      case JsString(data) => Hash(Base64Data(data))
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a Hash object")
    }

    override def write(obj: Hash): JsValue = obj.base64Data.toJson
  }

  implicit object PublicKeyFormat extends JsonFormat[PublicKey] {
    override def read(json: JsValue): PublicKey = json match {
      case JsString(data) => PublicKey(Base64Data(data))
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a PublicKey object")
    }

    override def write(obj: PublicKey): JsValue = obj.base64Data.toJson
  }

  implicit object SignatureFormat extends JsonFormat[Signature] {
    override def read(json: JsValue): Signature = json match {
      case JsString(data) => Signature(Base64Data(data))
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a Signature object")
    }

    override def write(obj: Signature): JsValue = obj.signature.toJson
  }

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    override def read(json: JsValue): Timestamp = json match {
      case JsNumber(time) => Timestamp(time.toLong)
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a Timestamp object")
    }

    override def write(obj: Timestamp): JsValue = obj.time.toJson
  }

  implicit object WitnessSignaturePairFormat extends JsonFormat[WitnessSignaturePair] {
    override def read(json: JsValue): WitnessSignaturePair = json.asJsObject().getFields("witness", "signature") match {
      case Seq(JsString(w), JsString(s)) => WitnessSignaturePair(PublicKey(Base64Data(w)), Signature(Base64Data(s)))
      case _ =>
        throw new IllegalArgumentException("Can't parse json value " + json + " to a WitnessSignaturePair object")
    }

    override def write(obj: WitnessSignaturePair): JsValue = JsObject(
      "witness" -> JsString(obj.witness.base64Data.data),
      "signature" -> JsString(obj.signature.signature.data),
    )
  }

  // TODO: implement
  implicit object optionAnyFormat extends JsonFormat[Option[Any]] {
    override def read(json: JsValue): Option[Any] = json match {
      case JsNumber(value) => Some(Timestamp(value.longValue))
      case _ => None
    }

    override def write(obj: Option[Any]): JsValue = obj match {
      case Some(timestamp : Timestamp) => timestamp.toJson
      case _ => JsNull
    }
  }

  implicit val createLaoFormat: RootJsonFormat[CreateLao] = jsonFormat5(CreateLao.apply)
  implicit val stateLaoFormat: JsonFormat[StateLao] = jsonFormat8(StateLao.apply)
  implicit val updateLaoFormat: JsonFormat[UpdateLao] = jsonFormat4(UpdateLao.apply)

  implicit val createMeetingFormat: JsonFormat[CreateMeeting] = jsonFormat7(CreateMeeting.apply)
  implicit val stateMeetingFormat: JsonFormat[StateMeeting] = jsonFormat10(StateMeeting.apply)

  implicit val closeRollCallFormat: JsonFormat[CloseRollCall] = jsonFormat4(CloseRollCall.apply)
  implicit val createRollCallFormat: JsonFormat[CreateRollCall] = jsonFormat7(CreateRollCall.apply)
  implicit val openRollCallFormat: JsonFormat[OpenRollCall] = jsonFormat3(OpenRollCall.apply)
  implicit val reopenRollCallFormat: JsonFormat[ReopenRollCall] = jsonFormat3(ReopenRollCall.apply)

  implicit val witnessMessageFormat: JsonFormat[WitnessMessage] = jsonFormat2(WitnessMessage.apply)

  implicit val messageFormat : JsonFormat[Message] = jsonFormat5(Message.apply)

  implicit val broadcastFormat : JsonFormat[Broadcast] = jsonFormat2(Broadcast.apply)
  implicit val catchupFormat : JsonFormat[Catchup] = jsonFormat1(Catchup.apply)
  implicit val publishFormat : JsonFormat[Publish] = jsonFormat2(Publish.apply)
  implicit val subscribeFormat : JsonFormat[Subscribe] = jsonFormat1(Subscribe.apply)
  implicit val unsubscribeFormat : JsonFormat[Unsubscribe] = jsonFormat1(Unsubscribe.apply)
}
