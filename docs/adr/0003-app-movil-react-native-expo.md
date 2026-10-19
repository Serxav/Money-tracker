# ADR 0003 — App móvil con React Native + Expo

- **Estado:** aceptado
- **Fecha:** 2026-10-19
- **Afecta a:** `mobile/`, CI, reparto de tareas

## Contexto

La app se desarrolla para Android e iOS a la vez ([ARQUITECTURA.md](../../ARQUITECTURA.md)), con Jan como responsable y el apoyo de Sergi y Aleix. El equipo es pequeño, no todos tienen un Mac y el backend ya está en Java ([ADR 0001](0001-stack-backend-java.md)).

## Decisión

**React Native con Expo (SDK 58) y TypeScript**, con:

| Necesidad | Elección |
|---|---|
| Navegación | Expo Router (rutas en `src/app`, protegidas según la sesión) |
| Datos del servidor | TanStack Query (estados de carga/error, caché e invalidación) |
| Validación de respuestas | zod |
| Tokens | `expo-secure-store` (Keychain / Keystore) |
| Tests | Jest (`jest-expo`) + Testing Library |
| Calidad | TypeScript estricto, ESLint (`eslint-config-expo`), `expo-doctor` |

## Por qué

- Una sola base de código para las dos plataformas: lo más realista para el tamaño del equipo.
- Con EAS Build se puede compilar iOS en la nube sin un Mac propio (sigue haciendo falta la cuenta de desarrollador de Apple).
- Los módulos de Expo cubren lo que exige [SEGURIDAD_Y_ETICA.md](../../SEGURIDAD_Y_ETICA.md) (almacenamiento seguro, configuración nativa sin tocar `ios/` ni `android/`).

## Consecuencias

- Dos lenguajes en el proyecto (Java y TypeScript). El contrato `backend/openapi.yaml` es la referencia común; los esquemas zod de la app deben seguirlo.
- Las dependencias se instalan con `npx expo install` para que sean compatibles con la SDK.
- Pendiente: configurar EAS (perfiles de build, TestFlight y Play Console) cuando existan las cuentas de las tiendas.
