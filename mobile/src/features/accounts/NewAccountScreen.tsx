import { router } from 'expo-router';
import { useState } from 'react';

import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { parseAmountToMinor } from '@/shared/money';
import { Button } from '@/shared/ui/Button';
import { Choice } from '@/shared/ui/Choice';
import { ErrorState, InlineError, Loading } from '@/shared/ui/Feedback';
import { Screen } from '@/shared/ui/Screen';
import { TextField } from '@/shared/ui/TextField';

import { useCreateAccount, useCurrencies } from './api';

export function NewAccountScreen() {
  const currencies = useCurrencies();
  const create = useCreateAccount();
  const [name, setName] = useState('');
  const [currency, setCurrency] = useState<string | null>('EUR');
  const [initial, setInitial] = useState('');
  const [errors, setErrors] = useState<{ name?: string; initial?: string }>({});

  if (currencies.isPending) return <Loading />;
  if (currencies.isError) {
    return <ErrorState message={errorMessage(currencies.error)} onRetry={() => currencies.refetch()} />;
  }
  const selected = currencies.data.find((c) => c.code === currency) ?? null;

  const submit = () => {
    const next: typeof errors = {};
    if (name.trim().length === 0) next.name = t.errors.required;
    const amount = initial.trim() === '' ? 0 : parseAmountToMinor(initial, selected?.fractionDigits ?? 2);
    if (amount === null) next.initial = t.errors.invalidAmount;
    setErrors(next);
    if (Object.keys(next).length > 0 || !selected || amount === null) return;
    create.mutate(
      { name: name.trim(), currency: selected.code, initialBalance: amount },
      { onSuccess: () => router.back() },
    );
  };

  return (
    <Screen>
      <TextField label={t.accounts.name} value={name} onChangeText={setName} maxLength={60} error={errors.name} />
      <Choice
        label={t.accounts.currency}
        options={currencies.data.map((c) => ({ value: c.code, label: c.code }))}
        value={currency}
        onChange={setCurrency}
      />
      <TextField
        label={`${t.accounts.initialBalance} (${selected?.code ?? ''})`}
        value={initial}
        onChangeText={setInitial}
        keyboardType="decimal-pad"
        placeholder={selected?.fractionDigits ? '0,00' : '0'}
        hint={t.accounts.currencyFixed}
        error={errors.initial}
      />
      <InlineError message={create.isError ? errorMessage(create.error) : null} />
      <Button label={t.common.save} onPress={submit} loading={create.isPending} />
    </Screen>
  );
}
