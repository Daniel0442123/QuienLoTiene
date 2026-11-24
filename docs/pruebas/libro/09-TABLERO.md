# Hoja «Tablero» · Indicadores de la campaña

> **Control del documento** · Versión 1.17 · 24/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Corte del Sprint 1 registrado el 12/11/2026 sobre v0.2.0. Ejecución del Sprint 2 registrada como borrador el 16/11/2026 sobre main 5e8ab98 (pendiente de firma de QA). Corte del Sprint 3 (C2) registrado como borrador el 22/11/2026 sobre v0.5.2 (`8d64314`); el mismo día se ejecutaron los 7 no funcionales que faltaban sobre la rama de sus correcciones, y los de seguridad se re-probaron en producción sobre v0.5.3. Avance del C3 registrado como borrador el 22/11/2026 sobre `v0.5.4` (`f22f20c`): punta a punta, no funcionales sobre el VPS y «Cómo llegar».
>
> En el Excel se calcula solo con fórmulas sobre las hojas de ejecución y defectos. Aquí se copia el valor al cerrar cada sprint y cada ciclo.

## Avance por ciclo

| Ciclo | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | % avance | % aprobación | Meta |
|---|---|---|---|---|---|---|---|---|
| C1 (Sprints 1 y 2) · corte S1 12/11 | 61 | 38 | 38 | 0 | 0 | 62,3 % | 100 % | 100 % ejecutados |
| C1 (Sprints 1 y 2) · ejecución S2 16/11 (borrador) | 61 | 61 | 61 | 0 | 0 | 100 % | 100 % | 100 % ejecutados |
| C2 (Sprint 3) · corte 22/11 (borrador) | 34 (31 + 3 de HU-008 por SC-018; sin re-pruebas) | 34 | 34 | 0 | 0 | 100 % | 100 % | 100 % ejecutados |
| C3 (Cierre, regresión) · avance 22/11 (borrador) | 104 (86 de regresión y Could + 5 E2E + 13 NF) | 77 | 77 | 0 | 0 | 74,0 % | 100 % | ≥ 95 % aprobados |
| C3 (Cierre, regresión) · avance 22/11 con HU-015 y HU-022 (borrador) | 100 (104 − 4 «No aplica» por SC-021) | 81 | 81 | 0 | 0 | 81,0 % | 100 % | ≥ 95 % aprobados |
| C3 (Cierre) · avance 22/11 sobre v0.5.4 con E2E y no funcionales (borrador) | 100 | 96 | 94 | 2 | 0 | 96,0 % | 97,9 % | ≥ 95 % aprobados |
| C3 (Cierre) · avance 22/11 con la matriz de navegadores (borrador) | 100 | 97 | 95 | 2 | 0 | 97,0 % | 97,9 % | ≥ 95 % aprobados |
| C3 (Cierre) · 22/11 con SC-022 (borrador) | 98 (100 − NF-USA-027 y NF-POR-029 «No aplica») | 97 | 95 | 2 | 0 | 99,0 % | 97,9 % | ≥ 95 % aprobados |
| C3 (Cierre) · 22/11 con la re-prueba sobre v0.5.5 (borrador) | 98 | 97 | 97 | 0 | 0 | 99,0 % | 100 % | ≥ 95 % aprobados |
| C3 (Cierre) · 23/11 con la prueba de usabilidad, SC-023 (borrador) | 99 (100 − NF-POR-029 «No aplica») | 98 | 98 | 0 | 0 | 99,0 % | 100 % | ≥ 95 % aprobados |

## Cobertura por módulo (JaCoCo, líneas de domain + application)

| Módulo | Corte S1 (08/11) | Cierre C1 (20/11; valor del 16/11) | Cierre C2 (22/11) | Final C3 (25/11) | Meta |
|---|---|---|---|---|---|
| identidad | 94,1 % | 93,0 % | 94,0 % | 94,0 % | ≥ 70 % |
| clientes | 97,9 % | 84,4 % | 84,4 % | 84,4 % | ≥ 70 % |
| tiendas | 98,4 % | 92,8 % | 90,5 % | 90,5 % | ≥ 70 % |
| catalogo | 100 % | 100 % | 100 % | 100 % | ≥ 70 % |
| solicitudes | 100 % | 99,6 % | 99,7 % | 99,7 % | ≥ 70 % |
| ordenes | — (sin código) | 99,0 % | 96,8 % | 96,8 % | ≥ 70 % |
| mensajeria | — (sin código) | 100 % | 100 % | 100 % | ≥ 70 % |
| reputacion | — (sin código) |  | 97,9 % | 97,9 % | ≥ 70 % |
| notificaciones | — (sin código) | 100 % | 100 % | 100 % | ≥ 70 % |
| administracion | 100 % | 100 % | 97,9 % | 97,9 % | ≥ 70 % |
| **Total** | 97,2 % (incluye shared) | 95,5 % (shared 77,4 %) | 95,3 % (shared 77,4 %) | 95,5 % (shared 77,4 %; 22/11 con HU-015 y HU-022) | ≥ 70 % |

## Defectos

