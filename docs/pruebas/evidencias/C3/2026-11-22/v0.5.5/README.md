# Re-pruebas sobre `v0.5.5` (22/11/2026, noche)

`v0.5.5` (`2d6a71a`, #92) en producción desde las 10:51 p. m. (hora de Bogotá). Corrige DEF-016, DEF-017 y DEF-018.

| Archivo | Caso | Qué muestra |
|---|---|---|
| `DEF-017-botones-mapa-44px.png` | NF-USA-028 (DEF-017) | `https://quienlotiene.tech/registrar-tienda` en Chromium con tamaño de iPhone 13: los botones + y − del mapa miden 44 × 44 px (10:52 p. m.). Los mosaicos del mapa no cargan porque el entorno de pruebas no llega a OpenStreetMap |
| `NF-ESC-020-k6-corrida5.txt` y `NF-ESC-020-sse-corrida5.txt` | NF-ESC-020 (DEF-018) | Corrida 5 (03:53Z): 100 usuarios durante 4 min, 0 de 94.031 peticiones con error, p95 de la API 1,5 s (la API llevaba 2 minutos de reiniciada); 50 de 50 conexiones SSE abiertas al final |
| `NF-ESC-020-k6-corrida6.txt` y `NF-ESC-020-sse-corrida6.txt` | NF-ESC-020 (DEF-018) | Corrida 6 (03:58Z): 0 de 141.428 peticiones con error, p95 de la API 296 ms; 50 de 50 conexiones SSE |

La corrida 4 (sobre `v0.5.4` con Caddy en 256 MiB aplicado a mano, con `docker stats` en el VPS) está en `../no-funcionales/`. Guiones: `scripts/carga/` (`carga.js` con `ESCENARIO=escala` y `sse.mjs conexiones 260`).
