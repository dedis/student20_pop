import React, { useState } from 'react';
import {
  StyleSheet, TextInput, TextStyle, View, ViewStyle,
} from 'react-native';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';
import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';
import { Spacing, Typography } from 'styles';
import { HDWallet } from 'model/objects/HDWallet';
import PROPS_TYPE from 'res/Props';
import PropTypes from 'prop-types';

const styles = StyleSheet.create({
  textInput: {
    ...Typography.base,
    borderBottomWidth: 2,
    marginVertical: Spacing.s,
    marginHorizontal: Spacing.xl,
  } as TextStyle,
  smallPadding: {
    padding: '1rem',
  } as ViewStyle,
  largePadding: {
    padding: '2rem',
  } as ViewStyle,
});

/**
 * wallet screen to set an already existing mnemonic
 * @constructor
 */
const WalletSetSeed = ({ navigation }: IPropTypes) => {
  /* used to set the mnemonic seed inserted by the user */
  const [seed, setSeed] = useState('');

  const initWallet = () => {
    const wallet: HDWallet = new HDWallet();
    wallet.initialize(seed)
      .then((seedIsValid) => ((seedIsValid)
        ? navigation.navigate(STRINGS.navigation_synced_wallet)
        : navigation.navigate(STRINGS.navigation_wallet_error)));
  };

  function getInsertSeedWalletDisplay() {
    return (
      <View style={styleContainer.centered}>
        <TextBlock text={STRINGS.type_seed_info} />
        <TextInput
          style={styles.textInput}
          placeholder={STRINGS.type_seed_example}
          onChangeText={(input: string) => setSeed(input)}
        />
        <View style={styles.smallPadding} />
        <WideButtonView
          title={STRINGS.setup_wallet}
          onPress={() => initWallet()}
        />
        <WideButtonView
          title={STRINGS.back_to_wallet_home}
          onPress={() => navigation.navigate(STRINGS.navigation_home_tab_wallet)}
        />
      </View>
    );
  }

  return getInsertSeedWalletDisplay();
};

const propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};
WalletSetSeed.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default WalletSetSeed;
