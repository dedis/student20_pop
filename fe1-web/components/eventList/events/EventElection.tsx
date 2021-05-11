import React from 'react';
import ParagraphBlock from 'components/ParagraphBlock';
import PropTypes from 'prop-types';
import { Election, Timestamp } from 'model/objects';
import { SectionList, Text } from 'react-native';

/**
 * Component used to display a Election event in the LAO event list
 */

const EventElection = (props: IPropTypes) => {
  const { event } = props;
  const startsAtString = event.start.before(Timestamp.EpochNow()) ? 'Started at' : 'Starts at';
  const endsAtString = event.end.before(Timestamp.EpochNow()) ? 'Ended at' : 'Ends at';
  const questions = event.questions.map((q) => ({ title: q.question, data: q.ballot_options }));

  return (
    <>
      <ParagraphBlock text={`${startsAtString} ${event.start.timestampToString()}`} />
      <ParagraphBlock text={`${endsAtString} ${event.end.timestampToString()}`} />
      <SectionList
        sections={questions}
        keyExtractor={(item, index) => item + index}
        renderSectionHeader={({ section: { title } }) => (
          <Text>{title}</Text>
        )}
        renderItem={({ item }) => (
          <Text>{`\u2022 ${item}`}</Text>
        )}
      />
    </>
  );
};

const propTypes = {
  event: PropTypes.instanceOf(Election).isRequired,
};
EventElection.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default EventElection;
