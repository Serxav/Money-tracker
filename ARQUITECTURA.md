# Arquitectura

Visión general de cómo encajan las piezas del Money Tracker. Las tecnologías concretas están **por decidir** por el equipo; aquí se dejan como propuesta.

```
 ┌──────────────┐
 │ App móvil    │
 │ Android+iOS  │
 │    (Jan)     │
 └──────┬───────┘
        │  HTTPS / JSON
  ┌─────▼─────┐
  │  Backend  │  API REST
  │(Sergi/Aleix)
  └─────┬─────┘
  ┌─────▼─────┐
  │    BD     │
  └───────────┘
```

La UI (Pau) define el diseño de la app móvil, de modo que Android e iOS mantengan una experiencia coherente.

> **Por ahora solo hay app móvil, para Android e iOS a la vez.** No se desarrolla versión web. Un único equipo móvil mantiene las dos plataformas, por lo que se recomienda una base de código compartida (ver decisiones pendientes). La API es agnóstica del cliente (REST + JSON), de modo que una web futura podría consumirla sin cambios en el backend.

> **Principio básico:** la app móvil es un **cliente no confiable**. Las validaciones y restricciones de la app mejoran la experiencia, pero la validación, la autorización y los permisos se imponen siempre en el servidor (ver [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md)).

## Organización del código

- **Separar capas**: interfaz, lógica de negocio, acceso a datos y comunicación con servicios externos. Frontend (app), backend y almacenamiento son responsabilidades distintas.
- **Organizar por funcionalidades** (*feature-based*): autenticación, cuentas, categorías, movimientos, resumen, presupuestos, ajustes. Cada una con su UI, lógica y datos.
- **Backend como monolito modular**: un solo servicio con módulos de límites claros y contratos estables. No se dividen en microservicios salvo necesidad real y medida.
- Archivos y clases pequeños, una responsabilidad cada uno; SOLID cuando ayude a reducir acoplamiento y a probar mejor; sin duplicación, dependencias circulares ni abstracciones innecesarias.
- **Centralizar** configuración, gestión de errores, cliente HTTP y componentes compartidos.
- **Convenciones** de nombres, estilo y estructura acordadas y aplicadas con formateador y linter (ver [WORKFLOW.md](WORKFLOW.md) §14).
- **Decisiones técnicas** importantes en un ADR corto (`docs/adr/NNNN-titulo.md`: contexto, decisión, consecuencias).

Estructura orientativa (a adaptar al framework elegido):

```text
mobile/
├── src/
│   ├── core/        # red, configuración, errores, seguridad, almacenamiento seguro
│   ├── features/    # auth/, accounts/, transactions/, summary/, budgets/...
│   ├── shared/      # componentes y utilidades comunes
│   └── app_entry
├── assets/          # imágenes, iconos, fuentes
├── tests/
└── README.md
backend/
├── src/modules/     # auth, accounts, categories, transactions, summary, budgets, currencies
├── src/core/        # configuración, errores, middleware, logging
├── migrations/
├── tests/
├── openapi.yaml
└── README.md
docs/adr/            # decisiones de arquitectura
.env.example         # variables necesarias, sin valores secretos
```

## Backend

