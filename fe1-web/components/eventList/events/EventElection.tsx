import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import {
  Election,
  ElectionStatus,
  EventTags,
  Hash,
  QuestionResult,
  RegisteredVote,
  Timestamp,
  Vote,
} from 'model/objects';
import {
  SectionList, StyleSheet, Text, TextStyle,
} from 'react-native';
import { Spacing, Typography } from 'styles';
import { castVote, terminateElection } from 'network';
import CheckboxList from 'components/CheckboxList';
import WideButtonView from 'components/WideButtonView';
import TimeDisplay from 'components/TimeDisplay';
import STRINGS from 'res/strings';
import { Badge } from 'react-native-elements';
import { dispatch, getStore, updateEvent } from 'store';
import { useSelector } from 'react-redux';
import BarChartDisplay from 'components/BarChartDisplay';
import { getEventFromId } from 'ingestion/handlers/Utils';

/**
 * Component used to display a Election event in the LAO event list
 */

const styles = StyleSheet.create({
  text: {
    ...Typography.base,
  } as TextStyle,
  textOptions: {
    marginHorizontal: Spacing.s,
    fontSize: 16,
    textAlign: 'center',
  } as TextStyle,
  textQuestions: {
    ...Typography.base,
    fontSize: 20,
  } as TextStyle,
});

const EventElection = (props: IPropTypes) => {
  const { election } = props;
  const { isOrganizer } = props;
  const questions = election.questions.map((q) => ({ title: q.question, data: q.ballot_options }));
  const [selectedBallots, setSelectedBallots] = useState(new Array(questions.length).fill([]));
  const [hasVoted, setHasVoted] = useState(0);
  const untilStart = (election.start.valueOf() - Timestamp.EpochNow().valueOf()) * 1000;
  const untilEnd = (election.end.valueOf() - Timestamp.EpochNow().valueOf()) * 1000;
  // This makes sure the election status is always updated
  const electionFromStore = useSelector((state) => (
    // @ts-ignore
    state.events.byLaoId[election.lao].byId[election.id]));
  if (!electionFromStore) {
    console.debug('Error in Election display: Election doesnt exist in store');
    return null;
  }

  const updateSelectedBallots = (values: number[], idx: number) => {
    setSelectedBallots((prev) => prev.map((item, id) => ((idx === id) ? values : item)));
  };
  const concatenateIndexes = (indexes: number[]) => {
    let concatenated = '';
    indexes.forEach((index) => {
      concatenated += index.toString();
    });
    return concatenated;
  };

  // Prepares the votes with the hash and the vote indexes to match the protocol
  // id: SHA256('Vote'||election_id||question_id||(vote_index(es)|write_in))
  const refactorVotes = (selected: number[][]) => {
    const votes: Vote[] = selected.map((item, idx) => ({
      id: Hash.fromStringArray(
        EventTags.VOTE,
        election.id.toString(),
        election.questions[idx].id,
        concatenateIndexes(item),
      ),
      question: new Hash(election.questions[idx].id),
      vote: item,
    }));
    return votes;
  };

  const onCastVote = () => {
    castVote(election.id, refactorVotes(selectedBallots))
      .then(() => setHasVoted((prev) => prev + 1))
      .catch((err) => {
        console.error('Could not cast Vote, error:', err);
      });
  };

  const calculateVoteHash = () => {
    const votes: { messageId: number, voteIDs: Hash[]; }[] = electionFromStore.registered_votes.map(
      (registeredVote: RegisteredVote) => (
        {
          messageId: registeredVote.messageId,
          voteIDs: registeredVote.votes.map((vote) => vote.id),
        }
      ),
    );
    // Sort by message ID
    votes.sort((a, b) => (
      a.messageId.valueOf() < b.messageId.valueOf() ? -1 : 1));
    const arrayToHash: Hash[] = [];
    votes.forEach((registeredVote) => { arrayToHash.push(...registeredVote.voteIDs); });
    const stringArray: string[] = arrayToHash.map((hash) => hash.valueOf());
    return Hash.fromStringArray(...stringArray);
  };

  const onTerminateElection = () => {
    console.log('Terminating Election');
    terminateElection(election.id, calculateVoteHash())
      .then(() => console.log('Election Terminated'))
      .catch((err) => {
        console.error('Could not terminate election, error:', err);
      });
  };

  const updateElection = (status: ElectionStatus) => {
    const storeState = getStore().getState();
    const oldElec = getEventFromId(storeState, election.id) as Election;
    const newElec = new Election({ ...oldElec, electionStatus: status });
    dispatch(updateEvent(election.lao, newElec.toState()));
  };

  // This makes sure the screen gets updated when the event starts
  useEffect(() => {
    if (untilStart >= 0) {
      const startTimer = setTimeout(() => {
        updateElection(ElectionStatus.RUNNING);
        // setStatus(ElectionStatus.RUNNING);
      }, untilStart);
      return () => clearTimeout(startTimer);
    }
    return () => {};
  }, []);

  // This makes sure the screen gets updated when the event ends - user can't vote anymore
  useEffect(() => {
    if (untilEnd >= 0) {
      const endTimer = setTimeout(() => {
        updateElection(ElectionStatus.FINISHED);
      }, untilEnd);
      return () => clearTimeout(endTimer);
    }
    return () => {};
  }, []);

  // Here we use the election object form the redux store in order to see the electionStatus
  // update when an  incoming electionEnd or electionResult message comes (in handler/Election.ts)
  const getElectionDisplay = (status: ElectionStatus) => {
    switch (status) {
      case ElectionStatus.NOTSTARTED:
        return (
          <SectionList
            sections={questions}
            keyExtractor={(item, index) => item + index}
            renderSectionHeader={({ section: { title } }) => (
              <Text style={styles.textQuestions}>{title}</Text>
            )}
            renderItem={({ item }) => (
              <Text style={styles.textOptions}>{`\u2022 ${item}`}</Text>
            )}
          />
        );
      case ElectionStatus.RUNNING:
        return (
          <>
            {(questions.map((q, idx) => (
              <CheckboxList
                key={q.title + idx.toString()}
                title={q.title}
                values={q.data}
                onChange={(values: number[]) => updateSelectedBallots(values, idx)}
              />
            )))}
            <WideButtonView
              title={STRINGS.cast_vote}
              onPress={onCastVote}
            />
            <Badge value={hasVoted} status="success" />
          </>
        );
      case ElectionStatus.FINISHED:
        return (
          <>
            <Text style={styles.text}>Election finished</Text>
            {isOrganizer && (
              <WideButtonView
                title="Terminate Election / Tally Votes"
                onPress={onTerminateElection}
              />
            )}
          </>
        );
      case ElectionStatus.TERMINATED:
        return (
          <>
            <Text style={styles.text}>Election Terminated</Text>
            <Text style={styles.text}>Waiting for result</Text>
          </>
        );
      case ElectionStatus.RESULT:
        return (
          <>
            <Text style={styles.text}>Election Result</Text>
            {electionFromStore.questionResult.map((question: QuestionResult) => (
              <BarChartDisplay data={question.result} key={question.id.valueOf()} />
            ))}
          </>
        );
      default:
        console.warn('Election Status was undefined in Election display', electionFromStore);
        return null;
    }
  };

  return (
    <>
      <TimeDisplay start={election.start.valueOf()} end={election.end.valueOf()} />
      {getElectionDisplay(electionFromStore.electionStatus)}
    </>
  );
};

const propTypes = {
  election: PropTypes.instanceOf(Election).isRequired,
  isOrganizer: PropTypes.bool.isRequired,
};
EventElection.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default EventElection;
