import React from 'react';
import {
  StyleSheet, View, ViewStyle,
} from 'react-native';
import styleContainer from 'styles/stylesheets/container';
import STRINGS from 'res/strings';
import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';
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

/**
 * wallet home screen
 * @constructor
 */
const WalletHome = ({ navigation }: IPropTypes) => {
  function getStartWalletDisplay() {
    return (
      <View style={styleContainer.centered}>
        <TextBlock bold text={STRINGS.welcome_to_wallet_display} />
        <View style={styles.smallPadding} />
        <TextBlock text={STRINGS.info_to_set_wallet} />
        <TextBlock text={STRINGS.caution_information_on_seed} />
        <View style={styles.largePadding} />
        <WideButtonView
          title={STRINGS.create_new_wallet_button}
          onPress={() => {
            navigation.navigate(STRINGS.navigation_show_seed_wallet);
          }}
        />
        <WideButtonView
          title={STRINGS.import_seed_button}
          onPress={() => {
            navigation.navigate(STRINGS.navigation_insert_seed_tab_wallet);
          }}
        />
      </View>
    );
  }

  return getStartWalletDisplay();
};

const propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};
WalletHome.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default WalletHome;
