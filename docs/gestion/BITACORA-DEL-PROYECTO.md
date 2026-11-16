# Bitácora del proyecto · Quién lo tiene

> **Control del documento** · Versión 1.1 · 13/11/2026 · Responsable: Brayan Grajales Escarraga (Scrum Master) · Estado: Vigente · Procedimiento: [`PROCEDIMIENTO-DE-SEGUIMIENTO.md`](PROCEDIMIENTO-DE-SEGUIMIENTO.md)

Registro cronológico de **qué se hizo, cuándo y quién lo hizo** en el proyecto. Cubre los pull requests (sección 2, generada), los hechos que no pasan por un pull request (sección 3) y los compromisos que quedaron abiertos (sección 4). Las desviaciones del plan no se describen aquí: se registran como solicitudes de cambio en [`CONTROL-DE-CAMBIOS.md`](CONTROL-DE-CAMBIOS.md) y aquí solo se cita su número.

Normas que atiende: ISO/IEC/IEEE 12207:2017 (6.3.2 seguimiento y control del proyecto, 6.3.6 gestión de la información), ISO 9001:2015 (7.5.3 control de la información documentada, 9.1 seguimiento y medición).

---

## 1. Cómo leer y mantener esta bitácora

- **Una fila por pull request.** La tabla de la sección 2 la genera `scripts/gestion/bitacora.py` desde GitHub; no se edita a mano entre los marcadores.
- **Cuándo se regenera:** al cerrar cada día de trabajo con fusiones, en cada corte de sprint y antes de crear una etiqueta `v*`. La regenera el Scrum Master (o quien él delegue) en un PR `docs(gestion): actualiza la bitácora`.
- **Horas:** America/Bogota (UTC−5), formato dd/mm/aaaa hh:mm.
- **«Aprobación formal en GitHub»:** «Sí» solo cuando el PR tiene una revisión *Approve* registrada. Fusionar el PR no cuenta como aprobación.
- **«Asistido por IA»:** «Sí» cuando la descripción del PR declara que se elaboró con Claude Code. Se registra por transparencia; el responsable sigue siendo el autor del PR.
- **Versión:** primera etiqueta `v*` que contiene el cambio («Sin liberar» si todavía no hay etiqueta).

## 2. Pull requests

Fuente: API de GitHub del repositorio `Daniel0442123/QuienLoTiene`, consultada el 13/11/2026. Para los PR #1 a #31 se comprobó en GitHub que **ninguno tiene revisiones registradas** (búsqueda `review:none` y lectura de las revisiones de #1, #2, #3, #10 y #22). La columna «Fusionó» se tomó de GitHub para #4, #21, #22, #23, #24, #26, #27, #28, #29 y #30; en el resto se completó con la misma cuenta y se confirma la próxima vez que se ejecute el script con `GITHUB_TOKEN`.

