# Acta de inicio del ciclo C3

> **Control del documento** · Versión 0.3 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión y firma de QA y del Product Owner) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Anexo B del Plan Maestro. Preparada con Claude Code para que QA y el Product Owner la revisen y la firmen. El plan del ciclo fija el inicio el 23/11. El acta y la regresión automatizada se adelantan al 22/11 porque la versión candidata ya está desplegada.

| Campo | Valor |
|---|---|
| Fecha | 2026-11-22 |
| Ciclo y fechas | C3 · Cierre · 22/11/2026 al 26/11/2026 (plan: 23/11 al 25/11 y humo el 26/11) |
| Versión y commit en pruebas | v0.5.3 · `ff9ac6d` (PR #86) · desplegada en https://quienlotiene.tech el 21/11 a las 11:43 p. m. ([despliegue 13](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44986293432), [Release v0.5.3](https://github.com/Daniel0442123/QuienLoTiene/releases/tag/v0.5.3)) |
| Asistentes | Yanimar Burbano Piamba, Francisco José Castro Muñoz, Wilson Javier Galindez Certuche (por confirmar al firmar) |
| Criterios de entrada verificados (Plan Maestro 6.1) | ☑ Versión desplegada con notas (etiqueta anotada v0.5.3 y `REGISTRO-DE-CAMBIOS.md`) · ☑ Pipeline en verde ([CI run #261](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44985758759): 386 unitarias y 67 de integración, 0 fallas) · ☑ Datos de prueba cargados (las pruebas automatizadas crean sus datos con Testcontainers; en producción, las cuentas `@prueba.quienlotiene.tech`) · ☑ Libro y carpeta de evidencias listos (`05-EJECUCION-C3.md`, `evidencias/C3/`) · ☑ S1/S2 del ciclo anterior corregidos o diferidos (el C2 cerró con 0 S1 y 0 S2; DEF-013 a DEF-015 (S3) cerrados el 22/11) |
| Alcance confirmado | La regresión completa de los 85 casos funcionales y de seguridad, más los casos del Cierre: 10 de historias Could, 5 de punta a punta (E2E-01 a E2E-05) y 13 no funcionales sobre el VPS (ver `02-PLAN-DE-PRUEBAS-C3.md`) |
| Pendientes o excepciones | Se detallan después de las firmas. |
| Decisión | ☑ Se inicia el ciclo (propuesta, pendiente de firma) · ☐ Se pospone hasta: |

**Pendientes o excepciones:**

1. Las historias Could HU-006 (verificar correo), HU-011 (tarifas de domicilio avanzadas), HU-015 (resumen de indicadores de la tienda) y HU-022 (ampliar el radio automáticamente) están planeadas para el Sprint de Cierre (23/11 al 25/11), pero no estaban implementadas al hacer la regresión. El mismo 22/11 Daniel Montenegro decidió hacer HU-015 y HU-022 y sacar HU-006 y HU-011 (SC-021, aprobada por el Product Owner el mismo día). HU-015 y HU-022 se implementaron y sus 4 casos se ejecutan en este ciclo. Los 4 de HU-006 y HU-011 quedan «No aplica» por SC-021, como permite el objetivo 4 del plan del C3.
2. HU-008 se adelantó al Sprint 3 por SC-018. CP-CLI-008-01 a 03 ya se aprobaron en el C2 y en el C3 entran a la regresión.
3. DEF-012 (S4, «Elegida · Prepara el pedido.» en pedidos cerrados) sigue abierto. El triaje decide si se corrige en el C3 o se difiere con plan.
4. Todavía no existen las pruebas de punta a punta con Playwright ni los guiones de carga con k6. Se escriben en este ciclo.
5. NF-DIS-018 necesita los datos del monitor externo entre el 16/11 y el 26/11. NF-COM-026 y NF-USA-027 necesitan pruebas manuales en un iPhone, en Safari y con un usuario.

| Rol | Nombre | Firma |
|---|---|---|
| Líder de QA | Yanimar Burbano Piamba | |
| QA no funcional y seguridad | Francisco José Castro Muñoz | |
| Product Owner | Wilson Javier Galindez Certuche | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador del acta de inicio del C3 sobre `v0.5.3`, preparado con Claude Code para que QA y el Product Owner lo revisen y firmen |
| 0.2 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Excepción 1: HU-015 y HU-022 implementadas; HU-006 y HU-011 salen por SC-021 |
| 0.3 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | SC-021 aprobada |