| Severidad | Abiertos | Corregidos | Cerrados | Diferidos | Meta al liberar |
|---|---|---|---|---|---|
| S1 Crítica | 0 | 0 | 0 | 0 | 0 abiertos |
| S2 Alta | 0 | 0 | 1 (DEF-011) | 0 | 0 abiertos |
| S3 Media | 0 | 0 | 8 (DEF-001, DEF-002, DEF-006, DEF-007, DEF-013, DEF-014, DEF-015, DEF-018) | 0 | ≤ 5 con plan |
| S4 Baja | 0 | 0 | 8 (DEF-003, DEF-004, DEF-005, DEF-008, DEF-009, DEF-010, DEF-016, DEF-017) | 1 (DEF-012, aceptado en el acta con plan para `v1.0.1`) | Sin límite, con plan |

## Indicadores

Valores del 22/11/2026 (avance del C3 con la re-prueba sobre `v0.5.5`, borrador). Los del corte del Sprint 1 están en la versión 1.0 de esta hoja.

| Indicador | Fórmula | Valor |
|---|---|---|
| Avance de ejecución (C3) | Casos ejecutados / casos planeados | 98 / 99 = 99,0 % (falta NF-DIS-018; NF-POR-029 «No aplica» por SC-022; NF-USA-027 aprobado en la prueba de usabilidad de caja negra, SC-023) |
| Tasa de aprobación (C3) | Aprobados / (ejecutados − No aplica) | 97 / 97 = 100 % |
| Tasa de aprobación (campaña, resultado final) | Aprobados / concluyentes | 114 / 114 = 100 % (86 funcionales y de seguridad, 30 no funcionales y 5 E2E; 6 No aplica y 1 sin ejecutar) |
| Casos Alta ejecutados | Casos Alta con resultado / casos Alta | 54 / 54 = 100 % (y E2E-01 a E2E-03) |
| Cobertura de requisitos | Requisitos con todos sus casos ejecutados / requisitos en alcance | 36 RF de 36 (RF-006 y RF-011 fuera por SC-021) y 28 RNF de 29 (RNF-029 fuera por SC-022; RNF-027 verificado parcialmente por SC-023) |
| Densidad de defectos | Defectos registrados / requisitos en alcance | 18 / 66 (36 RF + 30 RNF) = 0,27 |
| Defectos reabiertos | Reabiertos / corregidos | 0 / 17 |
| Cobertura de código | Líneas cubiertas / líneas de domain + application | 2753 / 2884 = 95,5 % |
| Pruebas automatizadas | Total `@Test` (unitarias + integración) · pasan | 470 (398 unitarias + 72 de integración) · 470 pasan; además 361 de Vitest y 5 de Playwright |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 12/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | docs(pruebas): corte del Sprint 1 del ciclo C1 sobre v0.2.0 (#32) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
| 1.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Valores de la ejecución del Sprint 2 del 16/11 (borrador, pendiente de firma de QA) |
| 1.2 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Defectos después de la re-prueba de DEF-005 a DEF-009 (main 55ab5b6) y nuevo DEF-010 |
| 1.3 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-010 en verificación tras su re-prueba (main e679ef3): 0 defectos abiertos |
| 1.4 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Los 10 defectos del C1 pasan a Cerrados (cierre aprobado por Daniel Montenegro el 16/11) |
| 1.5 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Corte del Sprint 3 (C2) sobre v0.5.2: avance, cobertura por módulo y DEF-012 abierto (borrador, pendiente de firma de QA) |
| 1.6 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | C2 con 34 de 34 casos ejecutados, cobertura del 22/11 y DEF-013 a DEF-015 en corrección (borrador, pendiente de firma de QA) |
| 1.7 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-013 a DEF-015 en verificación tras la re-prueba en producción sobre `v0.5.3` |
| 1.8 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-013 a DEF-015 cerrados |
| 1.9 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Avance del C3 al 22/11 (regresión automatizada) y cobertura por módulo sobre `v0.5.3` |
| 1.10 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Avance del C3 con HU-015 y HU-022 aprobadas y HU-006 y HU-011 fuera por SC-021: 81 de 100 |
| 1.11 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Avance del C3 sobre `v0.5.4` (96 de 100, 97,9 %), defectos con DEF-016 a DEF-018 y DEF-011 en S2, e indicadores al 22/11 (borrador, pendiente de firma de QA) |
| 1.12 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | NF-COM-026 aprobado: avance del C3 en 97 de 100 (97,9 %) y campaña en 112 de 114 (98,2 %) (borrador, pendiente de firma de QA) |
| 1.13 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | SC-022: NF-USA-027 y NF-POR-029 «No aplica»; avance del C3 en 97 de 98 |
| 1.14 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Re-prueba sobre `v0.5.5`: NF-ESC-020 y NF-USA-028 aprobados; C3 en 97 de 97 (100 %) y campaña en 114 de 114; DEF-016 a DEF-018 en verificación |
| 1.15 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-016, DEF-017 y DEF-018 cerrados (visto bueno de Daniel Montenegro) |
| 1.16 | 23/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-012 pasa a Diferido (aceptado en el acta con plan) |
| 1.17 | 24/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Prueba de usabilidad de caja negra (SC-023): NF-USA-027 aprobado; C3 en 98 de 99 |
