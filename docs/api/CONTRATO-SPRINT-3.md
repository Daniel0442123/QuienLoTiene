# Contrato de la API del Sprint 3 · Quién lo tiene

> **Control del documento** · Versión 0.1 · 19/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: En revisión · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

Acuerdo del «primer día» del backlog del Sprint 3 (`docs/planificacion/BACKLOG-SPRINT-3.md`): rutas, cuerpos, códigos de error y eventos de tiempo real de HU-014, HU-023, HU-025, HU-029, HU-033, HU-034, HU-035, HU-037 y HU-038. HU-026 no necesita rutas nuevas (la orden ya trae la ubicación) y HU-031 se agrega a este contrato cuando entre.

A diferencia del Sprint 2, no hay un `contrato-sprint-3.yaml` escrito a mano: las historias se construyen en el mismo pull request que este contrato, así que la fuente de verdad es `/v3/api-docs` desde el primer día y Monica genera los tipos con el mismo comando de C-04 (PR #48). Las reglas comunes son las del Sprint 2: JWT en `Authorization`, fechas ISO-8601 en UTC, dinero en pesos enteros, `ProblemDetail` con `codigo` y `404` (no `403`) cuando el recurso es de otro usuario (R-03).

## Rutas

| Método y ruta | Rol | Historia | Qué hace |
|---|---|---|---|
| `POST /api/v1/ordenes/{id}/cancelacion` | CLIENTE, TIENDA | HU-029 | Cancelar con `{ "motivo": "..." }` (3 a 200 caracteres). El cliente puede en CONFIRMADO, PREPARANDO y LISTO_PARA_RECOGER; la tienda solo en CONFIRMADO. Responde la orden CANCELADO |
| `GET /api/v1/ordenes/{id}` | CLIENTE, TIENDA | HU-025, HU-029 | Igual que en el Sprint 2, con tres campos nuevos: `reservaVence`, `motivoCancelacion` y `canceladaPor` (`CLIENTE` o `TIENDA`), los tres nulables |
| `POST /api/v1/ordenes/{id}/resena` | CLIENTE | HU-034 | Calificar con `{ "estrellas": 1..5, "comentario": "..." }` (comentario opcional, hasta 500). Solo orden RESUELTA del cliente y una vez |
| `GET /api/v1/ordenes/{id}/resena` | CLIENTE, TIENDA | HU-034 | La reseña de esa orden, o `404 REP-003` si todavía no tiene |
| `GET /api/v1/tiendas/{id}` | todos | HU-014 | Detalle público de una tienda APROBADA: nombre, categoría, teléfono, ubicación, abierta, horario, domicilio y medios de pago. Sin RUT ni documentos |
| `GET /api/v1/tiendas/{id}/resenas` | todos | HU-014, HU-034 | Promedio (0 sin reseñas), total y las 10 reseñas más recientes, sin el nombre del cliente |
| `GET /api/v1/categorias` | todos | HU-038 | Igual que antes, pero solo las categorías activas |
| `GET /api/v1/catalogo/restriccion?producto=` | CLIENTE | HU-023 | `{ "restringido": true, "aviso": "Este medicamento necesita fórmula médica" }` para avisar antes de publicar |
| `POST /api/v1/solicitudes` | CLIENTE | HU-023 | Igual que antes; la respuesta trae `restringida` |
| `GET /api/v1/admin/categorias` | ADMIN | HU-038 | Todas, activas e inactivas |
| `PATCH /api/v1/admin/categorias/{id}` | ADMIN | HU-038 | `{ "activa": false }` para desactivar (y `true` para deshacer). Queda auditado |
| `GET /api/v1/admin/productos-restringidos` | ADMIN | HU-038 | Lista de palabras clave |
| `POST /api/v1/admin/productos-restringidos` | ADMIN | HU-038 | `{ "palabraClave": "amoxicilina" }` (2 a 80). Queda auditado |
| `DELETE /api/v1/admin/productos-restringidos/{id}` | ADMIN | HU-038 | Quitar una palabra. Queda auditado |
| `POST /api/v1/ordenes/{id}/reportes` | CLIENTE, TIENDA | HU-035 | `{ "motivo": "DEMORA_ENTREGA" \| "PRODUCTO_DIFERENTE" \| "COBRO_DIFERENTE" \| "OTRO", "descripcion": "..." }`; la descripción es obligatoria con OTRO (hasta 1000). Queda ABIERTO |
| `GET /api/v1/admin/reportes?estado=ABIERTO` | ADMIN | HU-037 | Reportes con la orden, quién reportó (rol), la tienda y cuántos reportes tiene la tienda |
| `POST /api/v1/admin/reportes/{id}/resolucion` | ADMIN | HU-037 | `{ "nota": "..." }` (3 a 500). Queda RESUELTO y auditado |
| `POST /api/v1/admin/tiendas/{id}/suspension` | ADMIN | HU-037 | `{ "motivo": "..." }`. La tienda queda SUSPENDIDA: no recibe solicitudes y su dueño no puede iniciar sesión (IDE-009). Auditado |
| `POST /api/v1/admin/tiendas/{id}/reactivacion` | ADMIN | HU-037 | Deshace la suspensión. Auditado |
| `GET /api/v1/ordenes/{id}/mensajes` | CLIENTE, TIENDA | HU-033 | Mensajes de la orden, del más antiguo al más reciente, y `soloLectura` |
| `POST /api/v1/ordenes/{id}/mensajes` | CLIENTE, TIENDA | HU-033 | `{ "texto": "..." }` (1 a 1000). Solo con la orden activa |

## Eventos de tiempo real nuevos o con cambios

| `event` | Quién lo recibe | Datos |
|---|---|---|
| `estado-orden-cambiado` | El cliente y la tienda de la orden | Igual que antes y `motivo` (nulable): el motivo cuando se cancela |
| `mensaje-recibido` | La otra parte de la orden | Orden y mensaje (el frontend pide los mensajes por REST) |

## Códigos de error nuevos

| Código | HTTP | Cuándo |
|---|---|---|
| `ORD-005` | 409 | Cancelar una orden que ya salió o se entregó: «escribe a la tienda o reporta un problema» (CP-ORD-029-02) |
| `ORD-006` | 422 | Pedir domicilio en una solicitud de producto restringido (CP-SOL-023-02) |
| `REP-001` | 422 | Calificar una orden que no está RESUELTA o que no es tuya (CP-REP-034-02) |
| `REP-002` | 409 | Calificar dos veces la misma orden (CP-REP-034-03) |
| `REP-003` | 404 | La orden todavía no tiene reseña, o el reporte no existe |
| `REP-004` | 400 | Reseña o reporte con datos que no cumplen las reglas: estrellas fuera de 1 a 5, comentario largo o motivo OTRO sin descripción |
| `REP-005` | 409 | Resolver un reporte que ya está resuelto |
| `CAT-001` | 404 | Categoría o palabra restringida no encontrada |
| `CAT-002` | 409 | La palabra restringida ya está en la lista |
| `MEN-002` | 409 | Escribir en el chat de una orden que ya no está activa (CP-MEN-033-02) |
| `TIE-012` | 409 | Suspender una tienda que no está APROBADA, o reactivar una que no está SUSPENDIDA |

## Decisiones tomadas en este contrato

1. **Quién cancela y cuándo** (diagrama 05 y 15): el cliente cancela mientras la orden no salió de la tienda (CONFIRMADO, PREPARANDO y también LISTO_PARA_RECOGER, porque una recogida no ha salido); la tienda solo en CONFIRMADO, antes de empezar a prepararla. En EN_CAMINO o ENTREGADO responde `ORD-005`; en un estado final (RESUELTA, CANCELADO, VENCIDA) responde `ORD-002`. El motivo es texto libre y llega a la otra parte en el aviso.
2. **La reserva cuenta desde «Listo para recoger»** (HU-025): la nota del diagrama 34 dice que la tienda fija de 30 a 60 minutos en su respuesta y se copian al crear la orden. Eso cambiaría HU-019 (ya cerrada) y haría vencer la reserva mientras la tienda todavía prepara. Se propone: 30 minutos fijos (`quienlotiene.ordenes.minutos-reserva`) desde que la tienda marca LISTO_PARA_RECOGER, que es cuando el cliente puede ir. Una tarea cada minuto pasa a VENCIDA las que pasaron su `reservaVence` y avisa a los dos. Si el comité quiere los 30 a 60 minutos por respuesta, se agrega después sin romper el contrato.
3. **Reseña por orden, no por tienda** (diagrama 09: `orden_id UNIQUE`): solo el cliente de una orden RESUELTA, una vez. El promedio se calcula al consultar la tienda (con un índice por `tienda_id`), sin columna guardada.
4. **Detalle de la tienda en dos rutas:** cualquier usuario con sesión puede ver una tienda APROBADA; una PENDIENTE, RECHAZADA o SUSPENDIDA responde `404 TIE-006`. Las reseñas van en `/resenas`, que sirve el módulo `reputacion`: si `tiendas` pidiera las reseñas a `reputacion` habría un ciclo entre módulos (`reputacion` → `ordenes` → `tiendas` → `reputacion`) y Modulith no lo deja pasar (RNF-021). La pantalla hace las dos consultas en paralelo. Las reseñas muestran estrellas, comentario y fecha, sin el nombre del cliente.
5. **Restringidos:** la lista es de palabras clave. Un producto es restringido si contiene alguna, sin importar mayúsculas ni tildes («Amoxicilina 500 mg» contiene «amoxicilina»). La marca se guarda en la solicitud al publicarla, así que cambiar la lista no cambia solicitudes viejas. Con la marca, las tiendas no ofrecen domicilio (`ofreceDomicilio = false`) y crear la orden con DOMICILIO responde `ORD-006`.
6. **Categorías:** una categoría inactiva desaparece de `GET /api/v1/categorias` (HU-016 y HU-002); las tiendas que ya la tienen la conservan.
7. **Suspender:** suspende la tienda y la cuenta de su dueño, y cierra sus sesiones (los tokens de refresco se revocan). Pide confirmación en la interfaz (SC-011 punto 8). Reportar no suspende solo: lo decide el administrador.
8. **Chat:** solo los dos participantes; se guarda en `mensajeria.mensaje`. Con la orden en un estado final queda en solo lectura: se puede leer pero no escribir (`MEN-002`).

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 19/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión del contrato del Sprint 3 |
