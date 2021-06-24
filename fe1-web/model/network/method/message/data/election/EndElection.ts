import {
  Hash, Timestamp,
} from 'model/objects';
import { ProtocolError } from 'model/network/ProtocolError';
import { validateDataObject } from 'model/network/validation';
import { ActionType, MessageData, ObjectType } from '../MessageData';
import { checkTimestampStaleness } from '../Checker';

export class EndElection implements MessageData {
  public readonly object: ObjectType = ObjectType.ELECTION;

  public readonly action: ActionType = ActionType.END;

  public readonly lao: Hash;

  public readonly election: Hash;

  public readonly created_at: Timestamp;

  public readonly registered_votes: Hash;

  constructor(msg: Partial<EndElection>) {
    if (!msg.election) {
      throw new ProtocolError('Undefined \'id\' parameter encountered during \'EndElection\'');
    }

    if (!msg.lao) {
      throw new ProtocolError('Undefined \'lao\' parameter encountered during \'EndElection\'');
    }
    this.lao = msg.lao;

    if (!msg.created_at) {
      throw new ProtocolError('Undefined \'created_at\' parameter encountered during \'EndElection\'');
    }
    checkTimestampStaleness(msg.created_at);
    this.created_at = msg.created_at;
    if (!msg.registered_votes) {
      throw new ProtocolError('Undefined \'registered_votes\' parameter encountered during \'EndElection\'');
    }

    this.registered_votes = msg.registered_votes;

    if (!msg.election) {
      throw new ProtocolError('Invalid \'election\' parameter encountered during \'EndElection\'');
    }
    this.election = msg.election;
  }

  public static fromJson(obj: any): EndElection {
    const { errors } = validateDataObject(ObjectType.ELECTION, ActionType.END, obj);

    if (errors !== null) {
      throw new ProtocolError(`Invalid election end\n\n${errors}`);
    }

    return new EndElection({
      ...obj,
      created_at: new Timestamp(obj.created_at),
      election: new Hash(obj.election),
      lao: new Hash(obj.lao),
    });
  }
}
