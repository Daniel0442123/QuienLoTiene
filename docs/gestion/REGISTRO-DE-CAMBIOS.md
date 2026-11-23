# Registro de cambios por versión · Quién lo tiene

> **Control del documento** · Versión 1.13 · 22/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: Vigente

Notas de versión del producto: qué trae cada etiqueta `v*` que se despliega. Sigue el formato de *Keep a Changelog* y las versiones SemVer de D-22. El detalle de cada PR (autor, fechas, aprobación) está en la [bitácora](BITACORA-DEL-PROYECTO.md); las desviaciones, en el [control de cambios](CONTROL-DE-CAMBIOS.md).

Normas que atiende: ISO/IEC/IEEE 12207:2017 (6.4.10 transición, 6.3.5 gestión de la configuración: identificación de versiones), ISO/IEC/IEEE 29119-3 (las notas de versión son entrada de cada ciclo de pruebas), ISO 10007 (registro del estado de la configuración).

**Cómo se mantiene:** cada versión tiene sus notas y su **evidencia de calidad** (CI, pruebas, cobertura, escaneos de seguridad y despliegue). Al terminar el despliegue de una etiqueta, el flujo `version.yml` crea la Release de GitHub con estas notas y la evidencia generada por `scripts/gestion/evidencia_version.py`, y le adjunta los resultados de pruebas, la cobertura y el SBOM, porque GitHub Actions solo guarda esos artefactos 14 o 30 días. Además, antes de crear una etiqueta, quien la crea mueve lo de «Sin liberar» a una sección nueva con la versión y la fecha, y crea la etiqueta **anotada** (`git tag -a v0.3.0 -m "Sprint 2: …"`), para que quede quién y cuándo. Las etiquetas `v0.1.0` a `v0.2.0` son ligeras (sin autor ni fecha); su fecha aquí es la del commit etiquetado.

---

## Sin liberar

Correcciones para la `v0.5.5`, en el #92. Se etiqueta después de fusionarlo.

### Corregido
- **DEF-016 (S4).** Al vencer la solicitud, la lista de respuestas ya no dice «Te avisamos en esta pantalla apenas una tienda conteste» mientras la API todavía no marca el vencimiento. Desde la hora de vencimiento muestra el aviso de vencida y quita «Elegir», como ya hacía el título (`RespuestasDeSolicitud.tsx`).
- **DEF-017 (S4).** Los botones + y − del mapa miden 44 × 44 px (RNF-028); antes medían 30 × 30 px (`styles/index.css`).
- **DEF-018 (S3).** El `mem_limit` de Caddy sube de 128 a 256 MiB en `compose.prod.yaml`; con 100 usuarios llegó a 126,5 MiB. El stack queda en 1.408 MiB como máximo, dentro de los 2 GB de RNF-004. Falta repetir NF-ESC-020 después del despliegue.

### Pruebas
- Frontend: 363 pruebas con Vitest en verde, 2 nuevas para DEF-016 (`SolicitudVencidaPorReloj.test.tsx`), que fallan sin la corrección. Botones del mapa medidos en Chromium a 375 px: 44 × 44.

## v0.5.4 · 22/11/2026 · Indicadores de la tienda y radio ampliado (Sprint de Cierre)

