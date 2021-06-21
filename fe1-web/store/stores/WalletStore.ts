import { dispatch, getStore } from '../Storage';
import { getWalletState, setWalletState, clearWalletState } from '../reducers';

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
   * This function dispatches action to clear the wallet state.
   */
  export function clear(): void {
    dispatch(clearWalletState());
  }

  /**
   * returns the wallet state: the encrypted wallet seed or throws
   * an error if the state was never initialized.
   */
  export async function get(): Promise<string | undefined> {
    const { walletState } = getWalletState(getStore().getState());
    if (!walletState) {
      console.log('No wallet in redux storage, insert 12-word mneonic to backup your wallet.');
    }

    return walletState;
  }
}
