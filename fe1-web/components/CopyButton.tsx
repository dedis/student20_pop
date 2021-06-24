import React from 'react';
import {
  StyleSheet, Image, Pressable, ImageStyle,
} from 'react-native';
import PropTypes from 'prop-types';

import { getNavigator } from 'utils/Navigator';

const copyIcon = require('res/img/copy.svg');

/**
 * Copy to clipboard button
 */

const styles = StyleSheet.create({
  icon: {
    width: 26,
    height: 26,
    cursor: 'pointer',
  } as ImageStyle,
});

function CopyButton({ data }: IPropTypes) {
  return (
    <Pressable onPress={() => getNavigator().clipboard.writeText(data)}>
      <Image style={styles.icon} source={copyIcon} />
    </Pressable>
  );
}

const propTypes = {
  data: PropTypes.string.isRequired,
};
CopyButton.prototype = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default CopyButton;
