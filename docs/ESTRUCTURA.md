# Estructura del proyecto Quién lo tiene

Monolito modular con Spring Boot y Clean Architecture dentro de cada módulo (ver el documento de arquitectura).

## Backend (`src/main/java/com/example/quienlotiene`)

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

## Otras carpetas

- `frontend/`: aplicación React + Vite (PWA).
- `docs/`: requisitos, arquitectura, diagramas `.drawio` y documentación de la API.
- `docker/`: Dockerfile de la API, inicialización de PostgreSQL/PostGIS y configuración de Caddy.
- `.github/workflows/`: integración continua.
- `scripts/`: utilidades de desarrollo y copias de seguridad.

Los archivos `.gitkeep` solo existen para que Git conserve las carpetas vacías; se pueden borrar cuando la carpeta tenga contenido.
