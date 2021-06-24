import React from 'react';
import { useSelector } from 'react-redux';
import { ScrollView } from 'react-native';

import { makeEventsList } from 'store';
import { LaoEvent, Timestamp } from 'model/objects';

import EventListCollapsible from 'components/eventList/EventListCollapsible';
import LaoProperties from 'components/eventList/LaoProperties';

/**
 * Attendee screen: lists LAO properties and past/ongoing/future events
 *
 * TODO By default only the past and present section are open.
 * TODO use the data receive by the organization server
 */
const Attendee = () => {
  const eventList = makeEventsList();
  const events = useSelector(eventList);

  const now = Timestamp.EpochNow();
  const pastEvents: LaoEvent[] = [];
  const currentEvents: LaoEvent[] = [];
  const futureEvents: LaoEvent[] = [];

  events.forEach((e: LaoEvent) => {
    if ((e.end && e.end.before(now))
      || (!e.end && e.start.before(now))) {
      pastEvents.push(e);
      return;
    }
    if (e.start.after(now)) {
      futureEvents.push(e);
      return;
    }
    currentEvents.push(e);
  });
  // TODO: nesting logic

  const DATA_EXAMPLE = [
    {
      title: 'Future',
      data: futureEvents,
    },
    {
      title: 'Present',
      data: currentEvents,
    },
    {
      title: 'Past',
      data: pastEvents,
    },
  ];

  return (
    <ScrollView>
      <LaoProperties />
      <EventListCollapsible data={DATA_EXAMPLE} />
    </ScrollView>
  );
};

export default Attendee;
