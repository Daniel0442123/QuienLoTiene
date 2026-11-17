# Re-prueba de DEF-005 a DEF-009 · 16/11/2026, 9:45 p. m. (hora de Colombia)

Versión: `main` 55ab5b6 (con #46, #47 y #48). Mismo ambiente del recorrido (`../scripts/README.md`): base PostGIS nueva en un contenedor, `java -jar` con el perfil dev, `vite` y los datos de `../scripts/setup.py` (3 droguerías y 2 clientes). Borrador preparado para que QA lo revise y firme.

| Defecto | Cómo se re-probó | Resultado | Evidencia |
|---|---|---|---|
| DEF-005 | Ana abre «Publicar solicitud» (el producto tiene el foco) y toca «Preguntar» una vez con todo vacío | Pasa: marca producto y categoría, con el resumen «Revisa estos datos antes de preguntar:» y un enlace a cada error | `capturas/01` |
| DEF-006 | `GET /v3/api-docs` en vivo; `npm run api:tipos` y `git diff`; `npm run tipos` | Pasa: 38 operaciones, todas con 2xx; 0 `*/*`; los 4xx y 5xx como `application/problem+json`; enums con nombre; `historial[].estado` con `EstadoOrden`; 7 eventos SSE; `DatosDireccion.id` obligatorio; `mediosPagoAceptados` con `MedioPago`. `api-tipos.ts` no cambia y `tsc` da 0 errores | `api-docs-main-55ab5b6.json` |
| DEF-007 | Ana elige la respuesta de Droguería La 5 (efectivo y transferencia) y luego la de Botica San José (efectivo y datáfono) | Pasa: «¿Cómo pagas?» ofrece `EFECTIVO, TRANSFERENCIA` y `EFECTIVO, DATAFONO`. El pedido con Botica en efectivo se crea | `capturas/02`, `capturas/03` |
| DEF-009 | Botica San José abre el pedido; Ana abre el suyo | Pasa: la tienda lee «El cliente paga con efectivo.» y el cliente «Pagas con efectivo a la tienda.» | `capturas/04`, `capturas/05` |
| DEF-008 | Luis publica y deja la pantalla abierta; se adelanta `vence_en` en la base y la tarea de vencimiento (cada minuto) la cierra | Pasa: el título cambia solo a «Tu solicitud venció» y dice que el plazo terminó, sin «pueden responder». En la base queda `VENCIDA` | `capturas/06`, `capturas/07` |

Hallazgo nuevo: en `capturas/07` el texto termina en «10:17 p. m..» (punto doble). Se registra como DEF-010 (S4).

Salida completa de la corrida: `reprueba-salida.json`. Script: `scripts/reprueba.js` (Playwright, 390 × 844, escala 2).
