# Registro de cambios por versión · Quién lo tiene

> **Control del documento** · Versión 1.2 · 17/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: Vigente

Notas de versión del producto: qué trae cada etiqueta `v*` que se despliega. Sigue el formato de *Keep a Changelog* y las versiones SemVer de D-22. El detalle de cada PR (autor, fechas, aprobación) está en la [bitácora](BITACORA-DEL-PROYECTO.md); las desviaciones, en el [control de cambios](CONTROL-DE-CAMBIOS.md).

Normas que atiende: ISO/IEC/IEEE 12207:2017 (6.4.10 transición, 6.3.5 gestión de la configuración: identificación de versiones), ISO/IEC/IEEE 29119-3 (las notas de versión son entrada de cada ciclo de pruebas), ISO 10007 (registro del estado de la configuración).

**Cómo se mantiene:** cada versión tiene sus notas y su **evidencia de calidad** (CI, pruebas, cobertura, escaneos de seguridad y despliegue). Al terminar el despliegue de una etiqueta, el flujo `version.yml` crea la Release de GitHub con estas notas y la evidencia generada por `scripts/gestion/evidencia_version.py`, y le adjunta los resultados de pruebas, la cobertura y el SBOM, porque GitHub Actions solo guarda esos artefactos 14 o 30 días. Además, antes de crear una etiqueta, quien la crea mueve lo de «Sin liberar» a una sección nueva con la versión y la fecha, y crea la etiqueta **anotada** (`git tag -a v0.3.0 -m "Sprint 2: …"`), para que quede quién y cuándo. Las etiquetas `v0.1.0` a `v0.2.0` son ligeras (sin autor ni fecha); su fecha aquí es la del commit etiquetado.

---

## Sin liberar

Nada todavía.

## v0.3.0 · 17/11/2026 · Sprint 2 y cierre del ciclo C1

Etiqueta sobre `main` después del #55. Primera versión desplegada con el flujo completo: el cliente pregunta, las tiendas responden en tiempo real, el cliente elige y la tienda entrega el pedido.

