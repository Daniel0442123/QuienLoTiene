# Control de cambios · Quién lo tiene

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Aprobó: Pedro (dueño del proyecto), 13/11/2026 · Estado: Vigente

Procedimiento y registro de las **solicitudes de cambio (SC)**: todo lo que se aparta de lo planeado o aprobado, por cualquier motivo, queda aquí con su motivo, su impacto, quién lo aprobó, cuándo, en qué PR se hizo y qué documentos se ajustaron.

Normas que atiende: ISO 10007:2017 (control de cambios de la configuración), ISO/IEC/IEEE 12207:2017 (6.3.5 gestión de la configuración), ISO 9001:2015 (6.3 planificación de los cambios y 8.5.6 control de los cambios), ISO/IEC/IEEE 29119-3 (actualización de la documentación de pruebas cuando cambia la base de prueba).

---

## 1. Qué es una desviación del plan

Un cambio necesita una SC cuando modifica algo que ya estaba aprobado o planeado:

| Línea base | Ejemplos | Documento que manda |
|---|---|---|
| Alcance y requisitos | Agregar, quitar o adelantar una HU; cambiar un criterio de aceptación o un caso de prueba | SRS, `BACKLOG-SPRINT-n.md`, `docs/pruebas/libro/02-CASOS.md` |
| Cronograma y responsables | Mover una historia de sprint, correr fechas, reasignar el trabajo de un integrante | Plan de proyecto, backlog del sprint |
| Arquitectura y decisiones | Cambiar una decisión D-xx, F-xx o DA-xx; usar otra librería o patrón | `DECISIONES-BACKEND.md`, `DECISIONES-FRONTEND.md`, documento de arquitectura |
| Contrato de la API | Cambiar un endpoint, un código HTTP o un código de error publicado | OpenAPI, `docs/api/CODIGOS-DE-ERROR.md` |
| Modelo de datos | Columnas o tablas distintas del diagrama | `docs/diagramas/09-entidad-relacion.drawio` |
| Dependencias e infraestructura | Dependencia o herramienta no prevista; cambio en el VPS, Docker o el pipeline | `pom.xml`, `package.json`, `DESPLIEGUE.md` |
| Proceso | Cambiar cómo se revisa, prueba, versiona o documenta | Este documento, Plan Maestro de Pruebas |

**No** necesita SC: implementar lo que ya dice el plan, corregir un defecto sin cambiar el comportamiento esperado, refactorizar sin cambiar contratos, actualizar una dependencia por seguridad dentro de la misma versión mayor (se anota en la bitácora).

## 2. Clasificación y quién aprueba

| Clase | Cuándo | Aprueba | Plazo |
|---|---|---|---|
| **Menor** | Afecta un solo módulo, no cambia alcance ni fechas, se revierte en un PR | Arquitecto de software (Daniel Montenegro Almendra) | En la revisión del PR |
| **Mayor** | Cambia alcance, fechas, un contrato que usa otro integrante, una decisión aprobada por el dueño del proyecto o el proceso | Comité de control de cambios: Product Owner (Wilson Javier Galindez Certuche), arquitecto de software y Scrum Master (Brayan Grajales Escarraga); el líder de QA (Yanimar Burbano Piamba) si toca casos o el plan de pruebas | Antes de fusionar el PR |
| **Urgente** | Producción caída o vulnerabilidad crítica | Arquitecto de software, y se ratifica en el comité dentro de las 24 horas | Inmediato |

Cuando el autor del cambio es quien debe aprobarlo, aprueba otro integrante del comité (SC-010).

## 3. Pasos

1. **Registrar.** Quien detecta la desviación agrega una fila en la sección 5 con el siguiente número libre, estado `Propuesta`. Si es mayor, llena además la plantilla [`plantillas/SOLICITUD-DE-CAMBIO.md`](plantillas/SOLICITUD-DE-CAMBIO.md) en `docs/gestion/solicitudes/SC-nnn.md` (o el formato Word `entregables/Formato-Solicitud-de-Cambio.docx`).
2. **Analizar el impacto.** Alcance, cronograma, calidad (casos de prueba), riesgos y la lista de documentos afectados (sección 4).
3. **Decidir.** El aprobador de la sección 2 la marca `Aprobada`, `Rechazada` o `Diferida`, con su nombre y la fecha. La decisión puede quedar en la revisión del PR («Approve» con el número SC-nnn en el comentario) o en el acta de la reunión.
4. **Implementar.** El PR cita la SC en su sección «Control de cambios» (el flujo `Control de cambios` de GitHub Actions comprueba que la SC exista en este registro).
5. **Actualizar la documentación en el mismo PR.** Cada documento afectado sube de versión y anota el cambio en su «Historial del documento» con el número de la SC (procedimiento documental). Si un documento no se puede actualizar en ese PR, se deja como compromiso abierto en la bitácora.
6. **Verificar y cerrar.** QA confirma que los casos afectados se actualizaron y pasan; la SC queda `Cerrada` con la fecha.

