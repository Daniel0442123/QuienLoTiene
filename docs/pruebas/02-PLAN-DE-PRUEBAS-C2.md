# Plan de pruebas · Ciclo 2 (C2) · Sprint 3: unitarias, integración y seguridad

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

| Campo | Valor |
|---|---|
| Sistema | Quién lo tiene · Byte Force |
| Responsables de QA | Yanimar Burbano Piamba (pruebas funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad) |
| Fechas | 16/11/2026 al 22/11/2026 |
| Versión | v0.x.y (se anota la versión real en el acta de inicio) |
| Documentos relacionados | Plan Maestro de Pruebas · Libro de control (`libro/`) · SRS |

## 1. Introducción y alcance

Este plan cubre el segundo ciclo de pruebas de Quién lo tiene, correspondiente al Sprint 3: reseñas, reportes, panel de administración, mini chat, notificaciones push y las funciones Should. Además de las pruebas unitarias de las historias nuevas, el ciclo agrega las pruebas de integración y seguridad que necesitan varios módulos funcionando (autorización por objeto, sesiones, límites, archivos, auditoría, eventos) y la **re-prueba obligatoria** de todo caso Fallido o Bloqueado en C1. Desde el 16/11 el VPS queda estable para la evaluación.

Alcance en números: 22 casos funcionales y de seguridad, 9 casos no funcionales y las re-pruebas de C1.

## 2. Objetivos de esta ronda de pruebas

1. Ejecutar y registrar los 22 casos de las 11 historias del Sprint 3.
2. Re-probar todos los casos Fallidos o Bloqueados de C1 y cerrar sus defectos.
3. Ejecutar los 9 casos no funcionales de integración y seguridad (RNF-003, 007, 008, 009, 012, 013, 015, 024, 025).
4. Mantener la cobertura ≥ 70 % en todos los módulos y la suite completa en verde como regresión.
5. Hacer al menos una sesión exploratoria por responsable de QA sobre el flujo principal desplegado.

## 3. Elementos a probar

Priorizados por la prioridad de la historia (MoSCoW) y, dentro de ella, por número de historia. El detalle de cada caso está en `libro/02-CASOS.md`.

| Prioridad | Historia | Requisito | Módulo | Prioridad del caso | Sprint | Casos | Nivel | QA |
|---|---|---|---|---|---|---|---|---|
| 1 | HU-034 | Calificar tienda | reputacion | Alta | Sprint 3 | CP-REP-034-01, CP-REP-034-02, CP-REP-034-03 | Unitaria | Yanimar |
| 2 | HU-014 | Ver detalle de tienda | tiendas | Media | Sprint 3 | CP-TIE-014-01, CP-TIE-014-02 | Integración (API), Unitaria | Francisco, Yanimar |
| 3 | HU-023 | Advertir productos restringidos | solicitudes | Media | Sprint 3 | CP-SOL-023-01, CP-SOL-023-02 | Unitaria | Yanimar |
| 4 | HU-025 | Reservar para recogida | ordenes | Media | Sprint 3 | CP-ORD-025-01, CP-ORD-025-02 | Unitaria | Yanimar |
| 5 | HU-026 | Ver cómo llegar | ordenes | Media | Sprint 3 | CP-ORD-026-01 | Manual (interfaz) | Yanimar |
| 6 | HU-029 | Cancelar orden | ordenes | Media | Sprint 3 | CP-ORD-029-01, CP-ORD-029-02 | Unitaria | Yanimar |
| 7 | HU-031 | Notificaciones push web | notificaciones | Media | Sprint 3 | CP-NOT-031-01, CP-NOT-031-02 | Manual (navegador) | Yanimar |
| 8 | HU-033 | Mini chat | mensajeria | Media | Sprint 3 | CP-MEN-033-01, CP-MEN-033-02 | Unitaria | Yanimar |
| 9 | HU-035 | Reportar problema | reputacion | Media | Sprint 3 | CP-REP-035-01, CP-REP-035-02 | Unitaria | Francisco, Yanimar |
| 10 | HU-037 | Gestionar reportes y suspender | administracion | Media | Sprint 3 | CP-ADM-037-01, CP-ADM-037-02 | Unitaria | Francisco, Yanimar |
| 11 | HU-038 | Gestionar categorías y restringidos | catalogo | Media | Sprint 3 | CP-CAT-038-01, CP-CAT-038-02 | Unitaria | Yanimar |

