# Despliegue en producción

Cómo está montado el VPS, cómo se publica una versión, cómo se vuelve atrás y cómo se restauran los respaldos. Complementa el diagrama `docs/diagramas/08-despliegue.drawio`, `DECISIONES-BACKEND.md` (secciones 11 a 14) y `DECISIONES-FRONTEND.md` (F-05 y sección 13).

## 1. Arquitectura

```
Internet ──443/80──► caddy ──8080 (red interna)──► api ──5432 (red interna)──► db
                     │ TLS de Let's Encrypt        │ Spring Boot, perfil prod   │ PostgreSQL 17 + PostGIS 3.5
                     │ PWA en /, API en /api/*     │ volumen uploads            │ volumen pgdata
                     │ volumen caddy_data          │
```

| Pieza | Dónde |
|---|---|
| VPS | Hostinger KVM 1, Debian 13, 4 GB de RAM + 2 GB de swap, `179.66.132.95` |
| Dominio | `quienlotiene.tech` (registros A y AAAA al VPS, `www` como CNAME que Caddy redirige) |
| Carpeta de la app | `/opt/quienlotiene` (`.env`, `compose.prod.yaml`, `respaldo/`) |
| Imágenes | `ghcr.io/daniel0442123/quienlotiene-api` y `quienlotiene-caddy`, etiquetadas con la versión (`v0.1.0`) y `latest` |
| Contenedores | `docker/api/Dockerfile`, `docker/caddy/Dockerfile` + `Caddyfile`, `compose.prod.yaml` |
| Despliegue | `.github/workflows/despliegue.yml`, al crear una etiqueta `v*` |
| Respaldos | `scripts/respaldo/`, restic cifrado a Backblaze B2, todos los días a las 3:00 a. m. (hora de Colombia) |

Solo Caddy publica puertos. La base y la API no tienen `ports:` en `compose.prod.yaml` porque Docker se salta `ufw`: un puerto publicado queda abierto aunque el cortafuegos diga lo contrario. El puerto 80 solo redirige a HTTPS y atiende el reto del certificado; nunca entrega contenido. HSTS, la cookie `Secure` del refresco y las rutas relativas de la PWA garantizan que nada sensible viaje por HTTP.

## 2. Preparación del servidor (hecha el 11/11/2026)

Queda documentada para poder repetirla en otro VPS.

1. **Usuario y SSH**: usuario `despliegue` con `sudo` y llave ed25519. `/etc/ssh/sshd_config.d/10-endurecido.conf` con `PermitRootLogin no`, `PasswordAuthentication no`, `KbdInteractiveAuthentication no` y `AllowUsers despliegue`.
2. **Sistema**: zona horaria UTC, nombre `quienlotiene` (también en `/etc/hosts`), `unattended-upgrades` activo, fail2ban con la jaula `sshd` (`backend = systemd`, 5 intentos, 1 hora) y swap de 2 GB con `vm.swappiness=10`.
3. **Cortafuegos doble**: `ufw` y el firewall del panel de Hostinger, los dos con solo TCP 22, 80 y 443 y UDP 443 (HTTP/3).
4. **Docker** desde el repositorio oficial, con `/etc/docker/daemon.json` = `{ "log-driver": "local", "log-opts": { "max-size": "10m", "max-file": "5" }, "live-restore": true }`. El usuario `despliegue` está en el grupo `docker`.
5. **Secretos** en `/opt/quienlotiene/.env` (`chmod 600`), generados en el servidor: clave de la base, par de llaves JWT, llave AES del TOTP, secreto TOTP del administrador y datos del administrador inicial. Ver `.env.ejemplo`. En el VPS: `SPRING_PROFILES_ACTIVE=prod`, `QLT_DB_URL=jdbc:postgresql://db:5432/quienlotiene`, `QLT_DOMINIO=quienlotiene.tech`, `QLT_CORS_ORIGENES=https://quienlotiene.tech` y `QLT_ARCHIVOS_DIRECTORIO=/datos/archivos`. Hay una copia en el gestor de contraseñas del equipo.
6. **Llave de despliegue**: un par ed25519 solo para GitHub Actions, con `no-port-forwarding,no-agent-forwarding,no-X11-forwarding` en `authorized_keys`. La privada solo está en los secretos de GitHub.

Secretos del repositorio (Settings → Secrets and variables → Actions):

| Secreto | Valor |
|---|---|
| `VPS_HOST` | IP del VPS |
| `VPS_USUARIO` | `despliegue` |
| `VPS_LLAVE_SSH` | llave privada de despliegue |
| `VPS_HUELLA` | salida de `ssh-keyscan -t ed25519 <IP>` (el workflow rechaza un servidor con otra huella) |

## 3. Publicar una versión

```bash
git checkout main && git pull
git tag v0.1.0
git push origin v0.1.0
```

El workflow `Despliegue`:

1. Corre `./mvnw verify` sobre la etiqueta.
2. Construye las dos imágenes, las escanea con Trivy (falla con vulnerabilidades críticas o altas que tengan arreglo) y las publica en GHCR.
3. Copia `compose.prod.yaml` y `scripts/respaldo/` al VPS, escribe `QLT_VERSION=<etiqueta>` en el `.env`, descarga las imágenes con un token que solo vive durante el job y ejecuta `docker compose up -d --wait`.
4. Comprueba que `https://quienlotiene.tech/` responde.

En el primer despliegue Caddy pide el certificado de Let's Encrypt; tarda unos segundos.

## 4. Volver a una versión anterior

Desde GitHub: Actions → Despliegue → Run workflow, con la etiqueta anterior (por ejemplo `v0.1.0`).

O directo en el VPS, si las imágenes de esa versión todavía están en GHCR:

```bash
cd /opt/quienlotiene
sed -i 's/^QLT_VERSION=.*/QLT_VERSION=v0.1.0/' .env
docker compose -f compose.prod.yaml pull && docker compose -f compose.prod.yaml up -d --wait
```

Las migraciones de Flyway no se deshacen: si la versión nueva cambió la base, volver atrás puede necesitar restaurar el respaldo anterior (sección 6).

## 5. Operación diaria

```bash
cd /opt/quienlotiene
docker compose -f compose.prod.yaml ps                 # estado y salud de los contenedores
docker compose -f compose.prod.yaml logs -f api        # logs JSON de la API (perfil prod)
docker compose -f compose.prod.yaml logs -f caddy      # certificados y peticiones
docker compose -f compose.prod.yaml restart api
docker stats --no-stream                               # memoria: API ≤ 640 MB, base ≤ 512 MB, Caddy ≤ 128 MB
```

La salud de la API está en `/actuator/health/readiness` dentro de la red interna (la usa el healthcheck). No sale a internet porque Caddy solo pasa `/api/*`.

## 6. Respaldos

**Qué se guarda**: la base completa (`pg_dump --format=custom`) y el volumen `uploads` con los documentos de las tiendas, cifrados con restic. Retención: 7 diarios, 4 semanales y 6 mensuales.

### 6.1 Puesta en marcha (una vez)

1. En Backblaze B2: crear un bucket privado (por ejemplo `quienlotiene-respaldos`) y una llave de aplicación con acceso solo a ese bucket. Anotar el endpoint S3 del bucket, el `keyID` y la `applicationKey`.
2. En el VPS:

```bash
sudo apt install -y restic
cd /opt/quienlotiene
sudo cp respaldo/respaldo.env.ejemplo respaldo.env
sudo chown root: respaldo.env && sudo chmod 600 respaldo.env
sudo nano respaldo.env              # repositorio, llaves de B2 y RESTIC_PASSWORD (openssl rand -base64 33)
sudo bash -c 'set -a; source /opt/quienlotiene/respaldo.env; restic init'
sudo /opt/quienlotiene/respaldo/respaldo.sh
sudo /opt/quienlotiene/respaldo/restaurar.sh probar
sudo cp respaldo/quienlotiene-respaldo.service respaldo/quienlotiene-respaldo.timer /etc/systemd/system/
sudo systemctl daemon-reload && sudo systemctl enable --now quienlotiene-respaldo.timer
systemctl list-timers quienlotiene-respaldo.timer
```

Guardar `RESTIC_PASSWORD` en el gestor de contraseñas: sin ella los respaldos no se pueden leer.

3. Opcional: crear un chequeo en Healthchecks.io (periodo de 1 día) y poner su URL en `QLT_PING_RESPALDO` para recibir un correo si el respaldo falla o no corre.

### 6.2 Verificar (antes de cada entrega y una vez al mes)

```bash
sudo /opt/quienlotiene/respaldo/restaurar.sh probar
```

Restaura el último respaldo de la base en un contenedor temporal, cuenta las tablas por esquema y lo borra. No toca producción. El Plan Maestro de Pruebas pide un respaldo "verificado" antes de la liberación: esta es la evidencia.

### 6.3 Restaurar producción

```bash
sudo bash -c 'set -a; source /opt/quienlotiene/respaldo.env; restic snapshots'   # elegir el id
sudo /opt/quienlotiene/respaldo/restaurar.sh produccion <id>                    # sin id usa el último
```

Detiene la API, crea la base limpia, restaura la base y los documentos, y vuelve a levantar la API. Pide escribir `RESTAURAR` para confirmar.

## 7. Monitoreo (RNF-018)

- Un chequeo externo gratuito (UptimeRobot o similar) cada 5 minutos contra `https://quienlotiene.tech/`.
- Healthchecks.io para el respaldo diario (sección 6.1).
- `docker compose ps` muestra `healthy` o `unhealthy` según el healthcheck de la base y de la API.

## 8. Pendientes

- **Content-Security-Policy** en el `Caddyfile`: acordarla con el frontend cuando estén definidas las fuentes del mapa (teselas de OpenStreetMap) y de Web Push.
- **Ajustar memoria** con datos reales del C3 (`docker stats`).