Estados: `Propuesta → En análisis → Aprobada | Rechazada | Diferida → Implementada → Cerrada`. Un registro hecho después del cambio se marca `Implementada · por ratificar` hasta que el aprobador lo confirme.

## 4. Documentos que se revisan según el tipo de cambio

| Si cambia… | Actualizar |
|---|---|
| Una HU o un criterio de aceptación | SRS, backlog del sprint, `02-CASOS.md`, `07-TRAZABILIDAD.md`, plan del ciclo (`02-PLAN-DE-PRUEBAS-Cn.md`) y su Word |
| Un endpoint o un código de error | `CODIGOS-DE-ERROR.md`, OpenAPI (sale del código), mensajes del frontend (`mensajes-error.ts`), casos de prueba que lo verifican |
| Una decisión D-xx o F-xx | `DECISIONES-*.md` (tabla y sección), documento de arquitectura Word y PDF |
| El modelo de datos | `09-entidad-relacion.drawio` y `entidad-relacion.png`, diagrama de clases si aplica |
| Fechas o responsables | Plan de proyecto (Word y PDF), backlog, `01-PARAMETROS-Y-EQUIPO.md` |
| Dependencias o infraestructura | `DECISIONES-*.md` (sección de dependencias), `DESPLIEGUE.md`, `08-despliegue.drawio` |
| El proceso | Este documento, `PROCEDIMIENTO-DE-SEGUIMIENTO.md`, `PROCEDIMIENTO-DOCUMENTAL.md`, plantilla del PR |

## 5. Registro de solicitudes de cambio

Las SC-001 a SC-008, SC-012 y SC-013 se registraron el 13/11/2026 al reconstruir la historia del proyecto: los cambios ya estaban hechos y explicados dentro de su PR, pero sin número ni aprobación formal.

