# Cómo contribuir

## Flujo de trabajo

> Estructura de ramas: `main` → `desarrollo/X.Y.Z/RCn` (ver [WORKFLOW.md](WORKFLOW.md)). Las ramas de tarea salen de la rama `desarrollo/X.Y.Z/RCn` abierta de la versión en curso y los PRs van hacia esa misma rama. A `main` solo llegan versiones finales.

1. **Nada directo a `main` ni a `desarrollo/**`.** Cada tarea va en su propia rama, creada desde la rama `desarrollo/X.Y.Z/RCn` abierta:
   - `feat/nombre-corto` → funcionalidad nueva
   - `fix/nombre-corto` → corrección
   - `docs/nombre-corto` → documentación
2. Abre un **Pull Request** hacia esa rama `desarrollo/X.Y.Z/RCn` y pide revisión a otro miembro del equipo.
3. Cuando esté aprobado, se fusiona con **Squash and merge** (la rama se borra sola).

## Commits

Mensajes cortos y en imperativo, con prefijo:

```
feat: añade lectura de la batería
fix: corrige el cálculo de ciclos
docs: actualiza el README
```

## Issues

- **Nueva idea** → para proponer proyectos o funcionalidades.
- **Error** → si algo no funciona.
- Asígnate el issue antes de empezar a trabajar en él para no pisarnos.

## Antes de abrir un PR

- [ ] Funciona en tu equipo y pasan el formateador, el linter y los tests.
- [ ] Has añadido o actualizado tests de lo que cambias (y de la autorización si tocas la API).
- [ ] No subes contraseñas, API keys, certificados de firma ni archivos `.env` (solo `.env.example`, sin valores).
- [ ] No registras datos personales ni financieros en logs.
- [ ] Has leído [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md) §10 si tocas autenticación, permisos o datos.
- [ ] Has actualizado la documentación si hace falta.
