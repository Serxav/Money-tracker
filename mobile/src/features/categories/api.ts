import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { request } from '@/core/api/client';
import { categorySchema, type MovementType, pageSchema } from '@/core/api/schemas';

const categoryPage = pageSchema(categorySchema);

export const categoryKeys = { all: ['categories'] as const };

export function useCategories() {
  return useQuery({
    queryKey: categoryKeys.all,
    queryFn: ({ signal }) =>
      request('GET', '/v1/categories?limit=100', { schema: categoryPage, signal }),
    select: (page) => page.items,
  });
}

export function useCreateCategory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: { name: string; type: MovementType }) =>
      request('POST', '/v1/categories', { body: input, schema: categorySchema }),
    onSuccess: () => qc.invalidateQueries({ queryKey: categoryKeys.all }),
  });
}

export function useDeleteCategory() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => request('DELETE', `/v1/categories/${encodeURIComponent(id)}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: categoryKeys.all }),
  });
}
