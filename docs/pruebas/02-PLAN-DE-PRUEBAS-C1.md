# Plan de pruebas · Ciclo 1 (C1) · Pruebas unitarias de los Sprints 1 y 2

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

| Campo | Valor |
|---|---|
| Sistema | Quién lo tiene · Byte Force |
| Responsables de QA | Yanimar Burbano Piamba (pruebas funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad) |
| Fechas | 02/11/2026 al 15/11/2026 (corte parcial el 08/11) |
| Versión | v0.x.y (se anota la versión real en el acta de inicio) |
| Documentos relacionados | Plan Maestro de Pruebas · Libro de control (`libro/`) · SRS |

## 1. Introducción y alcance

Este plan cubre el primer ciclo de pruebas de Quién lo tiene, que va del Sprint 1 al Sprint 2. Siguiendo la recomendación del docente, el ciclo se concentra en las **pruebas unitarias** de las reglas de dominio y de los casos de uso de los módulos construidos en esos sprints: identidad, tiendas, solicitudes, órdenes, notificaciones, mensajería y administración. Las pruebas de integración solo se ejecutan donde la regla depende de la base de datos (consultas por radio con PostGIS), de la capa web (token, privacidad de la respuesta de la API) o del tiempo real (SSE). Al cerrar el Sprint 1 (08/11) se hace un corte parcial con lo que esté implementado; el ciclo se cierra el 15/11.

Alcance en números: 53 casos funcionales y de seguridad, 8 casos no funcionales.

## 2. Objetivos de esta ronda de pruebas

1. Ejecutar y registrar los 53 casos funcionales y de seguridad de las 22 historias de los Sprints 1 y 2.
2. Dejar cada historia terminada con sus pruebas unitarias en verde y etiquetadas con `@HU`/`@CP` (definición de terminado).
3. Alcanzar una cobertura de líneas ≥ 70 % en `domain` y `application` de cada módulo del ciclo (RNF-022).
4. Dejar en verde desde el inicio las verificaciones automáticas del pipeline: Spring Modulith, ArchUnit, Trivy, Dependency-Check y escaneo de secretos.
5. Registrar todo defecto encontrado con su severidad, prioridad y evidencia, y corregir los S1 y S2 dentro del ciclo.

## 3. Elementos a probar

Priorizados por la prioridad de la historia (MoSCoW) y, dentro de ella, por número de historia. El detalle de cada caso está en `libro/02-CASOS.md`.

