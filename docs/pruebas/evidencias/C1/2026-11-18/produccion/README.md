# Prueba de punta a punta en producción · 18/11/2026

> **Control del documento** · Versión 1.1 · 18/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión de QA) · Cambios: «Historial del documento» al final

| Campo | Valor |
|---|---|
| Versión | `v0.3.1` en https://quienlotiene.tech (commit `3ee6b2f`) |
| Ambiente | Producción (producción-pruebas, SC-016). Chrome en Windows con Claude in Chrome |
| Cuentas | usuario1 y tienda1 `@prueba.quienlotiene.tech` («Tienda Uno», Farmacia, aprobada y abierta). Pedro escribió las credenciales; Claude hizo los clics |
| Hizo | Pedro (dueño del proyecto) con Claude Code, de 1:39 p. m. a 1:49 p. m. (hora de Bogotá) |
| Objetivo | Re-probar DEF-011 en producción y recorrer el flujo del Sprint 2 |

## Pasos y resultado

| N.º | Paso | Resultado |
|---|---|---|
| 1 | usuario1 pregunta por «Ibuprofeno 400 mg prueba DEF-011», Farmacia, para recoger, ubicación en el centro de Popayán | Pasa. «Le preguntamos a 1 tienda cercana»; la dirección queda en `/solicitudes/nueva?solicitud=d215579b-…` |
| 2 | tienda1 ve la solicitud en «Solicitudes nuevas» (a 1,8 km) y responde «Tengo algo parecido»: Advil 400 mg, caja x 10, $ 7.500 | Pasa. Queda en «Mis respuestas» como «Esperando a que el cliente elija» |
| 3 | usuario1 abre la solicitud, ve la respuesta y recarga con F5 (**DEF-011**) | Pasa. Después de recargar sigue «Esperando respuestas» con la respuesta de Tienda Uno y «Elegir» (captura 01) |
| 4 | usuario1 elige la respuesta y confirma: recoger, efectivo | Pasa. Solo ofrece los 3 medios de pago de la tienda (DEF-007). Pedido D6D0DF3E «Confirmado · 1:45 p. m.»; al recargar se mantiene (captura 02) |
| 5 | Enlace de WhatsApp del cliente | Pasa. `wa.me/57<celular de la tienda>` con el número del pedido en el mensaje. No se abrió |
| 6 | tienda1 abre el pedido | Pasa. Ve nombre y celular del cliente, WhatsApp y «El cliente paga con efectivo» (DEF-009) |
| 7 | tienda1 avanza Preparando › Listo para recoger › Entregado | Pasa. 1:46, 1:46 y 1:47 p. m.; cada cambio dice «El cliente ya lo ve»; al recargar sigue en «Entregado» |
| 8 | usuario1 ve «A las 1:47 p. m., Tienda Uno marcó tu pedido como entregado. ¿Ya lo tienes?» y toca «Recibí mi pedido» | Pasa. «Recibido · 1:49 p. m.»; en «Mis pedidos» aparece en «Anteriores» (captura 03) |

## Observaciones (no son defectos de esta versión)

- El panel de la tienda no muestra su categoría ni su ubicación; para saberlas hubo que consultar la base. Se corrige con SC-017.
- Si el cliente cierra la app sin elegir, no tiene una pantalla con sus preguntas abiertas. Queda propuesto para HU-008 en el Sprint 3 (SC-018).
- Las pestañas del mismo navegador comparten la sesión (cookie de refresco), así que el cliente y la tienda se turnaron en la misma pestaña. Es el comportamiento esperado.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 18/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Registro de la prueba en producción de `v0.3.1`, preparado con Claude Code para que QA lo revise |
| 1.1 | 18/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Las dos observaciones quedan registradas como SC-017 (aprobada) y SC-018 (propuesta) |
