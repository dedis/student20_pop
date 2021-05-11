import React from 'react';
import PropTypes from 'prop-types';
import {
  StyleSheet, TextInput, TextStyle, View, ViewStyle,
} from 'react-native';
import DeleteButton from 'components/DeleteButton';
import STRINGS from 'res/strings';
import { Spacing, Typography, Views } from 'styles';

/**
 * TextInput component which is removable by clicking the trashcan
 * It is used by the TextInputList.tsx component
 */

const styles = StyleSheet.create({
  view: {
    ...Views.base,
    flexDirection: 'row',
    zIndex: 3,
  } as ViewStyle,
  textInput: {
    ...Typography.base,
    borderBottomWidth: 2,
    marginVertical: Spacing.s,
    marginHorizontal: Spacing.xl,
  } as TextStyle,
});

const RemovableTextInput = (props: IPropTypes) => {
  const { onRemove } = props;
  const { onChange } = props;
  const { id } = props;
  const { value } = props;
  const placeholder = STRINGS.add_option;

  return (
    <View style={styles.view}>
      <TextInput
        style={styles.textInput}
        placeholder={placeholder}
        onChangeText={(text: string) => { onChange(id, text); }}
        key={id}
        value={value}
      />
      <DeleteButton action={() => { onRemove(id); }} />
    </View>
  );
};

const propTypes = {
  onRemove: PropTypes.func.isRequired,
  onChange: PropTypes.func.isRequired,
  id: PropTypes.number.isRequired,
  value: PropTypes.string,
};

RemovableTextInput.propTypes = propTypes;

RemovableTextInput.defaultProps = {
  value: '',
};

type IPropTypes = {
  onRemove: Function,
  onChange: Function,
  id: number,
  value: string,
};

export default RemovableTextInput;
