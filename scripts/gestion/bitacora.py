#!/usr/bin/env python3
"""Genera la tabla de la bitácora del proyecto a partir de los pull requests.

Registra qué se hizo, cuándo, quién lo hizo, quién lo fusionó, si quedó una
aprobación formal en GitHub, qué historias (HU) y solicitudes de cambio (SC)
toca y en qué versión salió. Escribe la tabla entre los marcadores
``<!-- inicio-bitacora -->`` y ``<!-- fin-bitacora -->`` de
``docs/gestion/BITACORA-DEL-PROYECTO.md``.

Uso (desde la raíz del repositorio):

    # Con la API de GitHub (recomendado; necesita un token de solo lectura)
    GITHUB_TOKEN=... python3 scripts/gestion/bitacora.py

    # Sin conexión, desde un JSON con la forma de la API de GitHub
    python3 scripts/gestion/bitacora.py --entrada prs.json

Procedimiento: docs/gestion/PROCEDIMIENTO-DE-SEGUIMIENTO.md.
"""

from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import urllib.request
from datetime import datetime, timedelta, timezone
from pathlib import Path

REPOSITORIO = os.environ.get("GITHUB_REPOSITORY", "Daniel0442123/QuienLoTiene")
ARCHIVO_BITACORA = Path("docs/gestion/BITACORA-DEL-PROYECTO.md")
INICIO = "<!-- inicio-bitacora -->"
FIN = "<!-- fin-bitacora -->"
ZONA_BOGOTA = timezone(timedelta(hours=-5))

# Cuenta de GitHub → integrante (docs/gestion/README.md, sección «Equipo y cuentas»).
INTEGRANTES = {
    "Daniel0442123": "Daniel Montenegro Almendra",
    "daniel0442": "Daniel Montenegro Almendra",
    "monicacajas-art": "Monica Anyi Cajas Mellizo",
}

TIPOS = {
    "feat": "Funcionalidad",
    "fix": "Corrección",
    "docs": "Documentación",
    "build": "Construcción y despliegue",
    "chore": "Mantenimiento",
    "test": "Pruebas",
    "refactor": "Refactorización",
    "ci": "Integración continua",
    "perf": "Rendimiento",
    "style": "Formato",
}


def llamar_api(ruta: str, token: str) -> list | dict:
    peticion = urllib.request.Request(
        f"https://api.github.com/repos/{REPOSITORIO}{ruta}",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "User-Agent": "bitacora-quienlotiene",
        },
    )
    with urllib.request.urlopen(peticion, timeout=30) as respuesta:
        return json.load(respuesta)


def leer_de_github(token: str) -> list[dict]:
    pull_requests: list[dict] = []
    pagina = 1
    while True:
        lote = llamar_api(f"/pulls?state=all&per_page=100&page={pagina}", token)
        if not lote:
            break
        pull_requests.extend(lote)
        pagina += 1
    for pull_request in pull_requests:
        numero = pull_request["number"]
        detalle = llamar_api(f"/pulls/{numero}", token)
        pull_request["merged_by"] = detalle.get("merged_by")
        pull_request["reviews"] = llamar_api(f"/pulls/{numero}/reviews", token)
    return pull_requests


def login(valor) -> str:
    if isinstance(valor, dict):
        return valor.get("login", "")
    return valor or ""


def nombre(cuenta: str) -> str:
    if not cuenta:
        return "—"
    return f"{INTEGRANTES.get(cuenta, cuenta)} (@{cuenta})"


def fecha_local(iso: str | None) -> str:
    if not iso:
        return "—"
    instante = datetime.fromisoformat(iso.replace("Z", "+00:00"))
    return instante.astimezone(ZONA_BOGOTA).strftime("%d/%m/%Y %H:%M")


def version_de(numero: int) -> str:
    """Primera etiqueta v* que contiene el commit fusionado del PR."""
    try:
        commit = subprocess.run(
            ["git", "log", "main", "--format=%H", f"--grep=(#{numero})$"],
            capture_output=True, text=True, check=True,
        ).stdout.split()
        if not commit:
            return "—"
        etiquetas = subprocess.run(
            ["git", "tag", "--contains", commit[0], "--sort=v:refname", "-l", "v*"],
            capture_output=True, text=True, check=True,
        ).stdout.split()
        return etiquetas[0] if etiquetas else "Sin liberar"
    except (subprocess.CalledProcessError, FileNotFoundError):
        return "—"


def ids(patron: str, texto: str) -> str:
    encontrados = sorted(set(re.findall(patron, texto)))
    return ", ".join(encontrados) if encontrados else "—"


