import { router, Stack } from 'expo-router';
import { Alert, FlatList, Pressable, StyleSheet, Text, View } from 'react-native';

import type { Movement } from '@/core/api/schemas';
import { useCategories } from '@/features/categories/api';
import { useAccountMovements } from '@/features/transactions/api';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { formatMoney } from '@/shared/money';
import { Button } from '@/shared/ui/Button';
import { EmptyState, ErrorState, InlineError, Loading } from '@/shared/ui/Feedback';
import { colors, minTouch, spacing } from '@/shared/ui/theme';

import { useAccount, useDeleteAccount } from './api';

export function AccountScreen({ id }: { id: string }) {
  const account = useAccount(id);
  const movements = useAccountMovements(id);
  const categories = useCategories();
  const remove = useDeleteAccount();

  if (account.isPending) return <Loading />;
  if (account.isError) {
    return <ErrorState message={errorMessage(account.error)} onRetry={() => account.refetch()} />;
  }
  const a = account.data;
  const categoryName = (m: Movement) => categories.data?.find((c) => c.id === m.categoryId)?.name ?? '';
  const items = movements.data?.pages.flatMap((p) => p.items) ?? [];

  const confirmDelete = () =>
    Alert.alert(t.accounts.deleteAccount, t.accounts.deleteAccountMessage, [
      { text: t.common.cancel, style: 'cancel' },
      {
        text: t.common.delete,
        style: 'destructive',
        onPress: () => remove.mutate(id, { onSuccess: () => router.back() }),
      },
    ]);

  return (
    <View style={styles.container}>
      <Stack.Screen options={{ title: a.name }} />
      <Text style={styles.label}>{t.accounts.balance}</Text>
      <Text style={[styles.balance, a.balance < 0 && styles.negative]} accessibilityRole="header">
        {formatMoney(a.balance, a.currency, a.fractionDigits)}
      </Text>
      <Button
        label={t.home.newMovement}
        onPress={() => router.push({ pathname: '/movements/new', params: { accountId: id } })}
      />
      <Text style={styles.section} accessibilityRole="header">
        {t.accounts.movements}
      </Text>
      {movements.isError ? (
        <ErrorState message={errorMessage(movements.error)} onRetry={() => movements.refetch()} />
      ) : (
        <FlatList
          data={items}
          keyExtractor={(m) => m.id}
          ListEmptyComponent={movements.isPending ? <Loading /> : <EmptyState message={t.accounts.noMovements} />}
          onEndReached={() => movements.hasNextPage && !movements.isFetchingNextPage && movements.fetchNextPage()}
          renderItem={({ item }) => {
            const signed = item.type === 'EXPENSE' ? -item.amount : item.amount;
            const amount = formatMoney(signed, item.currency, a.fractionDigits);
            return (
              <Pressable
                accessibilityRole="button"
                accessibilityLabel={`${item.date}, ${categoryName(item)}, ${amount}${item.description ? ', ' + item.description : ''}`}
                onPress={() => router.push({ pathname: '/movements/[id]', params: { id: item.id } })}
                style={styles.row}>
                <View style={styles.rowText}>
                  <Text style={styles.rowTitle}>{item.description || categoryName(item)}</Text>
                  <Text style={styles.rowMeta}>
                    {item.date} · {categoryName(item)}
                  </Text>
                </View>
                {/* El signo y la palabra no dependen solo del color. */}
                <Text style={[styles.amount, item.type === 'EXPENSE' ? styles.negative : styles.positive]}>
                  {amount}
                </Text>
              </Pressable>
            );
          }}
          ListFooterComponent={
            <View>
              <InlineError message={remove.isError ? errorMessage(remove.error) : null} />
              <Button label={t.accounts.deleteAccount} variant="secondary" onPress={confirmDelete} loading={remove.isPending} />
            </View>
          }
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.m },
  label: { color: colors.muted, fontSize: 14 },
  balance: { fontSize: 32, fontWeight: '700', color: colors.text, marginBottom: spacing.m, fontVariant: ['tabular-nums'] },
  section: { fontSize: 18, fontWeight: '600', color: colors.text, marginTop: spacing.l, marginBottom: spacing.s },
  row: {
    minHeight: minTouch,
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.s,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: colors.border,
  },
  rowText: { flex: 1, paddingRight: spacing.s },
  rowTitle: { fontSize: 16, color: colors.text },
  rowMeta: { fontSize: 13, color: colors.muted },
  amount: { fontSize: 16, fontVariant: ['tabular-nums'] },
  negative: { color: colors.expense },
  positive: { color: colors.income },
});
