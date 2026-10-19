# Cómo contribuir

## Flujo de trabajo

> Las versiones, RCs, `develop` y `release/*` están descritos en [WORKFLOW.md](WORKFLOW.md). Las ramas de tarea salen de `develop` y los PRs van hacia `develop`.

1. **Nada directo a `main`.** Cada tarea va en su propia rama:
   - `feat/nombre-corto` → funcionalidad nueva
   - `fix/nombre-corto` → corrección
   - `docs/nombre-corto` → documentación
2. Abre un **Pull Request** hacia `main` y pide revisión a otro miembro del equipo.
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
