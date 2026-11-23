# Punta a punta con Playwright (E2E-01 a E2E-05) · 22/11/2026

- **Versión:** pila local construida desde la etiqueta `v0.5.4` (`f22f20c`): PostGIS 17, la API (`java -jar`, perfil dev) y la
  PWA con `vite preview`. Administrador de prueba con su propio secreto TOTP; los correos son `@prueba.quienlotiene.tech`.
- **Por qué no en el VPS:** E2E-03 inicia sesión como administrador con el segundo factor, y el de producción es personal;
  así tampoco quedan pedidos ni calificaciones de prueba en producción. El catálogo (`libro/02-CASOS.md` §3) pedía el VPS:
  queda como desviación para que QA la acepte.
- **Guiones:** [`e2e/`](../../../../../../e2e/README.md). Chromium 1 trabajador, geolocalización de Popayán, cada actor
  (cliente, tienda, administrador) en su propio contexto del navegador y todo por la interfaz.
- **Tiempos:** los aprobados por el plan, 10 minutos para ampliar el radio y 30 para vencer.

## Corrida oficial (21:33Z a 22:05Z, 4:33 p. m. a 5:05 p. m.)

[`salida.txt`](salida.txt) · reporte HTML [`reporte/index.html`](reporte/index.html) · [`reporte/resultados.json`](reporte/resultados.json)

| Escenario | Resultado | Duración | Qué se comprobó |
|---|---|---|---|
| E2E-01 | Aprobado | 2,6 s | El cliente publica; la tienda responde «Sí lo tengo» con precio; la respuesta y el total ($8.000) aparecen en la pantalla del cliente sin recargar |
| E2E-02 | Aprobado | 4,0 s | El cliente elige con domicilio, dirección guardada y efectivo ($11.000 con el domicilio); la tienda pasa el pedido por Preparando, En camino y Entregado; el cliente toca «Recibí mi pedido» |
| E2E-03 | Aprobado | 2,9 s | Una tienda se registra con foto y RUT; el administrador inicia sesión con frase y código TOTP y la aprueba; la tienda abre y recibe la primera solicitud |
| E2E-04 | Aprobado | 4,1 s | Después de recibir el pedido el cliente califica con 4 estrellas y comentario, y la reseña se ve en el detalle de la tienda |
| E2E-05 | Aprobado | 31,0 min | Con una tienda cercana que no responde, la pantalla pasa de «3 km» a «5 km» sin recargar a los 11,0 min, la tienda a 4 km recibe la solicitud, el título cambia a «Tu solicitud venció» a los 30,0 min y la lista dice «Ninguna tienda la tenía disponible.» a los 31,0 min |

La ampliación corre cada minuto a partir de los 10 minutos, por eso llega entre el minuto 10 y el 11.

## Primera corrida y DEF-016 (21:02Z a 21:33Z)

[`primera-corrida/salida.txt`](primera-corrida/salida.txt) · [`primera-corrida/reporte/index.html`](primera-corrida/reporte/index.html)

E2E-01 a E2E-04 pasaron. E2E-05 amplió el radio y venció a tiempo, pero falló en la última comprobación: con el título
«Tu solicitud venció», la lista seguía diciendo «Todavía no hay respuestas · Te avisamos en esta pantalla apenas una tienda
conteste.» (ver el árbol de la página en
[`primera-corrida/reporte/data/59c891eb67ec9079c78660c30e5e29aa78acd81f.md`](primera-corrida/reporte/data/59c891eb67ec9079c78660c30e5e29aa78acd81f.md),
líneas 53 a 61). El título cambia con el reloj del navegador y la lista espera a que la tarea programada marque la
solicitud VENCIDA, hasta un minuto después. Se registró como **DEF-016** (S4) y la prueba ahora espera hasta 2 minutos
por el mensaje final y anota cuándo llega (31,0 min en la corrida oficial).

Antes de la primera corrida, una corrida falló por el límite de 5 inicios de sesión por minuto por cuenta (NF-SEG-009):
las pruebas iniciaban sesión como administrador en cada escenario. Ahora comparten una sesión de administrador.
