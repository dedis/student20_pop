import { Store } from 'redux';
import {
  getLaoMessagesState, getLaosState, processMessages,
} from 'store';
import { ExtendedMessage } from 'model/network/method/message';
import { handleMessage } from './handlers';

export function makeMessageStoreWatcher(store: Store) {
  let previousValue: string[] | undefined;
  let currentValue: string[] | undefined;
  return () => {
    const state = store.getState();
    const laoId = getLaosState(state)?.currentId;

    if (!laoId) {
      // not connected to a LAO, return immediately
      return;
    }

    const msgState = getLaoMessagesState(laoId, state);
    console.log('Message state is: ', msgState);
    const newValue = msgState.unprocessedIds;
    [previousValue, currentValue] = [currentValue, newValue];

    if ((previousValue) && previousValue.length === currentValue.length
      && previousValue.every((value, index) => value === currentValue[index])) {
      // no change detected, return immediately
      return;
    }

    const msgs = currentValue.map((id: string) => ExtendedMessage.fromState(msgState.byId[id]));

    msgs.forEach((msg) => {
      try {
        const handled = handleMessage(msg);
        if (handled) {
          store.dispatch(processMessages(laoId, msg.message_id));
        }
      } catch (err) {
        console.error('An exception was raised when processing the message:', msg, err);
      }
    });
  };
}
