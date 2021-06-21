import React, { useState } from 'react';
import {
  Platform, StyleSheet,
} from 'react-native';
import { useSelector } from 'react-redux';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';

import { getStore, getKeyPairState, makeCurrentLao } from 'store';
import { PublicKey } from 'model/objects';

import STRINGS from 'res/strings';
import Home from 'parts/Home';
import Identity from 'parts/lao/Identity';
import Attendee from 'parts/lao/attendee/Attendee';
import WalletSyncedSeed from 'parts/wallet/WalletSyncedSeed';
import { WalletStore } from 'store/stores/WalletStore';
import OrganizerNavigation from './organizer/OrganizerNavigation';
import WitnessNavigation from './witness/WitnessNavigation';
import WalletNavigation from './wallet/WalletNavigation';

const OrganizationTopTabNavigator = createMaterialTopTabNavigator();

/**
 * Navigation when connected to a lao
 *
 * Displays the following components:
 *  - Home
 *  - Lao tab (corresponding to user role)
 *  - Identity
 *  - name of the connected lao (fake link)
 */
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

function getLaoTabName(isOrganizer: boolean, isWitness: boolean): string {
  if (isOrganizer) {
    return STRINGS.organization_navigation_tab_organizer;
  }

  if (isWitness) {
    return STRINGS.organization_navigation_tab_witness;
  }

  return STRINGS.organization_navigation_tab_attendee;
}

function buildTabComponent(isOrganizer: boolean, isWitness: boolean) {
  const tabName: string = getLaoTabName(isOrganizer, isWitness);
  let component;

  if (isOrganizer) {
    component = OrganizerNavigation;
  } else if (isWitness) {
    component = WitnessNavigation;
  } else {
    component = Attendee;
  }

  return (
    <OrganizationTopTabNavigator.Screen
      name={tabName}
      component={component}
    />
  );
}

function displaySyncedWallet() {
  return (
    <OrganizationTopTabNavigator.Screen
      name={STRINGS.navigation_synced_wallet}
      component={WalletSyncedSeed}
    />
  );
}

function displayHomeWallet() {
  return (
    <OrganizationTopTabNavigator.Screen
      name={STRINGS.navigation_home_tab_wallet}
      component={WalletNavigation}
    />
  );
}

// Cannot omit the "component" attribute in Screen
// Moreover, cannot use a lambda in "component"
const DummyComponent = () => null;

function LaoNavigation() {
  const laoSelect = makeCurrentLao();
  const lao = useSelector(laoSelect);

  const publicKeyRaw = getKeyPairState(getStore().getState()).keyPair?.publicKey;
  const publicKey = publicKeyRaw ? new PublicKey(publicKeyRaw) : undefined;

  const [walletIsInitialized, setWalletIsInitialized] = useState(false);
  WalletStore.get().then((encryptedSeed) => setWalletIsInitialized(encryptedSeed !== undefined));

  const isOrganizer = !!(lao && publicKey && (publicKey.equals(lao.organizer)));
  const isWitness = !!(lao && publicKey && lao.witnesses.some((w) => publicKey.equals(w)));

  const tabName: string = getLaoTabName(isOrganizer, isWitness);
  const laoName: string = (lao) ? lao.name : STRINGS.unused;

  return (
    <OrganizationTopTabNavigator.Navigator
      style={styles.navigator}
      initialRouteName={tabName}
    >

      <OrganizationTopTabNavigator.Screen
        name={STRINGS.navigation_tab_home}
        component={Home}
      />

      { buildTabComponent(isOrganizer, isWitness) }

      <OrganizationTopTabNavigator.Screen
        name={STRINGS.organization_navigation_tab_identity}
        component={Identity}
      />

      { !walletIsInitialized && displayHomeWallet() }
      { walletIsInitialized && displaySyncedWallet() }

      <OrganizationTopTabNavigator.Screen
        name={laoName}
        component={DummyComponent}
        listeners={{
          tabPress: (e) => {
            // => do nothing
            e.preventDefault();
          },
        }}
      />
    </OrganizationTopTabNavigator.Navigator>
  );
}

export default LaoNavigation;
