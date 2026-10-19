import { useState } from 'react';
import { StyleSheet, Text } from 'react-native';

import { useSession } from '@/core/session/SessionProvider';
import { useCurrencies } from '@/features/accounts/api';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { Button } from '@/shared/ui/Button';
import { Choice } from '@/shared/ui/Choice';
import { InlineError } from '@/shared/ui/Feedback';
import { Screen } from '@/shared/ui/Screen';
import { TextField } from '@/shared/ui/TextField';
import { colors, spacing } from '@/shared/ui/theme';

import { useDeleteMe, useUpdateMe } from './api';

export function SettingsScreen() {
  const { user, setUser, signOut } = useSession();
  const currencies = useCurrencies();
  const update = useUpdateMe();
  const deleteMe = useDeleteMe();
  const [confirming, setConfirming] = useState(false);
  const [password, setPassword] = useState('');

  const changeCurrency = (code: string) =>
    update.mutate({ baseCurrency: code }, { onSuccess: (u) => setUser(u) });

  const deleteAccount = () =>
    deleteMe.mutate(password, {
      onSuccess: () => {
        setPassword('');
        signOut();
      },
    });

  return (
    <Screen>
      <Text style={styles.section} accessibilityRole="header">
        {t.settings.account}
      </Text>
      <Text style={styles.text}>{user?.email}</Text>
      {currencies.data ? (
        <Choice
          label={t.settings.baseCurrency}
          options={currencies.data.map((c) => ({ value: c.code, label: c.code }))}
          value={user?.baseCurrency ?? null}
          onChange={changeCurrency}
        />
      ) : null}
      <InlineError message={update.isError ? errorMessage(update.error) : null} />
      <Text style={styles.muted}>{t.settings.exportPending}</Text>
      <Button label={t.settings.logout} variant="secondary" onPress={signOut} />

      <Text style={styles.section} accessibilityRole="header">
        {t.settings.deleteAll}
      </Text>
      {confirming ? (
        <>
          <Text style={styles.text}>{t.settings.deleteAllMessage}</Text>
          <TextField
            label={t.auth.password}
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            autoComplete="current-password"
          />
          <InlineError message={deleteMe.isError ? errorMessage(deleteMe.error) : null} />
          <Button
            label={t.settings.deleteAll}
            variant="danger"
            onPress={deleteAccount}
            disabled={password.length === 0}
            loading={deleteMe.isPending}
          />
          <Button label={t.common.cancel} variant="secondary" onPress={() => setConfirming(false)} />
        </>
      ) : (
        <Button label={t.settings.deleteAll} variant="danger" onPress={() => setConfirming(true)} />
      )}
    </Screen>
  );
}

const styles = StyleSheet.create({
  section: { fontSize: 18, fontWeight: '600', color: colors.text, marginTop: spacing.l, marginBottom: spacing.s },
  text: { fontSize: 16, color: colors.text, marginBottom: spacing.m },
  muted: { fontSize: 14, color: colors.muted, marginVertical: spacing.s },
});