**No funcionales del ciclo**

| Caso | Requisito | Criterio de aceptación | Herramienta | QA |
|---|---|---|---|---|
| NF-REN-003 | RNF-003 | Menor o igual a 200 ms usando índice GiST. | Testcontainers (PostGIS) + EXPLAIN | Francisco Castro |
| NF-SEG-007 | RNF-007 | 0 accesos indebidos; cada endpoint con datos personales tiene prueba negativa. | JUnit + pruebas de autorización API | Francisco Castro |
| NF-SEG-008 | RNF-008 | JWT de 15 minutos; refresco rotativo en cookie HttpOnly, Secure y SameSite=Strict. | Pruebas de API (Spring Security Test) | Francisco Castro |
| NF-SEG-009 | RNF-009 | Máximo 5 intentos de inicio de sesión por minuto por cuenta y 10 solicitudes por hora por cliente; el exceso recibe HTTP 429. | Pruebas de API / k6 | Francisco Castro |
| NF-SEG-012 | RNF-012 | Solo JPEG, PNG, WebP y PDF de hasta 5 MB, tipo verificado por contenido y metadatos EXIF eliminados. | JUnit + pruebas de API | Francisco Castro |
| NF-SEG-013 | RNF-013 | 100 % registrados con usuario, fecha y acción, sin datos personales en los logs. | Pruebas de integración | Francisco Castro |
| NF-FIA-015 | RNF-015 | 0 eventos perdidos en la prueba de reinicio. | Spring Modulith (event publication registry) | Francisco Castro |
| NF-MAN-024 | RNF-024 | 100 % de endpoints documentados. | springdoc OpenAPI | Francisco Castro |
| NF-COM-025 | RNF-025 | JSON sobre HTTPS con prefijo /api/v1. | Pruebas de API | Francisco Castro |

## 4. Elementos fuera de alcance

| Elemento | Razón |
|---|---|
| Punta a punta (Playwright) | Se ejecuta en C3. |
| Carga sobre el VPS y disponibilidad | Se mide en C3; en C2 se puede correr una línea base local. |
| Historias Could (HU-006, 008, 011, 015, 022) | Se prueban en C3 si se implementan. |

## 5. Enfoque y técnicas de prueba

El enfoque parte del requisito y no del código: cada criterio de aceptación del SRS tiene su caso, y el caso se escribe como prueba antes o junto con la implementación (shift-left). Primero se ve fallar la prueba, luego se implementa y se re-ejecuta (ciclo rojo-verde).

| Técnica | Casos |
|---|---|
| Basada en requisitos (clase válida) | CP-TIE-014-01, CP-SOL-023-01, CP-ORD-026-01, CP-MEN-033-01, CP-REP-034-01, CP-REP-035-01, CP-CAT-038-02 |
| Partición de equivalencia (clase inválida) | CP-TIE-014-02, CP-SOL-023-02, CP-NOT-031-01, CP-NOT-031-02, CP-REP-034-02, CP-REP-034-03, CP-REP-035-02 |
| Transición de estados | CP-ORD-029-01, CP-ORD-029-02, CP-MEN-033-02, CP-ADM-037-01, CP-ADM-037-02, CP-CAT-038-01 |
| Valores límite | CP-ORD-025-01, CP-ORD-025-02 |

Criterios transversales:

- **Pruebas unitarias** sin Spring ni base de datos: casos de uso con sus puertos simulados con Mockito; entidades y máquinas de estado directamente.
- **Tiempo controlado:** las reglas de vencimiento y de límite por hora usan un `Clock` inyectado.
- **Entradas inválidas:** los comandos lanzan una excepción de dominio con mensaje claro; la API la traduce a un error RFC 9457 con el código HTTP correspondiente (400, 403, 404, 409, 429).
- **Etiquetas:** cada `@Test` lleva `@Tag("HU-xxx")` y `@Tag("CP-xxx")`; el nombre en `@DisplayName` empieza con el ID del caso del libro.

## 6. Criterios de aceptación (criterios de salida)

El ciclo se considera terminado cuando se cumplan todos los siguientes criterios:

1. Los 22 casos nuevos y todas las re-pruebas de C1 tienen resultado registrado.
2. La suite automatizada completa (C1 + C2) en verde.
3. Cobertura ≥ 70 % en domain y application de todos los módulos.
4. 0 defectos S1 y S2 abiertos; los S3 con plan.
5. Los 9 no funcionales del ciclo medidos y registrados.
6. Informe de cierre C2, evidencias y análisis de defectos diligenciados.

