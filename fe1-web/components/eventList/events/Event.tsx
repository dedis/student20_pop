import React, { useState } from 'react';
import { View, ViewStyle, TouchableOpacity } from 'react-native';
import PropTypes from 'prop-types';

import { Spacing } from 'styles';

import ParagraphBlock from 'components/ParagraphBlock';
import styleEventView from 'styles/stylesheets/eventView';
import TextBlock from 'components/TextBlock';
import {
  Election, Hash, RollCall, Timestamp,
} from 'model/objects';
import { Meeting } from 'model/objects/Meeting';
import EventMeeting from './EventMeeting';
import EventRollCall from './EventRollCall';
import EventElection from './EventElection';
import ListCollapsibleIcon from '../ListCollapsibleIcon';

/**
 * The Event item component: display the correct representation of the event according to its type,
 * otherwise display its name and in all cases its nested events
*/
const Event = (props: IPropTypes) => {
  const { event } = props;
  const { isOrganizer } = props;
  const { renderItemFn } = props;

  const [toggleChildrenVisible, setToggleChildrenVisible] = useState(false);

  const hasChildren = (): boolean => !!event.children && event.children.length !== 0;

  const toggleChildren = () => {
    setToggleChildrenVisible(hasChildren() && !toggleChildrenVisible);
  };

  const buildListCollapsibleIcon = () => (
    <TouchableOpacity onPress={toggleChildren} style={{ textAlign: 'right' } as ViewStyle}>
      <ListCollapsibleIcon isOpen={toggleChildrenVisible} />
    </TouchableOpacity>
  );

  const buildEvent = () => {
    if (event instanceof Meeting) {
      return (
        <EventMeeting
          event={event}
          childrenVisibility={toggleChildrenVisible}
          renderItemFn={renderItemFn}
        />
      );
    }
    if (event instanceof RollCall) {
      // if (isOrganizer) {
      //   console.log('is organizer => returning null in Event');
      //   return null;
      // }
      return (
        <EventRollCall
          event={event}
          isOrganizer={isOrganizer}
        />
      );
    }
    if (event instanceof Election) {
      return (
        <EventElection
          election={event}
          isOrganizer={isOrganizer}
        />
      );
    }
    return <ParagraphBlock text={`${event.name} (default event => no mapping in Event.tsx)`} />;
  };

  return (
    <View style={[styleEventView.default, { marginTop: Spacing.s }]}>
      <TextBlock text={event.name} />
      { hasChildren() && buildListCollapsibleIcon() }
      { buildEvent() }
    </View>
  );
};

const propTypes = {
  event: PropTypes.shape({
    id: PropTypes.instanceOf(Hash).isRequired,
    start: PropTypes.instanceOf(Timestamp).isRequired,
    end: PropTypes.instanceOf(Timestamp),
  }).isRequired,
  renderItemFn: PropTypes.func.isRequired,
  isOrganizer: PropTypes.bool.isRequired,
};
Event.propTypes = propTypes;

type IPropTypes = PropTypes.InferProps<typeof propTypes>;

export default Event;
