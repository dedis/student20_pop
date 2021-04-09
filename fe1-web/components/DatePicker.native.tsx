import React, { useState } from 'react';
import PropTypes from 'prop-types';
import DateTimePicker from '@react-native-community/datetimepicker';
import { View, Button, Platform } from 'react-native';

const DatePicker = (props: IPropTypes) => {
  const { selected } = props;
  const { onChange } = props;
  const initialEndDate = new Date();

  const [date, setDate] = useState(new Date(1598051730000));
  const [mode, setMode] = useState('date');
  const [show, setShow] = useState(false);

  const changeDate = (event, selectedDate) => {
    const currentDate = selectedDate || date;
    setShow(Platform.OS === 'ios');
    setDate(currentDate);
  };

  const showMode = (currentMode) => {
    setShow(true);
    setMode(currentMode);
  };

  const showDatepicker = () => {
    showMode('date');
  };

  const showTimepicker = () => {
    showMode('time');
  };


  return (
    <View>
      <View>
        <Button onPress={showDatepicker} title="Pick Date" />
      </View>
      <View>
        <Button onPress={showTimepicker} title="Show time picker!" />
      </View>
      <DateTimePicker
        value={initialEndDate}
        mode="default"
        display="default"
        onChange={(date: any) => changeDate(date)}
      />
    </View>

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
