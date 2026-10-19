import { useLocalSearchParams } from 'expo-router';

import { EditMovementScreen } from '@/features/transactions/EditMovementScreen';

export default function EditMovementRoute() {
  const { id } = useLocalSearchParams<{ id: string }>();
  return <EditMovementScreen id={id} />;
}
