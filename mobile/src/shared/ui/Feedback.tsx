import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { t } from '@/shared/i18n/es';

import { Button } from './Button';
import { colors, spacing } from './theme';

export function Loading() {
  return (
    <View style={styles.center} accessibilityLabel={t.common.loading}>
      <ActivityIndicator size="large" color={colors.primary} />
    </View>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <View style={styles.center}>
      <Text accessibilityRole="alert" style={styles.error}>
        {message}
      </Text>
      {onRetry ? <Button label={t.common.retry} variant="secondary" onPress={onRetry} /> : null}
    </View>
  );
}

export function EmptyState({ message }: { message: string }) {
  return (
    <View style={styles.center}>
      <Text style={styles.muted}>{message}</Text>
    </View>
  );
}

export function InlineError({ message }: { message: string | null }) {
  if (!message) {
    return null;
  }
  return (
    <Text accessibilityRole="alert" accessibilityLiveRegion="polite" style={styles.inline}>
      {message}
    </Text>
  );
}

const styles = StyleSheet.create({
  center: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: spacing.l },
  error: { color: colors.danger, fontSize: 16, textAlign: 'center', marginBottom: spacing.m },
  muted: { color: colors.muted, fontSize: 16, textAlign: 'center' },
  inline: { color: colors.danger, fontSize: 15, marginVertical: spacing.s },
});