## 7. Entregables de esta ronda

| N.º | Entregable | Descripción | Responsable |
|---|---|---|---|
| 1 | Libro de control actualizado | Hoja «Ejecución C2», «Defectos», «Bitácora», «Trazabilidad» y «Tablero» | Yanimar y Francisco |
| 2 | Suite de pruebas automatizadas | Pruebas JUnit (y Cucumber/Playwright en C3) etiquetadas con @HU/@CP | Desarrolladores; QA revisa |
| 3 | Reporte de ejecución y cobertura | Salida de `./mvnw verify`, `jacoco.csv` y captura del resumen en `evidencias/C2/` | Yanimar |
| 4 | Documento de evidencias | Ficha y capturas por caso ejecutado | Yanimar y Francisco |
| 5 | Reportes de defecto | Un DEF-nnn por defecto con evidencia | Quien lo encuentra |
| 6 | Informe de cierre del ciclo | Plantilla `INFORME-DE-CIERRE-DE-CICLO.md` | Yanimar |
| 7 | Análisis de defectos | Error → defecto → falla y principios | Yanimar |

## 8. Responsabilidades del equipo

| Integrante | Rol / responsabilidad en esta ronda | Casos |
|---|---|---|
| Yanimar Burbano Piamba | Líder de QA: prepara el ciclo, ejecuta y registra los casos funcionales, conduce el triaje y elabora los informes | 19 casos: CP-TIE-014-01, CP-SOL-023-01, CP-SOL-023-02, CP-ORD-025-01, CP-ORD-025-02, CP-ORD-026-01, CP-ORD-029-01, CP-ORD-029-02, CP-NOT-031-01, CP-NOT-031-02, CP-MEN-033-01, CP-MEN-033-02, CP-REP-034-01, CP-REP-034-02, CP-REP-034-03, CP-REP-035-01, CP-ADM-037-02, CP-CAT-038-01, CP-CAT-038-02 |
| Francisco José Castro Muñoz | QA no funcional y seguridad: ejecuta y registra los casos de seguridad y no funcionales, mantiene los escaneos | 12 casos: CP-TIE-014-02, CP-REP-035-02, CP-ADM-037-01, NF-REN-003, NF-SEG-007, NF-SEG-008, NF-SEG-009, NF-SEG-012, NF-SEG-013, NF-FIA-015, NF-MAN-024, NF-COM-025 |
| Desarrolladores (Daniel, Deiner, Monica) | Escriben las pruebas automatizadas de su código y corrigen los defectos asignados | — |

## 9. Cronograma

| Actividad | Fecha estimada |
|---|---|
| Acta de inicio del C2 y plan aprobado | 16/11/2026 |
| Re-prueba de Fallidos y Bloqueados de C1 | 16/11 al 17/11/2026 |
| Pruebas unitarias por historia del Sprint 3 (en cada pull request) | 16/11 al 21/11/2026 |
| No funcionales de integración y seguridad | 18/11 al 21/11/2026 |
| Sesiones exploratorias | 20/11 al 21/11/2026 |
| Cierre del C2: libro, tablero, evidencias, análisis de defectos e informe | 22/11/2026 |

## 10. Riesgos y contingencias

| N.º | Riesgo | Probabilidad | Impacto | Contingencia |
|---|---|---|---|---|
| R1 | Las correcciones de C1 rompen casos ya aprobados | Media | Alto | La suite automatizada completa corre en cada pull request como regresión. |
| R2 | Push web y SSE dependen del navegador | Media | Medio | CP manuales con evidencia en video o captura; probar en Chrome y en un Android real. |
| R3 | Poco tiempo de QA para re-pruebas y casos nuevos | Alta | Medio | Los desarrolladores ejecutan la re-prueba automatizada; QA verifica y registra. |
| R4 | Cambios de versión en el VPS durante el ciclo | Media | Medio | Notas de versión en cada despliegue y registro de la versión en la bitácora. |

## Aprobación

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| Elaboró (líder de QA) | Yanimar Burbano Piamba | | |
| Elaboró (QA no funcional) | Francisco José Castro Muñoz | | |
| Aprobó (Product Owner) | Wilson Javier Galindez Certuche | | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
