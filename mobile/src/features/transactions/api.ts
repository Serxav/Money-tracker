import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { request } from '@/core/api/client';
import { type MovementType, movementSchema, pageSchema } from '@/core/api/schemas';
import { accountKeys } from '@/features/accounts/api';

const movementPage = pageSchema(movementSchema);
const PAGE_SIZE = 20;

export interface MovementInput {
  accountId: string;
  categoryId: string;
  type: MovementType;
  amount: number;
  date: string;
  description?: string;
}

export const movementKeys = {
  all: ['movements'] as const,
  byAccount: (accountId: string) => ['movements', 'account', accountId] as const,
  detail: (id: string) => ['movements', id] as const,
};

/** Movimientos de una cuenta, paginados (más recientes primero). */
export function useAccountMovements(accountId: string) {
  return useInfiniteQuery({
    queryKey: movementKeys.byAccount(accountId),
    initialPageParam: 0,
    queryFn: ({ pageParam, signal }) =>
      request(
        'GET',
        `/v1/transactions?accountId=${encodeURIComponent(accountId)}&page=${pageParam}&limit=${PAGE_SIZE}`,
        { schema: movementPage, signal },
      ),
    getNextPageParam: (last) =>
      (last.page + 1) * last.limit < last.total ? last.page + 1 : undefined,
  });
}

export function useMovement(id: string) {
  return useQuery({
    queryKey: movementKeys.detail(id),
    queryFn: ({ signal }) =>
      request('GET', `/v1/transactions/${encodeURIComponent(id)}`, { schema: movementSchema, signal }),
  });
}

function useInvalidateMoney() {
  const qc = useQueryClient();
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: movementKeys.all }),
      qc.invalidateQueries({ queryKey: accountKeys.all }),
    ]);
}

/** La misma Idempotency-Key en los reintentos evita crear el movimiento dos veces. */
export function useCreateMovement() {
  const invalidate = useInvalidateMoney();
  return useMutation({
    mutationFn: ({ input, idempotencyKey }: { input: MovementInput; idempotencyKey: string }) =>
      request('POST', '/v1/transactions', { body: input, schema: movementSchema, idempotencyKey }),
    onSuccess: invalidate,
  });
}

export function useUpdateMovement(id: string) {
  const invalidate = useInvalidateMoney();
  return useMutation({
    mutationFn: (input: MovementInput) =>
      request('PUT', `/v1/transactions/${encodeURIComponent(id)}`, { body: input, schema: movementSchema }),
    onSuccess: invalidate,
  });
}

export function useDeleteMovement() {
  const invalidate = useInvalidateMoney();
  return useMutation({
    mutationFn: (id: string) => request('DELETE', `/v1/transactions/${encodeURIComponent(id)}`),
    onSuccess: invalidate,
  });
}
