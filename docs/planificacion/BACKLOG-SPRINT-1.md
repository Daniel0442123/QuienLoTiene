# Backlog del Sprint 1 por persona y módulo · Quién lo tiene

Versión 1.0 · 07/11/2026 · Propuesta para la planning del Sprint 1 (la ajustan Wilson como Product Owner y Brayan como Scrum Master).

Fuentes: roles de `docs/presentacion/prompt-claude-design.md` (matriz RACI), alcance del Sprint 1 en `docs/pruebas/02-PLAN-DE-PRUEBAS-C1.md`, casos en `docs/pruebas/libro/02-CASOS.md`, convenciones en `docs/arquitectura/DECISIONES-BACKEND.md`.

> **Ojo con las fechas:** el plan de proyecto ponía el Sprint 1 del 02/11 al 08/11, pero el código del backend empieza ahora. Hay que decidir en la planning si el Sprint 1 se corre una semana (09/11 al 15/11) o si se reparte su alcance entre los Sprints 1 y 2. El corte de pruebas del 08/11 quedaría con el estado de este Sprint 0.

## 1. Objetivo del sprint

Que un cliente se registre e inicie sesión, que una tienda se registre y el administrador la apruebe, y que el cliente publique una solicitud que llegue a las tiendas abiertas dentro del radio.

## 2. Equipo y responsabilidades

| Integrante | Rol | Responsable en este sprint |
|---|---|---|
| Daniel Montenegro Almendra | Arquitecto de software · backend | Núcleo `shared`, módulos `identidad` y `solicitudes`; aprueba todos los pull requests del backend |
| Deiner Stiven Ramírez Vente | Backend | Módulos `tiendas`, `clientes` y `administracion` |
| Monica Anyi Cajas Mellizo | UX · frontend | Proyecto React base y pantallas del sprint |
| Yanimar Burbano Piamba | QA funcional | Casos funcionales del C1, libro de control, revisión de etiquetas `@HU`/`@CP` en los pull requests |
| Francisco José Castro Muñoz | QA no funcional y seguridad | Casos de seguridad, escaneos del pipeline (Trivy, SpotBugs, Gitleaks) |
| Wilson Javier Galindez Certuche | Product Owner · documentador | Prioridad y criterios de aceptación, códigos de error y documentación |
| Brayan Grajales Escarraga | Scrum Master | Tablero en GitHub Projects, ceremonias, impedimentos, informe de corte |

## 3. Historias por persona

Orden sugerido: primero lo que desbloquea a los demás (columna "Depende de").

### Daniel · `shared`, `identidad`, `solicitudes`

| Orden | Historia | Módulo | Casos de prueba | Depende de |
|---|---|---|---|---|
| 1 | T-01 Base común: `ReglaNegocioException`, `ManejadorGlobalErrores` (ProblemDetail), filtro `X-Correlacion-Id`, `Clock` como bean | shared | — | — |
| 2 | T-02 Seguridad: emisión y validación de JWT RS256, token de refresco rotativo, Argon2id, roles | shared / identidad | — | T-01 |
| 3 | HU-001 Registrar cliente | identidad | CP-IDE-001-01, -02, -03 | T-02 |
| 4 | HU-005 Aceptar política de datos | identidad | CP-IDE-005-01, -02 | HU-001 |
| 5 | HU-003 Iniciar y cerrar sesión | identidad | CP-IDE-003-01, -02, -03 | T-02 |
| 6 | HU-016 Publicar solicitud (límite de 10 por hora) | solicitudes | CP-SOL-016-01, -02, -03 | HU-003 |
| 7 | HU-017 Difundir a tiendas elegibles (PostGIS, radio 3 km) | solicitudes | CP-SOL-017-01, -02, -03 | HU-016, `TiendasApi` de HU-013 |
| 8 | HU-004 Segundo factor del administrador (TOTP) | identidad | CP-IDE-004-01, -02 | HU-003 |

### Deiner · `tiendas`, `clientes`, `administracion`