<!-- inicio-bitacora -->
| PR | Qué se hizo | Tipo · módulo | HU / tarea | SC | Autor (quién) | Abierto | Fusionado (cuándo) | Fusionó | Aprobación formal en GitHub | Versión | Estado | Asistido por IA |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| [#1](https://github.com/Daniel0442123/QuienLoTiene/pull/1) | chore: base técnica del Sprint 0 | Mantenimiento | — | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 19:19 | 07/11/2026 19:23 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#2](https://github.com/Daniel0442123/QuienLoTiene/pull/2) | feat(shared): base común de errores, correlación y reloj (T-01) | Funcionalidad · shared | HU-001, HU-002, T-01, T-02 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 19:36 | 07/11/2026 20:45 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#3](https://github.com/Daniel0442123/QuienLoTiene/pull/3) | feat(shared): seguridad con JWT RS256, roles y Argon2id (T-02) | Funcionalidad · shared | T-01, T-02 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 19:46 | 07/11/2026 20:54 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#4](https://github.com/Daniel0442123/QuienLoTiene/pull/4) | feat(identidad): registrar cliente (HU-001) | Funcionalidad · identidad | HU-001, T-01, T-02 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 19:54 | 07/11/2026 21:01 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#5](https://github.com/Daniel0442123/QuienLoTiene/pull/5) | feat(identidad): aceptar política de datos en el registro (HU-005) | Funcionalidad · identidad | HU-001, HU-005 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 20:08 | 07/11/2026 21:09 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#6](https://github.com/Daniel0442123/QuienLoTiene/pull/6) | feat(identidad): iniciar y cerrar sesión con refresco rotativo (HU-003) | Funcionalidad · identidad | HU-003, HU-005 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 20:19 | 07/11/2026 21:17 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#7](https://github.com/Daniel0442123/QuienLoTiene/pull/7) | feat(solicitudes): publicar solicitud con límite de 10 por hora (HU-016) | Funcionalidad · solicitudes | HU-003, HU-016 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 20:31 | 07/11/2026 21:36 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#8](https://github.com/Daniel0442123/QuienLoTiene/pull/8) | feat(identidad): segundo factor TOTP del administrador (HU-004) | Funcionalidad · identidad | HU-004 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 21:55 | 08/11/2026 08:53 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#9](https://github.com/Daniel0442123/QuienLoTiene/pull/9) | feat(tiendas): registrar tienda con documentos (HU-002) | Funcionalidad · tiendas | HU-002, HU-004 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 22:11 | 08/11/2026 08:57 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#10](https://github.com/Daniel0442123/QuienLoTiene/pull/10) | feat(administracion): aprobar o rechazar tiendas con auditoría (HU-036) | Funcionalidad · administracion | HU-002, HU-004, HU-036 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 22:20 | 08/11/2026 09:01 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#11](https://github.com/Daniel0442123/QuienLoTiene/pull/11) | feat(tiendas): perfil y horario de la tienda (HU-009) | Funcionalidad · tiendas | HU-009, HU-036 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 22:31 | 08/11/2026 09:05 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#12](https://github.com/Daniel0442123/QuienLoTiene/pull/12) | feat(tiendas): abrir o cerrar tienda y TiendasApi (HU-013) | Funcionalidad · tiendas | HU-009, HU-013 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 22:48 | 08/11/2026 09:08 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#13](https://github.com/Daniel0442123/QuienLoTiene/pull/13) | feat(solicitudes): difundir a tiendas elegibles (HU-017) | Funcionalidad · solicitudes | HU-013, HU-017 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 23:07 | 08/11/2026 09:14 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#14](https://github.com/Daniel0442123/QuienLoTiene/pull/14) | feat(tiendas): domicilio con tarifa fija (HU-010) | Funcionalidad · tiendas | HU-010, HU-017 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 23:14 | 08/11/2026 09:18 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#15](https://github.com/Daniel0442123/QuienLoTiene/pull/15) | feat(tiendas): medios de pago de la tienda (HU-012) | Funcionalidad · tiendas | HU-010, HU-012 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 23:21 | 08/11/2026 09:21 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#16](https://github.com/Daniel0442123/QuienLoTiene/pull/16) | feat(clientes): perfil y direcciones del cliente (HU-007) | Funcionalidad · clientes | HU-007, HU-012 | — | Daniel Montenegro Almendra (@Daniel0442123) | 07/11/2026 23:29 | 08/11/2026 09:25 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#17](https://github.com/Daniel0442123/QuienLoTiene/pull/17) | feat(front): andamiaje del frontend y decisiones de usabilidad y accesibilidad | Funcionalidad · front | HU-003 | — | Daniel Montenegro Almendra (@Daniel0442123) | 08/11/2026 10:22 | 08/11/2026 10:25 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#18](https://github.com/Daniel0442123/QuienLoTiene/pull/18) | fix(front): corrige las alertas de Dependabot de minimatch y glob | Corrección · front | — | — | Daniel Montenegro Almendra (@Daniel0442123) | 08/11/2026 10:51 | 08/11/2026 10:53 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#19](https://github.com/Daniel0442123/QuienLoTiene/pull/19) | build(despliegue): imágenes, compose de producción, despliegue por etiquetas y respaldos | Construcción y despliegue · despliegue | — | — | Daniel Montenegro Almendra (@Daniel0442123) | 11/11/2026 22:19 | 11/11/2026 22:25 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.0 | Fusionado | Sí |
| [#20](https://github.com/Daniel0442123/QuienLoTiene/pull/20) | fix(despliegue): compila Caddy 2.11.9 con golang.org/x/net corregido para pasar Trivy | Corrección · despliegue | — | — | Daniel Montenegro Almendra (@Daniel0442123) | 11/11/2026 22:43 | 11/11/2026 22:47 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.1 | Fusionado | Sí |
| [#21](https://github.com/Daniel0442123/QuienLoTiene/pull/21) | feat(front): simula la API con MSW para trabajar sin backend | Funcionalidad · front | — | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 11/11/2026 23:10 | 11/11/2026 23:36 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.2 | Fusionado | Sí |
| [#22](https://github.com/Daniel0442123/QuienLoTiene/pull/22) | feat(front): pantalla de registro de cliente con autorización de datos | Funcionalidad · front | HU-001, HU-003, HU-005 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 11/11/2026 23:11 | 11/11/2026 23:38 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.1.2 | Fusionado | Sí |
| [#23](https://github.com/Daniel0442123/QuienLoTiene/pull/23) | feat(front): pantalla del código TOTP del administrador | Funcionalidad · front | HU-004 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 11/11/2026 23:40 | 12/11/2026 00:34 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#24](https://github.com/Daniel0442123/QuienLoTiene/pull/24) | feat(front): pantalla de registro de tienda con documentos y mapa | Funcionalidad · front | HU-002, HU-005 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 00:14 | 12/11/2026 00:37 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#25](https://github.com/Daniel0442123/QuienLoTiene/pull/25) | fix(respaldo): la prueba de restauración espera al servidor definitivo de PostGIS | Corrección · respaldo | — | — | Daniel Montenegro Almendra (@Daniel0442123) | 12/11/2026 00:22 | 12/11/2026 00:29 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#26](https://github.com/Daniel0442123/QuienLoTiene/pull/26) | feat(front): panel de la tienda con apertura, horario, domicilio y medios de pago | Funcionalidad · front | HU-009, HU-010, HU-012, HU-013 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 06:26 | 12/11/2026 08:23 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#27](https://github.com/Daniel0442123/QuienLoTiene/pull/27) | feat(front): pantalla para publicar solicitud | Funcionalidad · front | HU-016 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 07:29 | 12/11/2026 08:42 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#28](https://github.com/Daniel0442123/QuienLoTiene/pull/28) | feat(front): pantalla del administrador para revisar tiendas | Funcionalidad · front | HU-036 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 08:17 | 12/11/2026 08:46 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#29](https://github.com/Daniel0442123/QuienLoTiene/pull/29) | feat(front): pantalla de perfil y direcciones del cliente | Funcionalidad · front | HU-007 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 09:07 | 12/11/2026 14:32 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#30](https://github.com/Daniel0442123/QuienLoTiene/pull/30) | feat(front): identidad visual con la paleta Verde botica, Atkinson Hyperlegible e íconos | Funcionalidad · front | — | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 09:28 | 12/11/2026 14:34 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#31](https://github.com/Daniel0442123/QuienLoTiene/pull/31) | feat(catalogo): listar categorías en GET /api/v1/categorias | Funcionalidad · catalogo | HU-002, HU-016 | — | Daniel Montenegro Almendra (@Daniel0442123) | 12/11/2026 14:59 | 12/11/2026 15:11 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | v0.2.0 | Fusionado | Sí |
| [#32](https://github.com/Daniel0442123/QuienLoTiene/pull/32) | docs(pruebas): corte del Sprint 1 del ciclo C1 sobre v0.2.0 | Documentación · pruebas | HU-001 | — | Daniel Montenegro Almendra (@Daniel0442123) | 12/11/2026 15:59 | 12/11/2026 23:49 | Daniel Montenegro Almendra (@Daniel0442123) | No registrada | Sin liberar | Fusionado | Sí |
| [#33](https://github.com/Daniel0442123/QuienLoTiene/pull/33) | docs(diseno): mockups de las historias pendientes del Sprint 2 en adelante | Documentación · diseno | HU-015, HU-018, HU-019, HU-023, HU-035, HU-037 | — | Monica Anyi Cajas Mellizo (@monicacajas-art) | 12/11/2026 21:29 | — | — | No registrada | — | Abierto | Sí |

_Tabla generada el 12/11/2026 23:54 (hora de Bogotá) con `scripts/gestion/bitacora.py`._
<!-- fin-bitacora -->

### Hallazgos de la reconstrucción (13/11/2026)

| # | Hallazgo | Evidencia | Tratamiento |
|---|---|---|---|
| H-01 | Ningún PR tiene aprobación registrada, aunque D-22 y el CODEOWNERS exigen la aprobación del arquitecto | 0 revisiones en los 33 PR | SC-010 (aprobación obligatoria y segundo revisor para los PR del arquitecto) |
| H-02 | Varios PR se fusionaron entre 2 y 7 minutos después de abrirse | #1 (4 min), #17 (3 min), #18 (2 min), #19 (6 min), #20 (3 min), #25 (7 min) | Regla de revisión del procedimiento de control de cambios, sección 5 |
| H-03 | Desviaciones del plan escritas solo dentro del PR, sin número ni aprobador | #7, #17, #20, #24 y #31 | Registradas hacia atrás como SC-001 a SC-008 |
| H-04 | Etiquetas `v0.1.0` a `v0.2.0` ligeras (sin fecha, autor ni notas) | `git for-each-ref refs/tags` | Notas en `REGISTRO-DE-CAMBIOS.md`; en adelante `git tag -a` (procedimiento documental, sección 6) |
| H-05 | 42 documentos MD sin encabezado de versión ni historial, incluidos los 10 registros nuevos del corte C1 (#32) | Revisión de `docs/` | Los 42 se completaron con su historial reconstruido desde Git en este cambio (los del libro de QA después de fusionar #32); `prompt-claude-design.md` es un texto para pegar en otra herramienta y queda fuera |
| H-06 | PDF de arquitectura y plan de proyecto atrasados frente a sus Word (WCAG 2.1) | PR #17, sección «Pendiente» | Compromiso C-01 |
| H-07 | Los PR no usan etiquetas, hitos ni responsable asignado (solo #19, #20, #22 y #25 tienen responsable) | API de GitHub | Procedimiento de seguimiento, sección 4 |
| H-08 | Las historias de `tiendas`, `clientes` y `administracion` las implementó otra persona distinta a la del backlog, sin registro del cambio | PR #9 a #16 frente a `BACKLOG-SPRINT-1.md` sección 2 | SC-012 y compromiso C-11 |
| H-09 | Las fechas del Sprint 1 cambiaron frente al plan de proyecto sin registro de la decisión | `BACKLOG-SPRINT-1.md` («Ojo con las fechas») | SC-013 y compromiso C-11 |
| H-10 | Los resultados de pruebas y la cobertura de cada versión solo viven como artefactos de GitHub Actions, que se borran a los 14 días; ninguna etiqueta tiene Release | `ci.yml` (`retention-days: 14`), API de releases | Flujo `version.yml` y compromiso C-14 |

## 3. Hechos fuera de pull requests

Decisiones, aprobaciones, despliegues, etiquetas y reuniones que no quedan en un PR. Una fila por hecho, en orden cronológico.

| Fecha | Hecho | Quién | Evidencia | SC / PR relacionado |
|---|---|---|---|---|
| 28/10/2026 | Estructura inicial del proyecto y paquete base `com.quienlotiene` (commits directos a `main`, antes de exigir PR) | Daniel Montenegro Almendra (@daniel0442) | `289b02f`, `9a823a4` | — |
| 29/10/2026 | Plan de proyecto en normas APA con portada (commits directos a `main`) | Daniel Montenegro Almendra (@daniel0442) | `0e278bf`, `ee48a04`, `d0a19d2` | — |
| 07/11/2026 | Aprobación de `DECISIONES-BACKEND.md` 1.0 (D-01 a D-23) | Pedro (dueño del proyecto) | Historial de `DECISIONES-BACKEND.md` | — |
| 07/11/2026 | Plan Maestro de Pruebas 1.0 | Yanimar Burbano Piamba, Francisco José Castro Muñoz | Control de cambios del Plan Maestro | — |
| 08/11/2026 | Aprobación de `DECISIONES-FRONTEND.md` 1.0 a 1.2 (F-01 a F-23) | Pedro (dueño del proyecto) | Historial de `DECISIONES-FRONTEND.md` | SC-004, SC-005 |
| 11/11/2026 | Etiqueta `v0.1.0` y primer despliegue al VPS (falló en Trivy) | Daniel Montenegro Almendra | PR #20, sección «Historia» | SC-006 |
| 11/11/2026 | Etiqueta `v0.1.1`: despliegue con Caddy 2.11.9 | Daniel Montenegro Almendra | `b117133` | SC-006 |
| 11/11/2026 | Etiqueta `v0.1.2` | Daniel Montenegro Almendra | `826c359` | — |
| 12/11/2026 | Respaldos con Backblaze B2 en marcha; prueba de restauración corregida | Daniel Montenegro Almendra | PR #25 | — |
| 12/11/2026 | Etiqueta `v0.2.0` (cierre funcional del Sprint 1) | Daniel Montenegro Almendra | `ea7351d` | — |
| 12/11/2026 | Corte del Sprint 1 del ciclo C1 sobre `v0.2.0`; fusionado a las 23:49 sin revisión registrada | Yanimar Burbano Piamba (QA), PR de Daniel Montenegro Almendra | PR #32 | — |
| 13/11/2026 | Se monta el seguimiento y control de cambios del proyecto (`docs/gestion/`) | Daniel Montenegro Almendra, a pedido de Pedro | Este PR | SC-009, SC-010 |

## 4. Compromisos abiertos

Lo que un PR, una reunión o una revisión dejó pendiente. Se cierra con la fecha y el PR que lo resolvió; nunca se borra la fila.

| ID | Compromiso | Origen | Responsable | Fecha compromiso | Estado | Cerrado en |
|---|---|---|---|---|---|---|
| C-01 | Exportar de nuevo a PDF la arquitectura y el plan de proyecto (todavía dicen WCAG 2.1) | PR #17 | Daniel Montenegro Almendra | 15/11/2026 | Abierto | |
| C-02 | Autenticar el SSE con `Authorization`, Caddy sirviendo `frontend/dist` y endpoint de la llave pública VAPID (DECISIONES-FRONTEND 15.2) | PR #17 | Daniel Montenegro Almendra | Sprint 2 | Abierto | |
| C-03 | Acordar la Content-Security-Policy con el frontend | PR #19 | Daniel Montenegro Almendra, Monica Anyi Cajas Mellizo | Sprint 2 | Abierto | |
| C-04 | Generar `frontend/src/services/api-tipos.ts` desde OpenAPI y borrar los tipos provisionales | PR #21, #22 | Monica Anyi Cajas Mellizo | Sprint 2 | Abierto | |
| C-05 | Validar que `categoriaId` exista al registrar tienda y al publicar solicitud | PR #7, #31 | Daniel Montenegro Almendra | Sprint 2 | Abierto | |
| C-06 | Exponer los medios de pago por `TiendasApi` para la orden (HU-024) | PR #15 | Daniel Montenegro Almendra | Sprint 2 | Abierto | |
| C-07 | Aprobar el texto de la política de datos 1.0 (hoy es borrador en `politicaDatos.ts`) | PR #22 | Wilson Javier Galindez Certuche | 15/11/2026 | Abierto | |
| C-08 | Revisar el diagrama `09-entidad-relacion` frente a las columnas `radio_metros` y `creado_en` | SC-003 | Daniel Montenegro Almendra | 15/11/2026 | Abierto | |
| C-09 | Activar en GitHub la protección de `main` con una aprobación obligatoria y el chequeo «Control de cambios» | SC-010 | Pedro (dueño del repositorio) | 14/11/2026 | Abierto | |
| C-10 | Agregar las secciones «Control de cambios» y «Documentos actualizados» a la descripción de #33 (#32 se fusionó antes de que existiera el chequeo) | Este PR | Monica Anyi Cajas Mellizo | Antes de fusionar #33 | Abierto | |
| C-11 | Documentar el motivo y la aprobación de SC-012 (reasignación) y SC-013 (fechas del Sprint 1) | SC-012, SC-013 | Brayan Grajales Escarraga | 15/11/2026 | Abierto | |
| C-12 | Agregar el endpoint de categorías a la trazabilidad y a los casos (`docs/pruebas/libro/`) cuando se fusione #32 | SC-007 | Yanimar Burbano Piamba | Después de #32 | Abierto | |
| C-13 | Agregar encabezado de control e historial a los archivos de `docs/pruebas/libro/`, defectos e informes del C1, y versión al Excel del libro de QA | H-05 | Daniel Montenegro Almendra | Después de #32 | Cerrado | 13/11/2026, PR #34 |
| C-14 | Ejecutar «Documentar versión» (`version.yml`, Run workflow) para `v0.1.0`, `v0.1.1`, `v0.1.2` y `v0.2.0`, antes de que venzan sus artefactos de pruebas | Este PR | Daniel Montenegro Almendra | 25/11/2026 | Abierto | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión: bitácora reconstruida desde el PR #1 hasta el #33, hechos fuera de PR y compromisos abiertos |
| 1.1 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Fusión de #32: tabla regenerada, hecho del corte, C-10 y C-13 actualizados |
