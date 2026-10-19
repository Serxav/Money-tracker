# Workflow del proyecto

Flujo de trabajo del Money Tracker (Backend + Web + app móvil Android/iOS): ramas, versiones, Release Candidates (RC), entornos, CI y changelog.

Complementa a [CONTRIBUTING.md](CONTRIBUTING.md) (reglas básicas de ramas, commits y PRs). Donde haya diferencias, **manda este documento**.

## 1. Resumen

```
feat/*  fix/*  docs/*
      \   |   /
       develop ──────────┬───────────────────────────────►  (integración, entorno dev)
                         │ corte de versión
                    release/0.2 ── rc.1 ── rc.2 ── final ──►  (estabilización, entorno staging)
                                     │      │        │
                                  v0.2.0-rc.1 …   v0.2.0
                                                     │
 main ───────────────────────────────────────────────┴────►  (solo versiones finales, entorno prod)
```

| Rama | Contenido | Entorno | Quién escribe |
|---|---|---|---|
| `main` | Solo versiones finales (`vX.Y.Z`). Siempre desplegable. | prod | Merge desde `release/*` o `hotfix/*` |
| `develop` | Siguiente versión en construcción. | dev | PRs de `feat/*`, `fix/*`, `docs/*` |
| `release/X.Y` | Versión congelada en estabilización (RCs). | staging | Solo fixes de estabilización |
| `feat/*`, `fix/*`, `docs/*` | Trabajo de cada tarea. | — | Quien tenga la tarea |
| `hotfix/X.Y.Z` | Arreglo urgente sobre producción. | — | Quien lo detecte |

Regla de oro: **nada directo a `main`, `develop` ni `release/*`**. Todo entra por Pull Request con revisión.

## 2. Versionado

