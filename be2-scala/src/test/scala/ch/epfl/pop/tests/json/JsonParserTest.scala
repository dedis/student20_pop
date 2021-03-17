package ch.epfl.pop.tests.json

import ch.epfl.pop.json.HighLevelProtocol._
import ch.epfl.pop.json.Objects
import ch.epfl.pop.model.network.method.message.data.lao.{CreateLao, StateLao, UpdateLao}
import ch.epfl.pop.model.network.method.message.data.meeting.{CreateMeeting, StateMeeting}
import ch.epfl.pop.model.network.method.message.data.rollCall.{CloseRollCall, CreateRollCall, OpenRollCall}
import ch.epfl.pop.model.network.method.message.data.{ActionType, ObjectType}
import ch.epfl.pop.model.objects._
import ch.epfl.pop.tests.json.JsonParserTestsUtils.embeddedMessage
import org.scalatest.Matchers.convertToAnyShouldWrapper
import org.scalatest.{FunSuite, Matchers}
import spray.json.{JsArray, JsNumber, JsObject, JsString, JsValue, enrichAny}

class JsonParserTest extends FunSuite with Matchers {

  val id : Hash = Hash(Base64Data("1D"))
  val name : String = "name"
  val location : String = "location"
  val start : Timestamp = Timestamp(222)
  val end : Timestamp = Timestamp(222)
  val creation : Timestamp = Timestamp(222)
  val last_modified : Timestamp = Timestamp(222)
  val organizer : PublicKey = PublicKey(Base64Data("0RG"))
  val witness1 : PublicKey = PublicKey(Base64Data("W1T1"))
  val witness2 : PublicKey = PublicKey(Base64Data("W1T2"))
  val attendee1 : PublicKey = PublicKey(Base64Data("4TT1"))
  val attendee2 : PublicKey = PublicKey(Base64Data("4TT2"))
  val modification_id : Hash = Hash(Base64Data("M0D1D"))
  val signature1 : Signature = Signature(Base64Data("S1G1"))
  val signature2 : Signature = Signature(Base64Data("S1G2"))

  test("Objects") {
    val base64Data = Base64Data("233")
    base64Data.toJson shouldBe JsString("233")
    base64Data.toJson.convertTo[Base64Data] shouldBe base64Data

    val hash = Hash(base64Data)
    hash.toJson shouldBe JsString("233")
    hash.toJson.convertTo[Hash] shouldBe hash

    val publicKey = PublicKey(base64Data)
    publicKey.toJson shouldBe JsString("233")
    publicKey.toJson.convertTo[PublicKey] shouldBe publicKey

    val signature = Signature(base64Data)
    signature.toJson shouldBe JsString("233")
    signature.toJson.convertTo[Signature] shouldBe signature

    val timestamp = Timestamp(22)
    timestamp.toJson shouldBe JsNumber(22)
    timestamp.toJson.convertTo[Timestamp] shouldBe timestamp

    val witnessSignature = WitnessSignaturePair(publicKey, signature)
    val jsonWitnessSignature = JsObject(
      "witness" -> JsString(publicKey.base64Data.data),
      "signature" -> JsString(signature.signature.data),
    )
    witnessSignature.toJson shouldBe jsonWitnessSignature
    witnessSignature.toJson.convertTo[WitnessSignaturePair] shouldBe witnessSignature
  }

