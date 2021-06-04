package ch.epfl.pop.model.network.method.message.data

import ch.epfl.pop.model.network.method.message.data.ActionType.ActionType
import ch.epfl.pop.model.network.method.message.data.ObjectType.ObjectType
import ch.epfl.pop.model.network.method.message.data.lao.{CreateLao, StateLao, UpdateLao}
import ch.epfl.pop.model.network.method.message.data.meeting.{CreateMeeting, StateMeeting}
import ch.epfl.pop.model.network.method.message.data.rollCall.{CloseRollCall, CreateRollCall, OpenRollCall, ReopenRollCall}
import ch.epfl.pop.model.network.method.message.data.witness.WitnessMessage

/*
 * Helps building MessageData instances
 */
object DataBuilder {
  /**
   * Builds a MessageData from its headers ('object' and 'action' fields) and its json representation
   *
   * @param _object 'object' field of the message data
   * @param action 'action' field of the message data
   * @param payload json string representation of the message data
   * @throws ch.epfl.pop.model.network.method.message.data.ProtocolException if the tuple (_object, action)
   *         doesn't make sense with respect to the protocol
   * @return
   */
  @throws(classOf[ProtocolException])
  def buildData(_object: ObjectType, action: ActionType, payload: String): MessageData = _object match {
    case ObjectType.LAO => buildLaoData(action, payload)
    case ObjectType.MEETING => buildMeetingData(action, payload)
    case ObjectType.ROLL_CALL => buildRollCallData(action, payload)
    case ObjectType.MESSAGE => buildWitnessData(action, payload)
    case _ => throw new ProtocolException(s"Unknown object '${_object}' encountered while creating a Data")
  }

  private def buildLaoData(action: ActionType, payload: String): MessageData = action match {
    case ActionType.CREATE => CreateLao.buildFromJson(payload)
    case ActionType.STATE => StateLao.buildFromJson(payload)
    case ActionType.UPDATE_PROPERTIES => UpdateLao.buildFromJson(payload)
    case _ => throw new ProtocolException(s"Unknown action '$action' encountered while creating a Lao Data")
  }

  private def buildMeetingData(action: ActionType, payload: String): MessageData = action match {
    case ActionType.CREATE => CreateMeeting.buildFromJson(payload)
    case ActionType.STATE => StateMeeting.buildFromJson(payload)
    case _ => throw new ProtocolException(s"Unknown action '$action' encountered while creating a Meeting Data")
  }

  private def buildRollCallData(action: ActionType, payload: String): MessageData = action match {
    case ActionType.CREATE => CreateRollCall.buildFromJson(payload)
    case ActionType.OPEN => OpenRollCall.buildFromJson(payload)
    case ActionType.REOPEN => ReopenRollCall.buildFromJson(payload)
    case ActionType.CLOSE => CloseRollCall.buildFromJson(payload)
    case _ => throw new ProtocolException(s"Unknown action '$action' encountered while creating a RollCall Data")
  }

  private def buildWitnessData(action: ActionType, payload: String): MessageData = action match {
    case ActionType.WITNESS => WitnessMessage.buildFromJson(payload)
    case _ => throw new ProtocolException(s"Unknown action '$action' encountered while creating a Witness Data")
  }
}
