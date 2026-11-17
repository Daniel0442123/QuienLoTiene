#!/usr/bin/env bash
# Limpieza de datos de uso de producción mientras el sitio es un ambiente de producción-pruebas
# (docs/despliegue/DESPLIEGUE.md, sección «Limpieza de datos de prueba»). Se ejecuta como root en el VPS.
#
#   limpiar.sh pruebas [--simular]   Borra las cuentas cuyo correo termina en @$QLT_LIMPIEZA_DOMINIO_PRUEBAS
#                                    (por defecto prueba.quienlotiene.tech) y todo lo que cuelga de ellas:
#                                    solicitudes, respuestas, órdenes, tiendas, horarios, documentos, direcciones.
#                                    Sirve antes y después del lanzamiento.
#   limpiar.sh total [--simular]     Borra todas las cuentas de clientes y tiendas y sus datos. Solo corre si el
#                                    .env tiene QLT_LIMPIEZA_TOTAL_PERMITIDA=si; al salir a producción real esa
#                                    línea se borra y el modo queda bloqueado.
#
# Nunca borra: la estructura ni el historial de Flyway, las categorías, las cuentas ADMIN ni la auditoría.
# Antes de borrar saca un respaldo con respaldo.sh; si el respaldo falla, no borra nada.
# --simular cuenta lo que se borraría, sin respaldo y sin borrar.
# Cada ejecución queda en /opt/quienlotiene/limpieza.log.
set -euo pipefail

readonly CARPETA=${QLT_CARPETA:-/opt/quienlotiene}
readonly PROYECTO=quienlotiene
readonly REGISTRO="$CARPETA/limpieza.log"
readonly SQL="$CARPETA/limpieza/limpieza.sql"

uso() {
    sed -n '4,13p' "$0" | sed 's/^# \{0,1\}//'
    exit 2
}

modo=${1:-}
simular=no
[[ "${2:-}" == "--simular" ]] && simular=si
[[ "$modo" == "pruebas" || "$modo" == "total" ]] || uso

cd "$CARPETA"
leer_env() {
    # Solo las variables que usa este script; el .env no se ejecuta
    grep -E "^$1=" .env 2>/dev/null | tail -1 | cut -d= -f2- | tr -d '"'"'"'' || true
}
dominio_sitio=$(leer_env QLT_DOMINIO)
dominio_pruebas=$(leer_env QLT_LIMPIEZA_DOMINIO_PRUEBAS)
dominio_pruebas=${dominio_pruebas:-prueba.quienlotiene.tech}
total_permitida=$(leer_env QLT_LIMPIEZA_TOTAL_PERMITIDA)

if [[ "$modo" == "total" && "$total_permitida" != "si" ]]; then
    echo "El modo total está bloqueado: el .env no tiene QLT_LIMPIEZA_TOTAL_PERMITIDA=si." >&2
    echo "Si el sitio ya tiene usuarios reales, usa «limpiar.sh pruebas»." >&2
    exit 1
fi

compose() {
    docker compose -f compose.prod.yaml "$@"
}

ejecutar_sql() {
    compose exec -T db sh -c 'psql -q -At -F "|" -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
        -v modo="$1" -v dominio="$2" -v aplicar="$3" -f -' _ "$modo" "$dominio_pruebas" "$1" < "$SQL"
}

mostrar_conteo() {
    grep '^CONTEO|' | cut -d'|' -f2- | awk -F'|' '{ printf "  %-36s %s\n", $1, $2 }'
}

echo "Modo: $modo$([[ $modo == pruebas ]] && echo " (cuentas @$dominio_pruebas)")"
echo "Lo que se borraría:"
ejecutar_sql no | mostrar_conteo

if [[ "$simular" == "si" ]]; then
    echo "Simulación: no se borró nada."
    exit 0
fi

echo
echo "Para seguir escribe el dominio del sitio (${dominio_sitio:-sin QLT_DOMINIO en el .env}):"
read -r confirmacion
if [[ -z "$dominio_sitio" || "$confirmacion" != "$dominio_sitio" ]]; then
    echo "No coincide. No se borró nada."
    exit 1
fi

echo "Sacando un respaldo antes de borrar"
"$CARPETA/respaldo/respaldo.sh"

echo "Borrando"
salida=$(ejecutar_sql si)
echo "$salida" | mostrar_conteo

carpeta_archivos=$(docker volume inspect "${PROYECTO}_uploads" --format '{{ .Mountpoint }}')
archivos=0
while IFS= read -r ruta; do
    [[ -z "$ruta" ]] && continue
    destino=$(realpath -m "$carpeta_archivos/$ruta")
    # Nunca fuera del volumen de documentos
    if [[ "$destino" == "$carpeta_archivos"/* ]]; then
        rm -f -- "$destino" && archivos=$((archivos + 1))
    fi
done < <(echo "$salida" | grep '^ARCHIVO|' | cut -d'|' -f2-)
echo "  documentos borrados del disco          $archivos"

{
    printf '%s · modo %s · %s\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)" "$modo" "${SUDO_USER:-$(whoami)}"
    echo "$salida" | grep '^CONTEO|' | cut -d'|' -f2- | sed 's/^/  /'
    echo "  archivos|$archivos"
} >> "$REGISTRO"
echo "Listo. Quedó anotado en $REGISTRO; anótalo también en la bitácora del proyecto."
