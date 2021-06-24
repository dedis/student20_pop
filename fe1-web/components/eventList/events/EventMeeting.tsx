import React from 'react';
import { FlatList } from 'react-native';

import PROPS_TYPE from 'res/Props';
import ParagraphBlock from 'components/ParagraphBlock';
import TimeDisplay from 'components/TimeDisplay';
import PropTypes from 'prop-types';

/**
 * Component used to display a Meeting event in the LAO event list
 */

const EventMeeting = (props: IPropTypes) => {
  const { event } = props;
  const { childrenVisibility } = props;
  const { renderItemFn } = props;

  return (
    <>
      <TimeDisplay start={event.start.valueOf()} />
      { event.end && <TimeDisplay end={event.end.valueOf()} /> }
      { event.location && <ParagraphBlock text={event.location} /> }
      { childrenVisibility && (
        <FlatList
          data={event.children}
          keyExtractor={(item) => item.id.toString()}
          renderItem={renderItemFn}
          listKey={`MeetingEvent-${event.id.toString()}`}
        />
      )}
    </>
  );
};

const propTypes = {
  event: PROPS_TYPE.meeting.isRequired,
  childrenVisibility: PropTypes.bool.isRequired,
  renderItemFn: PropTypes.func.isRequired,
};
EventMeeting.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default EventMeeting;
