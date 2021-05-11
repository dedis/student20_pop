import { createSelector, createSlice, PayloadAction } from '@reduxjs/toolkit';
import {
  ExtendedMessage, ExtendedMessageState, markExtMessageAsProcessed,
} from 'model/network/method/message';
import { Hash, WitnessSignatureState } from 'model/objects';
import { getLaosState } from './LaoReducer';

/**
 * Reducer & associated function implementation to store all known Messages
 */

interface MessageReducerState {
  byId: Record<string, ExtendedMessageState>,
  allIds: string[]
  unprocessedIds: string[],
}

interface MessageLaoReducerState {
  byLaoId: Record<string, MessageReducerState>,
}

const initialState: MessageLaoReducerState = {
  byLaoId: {},
};

const messageReducerPath = 'messages';
const messagesSlice = createSlice({
  name: messageReducerPath,
  initialState,
  reducers: {

    // Add a Message to the list of known Messages
    addMessages: {
      prepare(laoId: Hash | string, messages: ExtendedMessageState | ExtendedMessageState[]): any {
        const msgs = Array.isArray(messages) ? messages : [messages];
        return { payload: { laoId: laoId.valueOf(), messages: msgs } };
      },
      reducer(state, action: PayloadAction<{
        laoId: string;
        messages: ExtendedMessageState[];
      }>) {
        const { laoId, messages } = action.payload;

        // Lao not initialized, create it in the message state tree
        if (!(laoId in state.byLaoId)) {
          state.byLaoId[laoId] = {
            byId: {},
            allIds: [],
            unprocessedIds: [],
          };
        }

        messages.forEach((msg: ExtendedMessageState) => {
          if (msg.message_id in state.byLaoId[laoId].byId) {
            // don't add again a message we have already received
            // TODO: we might want to merge the witness signatures here
            return;
          }

          state.byLaoId[laoId].byId[msg.message_id] = msg;
          state.byLaoId[laoId].allIds.push(msg.message_id);
          state.byLaoId[laoId].unprocessedIds.push(msg.message_id);
        });
      },
    },

    // Remove a Message to the list of unprocessed Messages
    processMessages: {
      prepare(laoId: Hash | string, messageIds: String | String[]): any {
        const msgIds = Array.isArray(messageIds) ? messageIds : [messageIds];
        return {
          payload: {
            laoId: laoId.valueOf(),
            messageIds: msgIds.map((m: String) => m.valueOf()),
          },
        };
      },
      reducer(state, action: PayloadAction<{
        laoId: string;
        messageIds: string[];
      }>) {
        const { laoId, messageIds } = action.payload;
        if (!(laoId in state.byLaoId)) {
          return;
        }
        messageIds.forEach((messageId: string) => {
          state.byLaoId[laoId].byId[messageId] = markExtMessageAsProcessed(
            state.byLaoId[laoId].byId[messageId],
          );
          state.byLaoId[laoId].unprocessedIds = state.byLaoId[laoId].unprocessedIds
            .filter((e) => e !== messageId);
        });
      },
    },

    // Empty the list of known Messages ("reset")
    clearAllMessages: (state) => {
      state.byLaoId = {};
    },

    // Add witness signatures to a message
    addMessageWitnessSignature: {
      prepare(laoId: Hash | string, messageId: Hash | string, witSig: WitnessSignatureState): any {
        return {
          payload: {
            laoId: laoId.valueOf(),
            messageId: messageId.valueOf(),
            witnessSignature: witSig,
          },
        };
      },
      reducer(state, action: PayloadAction<{
        laoId: string;
        messageId: string;
        witnessSignature: WitnessSignatureState;
      }>) {
        const { laoId, messageId, witnessSignature } = action.payload;

        if (!(laoId in state.byLaoId)) {
          return;
        }

        const msg = state.byLaoId[laoId].byId[messageId];
        if (!msg.witness_signatures.every((ws) => ws.witness !== witnessSignature.witness)) {
          // avoid adding multiple times the same witness signature
          return;
        }

        msg.witness_signatures.push(witnessSignature);
      },
    },
  },
});

export const {
  addMessages, processMessages, addMessageWitnessSignature, clearAllMessages,
} = messagesSlice.actions;

export function getMessagesState(state: any): MessageLaoReducerState {
  return state[messageReducerPath];
}

export function getMessage(state: MessageReducerState, messageId: Hash | string)
  : ExtendedMessage | undefined {
  const id = messageId.valueOf();
  return id in state.byId
    ? ExtendedMessage.fromState(state.byId[id])
    : undefined;
}

export function makeLaoMessagesState() {
  return createSelector(
    // First input: all LAOs map
    (state) => getMessagesState(state).byLaoId,
    // Second input: current LAO id
    (state) => getLaosState(state).currentId,
    // Selector: returns a LaoState -- should it return a Lao object?
    (msgMap: Record<string, MessageReducerState>, laoId: string | undefined)
    : MessageReducerState | undefined => {
      if (laoId === undefined || !(laoId in msgMap)) {
        return undefined;
      }

      return msgMap[laoId];
    },
  );
}

export function getLaoMessagesState(laoId: Hash | string, state: any): MessageReducerState {
  const id = laoId.valueOf();
  const msgState = getMessagesState(state);
  if (msgState && id in msgState.byLaoId) {
    return msgState.byLaoId[id];
  }
  return {
    byId: {},
    allIds: [],
    unprocessedIds: [],
  };
}

export default {
  [messageReducerPath]: messagesSlice.reducer,
};
