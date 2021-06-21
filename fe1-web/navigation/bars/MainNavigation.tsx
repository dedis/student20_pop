import React from 'react';
import { Platform, StyleSheet } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';

import STRINGS from 'res/strings';

import Home from 'parts/Home';
import Launch from 'parts/Launch';
import ConnectNavigation from 'navigation/bars/ConnectNavigation';
import WalletNavigation from './wallet/WalletNavigation';

/**
 * The main tab navigation component
 *
 * create a tab navigator between the Home, Connect components and Launch component
 */
const HomeTopTabNavigator = createMaterialTopTabNavigator();

const styles = StyleSheet.create({
  navigator: {
    ...Platform.select({
      web: {
        width: '100vw',
      },
      default: {},
    }),
  },
});

export default function MainNavigation() {
  return (
    <HomeTopTabNavigator.Navigator
      style={styles.navigator}
      initialRouteName={STRINGS.navigation_tab_home}
    >
      <HomeTopTabNavigator.Screen name={STRINGS.navigation_tab_home} component={Home} />
      <HomeTopTabNavigator.Screen
        name={STRINGS.navigation_tab_connect}
        component={ConnectNavigation}
      />
      <HomeTopTabNavigator.Screen name={STRINGS.navigation_tab_launch} component={Launch} />
      <HomeTopTabNavigator.Screen
        name={STRINGS.navigation_home_tab_wallet}
        component={WalletNavigation}
      />
    </HomeTopTabNavigator.Navigator>
  );
}
