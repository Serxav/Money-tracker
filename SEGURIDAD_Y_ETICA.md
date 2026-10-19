# Seguridad, privacidad y ética

Guía para los programadores del Money Tracker (Backend y app móvil Android/iOS). Los datos financieros personales son **sensibles**: aunque no manejamos dinero real ni conectamos con bancos, un movimiento revela hábitos, salud, ideología o situación económica de una persona. Trátalos con ese nivel de cuidado desde el primer commit.

> Es una lista de requisitos mínimos, no un documento legal. Los puntos de RGPD deben contrastarse con el texto oficial y, si el proyecto sale del ámbito académico, con un profesional.

## Cómo usar este documento

- Léelo antes de empezar tu primera tarea.
- Cada PR debe cumplir la **checklist de §10**.
- Si dudas entre comodidad y seguridad/privacidad, gana la seguridad/privacidad. Si algo no está claro, abre una issue.
- Los requisitos se marcan con **DEBE** (obligatorio), **DEBERÍA** (recomendado).

---

## 1. Principios

0. **La app móvil es un cliente no confiable**: las validaciones y restricciones del cliente mejoran la experiencia, pero la seguridad y la autorización se imponen en el servidor. Quien ataca puede modificar la app, interceptar el tráfico o llamar a la API directamente.
1. **Mínimo dato necesario**: no recojas, guardes ni registres en logs nada que la funcionalidad no necesite.
2. **Seguridad por diseño y por defecto**: la opción más privada y segura es la que viene activada.
3. **Defensa en profundidad**: nunca dependas de una sola barrera (p. ej. la app oculta un botón *y* el backend valida el permiso).
4. **El cliente no es de fiar**: toda validación y toda autorización se hace en el backend.
5. **Transparencia**: la persona usuaria sabe qué datos tenemos, para qué y puede llevárselos o borrarlos.

---

## 2. Datos que manejamos

| Dato | Sensibilidad | Notas |
|---|---|---|
| Email, nombre | Personal | Identifican a la persona |
| Contraseña | Crítica | Nunca en claro; solo su hash |
| Movimientos (importe, fecha, descripción) | Alta | Las descripciones libres pueden contener datos de terceros o de salud |
| Cuentas y saldos | Alta | **No** guardar números reales de cuenta/tarjeta (IBAN, PAN, CVV) |
| Tokens de sesión | Crítica | Equivalen a la identidad |
| Logs y métricas | Media | Pueden filtrar datos sin querer |

**DEBE**: no pedir ni almacenar números completos de tarjeta, IBAN ni credenciales bancarias. Si en el futuro se añade conexión bancaria, requerirá revisión de seguridad y de normativa antes de implementarse.

---

## 3. Backend y API

