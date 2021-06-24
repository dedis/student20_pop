import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';

import STRINGS from 'res/strings';

import ConnectEnableCamera from 'parts/connect/ConnectEnableCamera';
import ConnectOpenScan from 'parts/connect/ConnectOpenScan';
import ConnectConfirm from 'parts/connect/ConnectConfirm';

/**
 * Define the connect panel stack navigation
 *
 * Contains four components:
 *  - The ConnectEnableCamera
 *  - The ConnectOpenScan
 *  - The ConnectConnecting
 *  - The ConnectConfirm
*/

const Stack = createStackNavigator();

export default function ConnectNavigation() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <Stack.Screen
        name={STRINGS.connect_unapproved_title}
        component={ConnectEnableCamera}
      />
      <Stack.Screen
        name={STRINGS.connect_scanning_title}
        component={ConnectOpenScan}
      />
      <Stack.Screen
        name={STRINGS.connect_confirm_title}
        component={ConnectConfirm}
      />
    </Stack.Navigator>
  );
}
