# Money Tracker — backend

API REST en **Java 21 + Spring Boot 4** con PostgreSQL. Contrato: [`openapi.yaml`](openapi.yaml). Decisiones: [ADR 0001](../docs/adr/0001-stack-backend-java.md) y [ADR 0002](../docs/adr/0002-base-de-datos-y-build.md).

## Requisitos

- JDK 21 o superior
- Docker o Podman (base de datos local y tests de integración)
- Nada más: Maven se descarga con el *wrapper* (`./mvnw`)

## Arrancar en local

```bash
cp .env.example .env          # en la raíz del repo; rellena DB_PASSWORD
docker compose up -d          # o: podman compose up -d
cd backend
set -a; source ../.env; set +a
./mvnw spring-boot:run
```

- API en `http://localhost:8080/v1/...`
- Con `SPRING_PROFILES_ACTIVE=dev`: Swagger UI en `http://localhost:8080/swagger-ui.html`
- En `dev`, si `JWT_SECRET` está vacío se genera una clave efímera (las sesiones caducan al reiniciar). **Fuera de `dev` es obligatorio** y debe tener al menos 32 bytes.

## Tests

```bash
./mvnw verify
```

- `*Test`: unitarios. `*IT`: integración contra PostgreSQL real (Testcontainers).
- `OpenApiContractIT` falla si `openapi.yaml` no coincide con la API. Tras cambiar la API, regenera el contrato y revisa el diff:

```bash
./mvnw verify -Dopenapi.update=true
```

Con **Podman** en lugar de Docker, exporta antes:

```bash
export DOCKER_HOST=unix://$XDG_RUNTIME_DIR/podman/podman.sock   # tras: systemctl --user start podman.socket
export TESTCONTAINERS_RYUK_DISABLED=true
```

## Estructura

```text
backend/
├── migrations/                # Flyway: V1 usuarios/sesiones, V2 cuentas/categorías, V3 movimientos
├── openapi.yaml               # contrato con la app móvil (verificado por los tests)
└── src/main/java/com/moneytracker/
    ├── core/                  # configuración, errores, seguridad, filtros, paginación
    ├── auth/                  # registro, login, refresh tokens
    ├── users/                 # /v1/me
    ├── currencies/            # divisas ISO 4217
    ├── accounts/              # cuentas
    ├── categories/            # categorías
    └── transactions/          # movimientos e idempotencia
```

## Convenciones de la API (resumen)

| Tema | Cómo es |
|---|---|
| Rutas | Prefijo `/v1` |
| Autenticación | `Authorization: Bearer <accessToken>` (JWT, 15 min). Renovar con `POST /v1/auth/refresh` (el refresh token rota en cada uso) |
| Importes | Enteros en **unidades menores** (`2550` = 25,50 €; en JPY `2550` = 2550 ¥). `fractionDigits` indica los decimales de cada divisa |
| Movimientos | `amount` siempre positivo; `type` (`INCOME`/`EXPENSE`) indica el signo. La divisa es la de la cuenta |
| Paginación | `?page=0&limit=20` (máx. 100). Respuesta `{items, page, limit, total}` |
| Idempotencia | `Idempotency-Key` (8-64 `[A-Za-z0-9_-]`) en `POST /v1/transactions` |
| Errores | `{code, message, requestId, details?}`; nunca detalles internos |
| Trazas | `X-Request-Id` opcional; se devuelve siempre en la respuesta |
| JSON | Estricto: campos desconocidos, decimales en enteros o números como texto → `400` |
| Lo ajeno | Un recurso de otro usuario responde `404` (no se revela que existe) |

## Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Sí | Conexión a PostgreSQL |
| `JWT_SECRET` | Sí (salvo `dev`) | Clave HMAC ≥ 32 bytes (Base64 o texto) |
| `SPRING_PROFILES_ACTIVE` | No | `dev` en local |
| `API_DOCS_ENABLED` | No | Expone OpenAPI/Swagger (desactivado por defecto) |
| `RATE_LIMIT_AUTH_PER_MINUTE`, `RATE_LIMIT_API_PER_MINUTE` | No | Límites por IP (10 y 300 por defecto) |
| `PORT` | No | Puerto HTTP (8080) |

## Limitaciones conocidas de la 0.1

- *Rate limiting* en memoria: válido para una sola instancia.
- Detrás de un proxy hay que configurar `server.forward-headers-strategy` para que la IP del cliente sea la real.
- RLS de PostgreSQL aún no activado; la autorización se aplica en los servicios y está cubierta por tests.
- El registro devuelve `409` si el email ya existe (falta verificación por email para evitarlo).
- Sin resumen mensual, tipos de cambio ni presupuestos (hitos `0.2.0` y `0.3.0`).
