# ADR 0002 — Base de datos, compilación y versión de Java

- **Estado:** aceptado para la versión alfa `0.1.0` (pendiente de confirmar por el equipo antes de la `1.0.0`)
- **Fecha:** 2026-10-19
- **Afecta a:** `backend/`, `compose.yaml`, CI

## Contexto

[ADR 0001](0001-stack-backend-java.md) fijó Java y Spring Boot para el backend, pero dejó abiertas la base de datos, la herramienta de compilación y la versión de Java. [ARQUITECTURA.md](../../ARQUITECTURA.md) recomienda un motor con RLS y buen soporte de transacciones (p. ej. PostgreSQL). Para construir la `0.1.0` hacía falta concretarlas.

## Decisión

| Tema | Decisión | Motivo |
|---|---|---|
| Base de datos | **PostgreSQL 17** | Es la recomendación de ARQUITECTURA.md: transacciones, restricciones, RLS para más adelante |
| Migraciones | **Flyway**, en `backend/migrations/` | Reproducibles y versionadas junto al código |
| Compilación | **Maven** con *wrapper* (`./mvnw`) | No hay que instalar nada; dependencias fijadas por el *parent* de Spring Boot |
| Java | **21 (LTS)** | LTS soportada por Spring Boot 4 y por el JDK de todo el equipo |
| Spring Boot | **4.1.x** | Versión estable con soporte vigente (la rama 3.5 ya no tiene soporte gratuito) |
| Tests | JUnit 5 + **Testcontainers** (PostgreSQL real) | Mismo motor en tests y producción |
| Desarrollo local | `compose.yaml` con PostgreSQL | Arranque en un comando con Docker o Podman |

## Consecuencias

- Hace falta Docker o Podman para los tests de integración y la base de datos local.
- Al pasar a Java 25 (LTS) bastará con cambiar `java.version` en el `pom.xml` y la CI.
- RLS **todavía no está activado** (ver "Pendiente" en `CHANGELOG.md`): la autorización se impone en el servicio y está cubierta por tests.
