import {
  QuestionResult,
} from 'model/objects';
import { ProtocolError } from 'model/network/ProtocolError';
import { validateDataObject } from 'model/network/validation';
import { ActionType, MessageData, ObjectType } from '../MessageData';

export class ElectionResult implements MessageData {
  public readonly object: ObjectType = ObjectType.ELECTION;

  public readonly action: ActionType = ActionType.RESULT;

  public readonly questions: QuestionResult[];

  constructor(msg: Partial<ElectionResult>) {
    if (!msg.questions) {
      throw new ProtocolError('Undefined \'questions\' parameter encountered during \'ElectionResult\'');
    }
    this.questions = msg.questions;
  }

  public static fromJson(obj: any): ElectionResult {
    const { errors } = validateDataObject(ObjectType.ELECTION, ActionType.RESULT, obj);

    if (errors !== null) {
      throw new ProtocolError(`Invalid election Result\n\n${errors}`);
    }

    return new ElectionResult({
      ...obj,
    });
  }
}
