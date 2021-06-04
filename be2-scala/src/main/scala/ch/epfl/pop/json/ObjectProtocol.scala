package ch.epfl.pop.json

import ch.epfl.pop.model.objects._
import spray.json._

object ObjectProtocol extends DefaultJsonProtocol {
  implicit object Base64DataFormat extends JsonFormat[Base64Data] {
    override def read(json: JsValue): Base64Data = json match {
      case JsString(data) => Base64Data(data)
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Base64Data object")
    }

    override def write(obj: Base64Data): JsValue = JsString(obj.data)
  }

  implicit object ChannelFormat extends JsonFormat[Channel] {
    override def read(json: JsValue): Channel = json match {
      case JsString(value) => Channel(value)
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Channel object")
    }

    override def write(obj: Channel): JsValue = JsString(obj.channel)
  }

  implicit object HashFormat extends JsonFormat[Hash] {
    override def read(json: JsValue): Hash = json match {
      case dataJs@JsString(_) => Hash(dataJs.convertTo[Base64Data])
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Hash object")
    }

    override def write(obj: Hash): JsValue = obj.base64Data.toJson
  }

  implicit object PublicKeyFormat extends JsonFormat[PublicKey] {
    override def read(json: JsValue): PublicKey = json match {
      case dataJs@JsString(_) => PublicKey(dataJs.convertTo[Base64Data])
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a PublicKey object")
    }

    override def write(obj: PublicKey): JsValue = obj.base64Data.toJson
  }

  implicit object SignatureFormat extends JsonFormat[Signature] {
    override def read(json: JsValue): Signature = json match {
      case dataJs@JsString(_) => Signature(dataJs.convertTo[Base64Data])
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Signature object")
    }

    override def write(obj: Signature): JsValue = obj.signature.toJson
  }

  implicit object TimestampFormat extends JsonFormat[Timestamp] {
    override def read(json: JsValue): Timestamp = json match {
      case JsNumber(time) => Timestamp(time.toLong)
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a Timestamp object")
    }

    override def write(obj: Timestamp): JsValue = obj.time.toJson
  }

  implicit object OptionalTimestampFormat extends JsonFormat[Option[Timestamp]] {
    override def read(json: JsValue): Option[Timestamp] = ???

    override def write(obj: Option[Timestamp]): JsValue = ???
  }

  implicit object WitnessSignaturePairFormat extends JsonFormat[WitnessSignaturePair] {
    final private val PARAM_WITNESS: String = "witness"
    final private val PARAM_SIGNATURE: String = "signature"

    override def read(json: JsValue): WitnessSignaturePair = json.asJsObject().getFields(PARAM_WITNESS, PARAM_SIGNATURE) match {
      case Seq(witness@JsString(_), signature@JsString(_)) => WitnessSignaturePair(
        witness.convertTo[PublicKey],
        signature.convertTo[Signature]
      )
      case _ => throw new IllegalArgumentException(s"Can't parse json value $json to a WitnessSignaturePair object")
    }

    override def write(obj: WitnessSignaturePair): JsValue = JsObject(
      PARAM_WITNESS -> JsString(obj.witness.base64Data.data),
      PARAM_SIGNATURE -> JsString(obj.signature.signature.data),
    )
  }
}
