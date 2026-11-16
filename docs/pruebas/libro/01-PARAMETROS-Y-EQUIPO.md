# Hojas «Parámetros» y «Equipo»

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

## Parámetros de la campaña

| Parámetro | Valor | Nota |
|---|---|---|
| Sistema | Quién lo tiene (API REST + PWA) | Monolito modular Spring Boot, 10 módulos |
| Versión en pruebas | v0.2.0 (`ea7351d`) | Corte del Sprint 1, 12/11/2026. Se actualiza con cada despliegue en el ambiente de pruebas |
| Versión objetivo de la liberación | v1.0.0 | Producción el 26/11/2026 |
| Repositorio | GitHub (rama `main`, una rama por historia) | Pull request obligatorio |
| Ambiente local | Java 21, Maven Wrapper, Docker Compose (`postgis/postgis:17-3.5-alpine`) | Pruebas unitarias e integración |
| Ambiente de integración continua | GitHub Actions | `./mvnw verify` en cada pull request |
| Ambiente de pruebas | VPS Hostinger KVM 1 (URL por definir) | Nunca datos personales reales |
| Producción | VPS Hostinger KVM 1 · https://quienlotiene.tech | Solo pruebas de humo |
| Fecha inicio C1 | 02/11/2026 | Sprints 1 y 2 |
| Fecha cierre C1 | 15/11/2026 | Corte parcial el 08/11 |
| Fechas C2 | 16/11/2026 al 22/11/2026 | Sprint 3 |
| Fechas C3 | 23/11/2026 al 25/11/2026 | Cierre: regresión, E2E, no funcionales |
| Comité de liberación (go / no-go) | 25/11/2026 | Acta de liberación |
| Liberación en producción | 26/11/2026 | Pruebas de humo |
| Meta de cobertura (JaCoCo) | ≥ 70 % de líneas en `domain` y `application` | RNF-022 |
| Meta de aprobación para liberar | ≥ 95 % de casos aprobados (resultado final) | Mínimo aceptable con excepción justificada en el acta: 90 % |
| Casos de prioridad Alta ejecutados | 100 % | |
| Defectos S1 y S2 abiertos al liberar | 0 | |
| Defectos S3 abiertos al liberar | ≤ 5, con plan de corrección | |

## Equipo de pruebas

| Integrante | Rol en QA | Responsabilidad | Casos a cargo | Correo |
|---|---|---|---|---|
| Yanimar Burbano Piamba | Líder de QA · pruebas funcionales | Prepara cada ciclo, ejecuta y registra los casos funcionales y E2E, conduce el triaje de defectos, elabora informes y actas | Funcionales (69), E2E (5), arquitectura y cobertura (4 NF) | |
| Francisco José Castro Muñoz | QA no funcional y seguridad | Ejecuta y registra los casos de seguridad y autorización, carga, disponibilidad, accesibilidad y escaneos | Seguridad (16), no funcionales (26) | |

## Participantes que no son QA (para el registro)

| Integrante | Rol | Participación en las pruebas |
|---|---|---|
| Wilson Javier Galindez Certuche | Product Owner | Aprueba el Plan Maestro, prioriza y difiere defectos, firma el acta de liberación |
| Brayan Grajales Escarraga | Scrum Master | Facilita el triaje y elimina impedimentos |
| Daniel Montenegro Almendra | Arquitecto de software · Developer backend | Escribe las pruebas unitarias e integración de identidad, solicitudes y órdenes; corrige defectos; administra el ambiente |
| Deiner Stiven Ramírez Vente | Developer backend | Escribe las pruebas de tiendas, catálogo, notificaciones, mensajería y reputación; corrige defectos |
| Monica Anyi Cajas Mellizo | UX Designer · Developer frontend | Corrige defectos de interfaz y accesibilidad |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 12/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | docs(pruebas): corte del Sprint 1 del ciclo C1 sobre v0.2.0 (#32) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
