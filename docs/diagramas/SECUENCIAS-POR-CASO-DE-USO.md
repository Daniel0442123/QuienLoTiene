# Diagramas de secuencia por caso de uso

Cada caso de uso del diagrama `01-casos-de-uso.drawio` tiene su diagrama de secuencia. Los códigos CU-xx siguen el orden en que aparecen en ese diagrama (Cliente, Tienda, Administrador y Reloj del sistema). Los participantes son los módulos del monolito (diagrama `07-componentes.drawio`): la línea "API REST módulo X" es el controlador y el caso de uso del módulo que recibe la petición, y "módulo X (fachada api)" es la fachada `api/` que otro módulo consulta.

Convención de nombres: `NN-secuencia-cuXX-nombre-corto.drawio`.

| CU | Caso de uso | Actor | Historias | Diagrama |
|---|---|---|---|---|
| CU-01 | Registrarse (incluye aceptar política de datos) | Cliente | HU-001, HU-005 | `10-secuencia-cu01-registrarse.drawio` |
| CU-02 | Iniciar y cerrar sesión | Cliente, Tienda | HU-003 | `11-secuencia-cu02-iniciar-cerrar-sesion.drawio` |
| CU-03 | Gestionar perfil y direcciones | Cliente | HU-007 | `12-secuencia-cu03-gestionar-perfil-direcciones.drawio` |
| CU-04 | Publicar solicitud (incluye difundir y advertir restringido) | Cliente | HU-016, HU-017, HU-023 | `03-secuencia-publicar-responder.drawio` (pasos 1 a 12) |
| CU-05 | Ver y ordenar respuestas | Cliente | HU-020 | `13-secuencia-cu05-ver-ordenar-respuestas.drawio` |
| CU-06 | Ver detalle de tienda | Cliente | HU-014 | `14-secuencia-cu06-ver-detalle-tienda.drawio` |
| CU-07 | Elegir respuesta y crear orden | Cliente | HU-024, HU-025, HU-026 | `04-secuencia-elegir-orden.drawio` (pasos 1 a 9) |
| CU-08 | Cancelar orden | Cliente | HU-029 | `15-secuencia-cu08-cancelar-orden.drawio` |
| CU-09 | Confirmar recepción | Cliente | HU-028 | `04-secuencia-elegir-orden.drawio` (pasos 16 a 20) |
| CU-10 | Calificar tienda | Cliente | HU-034 | `16-secuencia-cu10-calificar-tienda.drawio` |
| CU-11 | Consultar historial y repetir | Cliente | HU-008 | `17-secuencia-cu11-historial-repetir.drawio` |
| CU-12 | Contactar por WhatsApp o mini chat | Cliente, Tienda | HU-032, HU-033 | `18-secuencia-cu12-whatsapp-mini-chat.drawio` |
| CU-13 | Reportar problema | Cliente, Tienda | HU-035 | `19-secuencia-cu13-reportar-problema.drawio` |
| CU-14 | Recibir notificaciones en tiempo real (notificar al cliente) | Cliente | HU-030 | `20-secuencia-cu14-notificaciones-tiempo-real.drawio` |
| CU-15 | Registrar tienda con documentos (incluye aceptar política) | Tienda | HU-002, HU-005 | `21-secuencia-cu15-registrar-tienda.drawio` |
| CU-16 | Configurar perfil y horario | Tienda | HU-009 | `22-secuencia-cu16-perfil-horario.drawio` |
| CU-17 | Configurar domicilio y tarifas | Tienda | HU-010, HU-011 | `23-secuencia-cu17-domicilio-tarifas.drawio` |
| CU-18 | Configurar medios de pago | Tienda | HU-012 | `24-secuencia-cu18-medios-pago.drawio` |
| CU-19 | Abrir o cerrar tienda | Tienda | HU-013 | `25-secuencia-cu19-abrir-cerrar-tienda.drawio` |
| CU-20 | Ver solicitudes nuevas | Tienda | HU-018 | `26-secuencia-cu20-ver-solicitudes-nuevas.drawio` |
| CU-21 | Responder solicitud | Tienda | HU-019 | `03-secuencia-publicar-responder.drawio` (pasos 13 a 20) |
| CU-22 | Actualizar estado de orden | Tienda | HU-027 | `04-secuencia-elegir-orden.drawio` (pasos 10 a 15) |
| CU-23 | Ver indicadores | Tienda | HU-015 | `27-secuencia-cu23-ver-indicadores.drawio` |
| CU-24 | Recibir notificaciones push | Tienda | HU-031 | `28-secuencia-cu24-notificaciones-push.drawio` |
| CU-25 | Iniciar sesión con segundo factor | Administrador | HU-003, HU-004 | `29-secuencia-cu25-login-segundo-factor.drawio` |
| CU-26 | Aprobar o rechazar tiendas | Administrador | HU-036 | `30-secuencia-cu26-aprobar-rechazar-tiendas.drawio` |
| CU-27 | Gestionar reportes y suspender cuentas | Administrador | HU-037 | `31-secuencia-cu27-reportes-suspender.drawio` |
| CU-28 | Gestionar categorías y productos restringidos | Administrador | HU-038 | `32-secuencia-cu28-categorias-restringidos.drawio` |
| CU-29 | Vencer solicitudes sin elección | Reloj del sistema | HU-021 | `33-secuencia-cu29-vencer-solicitudes.drawio` |
| CU-30 | Vencer reservas no recogidas | Reloj del sistema | HU-025 | `34-secuencia-cu30-vencer-reservas.drawio` |
| CU-31 | Ampliar radio de búsqueda (incluye difundir) | Reloj del sistema | HU-022, HU-017 | `35-secuencia-cu31-ampliar-radio.drawio` |

## Casos incluidos o extendidos (morados en el diagrama 01)

No tienen diagrama propio porque se ejecutan dentro de otro caso; aparecen como pasos del diagrama indicado.

| Caso | Dónde aparece |
|---|---|
| Aceptar política de datos (HU-005) | CU-01 y CU-15 |
| Difundir a tiendas elegibles (HU-017) | 03 (pasos 5 a 12) y CU-31 |
| Advertir producto restringido (HU-023) | 03 (opt, paso 4) |
| Notificar al cliente en tiempo real (HU-030) | CU-14 |
| Registrar auditoría (RNF-013) | CU-25, CU-26, CU-27 y CU-28 |

## Supuestos que conviene confirmar con el equipo

- Las rutas REST (`/api/v1/...`) son una propuesta coherente con 03 y 04; ajústenlas cuando escriban los controladores.
- Eventos nuevos que no estaban en el diagrama 07: `MensajeEnviado`, `SolicitudVencida`, `RadioAmpliado` y `TiendaRevisada`.
- Los reportes viven en el módulo `reputacion`, porque el diagrama 07 muestra "administracion → reputacion: api reportes".
- Las categorías y los restringidos los escribe el propio módulo `catalogo` con endpoints restringidos al rol ADMIN.
