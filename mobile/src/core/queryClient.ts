import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      // El cliente HTTP ya reintenta los errores transitorios; aquí no se reintenta otra vez.
      retry: false,
    },
    mutations: {
      retry: false,
    },
  },
});
