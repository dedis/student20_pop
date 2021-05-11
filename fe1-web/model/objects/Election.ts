import { LaoEvent, LaoEventState, LaoEventType } from './LaoEvent';
import { Hash } from './Hash';
import { Timestamp } from './Timestamp';

export interface ElectionState extends LaoEventState {
  lao: string;
  name: string;
  version: string;
  created_at: number;
  start: number;
  end: number;
  questions: Question[];
}

export interface Question {
  id: string,
  question: string,
  voting_method: string,
  ballot_options: string[],
  write_in: boolean,
}

export class Election implements LaoEvent {
  public readonly lao: Hash;

  public readonly id: Hash;

  public readonly name: string;

  public readonly version: string;

  public readonly created_at: Timestamp;

  public readonly start: Timestamp;

  public readonly end: Timestamp;

  public readonly questions: Question[];

  constructor(obj: Partial<Election>) {
    if (obj === undefined || obj === null) {
      throw new Error('Error encountered while creating a RollCall object: '
        + 'undefined/null parameters');
    }
    if (obj.lao === undefined) {
      throw new Error("Undefined 'lao' when creating 'RollCall'");
    }
    if (obj.id === undefined) {
      throw new Error("Undefined 'id' when creating 'RollCall'");
    }
    if (obj.name === undefined) {
      throw new Error("Undefined 'name' when creating 'RollCall'");
    }
    if (obj.version === undefined) {
      throw new Error("Undefined 'version' when creating 'RollCall'");
    }
    if (obj.created_at === undefined) {
      throw new Error("Undefined 'creation' when creating 'RollCall'");
    }
    if (obj.start === undefined) {
      throw new Error("Undefined 'start' when creating 'RollCall'");
    }
    if (obj.end === undefined) {
      throw new Error("Undefined 'end' when creating 'RollCall'");
    }
    if (obj.questions === undefined) {
      throw new Error("Undefined 'questions' when creating 'RollCall'");
    }
    this.lao = obj.lao;
    this.id = obj.id;
    this.name = obj.name;
    this.version = obj.version;
    this.created_at = obj.created_at;
    this.start = obj.start;
    this.end = obj.end;
    this.questions = obj.questions;
  }

  public static fromState(e: ElectionState): Election {
    return new Election({
      lao: new Hash(e.lao),
      id: new Hash(e.id),
      name: e.name,
      version: e.version,
      created_at: new Timestamp(e.created_at),
      start: new Timestamp(e.start),
      end: new Timestamp(e.end),
      questions: e.questions,
    });
  }

  public toState(): ElectionState {
    const obj: any = JSON.parse(JSON.stringify(this));
    return {
      ...obj,
      eventType: LaoEventType.ELECTION,
    };
  }
}