  test("CreateLao") {
    val jsonCreateLao = s"""{
                           |    "object": "${ObjectType.LAO.toString}",
                           |    "action": "${ActionType.CREATE.toString}",
                           |    "id": "${id.base64Data.data}",
                           |    "name": "$name",
                           |    "creation": ${creation.time.toString},
                           |    "organizer": "${organizer.base64Data.data}",
                           |    "witnesses": ["${witness1.base64Data.data}", "${witness2.base64Data.data}"]
                           |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val createLao = CreateLao(id, name, creation, organizer, List(witness1, witness2))

    CreateLao.buildFromJson(createLao, jsonCreateLao) shouldBe createLao
  }

  test("StateLao") {
    val jsonStateLao = s"""{
                          |    "object": "${ObjectType.LAO.toString}",
                          |    "action": "${ActionType.STATE.toString}",
                          |    "id": "${id.base64Data.data}",
                          |    "name": "$name",
                          |    "creation": ${creation.time.toString},
                          |    "last_modified": ${last_modified.time.toString},
                          |    "organizer": "${organizer.base64Data.data}",
                          |    "witnesses": ["${witness1.base64Data.data}", "${witness2.base64Data.data}"],
                          |    "modification_id": "${modification_id.base64Data.data}",
                          |    "modification_signatures":[
                          |      {"witness": "${witness1.base64Data.data}", "signature": "${signature1.signature.data}"},
                          |      {"witness": "${witness2.base64Data.data}", "signature": "${signature2.signature.data}"}
                          |    ]
                          |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val stateLao = StateLao(id, name, creation, last_modified, organizer, List(witness1, witness2), modification_id,
      List(WitnessSignaturePair(witness1, signature1), WitnessSignaturePair(witness2, signature2)))

    StateLao.buildFromJson(stateLao, jsonStateLao) shouldBe stateLao
  }

  test("UpdateLao") {
    val jsonUpdateLao = s"""{
                           |    "object": "${ObjectType.LAO.toString}",
                           |    "action": "${ActionType.UPDATE_PROPERTIES.toString}",
                           |    "id": "${id.base64Data.data}",
                           |    "name": "$name",
                           |    "last_modified": ${last_modified.time.toString},
                           |    "witnesses": ["${witness1.base64Data.data}", "${witness2.base64Data.data}"]
                           |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val updateLao = UpdateLao(id, name, last_modified, List(witness1, witness2))

    UpdateLao.buildFromJson(updateLao, jsonUpdateLao) shouldBe updateLao
  }

  test("CreateMeeting") {
    val jsonCreateMeeting = s"""{
                           |    "object": "${ObjectType.MEETING.toString}",
                           |    "action": "${ActionType.CREATE.toString}",
                           |    "id": "${id.base64Data.data}",
                           |    "name": "$name",
                           |    "creation": ${creation.time.toString},
                           |    "location": "$location",
                           |    "start": ${start.time.toString},
                           |    "end": ${end.time.toString},
                           |    "extra": ""
                           |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val createMeeting = CreateMeeting(id, name, creation, Some(location), start, Some(end), None)

    CreateMeeting.buildFromJson(createMeeting, jsonCreateMeeting) shouldBe createMeeting
  }

  test("StateMeeting") {
    val jsonCreateMeeting = s"""{
                               |    "object": "${ObjectType.MEETING.toString}",
                               |    "action": "${ActionType.STATE.toString}",
                               |    "id": "${id.base64Data.data}",
                               |    "name": "$name",
                               |    "creation": ${creation.time.toString},
                               |    "last_modified": ${last_modified.time.toString},
                               |    "location": "$location",
                               |    "start": ${start.time.toString},
                               |    "end": ${end.time.toString},
                               |    "extra": "",
                               |    "modification_id": "${modification_id.base64Data.data}",
                               |    "modification_signatures":[
                               |      {"witness": "${witness1.base64Data.data}", "signature": "${signature1.signature.data}"},
                               |      {"witness": "${witness2.base64Data.data}", "signature": "${signature2.signature.data}"}
                               |    ]
                               |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val stateMeeting = StateMeeting(id, name, creation, last_modified, Some(location), start, Some(end), None,
      modification_id, List(WitnessSignaturePair(witness1, signature1), WitnessSignaturePair(witness2, signature2)))

    StateMeeting.buildFromJson(stateMeeting, jsonCreateMeeting) shouldBe stateMeeting
  }

  test("CloseRollCall") {
    val jsonCloseRollCall = s"""{
                               |    "object": "${ObjectType.ROLL_CALL.toString}",
                               |    "action": "${ActionType.CLOSE.toString}",
                               |    "update_id": "${id.base64Data.data}",
                               |    "closes": "${id.base64Data.data}",
                               |    "end": ${end.time.toString},
                               |    "attendees": ["${attendee1.base64Data.data}", "${attendee2.base64Data.data}"]
                               |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val closeRollCall = CloseRollCall(id, id, end, List(attendee1, attendee2))
    CloseRollCall.buildFromJson(closeRollCall, jsonCloseRollCall) shouldBe closeRollCall
  }

  test("CreateRollCall") {
    val jsonCreateRollCall = s"""{
                               |    "object": "${ObjectType.ROLL_CALL.toString}",
                               |    "action": "${ActionType.CLOSE.toString}",
                               |    "id": "${id.base64Data.data}",
                               |    "name": "$name",
                               |    "creation": ${creation.time.toString},
                               |    "start": ${start.time.toString},
                               |    "location": "$location"
                               |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val createRollCall = CreateRollCall(id, name, creation, Some(start), None, location, None)
    CreateRollCall.buildFromJson(createRollCall, jsonCreateRollCall) shouldBe createRollCall
  }

  test("OpenRollCall") {
    val jsonOpenRollCall = s"""{
                                |    "object": "${ObjectType.ROLL_CALL.toString}",
                                |    "action": "${ActionType.CLOSE.toString}",
                                |    "update_id": "${id.base64Data.data}",
                                |    "opens": "${id.base64Data.data}",
                                |    "start": ${start.time.toString}
                                |}""".stripMargin.filterNot((c: Char) => c.isWhitespace)

    val openRollCall = OpenRollCall(id, id, start)
    OpenRollCall .buildFromJson(openRollCall, jsonOpenRollCall) shouldBe openRollCall
  }
}
