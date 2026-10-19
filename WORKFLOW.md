# Workflow del proyecto

Flujo de trabajo del Money Tracker (Backend + app móvil Android/iOS): ramas, versiones, Release Candidates (RC), entornos, CI y changelog. Estructura de ramas: `main` → `desarrollo/X.Y.Z/RCn`.

Complementa a [CONTRIBUTING.md](CONTRIBUTING.md) (reglas básicas de ramas, commits y PRs). Donde haya diferencias, **manda este documento**.

## 1. Resumen

```
main                                   solo versiones finales (vX.Y.Z) · entorno prod
└── desarrollo/
    └── 0.1.0/
        ├── RC1    ← feat/*, fix/*, docs/*   integración de la versión · entorno dev
        │            tag v0.1.0-rc.1 → QA en staging
        └── RC2    ← fix/* (si RC1 no pasa la QA)
                     tag v0.1.0-rc.2 → QA ─┐
                                           └─► PR a main · tag v0.1.0
```

| Rama | Contenido | Entorno | Quién escribe |
|---|---|---|---|
| `main` | Solo versiones finales (`vX.Y.Z`). Siempre desplegable. | prod | PR desde la última `desarrollo/X.Y.Z/RCn` aprobada o desde `hotfix/*` |
| `desarrollo/X.Y.Z/RCn` | La versión `X.Y.Z` en construcción y su candidata número `n`. | dev | PRs de `feat/*`, `fix/*`, `docs/*`, `chore/*`, `ci/*` |
| `feat/*`, `fix/*`, `docs/*`, `chore/*`, `ci/*` | Trabajo de cada tarea. | — | Quien tenga la tarea |
| `hotfix/X.Y.Z` | Arreglo urgente sobre producción. | — | Quien lo detecte |

Reglas:

- **Nada directo a `main` ni a `desarrollo/**`**. Todo entra por Pull Request con revisión.
- Solo hay **una rama `desarrollo/X.Y.Z/RCn` abierta por versión**: la del RC más alto. Las anteriores quedan congeladas como histórico.
- Git no permite tener a la vez una rama `desarrollo/0.1.0` y otra `desarrollo/0.1.0/RC1` (una ruta no puede ser rama y "carpeta" a la vez): por eso **no se crean ramas `desarrollo/X.Y.Z`** sueltas, siempre con su `/RCn`.

## 2. Versionado

