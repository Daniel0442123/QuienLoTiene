# Plan de pruebas · Ciclo 3 (C3) · Cierre: regresión, sistema integrado y liberación

| Campo | Valor |
|---|---|
| Sistema | Quién lo tiene · Byte Force |
| Responsables de QA | Yanimar Burbano Piamba (pruebas funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad) |
| Fechas | 23/11/2026 al 25/11/2026 (liberación el 26/11) |
| Versión | v0.x.y (se anota la versión real en el acta de inicio) |
| Documentos relacionados | Plan Maestro de Pruebas · Libro de control (`libro/`) · SRS |

## 1. Introducción y alcance

Este plan cubre el último ciclo antes de la liberación en producción del 26/11/2026. Con los módulos ya desplegados juntos en el VPS, se ejecutan las pruebas que el docente recomendó dejar para este momento: los escenarios de **punta a punta** con Playwright y la **aceptación** con Cucumber de las historias Must. También se ejecutan la regresión completa de los 85 casos, los no funcionales sobre el VPS (carga, disponibilidad, accesibilidad, compatibilidad) y los casos de las historias Could que se hayan implementado. El resultado de este ciclo es el que decide la liberación (go / no-go).

Alcance en números: 10 casos funcionales y de seguridad, 13 casos no funcionales, 5 escenarios E2E y la regresión de los 85 casos.

## 2. Objetivos de esta ronda de pruebas

1. Ejecutar la regresión completa de los 85 casos y obtener ≥ 95 % de aprobados en el resultado final.
2. Aprobar los 3 escenarios E2E obligatorios (E2E-01 a E2E-03) y, si hay tiempo, E2E-04 y E2E-05.
3. Medir los 13 casos no funcionales sobre el VPS y compararlos con sus criterios.
4. Ejecutar los 10 casos de las historias Could implementadas, o marcarlos No aplica con justificación.
5. Cerrar con 0 defectos S1 y S2 abiertos y presentar el acta de liberación.

## 3. Elementos a probar

Priorizados por la prioridad de la historia (MoSCoW) y, dentro de ella, por número de historia. El detalle de cada caso está en `libro/02-CASOS.md`.

| Prioridad | Historia | Requisito | Módulo | Prioridad del caso | Sprint | Casos | Nivel | QA |
|---|---|---|---|---|---|---|---|---|
| 1 | HU-006 | Verificar correo | identidad | Baja | Cierre | CP-IDE-006-01, CP-IDE-006-02 | Unitaria | Yanimar |
| 2 | HU-008 | Consultar historial y repetir | clientes | Baja | Cierre | CP-CLI-008-01, CP-CLI-008-02 | Unitaria | Yanimar |
| 3 | HU-011 | Tarifas de domicilio avanzadas | tiendas | Baja | Cierre | CP-TIE-011-01, CP-TIE-011-02 | Unitaria | Yanimar |
| 4 | HU-015 | Ver resumen de indicadores | tiendas | Baja | Cierre | CP-TIE-015-01, CP-TIE-015-02 | Unitaria | Yanimar |
| 5 | HU-022 | Ampliar radio automáticamente | solicitudes | Baja | Cierre | CP-SOL-022-01, CP-SOL-022-02 | Unitaria | Yanimar |

**No funcionales del ciclo**

