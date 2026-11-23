# NF-COM-026 · Compatibilidad (aprobado, 22/11/2026)

## iPhone 17 Pro Max con iOS 27.2.1 (Safari y Chrome), 5:03 p. m. a 5:25 p. m.

Capturas de Daniel Montenegro en producción (`v0.5.4`), como cliente:

| Captura | Navegador | Qué se ve |
|---|---|---|
| `iphone-17-pro-max/1-safari-preguntar.jpg` | Safari | Formulario «¿Qué estás buscando?» con categoría, ubicación y mapa; botón «Preguntar» fijo abajo |
| `iphone-17-pro-max/2-safari-sin-tiendas-abiertas.png` | Safari | «Esperando respuestas» sin tiendas abiertas cerca (domingo de noche): comportamiento esperado |
| `iphone-17-pro-max/3-chrome-inicio.png` | Chrome | Página de inicio sin sesión |
| `iphone-17-pro-max/4-chrome-preguntar-mapa.png` | Chrome | Formulario con el mapa y el punto elegido |
| `iphone-17-pro-max/5-chrome-sin-tiendas-abiertas.png` | Chrome | Igual que la captura 2 |

| `iphone-17-pro-max/6-safari-respuesta-en-vivo.jpg` | Safari | 5:16 p. m.: la respuesta de Tienda Uno ($2.500) llega sin recargar; «Buscamos a 5 km» (el radio se amplió a los 10 min) |
| `iphone-17-pro-max/7-safari-confirmar-pedido.png` | Safari | Confirmar pedido: recoger en la tienda, medios de pago y total |
| `iphone-17-pro-max/8-safari-pedido-confirmado.png` | Safari | Pedido 1A539F1D confirmado con «Cómo llegar», WhatsApp y chat |
| `iphone-17-pro-max/9-safari-listo-para-recoger.png` | Safari | Preparando y Listo para recoger marcados por la tienda sin recargar; reserva hasta las 5:48 p. m. |
| `iphone-17-pro-max/10-safari-como-llegar-google-maps.png` | Safari | «Cómo llegar» abre Google Maps con la ruta hasta la tienda (5,1 km) |
| `iphone-17-pro-max/11-chrome-cancelar-sin-motivo.png` | Chrome | Pedido confirmado; cancelar sin motivo muestra «Escribe por qué cancelas.» |
| `iphone-17-pro-max/12-chrome-cancelar-con-motivo.png` | Chrome | Motivo «Ya no lo necesito» (18 de 200 caracteres) |
| `iphone-17-pro-max/13-chrome-pedido-cancelado.png` | Chrome | 5:24 p. m.: «Cancelado por el cliente» con el motivo |

La respuesta a la pregunta de Chrome salió por $25.002.800: el formulario de la tienda trae el último precio que dio
(SC-011) y al escribir 2800 sin borrar, los dígitos se agregaron al final. El formulario muestra «El cliente verá: $ …»
antes de enviar. Se anota como observación de usabilidad, no como defecto.

Resultado en iPhone 17 Pro Max: Safari y Chrome cumplen el flujo del cliente (publicar, respuesta en vivo, pedido,
estados en vivo, «Cómo llegar» y cancelación). La tienda se manejó desde el navegador integrado de la app de escritorio
(Chromium).

iOS 27.2.1 (Configuración › General › Información, 22/11 5:37 p. m.).

## Push en iPhone con la app instalada (HU-031), 5:57 p. m. a 5:58 p. m.

| Captura | Qué se ve |
|---|---|
| `iphone-17-pro-max/14-app-instalada-avisos-activados.png` | App agregada a la pantalla de inicio, sesión de Tienda Uno: «Los avisos están activados en este dispositivo.» |
| `iphone-17-pro-max/15-push-con-app-cerrada.png` | Con la app cerrada llega «Solicitud nueva · Un vecino pregunta si tienes un producto. Toca para responder.», con sonido |
| `iphone-17-pro-max/16-tienda-panel-abierta.png` | 8:15 p. m., app instalada como Tienda Uno: aprobada, Farmacia, abierta, avisos con la app cerrada |
| `iphone-17-pro-max/17-tienda-solicitudes-nuevas.png` | Bandeja «Nuevas»: 9 esperando respuesta, dos «Acetaminofén - prueba_navegador_Chrome» urgentes a 3,8 km |
| `iphone-17-pro-max/18-tienda-responder-precio-sugerido.png` | Responder «Sí lo tengo» con el precio sugerido de $ 2.000 («Es el último precio que diste para este producto») |
| `iphone-17-pro-max/19-tienda-mis-respuestas.png` | 8:16 p. m., «Mis respuestas»: las dos respuestas de $ 2.000 esperando a que el cliente elija |

