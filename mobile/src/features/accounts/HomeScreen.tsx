import { Link, router } from 'expo-router';
import { FlatList, Pressable, RefreshControl, StyleSheet, Text, View } from 'react-native';

import { useSession } from '@/core/session/SessionProvider';
import { errorMessage } from '@/shared/errorMessage';
import { t } from '@/shared/i18n/es';
import { formatMoney } from '@/shared/money';
import { Button } from '@/shared/ui/Button';
import { EmptyState, ErrorState, Loading } from '@/shared/ui/Feedback';
import { colors, minTouch, spacing } from '@/shared/ui/theme';

import { useAccounts } from './api';

/** Inicio: cuentas con su saldo, en la divisa de cada una (sin convertir hasta la 0.2). */
export function HomeScreen() {
  const { user } = useSession();
  const accounts = useAccounts();

  if (accounts.isPending) return <Loading />;
  if (accounts.isError) {
    return <ErrorState message={errorMessage(accounts.error)} onRetry={() => accounts.refetch()} />;
  }

  const items = accounts.data.items;
  return (
    <View style={styles.container}>
      <FlatList
        data={items}
        keyExtractor={(a) => a.id}
        refreshControl={<RefreshControl refreshing={accounts.isRefetching} onRefresh={() => void accounts.refetch()} />}
        ListHeaderComponent={
          <Text style={styles.subtitle} accessibilityRole="text">
            {user?.email}
          </Text>
        }
        ListEmptyComponent={<EmptyState message={t.home.empty} />}
        renderItem={({ item }) => (
          <Pressable
            accessibilityRole="button"
            accessibilityLabel={`${item.name}, ${t.accounts.balance} ${formatMoney(item.balance, item.currency, item.fractionDigits)}`}
            onPress={() => router.push({ pathname: '/accounts/[id]', params: { id: item.id } })}
            style={styles.card}>
            <Text style={styles.name}>{item.name}</Text>
            <Text style={[styles.balance, item.balance < 0 && styles.negative]}>
              {formatMoney(item.balance, item.currency, item.fractionDigits)}
            </Text>
          </Pressable>
        )}
      />
      <View style={styles.actions}>
        {items.length > 0 ? (
          <Button label={t.home.newMovement} onPress={() => router.push('/movements/new')} />
        ) : null}
        <Button label={t.home.newAccount} variant="secondary" onPress={() => router.push('/accounts/new')} />
        <View style={styles.links}>
          <Link href="/categories" style={styles.link} accessibilityRole="link">
            {t.home.categories}
          </Link>
          <Link href="/settings" style={styles.link} accessibilityRole="link">
            {t.home.settings}
          </Link>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: spacing.m },
  subtitle: { color: colors.muted, marginBottom: spacing.m },
  card: {
    minHeight: minTouch,
    padding: spacing.m,
    borderRadius: 12,
    backgroundColor: colors.surface,
    marginBottom: spacing.s,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  name: { fontSize: 17, color: colors.text, fontWeight: '600', flexShrink: 1 },
  balance: { fontSize: 17, color: colors.text, fontVariant: ['tabular-nums'] },
  negative: { color: colors.expense },
  actions: { paddingTop: spacing.s },
  links: { flexDirection: 'row', justifyContent: 'space-around' },
  link: { color: colors.primary, fontSize: 16, padding: spacing.m },
});
