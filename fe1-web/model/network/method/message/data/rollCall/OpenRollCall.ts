import { Hash, Timestamp } from 'model/objects';
import { ProtocolError } from 'model/network/ProtocolError';
import { validateDataObject } from 'model/network/validation';
import { ActionType, MessageData, ObjectType } from '../MessageData';
import { checkTimestampStaleness } from '../Checker';

export class OpenRollCall implements MessageData {
  public readonly object: ObjectType = ObjectType.ROLL_CALL;

  public readonly action: ActionType = ActionType.OPEN;

  public readonly update_id: Hash;

  public readonly opens: Hash;

  public readonly opened_at: Timestamp;

  constructor(msg: Partial<OpenRollCall>) {
    if (!msg.opened_at) {
      throw new ProtocolError("Undefined 'opened_at' parameter encountered during 'OpenRollCall'");
    }
    checkTimestampStaleness(msg.opened_at);
    this.opened_at = msg.opened_at;

    if (!msg.update_id) {
      throw new ProtocolError("Undefined 'update_Id' parameter encountered during 'OpenRollCall'");
    }
    this.update_id = msg.update_id;

    if (!msg.opens) {
      throw new ProtocolError("Undefined 'opens' parameter encountered during 'OpenRollCall'");
    }
    this.opens = msg.opens;
  }

  public static fromJson(obj: any): OpenRollCall {
    const { errors } = validateDataObject(ObjectType.ROLL_CALL, ActionType.OPEN, obj);

    if (errors !== null) {
      throw new ProtocolError(`Invalid open roll call\n\n${errors}`);
    }

    return new OpenRollCall({
      ...obj,
      opened_at: new Timestamp(obj.opened_at),
      update_id: new Hash(obj.update_id),
      opens: new Hash(obj.opens),
    });
  }
}
