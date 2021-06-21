import React, { useState } from 'react';
import {
  ScrollView,
  StyleSheet, View, ViewStyle,
} from 'react-native';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';
import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';
import { HDWallet } from 'model/objects/HDWallet';
import { WalletStore } from 'store/stores/WalletStore';
import { Hash } from 'model/objects';
import CopiableTextBlock from 'components/CopiableTextBlock';
import QRCode from 'components/QRCode';
import PROPS_TYPE from 'res/Props';
import PropTypes from 'prop-types';

const styles = StyleSheet.create({
  smallPadding: {
    padding: '1rem',
  } as ViewStyle,

  largePadding: {
    padding: '2rem',
  } as ViewStyle,
});

let cachedKeyPairs: Map<[Hash, string], string>;

/**
 * wallet UI once the wallet is synced
 * @constructor
 */
const WalletSyncedSeed = ({ navigation }: IPropTypes) => {
  /* boolean set to true if the token recover process is finished */
  const [tokensRecovered, setTokensRecovered] = useState(false);
  const [showPublicKey, setShowPublicKey] = useState(false);
  const [showQRPublicKey, setShowQRPublicKey] = useState(false);

  WalletStore.get().then((encryptedSeed) => {
    if (encryptedSeed !== undefined) {
      HDWallet.fromState(encryptedSeed)
        .then((wallet) => {
          wallet.recoverWalletPoPTokens().then((cachedTokens) => {
            cachedKeyPairs = cachedTokens;
          });
        });
    }
  });

  function showTokens() {
    const tokens: string[] = [];
    const laoId: string[] = [];
    const rollCallNames: string[] = [];

    if (cachedKeyPairs.size === 0) {
      return (
        <View>
          <TextBlock text={STRINGS.no_tokens_in_wallet} />
          <View style={styles.largePadding} />
          <WideButtonView
            title={STRINGS.back_to_wallet_home}
            onPress={() => setTokensRecovered(false)}
          />
          <WideButtonView
            title={STRINGS.logout_from_wallet}
            onPress={() => {
              HDWallet.logoutFromWallet();
              navigation.navigate(STRINGS.navigation_home_tab_wallet);
            }}
          />
        </View>
      );
    }

    let i = 0;

    cachedKeyPairs.forEach((value, key) => {
      const ids: string[] = key.toString().split(',');
      // eslint-disable-next-line prefer-destructuring
      laoId[i] = ids[0];
      // eslint-disable-next-line prefer-destructuring
      rollCallNames[i] = ids[1];
      tokens[i] = value;
      i += 1;
    });

    /* the below 4 functions are to manage user interaction with buttons */
    function hidePublicKeyButton() {
      return (
        <WideButtonView
          title={STRINGS.hide_public_keys}
          onPress={() => {
            setShowPublicKey(false);
          }}
        />
      );
    }

    function showPublicKeyButton() {
      return (
        <WideButtonView
          title={STRINGS.show_public_keys}
          onPress={() => {
            setShowPublicKey(true);
          }}
        />
      );
    }

    function hideQRButton() {
      return (
        <WideButtonView
          title={STRINGS.hide_qr_public_keys}
          onPress={() => {
            setShowQRPublicKey(false);
          }}
        />
      );
    }

    function showQRButton() {
      return (
        <WideButtonView
          title={STRINGS.show_qr_public_keys}
          onPress={() => {
            setShowQRPublicKey(true);
          }}
        />
      );
    }

    /**
     * this functions displays the LAOId, the RollCall name
     * and the public key generated from the two
     */
    function displayTokens() {
      return (
        <View>
          { laoId.map((value, key) => (
            <View style={styleContainer.centered}>
              <View style={styles.smallPadding} />
              <TextBlock bold text={STRINGS.lao_id} />
              <CopiableTextBlock id={key} text={value} visibility />
              <TextBlock bold text={STRINGS.roll_call_name} />
              <TextBlock text={rollCallNames[key]} visibility />
              <View style={styles.smallPadding} />
              <CopiableTextBlock id={key} text={tokens[key]} visibility={showPublicKey} />
              <View style={styles.smallPadding} />
              <QRCode value={tokens[key]} visibility={showQRPublicKey} />
            </View>
          ))}
        </View>
      );
    }

    return (
      <ScrollView>
        <View style={styles.largePadding} />
        <TextBlock bold text={STRINGS.your_tokens_title} />
        <View style={styles.smallPadding} />
        { displayTokens() }
        {!showPublicKey && showPublicKeyButton()}
        {showPublicKey && hidePublicKeyButton()}
        {!showQRPublicKey && showQRButton()}
        {showQRPublicKey && hideQRButton()}
        <WideButtonView
          title={STRINGS.back_to_wallet_home}
          onPress={() => setTokensRecovered(false)}
        />
        <View style={styles.largePadding} />
      </ScrollView>
    );
  }

  function recoverTokens() {
    return (
      <View>
        <TextBlock bold text={STRINGS.wallet_synced_info} />
        <View style={styles.largePadding} />
        <WideButtonView
          title={STRINGS.show_tokens_title}
          onPress={() => {
            setTokensRecovered(true);
          }}
        />
        <WideButtonView
          title={STRINGS.logout_from_wallet}
          onPress={() => {
            HDWallet.logoutFromWallet();
            navigation.navigate(STRINGS.navigation_home_tab_wallet);
          }}
        />
      </View>
    );
  }

  function getWalletDisplay() {
    return (
      <View style={styleContainer.centered}>
        {!tokensRecovered && recoverTokens()}
        {tokensRecovered && showTokens()}
      </View>
    );
  }

  return getWalletDisplay();
};

const propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};
WalletSyncedSeed.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default WalletSyncedSeed;
