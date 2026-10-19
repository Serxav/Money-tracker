# Reparto de tareas

| Área | Responsable |
|---|---|
| **UI (Interfaz de usuario)** | Pau |
| **Aplicación móvil (Android + iOS a la vez)** | Jan |
| **Backend** | Sergi y Aleix |

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
- [ ] Entregar assets (iconos, logo) a Jan, Sergi y Aleix en `design/`.
- [ ] Revisar que las implementaciones respetan el diseño.

## Jan — App móvil (Android + iOS)

Implementación de la aplicación móvil para **Android e iOS a la vez**, siguiendo el diseño de Pau. La elección de framework (ver [ARQUITECTURA.md](ARQUITECTURA.md)) condiciona el plan; con un framework multiplataforma se comparte casi todo el código.

- [ ] Elegir framework (multiplataforma o nativo) y crear el proyecto base en `mobile/`.
- [ ] Confirmar acceso a un Mac y cuenta de desarrollador de Apple para iOS.
- [ ] Implementar las pantallas diseñadas por Pau.
- [ ] Conexión con la API del backend (login, movimientos, categorías, cuentas).
- [ ] Gestión de sesión del usuario.
- [ ] CRUD de movimientos, categorías y cuentas (cada cuenta con su divisa).
- [ ] Formato de importes según divisa e idioma.
- [ ] Pantalla de resumen y gráficos.
- [ ] Presupuestos y avisos.
- [ ] Manejo de errores y estados sin conexión.
- [ ] Almacenamiento seguro en cada plataforma (Keystore en Android, Keychain en iOS).
- [ ] Pruebas en dispositivo / emulador / simulador en ambas plataformas.
- [ ] Generación del APK (Android) y distribución de los RC por TestFlight (iOS).

## Sergi y Aleix — Backend

### Backend

- [ ] Elegir tecnología y crear proyecto base en `backend/`.
- [ ] Diseñar e implementar la base de datos (usuarios, cuentas, categorías, movimientos, presupuestos).
- [ ] Autenticación (registro, login, tokens).
- [ ] Endpoints CRUD de cuentas, categorías y movimientos.
- [ ] Divisas por cuenta e importes en unidad menor (ISO 4217).
- [ ] Tabla de tipos de cambio y tarea de actualización periódica.
- [ ] Endpoint de resumen / estadísticas mensuales con conversión a la divisa base.
- [ ] Presupuestos.
- [ ] Documentar la API (para que Jan y la app móvil la consuman).
- [ ] Validaciones y manejo de errores.
- [ ] Exportación CSV.

### Sugerencia de división interna

| Persona | Foco principal |
|---|---|
| Sergi | Backend (API + BD) |
| Aleix | Backend (autenticación, divisas y exportación) y apoyo a la app móvil, p. ej. iOS |

*(Ajustable entre ellos. Sin web, el backend es más pequeño y la app móvil —dos plataformas, una sola persona— es el cuello de botella: conviene que uno de los dos apoye en móvil.)*

---

## Fases del proyecto

| Fase | Contenido | Responsables |
|---|---|---|
| 1. Diseño y base | Guía de estilo + wireframes; proyectos base; modelo de datos y API definida | Pau / Jan / Sergi y Aleix |
| 2. Núcleo | Login, CRUD de movimientos, categorías y cuentas (backend + clientes) | Todos |
| 3. Valor añadido | Resumen, gráficos, presupuestos | Todos |
| 4. Cierre | Pruebas, pulido de UI, documentación, entrega | Todos |

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
