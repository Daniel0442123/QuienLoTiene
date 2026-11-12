#!/usr/bin/env bash
# Restaurar un respaldo de restic (docs/despliegue/DESPLIEGUE.md, sección "Respaldos"). Se ejecuta como root.
#
#   restaurar.sh probar              Restaura el último respaldo de la base en un contenedor temporal y cuenta las
#                                    tablas. No toca producción: sirve para verificar que los respaldos sirven.
#   restaurar.sh produccion [id]     Detiene la API, reemplaza la base y los documentos por el respaldo indicado
#                                    (por defecto el último) y vuelve a levantar la API. Pide confirmación.
set -euo pipefail

readonly CARPETA=/opt/quienlotiene
readonly PROYECTO=quienlotiene
readonly IMAGEN_BASE=postgis/postgis:17-3.5-alpine
readonly CONTENEDOR_PRUEBA=quienlotiene-prueba-restauracion

set -a
# shellcheck source=/dev/null
source "$CARPETA/respaldo.env"
set +a
cd "$CARPETA"

compose() {
    docker compose -f compose.prod.yaml "$@"
}

probar() {
    echo "Restaurando el último respaldo de la base en un contenedor temporal"
    docker rm -f "$CONTENEDOR_PRUEBA" >/dev/null 2>&1 || true
    trap 'docker rm -f "$CONTENEDOR_PRUEBA" >/dev/null 2>&1 || true' EXIT
    docker run -d --name "$CONTENEDOR_PRUEBA" -e POSTGRES_PASSWORD=prueba -e POSTGRES_DB=quienlotiene \
        "$IMAGEN_BASE" >/dev/null
    # La imagen arranca primero un servidor temporal (solo por socket) para inicializarse y luego lo reinicia.
    # Por TCP solo responde el servidor definitivo: se espera a ese para no restaurar en el temporal.
    local listo=false
    for _ in $(seq 1 60); do
        if docker exec "$CONTENEDOR_PRUEBA" pg_isready -h 127.0.0.1 -U postgres -d quienlotiene >/dev/null 2>&1; then
            listo=true
            break
        fi
        sleep 2
    done
    [[ "$listo" == true ]] || { echo "La base temporal no arrancó en 2 minutos"; exit 1; }
    # La imagen de PostGIS crea sus extensiones en la base inicial; se restaura en una base limpia desde template0
    docker exec "$CONTENEDOR_PRUEBA" createdb -U postgres -T template0 restaurada
    restic dump --tag base latest quienlotiene.dump \
        | docker exec -i "$CONTENEDOR_PRUEBA" pg_restore -U postgres -d restaurada --no-owner --no-privileges
    echo "Tablas restauradas por esquema:"
    docker exec "$CONTENEDOR_PRUEBA" psql -U postgres -d restaurada -c \
        "select table_schema as esquema, count(*) as tablas from information_schema.tables
         where table_schema not in ('pg_catalog', 'information_schema', 'tiger', 'tiger_data', 'topology')
         group by table_schema order by table_schema;"
    echo "Documentos en el último respaldo de archivos: $(restic ls --tag archivos latest | grep -c . || true) rutas"
    echo "Prueba de restauración terminada"
}

produccion() {
    local respaldo=${1:-latest}
    echo "Esto reemplaza la base y los documentos de PRODUCCIÓN por el respaldo '$respaldo'."
    read -r -p "Escribe RESTAURAR para continuar: " confirmacion
    [[ "$confirmacion" == "RESTAURAR" ]] || { echo "Cancelado"; exit 1; }

    compose stop api
    echo "Restaurando la base (se borra y se crea limpia desde template0)"
    compose exec -T db sh -c 'dropdb -U "$POSTGRES_USER" --if-exists --force "$POSTGRES_DB" \
        && createdb -U "$POSTGRES_USER" -T template0 "$POSTGRES_DB"'
    restic dump --tag base "$respaldo" quienlotiene.dump \
        | compose exec -T db sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges'
    echo "Restaurando los documentos"
    local carpeta_archivos
    carpeta_archivos=$(docker volume inspect "${PROYECTO}_uploads" --format '{{ .Mountpoint }}')
    restic restore --tag archivos "$respaldo" --target / --include "$carpeta_archivos"
    compose start api
    echo "Restauración terminada; revisa la aplicación"
}

case "${1:-}" in
    probar) probar ;;
    produccion) produccion "${2:-}" ;;
    *) echo "Uso: $0 probar | produccion [id-del-respaldo]"; exit 2 ;;
esac
