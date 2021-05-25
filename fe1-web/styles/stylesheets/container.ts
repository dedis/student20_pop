import { StyleSheet } from 'react-native';
import { Spacing } from 'styles';

const styles = StyleSheet.create({
  flex: {
    flex: 1,
    justifyContent: 'space-evenly',
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
  },
  anchoredCenter: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'space-evenly',
  },
  centerWithMargin: {
    flex: 1,
    alignItems: 'center',
    marginHorizontal: Spacing.xl,
    marginVertical: Spacing.xs,
  },
});

export default styles;