| Caso | Requisito | Criterio de aceptación | Herramienta | QA |
|---|---|---|---|---|
| NF-REN-001 | RNF-001 | p95 menor o igual a 500 ms en prueba de carga con k6 o JMeter. | k6 | Francisco Castro |
| NF-REN-002 | RNF-002 | p95 menor o igual a 3 s. | k6 + cliente SSE | Francisco Castro |
| NF-REN-004 | RNF-004 | API como máximo 640 MB y todo el stack como máximo 2 GB de los 4 GB del VPS. | docker stats | Francisco Castro |
| NF-REN-005 | RNF-005 | Mayor o igual a 80 y primera carga de 3 s o menos en 4G simulado. | Lighthouse (móvil) | Francisco Castro |
| NF-SEG-010 | RNF-010 | Solo TLS 1.2 y 1.3, HSTS activo y calificación A o superior. | SSL Labs | Francisco Castro |
| NF-FIA-017 | RNF-017 | RPO de 24 h, RTO de 4 h y una restauración probada antes de la entrega. | pg_dump / restauración | Francisco Castro |
| NF-DIS-018 | RNF-018 | Mayor o igual a 99 % entre el 16/11/2026 y el 26/11/2026. | Monitor externo (UptimeRobot) | Francisco Castro |
| NF-DIS-019 | RNF-019 | Como máximo 60 s por despliegue. | Script de despliegue + monitor | Francisco Castro |
| NF-ESC-020 | RNF-020 | 100 usuarios concurrentes y 50 conexiones SSE con tasa de error menor a 1 %. | k6 | Francisco Castro |
| NF-COM-026 | RNF-026 | Funciona en las dos últimas versiones de Chrome, Edge, Firefox y Safari, en Android 10+ e iOS 16.4+. | Manual (matriz de navegadores) | Francisco Castro |
| NF-USA-027 | RNF-027 | Publicar una solicitud en 60 s o menos; responder Sí en 3 toques o menos; No lo tengo en 1 toque. | Prueba con 5 usuarios | Francisco Castro |
| NF-USA-028 | RNF-028 | Mayor o igual a 90, texto base de 16 px o más, botones de 44 × 44 px o más y contraste WCAG 2.1 AA. | Lighthouse + axe + checklist WCAG | Francisco Castro |
| NF-POR-029 | RNF-029 | 15 minutos o menos siguiendo el manual de despliegue. | Manual de despliegue en servidor limpio | Francisco Castro |

**Punta a punta**

| ID | Escenario | Historias | Obligatorio |
|---|---|---|---|
| E2E-01 | Publicar y recibir respuesta | HU-001, HU-005, HU-016, HU-017, HU-019, HU-020 | Sí |
| E2E-02 | Elegir y entregar a domicilio | HU-020, HU-024, HU-027, HU-028 | Sí |
| E2E-03 | Aprobar una tienda con segundo factor | HU-002, HU-003, HU-004, HU-036 | Sí |
| E2E-04 | Calificar después de la entrega | HU-028, HU-034 | Si hay tiempo |
| E2E-05 | Solicitud sin respuestas que amplía el radio y vence | HU-021, HU-022 | Si hay tiempo |

## 4. Elementos fuera de alcance

