# NF-SEG-013 · Re-prueba en producción sobre v0.5.3

- Fecha y hora: 22/11/2026 de 9:47 a. m. a 9:50 a. m. (hora de Bogotá). Las capturas del 21/11 a las 11:59 p. m. y del 22/11 a las 12:02 a. m. son de los inicios de sesión del administrador.
- Versión: `v0.5.3` (`ff9ac6d`).
- Cómo:
  1. Daniel Montenegro inició sesión como administrador y después como tienda1 en el navegador de Claude.
  2. Como tienda1 se abrió el pedido F5305EE7, que está «En camino» y muestra el nombre, el celular y la dirección del cliente. La pantalla pidió también `GET /ordenes/{id}/whatsapp`.
  3. Daniel Montenegro leyó el log en el VPS con `docker compose -f compose.prod.yaml logs api --since 30m | grep -E "inició sesión|vio los datos del cliente|pidió el contacto"`.

| Hora (UTC) | Logger | Mensaje |
|---|---|---|
| 04:59:07 y 05:02:10 | auditoria | El administrador `7025f7f6-…` inició sesión con segundo factor |
| 14:47:49 | auditoria | El usuario `36fd97ff-…` inició sesión |
| 14:48:55 | auditoria | La tienda del usuario `36fd97ff-…` vio los datos del cliente de la orden `f5305ee7-…` |
| 14:48:55 | auditoria | La tienda del usuario `36fd97ff-…` pidió el contacto del cliente de la orden `f5305ee7-…` |
| 14:49:10 y 14:49:40 | auditoria | La tienda del usuario `36fd97ff-…` vio los datos del cliente de la orden `f5305ee7-…` (al volver a la pantalla) |

Cada línea trae fecha, usuario, acción e id de correlación, y ninguna trae el correo, la frase, ni el nombre o el celular del cliente. El pedido ECF43503, ya cerrado, no muestra los datos del cliente y por eso no deja registro.

Resultado: **Aprobado**. Capturas: [`NF-SEG-013-01-log-admin.png`](NF-SEG-013-01-log-admin.png) y [`NF-SEG-013-02-log-tienda.png`](NF-SEG-013-02-log-tienda.png).