Etiqueta sobre `main` después de este PR. Trae las dos historias Could del Sprint de Cierre que se hicieron (#89). HU-006 y HU-011 salen del alcance por SC-021, aprobada por el Product Owner (#90).

### Agregado
- **HU-022, ampliar el radio automáticamente (CU-31).** Si una solicitud abierta lleva 10 minutos sin ninguna respuesta, el radio de búsqueda pasa de 3 a 5 km, una sola vez. La solicitud llega solo a las tiendas que no la tenían, con el aviso de siempre (SSE o push). El cliente recibe el evento nuevo `radio-ampliado`: su pantalla de espera muestra el radio nuevo y anuncia «Nadie ha respondido aún. Ampliamos la búsqueda a 5 km». Los dos valores se configuran con `quienlotiene.solicitudes.radio-ampliado-en-metros` y `espera-para-ampliar`. No hay migración: la columna `fue_ampliado` ya existía.
- **HU-015, resumen de indicadores (CU-23).** Ruta nueva `GET /api/v1/tiendas/me/indicadores?periodo=7|30` y pantalla «Tu resumen» en el panel de la tienda (`/tienda/resumen`, mockup 06). Muestra las solicitudes recibidas, respondidas y ganadas, con «Respondiste» y «Te eligieron» según la fórmula de SC-011 punto 6. Cuando no hay datos muestra «—».

### Pruebas
- 398 unitarias y 72 de integración, 0 fallas; cobertura de `domain` y `application` del 95,5 %. Frontend: 361 pruebas. CP-TIE-015-01/02 y CP-SOL-022-01/02 aprobados en el C3 (#89).

## v0.5.3 · 22/11/2026 · Seguridad de los no funcionales del C2

Etiqueta sobre `main` después de este PR. Trae las correcciones que salieron al escribir las pruebas de los no funcionales del C2 (#85).

### Agregado
- Límite de 5 intentos de inicio de sesión por minuto por cuenta: el sexto responde 429 con el código nuevo `IDE-015` y `Retry-After` (RNF-009, DEF-013). El frontend ya muestra el mensaje genérico de «demasiados intentos».

### Corregido
- Las fotos y documentos de imagen que sube la tienda se guardan sin metadatos: se quitan EXIF y XMP de JPEG, PNG y WebP, y una imagen dañada se rechaza con `TIE-003` (RNF-012, DEF-014). Las fotos que ya estaban guardadas no se modifican.
- Auditoría: cuando la tienda ve el nombre y el celular del cliente de una orden, o pide su contacto de WhatsApp, queda un registro en el log `auditoria` con los ids; los inicios de sesión (correctos, fallidos y de cuentas suspendidas) también pasan a ese log (RNF-013, DEF-015).

### Pruebas
- Pruebas nuevas o etiquetadas para NF-REN-003, NF-SEG-007, 008, 009, 012, 013 y NF-FIA-015: 386 unitarias y 67 de integración, 0 fallas; cobertura de `domain` y `application` 95,3 % (#85).
- Las 3 filas de CP-CLI-008 (HU-008, SC-018) en el Excel de control de QA (#85).

## v0.5.2 · 22/11/2026 · Contrato del chat y título del inicio

Etiqueta sobre `main` después de este PR. Trae las correcciones fusionadas después de `v0.5.1`.

### Corregido
- Contrato de la API: el texto del mensaje del chat vuelve a ser obligatorio en `/v3/api-docs` (`texto` de `EnviarMensajeRequest` con `requiredMode REQUIRED`) y `api-tipos.ts` del frontend se regeneró; una prueba de contrato lo cuida. El dominio sigue respondiendo `MEN-003` (HU-033, #79).
- El título de la pestaña en el inicio sin sesión ya no repite el nombre de la app (SC-020, #82).

### Documentación
- Mockups del menú lateral por rol y del inicio nuevo (tableros 14 a 16) y cierre de C-21 (SC-020, #81).

## v0.5.1 · 22/11/2026 · Menú lateral por rol e inicio

Etiqueta sobre `main` después del #78. Solo frontend; la API no cambia.

### Cambiado
- Menú principal según el rol, con ícono y texto por módulo: barra lateral fija en escritorio y botón «Menú» que lo abre en celular. El cliente ve Inicio, Preguntar, Mis preguntas, Mis pedidos y Mi cuenta; la tienda, su panel, solicitudes nuevas, pedidos, perfil y horario, domicilio y medios de pago; el administrador, tiendas, reportes y catálogo. El módulo actual va marcado y «Cerrar sesión» pasa al final del menú (SC-020).
- Página de inicio con contenido: sin sesión explica qué hace la app con «Cómo funciona» en tres pasos y la invitación a registrar la tienda; con sesión muestra un saludo y accesos rápidos según el rol (SC-020).

## v0.5.0 · 21/11/2026 · Frontend del Sprint 3

Etiqueta sobre `main` después del #74. Trae las pantallas de las 12 historias del Sprint 3, que en `v0.4.1` solo se usaban desde la API, y los ajustes del contrato que pidió el frontend.

### Agregado
- Cancelar el pedido, reserva para recoger con cuenta regresiva y vencimiento, y «Cómo llegar» con la app de mapas (HU-029, HU-025, HU-026, SC-019, #67).
- Calificar la tienda y detalle público de la tienda con horario, mapa, medios de pago y reseñas recientes (HU-034, HU-014, #68).
- `/admin/catalogo` para categorías y productos restringidos, y aviso «Este medicamento necesita fórmula médica» sin domicilio (HU-038, HU-023, #69).
- Reportar un problema y bandeja `/admin/reportes` para resolver, suspender y reactivar tiendas (HU-035, HU-037, #70).
- Chat del pedido en tiempo real, de solo lectura con el pedido terminado (HU-033, #71).
- Avisos push para la tienda con la app cerrada, con el service worker `sw-push.js` (HU-031, #72). Necesitan las claves VAPID en el `.env` del VPS.
- «Mis preguntas», historial de preguntas y «Repetir» (HU-008, SC-018, #73).
- Bandeja de reportes con `nombreTienda` y `estadoTienda` (HU-037, #74).

### Cambiado
- `/v3/api-docs`: `mediosPago` del detalle de la tienda como lista de `MedioPago` y `motivo` del reporte con sus cuatro valores; el texto vacío o muy largo del chat responde `MEN-003` (#74). `CONTRATO-SPRINT-3.md` 0.5.

### Corregido
- CI: el Maven del wrapper se guarda en caché y se descarga con hasta tres intentos; la descarga falló en el #69 y en el #71 (#74).
- Dos pruebas del frontend que fallaban a veces con la suite completa: panel de la tienda (HU-013) y «Esperando respuestas» (HU-016) (#73).

## v0.4.1 · 20/11/2026 · Imágenes sin la vulnerabilidad que bloqueó v0.4.0

Etiqueta sobre `main` después de este PR. Mismo código que `v0.4.0`, que no llegó a desplegarse: Trivy bloqueó la imagen de la API (RNF-011).

### Seguridad
- Imagen de la API: se quita `/usr/bin/pebble`, que trae la base Ubuntu 26.04 de `eclipse-temurin:21-jre` y la API no usa, con CVE-2026-97054 (alta) en la librería estándar de Go.
- Imagen de Caddy: se compila con Go 1.27.4, que corrige CVE-2026-97054 en `net/http`.

### Corregido
- La construcción de la imagen de la API reintenta hasta tres veces la descarga de Maven, que falló tres veces el 20/11/2026 en los runners de GitHub.

## v0.4.0 · 20/11/2026 · Backend del Sprint 3

Etiqueta sobre `main` después del #64. **No llegó a desplegarse:** Trivy encontró CVE-2026-97054 en la imagen de la API y el despliegue se detuvo antes de publicar; se libera como `v0.4.1`. Trae el backend completo del Sprint 3; las pantallas de estas historias llegan con el frontend del sprint, así que en esta versión solo se usan desde la API.

### Agregado
- Backend del Sprint 3 (#63): cancelar la orden (HU-029), reserva para recoger de 30 minutos desde «Listo para recoger» que vence sola (HU-025, SC-019), calificar la tienda (HU-034), detalle público de la tienda (HU-014), categorías y productos restringidos (HU-038, HU-023), reportar un problema y gestionarlo con suspensión de cuentas (HU-035, HU-037), mini chat de la orden (HU-033), avisos Web Push (HU-031) y «Mis preguntas» con repetir (HU-008, SC-018).
- Migraciones nuevas: `ordenes` (reserva y cancelación), `reputacion.resena`, `catalogo.producto_restringido`, `reputacion.reporte`, `mensajeria.mensaje` y `notificaciones.suscripcion_push`.
- Configuración: claves VAPID `QLT_VAPID_CLAVE_PUBLICA`, `QLT_VAPID_CLAVE_PRIVADA` y `QLT_VAPID_SUJETO` en el `.env` del VPS; sin ellas el push queda apagado y lo demás funciona igual.
- «Mi tienda»: categoría y ubicación de la tienda (SC-017, #61).
- Pruebas: integración del detalle público de la tienda sin RUT ni documentos (CP-TIE-014-02) y caso CP-CLI-008-03 (#64).

### Documentación
- SC-018 aprobada y SC-019 ratificada por el comité el 19/11/2026 (#64). Prueba en producción de `v0.3.1` e informe de cierre del C1 listo para firma (#60, #62).

## v0.3.1 · 17/11/2026 · Corrección encontrada en producción

Etiqueta sobre `main` después del #58.

### Corregido
- DEF-011: al recargar «Esperando respuestas», el cliente ya no pierde su solicitud ni las respuestas de las tiendas. La solicitud queda en la dirección de la página (`?solicitud=<id>`) y se vuelve a pedir a la API (HU-016, HU-020, #58).

### Documentación
- Evidencia de calidad de `v0.3.0`, bitácora con el despliegue y la primera limpieza de producción, y SC-016 implementada (#57).

### Evidencia de calidad

Tomada de la [Release v0.3.1](https://github.com/Daniel0442123/QuienLoTiene/releases/tag/v0.3.1), que generó `version.yml` el 17/11/2026 17:29 sobre el commit `3ee6b2f`.

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: OWASP Dependency-Check, análisis de seguridad, compilar, pruebas y cobertura | [44188169927](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44188169927) · 17/11/2026 17:16 | ✅ Correcto |
| CI frontend (último cambio del frontend incluido, `e0d5d96`) | [44187560958](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44187560958) | ✅ Correcto |
| Despliegue: pruebas, imágenes con Trivy, GHCR, VPS y verificación HTTPS | [44188894630](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44188894630) · 17/11/2026 17:29 | ✅ Correcto |
| Prueba de punta a punta en producción, con la re-prueba de DEF-011 | [`evidencias/C1/2026-11-18/produccion/`](../pruebas/evidencias/C1/2026-11-18/produccion/README.md) · 18/11/2026 13:49 | ✅ 8 de 8 pasos |

Pruebas del backend: 333 (278 unitarias y 55 de integración), 0 fallas; cobertura de líneas 95,2 % en `domain` y `application`. Frontend: 248 pruebas en verde (#58).

## v0.3.0 · 17/11/2026 · Sprint 2 y cierre del ciclo C1

Etiqueta sobre `main` después del #55. Primera versión desplegada con el flujo completo: el cliente pregunta, las tiendas responden en tiempo real, el cliente elige y la tienda entrega el pedido.

### Agregado
- Backend del Sprint 2: contrato de la API, eventos en tiempo real (SSE), respuestas de las tiendas, órdenes con su historial y enlace de WhatsApp (HU-018 a HU-021, HU-024, HU-027, HU-028, HU-030, HU-032, #35). Migraciones nuevas: `solicitudes.respuesta` y `ordenes.orden` con su historial.
- Hasta 5 conexiones de tiempo real por usuario (HU-030, SC-014, #44).
- Frontend: tipos generados desde el contrato y simulación de respuestas y órdenes (#38).
- Frontend: solicitudes nuevas y responder desde la tienda (HU-018, HU-019, #39).
- Frontend: respuestas y estado de la solicitud para el cliente (HU-020, HU-021, #40).
- Frontend: elegir una respuesta y confirmar el pedido (HU-024, #41).
- Frontend: seguimiento del pedido, recepción y WhatsApp (HU-027, HU-028, HU-032, #42).
- Frontend: tiempo real con SSE y alerta sonora de las solicitudes urgentes (HU-030, #43).
- CI: OWASP Dependency-Check junto con Trivy (DEF-002, #36).
- Operación: `scripts/limpieza/limpiar.sh` para borrar los datos de prueba de producción, con modos «pruebas» y «total», respaldo previo y bloqueo al salir a producción real (SC-016, #56). El despliegue lo copia al VPS.
- Gestión: seguimiento y control de cambios del proyecto (bitácora, solicitudes de cambio, procedimientos, libro de control de cambios y chequeo «Control de cambios» en CI) y Release automática de cada versión (SC-009, #34). Decisiones de interfaz de los Sprints 2 y 3 (SC-011, #37).
- Pruebas: corte del Sprint 1 (#32), ejecución del Sprint 2, re-pruebas, informe de cierre del C1 y sus Word y Excel de entrega (#46, #49, #52 a #55).

### Corregido
- DEF-001 y DEF-006: el contrato OpenAPI declara las respuestas exitosas, los errores como `application/problem+json`, los enums con nombre y los eventos SSE; el frontend genera sus tipos desde `/v3/api-docs` (C-04, #35, #46, #48).
- DEF-003: la cobertura mínima de 70 % se exige por módulo (#35).
- DEF-004: la prueba de transiciones inválidas de la tienda lleva `@Tag("NF-FIA-016")` (#35).
- DEF-005: «Publicar solicitud» marca todos los datos que faltan al primer intento (#47).
- DEF-007: «Confirmar pedido» solo ofrece los medios de pago que acepta la tienda (#46, #47).
- DEF-008: la solicitud vencida ya no dice «Esperando respuestas» (#47).
- DEF-009: la tienda lee «El cliente paga con…» en su pedido (#47).
- DEF-010: sin punto doble después de la hora (#50).
- Pruebas del frontend más estables (#45, #51).

### Evidencia de calidad

Tomada de la [Release v0.3.0](https://github.com/Daniel0442123/QuienLoTiene/releases/tag/v0.3.0), que generó `version.yml` el 17/11/2026 00:25 sobre el commit `ed3da54`. La Release guarda también los resultados de las pruebas, el reporte de JaCoCo y el SBOM.

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: OWASP Dependency-Check, análisis de seguridad, compilar, pruebas y cobertura | [44022342818](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44022342818) · 17/11/2026 00:13 | ✅ Correcto |
| CI frontend (último cambio del frontend incluido, `e679ef3`) | [44009478982](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44009478982) | ✅ Correcto |
| Despliegue: pruebas, imágenes con Trivy, GHCR, VPS y verificación HTTPS | [44022656849](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44022656849) · 17/11/2026 00:25 | ✅ Correcto |

Pruebas del backend en esta versión: 333 (278 unitarias y 55 de integración), 0 fallas. Cobertura de líneas: 95,2 % en `domain` y `application` (meta ≥ 70 %, RNF-022) y 60,8 % en total.

## v0.2.0 · 12/11/2026 · Cierre funcional del Sprint 1

Commit `ea7351d`. Base del corte del Sprint 1 del ciclo C1 (PR #32).

### Agregado
- Frontend: pantalla del código TOTP del administrador (HU-004, #23).
- Frontend: registro de tienda con documentos y mapa con Leaflet (HU-002, HU-005, #24).
- Frontend: panel de la tienda con apertura, horario, domicilio y medios de pago (HU-009, HU-010, HU-012, HU-013, #26).
- Frontend: publicar solicitud (HU-016, #27).
- Frontend: revisión de tiendas por el administrador (HU-036, #28).
- Frontend: perfil y direcciones del cliente (HU-007, #29).
- Frontend: identidad visual «Verde botica», Atkinson Hyperlegible e íconos (#30, SC-008).
- Backend: `GET /api/v1/categorias` y tabla `catalogo.categoria` con Farmacia y Ferretería (#31, SC-007).

### Corregido
- La prueba de restauración de respaldos espera al servidor definitivo de PostGIS (#25).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43297877152](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43297877152) · 12/11/2026 15:11 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43297877152](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43297877152) · 12/11/2026 15:11 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [43291603040](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43291603040) · 12/11/2026 14:34 (commit `d5005aa`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43298859794](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43298859794) · 12/11/2026 15:17 | ✅ Correcto |

Pruebas del backend en esta versión: 256 (209 unitarias y 47 de integración), 0 fallas, 97,2 % de cobertura de líneas en `domain` y `application` (corte del Sprint 1, PR #32, `docs/pruebas/evidencias/C1/2026-11-12/`). Las 47 de integración se confirmaron en el registro del job de CI.

## v0.1.2 · 11/11/2026

Commit `826c359`.

### Agregado
- Frontend: simulación de la API con MSW para trabajar sin backend (#21).
- Frontend: registro de cliente con autorización de datos y cierre de sesión (HU-001, HU-003, HU-005, #22).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43156152498](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152498) · 11/11/2026 23:38 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43156152498](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152498) · 11/11/2026 23:38 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [43156152502](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152502) · 11/11/2026 23:38 (este commit) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43156648125](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156648125) · 11/11/2026 23:43 | ✅ Correcto |

## v0.1.1 · 11/11/2026

Commit `b117133`.

### Seguridad
- Caddy 2.11.9 compilado con `golang.org/x/net` corregido para pasar Trivy; el despliegue de `v0.1.0` se había detenido (#20, SC-006).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43150490243](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150490243) · 11/11/2026 22:47 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43150490243](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150490243) · 11/11/2026 22:47 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [42497112512](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/42497112512) · 08/11/2026 10:53 (commit `cdb743b`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43150916232](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150916232) · 11/11/2026 22:51 | ✅ Correcto |

## v0.1.0 · 11/11/2026 · Primera versión desplegada

Commit `41d19b8`. Incluye el Sprint 0 y el backend del Sprint 1.

### Agregado
- Base técnica: `pom.xml`, perfiles, Compose con PostGIS, primera migración y CI (#1).
- `shared`: errores con ProblemDetail, correlación y reloj (T-01, #2); seguridad con JWT RS256, roles y Argon2id (T-02, #3).
- `identidad`: registro de cliente (HU-001, #4), autorización de datos (HU-005, #5), inicio, renovación y cierre de sesión (HU-003, #6), segundo factor TOTP del administrador (HU-004, #8).
- `solicitudes`: publicar con límite de 10 por hora (HU-016, #7, SC-001, SC-002, SC-003) y difusión a tiendas elegibles (HU-017, #13).
- `tiendas`: registro con documentos (HU-002, #9), perfil y horario (HU-009, #11), abrir o cerrar y `TiendasApi` (HU-013, #12), domicilio con tarifa fija (HU-010, #14), medios de pago (HU-012, #15).
- `administracion`: aprobar o rechazar tiendas con auditoría (HU-036, #10).
- `clientes`: perfil y direcciones (HU-007, #16).
- Frontend: andamiaje React + Vite + TypeScript, decisiones F-01 a F-23 y WCAG 2.2 AA (#17, SC-004, SC-005).
- Despliegue: imágenes, Compose de producción, despliegue por etiquetas y respaldos (#19).

### Seguridad
- Frontend: `minimatch` y `glob` vulnerables corregidos con un `override` de `glob` 14 (#18).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43148068473](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148068473) · 11/11/2026 22:25 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43148068473](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148068473) · 11/11/2026 22:25 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [42497112512](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/42497112512) · 08/11/2026 10:53 (commit `cdb743b`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43148805407](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148805407) · 11/11/2026 22:31 | ❌ Falló en «Trivy (imágenes)»: vulnerabilidades críticas en Caddy; se corrigió en v0.1.1 (SC-006) |

## Antes de los pull requests · 28/10/2026 y 29/10/2026

Commits directos a `main` (`289b02f` a `d0a19d2`): estructura inicial, paquete `com.quienlotiene` y plan de proyecto en normas APA.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión: notas de `v0.1.0` a `v0.2.0` reconstruidas desde Git, con la evidencia de calidad de cada versión tomada de GitHub Actions (SC-009) |
| 1.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.3.0` (Sprint 2 y cierre del C1) antes de etiquetar |
| 1.2 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Evidencia de calidad de `v0.3.0` copiada de su Release |
| 1.3 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.3.1` (DEF-011) antes de etiquetar |
| 1.4 | 18/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Evidencia de calidad de `v0.3.1` copiada de su Release y prueba en producción |
| 1.5 | 20/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.4.0` (backend del Sprint 3) antes de etiquetar, preparadas con Claude Code |
| 1.6 | 20/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | `v0.4.0` no se desplegó (Trivy); notas de `v0.4.1` con la corrección de las imágenes, preparadas con Claude Code |
| 1.7 | 21/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.5.0` (frontend del Sprint 3, #67 a #74) antes de etiquetar, preparadas con Claude Code |
| 1.8 | 21/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | «Sin liberar»: menú lateral por rol y página de inicio para `v0.5.1` (SC-020), preparado con Claude Code |
| 1.9 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de «Sin liberar» pasadas a `v0.5.1` (#78), preparado con Claude Code |
| 1.10 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.5.2` (#79, #81 y #82) antes de etiquetar, preparadas con Claude Code |
| 1.11 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.5.3` (#85: DEF-013, DEF-014, DEF-015 y pruebas de los no funcionales del C2) antes de etiquetar, preparadas con Claude Code |
| 1.12 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.5.4` (#89: HU-015 y HU-022; #90: SC-021) antes de etiquetar, preparadas con Claude Code |
| 1.13 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Sin liberar: correcciones de DEF-016, DEF-017 y DEF-018 para la `v0.5.5` |
