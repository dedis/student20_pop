import React from 'react';
import {
  StyleSheet, View, Button, FlatList,
} from 'react-native';
import STRINGS from '../res/strings';
import PROPS_TYPE from '../res/Props';
import { Buttons, Typography } from '../Styles';
import Attendee from './Attendee';

/**
* The Witness component
*
* Manage the Witness screen
*/
const styles = StyleSheet.create({
  container: {
  },
  text: {
    ...Typography.base,
  },
  button: {
    ...Buttons.base,
  },
});

const Witness = ({ navigation }) => (
  <FlatList
    style={styles.container}
    ListHeaderComponent={(
      <View>
        <View style={styles.button}>
          <Button
            onPress={() => navigation.navigate(STRINGS.witness_navigation_tab_video)}
            title={STRINGS.witness_video_button}
          />
        </View>
        <Attendee />
      </View>
    )}
  />
);

Witness.propTypes = {
  navigation: PROPS_TYPE.navigation.isRequired,
};

export default Witness;
