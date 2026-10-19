import { z } from 'zod';

import { request, setSessionListener } from '../client';
import { ApiError, NetworkError } from '../errors';

jest.mock('expo-crypto', () => ({ randomUUID: () => '00000000-0000-4000-8000-000000000000' }));

const store = { access: null as string | null, refresh: null as string | null };
jest.mock('@/core/session/tokenStore', () => ({
  tokenStore: {
    getAccessToken: () => store.access,
    setAccessToken: (t: string | null) => {
      store.access = t;
    },
    getRefreshToken: async () => store.refresh,
    setRefreshToken: async (t: string) => {
      store.refresh = t;
    },
    clear: async () => {
      store.access = null;
      store.refresh = null;
    },
  },
}));

const user = {
  id: '11111111-1111-4111-8111-111111111111',
  email: 'a@example.com',
  baseCurrency: 'EUR',
  locale: 'es',
  createdAt: '2026-10-19T10:00:00Z',
};

function json(status: number, body: unknown) {
  return Promise.resolve({
    status,
    ok: status >= 200 && status < 300,
    text: () => Promise.resolve(body === undefined ? '' : JSON.stringify(body)),
  } as Response);
}

const fetchMock = jest.fn();
globalThis.fetch = fetchMock as unknown as typeof fetch;

beforeEach(() => {
  fetchMock.mockReset();
  store.access = 'access-1';
  store.refresh = 'refresh-1';
  setSessionListener(() => {});
});

const schema = z.object({ ok: z.boolean() });

it('envía token, X-Request-Id y valida la respuesta', async () => {
  fetchMock.mockReturnValueOnce(json(200, { ok: true }));
  await expect(request('GET', '/v1/x', { schema })).resolves.toEqual({ ok: true });
  const [, init] = fetchMock.mock.calls[0];
  expect(init.headers.Authorization).toBe('Bearer access-1');
  expect(init.headers['X-Request-Id']).toBeDefined();
});

it('convierte el error estándar de la API en ApiError', async () => {
  fetchMock.mockReturnValueOnce(json(409, { code: 'ACCOUNT_NAME_TAKEN', message: 'x', requestId: 'r1' }));
  await expect(request('POST', '/v1/accounts', { body: {} })).rejects.toMatchObject({
    status: 409,
    code: 'ACCOUNT_NAME_TAKEN',
    requestId: 'r1',
  });
});

it('rechaza respuestas con formato inesperado', async () => {
  fetchMock.mockReturnValueOnce(json(200, { ok: 'sí' }));
  await expect(request('GET', '/v1/x', { schema })).rejects.toBeInstanceOf(NetworkError);
});

it('renueva la sesión una vez ante un 401 y repite la petición', async () => {
  const listener = jest.fn();
  setSessionListener(listener);
  fetchMock
    .mockReturnValueOnce(json(401, { code: 'UNAUTHORIZED', message: 'x' }))
    .mockReturnValueOnce(
      json(200, { accessToken: 'access-2', tokenType: 'Bearer', expiresIn: 900, refreshToken: 'refresh-2', user }),
    )
    .mockReturnValueOnce(json(200, { ok: true }));

  await expect(request('GET', '/v1/x', { schema })).resolves.toEqual({ ok: true });
  expect(store.access).toBe('access-2');
  expect(store.refresh).toBe('refresh-2');
  expect(fetchMock.mock.calls[2][1].headers.Authorization).toBe('Bearer access-2');
  expect(listener).toHaveBeenCalledWith(expect.objectContaining({ accessToken: 'access-2' }));
});

it('cierra la sesión si el refresh token ya no es válido', async () => {
  const listener = jest.fn();
  setSessionListener(listener);
  fetchMock
    .mockReturnValueOnce(json(401, { code: 'UNAUTHORIZED', message: 'x' }))
    .mockReturnValueOnce(json(401, { code: 'INVALID_SESSION', message: 'x' }));

  await expect(request('GET', '/v1/x', { schema })).rejects.toBeInstanceOf(ApiError);
  expect(store.refresh).toBeNull();
  expect(listener).toHaveBeenCalledWith(null);
});

it('no reintenta un POST sin Idempotency-Key si falla la red', async () => {
  fetchMock.mockRejectedValueOnce(new TypeError('Network request failed'));
  await expect(request('POST', '/v1/accounts', { body: {} })).rejects.toMatchObject({ kind: 'offline' });
  expect(fetchMock).toHaveBeenCalledTimes(1);
});

it('reintenta un POST con Idempotency-Key reutilizando la misma clave', async () => {
  fetchMock
    .mockRejectedValueOnce(new TypeError('Network request failed'))
    .mockReturnValueOnce(json(201, { ok: true }));
  await expect(
    request('POST', '/v1/transactions', { body: {}, schema, idempotencyKey: 'key-123456' }),
  ).resolves.toEqual({ ok: true });
  expect(fetchMock).toHaveBeenCalledTimes(2);
  expect(fetchMock.mock.calls[0][1].headers['Idempotency-Key']).toBe('key-123456');
  expect(fetchMock.mock.calls[1][1].headers['Idempotency-Key']).toBe('key-123456');
});
