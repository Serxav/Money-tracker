import { useMutation } from '@tanstack/react-query';

import { request } from '@/core/api/client';
import { userSchema } from '@/core/api/schemas';

export function useUpdateMe() {
  return useMutation({
    mutationFn: (input: { baseCurrency?: string; locale?: string }) =>
      request('PATCH', '/v1/me', { body: input, schema: userSchema }),
  });
}

export function useDeleteMe() {
  return useMutation({
    mutationFn: (password: string) => request('DELETE', '/v1/me', { body: { password } }),
  });
}
