# NF-COM-026 · Compatibilidad (en curso, 22/11/2026)

## iPhone 17 Pro Max (Safari y Chrome), 5:03 p. m. a 5:07 p. m.

Capturas de Pedro en producción (`v0.5.4`), como cliente:

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

Pendiente: versión de iOS, push con la app en la pantalla de inicio, Android, Edge y Firefox.
