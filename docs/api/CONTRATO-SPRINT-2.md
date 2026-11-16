# Contrato de la API del Sprint 2 · Quién lo tiene

> **Control del documento** · Versión 0.2 · 16/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: En revisión · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

Acuerdo del «primer día» del backlog del Sprint 2 (`docs/planificacion/BACKLOG-SPRINT-2.md`): rutas, cuerpos, códigos de error y eventos de tiempo real de HU-018, HU-019, HU-020, HU-021, HU-024, HU-027, HU-028, HU-030 y HU-032. El detalle completo está en [`contrato-sprint-2.yaml`](contrato-sprint-2.yaml) (OpenAPI 3.1); Monica puede generar sus simulaciones de MSW desde ese archivo mientras el backend se construye. Cuando el backend esté fusionado manda `/v3/api-docs`, que ya marca los campos obligatorios y los nulables (DEF-001).

## Rutas

| Método y ruta | Rol | Historia | Qué hace |
|---|---|---|---|
| `GET /api/v1/eventos` | todos | HU-030 | Flujo SSE, una conexión por usuario, latido cada 25 s, `Last-Event-ID` reenvía lo perdido (5 min) |
| `GET /api/v1/solicitudes/disponibles` | TIENDA | HU-018 | Solicitudes abiertas que llegaron a mi tienda y no he respondido; urgentes primero; sin datos del cliente |
| `POST /api/v1/solicitudes/{id}/respuestas` | TIENDA | HU-019 | Responder `SI`, `PARECIDO` o `NO`, una sola vez |
| `GET /api/v1/tiendas/me/respuestas` | TIENDA | HU-019, HU-021 | «Mis respuestas» de las últimas 24 h con estado ESPERANDO, ELEGIDA, NO_ELEGIDA o VENCIDA |
| `GET /api/v1/solicitudes/{id}` | CLIENTE | HU-020, HU-021 | Mi solicitud con estado, respuestas recibidas y orden si ya eligió |
| `GET /api/v1/solicitudes/{id}/respuestas?orden=precio\|distancia\|total` | CLIENTE | HU-020 | Tarjetas `SI` y `PARECIDO` ordenadas; las `NO` solo cuentan |
| `POST /api/v1/ordenes` | CLIENTE | HU-024 | Elegir una respuesta y crear la orden CONFIRMADO con el total desglosado |
| `GET /api/v1/ordenes` | CLIENTE, TIENDA | HU-027 | Mis órdenes, activas primero |
| `GET /api/v1/ordenes/{id}` | CLIENTE, TIENDA | HU-024, HU-027 | Una orden; la tienda ve los datos del cliente solo mientras está activa |
| `PATCH /api/v1/ordenes/{id}/estado` | TIENDA | HU-027 | Solo el paso siguiente válido |
| `POST /api/v1/ordenes/{id}/recepcion` | CLIENTE | HU-028 | «Recibí mi pedido»: ENTREGADO → RESUELTA |
| `GET /api/v1/ordenes/{id}/whatsapp` | CLIENTE, TIENDA | HU-032 | Enlace `wa.me` de la otra parte, solo con orden activa |

## Eventos de tiempo real

| `event` | Quién lo recibe | Datos |
|---|---|---|
| `solicitud-nueva` | Tiendas a las que llegó la solicitud | Producto, cantidad, modalidad, urgente, distancia aproximada, vencimiento |
| `respuesta-recibida` | Cliente (solo `SI` y `PARECIDO`) | Solicitud, respuesta, tipo y nombre de la tienda |
| `solicitud-cerrada` | Tiendas a las que llegó, al elegir o al vencer | Solicitud y estado `RESUELTA` o `VENCIDA` |
| `respuesta-no-elegida` | Tiendas que respondieron y no fueron elegidas | Solicitud y respuesta |
| `solicitud-vencida` | Cliente | Solicitud y respuestas recibidas |
| `orden-creada` | Tienda elegida | Orden, producto, total y modalidad |
| `estado-orden-cambiado` | El cliente y la tienda de la orden, en cada cambio (así se actualiza también la pestaña de quien hizo el cambio) | Orden y estado nuevo |

Los eventos llevan solo ids y datos mínimos (DECISIONES-BACKEND §4.3, diagrama 20): el frontend invalida la consulta de TanStack Query que corresponda y pide el detalle por REST.

## Códigos de error nuevos

Se agregan a `CODIGOS-DE-ERROR.md`: `SOL-003` a `SOL-008`, `ORD-001` a `ORD-004` y `MEN-001`. HU-030 no necesita códigos propios: sin sesión responde `GEN-003`.

## Decisiones tomadas en este contrato

1. **La orden se crea con una fachada, no con un evento** (decisión 3 del backlog). `ordenes` llama a `SolicitudesApi.elegirRespuesta` dentro de la misma transacción: si el medio de pago no se acepta o la dirección está fuera de cobertura, la solicitud no queda RESUELTA. Con un evento el cliente recibiría `201` antes de saber si la orden existe. Es lo que muestra el diagrama 04 (paso 3).
2. **Precio sugerido** (decisión 2 del backlog, SC-011): `precioSugeridoEnPesos` en la tarjeta de la tienda es el último precio por unidad que la tienda dio para el mismo producto (mismo texto sin importar mayúsculas ni espacios). No necesita tabla nueva: sale de las respuestas guardadas. Si SC-011 se rechaza, el campo llega siempre en `null`.
3. **«No lo tengo» con deshacer:** el deshacer lo maneja el frontend esperando unos segundos antes de enviar `NO`. El backend no ofrece borrar respuestas.
4. **Cantidad disponible:** la tienda dice cuántas unidades tiene, hasta la cantidad pedida. El subtotal y el total de la orden usan esas unidades: total = precio × cantidad disponible + domicilio (CP-ORD-024-01).
5. **Domicilio:** el backend calcula `ofreceDomicilio` y `costoDomicilioEnPesos` al responder, con la configuración de HU-010 y la ubicación de la solicitud. Al crear la orden se vuelve a calcular con la dirección elegida.
6. **Distancia aproximada:** la tienda ve la distancia redondeada a 100 m (mínimo 100 m) para no revelar la ubicación exacta del cliente; el frontend la muestra como «a 1,2 km» (CP-SOL-018-02).
7. **Estado de la respuesta para la tienda:** se calcula, no se guarda: ESPERANDO mientras la solicitud está abierta, ELEGIDA o NO_ELEGIDA cuando el cliente eligió y VENCIDA si venció sin elección.
8. **Transiciones de la orden** (diagrama 05): CONFIRMADO → PREPARANDO → EN_CAMINO (domicilio) o LISTO_PARA_RECOGER (recoger) → ENTREGADO → RESUELTA (solo el cliente). Cancelar (HU-029) y la reserva vencida (HU-025) quedan para el Sprint 3.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión del contrato del Sprint 2 para que el frontend empiece en paralelo |
| 0.2 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | `estado-orden-cambiado` llega a las dos partes; el contrato queda alineado con el backend implementado |
