# Hoja «Tablero» · Indicadores de la campaña

> **Control del documento** · Versión 1.6 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Corte del Sprint 1 registrado el 12/11/2026 sobre v0.2.0. Ejecución del Sprint 2 registrada como borrador el 16/11/2026 sobre main 5e8ab98 (pendiente de firma de QA). Corte del Sprint 3 (C2) registrado como borrador el 22/11/2026 sobre v0.5.2 (`8d64314`); el mismo día se ejecutaron los 7 no funcionales que faltaban sobre la rama de sus correcciones.
>
> En el Excel se calcula solo con fórmulas sobre las hojas de ejecución y defectos. Aquí se copia el valor al cerrar cada sprint y cada ciclo.

## Avance por ciclo

| Ciclo | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | % avance | % aprobación | Meta |
|---|---|---|---|---|---|---|---|---|
| C1 (Sprints 1 y 2) · corte S1 12/11 | 61 | 38 | 38 | 0 | 0 | 62,3 % | 100 % | 100 % ejecutados |
| C1 (Sprints 1 y 2) · ejecución S2 16/11 (borrador) | 61 | 61 | 61 | 0 | 0 | 100 % | 100 % | 100 % ejecutados |
| C2 (Sprint 3) · corte 22/11 (borrador) | 34 (31 + 3 de HU-008 por SC-018; sin re-pruebas) | 34 | 34 | 0 | 0 | 100 % | 100 % | 100 % ejecutados |
| C3 (Cierre, regresión) | 85 + 5 E2E + 13 NF | | | | | | | ≥ 95 % aprobados |

## Cobertura por módulo (JaCoCo, líneas de domain + application)

| Módulo | Corte S1 (08/11) | Cierre C1 (20/11; valor del 16/11) | Cierre C2 (22/11) | Final C3 (25/11) | Meta |
|---|---|---|---|---|---|
| identidad | 94,1 % | 93,0 % | 94,0 % |  | ≥ 70 % |
| clientes | 97,9 % | 84,4 % | 84,4 % |  | ≥ 70 % |
| tiendas | 98,4 % | 92,8 % | 90,5 % |  | ≥ 70 % |
| catalogo | 100 % | 100 % | 100 % |  | ≥ 70 % |
| solicitudes | 100 % | 99,6 % | 99,7 % |  | ≥ 70 % |
| ordenes | — (sin código) | 99,0 % | 96,8 % |  | ≥ 70 % |
| mensajeria | — (sin código) | 100 % | 100 % |  | ≥ 70 % |
| reputacion | — (sin código) |  | 97,9 % |  | ≥ 70 % |
| notificaciones | — (sin código) | 100 % | 100 % |  | ≥ 70 % |
| administracion | 100 % | 100 % | 97,9 % |  | ≥ 70 % |
| **Total** | 97,2 % (incluye shared) | 95,5 % (shared 77,4 %) | 95,3 % (shared 77,4 %) |  | ≥ 70 % |

## Defectos

| Severidad | Abiertos | Corregidos | Cerrados | Diferidos | Meta al liberar |
|---|---|---|---|---|---|
| S1 Crítica | 0 | 0 | 0 | 0 | 0 abiertos |
| S2 Alta | 0 | 0 | 0 | 0 | 0 abiertos |
| S3 Media | 3 en corrección (DEF-013, DEF-014, DEF-015) | 0 | 4 (DEF-001, DEF-002, DEF-006, DEF-007) | 0 | ≤ 5 con plan |
| S4 Baja | 1 (DEF-012) | 0 | 6 (DEF-003, DEF-004, DEF-005, DEF-008, DEF-009, DEF-010) | 0 | Sin límite, con plan |

## Indicadores

| Indicador | Fórmula | Valor |
|---|---|---|
| Avance de ejecución | Casos ejecutados / casos planeados | 38 / 61 = 62,3 % (corte S1) |
| Tasa de aprobación | Aprobados / (ejecutados − No aplica) | 38 / 38 = 100 % |
| Casos Alta ejecutados | Casos Alta con resultado / casos Alta | 28 / 51 = 54,9 % (los 23 restantes son del Sprint 2) |
| Cobertura de requisitos | Requisitos con todos sus casos ejecutados / 38 | 13 / 38 = 34,2 % (todas las RF del Sprint 1) |
| Densidad de defectos | Defectos confirmados / requisitos del módulo | 5 defectos / 30 requisitos del ciclo (22 RF + 8 RNF) = 0,17 (ninguno en la lógica de negocio) |
| Defectos reabiertos | Reabiertos / corregidos | 0 / 0 |
| Cobertura de código | Líneas cubiertas / líneas de domain + application | 1130 / 1163 = 97,2 % |
| Pruebas automatizadas | Total `@Test` (unitarias + integración) · pasan | 256 (209 unitarias + 47 de integración) · 256 pasan |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 12/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | docs(pruebas): corte del Sprint 1 del ciclo C1 sobre v0.2.0 (#32) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
| 1.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Valores de la ejecución del Sprint 2 del 16/11 (borrador, pendiente de firma de QA) |
| 1.2 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Defectos después de la re-prueba de DEF-005 a DEF-009 (main 55ab5b6) y nuevo DEF-010 |
| 1.3 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-010 en verificación tras su re-prueba (main e679ef3): 0 defectos abiertos |
| 1.4 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Los 10 defectos del C1 pasan a Cerrados (cierre aprobado por Pedro el 16/11) |
| 1.5 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Corte del Sprint 3 (C2) sobre v0.5.2: avance, cobertura por módulo y DEF-012 abierto (borrador, pendiente de firma de QA) |
| 1.6 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | C2 con 34 de 34 casos ejecutados, cobertura del 22/11 y DEF-013 a DEF-015 en corrección (borrador, pendiente de firma de QA) |