| Prioridad | Historia | Requisito | Módulo | Prioridad del caso | Sprint | Casos | Nivel | QA |
|---|---|---|---|---|---|---|---|---|
| 1 | HU-001 | Registrar cliente | identidad | Alta | Sprint 1 | CP-IDE-001-01, CP-IDE-001-02, CP-IDE-001-03 | Unitaria | Francisco, Yanimar |
| 2 | HU-002 | Registrar tienda | tiendas | Alta | Sprint 1 | CP-TIE-002-01, CP-TIE-002-02 | Unitaria | Yanimar |
| 3 | HU-003 | Iniciar y cerrar sesión | identidad | Alta | Sprint 1 | CP-IDE-003-01, CP-IDE-003-02, CP-IDE-003-03 | Integración (API + Spring Security), Unitaria | Francisco |
| 4 | HU-005 | Aceptar política de datos | identidad | Alta | Sprint 1 | CP-IDE-005-01, CP-IDE-005-02 | Unitaria | Francisco |
| 5 | HU-007 | Gestionar perfil y direcciones | clientes | Alta | Sprint 1 | CP-CLI-007-01, CP-CLI-007-02 | Unitaria | Yanimar |
| 6 | HU-009 | Configurar perfil de tienda | tiendas | Alta | Sprint 1 | CP-TIE-009-01, CP-TIE-009-02 | Unitaria | Yanimar |
| 7 | HU-010 | Configurar domicilio con tarifa fija | tiendas | Alta | Sprint 1 | CP-TIE-010-01, CP-TIE-010-02 | Unitaria | Yanimar |
| 8 | HU-012 | Configurar medios de pago | tiendas | Alta | Sprint 1 | CP-TIE-012-01, CP-TIE-012-02 | Unitaria | Yanimar |
| 9 | HU-013 | Abrir o cerrar tienda | tiendas | Alta | Sprint 1 | CP-TIE-013-01, CP-TIE-013-02 | Unitaria | Yanimar |
| 10 | HU-016 | Publicar solicitud | solicitudes | Alta | Sprint 1 | CP-SOL-016-01, CP-SOL-016-02, CP-SOL-016-03 | Unitaria | Francisco, Yanimar |
| 11 | HU-017 | Difundir a tiendas elegibles | solicitudes | Alta | Sprint 1 | CP-SOL-017-01, CP-SOL-017-02, CP-SOL-017-03 | Integración (PostGIS) | Yanimar |
| 12 | HU-018 | Ver solicitudes nuevas | solicitudes | Alta | Sprint 2 | CP-SOL-018-01, CP-SOL-018-02 | Integración (API), Unitaria | Francisco, Yanimar |
| 13 | HU-019 | Responder solicitud | solicitudes | Alta | Sprint 2 | CP-SOL-019-01, CP-SOL-019-02, CP-SOL-019-03, CP-SOL-019-04 | Unitaria | Yanimar |
| 14 | HU-020 | Ver y ordenar respuestas | solicitudes | Alta | Sprint 2 | CP-SOL-020-01, CP-SOL-020-02, CP-SOL-020-03 | Integración (SSE), Unitaria | Yanimar |
| 15 | HU-021 | Vencer y cerrar solicitudes | solicitudes | Alta | Sprint 2 | CP-SOL-021-01, CP-SOL-021-02 | Unitaria | Yanimar |
| 16 | HU-024 | Elegir respuesta y crear orden | ordenes | Alta | Sprint 2 | CP-ORD-024-01, CP-ORD-024-02, CP-ORD-024-03, CP-ORD-024-04 | Integración (API), Unitaria | Francisco, Yanimar |
| 17 | HU-027 | Actualizar estado de la orden | ordenes | Alta | Sprint 2 | CP-ORD-027-01, CP-ORD-027-02 | Unitaria | Yanimar |
| 18 | HU-028 | Confirmar recepción | ordenes | Alta | Sprint 2 | CP-ORD-028-01, CP-ORD-028-02 | Unitaria | Francisco, Yanimar |
| 19 | HU-030 | Notificar en tiempo real | notificaciones | Alta | Sprint 2 | CP-NOT-030-01, CP-NOT-030-02 | Integración (SSE) | Yanimar |
| 20 | HU-032 | Contactar por WhatsApp | mensajeria | Alta | Sprint 2 | CP-MEN-032-01, CP-MEN-032-02 | Manual (interfaz) | Yanimar |
| 21 | HU-036 | Aprobar o rechazar tiendas | administracion | Alta | Sprint 1 | CP-ADM-036-01, CP-ADM-036-02 | Unitaria | Yanimar |
| 22 | HU-004 | Segundo factor del administrador | identidad | Media | Sprint 1 | CP-IDE-004-01, CP-IDE-004-02 | Unitaria | Francisco |

**No funcionales del ciclo**

| Caso | Requisito | Criterio de aceptación | Herramienta | QA |
|---|---|---|---|---|
| NF-SEG-006 | RNF-006 | Mínimo 15 caracteres, hash Argon2id o bcrypt, rechazo de contraseñas de la lista de filtradas. | JUnit | Francisco Castro |
| NF-SEG-011 | RNF-011 | 0 críticas y 0 altas sin justificar en el build de entrega. | Trivy + OWASP Dependency-Check (CI) | Francisco Castro |
| NF-SEG-014 | RNF-014 | 100 % con fecha y versión de la política aceptada. | JUnit | Francisco Castro |
| NF-FIA-016 | RNF-016 | 0 aceptadas; todas las combinaciones probadas. | JUnit (máquinas de estado) | Yanimar Burbano |
| NF-ESC-021 | RNF-021 | 0 violaciones de dependencias entre módulos. | Spring Modulith verify() | Yanimar Burbano |
| NF-MAN-022 | RNF-022 | Mayor o igual a 70 %. | JaCoCo | Yanimar Burbano |
| NF-MAN-023 | RNF-023 | 0 violaciones en el pipeline. | ArchUnit | Yanimar Burbano |
| NF-POR-030 | RNF-030 | 0 secretos en el repositorio; toda la configuración por variables de entorno. | gitleaks / escaneo de secretos (CI) | Francisco Castro |

## 4. Elementos fuera de alcance

| Elemento | Razón |
|---|---|
| Punta a punta (Playwright) | Se ejecuta en C3, cuando el flujo principal esté desplegado (recomendación del docente). |
| Aceptación con Cucumber | Se automatiza en C3 solo para historias Must; los escenarios Gherkin sirven ahora como especificación de las unitarias. |
| Carga, disponibilidad y compatibilidad | Requieren el VPS estable; van en C3. |
| Historias del Sprint 3 y del Cierre | Se prueban en C2 y C3. |
| Pantallas del frontend | Se verifican de forma manual solo en los casos marcados como Manual; el resto en C3. |

