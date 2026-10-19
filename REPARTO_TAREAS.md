# Reparto de tareas

| Área | Responsable |
|---|---|
| **UI (Interfaz de usuario)** | Pau |
| **Aplicación Android** | Jan |
| **Backend + Desktop** | Sergi y Aleix |

---

## Pau — UI

Diseño de la interfaz para Desktop y Android.

- [ ] Definir guía de estilo (colores, tipografía, iconos, componentes).
- [ ] Wireframes de las pantallas principales.
- [ ] Mockups finales (versión móvil y versión escritorio).
- [ ] Pantallas: login/registro, inicio con resumen, lista de movimientos, añadir/editar movimiento, categorías y cuentas, estadísticas, presupuestos, ajustes.
- [ ] Diseño de gráficos y estados vacíos / de error.
- [ ] Modo claro / oscuro (opcional).
- [ ] Entregar assets (iconos, logo) a Jan, Sergi y Aleix en `design/`.
- [ ] Revisar que las implementaciones respetan el diseño.

## Jan — App móvil (Android)

Implementación de la aplicación móvil siguiendo el diseño de Pau.

- [ ] Crear proyecto Android y estructura base.
- [ ] Implementar las pantallas diseñadas por Pau.
- [ ] Conexión con la API del backend (login, movimientos, categorías, cuentas).
- [ ] Gestión de sesión del usuario.
- [ ] CRUD de movimientos, categorías y cuentas.
- [ ] Pantalla de resumen y gráficos.
- [ ] Presupuestos y avisos.
- [ ] Manejo de errores y estados sin conexión.
- [ ] Pruebas en dispositivo / emulador y generación del APK.

## Sergi y Aleix — Backend + Desktop

### Backend

- [ ] Elegir tecnología y crear proyecto base en `backend/`.
- [ ] Diseñar e implementar la base de datos (usuarios, cuentas, categorías, movimientos, presupuestos).
- [ ] Autenticación (registro, login, tokens).
- [ ] Endpoints CRUD de cuentas, categorías y movimientos.
- [ ] Endpoint de resumen / estadísticas mensuales.
- [ ] Presupuestos.
- [ ] Documentar la API (para que Jan y la app Desktop la consuman).
- [ ] Validaciones y manejo de errores.
- [ ] Exportación CSV.

### Desktop

- [ ] Elegir tecnología y crear proyecto base en `desktop/`.
- [ ] Implementar las pantallas diseñadas por Pau.
- [ ] Conexión con la API del backend.
- [ ] CRUD de movimientos, categorías y cuentas.
- [ ] Resumen, gráficos y presupuestos.
- [ ] Empaquetado / instalador.

### Sugerencia de división interna

| Persona | Foco principal |
|---|---|
| Sergi | Backend (API + BD) |
| Aleix | Desktop (con apoyo en backend) |

*(Ajustable entre ellos según carga de trabajo.)*

---

## Fases del proyecto

| Fase | Contenido | Responsables |
|---|---|---|
| 1. Diseño y base | Guía de estilo + wireframes; proyectos base; modelo de datos y API definida | Pau / Jan / Sergi y Aleix |
| 2. Núcleo | Login, CRUD de movimientos, categorías y cuentas (backend + clientes) | Todos |
| 3. Valor añadido | Resumen, gráficos, presupuestos | Todos |
| 4. Cierre | Pruebas, pulido de UI, documentación, entrega | Todos |

## Dependencias entre áreas

- **Jan** y **Sergi/Aleix (Desktop)** necesitan los diseños de **Pau** para construir las pantallas.
- **Jan** y **Desktop** necesitan la **API documentada** del backend; mientras tanto pueden trabajar con datos de prueba.
- Acordar pronto el contrato de la API (rutas y formato JSON) para trabajar en paralelo.

## Comunicación

- Reunión breve semanal para revisar avance y bloqueos.
- Tareas y bugs en **Issues** de GitHub; una issue por tarea, asignada a su responsable.
- Cambios mediante Pull Request (ramas `feat/`, `fix/`, `docs/` y commits con prefijo; ver [CONTRIBUTING.md](CONTRIBUTING.md)).
