import { Message } from 'model/network/method/message';
import {
  ActionType,
  CloseRollCall,
  CreateRollCall,
  ObjectType,
  OpenRollCall,
} from 'model/network/method/message/data';
import { RollCall, RollCallStatus } from 'model/objects';
import {
  addEvent, dispatch, getStore, makeCurrentLao, updateEvent,
} from 'store';
import { getEventFromId, hasWitnessSignatureQuorum } from './Utils';

const getCurrentLao = makeCurrentLao();

function handleRollCallCreateMessage(msg: Message): boolean {
  if (msg.messageData.object !== ObjectType.ROLL_CALL
    || msg.messageData.action !== ActionType.CREATE) {
    console.warn('handleRollCallCreateMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `roll_call/create was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const rcMsgData = msg.messageData as CreateRollCall;

  const rc = new RollCall({
    id: rcMsgData.id,
    name: rcMsgData.name,
    location: rcMsgData.location,
    description: rcMsgData.description,
    creation: rcMsgData.creation,
    proposed_start: rcMsgData.proposed_start,
    proposed_end: rcMsgData.proposed_end,
    status: RollCallStatus.CREATED,
  });

  dispatch(addEvent(lao.id, rc.toState()));
  return true;
}

function handleRollCallOpenMessage(msg: Message): boolean {
  if (msg.messageData.object !== ObjectType.ROLL_CALL
    || msg.messageData.action !== ActionType.OPEN) {
    console.warn('handleRollCallOpenMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `roll_call/open was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const rcMsgData = msg.messageData as OpenRollCall;
  const oldRC = getEventFromId(storeState, rcMsgData.opens) as RollCall;
  if (!oldRC) {
    console.warn(makeErr("no known roll call matching the 'opens' field"));
    return false;
  }

  const rc = new RollCall({
    ...oldRC,
    idAlias: rcMsgData.update_id,
    opened_at: rcMsgData.opened_at,
    status: RollCallStatus.OPENED,
  });

  dispatch(updateEvent(lao.id, rc.toState()));
  return true;
}

function handleRollCallCloseMessage(msg: Message): boolean {
  if (msg.messageData.object !== ObjectType.ROLL_CALL
    || msg.messageData.action !== ActionType.CLOSE) {
    console.warn('handleRollCallCloseMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `roll_call/close was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const rcMsgData = msg.messageData as CloseRollCall;
  const oldRC = getEventFromId(storeState, rcMsgData.closes) as RollCall;
  if (!oldRC) {
    console.warn(makeErr("no known roll call matching the 'closes' field"));
    return false;
  }

  const rc = new RollCall({
    ...oldRC,
    idAlias: rcMsgData.update_id,
    closed_at: rcMsgData.closed_at,
    status: RollCallStatus.CLOSED,
    attendees: rcMsgData.attendees,
  });

  // We can now dispatch an updated (closed) roll call, containing the attendees' public keys.
  //
  // Future development:
  // We could either dispatch a new action containing our newfound PoP tokens,
  // or we could extend the KeyPair reducer to listen on this updateEvent, so that
  // we can automatically retrieve the PoP tokens when such an event happens.
  dispatch(updateEvent(lao.id, rc.toState()));
  return true;
}

export function handleRollCallMessage(msg: Message) {
  if (msg.messageData.object !== ObjectType.LAO) {
    console.warn('handleRollCallMessage was called to process an unsupported message', msg);
    return false;
  }

  if (!hasWitnessSignatureQuorum(msg.witness_signatures)) {
    console.info('Roll-call operation will not be processed until witness quorum is reached', msg);
    return false;
  }

  switch (msg.messageData.action) {
    case ActionType.CREATE:
      return handleRollCallCreateMessage(msg);

    case ActionType.OPEN:
      return handleRollCallOpenMessage(msg);

    case ActionType.CLOSE:
      return handleRollCallCloseMessage(msg);

    case ActionType.REOPEN:
      // TODO: implement this logic
      // fallthrough

    default:
      console.warn('A LAO message was received but'
        + ' its processing logic is not yet implemented:', msg);
      return false;
  }
}
