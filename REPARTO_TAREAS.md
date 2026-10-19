# Reparto de tareas

| Área | Responsable |
|---|---|
| **UI (Interfaz de usuario)** | Pau |
| **Aplicación móvil (Android + iOS a la vez)** | Jan |
| **Backend** (+ apoyo a la app móvil) | Sergi y Aleix |

---

## Pau — UI

Diseño de la interfaz de la app móvil (Android e iOS: respetar las guías de cada plataforma).

- [ ] Definir guía de estilo (colores, tipografía, iconos, componentes).
- [ ] Wireframes de las pantallas principales.
- [ ] Mockups finales (Android e iOS).
- [ ] Pantallas: login/registro, inicio con resumen, lista de movimientos, añadir/editar movimiento, categorías y cuentas, estadísticas, presupuestos, ajustes.
- [ ] Diseño de gráficos y estados vacíos / de error.
- [ ] Selector de divisa, formato de importes y visualización de importes convertidos.
- [ ] Modo claro / oscuro (opcional).
- [ ] Accesibilidad: contraste suficiente, no transmitir significado solo con el color, tamaños de texto escalables, áreas táctiles mínimas y etiquetas para lectores de pantalla (TalkBack/VoiceOver).
- [ ] Recursos gráficos por densidades/tamaños, formatos adecuados, imágenes decorativas marcadas, fuentes con licencia revisada (autoalojadas) y registro de licencias de los recursos.
- [ ] Capturas, iconos y textos para las fichas de Google Play y App Store.
- [ ] Entregar assets (iconos, logo) a Jan, Sergi y Aleix en `design/`.
- [ ] Revisar que las implementaciones respetan el diseño.

## Jan — App móvil (Android + iOS)

Implementación de la aplicación móvil para **Android e iOS a la vez**, siguiendo el diseño de Pau. La elección de framework (ver [ARQUITECTURA.md](ARQUITECTURA.md)) condiciona el plan; con un framework multiplataforma se comparte casi todo el código.

- [ ] Elegir framework (multiplataforma o nativo) y crear el proyecto base en `mobile/`, con arquitectura por capas/features, cliente HTTP único y configuración por entorno (ver [ARQUITECTURA.md](ARQUITECTURA.md)).
- [ ] Confirmar acceso a un Mac y cuenta de desarrollador de Apple para iOS.
- [ ] Implementar las pantallas diseñadas por Pau.
- [ ] Conexión con la API del backend (login, movimientos, categorías, cuentas).
- [ ] Gestión de sesión del usuario.
- [ ] CRUD de movimientos, categorías y cuentas (cada cuenta con su divisa).
- [ ] Formato de importes según divisa e idioma.
- [ ] Pantalla de resumen y gráficos.
- [ ] Presupuestos y avisos.
- [ ] Manejo de errores y estados de interfaz (cargando, error, vacío, éxito) y sin conexión; sin respuestas antiguas que pisen datos nuevos.
- [ ] Validación de entradas en la app (solo para UX) y de la estructura de las respuestas.
- [ ] Sin secretos en el binario; tokens en Keystore/Keychain; limpieza de datos al cerrar sesión.
- [ ] Accesibilidad: etiquetas, escalado de texto y pruebas con TalkBack y VoiceOver.
- [ ] Tests unitarios y de interfaz de los flujos críticos; formateador, linter y tipos en la CI.
- [ ] Definir versiones mínimas de Android e iOS y probar en dispositivos reales y modestos.
- [ ] Identificadores, firma y certificados; fichas de las tiendas y declaraciones de privacidad.
- [ ] Almacenamiento seguro en cada plataforma (Keystore en Android, Keychain en iOS).
- [ ] Pruebas en dispositivo / emulador / simulador en ambas plataformas.
- [ ] Generación del APK (Android) y distribución de los RC por TestFlight (iOS).

## Sergi y Aleix — Backend y apoyo a móvil

### Backend

