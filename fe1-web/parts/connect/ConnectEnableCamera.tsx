import React from 'react';
import { View } from 'react-native';
import STRINGS from 'res/strings';
import PROPS_TYPE from 'res/Props';
import WideButtonView from 'components/WideButtonView';
import TextBlock from 'components/TextBlock';
import PropTypes from 'prop-types';
import styleContainer from 'styles/stylesheets/container';

/**
 * ConnectEnableCamera pane asks for the user to enable camera access in order to scan QR codes
 *
 * TODO ask the user for the camera permission when click on the permission button
 */
const ConnectEnableCamera = ({ navigation }: IPropTypes) => (
  <View style={styleContainer.flex}>
    <TextBlock text={STRINGS.connect_description} />
    <WideButtonView
      title={STRINGS.connect_button_camera}
      onPress={() => { navigation.navigate(STRINGS.connect_scanning_title); }}
    />
    <WideButtonView
      title={STRINGS.connect_connecting_validate}
      onPress={() => {
        navigation.navigate(STRINGS.connect_confirm_title);
      }}
    />
  </View>
);

const propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};
ConnectEnableCamera.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default ConnectEnableCamera;
