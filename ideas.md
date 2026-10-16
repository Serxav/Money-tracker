# 💡 Ideas de proyecto

Lluvia de ideas para el proyecto conjunto. Criterios que buscamos:

- **Útil de verdad** → que alguien la busque, la use y la recomiende (= estrellas ⭐).
- **Suma al currículum** → que podamos explicarla en una entrevista o en el proyecto final de DAM.
- **Abarcable entre 4** → que se pueda sacar una primera versión en pocas semanas.
- **Aprovecha lo que ya sabemos** → IA y agentes, macOS / Apple (ATLAS), redes y MikroTik, Linux, iGaming.

Cada idea lleva: qué es, por qué puede tener estrellas, qué aporta al CV, stack posible y cómo repartirla entre los tres.

---

## 🥇 Top 3 recomendadas

### 1. `mac-check` — Revisión de Macs de segunda mano y diagnóstico rápido
**Qué es:** una herramienta (CLI + informe HTML) que en un minuto te dice el estado real de un Mac: ciclos y salud de batería, estado del disco (SMART), si tiene MDM o perfil de empresa, si está vinculado a Activation Lock, número de serie y modelo, garantía, sensores, etc. Pensada para quien compra un Mac usado y para técnicos.

- ⭐ **Estrellas:** comprar Macs de segunda mano es muy común y no hay una herramienta open source sencilla y en español/inglés que lo haga todo. Fácil de compartir en Reddit, Wallapop, foros de Apple.
- 📄 **CV:** demuestra conocimiento técnico real de macOS (experiencia en servicio técnico Apple + ATLAS). Muy diferenciador.
- 🛠️ **Stack:** Swift o Python + comandos de macOS (`system_profiler`, `ioreg`, `profiles`, `diskutil`), informe en HTML.
- 👥 **Reparto:** (1) lectura de hardware y batería, (2) disco, seguridad y MDM, (3) informe visual + web del proyecto.

### 2. `key-guard` — Guardián de gasto para API keys de IA
**Qué es:** un proxy local muy ligero que se pone entre tus apps y las APIs de IA (Gemini, OpenAI, Claude…). Cuenta peticiones y tokens, avisa cuando te acercas al límite y **corta** antes de que te cobren. Permite dar "sub-claves" a otras personas (un compañero, un grupo de clase) con un límite propio sin enseñarles la key real.

- ⭐ **Estrellas:** miedo universal a las facturas sorpresa de APIs; mucha gente compartiendo keys en proyectos de clase o hackathons.
- 📄 **CV:** proxies, seguridad, gestión de credenciales, IA. Muy actual.
- 🛠️ **Stack:** Python (FastAPI) o Go, SQLite, panel web mínimo.
- 👥 **Reparto:** (1) proxy y conteo de tokens por proveedor, (2) sub-claves y límites, (3) panel y alertas.

### 3. `fct-diario` — Diario de prácticas que escribe tu memoria
**Qué es:** app web donde el alumno de FP apunta en 2 minutos al día lo que ha hecho en las prácticas (FCT / formación en empresa). Al final genera automáticamente la memoria y el informe de horas con ayuda de IA, en el formato que piden los centros.

- ⭐ **Estrellas:** todos los alumnos de FP (SMX, DAM, DAW, ASIX…) tienen que hacerlo y lo odian. Fácil de difundir entre institutos.
- 📄 **CV:** producto completo (front + back + IA) que resuelve un problema real; ideal como proyecto final de DAM.
- 🛠️ **Stack:** Kotlin/Compose Multiplatform o web (React/Svelte) + backend ligero + IA para redactar.
- 👥 **Reparto:** (1) app y diseño, (2) backend y cuentas, (3) generador de memoria con IA y exportación a PDF/Word.

---

## 🧠 Más ideas

### 4. Skills y plugins para agentes de código orientados a FP
Colección de *skills* para Claude Code y otros agentes pensadas para estudiantes: "explícame este error de Java como a alguien de primero", "revisa mi práctica de bases de datos con la rúbrica", "genera tests para mi ejercicio de Kotlin".
- ⭐ El ecosistema de agentes está creciendo y casi nada está enfocado a estudiantes.
- 🛠️ Markdown + scripts, muy rápido de empezar.

### 5. `routeros-lint` — Revisor de configuraciones MikroTik
Herramienta que lee un export de RouterOS y avisa de errores típicos y fallos de seguridad (servicios abiertos, usuario admin sin contraseña, firewall vacío, firmware viejo) y compara dos backups para ver qué ha cambiado.
- ⭐ Comunidad MikroTik grande y técnica; hay pocas herramientas así abiertas.
- 📄 Redes + seguridad + parsing.
- 🛠️ Python, reglas en YAML, opcional web.

### 6. `netdoctor` — "¿Por qué no me va internet?" explicado en cristiano
CLI multiplataforma (macOS, Linux, Windows) que hace todas las comprobaciones de red (IP, puerta de enlace, DNS, MTU, Wi-Fi, proxy) y te dice en lenguaje normal qué falla y cómo arreglarlo.
- ⭐ Útil para cualquiera; perfecto para clases de SMX/ASIX.
- 🛠️ Python o Go, un único ejecutable.

### 7. `mac-setup-dam` — Prepara tu Mac/Linux para DAM con un comando
Script que instala y configura todo lo que se usa en el ciclo (JDK, Android Studio, IntelliJ, MySQL, Git, VS Code, extensiones) y deja el equipo listo para el primer día.
- ⭐ Los repos de "dotfiles/setup" suelen acumular estrellas; ahorra horas a cada alumno nuevo.
- 🛠️ Bash/Zsh + Homebrew / apt, documentación clara.

### 8. Extensión de juego responsable para iGaming
Extensión de navegador que detecta webs de casino/apuestas y muestra el tiempo y el dinero gastados en la sesión, permite poner límites propios y un "modo pausa" que bloquea esas webs X días.
- ⭐ Tema con impacto social; asociaciones y medios suelen difundir este tipo de proyectos.
- 📄 Muestra conocimiento del sector iGaming desde el lado ético.
- 🛠️ JavaScript (WebExtensions), almacenamiento local, sin servidor.

---

## 🗳️ Cómo decidir

Cada uno puntúa del 1 al 5 y sumamos:

| Idea | Sergi | Persona 2 | Persona 3 | Total |
|---|---|---|---|---|
| 1. mac-check | | | | |
| 2. key-guard | | | | |
| 3. fct-diario | | | | |
| 4. Skills FP | | | | |
| 5. routeros-lint | | | | |
| 6. netdoctor | | | | |
| 7. mac-setup-dam | | | | |
| 8. Juego responsable | | | | |

## 🚀 Para que dé estrellas (sea cual sea)
- README cuidado con GIF/captura de lo que hace en los primeros 5 segundos.
- Instalación en una línea.
- En inglés (con versión en español), para llegar a más gente.
- Licencia MIT, issues con etiqueta `good first issue`.
- Publicarlo en Reddit, Hacker News "Show HN", X y foros del sector cuando tenga la v1.
