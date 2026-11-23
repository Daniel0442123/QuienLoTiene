# Re-pruebas sobre `v0.5.5` (22/11/2026, noche)

`v0.5.5` (`2d6a71a`, #92) en producción desde las 10:51 p. m. (hora de Bogotá). Corrige DEF-016, DEF-017 y DEF-018.

| Archivo | Caso | Qué muestra |
|---|---|---|
| `DEF-017-botones-mapa-44px.png` | NF-USA-028 (DEF-017) | `https://quienlotiene.tech/registrar-tienda` en Chromium con tamaño de iPhone 13: los botones + y − del mapa miden 44 × 44 px (10:52 p. m.). Los mosaicos del mapa no cargan porque el entorno de pruebas no llega a OpenStreetMap |
| `DEF-016-solicitud-vencida-1137.png` | E2E-05 (DEF-016) | Celular de Pedro (Chrome en Android), cliente con la solicitud «Prueba DEF-16» publicada a las 11:06 p. m. cerca de Tienda Uno, sin respuestas. A las 11:37 p. m. (vencía a las 11:36) la pantalla dice «Tu solicitud venció», «Vencida» y «Ninguna tienda la tenía disponible.», sin «Te avisamos en esta pantalla…». La captura es del minuto siguiente al vencimiento, cuando la API ya pudo haberla marcado; que la lista cambie justo a la hora la prueban `SolicitudVencidaPorReloj.test.tsx` (fallan sin la corrección) |
| `NF-ESC-020-k6-corrida5.txt` y `NF-ESC-020-sse-corrida5.txt` | NF-ESC-020 (DEF-018) | Corrida 5 (03:53Z): 100 usuarios durante 4 min, 0 de 94.031 peticiones con error, p95 de la API 1,5 s (la API llevaba 2 minutos de reiniciada); 50 de 50 conexiones SSE abiertas al final |
| `NF-ESC-020-k6-corrida6.txt` y `NF-ESC-020-sse-corrida6.txt` | NF-ESC-020 (DEF-018) | Corrida 6 (03:58Z): 0 de 141.428 peticiones con error, p95 de la API 296 ms; 50 de 50 conexiones SSE |

La corrida 4 (sobre `v0.5.4` con Caddy en 256 MiB aplicado a mano, con `docker stats` en el VPS) está en `../no-funcionales/`. Guiones: `scripts/carga/` (`carga.js` con `ESCENARIO=escala` y `sse.mjs conexiones 260`).