| Orden | Historia | Módulo | Casos de prueba | Depende de |
|---|---|---|---|---|
| 1 | HU-002 Registrar tienda (queda Pendiente, documentos en `AlmacenArchivos`) | tiendas | CP-TIE-002-01, -02 | T-01 |
| 2 | HU-036 Aprobar o rechazar tiendas | administracion | CP-ADM-036-01, -02 | HU-002 |
| 3 | HU-009 Configurar perfil y horario de la tienda | tiendas | CP-TIE-009-01, -02 | HU-002 |
| 4 | HU-013 Abrir o cerrar tienda + fachada `TiendasApi` para la difusión | tiendas | CP-TIE-013-01, -02 | HU-036 |
| 5 | HU-010 Configurar domicilio con tarifa fija | tiendas | CP-TIE-010-01, -02 | HU-009 |
| 6 | HU-012 Configurar medios de pago | tiendas | CP-TIE-012-01, -02 | HU-009 |
| 7 | HU-007 Gestionar perfil y direcciones del cliente | clientes | CP-CLI-007-01, -02 | HU-001 |

La fachada `TiendasApi` (consulta de tiendas aprobadas y abiertas dentro de un radio) la acuerdan Daniel y Deiner el primer día, para que HU-017 no espere a que HU-013 esté terminada.

### Monica · frontend

| Orden | Tarea | Depende de |
|---|---|---|
| 1 | Proyecto React base en `frontend/` (Vite, rutas, cliente HTTP con el token, manejo de errores ProblemDetail) | — |
| 2 | Pantallas de registro de cliente con aceptación de la política, inicio y cierre de sesión | HU-001, HU-003, HU-005 |
| 3 | Registro de tienda con carga de documentos | HU-002 |
| 4 | Panel de la tienda: perfil, horario, abrir/cerrar, domicilio y medios de pago | HU-009 a HU-013 |
| 5 | Publicar solicitud con ubicación | HU-016 |
| 6 | Pantalla del administrador para aprobar o rechazar tiendas | HU-036 |

Mientras el backend no esté listo, Monica trabaja contra el contrato OpenAPI que publique cada historia en `/swagger-ui`.

### Yanimar · QA funcional

- Diligenciar el acta de inicio del C1 y la ejecución C1 en `docs/pruebas/libro/` a medida que se cierra cada historia.
- Revisar en cada pull request que las pruebas tengan `@Tag("HU-xxx")` y `@Tag("CP-xxx")` y cubran los criterios de aceptación.
- Casos a su cargo: los marcados "Yanimar" en el plan C1 (HU-001, HU-002, HU-007, HU-009, HU-010, HU-012, HU-013, HU-016, HU-017, HU-036).
- NF-FIA-016 (máquinas de estado), NF-ESC-021 (Modulith) y NF-MAN-022/023 (JaCoCo y ArchUnit), que ya corren en el pipeline.

### Francisco · QA no funcional y seguridad

- Casos de seguridad: CP-IDE-001-03, HU-003, HU-004, HU-005 y CP-SOL-016 (límite por hora).
- NF-SEG-006 (frase ≥ 15 caracteres y Argon2id), NF-SEG-011 (Trivy sin críticas ni altas), NF-SEG-014 (fecha y versión de la política), NF-POR-030 (Gitleaks sin secretos).
- Revisar el reporte de SpotBugs/FindSecBugs del pipeline y abrir los defectos que salgan.

### Wilson · Product Owner y documentación

- Confirmar en la planning el orden de las historias y sus criterios de aceptación.
- Crear y mantener `docs/api/CODIGOS-DE-ERROR.md` con los códigos que vayan saliendo (`IDE-`, `TIE-`, `SOL-`, ...).
- Aceptar cada historia en la review del domingo.

### Brayan · Scrum Master

- Cargar este backlog en GitHub Projects (una tarjeta por historia, con responsable y casos).
- Daily de 15 minutos, review y retrospectiva; registrar impedimentos.
- Informe de corte del Sprint 1 con Yanimar y Francisco.

## 4. Definición de terminado (recordatorio)

Pull request aprobado por el arquitecto de software, CI en verde (formato, unitarias, Modulith, ArchUnit, cobertura ≥ 70 % en `domain` y `application`, integración y escaneos), criterios de aceptación verificados por QA y resultado anotado en el libro de control.

## 5. Base técnica lista (Sprint 0)

- `pom.xml` según la sección 19 de las decisiones del backend.
- `compose.yaml` con PostGIS y secretos en `.env` (plantilla `.env.ejemplo`).
- Primera migración Flyway: extensiones `postgis`, `pg_trgm`, `unaccent`, un esquema por módulo y tablas de eventos de Spring Modulith.
- Pruebas de arquitectura (`ModulosTest`, `ReglasDeArquitecturaTest`) y prueba de arranque con Testcontainers (`QuienLoTieneApplicationIT`).
- Pipeline `.github/workflows/ci.yml`, plantilla de pull request y `CODEOWNERS`.
