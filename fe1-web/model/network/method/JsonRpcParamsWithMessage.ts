import { Message } from './message';
import { ProtocolError } from '../ProtocolError';
import { JsonRpcParams } from './JsonRpcParams';

export class JsonRpcParamsWithMessage extends JsonRpcParams {
  public readonly message: Message;

  constructor(params: Partial<JsonRpcParamsWithMessage>) {
    super(params);

    if (params.message === undefined || params.message === null) {
      throw new ProtocolError("Undefined 'message' parameter in JSON-RPC");
    }

    if (params.channel === undefined) {
      throw new ProtocolError("Undefined 'channel' parameter in JSON-RPC");
    }

    this.message = params.message;
  }

  public static fromJson(obj: any): JsonRpcParamsWithMessage {
    // Schema validation already passed at top level
    // Here we assign the channel to the message for all incoming messages, then we can use
    // it in the handler for each message (especially important for election result message
    // as it has no election id in the message, we need to get it from the channel)
    // eslint-disable-next-line no-param-reassign
    obj.message.channel = obj.channel;
    return new JsonRpcParamsWithMessage({
      channel: obj.channel,
      message: Message.fromJson(obj.message),
    });
  }
}
