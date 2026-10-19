# Arquitectura

Visión general de cómo encajan las piezas del Money Tracker. Las tecnologías concretas están **por decidir** por el equipo; aquí se dejan como propuesta.

```
 ┌──────────────┐        ┌──────────────┐
 │ App Android  │        │ App Web      │
 │    (Jan)     │        │ (Sergi/Aleix)│
 └──────┬───────┘        └──────┬───────┘
        │      HTTPS / JSON     │
        └───────────┬───────────┘
              ┌─────▼─────┐
              │  Backend  │  API REST
              │(Sergi/Aleix)
              └─────┬─────┘
              ┌─────▼─────┐
              │    BD     │
              └───────────┘
```

La UI (Pau) define el diseño que implementan ambas aplicaciones, de modo que mantengan una experiencia coherente.

> **La app iOS se desarrolla al final** (última fase del plan, tras Web y Android). Por eso la API debe ser agnóstica del cliente (REST + JSON, sin depender de Android ni de la web) y así la app iOS la consuma sin cambios en el backend.

## Backend

- API REST con respuestas JSON.
- Autenticación de usuarios (registro, login, token).
- Persistencia en base de datos.

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
- **Presentación**: formato de importes según divisa e idioma (`Intl.NumberFormat` en web, `NumberFormat` en Android); mostrar siempre el código o símbolo de la divisa.
- **Pruebas**: tests de redondeo y de conversión con divisas de 0, 2 y 3 decimales.

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

Ambos clientes consumen la misma API y comparten pantallas principales:

1. Login / registro
2. Inicio (resumen y balance)
3. Lista de movimientos
4. Añadir / editar movimiento
5. Categorías y cuentas
6. Estadísticas y gráficos
7. Presupuestos y ajustes

## Decisiones pendientes

- Lenguaje y framework del backend.
- Base de datos (SQLite, PostgreSQL, MySQL...).
- Tecnología Web (React, Vue, Angular, HTML+JS...).
- Android: Kotlin + Jetpack Compose (recomendado) o Java + XML.
- Herramienta de diseño para la UI (Figma u otra).
- Proveedor de tipos de cambio y frecuencia de actualización.
- Qué tasa se usa en los resúmenes (la de la fecha del movimiento o la actual) y cómo se tratan las transferencias entre divisas.
- Lista inicial de divisas soportadas.
- Idiomas de la app (español, catalán, inglés...).
- Si Android funciona sin conexión y sincroniza después.
- iOS (última fase): tecnología (nativa SwiftUI o multiplataforma), quién la desarrolla y cómo se prueba/distribuye (requiere Mac y cuenta de desarrollador de Apple, TestFlight para los RC).