Usamos [SemVer](https://semver.org/lang/es/) con **una única versión para todo el producto** (backend y app móvil comparten número): `MAJOR.MINOR.PATCH`.

| Parte | Cuándo sube |
|---|---|
| `MAJOR` | Cambio incompatible (p. ej. API que rompe clientes). `1.0.0` = primera versión estable. |
| `MINOR` | Nuevas funcionalidades compatibles (un hito). |
| `PATCH` | Solo correcciones. |

Mientras estemos en `0.x.y` el producto se considera en desarrollo: los `MINOR` pueden incluir cambios incompatibles.

La versión única cubre **backend y la app móvil (Android e iOS)**: todas salen con el mismo número.

### Ramas y tags

| Rama | Tag que la cierra | Significado |
|---|---|---|
| `desarrollo/0.2.0/RC1` | `v0.2.0-rc.1` | Primer Release Candidate de la 0.2.0 |
| `desarrollo/0.2.0/RC2` | `v0.2.0-rc.2` | Segundo RC, tras corregir los fallos de la QA del primero |
| `main` | `v0.2.0` | Versión final |

El número de la rama y el del tag van siempre a la par (`RCn` ↔ `rc.n`). Los tags son inmutables: si un RC falla, se crea el siguiente; nunca se reescribe un tag.

### Documento de scope por versión

**Cada versión tiene su `docs/versiones/X.Y.Z.md`** con lo que entra, lo que no entra (y cuándo se espera), las limitaciones conocidas, los criterios de aceptación y la compatibilidad. Se parte de [`docs/versiones/PLANTILLA.md`](docs/versiones/PLANTILLA.md):

1. Se **crea en `desarrollo/X.Y.Z/RC1`** al empezar el hito, con el scope previsto.
2. Se **actualiza** si el scope cambia durante el desarrollo (mediante PR, como cualquier cambio).
3. Se **cierra** al etiquetar cada RC: refleja lo que realmente entra y su estado (RCn o publicada).
4. Un RC o una versión final **no se publica sin su documento de scope** (ver §5). Junto a él se guarda el informe de QA (`docs/versiones/X.Y.Z-qa.md`).

### Hitos (propuesta inicial)

| Versión | Objetivo |
|---|---|
| `0.1.0` | Login, cuentas (con divisa), categorías y CRUD de movimientos (backend + Android + iOS) |
| `0.2.0` | Resumen mensual y gráficos, con tipos de cambio y conversión a la divisa base |
| `0.3.0` | Presupuestos y avisos |
| `0.4.0` | Exportación CSV y pulido de UI |
| `1.0.0` | Versión estable de entrega (Backend + Android + iOS) |

La cadencia es **por hito**: se etiqueta el RC cuando todas las issues del hito están cerradas en la rama `desarrollo/X.Y.Z/RCn`.

## 3. Trabajo diario (features y fixes)

1. Asígnate una **issue** (nadie trabaja sin issue asignada).
2. Crea la rama **desde la rama de desarrollo abierta** de la versión (p. ej. `desarrollo/0.2.0/RC1`):
   ```bash
   git checkout desarrollo/0.2.0/RC1 && git pull
   git checkout -b feat/lista-movimientos
   ```
   Prefijos: `feat/`, `fix/`, `docs/`, `chore/`, `ci/`.
3. Haz commits pequeños con prefijo (ver §8).
4. Abre un **PR hacia esa rama `desarrollo/X.Y.Z/RCn`** rellenando la plantilla y enlazando la issue (`Closes #N`).
5. Pasa la CI y obtén **al menos 1 aprobación** de otra persona.
6. Se fusiona con **Squash and merge**; la rama de la tarea se borra.

> El PR de una persona lo revisa otra distinta. Para cambios de API, revisa también quien consume esa API (Jan).

## 4. Ciclo de una versión

### 4.1 Empezar una versión

Cuando la versión anterior está publicada en `main`:

```bash
git checkout main && git pull
git checkout -b desarrollo/0.2.0/RC1
git push -u origin desarrollo/0.2.0/RC1
```

- Se crea en ella `docs/versiones/0.2.0.md` con el scope previsto (§2).
- Se sube la versión de los proyectos a `0.2.0-SNAPSHOT` (backend) / `0.2.0` (app).
- Se apunta Dependabot (`.github/dependabot.yml`, `target-branch`) a la nueva rama.

### 4.2 Release Candidate

Cuando el hito está completo en `desarrollo/0.2.0/RC1`:

1. Se cierra la versión en esa rama (PR): versión sin `-SNAPSHOT`, `CHANGELOG.md` (§7) y documento de scope.
2. Se etiqueta el último commit de la rama:
   ```bash
   git checkout desarrollo/0.2.0/RC1 && git pull
   git tag -a v0.2.0-rc.1 -m "Release candidate 0.2.0-rc.1"
   git push origin v0.2.0-rc.1
   ```
3. El tag dispara la CI, que construye los artefactos y **despliega en staging** (§6).

### 4.3 QA del RC

- Se ejecuta el checklist de §5 sobre staging y se documenta en `docs/versiones/X.Y.Z-qa.md`.
- Los fallos se registran como issues con la etiqueta `rc-blocker` (impide publicar) o `rc-minor` (se puede posponer).
- **Si el RC no pasa la QA**, se abre la siguiente candidata desde la anterior y los arreglos van allí:
  ```bash
  git checkout desarrollo/0.2.0/RC1 && git pull
  git checkout -b desarrollo/0.2.0/RC2
  git push -u origin desarrollo/0.2.0/RC2
  ```
  Cada arreglo va en una rama `fix/*` con PR **hacia `desarrollo/0.2.0/RC2`**. En esta rama **solo se admiten correcciones** (nada de funcionalidades nuevas). Con los blockers corregidos se etiqueta `v0.2.0-rc.2` y se repite la QA.

### 4.4 Publicación final

Un RC se promueve a final cuando **cumple los criterios de aceptación** (§5) sin blockers abiertos:

1. PR de la última `desarrollo/X.Y.Z/RCn` → `main` (aprobación de al menos 2 personas).
2. Tras el merge, se etiqueta `main`:
   ```bash
   git checkout main && git pull
   git tag -a v0.2.0 -m "Release 0.2.0"
   git push origin v0.2.0
   ```
3. Se crea la **GitHub Release** con las notas del changelog y el APK adjunto.
4. Se despliega el backend en **prod** y se publica la app móvil (APK/AAB y App Store, o la vía de distribución acordada).
5. Se empieza la siguiente versión desde `main` (§4.1). Las ramas `desarrollo/X.Y.Z/RCn` ya publicadas se conservan como histórico (o se borran si el equipo lo prefiere; los tags se mantienen siempre).

## 5. QA y criterios de aceptación de un RC

### Criterios para publicar

- [ ] La CI está en verde sobre el commit del RC.
- [ ] No hay issues `rc-blocker` abiertas.
- [ ] El checklist de QA se ha completado en staging.
- [ ] El `CHANGELOG.md` está actualizado.
- [ ] Existe `docs/versiones/X.Y.Z.md` con el scope real de la versión.
- [ ] La versión es coherente en backend, Android e iOS.
- [ ] Las migraciones de base de datos se han probado sobre una copia de datos reales/semilla.
- [ ] No hay secretos en Git ni en el paquete distribuido, y las dependencias se han analizado.
- [ ] Hay copias de seguridad y se ha probado su restauración.
- [ ] Políticas de privacidad y términos de uso publicados y correctos; declaraciones de privacidad de las tiendas revisadas.
- [ ] Existe un plan para monitorizar la versión tras publicarla y mitigar una release defectuosa.

### Checklist de QA (plantilla por RC)

Copia esto en una issue `QA vX.Y.Z-rc.N` y marca lo verificado.

**Backend**
- [ ] Registro, login y sesión caducada
- [ ] CRUD de cuentas, categorías y movimientos
- [ ] Resumen mensual con datos coherentes
- [ ] Errores de validación con mensajes claros
- [ ] Migración de BD desde la versión anterior

**Android**
- [ ] Instalación del APK limpia y actualización sobre la versión anterior
- [ ] Flujos principales en un dispositivo real y en emulador
- [ ] Comportamiento sin conexión / con error de red
- [ ] Coincide con el diseño de Pau

**iOS**
- [ ] Instalación por TestFlight limpia y actualización sobre la versión anterior
- [ ] Flujos principales en un iPhone real y en simulador
- [ ] Comportamiento sin conexión / con error de red
- [ ] Coincide con el diseño de Pau y con las guías de Apple

**Seguridad, accesibilidad y rendimiento (ambas plataformas y backend)**
- [ ] Puntos de seguridad de [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md) §10 verificados
- [ ] Tests de autorización pasan (un usuario no accede a datos de otro)
- [ ] Conexión lenta, desconexión, sesión caducada y respuestas inesperadas probadas
- [ ] TalkBack (Android) y VoiceOver (iOS) permiten completar los flujos principales; texto escalable y contraste correctos
- [ ] Arranque, memoria, red y batería medidos en un dispositivo modesto y sin degradación respecto al RC anterior
- [ ] Probado en la versión mínima y en la última de Android e iOS

**Integración**
- [ ] Un movimiento creado en Android aparece en iOS y viceversa
- [ ] Mismos totales en ambos clientes

**Divisas**
- [ ] Cuentas en divisas de 0, 2 y 3 decimales muestran bien los importes
- [ ] Totales convertidos a la divisa base coinciden en Android e iOS
- [ ] Si fallan los tipos de cambio, la app sigue funcionando y lo indica

## 6. Entornos

| Entorno | Rama / tag | Despliegue | Datos | Uso |
|---|---|---|---|---|
| **dev** | `desarrollo/X.Y.Z/RCn` abierta | Automático en cada merge | De prueba, se pueden borrar | Integración continua entre el equipo |
| **staging** | Tags `vX.Y.Z-rc.N` | Automático al crear un tag RC | Copia anonimizada o semilla | QA del RC |
| **prod** | `main` (tags `vX.Y.Z`) | Al publicar la versión final | Reales | Usuarios |

Reglas:
- Un RC solo se despliega en staging; **nunca en prod**.
- Los secretos (claves, contraseñas, `.env`) viven en los *secrets* de GitHub o del hosting, nunca en el repo. Se versiona `.env.example` (sin valores) y cada entorno tiene credenciales distintas.
- La configuración de desarrollo y la de producción están separadas; en producción, *debug* y mensajes de error detallados desactivados.
- El APK de cada RC se adjunta como artefacto de la CI para que Jan y los testers lo instalen.

> Pendiente de decidir: dónde se alojan los entornos (hosting del backend y distribución de la app móvil).

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
- Al etiquetar el primer RC de la versión, `[Unreleased]` pasa a la sección de la versión con su fecha.
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
4. Lleva también el arreglo a la rama `desarrollo/X.Y.Z/RCn` abierta, si existe (PR).

## 10. CI con GitHub Actions

| Evento | Qué ejecuta |
|---|---|
| PR hacia `desarrollo/**` o `main` | Formato + lint + comprobación de tipos, tests de backend y móvil, análisis estático (p. ej. CodeQL), análisis de dependencias, escaneo de secretos, validación del `openapi.yaml`; build de comprobación |
| Merge en `desarrollo/**` | Lo anterior + despliegue en **dev** |
| Tag `vX.Y.Z-rc.N` | Tests + build + despliegue en **staging** + APK como artefacto |
| Tag `vX.Y.Z` | Build + despliegue en **prod** + GitHub Release con APK |

Los *checks* de la CI son obligatorios para fusionar un PR.

## 11. Protección de ramas (configuración en GitHub)

| Rama | Reglas |
|---|---|
| `main` | PR obligatorio, 2 aprobaciones, CI verde, sin push directo, solo desde `desarrollo/**` y `hotfix/*` |
| `desarrollo/**` | PR obligatorio, 1 aprobación, CI verde, sin push directo ni borrado |
| Tags `v*` | Solo las crean las personas con permiso de mantenimiento |

## 12. Roles

| Rol | Quién | Responsabilidad |
|---|---|---|
| Release manager | Rotativo por versión | Abre las ramas `desarrollo/X.Y.Z/RCn`, etiqueta los RC, coordina el QA y publica |
| Backend | Sergi y Aleix | API, BD, despliegue de staging/prod, apoyo a móvil |
| Móvil (Android + iOS) | Jan, con apoyo de Sergi y Aleix | APK, TestFlight y pruebas en dispositivo |
| UI | Pau | Revisión visual de cada RC |

## 13. Puesta en marcha (una sola vez)

- [x] Crear la rama `desarrollo/0.1.0/RC1` a partir de `main`.
- [ ] Configurar la protección de ramas de §11.
- [ ] Crear `CHANGELOG.md` con la sección `[Unreleased]`.
- [ ] Crear los workflows de GitHub Actions de §10.
- [ ] Crear las etiquetas `rc-blocker` y `rc-minor` en GitHub.
- [ ] Decidir dónde se alojan staging y prod.
- [x] Actualizar la plantilla de PR para apuntar a `desarrollo/X.Y.Z/RCn`.
- [ ] Añadir `.env.example` y archivos de bloqueo de dependencias (*lockfiles*) a cada proyecto.
- [ ] Activar Dependabot, *secret scanning* y *push protection* en GitHub.
- [ ] Configurar formateador, linter y comprobación de tipos en backend y móvil, y exigirlos en la CI.
- [ ] Crear `docs/adr/` para las decisiones técnicas.
- [ ] Preparar cuentas y requisitos de las tiendas (Google Play Console y Apple Developer) y el almacén seguro de firma.

## 14. Calidad, testing y rendimiento

### Calidad del código

- Formateador, linter y análisis estático obligatorios en la CI; comprobación de tipos activada.
- Evitar valores mágicos, código muerto y dependencias innecesarias; funciones y componentes con una sola responsabilidad.
- No ocultar errores con bloques vacíos ni capturas genéricas.
- Refactorizar de forma incremental y con tests; revisar todo con Pull Request.
- Dependencias actualizadas, con mantenimiento y licencia comprobados (ver [SEGURIDAD_Y_ETICA.md](SEGURIDAD_Y_ETICA.md) §9).
- Las decisiones y limitaciones relevantes se documentan en un ADR (`docs/adr/`).

### Tipos de pruebas

| Tipo | Qué cubre | Dónde |
|---|---|---|
| Unitarias | Reglas de negocio: cálculo de balances, presupuestos, redondeo y conversión de divisas (0, 2 y 3 decimales) | Backend y móvil |
| Integración | Módulos, base de datos y servicios (incl. proveedor de tipos de cambio simulado) | Backend |
| API / contrato | Contrato OpenAPI, errores, validación y **autorización** | Backend |
| Interfaz | Flujos críticos: registro/login, crear movimiento, ver resumen | Móvil |
| Regresión | Errores ya corregidos no reaparecen | Ambos |
| Seguridad | Acceso horizontal/vertical, *mass assignment*, inyección, límites | Backend |
| Rendimiento | Arranque, latencia, memoria, batería, red; carga del backend antes de la `1.0.0` | Ambos |
| Accesibilidad | TalkBack, VoiceOver, escalado de texto, contraste | Móvil |
| Dispositivos reales | Versión mínima y última de Android/iOS, gama baja, redes lentas | Móvil |

- Datos de prueba **reproducibles** y ficticios.
- Una cobertura alta no garantiza ausencia de defectos: priorizar los flujos críticos y las reglas de dinero.
- Todo PR con lógica nueva incluye sus tests.

### Rendimiento

- Medir antes de optimizar; no hacer suposiciones.
- No ejecutar trabajo pesado en el hilo principal; cancelar tareas y liberar recursos.
- Evitar renderizados, consultas y peticiones duplicadas; paginar listas.
- Reducir tamaño del paquete y carga inicial; limitar consumo de batería y de segundo plano.
- Probar en dispositivos modestos y con red lenta.

## 15. Publicación y despliegue de la app móvil

- Builds **reproducibles** (dependencias bloqueadas) y versión registrada en cada artefacto.
- **Firma y publicación restringidas** a pocas personas; claves fuera del repo y con copia segura.
- Migraciones del backend probadas en staging antes de producción y compatibles con la versión anterior de la app.
- **Publicación gradual** (despliegue por porcentajes en Google Play y publicación por fases en App Store) cuando sea posible.
- **Monitorizar la versión tras publicarla** (errores, latencia, disponibilidad) y tener un plan de mitigación.
- **Una versión móvil instalada no se puede revertir de forma instantánea**: el backend mantiene compatibilidad con versiones anteriores de la app (`/v1`), y un fallo grave se corrige con un hotfix (§9) y un ajuste en el servidor si es posible.
- Revisar requisitos de privacidad y publicación de App Store y Google Play; preparar iconos, capturas, descripciones y fichas.
- Probar *deep links* y notificaciones con la app abierta, cerrada y en segundo plano (si se usan).
- Documentar las releases y los cambios incompatibles en el `CHANGELOG.md`.

## 16. Prioridades por fase

| Cuándo | Qué hacer |
|---|---|
| **Desde el primer día** | Git y `.gitignore`; secretos fuera del repo; arquitectura clara; validación de entradas; autorización en servidor; permisos de BD (y RLS si aplica); manejo de errores; tests básicos |
| **Antes de publicar (RC / `1.0.0`)** | CI/CD completa; análisis de dependencias; *rate limiting*; HTTPS; logs seguros; accesibilidad; privacidad y políticas legales; pruebas en dispositivos reales; fichas de las tiendas |
| **Cuando crezca el producto** | CDN, colas y cachés avanzadas; pruebas de carga; despliegues graduales; observabilidad avanzada; optimización de infraestructura |
