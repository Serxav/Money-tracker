# Arquitectura

Visión general de cómo encajan las piezas del Money Tracker. Las tecnologías concretas están **por decidir** por el equipo; aquí se dejan como propuesta.

```
 ┌──────────────┐        ┌──────────────┐
 │ App Android  │        │ App Desktop  │
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

## Backend

- API REST con respuestas JSON.
- Autenticación de usuarios (registro, login, token).
- Persistencia en base de datos.

### Modelo de datos (borrador)

| Entidad | Campos principales |
|---|---|
| Usuario | id, nombre, email, contraseña (hash) |
| Cuenta | id, usuario_id, nombre, saldo_inicial |
| Categoría | id, usuario_id, nombre, icono, color, tipo (ingreso/gasto) |
| Movimiento | id, cuenta_id, categoría_id, importe, fecha, descripción, tipo |
| Presupuesto | id, categoría_id, límite, periodo |

### Endpoints (borrador)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/auth/register` | Crear usuario |
| POST | `/auth/login` | Iniciar sesión |
| GET/POST | `/accounts` | Listar / crear cuentas |
| GET/POST | `/categories` | Listar / crear categorías |
| GET/POST | `/transactions` | Listar (con filtros) / crear movimientos |
| PUT/DELETE | `/transactions/{id}` | Editar / borrar movimiento |
| GET | `/summary?month=YYYY-MM` | Resumen mensual |
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
- Tecnología Desktop (JavaFX, Electron, Qt...).
- Android: Kotlin + Jetpack Compose (recomendado) o Java + XML.
- Herramienta de diseño para la UI (Figma u otra).
