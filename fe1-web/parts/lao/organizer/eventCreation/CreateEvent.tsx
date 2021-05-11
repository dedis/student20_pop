import React from 'react';
import { useNavigation } from '@react-navigation/native';
import {
  StyleSheet, View, TextStyle, ViewStyle,
} from 'react-native';

import { Spacing, Typography, Views } from 'styles';
import STRINGS from 'res/strings';
import stylesContainer from 'styles/stylesheets/container';

import TextBlock from 'components/TextBlock';
import WideButtonView from 'components/WideButtonView';

const styleEvents = StyleSheet.create({
  view: {
    ...Views.base,
    flexDirection: 'row',
    zIndex: 3,
  } as ViewStyle,
  viewVertical: {
    ...Views.base,
    flexDirection: 'column',
    zIndex: 3,
  } as ViewStyle,
  textInput: {
    ...Typography.base,
    borderBottomWidth: 2,
    marginVertical: Spacing.s,
    marginHorizontal: Spacing.xl,
  } as TextStyle,
});

enum EventTypes {
  MEETING = 'Meeting',
  ROLL_CALL = 'Roll-Call',
  ELECTION = 'Election',
}

/**
 * Navigation panels to help manoeuvre through events creation
 */
const CreateEvent = () => {
  const navigation = useNavigation();

  const navigateToPanel = (type: string) => {
    switch (type) {
      case EventTypes.MEETING:
        navigation.navigate(STRINGS.organizer_navigation_creation_meeting, styleEvents);
        break;

      case EventTypes.ROLL_CALL:
        navigation.navigate(STRINGS.organizer_navigation_creation_roll_call, styleEvents);
        break;

      case EventTypes.ELECTION:
        navigation.navigate(STRINGS.organizer_navigation_creation_election, styleEvents);
        break;

      default:
        console.debug(`${type} (default event => no mapping in CreateEvent.tsx)`);
    }
  };

  return (
    <View style={stylesContainer.flex}>
      <TextBlock text={STRINGS.create_description} />

      { Object.values(EventTypes).map(
        (type: string) => (
          <WideButtonView title={type} key={`wide-btn-view-${type}`} onPress={() => navigateToPanel(type)} />
        ),
      )}

      <WideButtonView
        title={STRINGS.general_button_cancel}
        onPress={navigation.goBack}
      />
    </View>
  );
};

export default CreateEvent;