Usamos [SemVer](https://semver.org/lang/es/) con **una única versión para todo el producto** (backend, web y APK comparten número): `MAJOR.MINOR.PATCH`.

| Parte | Cuándo sube |
|---|---|
| `MAJOR` | Cambio incompatible (p. ej. API que rompe clientes). `1.0.0` = primera versión estable. |
| `MINOR` | Nuevas funcionalidades compatibles (un hito). |
| `PATCH` | Solo correcciones. |

Mientras estemos en `0.x.y` el producto se considera en desarrollo: los `MINOR` pueden incluir cambios incompatibles.

### Tags

| Tag | Significado |
|---|---|
| `v0.2.0-rc.1` | Primer Release Candidate de la 0.2.0 |
| `v0.2.0-rc.2` | Segundo RC (tras corregir fallos del anterior) |
| `v0.2.0` | Versión final |

La versión única cubre **backend, web y la app móvil (Android e iOS)**: todas salen con el mismo número.

Los tags son inmutables: si un RC falla, se crea `rc.N+1`, nunca se reescribe un tag.

### Hitos (propuesta inicial)

| Versión | Objetivo |
|---|---|
| `0.1.0` | Login, cuentas (con divisa), categorías y CRUD de movimientos (backend + web + Android + iOS) |
| `0.2.0` | Resumen mensual y gráficos, con tipos de cambio y conversión a la divisa base |
| `0.3.0` | Presupuestos y avisos |
| `0.4.0` | Exportación CSV y pulido de UI |
| `1.0.0` | Versión estable de entrega (Backend + Web + Android + iOS) |

La cadencia es **por hito**: se corta un RC cuando todas las issues del hito están cerradas en `develop`.

## 3. Trabajo diario (features y fixes)

1. Asígnate una **issue** (nadie trabaja sin issue asignada).
2. Crea la rama **desde `develop`**:
   ```bash
   git checkout develop && git pull
   git checkout -b feat/lista-movimientos
   ```
   Prefijos: `feat/`, `fix/`, `docs/`.
3. Haz commits pequeños con prefijo (ver §8).
4. Abre un **PR hacia `develop`** rellenando la plantilla y enlazando la issue (`Closes #N`).
5. Pasa la CI y obtén **al menos 1 aprobación** de otra persona.
6. Se fusiona con **Squash and merge**; la rama se borra.

> El PR de una persona lo revisa otra distinta. Para cambios de API, revisa también quien consume esa API (Jan o quien haga la web).

## 4. Ciclo de una release

### 4.1 Corte de versión

Cuando el hito está completo en `develop`:

```bash
git checkout develop && git pull
git checkout -b release/0.2
git push -u origin release/0.2
```

- `develop` queda libre para seguir con el siguiente hito.
- En `release/0.2` **solo se admiten correcciones** (nada de funcionalidades nuevas).
- Se actualiza el número de versión en los proyectos (backend, web, móvil) y se prepara el `CHANGELOG.md` (§7).

### 4.2 Release Candidate

```bash
git tag -a v0.2.0-rc.1 -m "Release candidate 0.2.0-rc.1"
git push origin v0.2.0-rc.1
```

El tag dispara la CI, que construye los artefactos y **despliega en staging** (§6).

### 4.3 QA del RC

- Se ejecuta el checklist de §5 sobre staging.
- Los fallos se registran como issues con la etiqueta `rc-blocker` (impide publicar) o `rc-minor` (se puede posponer).
- Cada fix va en una rama `fix/*` **hacia `release/0.2`** (PR con Squash and merge) y se vuelve a integrar en `develop` (§4.5).
- Con los blockers corregidos se crea el siguiente RC: `v0.2.0-rc.2`, y se repite el QA.

### 4.4 Publicación final

Un RC se promueve a final cuando **cumple los criterios de aceptación** (§5) sin blockers abiertos:

1. PR de `release/0.2` → `main` (aprobación de todo el equipo o de al menos 2 personas).
2. Tras el merge, se etiqueta `main`:
   ```bash
   git checkout main && git pull
   git tag -a v0.2.0 -m "Release 0.2.0"
   git push origin v0.2.0
   ```
3. Se crea la **GitHub Release** con las notas del changelog y el APK adjunto.
4. Se despliega backend y web en **prod**.

### 4.5 Sincronizar con develop

Los fixes hechos en la release deben volver a `develop`:

```bash
git checkout develop && git pull
git merge --no-ff main     # o release/0.2 si aún no se ha fusionado
git push
```

Se hace mediante PR si `develop` está protegida. Conviene hacerlo **inmediatamente tras publicar** para no perder correcciones.

## 5. QA y criterios de aceptación de un RC

### Criterios para publicar

- [ ] La CI está en verde sobre el commit del RC.
- [ ] No hay issues `rc-blocker` abiertas.
- [ ] El checklist de QA se ha completado en staging.
- [ ] El `CHANGELOG.md` está actualizado.
- [ ] La versión es coherente en backend, web, Android e iOS.
- [ ] Las migraciones de base de datos se han probado sobre una copia de datos reales/semilla.

### Checklist de QA (plantilla por RC)

Copia esto en una issue `QA vX.Y.Z-rc.N` y marca lo verificado.

**Backend**
- [ ] Registro, login y sesión caducada
- [ ] CRUD de cuentas, categorías y movimientos
- [ ] Resumen mensual con datos coherentes
- [ ] Errores de validación con mensajes claros
- [ ] Migración de BD desde la versión anterior

**Web**
- [ ] Flujos principales en Chrome y Firefox
- [ ] Responsive (móvil y escritorio)
- [ ] Estados vacíos y de error
- [ ] Coincide con el diseño de Pau

**Android**
- [ ] Instalación del APK limpia y actualización sobre la versión anterior
- [ ] Flujos principales en un dispositivo real y en emulador

**iOS**
- [ ] Instalación por TestFlight limpia y actualización sobre la versión anterior
- [ ] Flujos principales en un iPhone real y en simulador
- [ ] Comportamiento sin conexión / con error de red
- [ ] Coincide con el diseño de Pau y con las guías de Apple
- [ ] Comportamiento sin conexión / con error de red
- [ ] Coincide con el diseño de Pau

**Integración**
- [ ] Un movimiento creado en web aparece en Android, iOS y web, y viceversa
- [ ] Mismos totales en ambos clientes

**Divisas**
- [ ] Cuentas en divisas de 0, 2 y 3 decimales muestran bien los importes
- [ ] Totales convertidos a la divisa base coinciden en web, Android e iOS
- [ ] Si fallan los tipos de cambio, la app sigue funcionando y lo indica

## 6. Entornos

| Entorno | Rama / tag | Despliegue | Datos | Uso |
|---|---|---|---|---|
| **dev** | `develop` | Automático en cada merge | De prueba, se pueden borrar | Integración continua entre el equipo |
| **staging** | `release/*` (tags `-rc.N`) | Automático al crear un tag RC | Copia anonimizada o semilla | QA del RC |
| **prod** | `main` (tags `vX.Y.Z`) | Al publicar la versión final | Reales | Usuarios |

Reglas:
- Un RC solo se despliega en staging; **nunca en prod**.
- Los secretos (claves, contraseñas, `.env`) viven en los *secrets* de GitHub o del hosting, nunca en el repo.
- El APK de cada RC se adjunta como artefacto de la CI para que Jan y los testers lo instalen.

> Pendiente de decidir: dónde se alojan los entornos (hosting del backend y de la web).

## 7. Changelog

Se mantiene en `CHANGELOG.md` con el formato [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

```markdown
## [Unreleased]

## [0.2.0] - 2026-11-20
### Añadido
- Resumen mensual con gráficos por categoría (#12)
### Corregido
- Cálculo de balance con movimientos del mes anterior (#31)
```

- Los PRs `feat:` van en **Añadido/Cambiado**, los `fix:` en **Corregido**.
- Cada PR relevante añade su línea bajo `[Unreleased]`, o bien quien corta la release la genera a partir de los commits.
- Al cortar `release/X.Y`, `[Unreleased]` pasa a la sección de la versión con su fecha.
- Los RC **no** tienen sección propia: se acumulan en la de la versión.

## 8. Commits

Imperativo, corto y con prefijo (como en `CONTRIBUTING.md`):

```
feat: añade lista de movimientos
fix: corrige el cálculo del balance
docs: actualiza el workflow
chore: actualiza dependencias
test: añade tests de categorías
ci: ejecuta tests en los PR
```

Tras el Squash and merge, el título del PR se convierte en el mensaje del commit, así que **el título del PR debe seguir este formato**. Un cambio incompatible se marca con `!`: `feat!: cambia el formato de /transactions`.

## 9. Hotfix

Para un error grave en producción:

1. Rama desde `main`: `git checkout -b hotfix/0.2.1 main`.
2. Corrige, actualiza el changelog y abre PR hacia `main`.
3. Etiqueta `v0.2.1` (si hay tiempo, antes `v0.2.1-rc.1` en staging).
4. Fusiona también en `develop` (y en la `release/*` abierta, si existe).

## 10. CI con GitHub Actions

| Evento | Qué ejecuta |
|---|---|
| PR hacia `develop`, `release/*` o `main` | Lint + tests de backend, web y móvil; build de comprobación |
| Merge en `develop` | Lo anterior + despliegue en **dev** |
| Tag `vX.Y.Z-rc.N` | Tests + build + despliegue en **staging** + APK como artefacto |
| Tag `vX.Y.Z` | Build + despliegue en **prod** + GitHub Release con APK |

Los *checks* de la CI son obligatorios para fusionar un PR.

## 11. Protección de ramas (configuración en GitHub)

| Rama | Reglas |
|---|---|
| `main` | PR obligatorio, 2 aprobaciones, CI verde, sin push directo, solo desde `release/*` y `hotfix/*` |
| `develop` | PR obligatorio, 1 aprobación, CI verde, sin push directo |
| `release/*` | PR obligatorio, 1 aprobación, CI verde |
| Tags `v*` | Solo las crean las personas con permiso de mantenimiento |

## 12. Roles

| Rol | Quién | Responsabilidad |
|---|---|---|
| Release manager | Rotativo por versión | Corta la release, etiqueta los RC, coordina el QA y publica |
| Backend | Sergi y Aleix | API, BD, despliegue de staging/prod |
| Web | Sergi y Aleix | Cliente web |
| Móvil (Android + iOS) | Jan | APK, TestFlight y pruebas en dispositivo |
| UI | Pau | Revisión visual de cada RC |

## 13. Puesta en marcha (una sola vez)

- [ ] Crear la rama `develop` a partir de `main` y hacerla rama por defecto de los PRs.
- [ ] Configurar la protección de ramas de §11.
- [ ] Crear `CHANGELOG.md` con la sección `[Unreleased]`.
- [ ] Crear los workflows de GitHub Actions de §10.
- [ ] Crear las etiquetas `rc-blocker` y `rc-minor` en GitHub.
- [ ] Decidir dónde se alojan staging y prod.
- [ ] Actualizar la plantilla de PR para apuntar a `develop`.
