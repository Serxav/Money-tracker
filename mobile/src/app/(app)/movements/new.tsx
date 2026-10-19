import { useLocalSearchParams } from 'expo-router';

import { MovementForm } from '@/features/transactions/MovementForm';

export default function NewMovementRoute() {
  const { accountId } = useLocalSearchParams<{ accountId?: string }>();
  return <MovementForm defaultAccountId={accountId} />;
}
