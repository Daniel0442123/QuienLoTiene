# Pruebas de punta a punta (E2E-01 a E2E-05)

Escenarios del libro de control (`docs/pruebas/libro/02-CASOS.md`) con Playwright. Cada actor (cliente, tienda,
administrador) usa su propio contexto del navegador, como si fueran celulares distintos, y todo se hace por la interfaz.

| Archivo | Escenario |
|---|---|
| `e2e-01-publicar-y-recibir-respuesta.spec.ts` | El cliente publica y la respuesta de la tienda le aparece sin recargar |
| `e2e-02-elegir-y-entregar-a-domicilio.spec.ts` | Elegir con domicilio, dirección y efectivo; la tienda lleva el pedido a Entregado y el cliente confirma |
| `e2e-03-aprobar-tienda-con-segundo-factor.spec.ts` | Registro de tienda con documentos, aprobación del administrador con TOTP y primera solicitud |
| `e2e-04-calificar-despues-de-la-entrega.spec.ts` | Calificar con 4 estrellas y ver la reseña en el detalle de la tienda |
| `e2e-05-ampliar-radio-y-vencer.spec.ts` | Sin respuestas: el radio pasa de 3 a 5 km a los 10 min y la solicitud vence a los 30 min (tarda unos 32 min) |

## Cómo correrlas

Contra la pila local (lo que se hizo el 22/11/2026 con la `v0.5.4`):

1. Base: `docker run -d --name qlt-e2e-db -e POSTGRES_DB=quienlotiene -e POSTGRES_USER=quienlotiene -e POSTGRES_PASSWORD=<clave> -p 127.0.0.1:5432:5432 postgis/postgis:17-3.5-alpine`
2. API: `./mvnw -B -DskipTests package` y `java -jar target/QuienLoTiene-0.0.1-SNAPSHOT.jar` con `SPRING_DOCKER_COMPOSE_ENABLED=false`,
   `QLT_DB_CLAVE`, `QLT_TOTP_LLAVE_CIFRADO`, y un administrador de prueba (`QLT_ADMIN_CORREO`, `QLT_ADMIN_FRASE`,
   `QLT_ADMIN_SECRETO_TOTP`; ver `.env.ejemplo`).
3. PWA: `cd frontend && npm run build && npx vite preview --port 4173` (el proxy de Vite manda `/api` a la API).
4. Pruebas:

```bash
cd e2e
npm ci
E2E_ADMIN_CORREO=... E2E_ADMIN_FRASE=... E2E_ADMIN_SECRETO_TOTP=... npx playwright test
```

`E2E_URL` cambia el sitio (por defecto `http://localhost:4173`). Los correos que se crean son del dominio
`prueba.quienlotiene.tech`, así que `limpiar.sh pruebas` los borra si alguna vez se corren contra el VPS. Para probar
E2E-05 rápido, la API se puede arrancar con `QUIENLOTIENE_SOLICITUDES_ESPERAPARAAMPLIAR=1m` y
`QUIENLOTIENE_SOLICITUDES_VIGENCIA=3m`, y las pruebas con `E2E_ESPERA_AMPLIAR_MIN=1 E2E_VIGENCIA_MIN=3`; la corrida
oficial usa los 10 y 30 minutos aprobados.

El reporte queda en `reporte/` (HTML y `resultados.json`).
