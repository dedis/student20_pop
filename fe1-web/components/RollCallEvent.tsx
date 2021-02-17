import React from 'react';
import {
  StyleSheet, View, Text, FlatList,
} from 'react-native';

import { Spacing } from 'styles';
import PROPS_TYPE from 'res/Props';

import EventItem from './EventItem';

/**
 * Roll-call component: a status (Future, Open, Closed), the number of participants,
 * a QR code to show to the organizer and a list of the nested events
 *
 * TODO implement the QR code
 * TODO use data of the organizer server
*/
const styles = StyleSheet.create({
  view: {
    marginHorizontal: Spacing.s,
    borderWidth: 1,
    borderRadius: 5,
    paddingHorizontal: Spacing.xs,
    marginBottom: Spacing.xs,
  },
  text: {
  },
  flatList: {
    marginTop: Spacing.xs,
  },
});

const RollCallEvent = ({ event }) => {
  const getState = () => {
    if (event.scheduled) {
      return 'Future';
    }
    if (event.end) {
      return 'Close';
    }
    return 'Open';
  };

  return (
    <View style={styles.view}>
      <Text style={styles.text}>{`Status: ${getState()}`}</Text>
      <Text style={styles.text}>Participants #</Text>
      {!event.scheduled && !event.end && (
        <Text style={styles.text}>QR code</Text>
      )}
      <FlatList
        data={event.children}
        keyExtractor={(item) => item.id.toString()}
        renderItem={({ item }) => <EventItem event={item} />}
        listKey={`RollCallEvent-${event.id.toString()}`}
        style={styles.flatList}
      />
    </View>
  );
};

RollCallEvent.propTypes = {
  event: PROPS_TYPE.event.isRequired,
};

export default RollCallEvent;