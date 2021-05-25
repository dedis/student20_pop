import React, { useState } from 'react';
import {
  StyleSheet, View, ViewStyle,
} from 'react-native';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';
import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';
import { HDWallet } from 'model/objects/HDWallet';
import { WalletStore } from 'store/stores/WalletStore';

const styles = StyleSheet.create({
  largePadding: {
    padding: '2rem',
  } as ViewStyle,
});

let wallet: HDWallet;

/**
 * retrieves the tokens from the wallet seed
 * using HD wallet functionalities
 */
const retrieveTokensFromSeed = () => {
  wallet.getDecryptedSeed()
    .then(() => {
      // TODO : recover all PoP tokens from wallet
    });
};

/**
 * wallet UI once the wallet is synced
 * @constructor
 */
const WalletSyncedSeed = () => {
  /* boolean set to true if the token recover process is finished */
  const [tokensRecovered, setTokensRecovered] = useState(false);

  WalletStore.get().then((encryptedSeed) => HDWallet
    .fromState(encryptedSeed)
    .then((w) => {
      wallet = w;
    }));

  function showTokens() {
    // TODO
    return (
      <View>
        <TextBlock bold text="TOKENS" />
      </View>
    );
  }

  function recoverTokens() {
    return (
      <View>
        <TextBlock bold text={STRINGS.wallet_synced_info} />
        <View style={styles.largePadding} />
        <WideButtonView
          title={STRINGS.recover_tokens_title}
          onPress={() => {
            retrieveTokensFromSeed();
            setTokensRecovered(true);
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

export default WalletSyncedSeed;
