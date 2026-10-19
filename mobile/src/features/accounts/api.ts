import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { request } from '@/core/api/client';
import { accountSchema, currencySchema, pageSchema } from '@/core/api/schemas';

const accountPage = pageSchema(accountSchema);

export const accountKeys = {
  all: ['accounts'] as const,
  detail: (id: string) => ['accounts', id] as const,
};

export function useAccounts() {
  return useQuery({
    queryKey: accountKeys.all,
    queryFn: ({ signal }) => request('GET', '/v1/accounts?limit=100', { schema: accountPage, signal }),
  });
}

export function useAccount(id: string) {
  return useQuery({
    queryKey: accountKeys.detail(id),
    queryFn: ({ signal }) =>
      request('GET', `/v1/accounts/${encodeURIComponent(id)}`, { schema: accountSchema, signal }),
  });
}

export function useCurrencies() {
  return useQuery({
    queryKey: ['currencies'],
    staleTime: Infinity,
    queryFn: ({ signal }) => request('GET', '/v1/currencies', { schema: currencySchema.array(), signal }),
  });
}

export function useCreateAccount() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: { name: string; currency: string; initialBalance: number }) =>
      request('POST', '/v1/accounts', { body: input, schema: accountSchema }),
    onSuccess: () => qc.invalidateQueries({ queryKey: accountKeys.all }),
  });
}

export function useDeleteAccount() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => request('DELETE', `/v1/accounts/${encodeURIComponent(id)}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: accountKeys.all }),
  });
}
