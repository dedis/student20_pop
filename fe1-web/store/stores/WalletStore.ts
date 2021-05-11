import { dispatch, getStore } from '../Storage';
import { getWalletState, setWalletState } from '../reducers';

/**
 * This file represents the storage slice for the wallet state.
 * The wallet state is represented by the encrypted wallet seed.
 */
export namespace WalletStore {
  /**
   * This function dispatches action to store wallet state.
   * @param walletSeed the RSA-encrypted seed for this wallet
   */
  export function store(walletSeed: string): void {
    dispatch(setWalletState(walletSeed));
  }

  /**
   * returns the wallet state: the encrypted wallet seed or throws
   * an error if the state was never initialized.
   */
  export async function get(): Promise<string> {
    const { walletState } = getWalletState(getStore().getState());

    if (!walletState) {
      throw new Error('the wallet state is not initialized: null seed in redux store');
    }

    return walletState;
  }
}
