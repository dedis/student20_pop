import React, { useState } from 'react';
import {
  View, Platform, TextInput, ScrollView,
} from 'react-native';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigation } from '@react-navigation/native';

import STRINGS from 'res/strings';
import { requestCreateRollCall } from 'network/MessageApi';
import DatePicker from 'components/DatePicker';
import ParagraphBlock from 'components/ParagraphBlock';
import WideButtonView from 'components/WideButtonView';
import { Timestamp } from 'model/objects';

function dateToTimestamp(date: Date): Timestamp {
  return new Timestamp(Math.floor(date.getTime() / 1000));
}

/**
 * Screen to create a roll-call event
 *
 * TODO Send the Roll-call event in an open state to the organization server
 *  when the confirm button is press
 */
const CreateRollCall = ({ route }: any) => {
  const styles = route.params;
  const navigation = useNavigation();
  const initialStartDate = new Date();
  const initialEndDate = new Date();
  // Sets initial start date 5 minutes in the future to avoid: proposed_start < creation
  initialStartDate.setMinutes(initialStartDate.getMinutes() + 5);
  // Sets initial end date to 1 hour later than start date
  initialEndDate.setHours(initialEndDate.getHours() + 1);

  const [proposedStartDate, setProposedStartDate] = useState(dateToTimestamp(initialStartDate));
  const [proposedEndDate, setProposedEndDate] = useState(dateToTimestamp(initialEndDate));

  const [rollCallName, setRollCallName] = useState('');
  const [rollCallLocation, setRollCallLocation] = useState('');
  const [rollCallDescription, setRollCallDescription] = useState('');

  const buildDatePickerWeb = () => {
    const startTime = new Date(0);
    const endTime = new Date(0);
    startTime.setUTCSeconds(proposedStartDate.valueOf());
    endTime.setUTCSeconds(proposedEndDate.valueOf());

    return (
      <View style={styles.view}>
        <ParagraphBlock text={STRINGS.roll_call_create_proposed_start} />
        <DatePicker
          selected={startTime}
          onChange={(date: Date) => setProposedStartDate(dateToTimestamp(date))}
        />
        <ParagraphBlock text={STRINGS.roll_call_create_proposed_end} />
        <DatePicker
          selected={endTime}
          onChange={(date: Date) => setProposedEndDate(dateToTimestamp(date))}
        />
      </View>
    );
  };

  const buttonsVisibility: boolean = (rollCallName !== '' && rollCallLocation !== '');

  const createRollCall = () => {
    const description = (rollCallDescription === '') ? undefined : rollCallDescription;
    requestCreateRollCall(
      rollCallName, rollCallLocation, proposedStartDate, proposedEndDate,
      description,
    )
      .then(() => {
        navigation.navigate(STRINGS.organizer_navigation_tab_home);
      })
      .catch((err) => {
        console.error('Could not create roll call, error:', err);
      });
  };

  const onConfirmPress = () => createRollCall();

  return (
    <ScrollView>
      { /* see archive branches for date picker used for native apps */ }
      { Platform.OS === 'web' && buildDatePickerWeb() }

      <TextInput
        style={styles.textInput}
        placeholder={STRINGS.roll_call_create_name}
        onChangeText={(text: string) => { setRollCallName(text); }}
      />
      <TextInput
        style={styles.textInput}
        placeholder={STRINGS.roll_call_create_location}
        onChangeText={(text: string) => { setRollCallLocation(text); }}
      />
      <TextInput
        style={styles.textInput}
        placeholder={STRINGS.roll_call_create_description}
        onChangeText={(text: string) => { setRollCallDescription(text); }}
      />

      <WideButtonView
        title={STRINGS.general_button_confirm}
        onPress={onConfirmPress}
        disabled={!buttonsVisibility}
      />
      <WideButtonView
        title={STRINGS.general_button_cancel}
        onPress={navigation.goBack}
      />
    </ScrollView>
  );
};

export default CreateRollCall;
