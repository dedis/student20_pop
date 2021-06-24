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
 */

const CreateElection = ({ route }: any) => {
  const styles = route.params;
  const navigation = useNavigation();
  const initialStartDate = new Date();
  const initialEndDate = new Date();
  // Sets initial end date to 1 hour later than start date
  initialEndDate.setMinutes(initialEndDate.getMinutes() + 15);

  const [startDate, setStartDate] = useState(Timestamp.dateToTimestamp(initialStartDate));
  const [endDate, setEndDate] = useState(Timestamp.dateToTimestamp(initialEndDate));
  const [electionName, setElectionName] = useState('');
  const votingMethods = [STRINGS.election_method_Plurality, STRINGS.election_method_Approval];
  const minBallotOptions = 2;
  const currentLao: Lao = OpenedLaoStore.get();
  const emptyQuestion = { question: '', voting_method: votingMethods[0], ballot_options: [''] };
  const [questions, setQuestions] = useState([emptyQuestion]);

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

  const getQuestionObjects = (): Question[] => questions.map((item) => (
    {
      ...item,
      id: Hash.fromStringArray(
        EventTags.QUESTION, currentLao.id.toString(), item.question,
      ).toString(),
      write_in: false,
    }));

  const isInvalid = (obj: Question): boolean => (obj.question === ''
    || obj.ballot_options.length < minBallotOptions);

  // Confirm button only clickable when the Name, Question and 2 Ballot options have values
  const buttonsVisibility: boolean = (electionName !== ''
    && !getQuestionObjects().some(isInvalid));

  const onConfirmPress = () => {
    console.log(getQuestionObjects());
    const createdAt = Timestamp.EpochNow();
    const electionId = Hash.fromStringArray(
      EventTags.ELECTION, currentLao.id.toString(), createdAt.toString(), electionName,
    );
    requestCreateElection(
      electionName,
      electionId,
      STRINGS.election_version_identifier,
      createdAt,
      startDate,
      endDate,
      getQuestionObjects(),
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
      { questions.map((value, idx) => (
        <View key={idx.toString()}>
          <TextInput
            style={styles.textInput}
            placeholder={STRINGS.election_create_question}
            onChangeText={(text: string) => setQuestions(
              (prev) => prev.map((item, id) => (
                (id === idx) ? { ...item, question: text } : item)),
            )}
          />
          <View style={[styles.view, { marginHorizontal: 150 }]}>
            <ParagraphBlock text={STRINGS.election_voting_method} />
            <DropdownSelector
              values={votingMethods}
              onChange={(method: string) => setQuestions(
                (prev) => prev.map((item, id) => (
                  (id === idx) ? { ...item, voting_method: method } : item)),
              )}
            />
          </View>
          <TextInputList onChange={(ballot_options: string[]) => setQuestions(
            (prev) => prev.map((item, id) => (
              (id === idx) ? { ...item, ballot_options: ballot_options } : item)),
          )}
          />
        </View>
      ))}

      <View style={[styles.view, { zIndex: 'initial' }]}>
        <WideButtonView
          title="Add Question"
          onPress={() => setQuestions((prev) => [...prev, emptyQuestion])}
        />
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
