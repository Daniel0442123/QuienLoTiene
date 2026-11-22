# Comprobación en producción de la v0.5.4 (22/11/2026)

## HU-015 · «Tu resumen» de la tienda

- 4:14 p. m.: Pedro entró como «tienda 1» en el navegador integrado; `/tienda/resumen` carga.
- Muestra: 7 recibidas, 5 respondidas, 4 ganadas; «Respondiste 5 de 7 (71 %)» y «Te eligieron 4 de 5 (80 %)».
- `GET /api/v1/tiendas/me/indicadores?periodo=7` → 200 y `?periodo=30` → 200 (registro de red del navegador). Los dos
  periodos dan lo mismo porque toda la actividad de la tienda es de esta semana.
- Observación: el navegador tenía activa la versión anterior de la PWA hasta activar el service worker nuevo (la PWA usa
  `registerType: 'prompt'`). Un usuario la recibe al aceptar el aviso de actualización o al cerrar y abrir la app.

## HU-022 · Ampliación del radio

- 4:16:35 p. m. (21:16:35Z): un cliente de prueba (`@prueba.quienlotiene.tech`) publica «Prueba de radio ampliado
  v0.5.4» (Farmacia) en 2.336, -76.7, lejos de las tiendas: `radioEnMetros` 3000, 0 tiendas notificadas.
- 4:23:54 p. m.: sigue en 3000 (7 min).
- 4:28:31 p. m. (11,9 min): `radioEnMetros` 5000, estado ABIERTA. La ampliación corre cada minuto a partir de los 10.
- 0 tiendas notificadas también después: la única farmacia a menos de 5 km era la tienda de prueba de carga, que se aprobó
  a las 4:27 p. m. y todavía estaba cerrada.
