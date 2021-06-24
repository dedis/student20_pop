import React from 'react';
import { ScrollView } from 'react-native';

import STRINGS from 'res/strings';
import { useNavigation } from '@react-navigation/native';
import { useSelector } from 'react-redux';
import LaoProperties from 'components/eventList/LaoProperties';
import EventListCollapsible from 'components/eventList/EventListCollapsible';
import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';
import { makeEventsList } from 'store/reducers';
import { LaoEvent, Timestamp } from 'model/objects';

/**
 * Witness screen: button to navigate to the witness video screen,
 * a section list of events and lao properties
*/
const Witness = () => {
  const navigation = useNavigation();

  const eventList = makeEventsList();
  const events = useSelector(eventList);

  const now = Timestamp.EpochNow();
  const pastEvents: LaoEvent[] = [];
  const currentEvents: LaoEvent[] = [];
  const futureEvents: LaoEvent[] = [];

  events.forEach((e: LaoEvent) => {
    if (new Timestamp(e.end).before(now)) {
      pastEvents.push(e);
      return;
    }
    if (new Timestamp(e.start).after(now)) {
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
      <TextBlock bold text="Witness Panel" />
      <WideButtonView
        title={STRINGS.witness_video_button}
        onPress={() => navigation.navigate(STRINGS.witness_navigation_tab_video)}
      />
      <LaoProperties />
      <EventListCollapsible data={DATA_EXAMPLE} />
    </ScrollView>
  );
};

export default Witness;