La solicitud «Prueba de aviso push iPhone» se publicó a las 5:58 p. m. con una cuenta de cliente de la prueba de carga
(`scripts/carga/publicar-una.mjs`) junto a Tienda Uno: 1 tienda notificada. El aviso no lleva el producto ni datos del
cliente, como pide HU-031. iOS agrega «from Quién lo tiene» en inglés: es texto del sistema, no de la app.

## Android (Chrome y Firefox), 5:05 p. m. a 5:14 p. m.

| Captura | Navegador | Qué se ve |
|---|---|---|
| `android/1-chrome-preguntar.png` | Chrome | Formulario con Ferretería, mapa y «Preguntar» fijo abajo |
| `android/2-chrome-sin-tiendas-abiertas.png` | Chrome | Publicada «Tornillo - prueba_navegador_chrome»: sin ferreterías abiertas cerca (domingo) |
| `android/3-firefox-preguntar.png` | Firefox | Formulario con el punto marcado en el mapa |
| `android/4-firefox-sin-tiendas-abiertas.png` | Firefox | Publicada «Tornillos - prueba_navegador_Firefox»: mismo mensaje |
| `android/5-chrome-farmacia-centro.png` | Chrome | Farmacia con el pin cerca del centro (7:49 p. m.) |
| `android/6-chrome-farmacia-sin-tiendas.png` | Chrome | Esa solicitud no encontró tiendas: el pin quedó fuera de los 3 km de Tienda Uno |
| `android/7-chrome-esperando-1-tienda.png` | Chrome | 8:01 p. m., «Prueba solicitud 1» con el pin bien puesto: se preguntó a 1 tienda, radio 3 km, cierre 8:27 p. m. |
| `android/8-chrome-respuesta-en-vivo-830.png` | Chrome | 8:23 p. m., «Acetaminofén - prueba_navegador_Chrome»: radio ampliado a 5 km y la respuesta de Tienda Uno ($ 2.000, a 3,8 km, botón «Elegir») llegó sin recargar |
| `android/9-chrome-respuesta-en-vivo-827.png` | Chrome | La segunda solicitud igual (cierre 8:27 p. m.), misma respuesta en vivo |
| `android/10-firefox-esperando-1-tienda.jpg` | Firefox | 8:30 p. m., «Acetaminofen Prueba Firefox»: se preguntó a 1 tienda, radio 3 km, cierre 9:00 p. m. |
| `android/11-firefox-respuesta-en-vivo.png` | Firefox | 8:31 p. m., la respuesta de Tienda Uno ($ 2.500, a 147 m, «Elegir») apareció sin recargar |

En Android se probó publicar y la pantalla de espera. A las 7:51 p. m. una solicitud publicada por guion desde el punto exacto de Tienda Uno sí la notificó, lo que confirmó que la tienda estaba abierta y que el fallo de las 7:49 p. m. fue la ubicación del pin, no Android. Con el pin bien puesto, a las 8:01 p. m. la solicitud llegó a Tienda Uno.

Teléfono Android: Samsung Galaxy A57 5G (SM-A576B), Android 17, One UI 9.0.

A las 8:16 p. m. Tienda Uno respondió desde la app del iPhone y la respuesta apareció en vivo en Chrome de Android, después de la ampliación del radio a 5 km (HU-022).

A las 8:31 p. m. Tienda Uno respondió desde el navegador de escritorio y la respuesta apareció en vivo en Firefox de Android.

## Edge en un computador (Windows), 8:34 p. m.

| Captura | Qué se ve |
|---|---|
| `edge/1-edge-esperando-1-tienda.png` | «Acetaminofén Prueba Edge PC» publicada: se preguntó a 1 tienda, radio 3 km, cierre 9:03 p. m. |
| `edge/2-edge-respuesta-en-vivo.png` | Tienda Uno respondió a las 8:35 p. m. con $ 3.000 y la respuesta (a 2,8 km, «Elegir») apareció sin recargar |

## Resultado

Aprobado. En los 5 navegadores (Safari y Chrome en iOS 27.2.1, Chrome y Firefox en Android 17, Edge en Windows) se publicó una solicitud y la respuesta de la tienda llegó en vivo. Límite: se probó la versión actual de cada navegador, no la anterior.
