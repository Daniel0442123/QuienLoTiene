# Hoja «Tablero» · Indicadores de la campaña

> Corte del Sprint 1 registrado el 12/11/2026 sobre v0.2.0.
>
> En el Excel se calcula solo con fórmulas sobre las hojas de ejecución y defectos. Aquí se copia el valor al cerrar cada sprint y cada ciclo.

## Avance por ciclo

| Ciclo | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | % avance | % aprobación | Meta |
|---|---|---|---|---|---|---|---|---|
| C1 (Sprints 1 y 2) · corte S1 12/11 | 61 | 38 | 38 | 0 | 0 | 62,3 % | 100 % | 100 % ejecutados |
| C2 (Sprint 3) | 31 + re-pruebas | | | | | | | 100 % ejecutados |
| C3 (Cierre, regresión) | 85 + 5 E2E + 13 NF | | | | | | | ≥ 95 % aprobados |

## Cobertura por módulo (JaCoCo, líneas de domain + application)

| Módulo | Corte S1 (08/11) | Cierre C1 (15/11) | Cierre C2 (22/11) | Final C3 (25/11) | Meta |
|---|---|---|---|---|---|
| identidad | 94,1 % | | | | ≥ 70 % |
| clientes | 97,9 % | | | | ≥ 70 % |
| tiendas | 98,4 % | | | | ≥ 70 % |
| catalogo | 100 % | | | | ≥ 70 % |
| solicitudes | 100 % | | | | ≥ 70 % |
| ordenes | — (sin código) | | | | ≥ 70 % |
| mensajeria | — (sin código) | | | | ≥ 70 % |
| reputacion | — (sin código) | | | | ≥ 70 % |
| notificaciones | — (sin código) | | | | ≥ 70 % |
| administracion | 100 % | | | | ≥ 70 % |
| **Total** | 97,2 % (incluye shared) | | | | ≥ 70 % |

## Defectos

| Severidad | Abiertos | Corregidos | Cerrados | Diferidos | Meta al liberar |
|---|---|---|---|---|---|
| S1 Crítica | 0 | 0 | 0 | 0 | 0 abiertos |
| S2 Alta | 0 | 0 | 0 | 0 | 0 abiertos |
| S3 Media | 2 | 0 | 0 | 0 | ≤ 5 con plan |
| S4 Baja | 2 | 0 | 0 | 0 | Sin límite, con plan |

## Indicadores

| Indicador | Fórmula | Valor |
|---|---|---|
| Avance de ejecución | Casos ejecutados / casos planeados | 38 / 61 = 62,3 % (corte S1) |
| Tasa de aprobación | Aprobados / (ejecutados − No aplica) | 38 / 38 = 100 % |
| Casos Alta ejecutados | Casos Alta con resultado / casos Alta | 28 / 51 = 54,9 % (los 23 restantes son del Sprint 2) |
| Cobertura de requisitos | Requisitos con todos sus casos ejecutados / 38 | 13 / 38 = 34,2 % (todas las RF del Sprint 1) |
| Densidad de defectos | Defectos confirmados / requisitos del módulo | 4 defectos / 30 requisitos del ciclo (22 RF + 8 RNF) = 0,13 (ninguno en la lógica de negocio) |
| Defectos reabiertos | Reabiertos / corregidos | 0 / 0 |
| Cobertura de código | Líneas cubiertas / líneas de domain + application | 1130 / 1163 = 97,2 % |
| Pruebas automatizadas | Total `@Test` (unitarias + integración) · pasan | 256 (209 unitarias + 47 de integración) · 256 pasan |
