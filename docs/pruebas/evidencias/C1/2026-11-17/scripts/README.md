# Cómo se levantó el ambiente del recorrido del 17/11/2026

Ambiente de QA: contenedor Linux, Java 21, Node 22, Docker 29.9.2, Chromium con Playwright.

1. Base de datos: `docker run -d --name qa-db -e POSTGRES_DB=quienlotiene -e POSTGRES_USER=quienlotiene -e POSTGRES_PASSWORD=<clave local> -p 127.0.0.1:5433:5432 postgis/postgis:17-3.5-alpine`
2. Backend: `./mvnw -B clean verify` y luego `java -jar target/QuienLoTiene-0.0.1-SNAPSHOT.jar --spring.docker.compose.enabled=false` con `SPRING_PROFILES_ACTIVE=dev`, `QLT_DB_URL=jdbc:postgresql://localhost:5433/quienlotiene`, `QLT_DB_CLAVE`, `QLT_TOTP_LLAVE_CIFRADO` y el administrador (`QLT_ADMIN_*` con un secreto TOTP generado para la prueba).
3. Frontend: `cd frontend && npm ci && npx vite --host 127.0.0.1` (proxy de `/api` al 8080).
4. Datos: `python3 setup.py` (admin con segundo factor, 3 droguerías aprobadas y abiertas en Popayán, clientes Ana y Luis con su dirección).
5. Recorrido: `node flujo.js` y `node flujo2.js` (capturas a 390 × 844, escala 2). Pruebas por API: `python3 negativos.py`.

Las frases de contraseña de los scripts son de datos de prueba locales; no existen en ningún ambiente real.
