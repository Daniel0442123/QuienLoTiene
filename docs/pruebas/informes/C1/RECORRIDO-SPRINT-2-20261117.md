# Recorrido de punta a punta del Sprint 2 · 17/11/2026

> **Control del documento** · Versión 1.0 · 17/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión y firma de QA) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

| Campo | Valor |
|---|---|
| Ciclo | C1 · Sprint 2 |
| Versión | `main` 5e8ab98 (backend #35, #36, #37, #44; frontend #38 a #43 y #45) |
| Ambiente | Backend real en perfil dev con PostGIS 17-3.5, PWA con Vite, Chromium a 390 × 844 (ver `evidencias/C1/2026-11-17/scripts/README.md`) |
| Datos | 3 droguerías de Farmacia en Popayán (Droguería La 5: efectivo y transferencia, domicilio $ 5.000; Botica San José: efectivo y datáfono, domicilio $ 5.000; Farmacia Popular: solo efectivo, sin domicilio). Clientes Ana Gómez y Luis Pérez con la dirección «Casa» |
| Preparó | Claude Code a pedido de Pedro, para que QA (Yanimar Burbano y Francisco Castro) lo revise, lo complete y lo firme |
| Duración | 1,5 h |

## Resumen

El flujo completo del Sprint 2 funciona contra el backend real: Ana pregunta por «Loratadina 10 mg», la pregunta llega sola a las tres tiendas, La 5 responde «Sí lo tengo», Botica ofrece una alternativa y Popular dice que no. Ana ve las dos respuestas sin recargar, elige a La 5, confirma el pedido a domicilio en efectivo ($ 23.000), la tienda lo prepara, lo envía y lo entrega, y Ana confirma que lo recibió. Se probaron además la solicitud urgente, el medio de pago no aceptado, el vencimiento y los casos de seguridad por API.

- **23 de 23 casos** del Sprint 2 cumplen su resultado esperado (borrador de resultados en `libro/03-EJECUCION-C1.md`).
- **4 defectos nuevos**: DEF-006 (S3, contrato OpenAPI), DEF-007 (S3, medios de pago en «Confirmar pedido»), DEF-008 (S4, encabezado de la solicitud vencida) y DEF-009 (S4, texto del pago en el pedido de la tienda).
- **Re-pruebas**: DEF-002, DEF-003 y DEF-004 pasan; DEF-001 pasa en lo que pedía y lo pendiente queda en DEF-006; DEF-005 sigue abierto (captura 02).
- **C-04**: los tipos se generan, pero el frontend no compila solo con ellos (35 errores). Lista para Monica en `C-04-DIFERENCIAS-TIPOS.md`.

## Resultado por caso

| Caso | Resultado | Evidencia y nota |
|---|---|---|
| CP-SOL-018-01 | Aprobado | 05 y API: sin nombre, teléfono, dirección ni coordenadas; distancia redondeada |
| CP-SOL-018-02 | Aprobado | 05: «a 200 m» (real 236 m) |
| CP-SOL-019-01 | Aprobado | 06 a 08 |
| CP-SOL-019-02 | Aprobado | 09 |
| CP-SOL-019-03 | Aprobado | 11 |
| CP-SOL-019-04 | Aprobado | 12 y API 409 SOL-005 |
| CP-SOL-020-01 | Aprobado | 13 |
| CP-SOL-020-02 | Aprobado | 14 |
| CP-SOL-020-03 | Aprobado | 13 |
| CP-SOL-021-01 | Aprobado (con DEF-008) | 32 a 34 |
| CP-SOL-021-02 | Aprobado | 18 y 19 |
| CP-ORD-024-01 | Aprobado | 16 y 17 |
| CP-ORD-024-02 | Aprobado (con DEF-009) | 21, 27 y API |
| CP-ORD-024-03 | Aprobado | API 404 ORD-001 |
| CP-ORD-024-04 | Aprobado (con DEF-007) | 30 |
| CP-ORD-027-01 | Aprobado | 22 a 25 |
| CP-ORD-027-02 | Aprobado | API 409 ORD-002 |
| CP-ORD-028-01 | Aprobado | 26 |
| CP-ORD-028-02 | Aprobado | API 404 y 409 |
| CP-NOT-030-01 | Aprobado | 01 y 05 |
| CP-NOT-030-02 | Aprobado | 28 |
| CP-MEN-032-01 | Aprobado | 17, 21 y API |
| CP-MEN-032-02 | Aprobado | 13 y API 409 MEN-001 |

Pruebas por API: [api-negativos.md](../../evidencias/C1/2026-11-17/api-negativos.md).

## Capturas

| N.º | Archivo | Qué muestra |
|---|---|---|
| 28 | [28-tienda-urgente-primero.png](../../evidencias/C1/2026-11-17/recorrido/28-tienda-urgente-primero.png) | CP-NOT-030-02: se publican una urgente y luego una normal; la urgente queda primero (osciladores de la alerta: 2) |
| 29 | [29-cliente-solicitud-urgente-respuestas.png](../../evidencias/C1/2026-11-17/recorrido/29-cliente-solicitud-urgente-respuestas.png) | Luis abre su solicitud urgente por URL y ve la respuesta de Droguería La 5 |
| 30 | [30-cliente-medio-no-aceptado.png](../../evidencias/C1/2026-11-17/recorrido/30-cliente-medio-no-aceptado.png) | CP-ORD-024-04 / CP-TIE-012-01: la pantalla ofrece Datáfono aunque La 5 solo acepta efectivo y transferencia; el backend rechaza la orden |
| 31 | [31-cliente-pedido-recoger.png](../../evidencias/C1/2026-11-17/recorrido/31-cliente-pedido-recoger.png) | Con transferencia la orden se crea (recoger en la tienda) |
| 32 | [32-cliente-solicitud-abierta-antes-de-vencer.png](../../evidencias/C1/2026-11-17/recorrido/32-cliente-solicitud-abierta-antes-de-vencer.png) | Solicitud normal de Luis abierta, con la respuesta de La 5, antes de vencer |
| 33 | [33-cliente-solicitud-vencida.png](../../evidencias/C1/2026-11-17/recorrido/33-cliente-solicitud-vencida.png) | CP-SOL-021-01: la tarea de vencimiento la pasa a VENCIDA y el cliente lo ve sin recargar; ya no puede elegir |
| 34 | [34-tienda-respuesta-vencida.png](../../evidencias/C1/2026-11-17/recorrido/34-tienda-respuesta-vencida.png) | La tienda ve su respuesta a la solicitud vencida como «Vencida» |

## Observaciones que no se registraron como defecto

QA decide si alguna merece registro.

- La tienda ve «Vence en 31 min» en una solicitud recién publicada y el cliente «en 30 min» (05 y 04): el redondeo de los minutos es distinto en las dos pantallas.
- Si el cliente sale de «Esperando respuestas», no hay en la app un enlace para volver a su solicitud abierta; solo llega por la URL `/solicitudes/:id` o desde el pedido cuando ya eligió. No hay una historia del Sprint 2 que lo pida.
- Con el pedido ya cerrado, la página del pedido de la tienda pide el enlace de WhatsApp y recibe 409 (se ve en la consola); no se muestra nada al usuario.
- La alerta sonora de la urgente se comprobó contando los osciladores de Web Audio (2, una alerta); en el ambiente de QA no hay salida de audio.
- El vencimiento se forzó cambiando `vence_en` en la base para no esperar 30 minutos; la tarea programada (cada minuto) hizo el resto.

## Firmas

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| QA funcional | Yanimar Burbano Piamba | | |
| QA no funcional y seguridad | Francisco José Castro Muñoz | | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador del recorrido de punta a punta del Sprint 2 |
