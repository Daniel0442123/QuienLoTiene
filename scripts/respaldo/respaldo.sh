#!/usr/bin/env bash
# Respaldo diario de producción (diagrama 08-despliegue): la base con pg_dump y los documentos de las tiendas
# (volumen uploads), cifrados con restic en el almacenamiento externo. Lo ejecuta el timer de systemd
# quienlotiene-respaldo.timer como root. Configuración en /opt/quienlotiene/respaldo.env (ver respaldo.env.ejemplo).
set -euo pipefail

readonly CARPETA=/opt/quienlotiene
readonly PROYECTO=quienlotiene

set -a
# shellcheck source=/dev/null
source "$CARPETA/respaldo.env"
set +a

avisar() {
    # Healthchecks.io u otro servicio parecido: avisa si el respaldo falla o deja de correr
    if [[ -n "${QLT_PING_RESPALDO:-}" ]]; then
        curl -fsS -m 10 --retry 3 -o /dev/null "${QLT_PING_RESPALDO}${1:-}" || true
    fi
}

avisar /start
trap 'avisar /fail' ERR

cd "$CARPETA"

echo "Respaldando la base de datos"
# --stdin-from-command: si pg_dump falla, restic no guarda un respaldo a medias
restic backup --tag base --stdin-filename quienlotiene.dump --stdin-from-command -- \
    docker compose -f compose.prod.yaml exec -T db \
    sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" --format=custom'

echo "Respaldando los documentos de las tiendas"
carpeta_archivos=$(docker volume inspect "${PROYECTO}_uploads" --format '{{ .Mountpoint }}')
restic backup --tag archivos "$carpeta_archivos"

echo "Aplicando la retención: 7 diarios, 4 semanales y 6 mensuales"
restic forget --prune --group-by tags --keep-daily 7 --keep-weekly 4 --keep-monthly 6

echo "Respaldo terminado"
avisar
