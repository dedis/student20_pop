package ch.epfl.pop.tests

import ch.epfl.pop.Validate
import ch.epfl.pop.crypto.Hash
import ch.epfl.pop.json.JsonMessages.{BroadcastLaoMessageClient, BroadcastMeetingMessageClient, CloseRollCallMessageClient, CreateLaoMessageClient, CreateMeetingMessageClient, CreateRollCallMessageClient, OpenRollCallMessageClient, UpdateLaoMessageClient, WitnessMessageMessageClient}
import ch.epfl.pop.json.JsonUtils.MessageContentDataBuilder
import ch.epfl.pop.json.{Actions, ByteArray, HashJson, MessageContent, Methods, Objects}
import ch.epfl.pop.tests.MessageCreationUtils._
import com.google.crypto.tink.subtle.Ed25519Sign.KeyPair
import org.scalatest.FunSuite

import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.Base64
class ValidateTest extends FunSuite {

  /* --------------- VALIDATE MESSAGE CONTENT --------------- */

  test("Validate message content when correct") {

    val kp = generateKeyPair()
    val msg = "This is my message"
    val encodedData = b64EncodeToString(msg.getBytes(UTF_8))
    val signature = sign(kp, msg.getBytes(UTF_8))
    val id = Hash.computeMessageId(encodedData, signature)
    val witnessSignatures = Nil
    val content: MessageContent = MessageContent(encodedData, null, kp.getPublicKey, signature, id, witnessSignatures)
    assert(Validate.validate(content).isEmpty)
  }

  test("Validate message content fails with incorrect signature") {

    val pk = generateKeyPair().getPublicKey
    val msg = "This is my message"
    val encodedData = b64EncodeToString(msg.getBytes(UTF_8))
    val signature = "incorrect signature".getBytes
    val id = Hash.computeMessageId(encodedData, signature)
    val witnessSignatures = Nil
    val content: MessageContent = MessageContent(encodedData, null, pk, signature, id, witnessSignatures)
    assert(Validate.validate(content).nonEmpty)
  }

  test("Validate message content fails with incorrect id") {

    val kp = generateKeyPair()
    val msg = "This is my message"
    val encodedData = b64EncodeToString(msg.getBytes(UTF_8))
    val signature = sign(kp, msg.getBytes(UTF_8))
    val id = Hash.computeMessageId(encodedData, "incorrect".getBytes)
    val witnessSignatures = Nil
    val content: MessageContent = MessageContent(encodedData, null, kp.getPublicKey, signature, id, witnessSignatures)
    assert(Validate.validate(content).nonEmpty)
  }

  /* --------------- VALIDATE CREATE LAO --------------- */

