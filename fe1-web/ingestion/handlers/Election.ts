import { Message } from 'model/network/method/message';
import {
  ActionType,
  CastVote,
  ElectionResult,
  EndElection,
  ObjectType,
  SetupElection,
} from 'model/network/method/message/data';
import {
  channelFromIds, Election, ElectionStatus, RegisteredVote, getLastChannel,
} from 'model/objects';
import {
  addEvent, dispatch, getStore, KeyPairStore, makeCurrentLao, updateEvent,
} from 'store';
import { subscribeToChannel } from 'network/CommunicationApi';
import { getEventFromId } from './Utils';

const getCurrentLao = makeCurrentLao();

function handleElectionSetupMessage(msg: Message): boolean {
  if (msg.messageData.object !== ObjectType.ELECTION
    || msg.messageData.action !== ActionType.SETUP) {
    console.warn('handleElectionSetupMessage was called to process an unsupported message', msg);
    return false;
  }

  const makeErr = (err: string) => `election/setup was not processed: ${err}`;

  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }

  const elecMsg = msg.messageData as SetupElection;

  const election = new Election({
    lao: elecMsg.lao,
    id: elecMsg.id,
    name: elecMsg.name,
    version: elecMsg.version,
    created_at: elecMsg.created_at,
    start: elecMsg.start_time,
    end: elecMsg.end_time,
    questions: elecMsg.questions,
    registered_votes: [],
  });

  // Subscribing to the election channel corresponding to that election
  const electionChannel = channelFromIds(election.lao, election.id);
  subscribeToChannel(electionChannel).then(() => {}).catch((err) => {
    console.error('Could not subscribe to Election channel, error:', err);
  });

  dispatch(addEvent(lao.id, election.toState()));
  return true;
}

function handleCastVoteMessage(msg: Message): boolean {
  if (msg.messageData.object !== ObjectType.ELECTION
    || msg.messageData.action !== ActionType.CAST_VOTE) {
    console.warn('handleCastVoteMessage was called to process an unsupported message', msg);
    return false;
  }
  const makeErr = (err: string) => `election/cast-vote was not processed: ${err}`;
  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }
  const myPublicKey = KeyPairStore.getPublicKey();
  const isOrganizer = lao.organizer.equals(myPublicKey);
  const isWitness = lao.witnesses.some((w) => w.equals(myPublicKey));
  if (!isOrganizer && !isWitness) {
    // Then current user is an attendee and doesn't have to store the votes
    return true;
  }

  const castVoteMsg = msg.messageData as CastVote;
  const currentVote: RegisteredVote = {
    createdAt: castVoteMsg.created_at,
    sender: msg.sender,
    votes: castVoteMsg.votes,
    messageId: msg.message_id,
  };
  const election = getEventFromId(storeState, castVoteMsg.election) as Election;
  if (!election) {
    console.warn(makeErr('No active election to register vote '));
    return false;
  }

  if (election.registered_votes.some(
    (votes) => votes.sender.toString() === currentVote.sender.toString(),
  )) { // Update the vote if the person has already voted before
    election.registered_votes = election.registered_votes.map(
      (prevVote) => (
        prevVote.sender.toString() === currentVote.sender.toString()
        && prevVote.createdAt.valueOf() < currentVote.createdAt.valueOf() ? currentVote : prevVote),
    );
  } else {
    election.registered_votes = [...election.registered_votes, currentVote];
  }
  dispatch(updateEvent(lao.id, election.toState()));
  return true;
}

function handleElectionEndMessage(msg: Message) {
  console.log('Handling Election end message');
  if (msg.messageData.object !== ObjectType.ELECTION
    || msg.messageData.action !== ActionType.END) {
    console.warn('handleElectionEndMessage was called to process an unsupported message', msg);
    return false;
  }
  const makeErr = (err: string) => `election/end was not processed: ${err}`;
  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }
  const ElectionEndMsg = msg.messageData as EndElection;
  const election = getEventFromId(storeState, ElectionEndMsg.election) as Election;
  if (!election) {
    console.warn(makeErr('No active election to end'));
    return false;
  }

  // Change election status here such that it will change the election display in the event list
  election.electionStatus = ElectionStatus.TERMINATED;
  dispatch(updateEvent(lao.id, election.toState()));
  return true;
}

function handleElectionResultMessage(msg: Message) {
  if (msg.messageData.object !== ObjectType.ELECTION
    || msg.messageData.action !== ActionType.RESULT) {
    console.warn('handleElectionResultMessage was called to process an unsupported message', msg);
    return false;
  }
  const makeErr = (err: string) => `election/Result was not processed: ${err}`;
  const storeState = getStore().getState();
  const lao = getCurrentLao(storeState);
  if (!lao) {
    console.warn(makeErr('no LAO is currently active'));
    return false;
  }
  if (!msg.channel) {
    console.warn(makeErr('No channel found in message'));
    return false;
  }
  const electionId = getLastChannel(msg.channel);
  const ElectionResultMsg = msg.messageData as ElectionResult;
  const election = getEventFromId(storeState, electionId) as Election;
  if (!election) {
    console.warn(makeErr('No active election for the result'));
    return false;
  }

  election.questionResult = ElectionResultMsg.questions;
  election.electionStatus = ElectionStatus.RESULT;
  dispatch(updateEvent(lao.id, election.toState()));
  console.log('received election Result message: ', ElectionResultMsg);
  return true;
}

export function handleElectionMessage(msg: Message) {
  if (msg.messageData.object !== ObjectType.ELECTION) {
    console.warn('handleElectionMessage was called to process an unsupported message', msg);
    return false;
  }

  switch (msg.messageData.action) {
    case ActionType.SETUP:
      return handleElectionSetupMessage(msg);
    case ActionType.CAST_VOTE:
      return handleCastVoteMessage(msg);
    case ActionType.END:
      return handleElectionEndMessage(msg);
    case ActionType.RESULT:
      return handleElectionResultMessage(msg);

    default:
      console.warn('A Election message was received but'
        + ' its processing logic is not yet implemented:', msg);
      return false;
  }
}