- API REST con respuestas JSON.
- Autenticación de usuarios (registro, login, token).
- Persistencia en base de datos.
- Detalle de convenciones de la API en [Convenciones de la API](#convenciones-de-la-api).

### Modelo de datos (borrador)

| Entidad | Campos principales |
|---|---|
| Usuario | id, nombre, email, contraseña (hash), `divisa_base` (ISO 4217), idioma |
| Cuenta | id, usuario_id, nombre, `divisa` (ISO 4217), saldo_inicial |
| Categoría | id, usuario_id, nombre, icono, color, tipo (ingreso/gasto) |
| Movimiento | id, cuenta_id, categoría_id, `importe` (entero en unidad menor), fecha, descripción, tipo |
| Presupuesto | id, categoría_id, límite, `divisa`, periodo |
| TipoCambio | id, divisa_origen, divisa_destino, tasa, fecha, fuente |

### Multidivisa

Se aceptan **varias divisas** desde la primera versión. Propuesta de diseño (a validar por el equipo):

- **Cada cuenta tiene una única divisa** (código [ISO 4217](https://www.iso.org/iso-4217-currency-codes.html): `EUR`, `USD`, `GBP`...). Los movimientos heredan la divisa de su cuenta y no se pueden mezclar divisas dentro de una misma cuenta.
- **Importes como enteros en la unidad menor** de la divisa (céntimos), nunca `float`. El número de decimales depende de la divisa (`EUR` = 2, `JPY` = 0, `KWD` = 3), por lo que se toma de una tabla ISO 4217 y no se asume 2.
- **Divisa base del usuario**: en ella se muestran los totales del resumen, los gráficos y los presupuestos globales. Se puede cambiar en ajustes.
- **Tipos de cambio** en la tabla `TipoCambio`, actualizada por el backend desde un proveedor externo (por ejemplo el BCE, que publica tasas de referencia diarias). Los clientes **nunca** llaman al proveedor: solo el backend, y sin enviarle datos del usuario.
- **Conversión para resúmenes**: cada movimiento se convierte a la divisa base con la tasa de su fecha (o la más cercana anterior). Las cifras convertidas son **informativas**: el dato real es el importe original en su divisa.
- **Transferencias entre cuentas de distinta divisa**: fuera del alcance de la `0.1.0`; se decidirá después (qué tasa se aplica y cómo se registra).
- **Presentación**: formato de importes según divisa e idioma (`NumberFormat` en Android, `NumberFormatter` en iOS); mostrar siempre el código o símbolo de la divisa.
- **Pruebas**: tests de redondeo y de conversión con divisas de 0, 2 y 3 decimales.

### Convenciones de la API

- **Contrato**: descrito en `backend/openapi.yaml` (OpenAPI) y versionado en el repo; es la referencia común para backend y app móvil.
- **Versionado**: prefijo `/v1/...`; los cambios incompatibles van a `/v2` y se mantiene la compatibilidad con las versiones de la app aún instaladas (no se puede forzar a todos los usuarios a actualizar al instante).
- **Cliente HTTP único** en la app, con la URL base por entorno (dev, staging, prod), *timeouts*, cancelación y gestión de errores comunes.
- **Reintentos**: solo en operaciones seguras (lecturas) o idempotentes, con límite y espera progresiva (*exponential backoff*).
- **Idempotencia**: la creación de movimientos admite una cabecera `Idempotency-Key` para que un reintento (p. ej. tras perder la conexión) no duplique el registro.
- **Paginación** obligatoria en listados (`cursor` o `page`/`limit`, con límite máximo); sin consultas N+1 ni peticiones duplicadas.
- **Respuestas mínimas**: DTOs explícitos; no devolver campos internos, el hash de la contraseña, datos personales innecesarios ni registros completos si solo se necesitan algunos campos.
- **Validación**: el backend valida tipos, longitudes, rangos y enumeraciones y **rechaza campos inesperados**; el cliente además valida para dar mejor feedback, pero no es una barrera de seguridad. La app valida también la estructura de las respuestas.
- **Errores** con formato estándar (`code`, `message` genérico, `requestId`); nunca mensajes internos del servidor ni *stack traces*.
- **Trazabilidad**: cabecera `X-Request-Id` generada por la app y propagada a los logs del backend para correlacionar errores.
- **Caché** solo con reglas explícitas de expiración e invalidación (p. ej. tipos de cambio y listas de divisas).
- **Límites**: tamaño máximo de petición y respuesta, y *rate limiting* en el servidor por usuario, IP y token.
- **Monitorización** de latencia, errores y disponibilidad.

### Base de datos y permisos

- **Ninguna tabla queda pública por defecto**: se parte de acceso denegado y se abre solo lo necesario; usuario de aplicación con permisos mínimos.
- **Claves foráneas, restricciones, transacciones e índices** adecuados; migraciones reproducibles y probadas en `backend/migrations/`.
- **Copias de seguridad** con pruebas de restauración y política de retención.
- La app **nunca** habla directamente con la base de datos ni lleva credenciales de ella: solo con la API.
- **Row-Level Security (RLS)**: si el motor la soporta (p. ej. PostgreSQL), se activa en las tablas con datos de usuario, con políticas separadas de lectura, inserción, actualización y borrado, como segunda barrera además de la autorización en el servidor. Si se eligiera un servicio gestionado (BaaS) en el que el cliente accede directamente a los datos, RLS pasa a ser **imprescindible** y las claves públicas del cliente deben tener privilegios mínimos; las claves de servicio y administración viven solo en el servidor.

### Endpoints (borrador)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crear usuario |
| POST | `/auth/login` | Iniciar sesión |
| GET/POST | `/accounts` | Listar / crear cuentas |
| GET/POST | `/categories` | Listar / crear categorías |
| GET/POST | `/transactions` | Listar (con filtros) / crear movimientos |
| PUT/DELETE | `/transactions/{id}` | Editar / borrar movimiento |
| GET | `/summary?month=YYYY-MM&currency=EUR` | Resumen mensual (convertido a la divisa pedida; por defecto, la base del usuario) |
| GET | `/currencies` | Divisas soportadas |
| GET | `/exchange-rates?from=USD&to=EUR&date=YYYY-MM-DD` | Tipo de cambio de referencia |
| GET/POST | `/budgets` | Presupuestos |

## Clientes

La app móvil (Android e iOS) consume la API y tiene estas pantallas principales:

1. Login / registro
2. Inicio (resumen y balance)
3. Lista de movimientos
4. Añadir / editar movimiento
5. Categorías y cuentas
6. Estadísticas y gráficos
7. Presupuestos y ajustes

## Estado, persistencia y modo sin conexión

- **Fuente de verdad**: el servidor. La app solo guarda localmente lo necesario (caché y, si se decide, cola de operaciones pendientes).
- **Estados de interfaz** definidos para cada pantalla: cargando, error, vacío, éxito y actualizando.
- Evitar que una **respuesta antigua** sobrescriba datos más recientes (identificadores de petición o marcas de versión).
- **Datos locales** sensibles protegidos (almacenamiento seguro/cifrado de cada plataforma), con migraciones para la base local y **limpieza completa al cerrar sesión**.
- **Ciclo de vida**: manejar segundo plano, cancelación de tareas y reanudación.
- Si hay **modo offline**: definir qué acciones se permiten sin conexión, cómo se reintentan de forma segura (con `Idempotency-Key`) y cómo se resuelven los conflictos de sincronización. Hasta decidirlo, la app muestra claramente que no hay conexión.

## Escalabilidad

Se diseña para poder crecer, pero sin sobredimensionar al principio:

- Backend separado del cliente y monolito modular con contratos estables.
- **Tareas asíncronas/colas** para operaciones largas: actualización de tipos de cambio, exportación CSV, envíos.
- Paginación y procesamiento por lotes; operaciones idempotentes.
- Caché solo donde se mida un beneficio, con invalidación diseñada.
- **Medir antes de optimizar**: pruebas de carga y límites de consumo definidos antes de la `1.0.0`; CDN, caché avanzada y observabilidad avanzada cuando el producto crezca.
- Objetivos de disponibilidad, recuperación y capacidad cuando el producto lo requiera.

## Fuera de alcance por ahora

- **Versión web**: se podría añadir más tarde reutilizando la misma API (habría que revisar CORS, sesiones y los requisitos web de seguridad).
- **Subida de archivos** (adjuntar tickets o fotos), **pagos, suscripciones y emails comerciales**: no existen en el alcance actual. Si se añadieran, aplicar antes los apartados correspondientes de [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md) §12.

## Decisiones pendientes

- ~~Lenguaje y framework del backend~~: **decidido, Java (Spring Boot)** — ver [ADR 0001](docs/adr/0001-stack-backend-java.md). Pendiente: versión de Java y herramienta de compilación.
- Base de datos (SQLite, PostgreSQL, MySQL...). Recomendable un motor con RLS y buen soporte de transacciones (p. ej. PostgreSQL).
- **API propia o servicio gestionado (BaaS)**: con API propia el cliente nunca toca la BD (recomendado); con BaaS, RLS es obligatorio.
- Versiones mínimas soportadas de Android e iOS.
- Si la app usará notificaciones push o *deep links* (aumentan la superficie de ataque y las pruebas).
- Objetivos de disponibilidad y recuperación (RTO/RPO) para la `1.0.0`.
- **App móvil (Android + iOS a la vez)**: framework. Opciones: multiplataforma con una sola base de código (Flutter, React Native o Kotlin Multiplatform; lo más realista para una persona/equipo pequeño) o dos apps nativas (Kotlin + Jetpack Compose y Swift + SwiftUI; más trabajo y doble mantenimiento).
- Herramienta de diseño para la UI (Figma u otra).
- Proveedor de tipos de cambio y frecuencia de actualización.
- Qué tasa se usa en los resúmenes (la de la fecha del movimiento o la actual) y cómo se tratan las transferencias entre divisas.
- Lista inicial de divisas soportadas.
- Idiomas de la app (español, catalán, inglés...).
- Si la app móvil funciona sin conexión y sincroniza después.
- iOS: compilar y firmar requiere un **Mac** y una cuenta de desarrollador de Apple (de pago, para TestFlight/App Store). Decidir quién dispone de ellos; si no hay, valorar compilar en un servicio de CI con macOS.
- Reparto concreto del trabajo móvil entre Jan, Sergi y Aleix (quién toma qué pantallas, iOS y pruebas en dispositivos).
