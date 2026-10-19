import { ApiError, NetworkError } from '@/core/api/errors';
import { t } from '@/shared/i18n/es';

/** Mensajes para el usuario según el código de error de la API (nunca detalles internos). */
const byCode: Record<string, string> = {
  INVALID_CREDENTIALS: 'Email o contraseña incorrectos.',
  EMAIL_UNAVAILABLE: 'No se puede registrar con ese email.',
  WEAK_PASSWORD: 'La contraseña es demasiado corta o fácil de adivinar.',
  RATE_LIMITED: 'Demasiados intentos. Espera un minuto y vuelve a probar.',
  ACCOUNT_NAME_TAKEN: 'Ya tienes una cuenta con ese nombre.',
  CATEGORY_NAME_TAKEN: 'Ya tienes una categoría con ese nombre.',
  ACCOUNT_NOT_EMPTY: 'La cuenta tiene movimientos: bórralos antes.',
  CATEGORY_IN_USE: 'La categoría tiene movimientos: cámbialos antes.',
  CATEGORY_TYPE_MISMATCH: 'La categoría no corresponde al tipo de movimiento.',
  INVALID_DATE: 'Fecha fuera del rango permitido.',
  INVALID_PASSWORD: 'Contraseña incorrecta.',
  UNSUPPORTED_CURRENCY: 'Divisa no soportada.',
  INVALID_SESSION: 'Tu sesión ha caducado. Vuelve a entrar.',
};

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return byCode[error.code] ?? (error.status >= 500 ? t.errors.generic : error.message);
  }
  if (error instanceof NetworkError) {
    return error.kind === 'offline'
      ? t.errors.offline
      : error.kind === 'timeout'
        ? t.errors.timeout
        : t.errors.invalidResponse;
  }
  return t.errors.generic;
}
