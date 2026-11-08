# Informe de pruebas de software · Quién lo tiene — Liberación 1.0

> Informe final de la campaña (C1 a C3). Se entrega el 25/11/2026 junto con el acta de liberación. Guardar en `docs/pruebas/informes/INFORME-DE-PRUEBAS.md`.

Ingeniería de Software III · Byte Force
Responsables de QA: Yanimar Burbano Piamba (pruebas funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad)
Fecha:

## Contenido

1. Introducción
2. Alcance de las pruebas
3. Plan de pruebas y responsables
4. Ejecución de las pruebas y matriz de requerimientos
5. Hallazgos y su prioridad
6. Documentación en el libro de control
7. Casos que no fue posible realizar y sus causas
8. Recomendaciones y conclusión
Anexo A. Matriz de trazabilidad detallada (85 casos)
Anexo B. Casos no realizados con su causa

## 1. Introducción

Qué es Quién lo tiene, objetivo del informe y alcance temporal (C1, C2, C3).

## 2. Alcance de las pruebas

| Sección del SRS | Requisitos | Casos |
|---|---|---|
| 10.1 Identidad y cuentas | RF-001 a RF-006 | |
| 10.2 Cliente | RF-007, RF-008 | |
| 10.3 Tiendas | RF-009 a RF-015 | |
| 10.4 Solicitudes y respuestas | RF-016 a RF-023 | |
| 10.5 Órdenes | RF-024 a RF-029 | |
| 10.6 Comunicación y notificaciones | RF-030 a RF-033 | |
| 10.7 Reputación | RF-034, RF-035 | |
| 10.8 Administración | RF-036 a RF-038 | |
| 11 No funcionales | RNF-001 a RNF-030 | 30 |

| Tipo de prueba | Casos | Qué cubre |
|---|---|---|
| Unitaria | | Reglas de dominio y casos de uso |
| Integración | | PostGIS, API, SSE |
| Seguridad | | Autenticación, autorización, privacidad |
| Manual de interfaz | | WhatsApp, mapas, push |
| Extremo a extremo | 5 | Flujos completos |
| No funcional | 30 | ISO/IEC 25010 |

## 3. Plan de pruebas y responsables

### 3.1 Plan aplicado

- Enfoque basado en requisitos y en riesgo, priorizando pruebas unitarias.
- Técnicas: basada en requisitos, valores límite, partición de equivalencia, transición de estados, tabla de decisión.
- Ciclos: C1 (Sprints 1 y 2), C2 (Sprint 3), C3 (Cierre).
- Criterios de liberación: 100 % Alta ejecutados, ≥ 95 % aprobados, 0 S1/S2, ≤ 5 S3 con plan, cobertura ≥ 70 %.

### 3.2 Integrantes

| Integrante | Responsabilidad | Qué hizo |
|---|---|---|
| Yanimar Burbano Piamba | Líder de QA · funcionales | |
| Francisco José Castro Muñoz | QA no funcional y seguridad | |

## 4. Ejecución de las pruebas y matriz de requerimientos

### 4.1 Sesiones de ejecución

| Sesión | Ciclo | Versión | Responsable | Resultado |
|---|---|---|---|---|

### 4.2 Resultados

| Módulo | Casos | Aprob. | Fall. | Bloq. | Cobertura |
|---|---|---|---|---|---|

### 4.3 Matriz de requerimientos (trazabilidad)

| Requisito | Nombre | Secc. SRS | Casos | Apr. | Fall. | Bloq. | Defectos | Estado |
|---|---|---|---|---|---|---|---|---|

## 5. Hallazgos y su prioridad

| ID | Severidad | Prioridad | Estado | Caso | Qué falla | Versión |
|---|---|---|---|---|---|---|

Orden de atención recomendado y justificación:

| # | Defecto | Sev. / Prior. | Por qué esa clasificación | Acción sugerida |
|---|---|---|---|---|

Observaciones sin defecto:

-

## 6. Documentación en el libro de control

| Hoja | Qué se registró | Columnas diligenciadas |
|---|---|---|

Convenciones usadas: resultados de la lista del libro; todo Fallido con DEF-nnn; todo Bloqueado con causa; historial conservado; evidencias en `evidencias/<ciclo>/<sesión>/<caso>/`.

## 7. Casos que no fue posible realizar y sus causas

| Causa | Casos | Detalle | Qué se habría requerido |
|---|---|---|---|

## 8. Recomendaciones y conclusión

### Para el producto (desarrollo)
### Para la documentación
### Para una próxima ejecución de pruebas
### Lecciones aprendidas
### Conclusión

## Anexo A. Matriz de trazabilidad detallada

| Requisito | Secc. SRS | Caso | Título | Prior. | Resultado final | Defecto |
|---|---|---|---|---|---|---|

## Anexo B. Casos no realizados con su causa

| Caso | Título | Causa |
|---|---|---|