| Elemento | Razón |
|---|---|
| Pruebas de penetración y OWASP ASVS completa | Fuera del alcance académico; se programan antes de abrir el servicio a tiendas reales. |
| RF-039 a RF-042 (Won't) | No hacen parte de esta entrega. |
| Pruebas con usuarios o tiendas reales | La demo usa datos de prueba. |

## 5. Enfoque y técnicas de prueba

El enfoque parte del requisito y no del código: cada criterio de aceptación del SRS tiene su caso, y el caso se escribe como prueba antes o junto con la implementación (shift-left). Primero se ve fallar la prueba, luego se implementa y se re-ejecuta (ciclo rojo-verde).

| Técnica | Casos |
|---|---|
| Basada en requisitos (clase válida) | CP-IDE-006-01, CP-CLI-008-01, CP-CLI-008-02, CP-TIE-015-01 |
| Partición de equivalencia (clase inválida) | CP-TIE-015-02 |
| Valores límite | CP-IDE-006-02, CP-TIE-011-01, CP-TIE-011-02, CP-SOL-022-01, CP-SOL-022-02 |

Criterios transversales:

- **Pruebas unitarias** sin Spring ni base de datos: casos de uso con sus puertos simulados con Mockito; entidades y máquinas de estado directamente.
- **Tiempo controlado:** las reglas de vencimiento y de límite por hora usan un `Clock` inyectado.
- **Entradas inválidas:** los comandos lanzan una excepción de dominio con mensaje claro; la API la traduce a un error RFC 9457 con el código HTTP correspondiente (400, 403, 404, 409, 429).
- **Etiquetas:** cada `@Test` lleva `@Tag("HU-xxx")` y `@Tag("CP-xxx")`; el nombre en `@DisplayName` empieza con el ID del caso del libro.

## 6. Criterios de aceptación (criterios de salida)

El ciclo se considera terminado cuando se cumplan todos los siguientes criterios:

1. Regresión completa ejecutada: 100 % de casos Alta con resultado.
2. ≥ 95 % de casos aprobados en el resultado final (mínimo 90 % con excepción en el acta).
3. E2E-01 a E2E-03 aprobados.
4. Los 13 no funcionales medidos en el VPS.
5. 0 defectos S1 y S2 abiertos; ≤ 5 S3 con plan.
6. Informe de cierre C3, informe final de pruebas y acta de liberación firmados.

## 7. Entregables de esta ronda

| N.º | Entregable | Descripción | Responsable |
|---|---|---|---|
| 1 | Libro de control actualizado | Hoja «Ejecución C3», «Defectos», «Bitácora», «Trazabilidad» y «Tablero» | Yanimar y Francisco |
| 2 | Suite de pruebas automatizadas | Pruebas JUnit (y Cucumber/Playwright en C3) etiquetadas con @HU/@CP | Desarrolladores; QA revisa |
| 3 | Reporte de ejecución y cobertura | Salida de `./mvnw verify`, `jacoco.csv` y captura del resumen en `evidencias/C3/` | Yanimar |
| 4 | Documento de evidencias | Ficha y capturas por caso ejecutado | Yanimar y Francisco |
| 5 | Reportes de defecto | Un DEF-nnn por defecto con evidencia | Quien lo encuentra |
| 6 | Informe de cierre del ciclo | Plantilla `INFORME-DE-CIERRE-DE-CICLO.md` | Yanimar |
| 7 | Análisis de defectos | Error → defecto → falla y principios | Yanimar |
| 8 | Informe final de pruebas | Plantilla `INFORME-DE-PRUEBAS.md` | Yanimar |
| 9 | Acta de liberación y pruebas de humo | Anexos E y H del Plan Maestro | Yanimar, Francisco y Product Owner |

## 8. Responsabilidades del equipo

| Integrante | Rol / responsabilidad en esta ronda | Casos |
|---|---|---|
| Yanimar Burbano Piamba | Líder de QA: prepara el ciclo, ejecuta y registra los casos funcionales y E2E, conduce el triaje y elabora los informes | 10 casos: CP-IDE-006-01, CP-IDE-006-02, CP-CLI-008-01, CP-CLI-008-02, CP-TIE-011-01, CP-TIE-011-02, CP-TIE-015-01, CP-TIE-015-02, CP-SOL-022-01, CP-SOL-022-02 · E2E-01 a E2E-05 · regresión |
| Francisco José Castro Muñoz | QA no funcional y seguridad: ejecuta y registra los casos de seguridad y no funcionales, mantiene los escaneos | 13 casos: NF-REN-001, NF-REN-002, NF-REN-004, NF-REN-005, NF-SEG-010, NF-FIA-017, NF-DIS-018, NF-DIS-019, NF-ESC-020, NF-COM-026, NF-USA-027, NF-USA-028, NF-POR-029 |
| Desarrolladores (Daniel, Deiner, Monica) | Escriben las pruebas automatizadas de su código y corrigen los defectos asignados | — |

## 9. Cronograma

| Actividad | Fecha estimada |
|---|---|
| Acta de inicio del C3; versión candidata desplegada | 23/11/2026 |
| Regresión completa automatizada y manual de prioridad Alta | 23/11/2026 |
| E2E con Playwright y aceptación Cucumber | 23/11 al 24/11/2026 |
| Carga (k6), Lighthouse, accesibilidad y matriz de navegadores | 24/11/2026 |
| Re-prueba de correcciones y cierre del libro | 25/11/2026 |
| Informe de cierre C3, informe final de pruebas y comité go / no-go | 25/11/2026 |
| Despliegue en producción y pruebas de humo | 26/11/2026 |

## 10. Riesgos y contingencias

| N.º | Riesgo | Probabilidad | Impacto | Contingencia |
|---|---|---|---|---|
| R1 | No hay tiempo para automatizar los 5 E2E | Alta | Medio | Solo E2E-01 a E2E-03 son obligatorios; si Playwright no está listo se ejecutan manualmente con evidencia en video. |
| R2 | Un defecto S1 o S2 aparece el último día | Media | Alto | Congelar funciones el 23/11: solo correcciones; reversión a la versión anterior si la corrección no es segura. |
| R3 | El VPS no alcanza el rendimiento esperado | Media | Medio | Línea base en C2; ajustar memoria según el presupuesto de la arquitectura. |
| R4 | La aprobación queda entre 90 % y 95 % | Media | Alto | El acta de liberación puede aceptar la excepción solo con defectos S3/S4 y plan de corrección. |

## Aprobación

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| Elaboró (líder de QA) | Yanimar Burbano Piamba | | |
| Elaboró (QA no funcional) | Francisco José Castro Muñoz | | |
| Aprobó (Product Owner) | Wilson Javier Galindez Certuche | | |