def tipo_de(titulo: str) -> str:
    coincidencia = re.match(r"(\w+)(?:\(([^)]+)\))?!?:", titulo)
    if not coincidencia:
        return "—"
    tipo = TIPOS.get(coincidencia.group(1), coincidencia.group(1))
    return f"{tipo} · {coincidencia.group(2)}" if coincidencia.group(2) else tipo


def estado_de(pull_request: dict) -> str:
    if pull_request.get("merged_at"):
        return "Fusionado"
    if pull_request.get("state") == "open":
        return "Borrador" if pull_request.get("draft") else "Abierto"
    return "Cerrado sin fusionar"


def aprobacion_de(pull_request: dict) -> str:
    aprobadores = sorted({
        login(revision.get("user"))
        for revision in pull_request.get("reviews") or []
        if revision.get("state") == "APPROVED"
    })
    if aprobadores:
        return "Sí: " + ", ".join(f"@{cuenta}" for cuenta in aprobadores)
    return "No registrada"


def historia_de(cuerpo: str) -> str:
    """Texto de la sección «Historia» del PR (sin ella, el cuerpo completo)."""
    coincidencia = re.search(r"^##\s+Historia[^\n]*\n(.*?)(?=^##\s|\Z)", cuerpo, re.MULTILINE | re.DOTALL)
    return coincidencia.group(1) if coincidencia else cuerpo


def fila(pull_request: dict) -> str:
    numero = pull_request["number"]
    titulo = pull_request["title"].replace("|", "\\|")
    cuerpo = pull_request.get("body") or ""
    texto = f"{titulo}\n{cuerpo}"
    asistido = "Sí" if "Claude Code" in cuerpo else "No"
    celdas = [
        f"[#{numero}](https://github.com/{REPOSITORIO}/pull/{numero})",
        titulo,
        tipo_de(pull_request["title"]),
        ids(r"HU-\d{3}|\bT-\d{2}\b", f"{titulo}\n{historia_de(cuerpo)}"),
        ids(r"SC-\d{3}", texto),
        nombre(login(pull_request.get("user"))),
        fecha_local(pull_request.get("created_at")),
        fecha_local(pull_request.get("merged_at")),
        nombre(login(pull_request.get("merged_by"))) if pull_request.get("merged_at") else "—",
        aprobacion_de(pull_request),
        version_de(numero) if pull_request.get("merged_at") else "—",
        estado_de(pull_request),
        asistido,
    ]
    return "| " + " | ".join(celdas) + " |"


def tabla(pull_requests: list[dict]) -> str:
    encabezado = (
        "| PR | Qué se hizo | Tipo · módulo | HU / tarea | SC | Autor (quién) | Abierto | "
        "Fusionado (cuándo) | Fusionó | Aprobación formal en GitHub | Versión | Estado | "
        "Asistido por IA |\n"
        "|---|---|---|---|---|---|---|---|---|---|---|---|---|"
    )
    filas = [fila(pr) for pr in sorted(pull_requests, key=lambda pr: pr["number"])]
    generado = datetime.now(ZONA_BOGOTA).strftime("%d/%m/%Y %H:%M")
    return f"{encabezado}\n" + "\n".join(filas) + f"\n\n_Tabla generada el {generado} (hora de Bogotá) con `scripts/gestion/bitacora.py`._"


def main() -> int:
    argumentos = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    argumentos.add_argument("--entrada", type=Path, help="JSON con los PR (forma de la API de GitHub)")
    argumentos.add_argument("--salida", type=Path, default=ARCHIVO_BITACORA)
    opciones = argumentos.parse_args()

    if opciones.entrada:
        pull_requests = json.loads(opciones.entrada.read_text(encoding="utf-8"))
    elif os.environ.get("GITHUB_TOKEN"):
        pull_requests = leer_de_github(os.environ["GITHUB_TOKEN"])
    else:
        print("Falta GITHUB_TOKEN o --entrada.", file=sys.stderr)
        return 1

    documento = opciones.salida.read_text(encoding="utf-8")
    if INICIO not in documento or FIN not in documento:
        print(f"{opciones.salida} no tiene los marcadores {INICIO} y {FIN}.", file=sys.stderr)
        return 1
    antes, resto = documento.split(INICIO, 1)
    _, despues = resto.split(FIN, 1)
    opciones.salida.write_text(f"{antes}{INICIO}\n{tabla(pull_requests)}\n{FIN}{despues}", encoding="utf-8")
    print(f"Bitácora actualizada con {len(pull_requests)} pull requests en {opciones.salida}.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
