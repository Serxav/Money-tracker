# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado [SemVer](https://semver.org/lang/es/). Ver [WORKFLOW.md](WORKFLOW.md) §7.

## [Unreleased]

### Añadido
- **Backend 0.1 (alfa de desarrollo)** en Java 21 + Spring Boot 4 + PostgreSQL ([backend/README.md](backend/README.md)):
  - Registro, login, refresco con rotación de sesiones, logout y borrado de cuenta con todos sus datos.
  - Cuentas con divisa ISO 4217 inmutable y saldo calculado; categorías de ingreso/gasto con categorías por defecto.
  - Movimientos con filtros, paginación e `Idempotency-Key`; importes enteros en unidades menores.
  - Divisas soportadas con sus decimales (`/v1/currencies`).
  - Seguridad: Argon2, JWT de 15 min, autorización por propietario (lo ajeno responde 404), JSON estricto, rate limiting, límite de tamaño, cabeceras de seguridad, errores sin detalles internos y `X-Request-Id`.
  - Contrato `backend/openapi.yaml` verificado por los tests; 49 tests (unitarios e integración con PostgreSQL real).
  - CI de GitHub Actions, CodeQL y Dependabot; `compose.yaml` y `.env.example` para desarrollo local.
- ADR 0002: PostgreSQL, Flyway, Maven y Java 21.
- Documentación inicial: README, arquitectura, reparto de tareas, workflow y guía de seguridad y ética.
- Buenas prácticas de ingeniería integradas: organización del código, convenciones de API, BD y permisos, secretos, testing, accesibilidad, publicación móvil y privacidad.
- App móvil planificada para Android e iOS a la vez. Sin versión web por ahora.
- Soporte multidivisa en el diseño (divisa por cuenta, divisa base del usuario y tipos de cambio).

### Pendiente para cerrar la 0.1.0
- **App móvil** (Android + iOS): falta decidir el framework (ver ARQUITECTURA.md).
- RLS en PostgreSQL como segunda barrera de autorización.
- Hosting de dev/staging y despliegue automático.
