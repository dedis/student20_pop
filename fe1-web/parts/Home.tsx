import React from 'react';
import { StyleSheet, View, FlatList } from 'react-native';
import { useSelector } from 'react-redux';

import { KeyPairStore, makeLaosList } from 'store';
import {
  Base64UrlData,
  KeyPair, Lao, PrivateKey, PublicKey,
} from 'model/objects';

import { Spacing } from 'styles';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';

import LAOItem from 'components/LAOItem';
import TextBlock from 'components/TextBlock';

import { sign } from 'tweetnacl';
import { encodeBase64 } from 'tweetnacl-util';
import WideButtonView from 'components/WideButtonView';

/**
 * Manage the Home screen component: if the user is not connected to any LAO, a welcome message
 * is displayed, otherwise a list available previously connected LAOs is displayed instead
 *
 * TODO use the list that the user have already connect to, and ask data to
 *  some organizer server if needed
*/
const styles = StyleSheet.create({
  flatList: {
    marginTop: Spacing.s,
  },
});

// FIXME: define interface + types, requires availableLaosReducer to be migrated first
function getConnectedLaosDisplay(laos: Lao[]) {
  return (
    <View style={styleContainer.centered}>
      <FlatList
        data={laos}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => <LAOItem LAO={item} />}
        style={styles.flatList}
      />
    </View>
  );
}

const storeKeyPair = () => {
  const pair = sign.keyPair();

  const keyPair: KeyPair = new KeyPair({
    publicKey: new PublicKey(Base64UrlData.fromBase64(encodeBase64(pair.publicKey)).valueOf()),
    privateKey: new PrivateKey(Base64UrlData.fromBase64(encodeBase64(pair.secretKey)).valueOf()),
  });

  KeyPairStore.store(keyPair);
  console.log('New keypair stored');
};

const setOrganizerKeyPair = () => {
  const keyPair: KeyPair = new KeyPair({
    publicKey: new PublicKey('Wto5aKBnfU0fIX2x1c_KB_-fVaW5COfOu-jLWkOIaWE='),
    privateKey: new PrivateKey('OCvBuCljq8vXcfxi0iZQmBELCSE54EHGoqafHVyVsr1a2jlooGd9TR8hfbHVz8oH_59VpbkI58676MtaQ4hpYQ=='),
  });
  KeyPairStore.store(keyPair);
};

function getWelcomeMessageDisplay() {
  return (
    <View style={styleContainer.centered}>
      <TextBlock bold text={STRINGS.home_welcome} />
      <TextBlock bold text={STRINGS.home_connect_lao} />
      <TextBlock bold text={STRINGS.home_launch_lao} />
      <WideButtonView onPress={storeKeyPair} title="Set Random Keypair" />
      <WideButtonView onPress={setOrganizerKeyPair} title="Set Organizer Keypair" />
    </View>
  );
}

const Home = () => {
  const laosList = makeLaosList();
  const laos: Lao[] = useSelector(laosList);

  return (laos && !laos.length)
    ? getConnectedLaosDisplay(laos)
    : getWelcomeMessageDisplay();
};

export default Home;
