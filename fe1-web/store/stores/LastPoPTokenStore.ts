import { dispatch, getStore } from '../Storage';
import { getLastPopTokenState, setPopTokenPrivateKey, setPopTokenPublicKey } from '../reducers';

/**
 * This file represents the storage slice for the pop token state.
 */
export namespace LastPopTokenStore {

  export function storePrivateKey(privateKey: string): void {
    dispatch(setPopTokenPrivateKey(privateKey));
  }

  export function storePublicKey(publicKey: string): void {
    dispatch(setPopTokenPublicKey(publicKey));
  }

  /**
   * returns the pop token' private key state
   */
  export function getPrivateKey(): string | undefined {
    const { popTokenPrivateKey } = getLastPopTokenState(getStore().getState());
    if (!popTokenPrivateKey) {
      console.log('No pop token was cached yet');
    }

    return popTokenPrivateKey;
  }

  /**
   * returns the pop token' public key state
   */
  export function getPublicKey(): string | undefined {
    const { popTokenPublicKey } = getLastPopTokenState(getStore().getState());
    if (!popTokenPublicKey) {
      console.log('No pop token was cached yet');
    }

    return popTokenPublicKey;
  }
}
