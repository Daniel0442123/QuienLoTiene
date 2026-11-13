# Informe de corte · Sprint 1 · Ciclo C1

> Anexo C del Plan Maestro (informe de avance).

| Campo | Valor |
|---|---|
| Fecha / ciclo / sprint / versión | 2026-11-12 / C1 / Sprint 1 / v0.2.0 (`ea7351d`) |
| Elaboró | Yanimar Burbano Piamba |

## 1. Avance (del tablero)

| Responsable | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | No ejecutados |
|---|---|---|---|---|---|---|
| Yanimar Burbano Piamba | 44 | 25 | 25 | 0 | 0 | 19 |
| Francisco José Castro Muñoz | 17 | 13 | 13 | 0 | 0 | 4 |
| **Total** | **61** | **38** | **38** | **0** | **0** | **23** |

Los 23 casos «No ejecutado» son de las historias del Sprint 2 (HU-018, HU-019, HU-020, HU-021, HU-024, HU-027, HU-028, HU-030 y HU-032), que todavía no tienen código. Avance del ciclo: 62,3 %. Aprobación sobre lo ejecutado: 100 %.

## 2. Pruebas automatizadas

| Métrica | Valor |
|---|---|
| Comando | `./mvnw -B clean verify` (Java 21.0.12, Docker con Testcontainers `postgis/postgis:17-3.5-alpine`) |
| Tests run / Failures / Errors / Skipped | Unitarias 209 / 0 / 0 / 0 · Integración 47 / 0 / 0 / 0 · **Total 256 / 0 / 0 / 0** |
| Cobertura de líneas domain + application (total) | 97,2 % (1130 de 1163 líneas) · identidad 94,1 %, clientes 97,9 %, tiendas 98,4 %, solicitudes 100 %, administracion 100 %, catalogo 100 % |
| Módulos por debajo del 70 % | Ninguno |
| Pipeline de GitHub Actions (enlace) | [CI run #83 sobre v0.2.0](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43297877152): compilación, pruebas y cobertura en verde; SpotBugs/FindSecBugs 0 hallazgos, Trivy 0 críticas y 0 altas, Gitleaks sin secretos |

## 3. Historias del sprint

| Historia | Casos | Pruebas automatizadas | Resultado | ¿Terminada según la definición de terminado? |
|---|---|---|---|---|
| HU-001 | CP-IDE-001-01, CP-IDE-001-02, CP-IDE-001-03 | 18 en verde | 3 de 3 aprobados | Sí |
| HU-002 | CP-TIE-002-01, CP-TIE-002-02 | 6 en verde | 2 de 2 aprobados | Sí |
| HU-003 | CP-IDE-003-01, CP-IDE-003-02, CP-IDE-003-03 | 9 en verde | 3 de 3 aprobados | Sí |
| HU-004 | CP-IDE-004-01, CP-IDE-004-02 | 7 en verde | 2 de 2 aprobados | Sí |
| HU-005 | CP-IDE-005-01, CP-IDE-005-02 | 6 en verde | 2 de 2 aprobados | Sí |
| HU-007 | CP-CLI-007-01, CP-CLI-007-02 | 6 en verde | 2 de 2 aprobados | Sí |
| HU-009 | CP-TIE-009-01, CP-TIE-009-02 | 6 en verde | 2 de 2 aprobados | Sí |
| HU-010 | CP-TIE-010-01, CP-TIE-010-02 | 6 en verde | 2 de 2 aprobados | Sí |
| HU-012 | CP-TIE-012-01, CP-TIE-012-02 | 4 en verde | 2 de 2 aprobados | Sí |
| HU-013 | CP-TIE-013-01, CP-TIE-013-02 | 4 en verde | 2 de 2 aprobados | Sí |
| HU-016 | CP-SOL-016-01, CP-SOL-016-02, CP-SOL-016-03 | 15 en verde | 3 de 3 aprobados | Sí |
| HU-017 | CP-SOL-017-01, CP-SOL-017-02, CP-SOL-017-03 | 4 en verde | 3 de 3 aprobados | Sí |
| HU-036 | CP-ADM-036-01, CP-ADM-036-02 | 9 en verde | 2 de 2 aprobados | Sí |

## 4. Defectos nuevos (S1 0 / S2 0 / S3 2 / S4 3)

| ID | Severidad | Prioridad | Estado | Caso | Qué falla |
|---|---|---|---|---|---|
| DEF-001 | S3 Media | P3 Normal | Asignado (Sprint 2) | Exploratoria-20261112 | El contrato OpenAPI de /v3/api-docs no marca required, enum ni nullable en los esquemas |
| DEF-002 | S3 Media | P3 Normal | Asignado (Sprint 2) | NF-SEG-011 | El pipeline de CI no ejecuta OWASP Dependency-Check, que NF-SEG-011 exige junto con Trivy |
| DEF-003 | S4 Baja | P4 Baja | Asignado (Sprint 2) | NF-MAN-022 | jacoco:check valida el 70 % sobre todo el proyecto (BUNDLE) y no por módulo como pide el plan |
| DEF-004 | S4 Baja | P4 Baja | Asignado (Sprint 2) | NF-FIA-016 | La prueba de transiciones inválidas de la tienda no lleva @Tag("NF-FIA-016") |
| DEF-005 | S4 Baja | P4 Baja | Asignado (Sprint 2) | Exploratoria-20261112 | Publicar solicitud muestra los errores por etapas: con el producto vacío no avisa que falta la categoría ni muestra el resumen |

Ningún defecto está en la lógica de negocio: dos son del pipeline y de la configuración de cobertura, uno de documentación de la API, uno de etiquetado de pruebas y uno de usabilidad en la pantalla de publicar solicitud. Por decisión del equipo, todos se corrigen en el Sprint 2.

### Verificación de la interfaz

Además de la suite automatizada, se levantó la PWA con el backend v0.2.0 y se recorrieron 45 vistas y estados de las 13 historias del Sprint 1 en tamaño de celular, con capturas de cada una (`evidencias/C1/2026-11-12/frontend/`). Todo el flujo funciona de punta a punta: registro, entrada con segundo factor del administrador, aprobación, configuración y apertura de la tienda, y la solicitud que llega a 1 tienda abierta. Detalle en `EXPLORATORIA-20261112-yanimar.md`; de ahí salió DEF-005.

## 5. Bloqueos y acciones

| Bloqueo | Casos afectados | Acción acordada | Responsable | Fecha |
|---|---|---|---|---|
| El corte se corrió el 12/11 y no el 08/11: el código del Sprint 1 se terminó después | Todos los del Sprint 1 | Registrar el corte con la fecha real y anotarlo en el acta de inicio | Yanimar Burbano | 2026-11-12 |
| Docker Hub limitó las descargas anónimas y no se pudieron repetir Trivy y Gitleaks en el ambiente de QA | NF-SEG-011, NF-POR-030 | Se usa como evidencia el job de seguridad del CI run #83 sobre el mismo commit | Francisco Castro | 2026-11-12 |
| Las historias del Sprint 2 aún no existen | 23 casos del Sprint 2 | Se ejecutan en cada pull request del Sprint 2 y al cierre del C1 | Yanimar Burbano | 2026-11-15 |

## 6. Plan para el siguiente sprint

1. Corregir DEF-001 a DEF-004 en el Sprint 2 y re-probarlos con el mismo caso antes del cierre del C1.
2. Ejecutar los 23 casos del Sprint 2 a medida que se fusiona cada historia, con sus `@Tag("HU-…")` y `@Tag("CP-…")`.
3. Completar NF-FIA-016 con las máquinas de estado de la solicitud (HU-021) y la orden (HU-027).
4. Cerrar el C1 el 15/11 con el informe de cierre, el documento de evidencias completo y el análisis de defectos.
