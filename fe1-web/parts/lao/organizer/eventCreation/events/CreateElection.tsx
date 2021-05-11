import React, { useState } from 'react';
import {
  View, Platform, TextInput, ScrollView,
} from 'react-native';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigation } from '@react-navigation/native';

import STRINGS from 'res/strings';
import DatePicker from 'components/DatePicker';
import ParagraphBlock from 'components/ParagraphBlock';
import WideButtonView from 'components/WideButtonView';
import {
  Hash, Lao, Timestamp, Question, EventTags,
} from 'model/objects';
import TextBlock from 'components/TextBlock';
import DropdownSelector from 'components/DropdownSelector';
import TextInputList from 'components/TextInputList';
import { requestCreateElection } from 'network';
import { OpenedLaoStore } from 'store';

/**
 * UI to create an Election Event
 *
 * TODO Implement support for multiple questions
 */

const CreateElection = ({ route }: any) => {
  const styles = route.params;
  const navigation = useNavigation();
  const initialStartDate = new Date();
  const initialEndDate = new Date();
  // Sets initial end date to 1 hour later than start date
  initialEndDate.setHours(initialEndDate.getHours() + 1);

  const [startDate, setStartDate] = useState(Timestamp.dateToTimestamp(initialStartDate));
  const [endDate, setEndDate] = useState(Timestamp.dateToTimestamp(initialEndDate));

  const [electionName, setElectionName] = useState('');
  const [electionQuestion, setElectionQuestion] = useState('');
  const [electionBallots, setElectionBallots] = useState(['']);
  const votingMethods = [STRINGS.election_method_Plurality, STRINGS.election_method_Approval];
  const [votingMethod, setVotingMethod] = useState(votingMethods[0]);
  const minBallotOptions = 2;
  const currentLao: Lao = OpenedLaoStore.get();

  const buildDatePickerWeb = () => {
    const startTime = new Date(0);
    const endTime = new Date(1);
    startTime.setUTCSeconds(startDate.valueOf());
    endTime.setUTCSeconds(endDate.valueOf());

    return (
      <View style={styles.viewVertical}>
        <View style={[styles.view, { padding: 5 }]}>
          <ParagraphBlock text={STRINGS.election_create_start_time} />
          <DatePicker
            selected={startTime}
            onChange={(date: Date) => setStartDate(Timestamp.dateToTimestamp(date))}
          />
        </View>
        <View style={[styles.view, { padding: 5, zIndex: 'initial' }]}>
          <ParagraphBlock text={STRINGS.election_create_finish_time} />
          <DatePicker
            selected={endTime}
            onChange={(date: Date) => setEndDate(Timestamp.dateToTimestamp(date))}
          />
        </View>
      </View>
    );
  };

  const QuestionObject: Question = {
    id: Hash.fromStringArray(
      EventTags.QUESTION, currentLao.id.toString(), electionQuestion,
    ).toString(),
    question: electionQuestion,
    voting_method: votingMethod,
    ballot_options: electionBallots,
    write_in: false,
  };

  // Confirm button only clickable when the Name, Question and 2 Ballot options have values
  const buttonsVisibility: boolean = (electionQuestion !== '' && electionName !== ''
    && electionBallots.length >= minBallotOptions);

  const onConfirmPress = () => {
    requestCreateElection(
      electionName,
      STRINGS.election_version_identifier,
      startDate,
      endDate,
      [QuestionObject],
    )
      .then(() => {
        navigation.navigate(STRINGS.organizer_navigation_tab_home);
      })
      .catch((err) => {
        console.error('Could not create Election, error:', err);
      });
  };

  return (
    <ScrollView>
      <TextBlock text={STRINGS.election_create_setup} bold />
      <TextInput
        style={styles.textInput}
        placeholder={STRINGS.election_create_name}
        onChangeText={(text: string) => { setElectionName(text); }}
      />
      { /* see archive branches for date picker used for native apps */ }
      { Platform.OS === 'web' && buildDatePickerWeb() }
      <TextBlock text={STRINGS.election_voting_method} />
      <DropdownSelector
        values={votingMethods}
        onChange={(method: string) => setVotingMethod(method)}
      />
      <TextInput
        style={styles.textInput}
        placeholder={STRINGS.election_create_question}
        onChangeText={(text: string) => { setElectionQuestion(text); }}
      />
      <TextInputList onChange={setElectionBallots} />
      <View style={[styles.view, { zIndex: 'initial' }]}>
        <WideButtonView
          title={STRINGS.general_button_cancel}
          onPress={navigation.goBack}
        />
        <WideButtonView
          title={STRINGS.general_button_confirm}
          onPress={onConfirmPress}
          disabled={!buttonsVisibility}
        />
      </View>
    </ScrollView>
  );
};

export default CreateElection;
