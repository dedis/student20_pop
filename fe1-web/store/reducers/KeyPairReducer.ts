import { AnyAction } from 'redux';
import { KeyPair } from 'model/objects';
import { ActionKeyPairReducer } from '../Actions';

const initialState: KeyPair | null = null;

/**
 * Reducer to store a set of public/private key
 *
 * Action types:
 *  - SET_KEYPAIR: set a key pair in local storage
 *
 *  Note: action is a JsObject of the form { type: <ACTION_TYPE> } where
 *  <ACTION_TYPE> is a string
 *
 * @param state JsObject containing the keys to store
 * @param action action to be executed by the reducer
 * @returns new key pair if action is valid, old key pair otherwise
 */
export function keyPairReducer(
  state: KeyPair | null = initialState, action: AnyAction,
): KeyPair | null {
  try {
    if (action.type === ActionKeyPairReducer.SET_KEYPAIR) {
      if (action.value === undefined || action.value === null) {
        return null;
      }
      return new KeyPair(action.value);
    }
  } catch (e) {
    console.exception(e);
  }

  console.log(`KeyPair storage stayed unchanged after action : '${action.type}'`);
  return state;
}
