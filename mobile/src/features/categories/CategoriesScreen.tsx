import { useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';

import type { MovementType } from '@/core/api/schemas';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { Button } from '@/shared/ui/Button';
import { Choice } from '@/shared/ui/Choice';
import { ErrorState, InlineError, Loading } from '@/shared/ui/Feedback';
import { Screen } from '@/shared/ui/Screen';
import { TextField } from '@/shared/ui/TextField';
import { colors, minTouch, spacing } from '@/shared/ui/theme';

import { useCategories, useCreateCategory, useDeleteCategory } from './api';

export function CategoriesScreen() {
  const categories = useCategories();
  const create = useCreateCategory();
  const remove = useDeleteCategory();
  const [type, setType] = useState<MovementType>('EXPENSE');
  const [name, setName] = useState('');

  if (categories.isPending) return <Loading />;
  if (categories.isError) {
    return <ErrorState message={errorMessage(categories.error)} onRetry={() => categories.refetch()} />;
  }

  const add = () => {
    if (name.trim().length === 0) return;
    create.mutate({ name: name.trim(), type }, { onSuccess: () => setName('') });
  };

  const confirmDelete = (id: string, label: string) =>
    Alert.alert(label, t.categories.deleteMessage, [
      { text: t.common.cancel, style: 'cancel' },
      { text: t.common.delete, style: 'destructive', onPress: () => remove.mutate(id) },
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
        onChange={setType}
      />
      {categories.data
        .filter((c) => c.type === type)
        .map((c) => (
          <View key={c.id} style={styles.row}>
            <Text style={styles.name}>{c.name}</Text>
            <Pressable
              accessibilityRole="button"
              accessibilityLabel={`${t.common.delete} ${c.name}`}
              onPress={() => confirmDelete(c.id, c.name)}
              style={styles.delete}>
              <Text style={styles.deleteText}>{t.common.delete}</Text>
            </Pressable>
          </View>
        ))}
      <InlineError message={remove.isError ? errorMessage(remove.error) : null} />
      <TextField label={t.categories.newName} value={name} onChangeText={setName} maxLength={40} />
      <InlineError message={create.isError ? errorMessage(create.error) : null} />
      <Button label={t.categories.add} onPress={add} loading={create.isPending} />
    </Screen>
  );
}

const styles = StyleSheet.create({
  row: {
    minHeight: minTouch,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.border,
  },
  name: { fontSize: 16, color: colors.text },
  delete: { minHeight: minTouch, minWidth: minTouch, justifyContent: 'center', paddingHorizontal: spacing.s },
  deleteText: { color: colors.danger, fontSize: 15 },
});