| SC | Fecha | Solicitó | Qué cambia frente al plan | Motivo | Clase | Impacto y documentos afectados | Decisión · aprobó · fecha | PR | Documentos actualizados | Estado |
|---|---|---|---|---|---|---|---|---|---|---|
| SC-001 | 07/11/2026 | Daniel Montenegro Almendra | `SOL-001` (límite de solicitudes) responde `429` y no `422` | El caso CP-SOL-016-03 pide HTTP 429 con el tiempo de espera | Menor | Contrato de la API; `DECISIONES-BACKEND` 7, 8 y 11; `CODIGOS-DE-ERROR.md`; frontend (#27) | Fusionado por Daniel Montenegro Almendra sin revisión registrada | #7 | `DECISIONES-BACKEND.md`, `CODIGOS-DE-ERROR.md` (en #7); historial de `DECISIONES-BACKEND.md` 1.2 (13/11) | Implementada · por ratificar |
| SC-002 | 07/11/2026 | Daniel Montenegro Almendra | El límite de 10 solicitudes por hora se cuenta en PostgreSQL y no con Bucket4j en memoria | Exacto para «la última hora», sobrevive a reinicios y se prueba con un reloj fijo | Menor | `DECISIONES-BACKEND` 11 seguía diciendo Bucket4j | Fusionado por Daniel Montenegro Almendra sin revisión registrada | #7 | `DECISIONES-BACKEND.md` 1.2, sección 11 (13/11) | Implementada · por ratificar |
| SC-003 | 07/11/2026 | Daniel Montenegro Almendra | Columnas `radio_metros` (entero) en vez de `radio_km` y `creado_en` en vez de `creada_en` en `solicitudes.solicitud` | Regla de nombres con unidad (sección 3) y de auditoría (sección 10); `ST_DWithin` trabaja en metros | Menor | Diagrama `09-entidad-relacion` (y su PNG) sigue con los nombres anteriores; `orden` y `calificacion` también dicen `creada_en` | Fusionado por Daniel Montenegro Almendra sin revisión registrada | #7 | Pendiente: compromiso C-08 | Implementada · por ratificar |
| SC-004 | 08/11/2026 | Monica Anyi Cajas Mellizo | Accesibilidad WCAG 2.2 AA en lugar de 2.1 AA | Versión vigente de la norma (DECISIONES-FRONTEND F-01) | Mayor | Plan Maestro, plan C3, casos, trazabilidad, ejecución C3, anexo G, arquitectura y plan de proyecto (Word, Excel y PDF) | Aprobada · Pedro · 08/11/2026 (DECISIONES-FRONTEND 1.0) | #17 | MD, Word y Excel en #17; PDF pendiente: compromiso C-01 | Implementada |
| SC-005 | 08/11/2026 | Monica Anyi Cajas Mellizo | Herramientas del frontend distintas a las decididas: oxlint en vez de ESLint, TypeScript 5.9 en vez de 7, axe-core directo en vez de `vitest-axe`, flujo `frontend.yml` propio | Compatibilidad al crear el andamiaje, con las mismas reglas `jsx-a11y` | Menor | `DECISIONES-FRONTEND.md` | Aprobada · Pedro · 08/11/2026 (DECISIONES-FRONTEND 1.1) | #17 | `DECISIONES-FRONTEND.md` 1.1 | Cerrada |
| SC-006 | 11/11/2026 | Daniel Montenegro Almendra | Caddy 2.11.9 compilado con `golang.org/x/net` corregido en lugar de la imagen oficial `caddy:2.10-alpine` | Trivy detuvo el primer despliegue por vulnerabilidades críticas de la imagen oficial | Urgente | `docker/caddy/Dockerfile`, despliegue | Fusionado por Daniel Montenegro Almendra sin revisión registrada; falta ratificar en el comité | #20 | `docker/caddy/Dockerfile` (en #20) | Implementada · por ratificar |
| SC-007 | 12/11/2026 | Monica Anyi Cajas Mellizo (propuso), Daniel Montenegro Almendra (implementó) | `GET /api/v1/categorias` y la tabla `catalogo.categoria` se adelantan al Sprint 1 (el módulo `catalogo` era del Sprint 3, HU-038) | HU-002 y HU-016 necesitan la lista de categorías en sus formularios | Mayor | Alcance del Sprint 1; `BACKLOG-SPRINT-1.md`; trazabilidad; contrato de la API | Fusionado por Daniel Montenegro Almendra sin revisión registrada; falta aprobación del Product Owner | #24, #31 | `BACKLOG-SPRINT-1.md` 1.1 (13/11); trazabilidad pendiente hasta que se fusione #32 (compromiso C-12) | Implementada · por ratificar |
| SC-008 | 12/11/2026 | Monica Anyi Cajas Mellizo | Dependencia nueva `@fontsource/atkinson-hyperlegible` (OFL-1.1) para la identidad visual | Tipografía para baja visión servida desde la app (DECISIONES-FRONTEND 15.1, punto 4) | Menor | `package.json`, README del frontend | Aprobada por Monica Anyi Cajas Mellizo (licencia) y fusionada por Daniel Montenegro Almendra sin revisión registrada | #30 | README del frontend (en #30) | Implementada · por ratificar |
| SC-009 | 13/11/2026 | Pedro (dueño del proyecto) | Se monta el seguimiento y el control de cambios: `docs/gestion/`, plantilla del PR, chequeo en CI, encabezado e historial en los documentos | Trazabilidad de qué se hizo, cuándo y quién, y control de las desviaciones según ISO | Mayor | Proceso; plantilla del PR; 41 documentos MD, 8 formatos Word y el Excel del libro de QA con su versión; nueva plantilla y libro de control de cambios | Aprobada · Pedro · 13/11/2026 (decisión en el hilo del proyecto: «Todo el plan») | Este PR | Todos los de este PR | Implementada |
| SC-010 | 13/11/2026 | Pedro (dueño del proyecto) | Toda fusión exige una revisión «Approve» registrada en GitHub y el chequeo «Control de cambios» en verde; cuando el autor es el arquitecto, aprueba otro integrante del comité | D-22 pide la aprobación del arquitecto, pero ninguno de los 33 PR la tiene registrada y él mismo es autor de 23 de ellos | Mayor | `DECISIONES-BACKEND` 17 (D-22), CODEOWNERS, protección de `main` | La revisión registrada quedó aprobada por Pedro (13/11/2026) con el plan; falta decidir quién aprueba los PR del arquitecto y activar la protección (compromiso C-09) | Este PR | Plantilla del PR, este documento | Aprobada parcialmente |
| SC-011 | 12/11/2026 | Monica Anyi Cajas Mellizo | Seis decisiones de diseño que no están en los requisitos (precio sugerido en HU-019, motivos de HU-035, periodo de HU-015, aviso de HU-023, confirmación de HU-037, «No lo tengo» con deshacer en HU-018) | Mockups de las historias pendientes | Mayor | SRS, casos de prueba, backend (guardar el último precio) | En análisis: la revisan Wilson Javier Galindez Certuche y Daniel Montenegro Almendra | #33 (abierto) | — | En análisis |
| SC-012 | 08/11/2026 | Por documentar | Las historias de `tiendas`, `clientes` y `administracion` (HU-002, HU-007, HU-009, HU-010, HU-012, HU-013, HU-036) las implementó Daniel Montenegro Almendra; el backlog las asignaba a Deiner Stiven Ramírez Vente | Por documentar | Mayor | Responsables en `BACKLOG-SPRINT-1.md` y en el plan de proyecto | Por documentar (Scrum Master) | #9 a #16 | `BACKLOG-SPRINT-1.md` 1.1 lo deja anotado | Por ratificar |
| SC-013 | 07/11/2026 | Por documentar | Fechas del Sprint 1: el plan decía 02/11 al 08/11, pero el código empezó el 07/11 y el corte se hizo el 12/11 sobre `v0.2.0` | El backlog pedía decidirlo en la planning («Ojo con las fechas») | Mayor | Plan de proyecto, `01-PARAMETROS-Y-EQUIPO.md`, plan C1 | Por documentar: falta registrar qué se decidió en la planning | — | — | Por ratificar |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión: procedimiento de control de cambios y registro de SC-001 a SC-013 (SC-009) |
