import { Message } from 'model/network/method/message';
import { ObjectType } from 'model/network/method/message/data';
import { handleLaoMessage } from './Lao';
import { handleWitnessMessage } from './Witness';
import { handleMeetingMessage } from './Meeting';
import { handleRollCallMessage } from './RollCall';
import { handleElectionMessage } from './Election';

/** Processes the messages from storage by dispatching them to the right handler
 *
 * @param msg a message
 *
 * @returns false if the message could not be handled
 * @returns true if the message was handled
 */
export function handleMessage(msg: Message) {
  switch (msg.messageData.object) {
    case ObjectType.LAO:
      return handleLaoMessage(msg);
    case ObjectType.MESSAGE:
      return handleWitnessMessage(msg);
    case ObjectType.MEETING:
      return handleMeetingMessage(msg);
    case ObjectType.ROLL_CALL:
      return handleRollCallMessage(msg);
    case ObjectType.ELECTION:
      return handleElectionMessage(msg);
    default:
      console.warn('A message was received and ignored because'
        + ' its processing logic is not yet implemented:', msg);
      return true; // pretend it's been handled
  }
}
