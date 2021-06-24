/** Enumeration of all possible "object" field values in MessageData */
export enum ObjectType {
  // uninitialized placeholder
  INVALID = '__INVALID_OBJECT__',

  LAO = 'lao',
  MESSAGE = 'message',
  MEETING = 'meeting',
  ROLL_CALL = 'roll_call',
  ELECTION = 'election',
}

/** Enumeration of all possible "action" field values in MessageData */
export enum ActionType {
  // uninitialized placeholder
  INVALID = '__INVALID_ACTION__',

  CAST_VOTE = 'cast_vote',
  CREATE = 'create',
  END = 'end',
  SETUP = 'setup',
  UPDATE_PROPERTIES = 'update_properties',
  STATE = 'state',
  WITNESS = 'witness',
  OPEN = 'open',
  REOPEN = 'reopen',
  RESULT = 'result',
  CLOSE = 'close',
}

export interface MessageData {
  readonly object: ObjectType;
  readonly action: ActionType;
}
