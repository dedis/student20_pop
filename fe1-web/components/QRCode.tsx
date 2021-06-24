import React from 'react';
import PropTypes from 'prop-types';
import QRCodeDisplay from 'qrcode.react';
import { View } from 'react-native';
import styleContainer from 'styles/stylesheets/container';

/**
 * Creates and displays a QR code with the public key of the current user
 */

const QRCode = (props: IPropTypes) => {
  const { value } = props;
  const { visibility } = props;
  const { size } = props;

  // Displays a QR code with the Public key of the current user
  return (visibility)
    ? (
      <View style={[styleContainer.anchoredCenter, { padding: 10, justifyContent: 'flex-start' }]}>
        <QRCodeDisplay value={value} size={size} />
      </View>
    )
    : null;
};

const propTypes = {
  visibility: PropTypes.bool.isRequired,
  size: PropTypes.number,
  value: PropTypes.string,
};
QRCode.propTypes = propTypes;

QRCode.defaultProps = {
  size: 160, // Size of the QR code in pixels
  value: '',
};

type IPropTypes = {
  visibility: boolean,
  size: number,
  value: string,
};

export default QRCode;
