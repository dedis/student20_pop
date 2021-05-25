import { Hash, Timestamp } from 'model/objects';

export enum LaoEventType {
  MEETING = 'MEETING',
  ROLL_CALL = 'ROLL_CALL',
  ELECTION = 'ELECTION',
  // Election, Poll, etc.
}

export interface LaoEventState {
  readonly eventType: LaoEventType;

  readonly id: string;

  readonly idAlias?: string;

  readonly start: number;

  readonly end?: number;
}

export interface LaoEvent {
  readonly id: Hash;

  readonly idAlias?: Hash;

  readonly start: Timestamp;

  readonly end?: Timestamp;

  toState(): LaoEventState;
}
