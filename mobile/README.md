# Money Tracker — app móvil (Android + iOS)

App en **React Native + Expo (SDK 58) + TypeScript**, una sola base de código para Android e iOS. Consume la API del [backend](../backend/README.md) según el contrato [`backend/openapi.yaml`](../backend/openapi.yaml).

## Requisitos

- Node.js LTS (24.x)
- Para probar: la app **Expo Go** en un móvil, o un emulador Android / simulador iOS (este último solo en macOS)
- El backend arrancado en local (ver su README)

## Arrancar

```bash
cd mobile
npm ci
cp .env.example .env.local      # opcional: define EXPO_PUBLIC_API_URL
npx expo start
```

| Dónde se ejecuta la app | `EXPO_PUBLIC_API_URL` |
|---|---|
| Emulador Android | `http://10.0.2.2:8080` (valor por defecto) |
| Simulador iOS | `http://localhost:8080` (valor por defecto) |
| Móvil físico con Expo Go | `http://<IP-de-tu-PC>:8080` (misma red) |
| Builds que no son de desarrollo | **Obligatorio `https://`**: la app no arranca con HTTP |

## Comprobaciones (las mismas que la CI)

```bash
npm run typecheck   # TypeScript estricto
npm run lint        # ESLint (eslint-config-expo)
npm test            # Jest + Testing Library
npm run check       # las tres
npx expo-doctor     # dependencias y configuración de Expo
```

## Estructura

```text
mobile/src/
├── app/                 # rutas (Expo Router): solo pantallas finas
│   ├── (auth)/          # login y registro (sin sesión)
│   └── (app)/           # resto de pantallas (con sesión)
├── core/
│   ├── api/             # cliente HTTP, esquemas zod de las respuestas, errores
│   ├── session/         # sesión y almacenamiento seguro de tokens
│   └── config.ts        # URL de la API por entorno
├── features/            # auth, accounts, categories, transactions, settings
└── shared/              # dinero, fechas, textos, componentes de UI accesibles
```

## Decisiones clave

- **Dinero sin coma flotante**: importes enteros en unidades menores, como la API. `parseAmountToMinor` respeta los decimales de cada divisa (EUR 2, JPY 0, KWD 3).
- **Sesión segura**: refresh token en Keychain/Keystore (`expo-secure-store`, solo este dispositivo); access token solo en memoria. Al cerrar sesión se borra todo, incluida la caché.
- **Cliente HTTP único**: timeout, `X-Request-Id`, reintentos con espera progresiva solo en lecturas o altas con `Idempotency-Key`, y renovación de sesión ante un 401.
- **Respuestas validadas** con zod: si el servidor responde algo inesperado, la app muestra un error en vez de datos corruptos.
- **Accesibilidad**: etiquetas en todos los controles, errores anunciados al lector de pantalla, áreas táctiles de 48 px, gasto/ingreso no indicado solo por color.
- **Android**: `allowBackup` desactivado para que los datos no salgan en copias del sistema.

## Limitaciones conocidas de la 0.1

- Sin resumen mensual ni conversión entre divisas (hito `0.2.0`); el inicio muestra cada cuenta en su divisa.
- Fecha como texto `AAAA-MM-DD` (falta un selector de fecha).
- Solo en español; sin modo oscuro; paleta provisional hasta la guía de estilo de Pau.
- Sin modo offline: sin conexión se muestra el error y se puede reintentar.
- Sin bloqueo con biometría ni protección de capturas de pantalla (pendiente de decidir).
- Sin configuración de EAS Build/TestFlight: hace falta la cuenta de Apple (ver ARQUITECTURA.md).
- No probada todavía en dispositivos reales: solo comprobada con tipos, lint, tests y la generación del bundle.
