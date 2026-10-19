# Money Tracker project

Proyecto conjunto de cuatro estudiantes de FP para crear una **herramienta open source útil de verdad**.

## 📌 Estado

**Versión `0.1.0` — RC1 (alfa de desarrollo).** Backend ([backend/README.md](backend/README.md)) y app móvil Android/iOS con React Native + Expo ([mobile/README.md](mobile/README.md)). Qué entra: [scope de la 0.1.0](docs/versiones/0.1.0.md). Cambios: [CHANGELOG.md](CHANGELOG.md). Pruebas: [sección de pruebas](#-pruebas-de-la-versión-010).

## 💰 Sobre el proyecto

Money Tracker es una aplicación para controlar ingresos y gastos personales, como **app móvil para Android e iOS**, con un backend común que guarda y sincroniza los datos. Una versión web podría añadirse más adelante, sin cambios en el backend.

- Funcionalidades previstas: movimientos, categorías, cuentas, resumen mensual, gráficos, presupuestos y exportación CSV. Soporta **varias divisas** con conversión a una divisa base.
- Arquitectura y modelo de datos: [ARQUITECTURA.md](ARQUITECTURA.md)
- Reparto de tareas y fases: [REPARTO_TAREAS.md](REPARTO_TAREAS.md)
- Versiones, RCs y ramas: [WORKFLOW.md](WORKFLOW.md)
- Seguridad, privacidad y ética: [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md)
- Seguridad (contacto para vulnerabilidades): [SECURITY.md](SECURITY.md)

| Área | Responsable |
|---|---|
| UI (interfaz de usuario) | Pau |
| App móvil (Android + iOS) | Jan |
| Backend + apoyo a la app móvil | Sergi y Aleix |

## 🧪 Pruebas de la versión 0.1.0

Resumen de todo lo probado para esta versión. Detalle completo en el [informe de QA](docs/versiones/0.1.0-qa.md).

### Tests automáticos

**Backend** — `cd backend && ./mvnw verify` → **49 tests en verde** (unitarios + integración contra PostgreSQL 17 real con Testcontainers):

| Suite | Tests | Qué comprueba |
|---|---|---|
| `PlatformIT` | 7 | Salud pública sin detalles, 401 con error estándar, token no válido, `X-Request-Id`, cabeceras de seguridad, rechazo de peticiones demasiado grandes |
| `AuthIT` | 14 | Registro (sin exponer el hash), email duplicado, contraseñas débiles, divisa no soportada, campos no permitidos, mismo error para email desconocido o contraseña mala, login sin distinguir mayúsculas, rotación del refresh token y revocación si se reutiliza, logout, `/v1/me`, borrado de cuenta con contraseña, divisas con sus decimales |
| `RateLimitIT` | 1 | Fuerza bruta en el login → 429 con `Retry-After` |
| `AccountsAndCategoriesIT` | 11 | Cuentas en EUR/JPY/KWD, nombres únicos, divisa inmutable, renombrar y borrar, **un usuario no puede ver ni tocar datos de otro**, no se puede inyectar el propietario, límites de paginación, categorías por defecto y su CRUD |
| `MovementsIT` | 10 | Divisa heredada de la cuenta y saldo, importes exactos en 0/2/3 decimales, importes y fechas no válidos (incluidos decimales o texto donde va un entero), tipo de categoría, autorización entre usuarios, filtros, orden y paginación, `Idempotency-Key`, edición y borrado, bloqueo de borrado de cuentas/categorías en uso |
| `OpenApiContractIT` | 1 | `backend/openapi.yaml` coincide con la API real |
| `PasswordPolicyTest`, `CurrencyServiceTest` | 5 | Política de contraseña y decimales ISO 4217 |

**App móvil** — `cd mobile && npm run check` → TypeScript estricto, ESLint y **19 tests en verde**; además `npx expo-doctor` (20/20) y generación de los bundles de Android e iOS:

| Suite | Tests | Qué comprueba |
|---|---|---|
| `money.test.ts` | 6 | Conversión de importes a unidades menores sin coma flotante (EUR, JPY, KWD), rechazo de formatos y máximos, formato con divisa |
| `dates.test.ts` | 2 | Fecha local `AAAA-MM-DD` y validación de fechas reales |
| `client.test.ts` | 7 | Token y `X-Request-Id`, errores de la API, respuestas con formato inesperado, renovación de sesión ante 401, cierre de sesión si el refresh token no vale, reintentos solo con `Idempotency-Key` |
| `schemas.test.ts` | 3 | Respuestas reales del backend aceptadas por los esquemas de la app |
| `TextField.test.tsx` | 1 | Etiqueta accesible y error anunciado como alerta |

**Prueba de humo del backend** con el `.jar` real en modo producción contra PostgreSQL: se niega a arrancar sin `JWT_SECRET`; registro, cuenta, movimiento y saldo correctos; Swagger desactivado (404); ningún email ni contraseña en los logs.

### QA manual en Android

**Entorno:** emulador de Android Studio *Medium Phone API 37*, build de desarrollo (`npx expo run:android`) y backend local.

| Área | Probado | Resultado |
|---|---|---|
| Arranque | Sin sesión → login; con sesión → inicio (arranque en frío ×3) | ✅ |
| Registro y login | Email no válido, campos vacíos, contraseña común, credenciales incorrectas, registro válido | ✅ |
| Sesión | Renovación automática al caducar el token; cerrar sesión no se restaura al reabrir; volver a entrar conserva los datos | ✅ |
| Cuentas | Alta en EUR y JPY con saldo inicial; importes con demasiados decimales rechazados; borrar vacía sí, con movimientos no | ✅ |
| Movimientos | Ingreso y gasto con saldos exactos (100 € → 1.567,50 €; 20.000 JPY → 18.766 JPY); editar (formulario precargado) y borrar | ✅ |
| Categorías | Alta, nombre duplicado rechazado, borrado; no deja borrar una en uso | ✅ |
| Ajustes | Cambio de divisa principal guardado en el servidor | ✅ |
| Privacidad | Borrar cuenta: contraseña incorrecta rechazada; con la correcta se borra todo y el login devuelve 401 | ✅ |
| Accesibilidad | Etiquetas en todos los controles, errores como alerta, texto al 150 % sin cortes, áreas táctiles ≥ 48 dp | ✅ |

**Fallos encontrados y corregidos durante la QA** (con test de regresión):

1. La app abría en "Unmatched Route" sin sesión → ahora abre en el login.
2. Lo mismo al recargar la app con sesión → ahora abre en el inicio.
3. Crear un movimiento devolvía `createdAt`/`updatedAt` a `null`, la app no cerraba el formulario y el siguiente alta fallaba por `Idempotency-Key` reutilizada → corregido en el backend.

**Pendiente de probar:** iOS (requiere macOS), un móvil Android físico y la versión mínima de Android, recorrido completo con TalkBack, comportamiento sin conexión y la divisa de 3 decimales (KWD) a mano.

### Cómo repetir las pruebas

```bash
cd backend && ./mvnw verify          # requiere Docker o Podman
cd mobile && npm ci && npm run check
```

Para la QA manual, sigue [backend/README.md](backend/README.md) y [mobile/README.md](mobile/README.md) y el checklist de [WORKFLOW.md](WORKFLOW.md) §5.

## 👥 Equipo

| | GitHub | Rol |
|---|---|---|
| Sergi Pascual | [@Serxav](https://github.com/Serxav) | Developer |
| Jan Rodríguez | [@P4NKK-TRYHARD](https://github.com/P4NKK-TRYHARD) | Developer |
| Pau Olivares. | [@Pau-Dev-O](https://github.com/Pau-Dev-O) | Developer |
| Aleix De Alburquerque. | [@condeduque](https://github.com/condeduque) | Developer |

## 🤝 Cómo trabajamos

Lee [CONTRIBUTING.md](CONTRIBUTING.md): ramas, commits y pull requests.

Ramas: `main` (solo versiones finales) → `desarrollo/X.Y.Z/RCn` (versión en construcción y su candidata). Detalle en [WORKFLOW.md](WORKFLOW.md).

## 📄 Licencia

[MIT](LICENSE) © 2026 Sergi Pascual Calvo y colaboradores.
