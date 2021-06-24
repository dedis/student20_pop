import React from 'react';
import PropTypes from 'prop-types';
import {
  StyleSheet, TextStyle, View, Text,
} from 'react-native';
import TimeAgo from 'react-timeago';
import STRINGS from 'res/strings';
import * as Spacing from '../styles/spacing';

/**
 * Displays a time in easily human readable format
 * For example: "12 hours ago", "5 minutes ago", "In 2 days"
 * On hover the exact time is displayed
 */

const styles = StyleSheet.create({
  textStandard: {
    marginHorizontal: Spacing.xs,
  } as TextStyle,
});
const TimeDisplay = (props: IPropTypes) => {
  const { start } = props;
  const { end } = props;

  let timeDisplay;

  if (start && end) {
    timeDisplay = (
      <View>
        <Text style={styles.textStandard}>
          {STRINGS.time_display_start}
          <TimeAgo date={start * 1000} />
          {'\n'}
          {STRINGS.time_display_end}
          <TimeAgo date={end * 1000} />
        </Text>
      </View>
    );
  } else if (start) {
    timeDisplay = (
      <View>
        <Text style={styles.textStandard}>
          {STRINGS.time_display_start}
          <TimeAgo date={start * 1000} />
        </Text>
      </View>
    );
  } else if (end) {
    timeDisplay = (
      <View>
        <Text style={styles.textStandard}>
          {STRINGS.time_display_end}
          <TimeAgo date={end * 1000} />
        </Text>
      </View>
    );
  } else {
    timeDisplay = null;
  }

  return (
    <>
      { timeDisplay }
    </>
  );
};

const propTypes = {
  start: PropTypes.number,
  end: PropTypes.number,
};
TimeDisplay.propTypes = propTypes;

TimeDisplay.defaultProps = {
  start: undefined,
  end: undefined,
};

type IPropTypes = {
  start: number,
  end: number,
};

export default TimeDisplay;
