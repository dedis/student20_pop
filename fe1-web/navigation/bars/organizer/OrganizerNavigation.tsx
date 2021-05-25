import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';

import STRINGS from 'res/strings';

import Organizer from 'parts/lao/organizer/Organizer';
import CreateEvent from 'parts/lao/organizer/eventCreation/CreateEvent';
import WitnessScanning from 'components/WitnessScanning';
import RollCallScanning from 'archive-vault/old-components/unused/RollCallScanning';
import CreateMeeting from 'parts/lao/organizer/eventCreation/events/CreateMeeting';
import CreateRollCall from 'parts/lao/organizer/eventCreation/events/CreateRollCall';
import CreateElection from 'parts/lao/organizer/eventCreation/events/CreateElection';

/**
 * Define the Organizer stack navigation
 * four different screen (Organizer, CreateEvent, WitnessScanning, RollCallScanning)
 *
 * The app are not use in the stack order, only organizer to one of the other screen
*/

const Stack = createStackNavigator();

export default function OrganizerNavigation() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <Stack.Screen
        name={STRINGS.organizer_navigation_tab_home}
        component={Organizer}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_tab_create_event}
        component={CreateEvent}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_tab_add_witness}
        component={WitnessScanning}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_tab_roll_call}
        component={RollCallScanning}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_creation_meeting}
        component={CreateMeeting}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_creation_roll_call}
        component={CreateRollCall}
      />
      <Stack.Screen
        name={STRINGS.organizer_navigation_creation_election}
        component={CreateElection}
      />
    </Stack.Navigator>
  );
}
