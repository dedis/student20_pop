import { createSlice, PayloadAction } from '@reduxjs/toolkit';

/**
 * This file represents the reducer for the pop token.
 * Its job is to store the pop token state.
 * The pop token state is represented by the pop token's public and private key
 */

interface LastPoPTokenReducerState {
  popTokenPrivateKey?: string;
  popTokenPublicKey?: string;
}

const initialState: LastPoPTokenReducerState = {
  popTokenPrivateKey: undefined,
  popTokenPublicKey: undefined,
};

/* name of pop token slice in storage */
const popTokenReducerPath = 'lastGeneratedPopToken';

/* the store slice in charge of the pop token state */
const popTokenSlice = createSlice({
  name: popTokenReducerPath,
  initialState,
  reducers: {
    /* set global pop token private key state */
    setPopTokenPrivateKey: (state, action: PayloadAction<string>) => {
      if (!action.payload) {
        console.log('Pop token private key storage was set to: null');
        state.popTokenPrivateKey = undefined;
      }

      state.popTokenPrivateKey = action.payload;
      // console.log('Pop token private key storage was updated with new state');
    },

    /* set global pop token public key state */
    setPopTokenPublicKey: (state, action: PayloadAction<string>) => {
      if (!action.payload) {
        console.log('Pop token storage was set to: null');
        state.popTokenPublicKey = undefined;
      }

      state.popTokenPublicKey = action.payload;
      console.log('Pop token public key storage was updated with new state');
    },
  },
});

export const {
  setPopTokenPrivateKey,
  setPopTokenPublicKey,
} = popTokenSlice.actions;

export default {
  [popTokenReducerPath]: popTokenSlice.reducer,
};

export const getTokenState = (state: any): LastPoPTokenReducerState => state[popTokenReducerPath];
