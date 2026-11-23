# Recorrido en producción del corte del Sprint 3 · 21/11/2026

> **Control del documento** · Versión 1.0 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión de QA) · Cambios: «Historial del documento» al final

| Campo | Valor |
|---|---|
| Versión | `v0.5.2` en https://quienlotiene.tech (commit `8d64314`) |
| Ambiente | Producción (producción-pruebas, SC-016). Cliente en el Chrome de Daniel Montenegro; tienda y administrador en el navegador integrado de Claude; push en un celular Android con Chrome |
| Cuentas | usuario1 y tienda1 `@prueba.quienlotiene.tech` («Tienda Uno», Farmacia) y la cuenta ADMIN de Daniel Montenegro con segundo factor. Daniel Montenegro escribió las credenciales |
| Hizo | Daniel Montenegro (dueño del proyecto) con Claude Code, el 21/11 de 9:40 p. m. a 10:48 p. m. (hora de Bogotá) |
| Objetivo | Probar en la interfaz real las historias del Sprint 3 que se pueden probar sin tocar datos reales |
| Ajuste temporal | El horario de Tienda Uno se amplió para que recibiera preguntas un sábado de noche; se devolvió a lunes a sábado de 07:00 a 18:00 y domingo cerrado a las 10:52 p. m. |

## Pasos y resultado

| N.º | Caso | Paso | Resultado | Capturas |
|---|---|---|---|---|
| 1 | SC-020 | Entrar como cliente, tienda y administrador | Pasa. Cada rol ve su menú lateral; el cliente no ve opciones de tienda ni de administración | 00, 03, 36 |
| 2 | CP-CAT-038-02 | Administrador: lista de productos restringidos | Pasa. Están Amoxicilina, Azitromicina y Ciprofloxacina | 36 |
| 3 | CP-SOL-023-01 / 023-02 | usuario1 pregunta por «Amoxicilina 500 mg» en Farmacia | Pasa. Aviso de consultar con la farmacia; domicilio «no disponible: necesita fórmula médica» | 01 |
| 4 | CP-CLI-008-03 | usuario1 sale sin elegir y entra a «Mis pedidos › Mis preguntas» | Pasa. Ve la pregunta abierta y la respuesta llega sin recargar | 02, 07, 08 |
| 5 | CP-CLI-008-01 | «Repetir» sobre el Paracetamol | Pasa. Abre una pregunta nueva con el mismo producto y cantidad; llega a 1 tienda | 05, 06 |
| 6 | CP-TIE-014-01 / 014-02 | Detalle público de Tienda Uno desde la respuesta | Pasa. Horario, teléfono, mapa, «Cómo llegar», domicilio $ 6.000, medios de pago y reseñas; no muestra RUT ni documentos | 09 a 11 |
| 7 | Humo | Elegir, recoger, efectivo | Pasa. Pedido ECF43503, $ 3.500, «Confirmado · 10:15 p. m.»; la tienda lo recibe en tiempo real | 12 a 14 |
| 8 | CP-ORD-026-01 | «Cómo llegar» | Pasa. Abre Google Maps en Cl. 18 Nte. # 8-90 | 15 |
| 9 | CP-MEN-033-01 | Chat con cliente y tienda abiertos a la vez | Pasa. Mensajes en los dos sentidos sin recargar (10:17 y 10:18 p. m.) | 16, 17, 19 |
| 10 | HU-025 | La tienda marca «Listo para recoger» | Pasa. Reserva «hasta las 10:48 p. m. · 30 min» en los dos lados (empieza en «Listo para recoger», SC-019) | 18, 20 |
| 11 | CP-ORD-025-02 | Entregado y Recibido | Pasa. Los dos a las 10:20 p. m. | 21, 22 |
| 12 | CP-REP-034-01 / 034-03 | Calificar a Tienda Uno | Pasa. 5 estrellas, «Excelente Servicio» (10:21 p. m.); el botón cambia a «Tu calificación» | 22 a 24 |
| 13 | CP-MEN-033-02 | Chat del pedido recibido | Pasa. Solo lectura | 25 |
| 14 | Humo | Pregunta a domicilio sin dirección guardada | Pasa. Pide guardar una dirección; Daniel Montenegro agregó «Casa» | 26 a 30 |
| 15 | CP-ORD-029-01 | Pedido 5E39F3E9 ($ 8.500, domicilio) en «Preparando»; el cliente cancela con motivo | Pasa. Cancelado a las 10:29 p. m.; la tienda lo ve cancelado y sin los datos del cliente | 31, 32 |
| 16 | CP-ORD-029-02 | Pedido F5305EE7 «En camino» (10:32 p. m.) | Pasa. No aparece «Cancelar pedido», solo «Reportar un problema» | 33 |
| 17 | CP-REP-035-01 | El cliente reporta «Demora en la entrega» (10:33 p. m.) | Pasa. El administrador lo ve Abierto | 34, 35, 37 |
| 18 | CP-ADM-037-02 | El administrador resuelve el reporte con una nota | Pasa. Resuelto a las 10:36 p. m. | 38 |
| 19 | NF-COM-025 | Registro de red del navegador durante el recorrido | Pasa. Todas las llamadas a `https://quienlotiene.tech/api/v1/…`, JSON, respuesta 200 | — |
| 20 | CP-NOT-031-01 | Celular: activar avisos como tienda1 y cerrar la app; usuario1 pregunta «Ibuprofeno prueba push» | Pasa. Con la app (PWA) cerrada llega «Solicitud nueva · Un vecino pregunta si tienes un producto. Toca para responder.» a las 10:40 p. m., sin datos del cliente; al tocarla abre la solicitud para responder | 39 a 44 |
| 21 | CP-NOT-031-02 | Celular: bloquear notificaciones del sitio; usuario1 publica «Ibuprofeno prueba bloqueo» | Pasa. No llega ningún aviso; al abrir la app la solicitud está en «Solicitudes nuevas» y el panel dice «Bloqueaste los avisos de ¿Quién lo tiene? en este navegador…» (10:44 a 10:48 p. m.) | 04, 45 a 48 |

No se hicieron en producción porque cambian datos reales: suspender cuentas (CP-ADM-037-01), desactivar categorías (CP-CAT-038-01), calificar sin orden (CP-REP-034-02), reportar una orden ajena (CP-REP-035-02) y dejar vencer una reserva (CP-ORD-025-01). Quedan cubiertos por las pruebas automatizadas de `../mvn-verify.txt`.

## Defecto y observaciones

- **DEF-012 (S4):** en «Mis respuestas» de la tienda, las respuestas de pedidos ya cancelados (5E39F3E9) o recibidos (ECF43503) siguen diciendo «Elegida · El cliente te eligió. Prepara el pedido.».
- El detalle de la tienda no tiene sección de fotos. Es lo acordado: los tableros 11 a 13 dejan HU-014 sin foto (`docs/diseno/mockups/README.md`), aunque el backlog todavía las nombra.
- Al enviar un reporte no aparece un mensaje de confirmación.
- La cuenta regresiva de la reserva muestra 31 minutos en la tienda y 30 en el cliente.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Registro del recorrido en producción de `v0.5.2` para el corte del Sprint 3, preparado con Claude Code para que QA lo revise |
