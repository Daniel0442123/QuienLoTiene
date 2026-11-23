# Pruebas de carga y de tiempo real (Plan Maestro §10.3)

| Guion | Caso | Qué hace |
|---|---|---|
| `preparar.mjs 50 cuentas.json` | — | Crea 50 clientes `@prueba.quienlotiene.tech` y guarda sus tokens. `preparar.mjs renovar cuentas.json` vuelve a iniciar sesión (el token dura 15 min) |
| `carga.js` (k6) | NF-REN-001, NF-ESC-020 | `ESCENARIO=rendimiento`: 50 usuarios, p95 ≤ 500 ms. `ESCENARIO=escala`: 100 usuarios, error < 1 % |
| `sse.mjs conexiones 200` | NF-ESC-020 | Abre 50 conexiones a `/api/v1/eventos` y cuenta las que siguen abiertas al final |
| `registrar-tienda.mjs <lat> <lon>` | NF-REN-002 | Registra la tienda que mide la difusión; un administrador la aprueba |
| `sse.mjs difusion 20` | NF-REN-002 | Con `TIENDA_CORREO`, `TIENDA_FRASE`, `TIENDA_LAT`, `TIENDA_LON`: mide de publicar a recibir «solicitud-nueva» |
| `disponibilidad.mjs 420` | NF-DIS-019 | Consulta `/` y `/api` cada segundo y mide el tiempo sin servicio durante un despliegue |

```bash
cd scripts/carga
node preparar.mjs 50 cuentas.json
docker run --rm -i -v "$PWD:/carga" -w /carga -e ESCENARIO=rendimiento grafana/k6:1.4.2 run carga.js
```

Node 22; detrás de un proxy, `NODE_USE_ENV_PROXY=1` (y `HTTPS_PROXY` para k6). `cuentas*.json` y `tienda-carga*.json`
tienen frases y tokens: no se suben (`.gitignore`). Resultados del 22/11/2026: `docs/pruebas/evidencias/C3/2026-11-22/no-funcionales/`.
