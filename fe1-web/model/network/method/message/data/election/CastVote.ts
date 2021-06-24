import {
  Hash, Timestamp, Vote,
} from 'model/objects';
import { ProtocolError } from 'model/network/ProtocolError';
import { validateDataObject } from 'model/network/validation';
import { ActionType, MessageData, ObjectType } from '../MessageData';
import { checkTimestampStaleness } from '../Checker';

export class CastVote implements MessageData {
  public readonly object: ObjectType = ObjectType.ELECTION;

  public readonly action: ActionType = ActionType.CAST_VOTE;

  public readonly lao: Hash;

  public readonly election: Hash;

  public readonly created_at: Timestamp;

  public readonly votes: Vote[];

  constructor(msg: Partial<CastVote>) {
    if (!msg.election) {
      throw new ProtocolError('Undefined \'id\' parameter encountered during \'CastVote\'');
    }

    if (!msg.lao) {
      throw new ProtocolError('Undefined \'lao\' parameter encountered during \'CastVote\'');
    }
    this.lao = msg.lao;

    if (!msg.created_at) {
      throw new ProtocolError('Undefined \'created_at\' parameter encountered during \'CastVote\'');
    }
    checkTimestampStaleness(msg.created_at);
    this.created_at = msg.created_at;
    if (!msg.votes) {
      throw new ProtocolError('Undefined \'votes\' parameter encountered during \'CastVote\'');
    }
    CastVote.validateVotes(msg.votes);
    this.votes = msg.votes;

    if (!msg.election) {
      throw new ProtocolError('Invalid \'election\' parameter encountered during \'CastVote\'');
    }
    this.election = msg.election;
  }

  public static validateVotes(votes: Vote[]) {
    votes.forEach((vote) => {
      if (!vote.id) {
        throw new ProtocolError('Undefined \'vote id\' parameter encountered during \'CastVote\'');
      }
      if (!vote.question) {
        throw new ProtocolError('Undefined \'question id\' parameter encountered during \'CastVote\'');
      }
      if (!vote.vote && !vote.write_in) {
        throw new ProtocolError('Undefined \'vote or write in\' parameters encountered during \'CastVote\'');
      }
      if (vote.vote && vote.write_in) {
        throw new ProtocolError('Defined both \'vote\' and \'write_in\' parameters, only 1 is allowed, encountered during \'CastVote\'');
      }
    });
  }

  public static fromJson(obj: any): CastVote {
    const { errors } = validateDataObject(ObjectType.ELECTION, ActionType.CAST_VOTE, obj);

    if (errors !== null) {
      throw new ProtocolError(`Invalid cast vote\n\n${errors}`);
    }

    return new CastVote({
      ...obj,
      created_at: new Timestamp(obj.created_at),
      election: new Hash(obj.election),
      lao: new Hash(obj.lao),
    });
  }
}
