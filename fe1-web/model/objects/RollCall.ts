import { LaoEvent, LaoEventState, LaoEventType } from './LaoEvent';
import { Hash } from './Hash';
import { Timestamp } from './Timestamp';
import { PublicKey } from './PublicKey';

export enum RollCallStatus {
  CREATED,
  OPENED,
  CLOSED,
  REOPENED,
}

export interface RollCallState extends LaoEventState {
  name: string;
  location: string;
  description?: string;
  creation: number;
  proposed_start: number;
  proposed_end: number;
  opened_at?: number;
  closed_at?: number;
  status: number;
  attendees?: string[];
}

export class RollCall implements LaoEvent {
  public readonly id: Hash;

  public readonly idAlias?: Hash;

  public readonly name: string;

  public readonly location: string;

  public readonly description?: string;

  public readonly creation: Timestamp;

  public readonly proposed_start: Timestamp;

  public readonly proposed_end: Timestamp;

  public readonly opened_at?: Timestamp;

  public readonly closed_at?: Timestamp;

  public readonly status: RollCallStatus;

  public readonly attendees?: PublicKey[];

  public get start() {
    return this.opened_at ?? this.proposed_start;
  }

  public get end() {
    return this.closed_at ?? this.proposed_end;
  }

  /* Not yet implemented:
   * This object should probably also keep a list of the time periods
   * during which the roll call was open (or re-opened).
   */

  constructor(obj: Partial<RollCall>) {
    if (obj === undefined || obj === null) {
      throw new Error('Error encountered while creating a RollCall object: '
      + 'undefined/null parameters');
    }

    if (obj.id === undefined) {
      throw new Error("Undefined 'id' when creating 'RollCall'");
    }
    if (obj.name === undefined) {
      throw new Error("Undefined 'name' when creating 'RollCall'");
    }
    if (obj.location === undefined) {
      throw new Error("Undefined 'location' when creating 'RollCall'");
    }
    if (obj.creation === undefined) {
      throw new Error("Undefined 'creation' when creating 'RollCall'");
    }
    if (obj.proposed_start === undefined) {
      throw new Error("Undefined 'proposed_start' when creating 'RollCall'");
    }
    if (obj.proposed_end === undefined) {
      throw new Error("Undefined 'proposed_end' when creating 'RollCall'");
    }
    if (obj.status === undefined) {
      throw new Error("Undefined 'status' when creating 'RollCall'");
    }

    this.id = obj.id;
    this.idAlias = obj.idAlias;
    this.name = obj.name;
    this.location = obj.location;
    this.description = obj.description;
    this.creation = obj.creation;
    this.proposed_start = obj.proposed_start;
    this.proposed_end = obj.proposed_end;
    this.status = obj.status;
    this.attendees = obj.attendees;
  }

  public static fromState(rc: RollCallState): RollCall {
    return new RollCall({
      id: new Hash(rc.id),
      idAlias: (rc.idAlias) ? new Hash(rc.idAlias) : undefined,
      name: rc.name,
      location: rc.location,
      description: rc.description,
      creation: new Timestamp(rc.creation),
      proposed_start: new Timestamp(rc.proposed_start),
      proposed_end: new Timestamp(rc.proposed_end),
      opened_at: new Timestamp(rc.opened_at),
      closed_at: new Timestamp(rc.closed_at),
      status: rc.status,
      attendees: rc.attendees?.map((a) => new PublicKey(a)),
    });
  }

  public toState(): RollCallState {
    const obj: any = JSON.parse(JSON.stringify(this));
    return {
      ...obj,
      start: this.start.valueOf(),
      end: this.end.valueOf(),
      eventType: LaoEventType.ROLL_CALL,
    };
  }
}
