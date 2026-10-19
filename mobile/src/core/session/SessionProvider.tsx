import { createContext, type ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';

import { refreshSession, request, setSessionListener, storeSession } from '@/core/api/client';
import { authResponseSchema, type User } from '@/core/api/schemas';
import { queryClient } from '@/core/queryClient';

import { tokenStore } from './tokenStore';

type Status = 'loading' | 'signedOut' | 'signedIn';

interface SessionValue {
  status: Status;
  user: User | null;
  signIn: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  setUser: (user: User) => void;
}

const SessionContext = createContext<SessionValue | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>('loading');
  const [user, setUserState] = useState<User | null>(null);

  useEffect(() => {
    setSessionListener((auth) => {
      if (auth) {
        setUserState(auth.user);
        setStatus('signedIn');
      } else {
        setUserState(null);
        setStatus('signedOut');
        queryClient.clear();
      }
    });
    // Restaurar la sesión guardada en el almacenamiento seguro.
    refreshSession().then((ok) => {
      if (!ok) {
        setStatus((s) => (s === 'loading' ? 'signedOut' : s));
      }
    });
    return () => setSessionListener(() => {});
  }, []);

  const signIn = useCallback(async (email: string, password: string) => {
    const auth = await request('POST', '/v1/auth/login', {
      body: { email, password },
      schema: authResponseSchema,
      auth: false,
    });
    await storeSession(auth);
  }, []);

  const register = useCallback(async (email: string, password: string) => {
    const auth = await request('POST', '/v1/auth/register', {
      body: { email, password },
      schema: authResponseSchema,
      auth: false,
    });
    await storeSession(auth);
  }, []);

  const signOut = useCallback(async () => {
    const refreshToken = await tokenStore.getRefreshToken();
    if (refreshToken) {
      // Mejor esfuerzo: si no hay conexión, la sesión local se borra igualmente.
      await request('POST', '/v1/auth/logout', { body: { refreshToken }, auth: false }).catch(() => {});
    }
    await tokenStore.clear();
    queryClient.clear();
    setUserState(null);
    setStatus('signedOut');
  }, []);

  const value = useMemo(
    () => ({ status, user, signIn, register, signOut, setUser: setUserState }),
    [status, user, signIn, register, signOut],
  );
  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionValue {
  const value = useContext(SessionContext);
  if (!value) {
    throw new Error('useSession debe usarse dentro de SessionProvider');
  }
  return value;
}