  private val laoName = "My LAO"
  private val laoCreation = 229388L
  private val kp: KeyPair = generateKeyPair()
  private val sk = kp.getPrivateKey
  private val pk = kp.getPublicKey
  private val laoOrganizer = kp.getPublicKey
  private val md = MessageDigest.getInstance("SHA-256")
  private val laoString = "[" + '"' + Base64.getEncoder.encodeToString(laoOrganizer) + "\",\"" + laoCreation.toString + "\",\"" +
    laoName + "\"]"
  private val laoId = md.digest(laoString.getBytes(UTF_8))
  private val root = "/root"
  private def createLao(id: HashJson = laoId, name: String = laoName, creation: Long = laoCreation,
                        organizer: ByteArray = laoOrganizer, sender: Array[Byte] = pk): CreateLaoMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Lao, Actions.Create)
      .setId(id)
      .setName(name)
      .setCreation(creation)
      .setOrganizer(organizer)
      .setWitnesses(Nil)
      .build()
    val params = getMessageParams(data, sender, sk, root)
    val create = CreateLaoMessageClient(params, 0, Methods.Publish)
    create
  }

  test("Create LAO validation works with correct parameters") {

    val create = createLao()
    assert(Validate.validate(create).isEmpty)
  }

  test("Create LAO validation fails with incorrect id") {
    val create = createLao(id = Array[Byte]())
    assert(Validate.validate(create).isDefined)
  }

  test("Create LAO validation fails with incorrect creation timestamp") {
    val create = createLao(creation = -1)
    assert(Validate.validate(create).isDefined)
  }

  test("Create LAO validation fails with incorrect organizer") {
    val create = createLao(sender = null)
    assert(Validate.validate(create).isDefined)
  }

  /* --------------- VALIDATE UPDATE LAO --------------- */

  private val updateLaoLastModified = laoCreation + 1
  private def updateLao(modified: Long = updateLaoLastModified, sender: Array[Byte] = pk): UpdateLaoMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Lao, Actions.UpdateProperties)
      .setId(laoId)
      .setName(laoName)
      .setCreation(laoCreation)
      .setLastModified(modified)
      .setWitnesses(Nil)
      .build()
    val params = getMessageParams(data, sender, sk, root)
    val update = UpdateLaoMessageClient(params, 0, Methods.Publish)
    update
  }

  test("Update LAO validation works with valid parameters") {
    assert(Validate.validate(updateLao()).isEmpty)
  }

  test("Update LAO validation fails with invalid modification timestamp") {
    assert(Validate.validate(updateLao(modified = -1)).isDefined)
  }

  /* --------------- VALIDATE BROADCAST LAO --------------- */

  private def broadcastLao(modificationId: HashJson, id: HashJson = laoId, creation: Long = laoCreation, name: String = laoName, lastModified: Long = laoCreation,
                           organizer: ByteArray = laoOrganizer, sender: Array[Byte] = pk): BroadcastLaoMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Lao, Actions.State)
      .setId(id)
      .setName(name)
      .setCreation(creation)
      .setLastModified(lastModified)
      .setOrganizer(organizer)
      .setWitnesses(Nil)
      .setModificationId(modificationId)
      .setModificationSignatures(Nil)
      .build()
    val params = getMessageParams(data, sender, sk, root)
    val broadcast = BroadcastLaoMessageClient(params, 0, Methods.Publish)
    broadcast
  }

  test("Broadcast LAO validation works with valid parameters") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id)
    assert(Validate.validate(broadcast, mod.data).isEmpty)
  }

  test("Broadcast LAO validation fails with LAO id different than in creation message") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, id = Array[Byte]())
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  test("Broadcast LAO validation fails with creation timestamp different than in creation message") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, creation = laoCreation - 100)
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  test("Broadcast LAO validation fails with last_modified timestamp different than in creation message") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, lastModified = laoCreation - 100)
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  test("Broadcast LAO validation fails with name different than in creation message") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, name = "I changed the name")
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  test("Broadcast LAO validation fails with organizer different than in creation message") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, organizer = Array[Byte]())
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  test("Broadcast LAO validation fails with sender different the organizer") {
    val mod = createLao().params.message.get
    val broadcast = broadcastLao(mod.message_id, sender = null)
    assert(Validate.validate(broadcast, mod.data).nonEmpty)
  }

  /* --------------- VALIDATE WITNESS --------------- */

  private def witness(key: Option[Array[Byte]] = None): WitnessMessageMessageClient = {
    val msgToWitness = createLao().params.message.get
    val witnessKp = generateKeyPair()
    val signingKey = key match {
      case Some(sk) => sk
      case None => witnessKp.getPrivateKey
    }
    val msgId = Base64.getEncoder.encode(msgToWitness.message_id)
    val signature = sign(signingKey, msgToWitness.message_id)
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Message, Actions.Witness)
      .setMessageId(new String(msgId))
      .setSignature(signature)
      .build()
    val params = getMessageParams(data, witnessKp, root)
    WitnessMessageMessageClient(params, 0, Methods.Publish)
  }

  test("Witness validation works with valid parameters") {
    val witnessMsg = witness()
    assert(Validate.validate(witnessMsg).isEmpty)
  }

  test("Witness validation fails with invalid signature") {
    val witnessMsg = witness(Some(sk))
    assert(Validate.validate(witnessMsg).nonEmpty)
  }

  /* --------------- VALIDATE CREATE MEETING --------------- */

  private val meetingName = "My meeting"
  private val meetingCreation = 98431198L
  private val meetingId = Hash.computeMeetingId(laoId, meetingCreation, meetingName)
  private val meetingLocation = "BC Hall"
  private val meetingStart = 98431198L + 1
  private val meetingEnd = meetingStart + 1

  private def createMeeting(id: ByteArray = meetingId, creation: Long = meetingCreation, start: Long = meetingStart,
                            end: Long = meetingEnd): CreateMeetingMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Meeting, Actions.Create)
      .setId(id)
      .setName(meetingName)
      .setCreation(creation)
      .setLastModified(creation)
      .setLocation(meetingLocation)
      .setStart(start)
      .setEnd(end)
      .build()
    val params = getMessageParams(data, pk, sk, root)
    CreateMeetingMessageClient(params, 0, Methods.Publish)
  }

  test("Create meeting validation works with valid parameters") {
    val meeting = createMeeting()
    assert(Validate.validate(meeting, laoId).isEmpty)
  }

  test("Create meeting validation fails with invalid meeting id") {
    val meeting = createMeeting(id = Array[Byte]())
    assert(Validate.validate(meeting, laoId).nonEmpty)
  }

  test("Create meeting validation fails with invalid creation timestamp") {
    val meeting = createMeeting(creation = -1)
    assert(Validate.validate(meeting, laoId).nonEmpty)
  }

  test("Create meeting validation fails with invalid start time") {
    val meeting = createMeeting(start = meetingCreation - 1000)
    assert(Validate.validate(meeting, laoId).nonEmpty)
  }

  test("Create meeting validation fails with invalid end time") {
    val meeting = createMeeting(end = meetingStart - 1000)
    assert(Validate.validate(meeting, laoId).nonEmpty)
  }

  /* --------------- VALIDATE BROADCAST MEETING --------------- */

  private def broadcastMeeting(modificationId: HashJson, id: HashJson = meetingId, name: String = meetingName,
                               creation: Long = meetingCreation, start: Long = meetingStart, end: Long = meetingEnd): BroadcastMeetingMessageClient = {

    val data = new MessageContentDataBuilder()
      .setHeader(Objects.Meeting, Actions.State)
      .setId(id)
      .setName(name)
      .setCreation(creation)
      .setLastModified(creation)
      .setLocation(meetingLocation)
      .setStart(start)
      .setEnd(end)
      .setModificationId(modificationId)
      .setModificationSignatures(Nil)
      .build()
    val params = getMessageParams(data, pk, sk, root)
    BroadcastMeetingMessageClient(params, 0, Methods.Publish)
  }

  test("Broadcast meeting validation works with valid parameters") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id)
    assert(Validate.validate(state, meetingMsg.data).isEmpty)
  }

  test("Broadcast meeting validation fails with meeting id different than in the creation message") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id, id = Array[Byte]())
    assert(Validate.validate(state, meetingMsg.data).nonEmpty)
  }

  test("Broadcast meeting validation fails with meeting name different than in the creation message") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id, name = "Changed name")
    assert(Validate.validate(state, meetingMsg.data).nonEmpty)
  }

  test("Broadcast meeting validation fails with meeting creation timestamp different than in the creation message") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id, creation = meetingCreation - 1)
    assert(Validate.validate(state, meetingMsg.data).nonEmpty)
  }

  test("Broadcast meeting validation fails with meeting start timestamp different than in the creation message") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id, start = meetingStart - 1)
    assert(Validate.validate(state, meetingMsg.data).nonEmpty)
  }

  test("Broadcast meeting validation fails with meeting end timestamp different than in the creation message") {
    val meetingMsg = createMeeting().params.message.get
    val state = broadcastMeeting(meetingMsg.message_id, end = meetingEnd - 1)
    assert(Validate.validate(state, meetingMsg.data).nonEmpty)
  }

  /* --------------- VALIDATE CREATE ROLL-CALL --------------- */

  private val rcCreation = 123213213L
  private val rcStart = rcCreation + 1
  private val rcName = "My roll-call"
  private val rcId = Hash.computeRollCallId(laoId, rcCreation, rcName)

  private def createRollCall(id: HashJson, creation: Long, start: Long): CreateRollCallMessageClient = {
  val data = new MessageContentDataBuilder()
    .setHeader(Objects.RollCall, Actions.Create)
    .setId(id)
    .setName(rcName)
    .setCreation(creation)
    .setStart(start)
    .build()

    val params = getMessageParams(data, pk, sk, root)
    CreateRollCallMessageClient(params, 0, Methods.Publish)
  }

  test("Create roll-call validation works with valid parameters") {
    val rcMsg = createRollCall(rcId, rcCreation, rcStart)
    assert(Validate.validate(rcMsg, laoId).isEmpty)
  }

  test("Create roll-call validation fails with invalid id") {
    //Replacing roll-call id by lao id
    val rcMsg = createRollCall(laoId, rcCreation, rcStart)
    assert(Validate.validate(rcMsg, laoId).isDefined)
  }

  test("Create roll-call validation fails with invalid start timestamp") {
    val rcMsg = createRollCall(laoId, rcCreation, rcCreation - 1)
    assert(Validate.validate(rcMsg, laoId).isDefined)
  }

  /* --------------- VALIDATE OPEN ROLL-CALL --------------- */

  private def openRollCall(start: Long): OpenRollCallMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.RollCall, Actions.Open)
      .setId(rcId)
      .setStart(start)
      .build()

    val params = getMessageParams(data, pk, sk, root)
    OpenRollCallMessageClient(params, 0, Methods.Publish)
  }

  test("Open roll-call validation works with valid parameters") {
    val rcMsg = openRollCall(rcStart)
    assert(Validate.validate(rcMsg).isEmpty)
  }

  test("Open roll-call validation fails with invalid start timestamp") {
    val rcMsg = openRollCall(0)
    assert(Validate.validate(rcMsg).isDefined)
  }

  /* --------------- VALIDATE CLOSE ROLL-CALL --------------- */

  private val rcEnd = rcStart + 1
  private def closeRollCall(start: Long, end: Long): CloseRollCallMessageClient = {
    val data = new MessageContentDataBuilder()
      .setHeader(Objects.RollCall, Actions.Close)
      .setId(rcId)
      .setStart(start)
      .setEnd(end)
      .setAttendees(List())
      .build()

    val params = getMessageParams(data, pk, sk, root)
    CloseRollCallMessageClient(params, 0, Methods.Publish)
  }

  test("Close roll-call validation works with valid parameters") {
    val rcMsg = closeRollCall(rcStart, rcEnd)
    assert(Validate.validate(rcMsg).isEmpty)
  }

  test("Close roll-call validation fails with invalid start timestamp") {
    val rcMsg = closeRollCall(0, rcEnd)
    assert(Validate.validate(rcMsg).isDefined)
  }

  test("Close roll-call validation fails with invalid end timestamp") {
    val rcMsg = closeRollCall(rcStart, rcStart)
    assert(Validate.validate(rcMsg).isDefined)
  }

}
