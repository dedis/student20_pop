import React from 'react';
import PropTypes from 'prop-types';
import { Picker } from '@react-native-picker/picker';
import styles from 'styles/stylesheets/container';

/**
 * Simple dropdown where the user can select options
 * Inputs:
 *  - String array of all options (required)
 *  - Callback function for when an item is selected (required)
 *      (e.g. {(method: string) => setSelectedElectionMethod(method)} )
 *  - Default selected option (not required, if not specified then first in the array is chosen)
 */

const DropdownSelector = (props: IPropTypes) => {
  const { selected } = props;
  const { onChange } = props;
  const { values } = props;
  const options: any = [];
  values.forEach((value) => options.push(<Picker.Item key={value} label={value || ''} />));
  return (
    <Picker
      selectedValue={selected}
      onValueChange={(val: any) => onChange(val)}
      style={styles.centerWithMargin}
    >
      { options }
    </Picker>
  );
};

const propTypes = {
  selected: PropTypes.string,
  onChange: PropTypes.func.isRequired,
  values: PropTypes.arrayOf(PropTypes.string).isRequired,
};
DropdownSelector.propTypes = propTypes;

DropdownSelector.defaultProps = {
  selected: undefined,
};

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default DropdownSelector;