- [ ] Elegir tecnología y crear proyecto base en `backend/`.
- [ ] Diseñar e implementar la base de datos (usuarios, cuentas, categorías, movimientos, presupuestos).
- [ ] Autenticación (registro, login, tokens).
- [ ] Endpoints CRUD de cuentas, categorías y movimientos.
- [ ] Divisas por cuenta e importes en unidad menor (ISO 4217).
- [ ] Tabla de tipos de cambio y tarea de actualización periódica.
- [ ] Endpoint de resumen / estadísticas mensuales con conversión a la divisa base.
- [ ] Presupuestos.
- [ ] Documentar la API en `backend/openapi.yaml` (contrato para que Jan y la app móvil la consuman) y versionarla (`/v1`).
- [ ] Validaciones (rechazo de campos inesperados) y manejo de errores con formato estándar, sin detalles internos.
- [ ] Autorización en el servidor en cada endpoint; el propietario sale del token.
- [ ] Permisos mínimos de BD, ninguna tabla pública y RLS si el motor lo soporta; migraciones reproducibles.
- [ ] *Rate limiting*, límites de tamaño, paginación e `Idempotency-Key` en la creación de movimientos.
- [ ] Logs seguros con `X-Request-Id`, métricas y alertas básicas.
- [ ] Copias de seguridad cifradas y prueba de restauración.
- [ ] Tests de API, autorización y seguridad (acceso a datos de otro usuario, *mass assignment*).
- [ ] Exportación CSV.

### Tareas transversales (todo el equipo)

- [ ] ADR para las decisiones técnicas (stack, framework móvil, API propia o BaaS) en `docs/adr/`.
- [ ] `.env.example`, archivos de bloqueo de dependencias y CI con formato, lint, tipos, tests, análisis estático, dependencias y secretos (ver [WORKFLOW.md](WORKFLOW.md) §10 y §14).
- [ ] Política de privacidad, términos de uso y proceso para ejercer derechos (responsable por asignar; ver [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md) §7).
- [ ] Acuerdo escrito de autoría y licencia entre los colaboradores.
- [ ] Plan de respuesta a incidentes y de mitigación de una release defectuosa.

### Apoyo a la app móvil

Cuando la API base (login, cuentas, movimientos) esté estable, Sergi y Aleix colaboran con Jan en la app móvil, de acuerdo con él:

- [ ] Acordar con Jan qué partes toma cada uno (p. ej. pantallas concretas, iOS, tests o integración con la API).
- [ ] Revisar los PRs móviles (ver [WORKFLOW.md](WORKFLOW.md) §3).
- [ ] Probar en dispositivos y simuladores de Android e iOS.

### Sugerencia de división interna

| Persona | Foco principal |
|---|---|
| Sergi | Backend (API + BD) y apoyo a la app móvil |
| Aleix | Backend (autenticación, divisas y exportación) y apoyo a la app móvil |

*(Sin web, el backend es más pequeño y la app móvil —dos plataformas— es el cuello de botella, así que **Sergi y Aleix apoyan a Jan en móvil** una vez la API base esté estable. Ajustable según la carga.)*

---

## Fases del proyecto

| Fase | Contenido | Responsables |
|---|---|---|
| 1. Diseño y base | Guía de estilo + wireframes; proyectos base; modelo de datos y API definida | Pau / Jan / Sergi y Aleix |
| 2. Núcleo | Login, CRUD de movimientos, categorías y cuentas (backend + clientes) | Todos |
| 3. Valor añadido | Resumen, gráficos, presupuestos | Todos |
| 4. Cierre | Pruebas (seguridad, accesibilidad, rendimiento, dispositivos reales), pulido de UI, políticas legales y fichas de tiendas, documentación, entrega | Todos |

## Fuera de alcance por ahora

- **Versión web**: no se desarrolla por ahora. La API se mantiene agnóstica del cliente para poder añadirla después.
- Transferencias entre cuentas de distinta divisa.
- Conexión con bancos reales.

## Dependencias entre áreas

- **Jan** (Android + iOS) necesita los diseños de **Pau** para construir las pantallas.
- **Jan** necesita la **API documentada** del backend; mientras tanto puede trabajar con datos de prueba (*mocks*).
- Acordar pronto el contrato de la API (rutas y formato JSON) para trabajar en paralelo.

## Comunicación

- Reunión breve semanal para revisar avance y bloqueos.
- Tareas y bugs en **Issues** de GitHub; una issue por tarea, asignada a su responsable.
- Cambios mediante Pull Request (ramas `feat/`, `fix/`, `docs/` y commits con prefijo; ver [CONTRIBUTING.md](CONTRIBUTING.md)).
