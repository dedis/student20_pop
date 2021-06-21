import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';

import STRINGS from 'res/strings';

import WalletHome from 'parts/wallet/WalletHome';
import WalletSetSeed from 'parts/wallet/WalletSetSeed';
import WalletShowSeed from 'parts/wallet/WalletShowSeed';
import WalletSyncedSeed from 'parts/wallet/WalletSyncedSeed';
import WalletError from 'parts/wallet/WalletError';

/**
 * Define the Wallet stack navigation
 * Allows to navigate between the wallet screens
 */

const Stack = createStackNavigator();

export default function WalletNavigation() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <Stack.Screen
        name={STRINGS.navigation_home_tab_wallet}
        component={WalletHome}
      />
      <Stack.Screen
        name={STRINGS.navigation_show_seed_wallet}
        component={WalletShowSeed}
      />
      <Stack.Screen
        name={STRINGS.navigation_insert_seed_tab_wallet}
        component={WalletSetSeed}
      />
      <Stack.Screen
        name={STRINGS.navigation_synced_wallet}
        component={WalletSyncedSeed}
      />
      <Stack.Screen
        name={STRINGS.navigation_wallet_error}
        component={WalletError}
      />
    </Stack.Navigator>
  );
}
