/** Error de la API con el código estable del backend (ver backend/README.md). */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly requestId?: string,
    readonly details: { field: string; message: string }[] = [],
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

/** Sin conexión, tiempo de espera agotado o respuesta con formato inesperado. */
export class NetworkError extends Error {
  constructor(
    readonly kind: 'offline' | 'timeout' | 'invalid-response',
    message: string,
  ) {
    super(message);
    this.name = 'NetworkError';
  }
}
