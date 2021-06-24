import React from 'react';
import PropTypes from 'prop-types';
import { View } from 'react-native';
import styleContainer from 'styles/stylesheets/container';
import { BarChart } from 'react-native-chart-kit';
import { MajorityResult } from 'model/objects';

/**
 * Displays a simple bar chart
 */

const BarChartDisplay = (props: IPropTypes) => {
  const { data } = props;

  const data2 = {
    labels: data.map((d) => d.ballot_option),
    datasets: [
      {
        data: data.map((d) => d.count),
      },
    ],
  };

  const chartConfig = {
    backgroundGradientFrom: '#1E2923',
    backgroundGradientFromOpacity: 0,
    backgroundGradientTo: '#08130D',
    backgroundGradientToOpacity: 0.3,
    height: 5000,
    color: (opacity = 1) => `rgba(0, 0, 0, ${opacity})`,
    strokeWidth: 2, // optional, default 3
    decimalPlaces: 0,
    useShadowColorFromDataset: false, // optional
  };

  // Displays a Bar Chart
  return (
    <View style={[styleContainer.anchoredCenter, { padding: 10, justifyContent: 'flex-start' }]}>
      <BarChart data={data2} width={460} height={260} chartConfig={chartConfig} />
    </View>
  );
};

const propTypes = {
  data: PropTypes.arrayOf(PropTypes.number).isRequired,
};
BarChartDisplay.propTypes = propTypes;

type IPropTypes = {
  data: MajorityResult[],
};

export default BarChartDisplay;
