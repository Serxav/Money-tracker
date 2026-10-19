import { Link } from 'expo-router';
import { useState } from 'react';
import { StyleSheet, Text } from 'react-native';

import { useSession } from '@/core/session/SessionProvider';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { Button } from '@/shared/ui/Button';
import { InlineError } from '@/shared/ui/Feedback';
import { Screen } from '@/shared/ui/Screen';
import { TextField } from '@/shared/ui/TextField';
import { colors, spacing } from '@/shared/ui/theme';

const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Login y registro comparten formulario. La validación de la app es solo de ayuda (UX). */
export function AuthForm({ mode }: { mode: 'login' | 'register' }) {
  const { signIn, register } = useSession();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});

  const submit = async () => {
    const errors: typeof fieldErrors = {};
    if (!EMAIL.test(email.trim())) errors.email = 'Email no válido.';
    if (mode === 'register' && password.length < 10) errors.password = t.auth.passwordHint;
    if (mode === 'login' && password.length === 0) errors.password = t.errors.required;
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) return;

    setSubmitting(true);
    setError(null);
    try {
      if (mode === 'login') {
        await signIn(email.trim(), password);
      } else {
        await register(email.trim(), password);
      }
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Screen>
      <Text accessibilityRole="header" style={styles.title}>
        {t.appName}
      </Text>
      <TextField
        label={t.auth.email}
        value={email}
        onChangeText={setEmail}
        autoCapitalize="none"
        autoComplete="email"
        keyboardType="email-address"
        textContentType="emailAddress"
        error={fieldErrors.email}
      />
      <TextField
        label={t.auth.password}
        value={password}
        onChangeText={setPassword}
        secureTextEntry
        autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
        textContentType={mode === 'login' ? 'password' : 'newPassword'}
        hint={mode === 'register' ? t.auth.passwordHint : undefined}
        error={fieldErrors.password}
      />
      <InlineError message={error} />
      <Button
        label={mode === 'login' ? t.auth.login : t.auth.register}
        onPress={submit}
        loading={submitting}
      />
      {mode === 'register' ? <Text style={styles.note}>{t.auth.privacyNote}</Text> : null}
      <Link href={mode === 'login' ? '/register' : '/login'} style={styles.link} accessibilityRole="link">
        {mode === 'login' ? t.auth.goToRegister : t.auth.goToLogin}
      </Link>
    </Screen>
  );
}

const styles = StyleSheet.create({
  title: { fontSize: 28, fontWeight: '700', color: colors.text, marginVertical: spacing.l },
  note: { color: colors.muted, fontSize: 13, marginTop: spacing.s },
  link: { color: colors.primary, fontSize: 16, paddingVertical: spacing.m, textAlign: 'center' },
});
