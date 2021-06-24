import React from 'react';
import { useSelector } from 'react-redux';

import EventListCollapsible from 'components/eventList/EventListCollapsible';
import LaoProperties from 'components/eventList/LaoProperties';
import { ScrollView } from 'react-native';
import { makeEventsList } from 'store/reducers';
import { LaoEvent, Timestamp } from 'model/objects';

/**
 * Organizer screen: lists editable LAO properties and past/ongoing/future events
 *
 * TODO By default only the past and present section are open.
 * TODO use the data received by the organization server
*/
const Organizer = () => {
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
  // Todo: Automatically move the events to the correct array with useEffect

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
      { /* Add edit button for the organizer in the Lao properties panel */ }
      <LaoProperties />
      <EventListCollapsible isOrganizer data={DATA_EXAMPLE} />
    </ScrollView>
  );
};

export default Organizer;
