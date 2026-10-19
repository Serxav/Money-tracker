import { router } from 'expo-router';
import { useRef, useState } from 'react';
import { Alert } from 'react-native';

import { newIdempotencyKey } from '@/core/api/client';
import type { Account, Category, Movement, MovementType } from '@/core/api/schemas';
import { useAccounts } from '@/features/accounts/api';
import { useCategories } from '@/features/categories/api';
import { isValidIsoDate, todayIso } from '@/shared/dates';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { minorToInput, parseAmountToMinor } from '@/shared/money';
import { Button } from '@/shared/ui/Button';
import { Choice } from '@/shared/ui/Choice';
import { EmptyState, ErrorState, InlineError, Loading } from '@/shared/ui/Feedback';
import { Screen } from '@/shared/ui/Screen';
import { TextField } from '@/shared/ui/TextField';

import { type MovementInput, useCreateMovement, useDeleteMovement, useUpdateMovement } from './api';

interface Props {
  movement?: Movement;
  defaultAccountId?: string;
}

/** Alta y edición de movimientos. El importe se introduce en la divisa de la cuenta elegida. */
export function MovementForm({ movement, defaultAccountId }: Props) {
  const accounts = useAccounts();
  const categories = useCategories();
  if (accounts.isPending || categories.isPending) return <Loading />;
  if (accounts.isError || categories.isError) {
    const error = accounts.error ?? categories.error;
    return (
      <ErrorState
        message={errorMessage(error)}
        onRetry={() => {
          accounts.refetch();
          categories.refetch();
        }}
      />
    );
  }
  if (accounts.data.items.length === 0) return <EmptyState message={t.movements.noAccounts} />;
  return (
    <Form
      accounts={accounts.data.items}
      categories={categories.data}
      movement={movement}
      defaultAccountId={defaultAccountId}
    />
  );
}

function Form({
  accounts,
  categories,
  movement,
  defaultAccountId,
}: Props & { accounts: Account[]; categories: Category[] }) {
  const initialAccount =
    accounts.find((a) => a.id === (movement?.accountId ?? defaultAccountId)) ?? accounts[0];
  const [type, setType] = useState<MovementType>(movement?.type ?? 'EXPENSE');
  const [accountId, setAccountId] = useState(initialAccount.id);
  const [categoryId, setCategoryId] = useState<string | null>(movement?.categoryId ?? null);
  const [amount, setAmount] = useState(
    movement ? minorToInput(movement.amount, initialAccount.fractionDigits) : '',
  );
  const [date, setDate] = useState(movement?.date ?? todayIso());
  const [description, setDescription] = useState(movement?.description ?? '');
  const [errors, setErrors] = useState<{ amount?: string; date?: string; category?: string }>({});

  // Una clave por intento de alta: los reintentos de red no duplican el movimiento.
  const idempotencyKey = useRef(newIdempotencyKey());
  const create = useCreateMovement();
  const update = useUpdateMovement(movement?.id ?? '');
  const remove = useDeleteMovement();
  const mutation = movement ? update : create;

  const account = accounts.find((a) => a.id === accountId) ?? initialAccount;
  const typeCategories = categories.filter((c) => c.type === type);

  const submit = () => {
    const next: typeof errors = {};
    const minor = parseAmountToMinor(amount, account.fractionDigits);
    if (minor === null || minor <= 0) next.amount = t.errors.invalidAmount;
    if (!isValidIsoDate(date)) next.date = t.errors.invalidDate;
    if (!categoryId || !typeCategories.some((c) => c.id === categoryId)) next.category = t.errors.required;
    setErrors(next);
    if (Object.keys(next).length > 0 || minor === null || !categoryId) return;

    const input: MovementInput = {
      accountId,
      categoryId,
      type,
      amount: minor,
      date,
      description: description.trim() || undefined,
    };
    if (movement) {
      update.mutate(input, { onSuccess: () => router.back() });
    } else {
      create.mutate(
        { input, idempotencyKey: idempotencyKey.current },
        {
          onSuccess: () => {
            idempotencyKey.current = newIdempotencyKey();
            router.back();
          },
        },
      );
    }
  };

  const confirmDelete = () =>
    movement &&
    Alert.alert(t.common.confirmDeleteTitle, t.movements.deleteMessage, [
      { text: t.common.cancel, style: 'cancel' },
      {
        text: t.common.delete,
        style: 'destructive',
        onPress: () => remove.mutate(movement.id, { onSuccess: () => router.back() }),
      },
    ]);

  return (
    <Screen>
      <Choice
        label={t.movements.type}
        options={[
          { value: 'EXPENSE', label: t.common.expense },
          { value: 'INCOME', label: t.common.income },
        ]}
        value={type}
        onChange={(v) => {
          setType(v);
          setCategoryId(null);
        }}
      />
      <Choice
        label={t.movements.account}
        options={accounts.map((a) => ({ value: a.id, label: `${a.name} (${a.currency})` }))}
        value={accountId}
        onChange={setAccountId}
      />
      <Choice
        label={t.movements.category}
        options={typeCategories.map((c) => ({ value: c.id, label: c.name }))}
        value={categoryId}
        onChange={setCategoryId}
      />
      <InlineError message={errors.category ?? null} />
      <TextField
        label={`${t.movements.amount} (${account.currency})`}
        value={amount}
        onChangeText={setAmount}
        keyboardType="decimal-pad"
        placeholder={account.fractionDigits > 0 ? '0,' + '0'.repeat(account.fractionDigits) : '0'}
        error={errors.amount}
      />
      <TextField
        label={t.movements.date}
        value={date}
        onChangeText={setDate}
        keyboardType="numbers-and-punctuation"
        maxLength={10}
        error={errors.date}
      />
      <TextField
        label={t.movements.description}
        value={description}
        onChangeText={setDescription}
        maxLength={255}
        hint={t.movements.descriptionHint}
      />
      <InlineError message={mutation.isError ? errorMessage(mutation.error) : null} />
      <Button label={t.common.save} onPress={submit} loading={mutation.isPending} />
      {movement ? (
        <>
          <InlineError message={remove.isError ? errorMessage(remove.error) : null} />
          <Button label={t.common.delete} variant="danger" onPress={confirmDelete} loading={remove.isPending} />
        </>
      ) : null}
    </Screen>
  );
}
