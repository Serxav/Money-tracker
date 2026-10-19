import { errorMessage } from '@/shared/errorMessage';
import { ErrorState, Loading } from '@/shared/ui/Feedback';

import { useMovement } from './api';
import { MovementForm } from './MovementForm';

export function EditMovementScreen({ id }: { id: string }) {
  const movement = useMovement(id);
  if (movement.isPending) return <Loading />;
  if (movement.isError) {
    return <ErrorState message={errorMessage(movement.error)} onRetry={() => movement.refetch()} />;
  }
  return <MovementForm movement={movement.data} />;
}
