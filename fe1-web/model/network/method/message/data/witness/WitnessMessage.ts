import { Hash, Signature } from 'model/objects';
import { ProtocolError } from 'model/network/ProtocolError';
import { ActionType, MessageData, ObjectType } from '../MessageData';

export class WitnessMessage implements MessageData {
  public readonly object: ObjectType = ObjectType.MESSAGE;

  public readonly action: ActionType = ActionType.WITNESS;

  public readonly message_id: Hash;

  public readonly signature: Signature;

  constructor(msg: Partial<WitnessMessage>) {
    if (!msg.message_id) throw new ProtocolError('Undefined \'message_id\' parameter encountered during \'WitnessMessage\'');
    this.message_id = msg.message_id;

    if (!msg.signature) throw new ProtocolError('Undefined \'signature\' parameter encountered during \'WitnessMessage\'');
    // FIXME verify signature without the public key
    // available? 0.o + uncomment 3 tests in "FromJson" test suite
    this.signature = msg.signature;
  }

  public static fromJson(obj: any): WitnessMessage {
    // FIXME add JsonSchema validation to all "fromJson"
    const correctness = true;

    return correctness
      ? new WitnessMessage({
        ...obj,
        message_id: new Hash(obj.message_id),
        signature: new Signature(obj.signature),
      })
      : (() => { throw new ProtocolError('add JsonSchema error message'); })();
  }
}