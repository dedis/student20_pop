import { createSlice, PayloadAction } from '@reduxjs/toolkit';

/**
 * This file represents the reducer for the wallet.
 * Its job is to store the wallet state.
 * The wallet state is represented by the wallet's encrypted seed.
 */

interface WalletReducerState {
  walletState?: string;
}

const initialState: WalletReducerState = {
  walletState: undefined,
};

/* name of wallet slice in storage */
const walletReducerPath = 'wallet';

/* the store slice in charge of the wallet state */
const walletSlice = createSlice({
  name: walletReducerPath,
  initialState,
  reducers: {
    /* set global wallet state */
    setWalletState: (state, action: PayloadAction<string>) => {
      if (!action.payload) {
        console.log('Wallet storage was set to: null');
        state.walletState = undefined;
      }

      state.walletState = action.payload;
      console.log('Wallet storage was updated with new state');
    },

    clearWalletState: (state) => {
      state.walletState = undefined;
      console.log('Wallet storage was cleared');
    },
  },
});

export const {
  setWalletState,
  clearWalletState,
} = walletSlice.actions;

export default {
  [walletReducerPath]: walletSlice.reducer,
};

export const getWalletState = (state: any): WalletReducerState => state[walletReducerPath];
