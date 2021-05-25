import React from 'react';
import {
  StyleSheet, Image, Pressable, ImageStyle,
} from 'react-native';
import PropTypes from 'prop-types';

const trashcan = require('res/img/delete.svg');

/**
 * Delete button that displays a little trashcan and executes an onPress action given in props
 */

const styles = StyleSheet.create({
  icon: {
    width: 26,
    height: 26,
    cursor: 'pointer',
  } as ImageStyle,
});

function DeleteButton({ action }: IPropTypes) {
  return (
    <Pressable onPress={action}>
      <Image style={styles.icon} source={trashcan} />
    </Pressable>
  );
}

const propTypes = {
  action: PropTypes.func.isRequired,
};
DeleteButton.prototype = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default DeleteButton;
