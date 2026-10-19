import { Stack } from 'expo-router';

import { t } from '@/shared/i18n/es';

export default function AppLayout() {
  return (
    <Stack>
      <Stack.Screen name="index" options={{ title: t.home.title }} />
      <Stack.Screen name="accounts/new" options={{ title: t.home.newAccount }} />
      <Stack.Screen name="accounts/[id]" options={{ title: '' }} />
      <Stack.Screen name="movements/new" options={{ title: t.movements.newTitle }} />
      <Stack.Screen name="movements/[id]" options={{ title: t.movements.editTitle }} />
      <Stack.Screen name="categories" options={{ title: t.categories.title }} />
      <Stack.Screen name="settings" options={{ title: t.settings.title }} />
    </Stack>
  );
}
