# Estructura del proyecto Quién lo tiene

Monolito modular con Spring Boot y Clean Architecture dentro de cada módulo (ver el documento de arquitectura).

## Backend (`src/main/java/com/quienlotiene`)

Un paquete por módulo de negocio: `identidad`, `clientes`, `tiendas`, `catalogo`, `solicitudes`, `ordenes`, `mensajeria`, `reputacion`, `notificaciones`, `administracion`.

Dentro de cada módulo:

| Carpeta | Contenido | Puede depender de |
|---|---|---|
| `api/` | Fachada pública y eventos que el módulo expone a los demás | application |
| `domain/model/` | Entidades, objetos de valor, máquinas de estado | solo Java y `shared` |
| `domain/event/` | Eventos de dominio | solo Java y `shared` |
| `domain/port/` | Interfaces Repository y de servicios externos | solo Java y `shared` |
| `application/usecase/` | Un caso de uso por acción del usuario | domain |
| `application/dto/` | DTOs de entrada y salida | domain |
| `infrastructure/web/` | Controladores REST (`/api/v1/...`) | application, domain |
| `infrastructure/persistence/` | Entidades JPA y adaptadores Spring Data | application, domain |

`shared/` es el núcleo compartido mínimo: tipos comunes (`domain/`) y configuración transversal (`infrastructure/config`, `security`, `web`, `storage`, `mail`).

## Recursos y pruebas

- `src/main/resources/db/migration/<modulo>/`: migraciones Flyway, una carpeta por módulo (un esquema de PostgreSQL por módulo).
- `src/test/java/.../arquitectura/`: pruebas de Spring Modulith y ArchUnit que verifican las fronteras.
- `src/test/java/.../<modulo>/`: pruebas de cada módulo.
- Convención: `*Test.java` = unitarias (`mvn test`), `*IT.java` = integración (`mvn verify`). Cada prueba lleva `@Tag("HU-xxx")` y `@Tag("CP-xxx")`. Detalle en `docs/pruebas/ESTRATEGIA-DE-PRUEBAS.md`.

## Otras carpetas

- `frontend/`: aplicación React + Vite + TypeScript (PWA). Estructura y comandos en `frontend/README.md`; convenciones en `docs/arquitectura/DECISIONES-FRONTEND.md`.
- `docs/despliegue/`: cómo está montado el VPS, cómo desplegar, volver atrás y restaurar respaldos.
- `docs/`: requisitos, arquitectura, diagramas `.drawio` y documentación de la API.
- `docs/pruebas/`: estrategia de pruebas, bitácora de ejecuciones, plantillas y registros de cada ejecución con sus evidencias.
- `docker/`: Dockerfile de la API (`docker/api`) y de Caddy con el build de la PWA y su `Caddyfile` (`docker/caddy`).
- `compose.yaml`: base local para desarrollo; `compose.prod.yaml`: producción en el VPS (ver `docs/despliegue/DESPLIEGUE.md`).
- `.github/workflows/`: integración continua (`ci.yml`, `frontend.yml`) y despliegue al VPS con etiquetas `v*` (`despliegue.yml`).
- `scripts/`: utilidades de desarrollo; `scripts/respaldo/` tiene el respaldo diario con restic y la restauración.

Los archivos `.gitkeep` solo existen para que Git conserve las carpetas vacías; se pueden borrar cuando la carpeta tenga contenido.
