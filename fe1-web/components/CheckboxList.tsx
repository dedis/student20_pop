import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {
  StyleSheet, View, ViewStyle, Text, TextStyle,
} from 'react-native';
import { Views, Typography } from 'styles';
import { CheckBox } from 'react-native-elements';

/**
 * Component with a title and a list of checkboxes below it.
 * Required Input: title (string), values of the checkboxes (string[]) and a function which accepts
 * the selected options
 * Optional Input: clickable options (number), it determines how many options are selectable by
 * the user. Default is 1
 * Returns: index of the selected options
 */

const styles = StyleSheet.create({
  text: {
    ...Typography.base,
  } as TextStyle,
  view: {
    ...Views.base,
    flexDirection: 'column',
    zIndex: 3,
  } as ViewStyle,
});

const CheckboxList = (props: IPropTypes) => {
  const { values } = props;
  const { onChange } = props;
  const { clickableOptions } = props;
  const { title } = props;
  const { disabled } = props;
  const [checked, setChecked] = useState(new Array(values.length).fill(false));

  // This function determines whether the user can check or uncheck a given checkbox
  const onCheckBoxPress = (idx: number): void => {
    if (checked[idx] === true || !(clickableOptions === checked.filter(Boolean).length)) {
      // A user can always uncheck || a user can check a box if he hasn't reached the max number of
      // selectable options yet
      setChecked((prev) => prev.map((item, id) => (idx === id ? !item : item)));
      onChange(values.map((val, id) => {
        if (checked[id] === true && id !== idx) {
          return idx;
        }
        if (id === idx && checked[id] === false) {
          return idx;
        }
        return -1;
      }).filter((prev) => prev !== -1));
    } else if (clickableOptions === 1) {
      // if only 1 selectable option the buttons should behave like radio buttons
      // (User doesn't have to deselect his option to select a new option)
      setChecked((prev) => prev.map((item, id) => (idx === id)));
      onChange(values.map((val, id) => ((id === idx) ? id : -1))
        .filter((prev) => prev !== -1));
    }
  };

  return (
    <View style={styles.view}>
      <Text style={styles.text}>{title}</Text>
      <View style={{ ...styles.view, flexDirection: 'row' }}>
        {values.map((value, idx) => (
          <CheckBox
            key={value}
            disabled={disabled}
            title={value}
            checked={checked[idx]}
            checkedIcon="dot-circle-o"
            uncheckedIcon="circle-o"
            onPress={() => onCheckBoxPress(idx)}
          />
        ))}
      </View>
    </View>
  );
};

const propTypes = {
  values: PropTypes.arrayOf(PropTypes.string).isRequired,
  onChange: PropTypes.func.isRequired,
  clickableOptions: PropTypes.number,
  title: PropTypes.string.isRequired,
  disabled: PropTypes.bool,
};

CheckboxList.propTypes = propTypes;

CheckboxList.defaultProps = {
  clickableOptions: 1,
  disabled: false,
};

type IPropTypes = {
  clickableOptions: number,
  values: string[],
  onChange: Function,
  title: string,
  disabled: boolean,
};

export default CheckboxList;
