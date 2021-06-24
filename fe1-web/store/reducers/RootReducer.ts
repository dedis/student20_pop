import AsyncStorage from '@react-native-community/async-storage';
import { persistCombineReducers } from 'redux-persist';

// not yet fully migrated, remove?
import openRollCallIDReducer from './OpenRollCallIDReducer';

// fully migrated:
import laoReducer from './LaoReducer';
import keyPairReducer from './KeyPairReducer';
import eventsReducer from './EventsReducer';
import messageReducer from './MessageReducer';
import walletReducer from './WalletReducer';

import { wrapWithClearStorageReducer } from './ClearStorageReducer';
import LastPoPTokenReducer from "./LastPoPTokenReducer";

const persistConfig = {
  key: 'root',
  storage: AsyncStorage,
};

const appReducer = persistCombineReducers(persistConfig, {
  ...keyPairReducer,
  ...laoReducer,
  ...eventsReducer,
  ...messageReducer,
  ...walletReducer,
  ...LastPoPTokenReducer,
  openedRollCallId: openRollCallIDReducer,
});

export const rootReducer = wrapWithClearStorageReducer(appReducer, AsyncStorage);
