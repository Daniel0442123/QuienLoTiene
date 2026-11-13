# Hoja «Bitácora» · Sesiones de prueba

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Una fila por responsable de QA y por día de pruebas. Las ejecuciones automáticas de CI en cada push no se registran aquí; sí las corridas que se usan como evidencia (cierre de historia, corte de sprint, cierre de ciclo, re-prueba).

| Fecha | Ciclo | Sprint | Responsable QA | Horas | Tipo de sesión | Versión / commit | Comando o alcance | Casos ejecutados | Aprobados | Fallidos | Bloqueados | Cobertura domain+application | Defectos nuevos | Impedimentos / bloqueos | Acciones acordadas | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 2026-11-12 | C1 | Sprint 1 | Yanimar Burbano | 0,5 | Automatizada | v0.2.0 · `ea7351d` | `./mvnw -B clean verify` (unitarias + integración con Testcontainers) · casos funcionales del Sprint 1 y NF-FIA-016, NF-ESC-021, NF-MAN-022, NF-MAN-023 | 25 | 25 | 0 | 0 | 97,2 % | 3 (DEF-001, DEF-003, DEF-004) | Docker Hub limitó las descargas anónimas: Trivy y Gitleaks no se repitieron localmente | Se toma como evidencia el CI run #83 del mismo commit. Los defectos se corrigen en el Sprint 2 | Corte del Sprint 1 fuera de fecha (planeado el 08/11). 256 pruebas, 0 fallas. Ejecución preparada con Claude Code; pendiente la revisión y firma de QA |
| 2026-11-12 | C1 | Sprint 1 | Francisco Castro | 0,5 | Automatizada | v0.2.0 · `ea7351d` | `./mvnw -B clean verify` y job «Análisis de seguridad» del CI run #83 · casos de seguridad del Sprint 1 y NF-SEG-006, NF-SEG-011, NF-SEG-014, NF-POR-030 | 13 | 13 | 0 | 0 | 97,2 % | 1 (DEF-002) | Docker Hub limitó las descargas anónimas | Agregar OWASP Dependency-Check en el Sprint 2 | SpotBugs/FindSecBugs 0, Trivy 0 críticas y 0 altas, Gitleaks sin secretos. Pendiente la revisión y firma de QA |
| 2026-11-12 | C1 | Sprint 1 | Yanimar Burbano | 1,0 | Exploratoria | v0.2.0 · `ea7351d` | PWA en local (backend dev + PostGIS + vite), 45 vistas y estados de las 13 historias del Sprint 1 · ver `informes/C1/EXPLORATORIA-20261112-yanimar.md` | 44 vistas | 44 | 0 | 0 | — | 1 (DEF-005) | El mapa de OpenStreetMap sale gris sin internet en el ambiente de QA | Confirmar si el mapa necesita respaldo para la demo | Evidencia complementaria de interfaz; no cambia el resultado de los casos. Pendiente la revisión y firma de QA |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 12/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | docs(pruebas): corte del Sprint 1 del ciclo C1 sobre v0.2.0 (#32) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
