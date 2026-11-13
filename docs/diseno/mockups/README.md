# Mockups de las historias pendientes

Mockups en alta fidelidad de las historias que todavía no tienen pantalla (Sprint 2 en adelante). Las pantallas del Sprint 1 ya están construidas en `frontend/`.

- Autora: Monica Cajas (UX y frontend). Fecha: 12/11/2026.
- Celular de 390 × 844 px, con la identidad visual del proyecto: paleta «Verde botica» y Atkinson Hyperlegible (ver `frontend/README.md`, sección «Identidad visual»).
- Cada pantalla dice su historia y la regla que la gobierna, tomada de los casos de prueba en `docs/pruebas/libro/02-CASOS.md`.
- Los datos (tiendas, precios, nombres, reseñas, lista de restringidos) son **inventados** para el mockup.
- Son imágenes estáticas: no son un prototipo navegable. El lienzo editable lo tiene Monica.

## Tableros

| Tablero | Historias |
|---|---|
| [01 · Cliente: respuestas y pedido](01-cliente-respuestas-y-pedido.png) | HU-020 respuestas en tiempo real (con el aviso de HU-022), HU-014 detalle de la tienda, HU-024 confirmar pedido |
| [02 · Cliente: seguimiento del pedido](02-cliente-seguimiento-del-pedido.png) | HU-027 estado del pedido, HU-029 cancelar, HU-032 WhatsApp, HU-028 confirmar recepción, HU-025 y HU-026 recoger en la tienda, HU-033 chat |
| [03 · Cliente: después del pedido](03-cliente-despues-del-pedido.png) | HU-034 calificar, HU-035 reportar un problema, HU-008 historial y repetir |
| [04 · Cliente: avisos](04-cliente-avisos.png) | HU-023 producto restringido, HU-006 enlace de verificación vencido |
| [05 · Tienda: solicitudes](05-tienda-solicitudes.png) | HU-018 y HU-030 solicitudes nuevas en tiempo real, HU-019 responder |
| [06 · Tienda: pedidos y configuración](06-tienda-pedidos-y-configuracion.png) | HU-027 pedido de la tienda, HU-015 indicadores, HU-011 tarifas de domicilio, HU-031 avisos push |
| [07 · Administrador](07-administrador.png) | HU-037 reportes y suspender cuentas, HU-038 categorías y productos restringidos |

HU-017 (difundir), HU-021 (vencer solicitudes) y HU-025 en su parte automática (vencer reservas) son procesos del sistema: no tienen pantalla propia y se ven en otras (tiempo para vencer, estado «Vencida», cuenta regresiva de la reserva).

## Decisiones para revisar con el Product Owner y el arquitecto

Estas decisiones son propuestas de diseño; no están en los requisitos:

1. **HU-019:** el precio viene lleno con el último que dio la tienda para ese producto, para cumplir «Sí en 3 toques o menos» (RNF-027). Necesita que el backend guarde ese último precio.
2. **HU-035:** motivos del reporte: demora en la entrega, llegó otro producto, me cobraron diferente y otro problema.
3. **HU-015:** periodo de 7 o 30 días en los indicadores.
4. **HU-023:** texto del aviso: «Este medicamento necesita fórmula médica».
5. **HU-037:** suspender una cuenta pide confirmación antes de hacerlo.
6. **HU-018:** el botón «No lo tengo» descarta en un toque y muestra «Deshacer» (RNF-027).

## Accesibilidad en el diseño

Textos de 16 px o más, zonas para tocar de 44 × 44 px o más, contraste de la paleta ya medido (texto 4,5:1 o más, bordes 3:1 o más) y estados dichos con texto, no solo con color («Urgente», «Abierta», «Algo parecido»). Falta revisarlo de nuevo cuando las pantallas estén construidas (axe, Lighthouse y lector de pantalla).
