# Mockups de las historias pendientes

> **Control del documento** · Versión 0.5 · 22/11/2026 · Responsable: Monica Anyi Cajas Mellizo (UX · frontend) · Estado: En revisión · Revisión: Pedro, 13/11/2026 (PR #33)

Mockups en alta fidelidad de las historias que todavía no tienen pantalla (Sprint 2 en adelante). Las pantallas del Sprint 1 ya están construidas en `frontend/`.

- Celular de 390 × 844 px (y uno de 320 × 640 para el reflujo), con la identidad visual del proyecto: paleta «Verde botica» y Atkinson Hyperlegible (ver `frontend/README.md`, sección «Identidad visual»).
- Cada pantalla dice su historia y la regla que la gobierna, tomada de los casos de prueba en `docs/pruebas/libro/02-CASOS.md`.
- Los datos (tiendas, precios, nombres, teléfonos, reseñas, lista de restringidos) son **inventados**. No hay datos personales reales (Ley 1581).
- Son imágenes estáticas, no un prototipo navegable.

## Fuente

- **`fuente/`** guarda el lienzo editable: un archivo `.dc.html` por tablero y `canvas.json` con su posición. Es la fuente de verdad; los PNG se generan desde ahí.
- Los PNG salieron de los archivos de `fuente/` de su versión, capturados con Edge sin interfaz al tamaño de cada tablero (los 01 a 10 en la 0.2, los 11 a 13 en la 0.3, los 01, 02 y 05 de nuevo en la 0.4 y los 14 a 16 en la 0.5). Los de celular miden 390 × 844 y los de escritorio 1024 × 720.
- Los `.dc.html` necesitan el editor de lienzo para verse con sus marcos; abiertos solos en el navegador muestran el contenido sin estilos de marco.

## Tableros

| Tablero | Historias | Sprint |
|---|---|---|
| [01 · Cliente: respuestas y pedido](01-cliente-respuestas-y-pedido.png) | HU-020 respuestas en tiempo real (con el aviso de HU-022), HU-014 detalle de la tienda, HU-024 confirmar pedido | 2 y 3 |
| [02 · Cliente: seguimiento del pedido](02-cliente-seguimiento-del-pedido.png) | HU-027 estado del pedido, HU-029 cancelar, HU-032 WhatsApp, HU-028 confirmar recepción, HU-025 y HU-026 recoger en la tienda, HU-033 chat | 2 y 3 |
| [03 · Cliente: después del pedido](03-cliente-despues-del-pedido.png) | HU-034 calificar, HU-035 reportar un problema, HU-008 historial y repetir | 3 y cierre |
| [04 · Cliente: avisos](04-cliente-avisos.png) | HU-023 producto restringido al confirmar, HU-006 enlace de verificación vencido | 3 y cierre |
| [05 · Tienda: solicitudes y responder](05-tienda-solicitudes.png) | HU-018 y HU-030 solicitudes nuevas, HU-019 «Sí lo tengo», «Tengo algo parecido» con error y respuesta duplicada | 2 |
| [06 · Tienda: pedidos y configuración](06-tienda-pedidos-y-configuracion.png) | HU-027 pedido nuevo, HU-015 indicadores, HU-011 tarifas de domicilio, HU-031 avisos push | 2, 3 y cierre |
| [07 · Administrador](07-administrador.png) | HU-037 reportes y suspender cuentas, HU-038 categorías y productos restringidos | 3 |
| [08 · Cliente: estados del Sprint 2](08-cliente-estados-sprint-2.png) | HU-020 sin respuestas (estado vacío), HU-021 solicitud vencida | 2 |
| [09 · Tienda: estados del Sprint 2](09-tienda-estados-sprint-2.png) | HU-021 respuesta «No elegida», HU-027 pedido en camino y pedido cancelado | 2 |
| [10 · Tienda: solicitudes a 320 px](10-tienda-solicitudes-320px.png) | HU-018 y HU-019 a 320 px (reflujo, WCAG 1.4.10) | 2 |
| [11 · Cliente: tienda, restringido y cancelar (Sprint 3)](11-cliente-sprint-3-tienda-restringido-y-cancelar.png) | HU-014 detalle con mapa y teléfono, HU-023 aviso al publicar, HU-029 confirmar antes de cancelar y pedido en camino sin «Cancelar» | 3 |
| [12 · Cliente: reserva, chat e historial (Sprint 3)](12-cliente-sprint-3-reserva-chat-e-historial.png) | HU-025 reserva vencida, HU-033 chat en solo lectura, HU-008 «Mis preguntas» (SC-018) e historial vacío | 3 |
| [13 · Administrador (Sprint 3)](13-administrador-sprint-3.png) | HU-037 confirmar antes de suspender, HU-038 quitar con deshacer | 3 |
| [14 · Navegación: menú abierto en el celular](14-navegacion-menu-abierto-en-celular.png) | SC-020: menú con ícono y texto para cliente, tienda y administrador, abierto con «Menú» y con la opción actual marcada | Cierre |
| [15 · Inicio sin sesión y por rol](15-inicio-sin-sesion-y-por-rol.png) | SC-020: inicio sin sesión («Cómo funciona», crear cuenta, entrar y registrar la tienda) e inicio de cliente, tienda y administrador con el menú cerrado | Cierre |
| [16 · Navegación en escritorio](16-navegacion-en-escritorio.png) | SC-020: menú lateral fijo para cada rol desde 1024 px | Cierre |

Los tableros 11 a 13 usan los textos de las pantallas ya construidas (PR #67 a #73). Donde difieren de los tableros 01 a 04 y 07, mandan ellos: HU-014 sin foto ni «Ver las 38 reseñas» (la API no las trae), HU-023 avisa al escribir el producto y no solo al confirmar, y HU-025 se confirma con la tienda (no hay «Ya lo recogí»).

**Navegación (SC-020):** los tableros 01 a 13 muestran el encabezado viejo, con enlaces en fila o el nombre de la sección. Desde la 0.5 manda el menú de los tableros 14 a 16: ícono y texto por opción, según el rol, fijo a la izquierda desde 1024 px y detrás del botón «Menú» en el celular. Los textos e íconos son los de `frontend/src/app/navegacion.ts` y `features/inicio/textos.ts`.

HU-017 (difundir) es un proceso del sistema sin pantalla propia. Los efectos visibles de HU-021 (vencer solicitudes) y HU-022 (ampliar el radio) están en los tableros 01, 08 y 09.

## Decisiones

### Ya aprobadas en DECISIONES-FRONTEND §8

- **HU-019:** «Sí lo tengo» abre precio y unidades ya precargados (Sí en 3 toques o menos).
- **HU-018:** «No lo tengo» en 1 toque, con deshacer durante unos segundos en vez de un diálogo de confirmación. El aviso dura al menos 5 s y se puede cerrar (regla 7).

Lo único nuevo es de dónde sale el precio precargado: el mockup propone el último precio que dio la tienda para ese producto. Eso toca el modelo de datos y la API, así que queda como **propuesta** hasta el PR del backend; si se adopta, lleva una solicitud de cambio menor (decide Daniel).

### Para que apruebe el Product Owner (Wilson)

Afinan la interfaz sin cambiar los criterios de aceptación:

1. **HU-035:** motivos del reporte: demora en la entrega, llegó otro producto, me cobraron diferente y otro problema.
2. **HU-015:** periodo de 7 o 30 días y los porcentajes «Respondiste» y «Te eligieron». RF-015 solo pide los tres conteos; si se quedan los porcentajes, hay que fijar la fórmula. El periodo agrega un filtro al endpoint (confirma Daniel).
3. **HU-023:** texto del aviso: «Este medicamento necesita fórmula médica».
4. **HU-037:** suspender una cuenta pide confirmación.
5. **HU-018 y HU-019:** un solo nombre para la alternativa: **«Tengo algo parecido»**, como en CP-SOL-019-02. DECISIONES-FRONTEND §8 (regla 3) dice «Parecido»; si se aprueba, se ajusta ese texto.
6. **HU-018:** a 320 px los botones van en dos filas: «Sí lo tengo» y «No lo tengo» arriba, «Tengo algo parecido» abajo a lo ancho. Se usa igual a 390 px.
7. **HU-021:** la tienda ve sus respuestas en una pestaña «Mis respuestas» con su estado (Esperando, Elegida, No elegida, Vencida).
8. **HU-027 y HU-029:** cuando el cliente cancela, la tienda deja de ver sus datos.

## Revisión del 13/11/2026: qué quedó (versión 0.3, C-15)

Hecho en los tableros 11 a 13:

- HU-014 con mapa y teléfono; HU-023 en la pantalla de publicar; HU-025 reserva vencida; HU-029 pedido en camino sin «Cancelar»; HU-033 chat en solo lectura; HU-008 historial vacío.
- Confirmación de HU-029 (cancelar con motivo) y de HU-037 (suspender con motivo), en el mismo lugar y sin diálogo, y deshacer en HU-038.
- Anotaciones de accesibilidad: nombre accesible de los botones repetidos («Repetir Paracetamol 500 mg»), qué anuncia cada pantalla nueva (aviso de HU-023 asociado al campo, «quedan 5 minutos» en HU-025, `role="log"` en HU-033, «Quitaste…» en HU-038), remitente y hora en cada mensaje del chat, y a dónde va el foco al confirmar.

Hecho en la versión 0.4 (C-20):

- Qué anuncia el lector de pantalla, sin mover el foco, en los tableros 01, 02 y 05 (texto en verde bajo el título): HU-020 «Llegó una respuesta de Droguería La 5.», HU-027 «Tu pedido ahora está: En camino.» y HU-018 «Solicitud urgente: «Salbutamol inhalador». Está primero en tu lista.». Son los mismos textos de la app.
- Nota «Accesibilidad en todas las pantallas» en el lienzo, junto a los tableros del Sprint 3:
  - **3.2.6 Ayuda en el mismo lugar:** la app todavía no tiene un mecanismo de ayuda. Si se agrega (contacto o soporte), va en el mismo sitio del pie en todas las pantallas.
  - **2.4.11 Foco no tapado:** ya está en la app. `html { scroll-padding-bottom: var(--alto-barra-inferior) }` en `frontend/src/styles/index.css` reserva el alto de la barra inferior fija, así un campo enfocado no queda debajo de ella.

Sigue pendiente:

- Columna «Mockup» en `docs/pruebas/libro/07-TRAZABILIDAD.md` para los casos manuales de interfaz, cuando se aprueben estos mockups.

## Accesibilidad en el diseño

Textos de 16 px o más, zonas para tocar de 44 × 44 px o más con 8 px entre ellas, contraste de la paleta ya medido (texto 4,5:1 o más, bordes 3:1 o más) y estados dichos con texto, no solo con color («Urgente», «Abierta», «No elegida», «Cancelado»). Los formularios de HU-019 muestran el error como dice DECISIONES-FRONTEND §9: resumen arriba con enlace al campo y mensaje debajo del campo. La verificación real (axe, Lighthouse, NVDA y TalkBack) se hace cuando las pantallas estén construidas.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 12/11/2026 | Monica Anyi Cajas Mellizo (@monicacajas-art) | Primera versión: 7 tableros con 21 pantallas de 27 historias (PR #33) |
| 0.2 | 16/11/2026 | Monica Anyi Cajas Mellizo (@monicacajas-art) | Cambios de la revisión del 13/11/2026: tableros 08 a 10 (estados del Sprint 2 y 320 px), HU-019 con «Tengo algo parecido» y respuesta duplicada, HU-020 vacío, HU-021 vencida y no elegida, HU-027 en camino y cancelado, textos de HU-006 y desglose de HU-023, aviso con cierre en HU-018, lienzo fuente en `fuente/`, encabezado de control y decisiones separadas en aprobadas y por aprobar (PR #33) |
| 0.3 | 21/11/2026 | Monica Anyi Cajas Mellizo (@monicacajas-art) | Tableros 11 a 13 del Sprint 3 (C-15): HU-014 con mapa, HU-023 al publicar, HU-025 vencida, HU-029 confirmar y en camino, HU-033 solo lectura, HU-008 «Mis preguntas» y vacío, confirmar en HU-037 y deshacer en HU-038, con anotaciones de accesibilidad |
| 0.4 | 21/11/2026 | Monica Anyi Cajas Mellizo (@monicacajas-art) | C-20: qué anuncia el lector en HU-018, HU-020 y HU-027 (tableros 01, 02 y 05) y nota de ayuda en el mismo lugar (3.2.6) y foco no tapado (2.4.11) (PR #77) |
| 0.5 | 22/11/2026 | Monica Anyi Cajas Mellizo (@monicacajas-art) | C-21: tableros 14 a 16 con el menú lateral por rol (celular y escritorio) y el inicio sin sesión y por rol de SC-020; los tableros 01 a 13 quedan con el encabezado viejo y mandan los nuevos (PR #81) |