Referencia: [OWASP API Security Top 10 (2023)](https://apisecurity.io/owasp-api-security-top-10/). Cada riesgo, aplicado a nuestra app:

| Riesgo OWASP | Qué significa aquí | Qué hacer |
|---|---|---|
| **API1 – Autorización a nivel de objeto (BOLA)** | Cambiar el `id` en `/transactions/42` y ver el movimiento de otra persona | **DEBE** comprobar en cada endpoint que el recurso pertenece al usuario autenticado. Tests que lo verifiquen |
| **API2 – Autenticación rota** | Login sin límite de intentos, tokens que no caducan | Ver §4 |
| **API3 – Autorización a nivel de propiedad** | Devolver campos de más (p. ej. el hash de contraseña) o permitir modificar `usuario_id` por *mass assignment* | **DEBE** usar DTOs/listas blancas de campos de entrada y de salida |
| **API4 – Consumo ilimitado de recursos** | Listados sin paginar, exportaciones masivas, fuerza bruta | Paginación obligatoria, límites de tamaño de petición y *rate limiting* |
| **API5 – Autorización a nivel de función** | Un usuario normal llama a un endpoint de administración | Denegar por defecto; permisos explícitos por rol |
| **API6 – Flujos de negocio sensibles** | Abuso del registro, de la exportación CSV o del restablecer contraseña | Límites por usuario/IP y alertas |
| **API7 – SSRF** | El servidor hace peticiones a URLs dadas por el usuario | No aceptar URLs arbitrarias; lista blanca de destinos |
| **API8 – Mala configuración** | Debug activo, CORS abierto, cabeceras ausentes | Ver checklist de configuración abajo |
| **API9 – Inventario inadecuado** | Endpoints antiguos o de pruebas olvidados en producción | Mantener la API documentada y versionada; retirar lo que no se use |
| **API10 – Consumo inseguro de APIs** | Confiar en datos de librerías o servicios externos | Validar también lo que viene de terceros |

### Reglas generales del backend

- **DEBE** tratar como no confiable todo lo que envía el cliente: IDs, roles, importes, divisas, permisos o propietarios (`usuario_id`). El propietario de un recurso se deduce del token, nunca de un campo de la petición.
- **DEBE** usar *allowlists* de campos aceptados y rechazar los inesperados (evita *mass assignment*: el cliente no puede cambiar `role`, `usuario_id` u otros campos protegidos enviando campos extra).
- **DEBE** devolver solo los campos necesarios (DTOs); nada de columnas internas ni registros completos.
- **DEBE** aplicar **rate limiting** en el servidor (por usuario, IP o token), especialmente en login, registro, recuperación de contraseña, verificación de códigos y exportación; además de límites de tamaño de petición y de tiempo de ejecución.
- **DEBE** tener desactivados en producción el modo *debug*, los endpoints de diagnóstico/pruebas y los de administración (o protegidos con autenticación y rol), y no dejar credenciales por defecto ni cuentas de prueba.
- **DEBERÍA** desactivar los métodos HTTP no usados y, si algún día se aceptan rutas, archivos o URLs dadas por el usuario, validarlos contra *path traversal* y SSRF.

- **DEBE** servirse solo por **HTTPS** (TLS 1.2 o superior) en staging y producción; redirigir HTTP o rechazarlo.
- **DEBE** validar tipo, longitud, rango y formato de **toda** entrada (importes numéricos y acotados, fechas válidas, longitud máxima de textos).
- **DEBE** usar **consultas parametrizadas / ORM**; prohibido construir SQL concatenando texto (inyección SQL).
- **DEBE** devolver las descripciones libres como datos, sin interpretarlas; si en el futuro se añade una web, escapar la salida para evitar XSS.
- **DEBE** devolver errores genéricos al cliente (sin *stack traces*, sin nombres de tablas) y registrar el detalle solo en el servidor.
- **DEBERÍA** usar importes en **unidades menores enteras** (céntimos) o tipos decimales exactos, nunca `float`, para evitar errores de redondeo. Al haber varias divisas, el número de decimales depende de la divisa (ISO 4217).
- **Divisas y tipos de cambio**: validar que el código de divisa sea ISO 4217 y esté soportado. Los tipos de cambio los obtiene **solo el backend** de un proveedor externo, **sin enviarle datos del usuario**; validar y acotar lo que devuelve (API10), tener un valor de reserva si el proveedor falla y no confiar en tasas enviadas por el cliente.
- **CORS**: la app móvil no lo necesita; si se añade una web, permitir solo sus orígenes, nunca `*` con credenciales.
- **Cabeceras de seguridad** en la API: `Content-Security-Policy`, `Strict-Transport-Security`, `X-Content-Type-Options`, `Referrer-Policy`, `X-Frame-Options`/`frame-ancestors`.
- **Base de datos**: usuario de aplicación con permisos mínimos, no accesible desde internet, copias de seguridad cifradas y restauración probada.
  - **Ninguna tabla pública por defecto**: acceso denegado y abrir solo lo necesario.
  - **RLS** (Row-Level Security) activado en las tablas con datos de usuario cuando el motor lo soporte, con políticas separadas para leer, insertar, actualizar y borrar. Se revisa por tabla, operación y relación entre registros, y **no sustituye** la autorización del servidor (defensa en profundidad).
  - **Test obligatorio**: un usuario no puede leer, modificar ni borrar registros de otro cambiando un ID (§9).
  - Si se usa un servicio donde el cliente accede directamente a los datos, su clave pública (*anon*) **no es un secreto ni un permiso de administrador**: se limita con RLS. Las claves de servicio y administración viven solo en el servidor y **nunca** en la app.
  - Migraciones reproducibles y probadas antes de producción.
- **Dependencias**: actualizadas, sin vulnerabilidades conocidas (§9).

### Secretos, cifrado y transporte

- **DEBE** mantener secretos y APIs privilegiadas **en el servidor**; nunca incrustar secretos permanentes en el binario móvil (se extraen con facilidad).
- **DEBE** guardar los secretos en un gestor de secretos o en el sistema de secretos de la plataforma de despliegue y de la CI. Al repo solo va `.env.example`, sin valores.
- **DEBE** rotar credenciales y revocar de inmediato las que se hayan expuesto.
- **DEBE** forzar HTTPS/TLS en todas las comunicaciones y no desactivar nunca la validación de certificados.
- **DEBERÍA** cifrar en reposo los datos sensibles cuando corresponda (BD y copias de seguridad), con las claves gestionadas por separado, usando cifrado autenticado y bibliotecas mantenidas. **Prohibido implementar criptografía propia.**
- **DEBERÍA** minimizar los datos sensibles guardados en local y revisar lo que queda en cachés, notificaciones y copias.

---

## 4. Autenticación y sesiones

- **DEBE** guardar contraseñas solo como hash con un algoritmo diseñado para ello: **Argon2id** (preferido) o bcrypt/scrypt. Nunca MD5, SHA-1 ni SHA-256 sin más, ni cifrado reversible.
  - Parámetros orientativos de Argon2id según la cheat sheet de OWASP (versión de 2023): mínimo 19 MiB de memoria, 2 iteraciones, paralelismo 1 ([resumen](https://Www.wikipedia.org/wiki/Argon2)). Los valores pueden haberse actualizado: **verificar en el [Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) al implementarlo**.
  - Usar la librería estándar del lenguaje/framework; no implementar el hash a mano. La sal la gestiona la librería.
- **DEBE** exigir una política mínima de contraseña (longitud mínima de 8–12 caracteres, sin obligar a símbolos raros) y comprobar contraseñas muy comunes.
- **DEBE** limitar intentos de login (por cuenta e IP) y responder igual si el usuario no existe o la contraseña es incorrecta, para no revelar qué emails están registrados.
- **Tokens**: caducidad corta para el *access token* y *refresh token* revocable; invalidar en logout y al cambiar la contraseña.
- **Móvil**: tokens solo en Keystore/Keychain (§5), nunca en preferencias en claro. Si en el futuro se añade una web: cookies `HttpOnly`, `Secure`, `SameSite`, protección CSRF y evitar `localStorage` para tokens.
- **DEBERÍA** ofrecer verificación del email y, a medio plazo, doble factor (2FA).
- **Restablecer contraseña**: enlaces de un solo uso y con caducidad corta; no revelar si el email existe.
- **Autorización en el servidor, siempre**: ocultar una pantalla en la app no protege el endpoint. En cada operación se comprueba que el usuario es propietario del recurso (o se le ha compartido expresamente), con privilegio mínimo y permisos explícitos.
- **Sesiones**: gestionar expiración, renovación, **revocación** y cierre de sesión; poder invalidar todas las sesiones de una cuenta comprometida. Usar OAuth 2.0 / OpenID Connect si se añade inicio de sesión con terceros.
- **Abuso y bots**: *rate limiting* progresivo en login, registro y recuperación y detección de abuso; CAPTCHA adaptativo solo si el riesgo lo justifica, siempre con alternativa accesible.
- **Secretos**: claves de firma, contraseñas de BD y claves de API en variables de entorno / *secrets* de GitHub. **Nunca** en el repositorio (ver `.gitignore` y [WORKFLOW.md](WORKFLOW.md) §6).

---

## 5. App móvil (Android e iOS)

Referencia: [OWASP MASVS](https://opensecurityarchitecture.org/frameworks/owasp-masvs-v2) (y su guía de pruebas MASTG). Objetivo: cumplir como mínimo el nivel **L1** (base para toda app en producción).

- **Almacenamiento**: **DEBE** evitar guardar datos sensibles en texto plano en ficheros, `SharedPreferences`, logs, copias de seguridad o portapapeles. Usar **EncryptedSharedPreferences / Jetpack Security** y el **Android Keystore** para claves y tokens ([resumen MASVS](https://appsecsanta.com/mobile-security/owasp-masvs-guide)).
- **Copias de seguridad**: revisar `android:allowBackup` para que no se exporten datos sensibles.
- **Red**: solo HTTPS (`usesCleartextTraffic=false`), validación correcta de certificados, **nunca** desactivar la verificación de TLS "para probar". El *certificate pinning* es recomendable (nivel L2), pero planificar la rotación de certificados.
- **Criptografía**: sin claves ni secretos *hardcodeados* en el código o el APK (se extraen fácilmente); evitar algoritmos obsoletos (MD5, SHA-1, DES).
- **Logs**: no registrar importes, descripciones, tokens ni emails en `Log.*` en builds de release.
- **Pantalla**: valorar `FLAG_SECURE` en pantallas con saldos (evita capturas y la vista previa en "recientes").
- **Bloqueo de la app**: DEBERÍA ofrecer bloqueo opcional con biometría / PIN del dispositivo.
- **Sin secretos permanentes en el binario** (claves privilegiadas, credenciales de BD, claves de servicio): todo lo que viaja en la app se considera público.
- **Versiones mínimas**: definir las de Android e iOS y probar en versiones y dispositivos reales distintos, incluidos modestos.
- **Firma y certificados**: identificadores, claves de firma y certificados fuera del repo, con acceso restringido y copia segura (si se pierde la clave de firma de Android no se puede actualizar la app).
- **Deep links y notificaciones** (si se usan): validar sus parámetros como cualquier entrada no confiable; las notificaciones **no** muestran importes ni datos sensibles en la pantalla de bloqueo.
- **Cierre de sesión**: borrar tokens, cachés y datos locales de la sesión.
- **Permisos**: pedir solo los estrictamente necesarios (la app no necesita contactos, ubicación ni SMS).
- **Release**: firma del APK con una clave protegida (no en el repo), ofuscación/minificación (R8) y `debuggable=false`.
- **iOS**: se aplican los mismos requisitos de MASVS: **Keychain** para tokens y claves (equivalente al Keystore), App Transport Security/TLS sin excepciones, sin datos sensibles en logs, copias de iCloud ni capturas de la vista de apps recientes. Con un framework multiplataforma, usar el almacenamiento seguro de cada plataforma, no el almacenamiento genérico del framework.
- **Dependencias y SDKs de terceros**: cada SDK (analítica, anuncios, crash reporting) es un posible fugitivo de datos. **No** incluir SDKs de publicidad ni analítica que envíen datos financieros a terceros.

---

## 6. Web (fuera de alcance por ahora)

No hay cliente web. Si se añade en el futuro, antes de empezar habrá que incorporar como mínimo: protección **XSS** (escapado por defecto, sin `innerHTML` con datos de usuario), **CSRF**, **CSP** restrictiva con SRI para scripts de terceros, ningún secreto en el front, no cachear datos sensibles, consentimiento de cookies no esenciales (§7) y CORS limitado a sus orígenes.

---

## 7. Privacidad y protección de datos (RGPD / LOPDGDD)

Si la app trata datos de personas en la UE, aplica el **RGPD** (y en España la **LOPDGDD**; autoridad: [AEPD](https://www.aepd.es)). Aunque el proyecto sea académico, lo construimos como si fuera real. Resumen de lo que implica (a verificar con el texto oficial; las fuentes consultadas son guías sectoriales, no el texto legal: [guía fintech](https://gdprlocal.com/gdpr-for-financial-institutions/), [Bloomberg Law](https://news.bloomberglaw.com/banking-law/insight-eu-gdpr-10-things-every-fintech-business-should-know)):

| Requisito | Qué implica para nosotros |
|---|---|
| **Minimización** (art. 5) | Solo email y contraseña para registrarse; sin pedir nombre completo, teléfono, fecha de nacimiento... si no hacen falta |
| **Limitación de finalidad** | Los datos se usan solo para dar el servicio. Prohibido venderlos, cederlos o usarlos para publicidad o perfilado |
| **Base legal y consentimiento** (arts. 6-7) | Consentimiento específico, informado y revocable para todo lo que no sea necesario para el servicio |
| **Información** | Política de privacidad clara (qué datos, para qué, cuánto tiempo, quién es el responsable, contacto) |
| **Acceso y portabilidad** (arts. 15, 20) | Función de **exportar todos los datos** en formato legible por máquina (nuestro CSV/JSON). Plazo de respuesta a solicitudes: 1 mes |
| **Conservación y borrado** | Política definida de cuánto se conservan cuentas, movimientos y logs, y cómo se eliminan |
| **Rectificación** | La persona puede editar sus datos |
| **Supresión ("derecho al olvido")** (art. 17) | Función de **borrar la cuenta y todos sus datos**, incluidos backups en un plazo razonable y los servicios de terceros |
| **Seguridad del tratamiento** (art. 32) | Cifrado en tránsito y en reposo cuando proceda, control de accesos, registro de incidentes |
| **Brecha de seguridad** (arts. 33-34) | Notificar a la autoridad en **72 h** y, si hay alto riesgo, a las personas afectadas |
| **Privacidad desde el diseño** (art. 25) | Los valores por defecto son los más privados |

Decisiones concretas para el equipo:

- **DEBE** publicar una **política de privacidad** precisa y coherente con el tratamiento real, y **términos de uso**. Documentar qué datos se recogen, para qué, con quién se comparten, cuánto tiempo se conservan y cómo se eliminan.
- **DEBE** tener un proceso para que las personas ejerzan sus derechos de privacidad, con un contacto.
- **DEBE** rellenar con exactitud las declaraciones de privacidad de las tiendas (*Privacy Nutrition Labels* de App Store y *Data safety* de Google Play); si el SDK de un tercero recoge datos, cuentan.
- **DEBE** pedir consentimiento previo y válido para la analítica y cualquier seguimiento que lo requiera; sin consentimiento, no se activa.
- **Edad**: si el servicio se limita a personas adultas (o a una edad mínima), el requisito se define y se comprueba **en el servidor**; no basta con una fecha editable en la app. No recoger documentos de identidad salvo que sea necesario y legal.
- **DEBE** existir un endpoint y una pantalla para **exportar** los datos y para **eliminar la cuenta**.
- **DEBE** definir y documentar un **plazo de retención** (qué se borra y cuándo; p. ej. cuentas inactivas, logs a 30 días).
- **DEBE** mantener un registro de qué terceros reciben datos (hosting, correo, analítica). Preferir proveedores con servidores en la UE y con contrato de encargado de tratamiento.
- **Menores**: no dirigirse a menores de 14 años (edad de consentimiento digital en España) sin consentimiento de sus tutores.
- **Datos de pruebas**: **nunca** usar datos reales de personas en dev/staging. Usar datos ficticios o anonimizados (ver [WORKFLOW.md](WORKFLOW.md) §6).
- **Descripciones libres**: avisar de que no introduzcan datos de terceros o especialmente sensibles (salud, ideología...).

---

## 8. Ética

Más allá de la ley, estas son decisiones que tomamos como equipo:

1. **Sin patrones oscuros**: nada de notificaciones o diseños que empujen a gastar, a dar más datos o que dificulten borrar la cuenta. Darse de baja es tan fácil como registrarse.
2. **Sin monetización oculta**: no vendemos datos ni mostramos publicidad basada en hábitos de gasto.
3. **Honestidad en los datos**: gráficos, balances y presupuestos deben ser correctos y no inducir a error. Los cálculos tienen tests.
4. **No es asesoramiento financiero**: la app informa del gasto del propio usuario; no recomienda inversiones ni productos. Evitar redacciones que lo parezcan.
5. **Sin sesgos ni perfilado**: si en el futuro se añaden sugerencias automáticas o categorización con IA, deben ser explicables, opcionales y no discriminar. Los datos de usuarios no se envían a servicios de IA de terceros sin consentimiento explícito.
6. **Accesibilidad e inclusión**: diseñar para personas con distintas discapacidades siguiendo WCAG y las pautas de accesibilidad de Android e iOS: etiquetas accesibles y descripciones útiles para lectores de pantalla (TalkBack y VoiceOver), imágenes decorativas marcadas como tales, no transmitir significado solo con el color, contraste suficiente, escalado de texto, áreas táctiles mínimas y orden de foco correcto; lenguaje claro y formatos de moneda/fecha/idioma configurables. No afirmar cumplimiento de WCAG/ADA sin una evaluación adecuada. Mostrar siempre el importe original junto al convertido y avisar de que los importes convertidos son aproximados.
7. **Bienestar de la persona**: los avisos de presupuesto informan, no culpabilizan ni generan ansiedad.
8. **Licencias, autoría y código ajeno**: respetar las licencias de las librerías, imágenes, fuentes y contenido de terceros (el proyecto es MIT), mantener los avisos y atribuciones exigidos y no incluir material protegido sin licencia compatible. Dejar por escrito quién posee código, diseños, marca y contenidos y bajo qué licencia se aporta (hay varias personas colaborando). Autoalojar las fuentes cuando la licencia lo permita en lugar de descargarlas de un tercero en cada arranque. Si usamos herramientas de IA para programar, **revisar** el código generado como cualquier otro (puede contener fallos de seguridad o código con licencia).
9. **Divulgación responsable**: quien encuentre una vulnerabilidad la comunica por canal privado al equipo (no en una issue pública) y se corrige antes de divulgarla. Añadir un `SECURITY.md` con el contacto.
10. **Pruebas de seguridad solo sobre nuestros propios entornos** y con permiso del equipo; nunca contra sistemas de terceros.

---

## 9. Seguridad en el ciclo de desarrollo

- **Dependencias y cadena de suministro**: instalar solo dependencias justificadas (mantenimiento, popularidad, licencia, permisos); versiones fijadas y archivos de bloqueo (*lockfiles*) en el repo; activar **Dependabot** y `npm audit` / `pip-audit` / OWASP Dependency-Check según el stack, incluidas las transitivas; revisar scripts de instalación y paquetes sospechosos; sustituir las abandonadas o vulnerables; revisar avisos en cada RC.
- **Secretos**: activar **GitHub secret scanning** y *push protection*; si un secreto se filtra, **se rota de inmediato** (borrar el commit no basta).
- **Análisis estático**: CodeQL u otra herramienta en la CI (ver [WORKFLOW.md](WORKFLOW.md) §10).
- **Revisión de código**: ningún PR se fusiona sin revisión; los cambios en autenticación, permisos o manejo de datos requieren **revisión de otra persona con atención específica a seguridad**.
- **Tests de seguridad**: tests automáticos de autorización horizontal (un usuario no accede a datos de otro cambiando un ID) y vertical (sin permisos de administración), de *mass assignment* (campos extra ignorados o rechazados), de inyección, de validación de entradas y de límites/*rate limiting*; pruebas con sesión caducada y respuestas inesperadas.
- **Logs y monitorización**: niveles `DEBUG`/`INFO`/`WARN`/`ERROR` y logs de depuración desactivados en producción. Registrar eventos de seguridad (login fallido, cambio de contraseña, exportación, borrado de cuenta) **sin** contraseñas, tokens, importes, descripciones ni datos personales innecesarios. Identificadores de correlación (`X-Request-Id`) entre app y backend, alertas para incidentes críticos y acceso, retención y borrado de logs definidos.
- **Analítica y reporte de errores**: revisar qué datos envía cada herramienta. La **repetición de sesiones (*session replay*) está desactivada**; si algún día se usa, se enmascaran todos los campos y toda la información financiera, y se verifica con pruebas reales.
- **Entornos**: separación estricta dev/staging/prod; sin acceso a prod desde equipos personales sin necesidad; credenciales distintas por entorno.
- **Backups**: cifrados, con acceso restringido y restauración probada antes de cada versión final.
- **CI/CD**: secretos de la CI fuera del repositorio; acceso restringido a la firma y la publicación; builds reproducibles y versiones registradas; migraciones probadas antes de producción.
- **Respuesta a incidentes**: definir quién decide, cómo se contiene, cómo se avisa (72 h) y cómo se documenta la lección aprendida.
- **QA de cada RC**: añadir al checklist de [WORKFLOW.md](WORKFLOW.md) §5 los puntos de seguridad de §10.

---

## 10. Checklist para PRs y RCs

**En cada PR que toque backend o móvil**
- [ ] Toda entrada se valida en el backend (tipos, longitud, rango; campos inesperados rechazados).
- [ ] Cada endpoint comprueba autenticación **y** que el recurso pertenece al usuario.
- [ ] No se devuelven más campos de los necesarios.
- [ ] El propietario del recurso se toma del token, no de un campo de la petición.
- [ ] Hay tests de autorización (acceso a datos de otro usuario) para los endpoints nuevos.
- [ ] No hay secretos, claves ni datos reales en el código, tests ni logs.
- [ ] No se registran datos personales ni financieros en logs.
- [ ] Consultas parametrizadas; salida escapada.
- [ ] Nuevas dependencias revisadas (mantenimiento, licencia, vulnerabilidades).
- [ ] Si se recoge un dato nuevo: está justificado, documentado en §2 y en la política de privacidad.

**Antes de publicar un RC o una versión final**
- [ ] Tráfico solo por HTTPS; sin `cleartext` ni verificación TLS desactivada.
- [ ] Modo debug desactivado y sin *stack traces* al cliente.
- [ ] Rate limiting y paginación activos; límites de tamaño de petición.
- [ ] Endpoints de administración y depuración desactivados; sin credenciales ni cuentas de prueba.
- [ ] Permisos de BD (y RLS si aplica) revisados y probados; ninguna tabla abierta por accidente.
- [ ] Sin secretos en Git ni en el paquete distribuido (APK/IPA).
- [ ] Cabeceras de seguridad de la API verificadas.
- [ ] APK de release firmado, `debuggable=false`, sin secretos en el APK.
- [ ] Exportación y borrado de cuenta funcionan de extremo a extremo.
- [ ] Dependencias sin vulnerabilidades críticas conocidas.
- [ ] Datos de staging ficticios o anonimizados.
- [ ] Backups cifrados y restauración probada.
- [ ] Logs sin secretos ni datos personales; *session replay* desactivado.
- [ ] Accesibilidad probada con TalkBack y VoiceOver; rendimiento y compatibilidad probados en dispositivos reales.
- [ ] Declaraciones de privacidad de las tiendas y términos de uso revisados.
- [ ] Plan para monitorizar, responder a incidentes y mitigar una release defectuosa (una app instalada no se revierte al instante).
- [ ] Política de privacidad y `SECURITY.md` actualizados.

---

## 11. Pendiente de decidir por el equipo

- [ ] Persona responsable de seguridad/privacidad (rotativa o fija).
- [ ] Stack definitivo (afecta a las librerías concretas de hash, tokens y validación).
- [ ] Dónde se aloja cada entorno y si está en la UE.
- [ ] Si habrá 2FA en la primera versión estable.
- [ ] Redactar la política de privacidad y el `SECURITY.md`.
- [ ] Plazos de retención de datos y logs.
- [ ] Edad mínima de uso y cómo se comprueba en el servidor.
- [ ] Autoría y licencia del código, diseños y marca entre los colaboradores.

---

## 12. Qué no aplica por ahora (y qué hacer si se añade)

Estos temas de las buenas prácticas generales para apps móviles **no están en el alcance actual**. Si se incorporan, hay que revisarlos **antes** de empezar a programarlos:

| Si se añade... | Revisar antes |
|---|---|
| **Subida de archivos** (fotos de tickets, adjuntos) | Limitar tipo, tamaño y número; validar el contenido real (no solo extensión ni `Content-Type`); nombres internos generados; almacenar fuera de directorios ejecutables; eliminar metadatos (EXIF); URLs firmadas de corta duración; antimalware si el riesgo lo justifica; permisos de lectura, descarga, sustitución y borrado |
| **Pagos, suscripciones o premium** | Procesar pagos en el servidor o con un proveedor establecido, nunca con importes calculados por el cliente; verificar pagos en el backend; idempotencia; precio, moneda, impuestos y periodicidad claros antes de comprar; cancelación fácil y visible, sin patrones oscuros; política de reembolso; reglas de compra de App Store y Google Play |
| **Emails comerciales o notificaciones de marketing** | Consentimiento previo y su registro, baja con un clic y respeto a las preferencias, separar transaccional de marketing, SPF/DKIM/DMARC, identificación del remitente |
| **Versión web** | Sección 6 de este documento |
| **Conexión con bancos reales** | Revisión específica de seguridad y de la normativa financiera aplicable |

---

## Referencias

- [OWASP API Security Top 10 – 2023](https://apisecurity.io/owasp-api-security-top-10/) (resumen; oficial: owasp.org/API-Security)
- [OWASP MASVS v2](https://opensecurityarchitecture.org/frameworks/owasp-masvs-v2) (oficial: mas.owasp.org/MASVS)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
- [GDPR para instituciones financieras (guía)](https://gdprlocal.com/gdpr-for-financial-institutions/)
- [AEPD – Agencia Española de Protección de Datos](https://www.aepd.es)
- [OWASP Cheat Sheet Series](https://cheatsheetseries.owasp.org/) (guías prácticas de seguridad)
- [Android Developers](https://developer.android.com/) y [Apple Developer](https://developer.apple.com/) (documentación oficial, almacenamiento seguro y requisitos de las tiendas)
- [WCAG – W3C](https://www.w3.org/WAI/standards-guidelines/wcag/) (accesibilidad)
- [OpenAPI Specification](https://www.openapis.org/) (contratos de API)

> Varias referencias son resúmenes secundarios obtenidos en una búsqueda web; antes de implementar un requisito concreto, consulta la fuente oficial indicada.