### Agregado
- Backend del Sprint 2: contrato de la API, eventos en tiempo real (SSE), respuestas de las tiendas, órdenes con su historial y enlace de WhatsApp (HU-018 a HU-021, HU-024, HU-027, HU-028, HU-030, HU-032, #35). Migraciones nuevas: `solicitudes.respuesta` y `ordenes.orden` con su historial.
- Hasta 5 conexiones de tiempo real por usuario (HU-030, SC-014, #44).
- Frontend: tipos generados desde el contrato y simulación de respuestas y órdenes (#38).
- Frontend: solicitudes nuevas y responder desde la tienda (HU-018, HU-019, #39).
- Frontend: respuestas y estado de la solicitud para el cliente (HU-020, HU-021, #40).
- Frontend: elegir una respuesta y confirmar el pedido (HU-024, #41).
- Frontend: seguimiento del pedido, recepción y WhatsApp (HU-027, HU-028, HU-032, #42).
- Frontend: tiempo real con SSE y alerta sonora de las solicitudes urgentes (HU-030, #43).
- CI: OWASP Dependency-Check junto con Trivy (DEF-002, #36).
- Operación: `scripts/limpieza/limpiar.sh` para borrar los datos de prueba de producción, con modos «pruebas» y «total», respaldo previo y bloqueo al salir a producción real (SC-016, #56). El despliegue lo copia al VPS.
- Gestión: seguimiento y control de cambios del proyecto (bitácora, solicitudes de cambio, procedimientos, libro de control de cambios y chequeo «Control de cambios» en CI) y Release automática de cada versión (SC-009, #34). Decisiones de interfaz de los Sprints 2 y 3 (SC-011, #37).
- Pruebas: corte del Sprint 1 (#32), ejecución del Sprint 2, re-pruebas, informe de cierre del C1 y sus Word y Excel de entrega (#46, #49, #52 a #55).

### Corregido
- DEF-001 y DEF-006: el contrato OpenAPI declara las respuestas exitosas, los errores como `application/problem+json`, los enums con nombre y los eventos SSE; el frontend genera sus tipos desde `/v3/api-docs` (C-04, #35, #46, #48).
- DEF-003: la cobertura mínima de 70 % se exige por módulo (#35).
- DEF-004: la prueba de transiciones inválidas de la tienda lleva `@Tag("NF-FIA-016")` (#35).
- DEF-005: «Publicar solicitud» marca todos los datos que faltan al primer intento (#47).
- DEF-007: «Confirmar pedido» solo ofrece los medios de pago que acepta la tienda (#46, #47).
- DEF-008: la solicitud vencida ya no dice «Esperando respuestas» (#47).
- DEF-009: la tienda lee «El cliente paga con…» en su pedido (#47).
- DEF-010: sin punto doble después de la hora (#50).
- Pruebas del frontend más estables (#45, #51).

### Evidencia de calidad

Tomada de la [Release v0.3.0](https://github.com/Daniel0442123/QuienLoTiene/releases/tag/v0.3.0), que generó `version.yml` el 17/11/2026 00:25 sobre el commit `ed3da54`. La Release guarda también los resultados de las pruebas, el reporte de JaCoCo y el SBOM.

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: OWASP Dependency-Check, análisis de seguridad, compilar, pruebas y cobertura | [44022342818](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44022342818) · 17/11/2026 00:13 | ✅ Correcto |
| CI frontend (último cambio del frontend incluido, `e679ef3`) | [44009478982](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44009478982) | ✅ Correcto |
| Despliegue: pruebas, imágenes con Trivy, GHCR, VPS y verificación HTTPS | [44022656849](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44022656849) · 17/11/2026 00:25 | ✅ Correcto |

Pruebas del backend en esta versión: 333 (278 unitarias y 55 de integración), 0 fallas. Cobertura de líneas: 95,2 % en `domain` y `application` (meta ≥ 70 %, RNF-022) y 60,8 % en total.

## v0.2.0 · 12/11/2026 · Cierre funcional del Sprint 1

Commit `ea7351d`. Base del corte del Sprint 1 del ciclo C1 (PR #32).

### Agregado
- Frontend: pantalla del código TOTP del administrador (HU-004, #23).
- Frontend: registro de tienda con documentos y mapa con Leaflet (HU-002, HU-005, #24).
- Frontend: panel de la tienda con apertura, horario, domicilio y medios de pago (HU-009, HU-010, HU-012, HU-013, #26).
- Frontend: publicar solicitud (HU-016, #27).
- Frontend: revisión de tiendas por el administrador (HU-036, #28).
- Frontend: perfil y direcciones del cliente (HU-007, #29).
- Frontend: identidad visual «Verde botica», Atkinson Hyperlegible e íconos (#30, SC-008).
- Backend: `GET /api/v1/categorias` y tabla `catalogo.categoria` con Farmacia y Ferretería (#31, SC-007).

### Corregido
- La prueba de restauración de respaldos espera al servidor definitivo de PostGIS (#25).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43297877152](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43297877152) · 12/11/2026 15:11 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43297877152](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43297877152) · 12/11/2026 15:11 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [43291603040](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43291603040) · 12/11/2026 14:34 (commit `d5005aa`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43298859794](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43298859794) · 12/11/2026 15:17 | ✅ Correcto |

Pruebas del backend en esta versión: 256 (209 unitarias y 47 de integración), 0 fallas, 97,2 % de cobertura de líneas en `domain` y `application` (corte del Sprint 1, PR #32, `docs/pruebas/evidencias/C1/2026-11-12/`). Las 47 de integración se confirmaron en el registro del job de CI.

## v0.1.2 · 11/11/2026

Commit `826c359`.

### Agregado
- Frontend: simulación de la API con MSW para trabajar sin backend (#21).
- Frontend: registro de cliente con autorización de datos y cierre de sesión (HU-001, HU-003, HU-005, #22).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43156152498](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152498) · 11/11/2026 23:38 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43156152498](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152498) · 11/11/2026 23:38 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [43156152502](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156152502) · 11/11/2026 23:38 (este commit) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43156648125](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43156648125) · 11/11/2026 23:43 | ✅ Correcto |

## v0.1.1 · 11/11/2026

Commit `b117133`.

### Seguridad
- Caddy 2.11.9 compilado con `golang.org/x/net` corregido para pasar Trivy; el despliegue de `v0.1.0` se había detenido (#20, SC-006).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43150490243](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150490243) · 11/11/2026 22:47 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43150490243](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150490243) · 11/11/2026 22:47 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [42497112512](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/42497112512) · 08/11/2026 10:53 (commit `cdb743b`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43150916232](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43150916232) · 11/11/2026 22:51 | ✅ Correcto |

## v0.1.0 · 11/11/2026 · Primera versión desplegada

Commit `41d19b8`. Incluye el Sprint 0 y el backend del Sprint 1.

### Agregado
- Base técnica: `pom.xml`, perfiles, Compose con PostGIS, primera migración y CI (#1).
- `shared`: errores con ProblemDetail, correlación y reloj (T-01, #2); seguridad con JWT RS256, roles y Argon2id (T-02, #3).
- `identidad`: registro de cliente (HU-001, #4), autorización de datos (HU-005, #5), inicio, renovación y cierre de sesión (HU-003, #6), segundo factor TOTP del administrador (HU-004, #8).
- `solicitudes`: publicar con límite de 10 por hora (HU-016, #7, SC-001, SC-002, SC-003) y difusión a tiendas elegibles (HU-017, #13).
- `tiendas`: registro con documentos (HU-002, #9), perfil y horario (HU-009, #11), abrir o cerrar y `TiendasApi` (HU-013, #12), domicilio con tarifa fija (HU-010, #14), medios de pago (HU-012, #15).
- `administracion`: aprobar o rechazar tiendas con auditoría (HU-036, #10).
- `clientes`: perfil y direcciones (HU-007, #16).
- Frontend: andamiaje React + Vite + TypeScript, decisiones F-01 a F-23 y WCAG 2.2 AA (#17, SC-004, SC-005).
- Despliegue: imágenes, Compose de producción, despliegue por etiquetas y respaldos (#19).

### Seguridad
- Frontend: `minimatch` y `glob` vulnerables corregidos con un `override` de `glob` 14 (#18).

### Evidencia de calidad

| Verificación | Corrida | Resultado |
|---|---|---|
| CI backend: compilar, pruebas unitarias e integración, cobertura ≥ 70 % | [43148068473](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148068473) · 11/11/2026 22:25 | ✅ Correcto |
| CI backend: SpotBugs y FindSecBugs, Trivy de dependencias, Gitleaks, SBOM | [43148068473](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148068473) · 11/11/2026 22:25 | ✅ Correcto |
| CI frontend: formato, lint, tipos, Vitest con cobertura, npm audit, build y Lighthouse | [42497112512](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/42497112512) · 08/11/2026 10:53 (commit `cdb743b`, último cambio del frontend incluido) | ✅ Correcto |
| Despliegue: pruebas, imágenes, Trivy de imágenes, GHCR y VPS | [43148805407](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/43148805407) · 11/11/2026 22:31 | ❌ Falló en «Trivy (imágenes)»: vulnerabilidades críticas en Caddy; se corrigió en v0.1.1 (SC-006) |

## Antes de los pull requests · 28/10/2026 y 29/10/2026

Commits directos a `main` (`289b02f` a `d0a19d2`): estructura inicial, paquete `com.quienlotiene` y plan de proyecto en normas APA.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión: notas de `v0.1.0` a `v0.2.0` reconstruidas desde Git, con la evidencia de calidad de cada versión tomada de GitHub Actions (SC-009) |
| 1.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Notas de `v0.3.0` (Sprint 2 y cierre del C1) antes de etiquetar |
| 1.2 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Evidencia de calidad de `v0.3.0` copiada de su Release |