## 5. Enfoque y técnicas de prueba

El enfoque parte del requisito y no del código: cada criterio de aceptación del SRS tiene su caso, y el caso se escribe como prueba antes o junto con la implementación (shift-left). Primero se ve fallar la prueba, luego se implementa y se re-ejecuta (ciclo rojo-verde).

| Técnica | Casos |
|---|---|
| Basada en requisitos (clase válida) | CP-IDE-001-01, CP-IDE-003-01, CP-IDE-004-01, CP-IDE-005-02, CP-CLI-007-01, CP-TIE-009-02, CP-TIE-010-01, CP-TIE-012-01, CP-TIE-012-02, CP-SOL-016-01, CP-SOL-018-01, CP-SOL-019-01, CP-SOL-019-03, CP-SOL-020-01, CP-SOL-020-02, CP-SOL-020-03, CP-ORD-024-01, CP-ORD-024-02, CP-NOT-030-01, CP-NOT-030-02, CP-MEN-032-01 |
| Partición de equivalencia (clase inválida) | CP-IDE-001-02, CP-TIE-002-02, CP-IDE-003-02, CP-IDE-004-02, CP-IDE-005-01, CP-CLI-007-02, CP-TIE-010-02, CP-SOL-016-02, CP-SOL-017-02, CP-SOL-017-03, CP-SOL-019-02, CP-SOL-019-04, CP-SOL-021-02, CP-ORD-024-03, CP-ORD-024-04, CP-ORD-028-02, CP-MEN-032-02 |
| Transición de estados | CP-TIE-002-01, CP-IDE-003-03, CP-TIE-013-01, CP-TIE-013-02, CP-ORD-027-01, CP-ORD-027-02, CP-ORD-028-01, CP-ADM-036-01, CP-ADM-036-02 |
| Valores límite | CP-IDE-001-03, CP-TIE-009-01, CP-SOL-016-03, CP-SOL-017-01, CP-SOL-018-02, CP-SOL-021-01 |

Criterios transversales:

- **Pruebas unitarias** sin Spring ni base de datos: casos de uso con sus puertos simulados con Mockito; entidades y máquinas de estado directamente.
- **Tiempo controlado:** las reglas de vencimiento y de límite por hora usan un `Clock` inyectado.
- **Entradas inválidas:** los comandos lanzan una excepción de dominio con mensaje claro; la API la traduce a un error RFC 9457 con el código HTTP correspondiente (400, 403, 404, 409, 429).
- **Etiquetas:** cada `@Test` lleva `@Tag("HU-xxx")` y `@Tag("CP-xxx")`; el nombre en `@DisplayName` empieza con el ID del caso del libro.

## 6. Criterios de aceptación (criterios de salida)

El ciclo se considera terminado cuando se cumplan todos los siguientes criterios:

1. Los 53 casos del ciclo tienen resultado registrado en «Ejecución C1» (o No aplica justificado).
2. Cada historia del ciclo tiene pruebas automatizadas etiquetadas y en verde: la ejecución completa reporta `Tests run: N, Failures: 0, Errors: 0`.
3. Cobertura de líneas ≥ 70 % en domain y application de cada módulo del ciclo.
4. Todo caso Fallido tiene su DEF-nnn con evidencia; 0 defectos S1 y S2 abiertos al cierre.
5. Verificaciones del pipeline en verde: Modulith, ArchUnit, Trivy, Dependency-Check, secretos.
6. Informe de corte del Sprint 1, informe de cierre C1 y documento de evidencias diligenciados con datos reales del libro.

## 7. Entregables de esta ronda

| N.º | Entregable | Descripción | Responsable |
|---|---|---|---|
| 1 | Libro de control actualizado | Hoja «Ejecución C1», «Defectos», «Bitácora», «Trazabilidad» y «Tablero» | Yanimar y Francisco |
| 2 | Suite de pruebas automatizadas | Pruebas JUnit (y Cucumber/Playwright en C3) etiquetadas con @HU/@CP | Desarrolladores; QA revisa |
| 3 | Reporte de ejecución y cobertura | Salida de `./mvnw verify`, `jacoco.csv` y captura del resumen en `evidencias/C1/` | Yanimar |
| 4 | Documento de evidencias | Ficha y capturas por caso ejecutado | Yanimar y Francisco |
| 5 | Reportes de defecto | Un DEF-nnn por defecto con evidencia | Quien lo encuentra |
| 6a | Informe de corte del Sprint 1 | Plantilla `INFORME-DE-CORTE.md` al 08/11 | Yanimar |
| 6 | Informe de cierre del ciclo | Plantilla `INFORME-DE-CIERRE-DE-CICLO.md` | Yanimar |
| 7 | Análisis de defectos | Error → defecto → falla y principios | Yanimar |

