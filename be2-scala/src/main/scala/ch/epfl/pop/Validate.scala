package ch.epfl.pop

import java.util.Arrays
import ch.epfl.pop.crypto.{Hash, Signature}
import ch.epfl.pop.json.{Actions, HashJson, Key, MessageContent, MessageContentData, MessageErrorContent}
import ch.epfl.pop.crypto.Signature.verify
import ch.epfl.pop.json.JsonMessages.{BroadcastLaoMessageClient, BroadcastMeetingMessageClient, CloseRollCallMessageClient, CreateLaoMessageClient, CreateMeetingMessageClient, CreateRollCallMessageClient, OpenRollCallMessageClient, UpdateLaoMessageClient, WitnessMessageMessageClient}
import ch.epfl.pop.json.JsonUtils.ErrorCodes.InvalidData

import java.util

/**
 * Methods used to verify that messages follow the protocol
 */
object Validate {

  /**
   * Verify that the signature and the id of the message are correct
   * @param content the message
   * @return None if correct, an error otherwise
   */
  def validate(content: MessageContent): Option[MessageErrorContent] = {
    if(!verify(content.encodedData, content.signature, content.sender)) {
      Some(MessageErrorContent(InvalidData.id, "Invalid signature"))
    }
    else {
      val id = Hash.computeMessageId(content.encodedData, content.signature)
      if(util.Arrays.equals(id, content.message_id)) {
        None
      }
      else {
        Some(MessageErrorContent(InvalidData.id, "Invalid message id"))
      }
    }
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @return None if correct, an error otherwise
   */
  def validate(msg: CreateLaoMessageClient): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val lao = midLevelMsg.data
    val id = Hash.computeLAOId(lao.organizer, lao.creation, lao.name)

    if (!util.Arrays.equals(id, lao.id)) getError("Invalid LAO id.")
    else if (! (lao.creation > 0)) getError("Creation timestamp should be positive.")
    else if (! util.Arrays.equals(midLevelMsg.sender, lao.organizer))
      getError("The sender of the message should be the same as the organizer of the LAO.")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @return None if correct, an error otherwise
   */
  def validate(msg: UpdateLaoMessageClient): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val laoUpdate = midLevelMsg.data

    if (!(laoUpdate.last_modified > 0))
      getError("Last modified should be positive")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @param laoMod the creation/update message this message refers to
   * @return None if correct, an error otherwise
   */
  def validate(msg: BroadcastLaoMessageClient, laoMod : MessageContentData): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val laoState = midLevelMsg.data

    val same = " should be the same in the state and the creation/modification message."
    if (!util.Arrays.equals(laoMod.id, laoState.id))
      getError("LAO id" + same)
    else if (laoMod.action == Actions.Create && !(laoState.creation == laoMod.creation))
      getError("The creation timestamp" + same )
    else if(laoMod.action == Actions.Create && !(laoState.last_modified == laoState.creation))
      getError("The creation and last_modified fields should be the same when creating an LAO")
    else if (!(laoState.name == laoMod.name))
      getError("The name" + same)
    else if (laoMod.action == Actions.UpdateProperties && !(laoState.last_modified == laoMod.last_modified))
      getError("The last modified timestamp" + same)
    else if (!util.Arrays.equals(laoState.organizer, laoMod.organizer))
      getError("The organizer" + same)
    else if (!(laoState.witnesses.length == laoMod.witnesses.length))
      getError("The number of witnesses" + same)
    else if (!compareWitnesses(laoState.witnesses, laoMod.witnesses))
      getError("Witnesses" + same)
    else if (!util.Arrays.equals(midLevelMsg.sender, laoState.organizer))
      getError("The sender of the message should be the same as the organizer of the LAO.")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @return None if correct, an error otherwise
   */
  def validate(msg: WitnessMessageMessageClient): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val witnessMsg = midLevelMsg.data
    if(! Signature.verify(witnessMsg.message_id, witnessMsg.signature, midLevelMsg.sender))
      getError("Invalid witness signature.")
    else
      None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @param laoId the id of the LAO
   * @return None if correct, an error otherwise
   */
  def validate(msg: CreateMeetingMessageClient, laoId: HashJson): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val createMsg = midLevelMsg.data
    val id = Hash.computeMeetingId(laoId, createMsg.creation, createMsg.name)
    if (!(util.Arrays.equals(id, createMsg.id)))
      getError("Invalid meeting id.")
    else if (!(createMsg.creation > 0))
      getError("Creation timestamp should be positive.")
    else if (!(createMsg.creation == createMsg.last_modified))
      getError("Creation timestamp should be equals to last_modified.")
    else if (!(createMsg.start > createMsg.creation))
      getError("Meeting start time should be bigger than the creation time.")
    else if (!(createMsg.end > createMsg.start))
      getError("Meeting end should be bigger than meeting start")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @param meetingMod the creation/update message this message refers to
   * @return None if correct, an error otherwise
   */
  def validate(msg: BroadcastMeetingMessageClient, meetingMod : MessageContentData): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val stateMsg = midLevelMsg.data
    val same = " should be the same in the state and the creation/modification message."

    if (!(util.Arrays.equals(stateMsg.id, meetingMod.id)))
      getError("The meeting id" + same)
    else if (!(stateMsg.name == meetingMod.name))
      getError("The meeting name" + same)
    else if (!(stateMsg.creation == meetingMod.creation))
      getError("The creation timestamp" + same)
    else if(!(stateMsg.start == meetingMod.start))
      getError("The start timestamp" + same)
    else if (!(stateMsg.end == meetingMod.end))
      getError("The end timestamp")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @param laoId the id of the LAO
   * @return None if correct, an error otherwise
   */
  def validate(msg: CreateRollCallMessageClient, laoId: HashJson): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val createMsg = midLevelMsg.data

    if(!util.Arrays.equals(Hash.computeRollCallId(laoId, createMsg.creation, createMsg.name), createMsg.id))
      getError("Invalid roll-call id.")
    else if (!(createMsg.creation > 0))
      getError("Creation timestamp should be positive.")
    else if(!(createMsg.start >= createMsg.creation || createMsg.scheduled > createMsg.creation))
      getError("The start/scheduled time should be greater than the creation time")
    else
      None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @return None if correct, an error otherwise
   */
  def validate(msg: OpenRollCallMessageClient): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val openMessage = midLevelMsg.data

    if (!(openMessage.start > 0))
      getError("Start timestamp should be positive.")
    else None
  }

  /**
   * Verify that the message parameters are valid, according to the protocol
   * @param msg the message to validate
   * @return None if correct, an error otherwise
   */
  def validate(msg: CloseRollCallMessageClient): Option[MessageErrorContent] = {
    val midLevelMsg = msg.params.message.get
    val closeMessage = midLevelMsg.data

    if (!(closeMessage.start > 0))
      getError("Start timestamp should be positive.")
    else if (!(closeMessage.end > closeMessage.start))
      getError("End time should be greater than start time.")
    else None
  }

  private def compareWitnesses(w1: List[Key], w2: List[Key]): Boolean = {
    assert(w1.length == w2.length)
    w1.zip(w2).forall(p => util.Arrays.equals(p._1, p._2))
  }
  private def getError(error: String): Some[MessageErrorContent] = {
    Some(MessageErrorContent(InvalidData.id, error))
  }
}
