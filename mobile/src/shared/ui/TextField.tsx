import { StyleSheet, Text, TextInput, type TextInputProps, View } from 'react-native';

import { colors, minTouch, spacing } from './theme';

interface Props extends Omit<TextInputProps, 'style'> {
  label: string;
  error?: string | null;
  hint?: string;
}

/** Campo con etiqueta visible y error anunciado por el lector de pantalla. */
export function TextField({ label, error, hint, ...input }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        accessibilityLabel={label}
        accessibilityHint={hint}
        placeholderTextColor={colors.muted}
        style={[styles.input, error ? styles.inputError : null]}
        {...input}
      />
      {hint && !error ? <Text style={styles.hint}>{hint}</Text> : null}
      {error ? (
        <Text accessibilityRole="alert" accessibilityLiveRegion="polite" style={styles.error}>
          {error}
        </Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { marginBottom: spacing.m },
  label: { color: colors.text, fontSize: 15, fontWeight: '600', marginBottom: spacing.xs },
  input: {
    minHeight: minTouch,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 8,
    paddingHorizontal: spacing.m,
    color: colors.text,
    fontSize: 16,
    backgroundColor: colors.background,
  },
  inputError: { borderColor: colors.danger },
  hint: { color: colors.muted, fontSize: 13, marginTop: spacing.xs },
  error: { color: colors.danger, fontSize: 14, marginTop: spacing.xs },
});
