import { Platform } from 'react-native';

/**
 * URL base de la API por entorno. Se define con EXPO_PUBLIC_API_URL (ver .env.example).
 * Fuera de desarrollo solo se admite HTTPS.
 */
function resolveApiUrl(): string {
  const fromEnv = process.env.EXPO_PUBLIC_API_URL?.trim();
  const url =
    fromEnv && fromEnv.length > 0
      ? fromEnv
      : Platform.OS === 'android'
        ? 'http://10.0.2.2:8080'
        : 'http://localhost:8080';
  if (!__DEV__ && !url.startsWith('https://')) {
    throw new Error('EXPO_PUBLIC_API_URL debe usar https:// fuera de desarrollo');
  }
  return url.replace(/\/+$/, '');
}

export const config = {
  apiUrl: resolveApiUrl(),
  requestTimeoutMs: 15_000,
  maxGetRetries: 2,
} as const;
