# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado [SemVer](https://semver.org/lang/es/). Ver [WORKFLOW.md](WORKFLOW.md) §7.

## [Unreleased]

## [0.1.0] - 2026-10-19

Versión alfa de desarrollo: backend y app móvil con registro, cuentas multidivisa, categorías y movimientos.

### Añadido
- **App móvil 0.1 (alfa de desarrollo)** para Android e iOS con React Native + Expo SDK 58 + TypeScript ([mobile/README.md](mobile/README.md)):
  - Registro, login, sesión persistente segura (Keychain/Keystore) y cierre de sesión.
  - Cuentas con su divisa y saldo; alta de cuentas.
  - Movimientos: alta (con `Idempotency-Key`), edición, borrado y listado paginado por cuenta.
  - Categorías: listado, alta y borrado.
  - Ajustes: divisa principal y borrado de la cuenta con todos los datos.
  - Importes sin coma flotante, respuestas validadas con zod, controles accesibles.
  - 16 tests (dinero, fechas, cliente HTTP con renovación de sesión, accesibilidad), CI móvil y `expo-doctor` sin incidencias.
- ADR 0003: framework móvil React Native + Expo.
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

### Pendiente (QA del RC y siguientes versiones)
- Probar la app en dispositivos reales Android e iOS (QA del RC).
- RLS en PostgreSQL como segunda barrera de autorización.
- Hosting de dev/staging y despliegue automático.