## 8. Responsabilidades del equipo

| Integrante | Rol / responsabilidad en esta ronda | Casos |
|---|---|---|
| Yanimar Burbano Piamba | Líder de QA: prepara el ciclo, ejecuta y registra los casos funcionales, conduce el triaje y elabora los informes | 44 casos: CP-IDE-001-01, CP-IDE-001-02, CP-TIE-002-01, CP-TIE-002-02, CP-CLI-007-01, CP-CLI-007-02, CP-TIE-009-01, CP-TIE-009-02, CP-TIE-010-01, CP-TIE-010-02, CP-TIE-012-01, CP-TIE-012-02, CP-TIE-013-01, CP-TIE-013-02, CP-SOL-016-01, CP-SOL-016-02, CP-SOL-017-01, CP-SOL-017-02, CP-SOL-017-03, CP-SOL-018-02, CP-SOL-019-01, CP-SOL-019-02, CP-SOL-019-03, CP-SOL-019-04, CP-SOL-020-01, CP-SOL-020-02, CP-SOL-020-03, CP-SOL-021-01, CP-SOL-021-02, CP-ORD-024-01, CP-ORD-024-04, CP-ORD-027-01, CP-ORD-027-02, CP-ORD-028-01, CP-NOT-030-01, CP-NOT-030-02, CP-MEN-032-01, CP-MEN-032-02, CP-ADM-036-01, CP-ADM-036-02, NF-FIA-016, NF-ESC-021, NF-MAN-022, NF-MAN-023 |
| Francisco José Castro Muñoz | QA no funcional y seguridad: ejecuta y registra los casos de seguridad y no funcionales, mantiene los escaneos | 17 casos: CP-IDE-001-03, CP-IDE-003-01, CP-IDE-003-02, CP-IDE-003-03, CP-IDE-004-01, CP-IDE-004-02, CP-IDE-005-01, CP-IDE-005-02, CP-SOL-016-03, CP-SOL-018-01, CP-ORD-024-02, CP-ORD-024-03, CP-ORD-028-02, NF-SEG-006, NF-SEG-011, NF-SEG-014, NF-POR-030 |
| Desarrolladores (Daniel, Deiner, Monica) | Escriben las pruebas automatizadas de su código y corrigen los defectos asignados | — |

## 9. Cronograma

| Actividad | Fecha estimada |
|---|---|
| Plan maestro, libro de control y plan del C1 aprobados | 07/11/2026 |
| JaCoCo y Failsafe en el `pom.xml`; pipeline con `./mvnw verify` | 08/11/2026 |
| Corte del Sprint 1: ejecución de los casos de las historias del Sprint 1 e informe de corte | 08/11/2026 |
| Pruebas unitarias por historia del Sprint 2 (en cada pull request) | 09/11 al 14/11/2026 |
| Integración: PostGIS (CP-SOL-017-*), API y SSE | 12/11 al 14/11/2026 |
| Re-prueba de defectos corregidos | 14/11 al 15/11/2026 |
| Cierre del C1: libro, tablero, evidencias e informe de cierre | 15/11/2026 |

## 10. Riesgos y contingencias

| N.º | Riesgo | Probabilidad | Impacto | Contingencia |
|---|---|---|---|---|
| R1 | No hay pruebas unitarias escritas al cerrar el Sprint 1 | Alta | Alto | El corte del 08/11 registra como «No ejecutado» lo que falte; desde el 09/11 ningún pull request se aprueba sin sus pruebas. |
| R2 | Las reglas con tiempo (vencimientos, límites por hora) son difíciles de probar | Media | Medio | Inyectar `java.time.Clock` en los casos de uso y usar un reloj fijo en las pruebas. |
| R3 | Testcontainers no corre en algún equipo con Windows | Media | Medio | Las pruebas `*IT` corren en GitHub Actions; en local se pueden omitir con `-DskipITs`. |
| R4 | Las pruebas no llevan las etiquetas `@HU`/`@CP` | Media | Medio | QA revisa las etiquetas en el pull request antes de aprobar. |
| R5 | Cobertura por debajo del 70 % | Media | Alto | `jacoco:check` hace fallar el build; se priorizan las reglas de negocio de la estrategia. |

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
