# Hoja «Tablero» · Indicadores de la campaña

> En el Excel se calcula solo con fórmulas sobre las hojas de ejecución y defectos. Aquí se copia el valor al cerrar cada sprint y cada ciclo.

## Avance por ciclo

| Ciclo | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | % avance | % aprobación | Meta |
|---|---|---|---|---|---|---|---|---|
| C1 (Sprints 1 y 2) | 61 | | | | | | | 100 % ejecutados |
| C2 (Sprint 3) | 31 + re-pruebas | | | | | | | 100 % ejecutados |
| C3 (Cierre, regresión) | 85 + 5 E2E + 13 NF | | | | | | | ≥ 95 % aprobados |

## Cobertura por módulo (JaCoCo, líneas de domain + application)

| Módulo | Corte S1 (08/11) | Cierre C1 (15/11) | Cierre C2 (22/11) | Final C3 (25/11) | Meta |
|---|---|---|---|---|---|
| identidad | | | | | ≥ 70 % |
| clientes | | | | | ≥ 70 % |
| tiendas | | | | | ≥ 70 % |
| catalogo | | | | | ≥ 70 % |
| solicitudes | | | | | ≥ 70 % |
| ordenes | | | | | ≥ 70 % |
| mensajeria | | | | | ≥ 70 % |
| reputacion | | | | | ≥ 70 % |
| notificaciones | | | | | ≥ 70 % |
| administracion | | | | | ≥ 70 % |
| **Total** | | | | | ≥ 70 % |

## Defectos

| Severidad | Abiertos | Corregidos | Cerrados | Diferidos | Meta al liberar |
|---|---|---|---|---|---|
| S1 Crítica | | | | | 0 abiertos |
| S2 Alta | | | | | 0 abiertos |
| S3 Media | | | | | ≤ 5 con plan |
| S4 Baja | | | | | Sin límite, con plan |

## Indicadores

| Indicador | Fórmula | Valor |
|---|---|---|
| Avance de ejecución | Casos ejecutados / casos planeados | |
| Tasa de aprobación | Aprobados / (ejecutados − No aplica) | |
| Casos Alta ejecutados | Casos Alta con resultado / casos Alta | |
| Cobertura de requisitos | Requisitos con todos sus casos ejecutados / 38 | |
| Densidad de defectos | Defectos confirmados / requisitos del módulo | |
| Defectos reabiertos | Reabiertos / corregidos | |
| Cobertura de código | Líneas cubiertas / líneas de domain + application | |
| Pruebas automatizadas | Total `@Test` (unitarias + integración) · pasan | |
