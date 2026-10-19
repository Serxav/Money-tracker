# ADR 0001 — Stack: backend en Java

- **Estado:** backend **aceptado**; app móvil y base de datos **pendientes** (siguen en "Decisiones pendientes" de [ARQUITECTURA.md](../../ARQUITECTURA.md)).
- **Fecha:** 2026-10-19
- **Decide:** equipo (Sergi, Aleix, Jan y Pau)
- **Afecta a:** `backend/`, CI, reparto de tareas

## Contexto

Money Tracker es una app móvil para Android e iOS con un backend común (ver [ARQUITECTURA.md](../../ARQUITECTURA.md)). Es un proyecto de cuatro estudiantes de FP (DAM): Sergi y Aleix llevan el backend y apoyan a Jan en el móvil. El backend debe cubrir lo que exigen [SEGURIDAD_Y_ETICA.md](../../SEGURIDAD_Y_ETICA.md) y [WORKFLOW.md](../../WORKFLOW.md): validación estricta, autorización en el servidor, *rate limiting*, contrato OpenAPI, migraciones reproducibles, tests de seguridad e importes exactos en varias divisas.

## Decisión

**El backend se desarrolla en Java.**

Propuesta de herramientas dentro del ecosistema Java (la elección concreta de cada pieza se confirmará al crear `backend/`):

| Necesidad | Propuesta |
|---|---|
| Lenguaje | Java, versión LTS vigente |
| Framework | **Spring Boot** (monolito modular) |
| Compilación | Maven o Gradle (a elegir; con dependencias bloqueadas) |
| Acceso a datos | Spring Data JPA (Hibernate) |
| Migraciones | Flyway o Liquibase, en `backend/migrations/` |
| Validación de entradas | Bean Validation (DTOs explícitos; campos inesperados rechazados) |
| Autenticación y autorización | Spring Security; contraseñas con Argon2id (o bcrypt) |
| Contrato de API | OpenAPI generado con springdoc, publicado como `backend/openapi.yaml` |
| *Rate limiting* | Bucket4j u otro filtro equivalente |
| Importes | `long` en unidades menores o `BigDecimal` con la escala de la divisa (nunca `double`), según [ARQUITECTURA.md](../../ARQUITECTURA.md#multidivisa) |
| Tests | JUnit 5 + Testcontainers (BD real) + tests de autorización y de contrato |
| Calidad | Formateador (p. ej. Spotless), Checkstyle/SpotBugs, CodeQL y OWASP Dependency-Check en la CI |

## Por qué Java

- Es lo que el equipo ha trabajado en DAM, así que baja la curva de aprendizaje de Sergi y Aleix.
- Spring Boot cubre de serie casi todo el checklist de seguridad y calidad (validación, seguridad, OpenAPI, tests).
- Tipado estático, ecosistema maduro, buena documentación y herramientas de seguridad y análisis estático.

## Consecuencias

- Habrá **dos lenguajes**: Java en el backend y el que se elija para el móvil. No se pueden compartir tipos ni validaciones entre ambos directamente; el **contrato OpenAPI** pasa a ser la fuente común (se pueden generar clientes y modelos a partir de él).
- El apoyo de Sergi y Aleix a Jan en el móvil dependerá del lenguaje móvil que se elija (ver más abajo).
- Hay que fijar la versión de Java, la herramienta de compilación y las convenciones de código antes de crear el proyecto base.
- El backend se empaqueta y despliega como aplicación JVM (contenedor o servicio gestionado) en una región de la UE; el hosting sigue pendiente.

## Qué NO se decide aquí (queda como estaba)

- **App móvil (Android + iOS a la vez):** el framework sigue pendiente. Java **no** es una opción práctica para el móvil (iOS no admite Java y las alternativas Java para iOS tienen poco ecosistema). Opciones que se mantienen abiertas:
  - Multiplataforma con una base de código: Flutter, React Native (Expo) o Kotlin Multiplatform.
  - Dos apps nativas: Kotlin + Jetpack Compose (Android) y Swift + SwiftUI (iOS).
- **Base de datos:** sigue pendiente (se recomienda un motor con RLS y buen soporte de transacciones, como PostgreSQL).
- Proveedor de tipos de cambio, hosting, herramienta de diseño y resto de decisiones listadas en [ARQUITECTURA.md](../../ARQUITECTURA.md).

## Alternativas consideradas para el backend

| Alternativa | Motivo para no elegirla ahora |
|---|---|
| NestJS (TypeScript) | Permitiría un solo lenguaje con React Native, pero el equipo conoce mejor Java |
| Kotlin + Spring Boot / Ktor | Muy buena opción, pero añade un lenguaje nuevo para el backend |
| Python (FastAPI/Django) | Rápido para empezar, pero menos tipado y menos conocido por el equipo |

## Revisión

Revisar esta decisión si, al elegir el framework móvil, compensa unificar el lenguaje (por ejemplo, Kotlin para backend y móvil) o si el equipo cambia.
