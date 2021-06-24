import {
  Hash, PublicKey, Base64UrlData, WitnessSignature, Signature, Timestamp, Channel,
} from 'model/objects';
import { Message, MessageState } from 'model/network/method/message/Message';

export interface ExtendedMessageState extends MessageState {
  receivedAt: number;
  processedAt?: number;
}

export function markExtMessageAsProcessed(msg: ExtendedMessageState, when?: Timestamp)
  : ExtendedMessageState {
  return {
    ...msg,
    processedAt: when?.valueOf() || Timestamp.EpochNow().valueOf(),
  };
}

export class ExtendedMessage extends Message {
  public readonly receivedAt: Timestamp;

  public processedAt?: Timestamp;

  constructor(msg: Partial<ExtendedMessage>) {
    super(msg);
    this.receivedAt = msg.receivedAt || Timestamp.EpochNow();
    this.processedAt = msg.processedAt;
  }

  public markAsProcessed(when?: Timestamp) {
    this.processedAt = when || Timestamp.EpochNow();
  }

  public static fromMessage(msg: Message, receivedAt?: Timestamp): ExtendedMessage {
    return new ExtendedMessage({
      ...msg,
      receivedAt: receivedAt,
    });
  }

  public static fromState(state: ExtendedMessageState): ExtendedMessage {
    return new ExtendedMessage({
      // message fields:
      data: new Base64UrlData(state.data),
      sender: new PublicKey(state.sender),
      signature: new Signature(state.signature),
      message_id: new Hash(state.message_id),
      channel: state.channel,
      witness_signatures: state.witness_signatures.map(
        (ws: any) => new WitnessSignature({
          witness: new PublicKey(ws.witness),
          signature: new Signature(ws.signature),
        }),
      ),
      // extended fields:
      receivedAt: state.receivedAt ? new Timestamp(state.receivedAt) : undefined,
      processedAt: state.processedAt ? new Timestamp(state.processedAt) : undefined,
    });
  }

  public toState(): ExtendedMessageState {
    return JSON.parse(JSON.stringify(this));
  }
}
