import React from 'react';
import PropTypes from 'prop-types';
import DateTimePicker from '@react-native-community/datetimepicker';

const DatePicker = (props: IPropTypes) => {
  const { selected } = props;
  const { onChange } = props;
  const initialEndDate = new Date();

  return (
    <DateTimePicker
      value={initialEndDate}
      mode="default"
      display="default"
      onChange={(date: any) => onChange(date)}
    />
  );
};

const propTypes = {
  // eslint-disable-next-line react/forbid-prop-types
  selected: PropTypes.any,
  onChange: PropTypes.func.isRequired,
};
DatePicker.propTypes = propTypes;

DatePicker.defaultProps = {
  selected: undefined,
};

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default DatePicker;
