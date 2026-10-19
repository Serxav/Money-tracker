import * as Crypto from 'expo-crypto';
import type { z } from 'zod';

import { config } from '@/core/config';
import { tokenStore } from '@/core/session/tokenStore';

import { ApiError, NetworkError } from './errors';
import { apiErrorBodySchema, authResponseSchema, type AuthResponse } from './schemas';

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestOptions<T> {
  body?: unknown;
  schema?: z.ZodType<T>;
  idempotencyKey?: string;
  /** false para las rutas de autenticación, que no llevan access token. */
  auth?: boolean;
  signal?: AbortSignal;
}

type SessionListener = (auth: AuthResponse | null) => void;
let onSessionChange: SessionListener = () => {};

/** El proveedor de sesión se suscribe para enterarse de refrescos y cierres forzados. */
export function setSessionListener(listener: SessionListener) {
  onSessionChange = listener;
}

let refreshInFlight: Promise<boolean> | null = null;

/** Renueva la sesión una sola vez aunque fallen varias peticiones a la vez. */
export function refreshSession(): Promise<boolean> {
  refreshInFlight ??= (async () => {
    try {
      const refreshToken = await tokenStore.getRefreshToken();
      if (!refreshToken) {
        return false;
      }
      const auth = await send('POST', '/v1/auth/refresh', {
        body: { refreshToken },
        schema: authResponseSchema,
        auth: false,
      });
      await storeSession(auth);
      return true;
    } catch (error) {
      if (error instanceof ApiError) {
        await tokenStore.clear();
        onSessionChange(null);
      }
      return false;
    } finally {
      refreshInFlight = null;
    }
  })();
  return refreshInFlight;
}

export async function storeSession(auth: AuthResponse) {
  tokenStore.setAccessToken(auth.accessToken);
  await tokenStore.setRefreshToken(auth.refreshToken);
  onSessionChange(auth);
}

/**
 * Petición a la API: timeout, X-Request-Id, token, validación de la respuesta con zod,
 * reintentos con espera progresiva solo en lecturas o creaciones idempotentes, y un único
 * intento de renovar la sesión ante un 401.
 */
export async function request<T = void>(
  method: Method,
  path: string,
  options: RequestOptions<T> = {},
): Promise<T> {
  try {
    return await withRetries(method, path, options);
  } catch (error) {
    const authRequired = options.auth !== false;
    if (authRequired && error instanceof ApiError && error.status === 401) {
      if (await refreshSession()) {
        return withRetries(method, path, options);
      }
    }
    throw error;
  }
}

async function withRetries<T>(method: Method, path: string, options: RequestOptions<T>) {
  const retryable = method === 'GET' || options.idempotencyKey !== undefined;
  const attempts = retryable ? config.maxGetRetries + 1 : 1;
  let lastError: unknown;
  for (let attempt = 0; attempt < attempts; attempt++) {
    try {
      return await send(method, path, options);
    } catch (error) {
      lastError = error;
      const transient =
        (error instanceof NetworkError && error.kind !== 'invalid-response') ||
        (error instanceof ApiError && error.status >= 500);
      if (!transient || attempt === attempts - 1) {
        throw error;
      }
      await delay(300 * 2 ** attempt);
    }
  }
  throw lastError;
}

async function send<T>(method: Method, path: string, options: RequestOptions<T>): Promise<T> {
  const headers: Record<string, string> = {
    Accept: 'application/json',
    'X-Request-Id': Crypto.randomUUID(),
  };
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (options.idempotencyKey) {
    headers['Idempotency-Key'] = options.idempotencyKey;
  }
  const token = tokenStore.getAccessToken();
  if (options.auth !== false && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), config.requestTimeoutMs);
  options.signal?.addEventListener('abort', () => controller.abort());

  let response: Response;
  try {
    response = await fetch(`${config.apiUrl}${path}`, {
      method,
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
      signal: controller.signal,
    });
  } catch {
    if (options.signal?.aborted) {
      throw new NetworkError('timeout', 'Petición cancelada');
    }
    throw controller.signal.aborted
      ? new NetworkError('timeout', 'El servidor tarda demasiado en responder')
      : new NetworkError('offline', 'Sin conexión con el servidor');
  } finally {
    clearTimeout(timeout);
  }

  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  const json = parseJson(text);

  if (!response.ok) {
    const parsed = apiErrorBodySchema.safeParse(json);
    if (parsed.success) {
      const e = parsed.data;
      throw new ApiError(response.status, e.code, e.message, e.requestId, e.details ?? []);
    }
    throw new ApiError(response.status, 'UNKNOWN', 'Error inesperado del servidor');
  }
  if (!options.schema) {
    return undefined as T;
  }
  const parsed = options.schema.safeParse(json);
  if (!parsed.success) {
    throw new NetworkError('invalid-response', 'Respuesta del servidor con formato inesperado');
  }
  return parsed.data;
}

function parseJson(text: string): unknown {
  if (!text) {
    return undefined;
  }
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
}

function delay(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function newIdempotencyKey(): string {
  return Crypto.randomUUID();
}
