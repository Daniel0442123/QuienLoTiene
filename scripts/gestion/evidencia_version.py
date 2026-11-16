#!/usr/bin/env python3
"""Arma las notas y la evidencia de calidad de una versión (etiqueta v*).

Junta en un Markdown:
1. Las notas de la versión, copiadas de docs/gestion/REGISTRO-DE-CAMBIOS.md.
2. La corrida de CI del commit etiquetado: cada job con su resultado y enlace.
3. Pruebas (unitarias e integración) y cobertura, leídas de los artefactos
   «resultados-pruebas» y «reporte-jacoco» de esa corrida.
4. La corrida de CI del frontend que corresponde a ese commit.
5. El despliegue de la etiqueta (workflow «Despliegue») con sus jobs.

Opcionalmente guarda los artefactos en una carpeta para adjuntarlos a la
Release de GitHub, que los conserva después de los 14 días que GitHub
Actions guarda los artefactos.

Uso:
    GITHUB_TOKEN=... python3 scripts/gestion/evidencia_version.py v0.2.0 \
        --salida notas.md --adjuntos evidencia/

Lo corre .github/workflows/version.yml al terminar cada despliegue.
Procedimiento: docs/gestion/PROCEDIMIENTO-DOCUMENTAL.md, sección 6.
"""

from __future__ import annotations

import argparse
import csv
import io
import json
import os
import re
import sys
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

REPOSITORIO = os.environ.get("GITHUB_REPOSITORY", "Daniel0442123/QuienLoTiene")
API = f"https://api.github.com/repos/{REPOSITORIO}"
ZONA_BOGOTA = timezone(timedelta(hours=-5))
RESULTADO = {"success": "✅ Correcto", "failure": "❌ Falló", "cancelled": "⏹ Cancelado", "skipped": "⏭ Omitido", None: "⏳ En curso"}


class Github:
    def __init__(self, token: str):
        self.token = token

    def _abrir(self, url: str):
        peticion = urllib.request.Request(url, headers={
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "User-Agent": "evidencia-quienlotiene",
        })
        return urllib.request.urlopen(peticion, timeout=60)

    def json(self, ruta: str):
        with self._abrir(ruta if ruta.startswith("http") else API + ruta) as respuesta:
            return json.load(respuesta)

    def binario(self, url: str) -> bytes:
        """Descarga un artefacto: GitHub redirige a un enlace firmado al que no se envía el token."""

        class SinRedireccion(urllib.request.HTTPRedirectHandler):
            def redirect_request(self, *args, **kwargs):
                return None

        peticion = urllib.request.Request(url, headers={
            "Authorization": f"Bearer {self.token}",
            "Accept": "application/vnd.github+json",
            "User-Agent": "evidencia-quienlotiene",
        })
        try:
            with urllib.request.build_opener(SinRedireccion).open(peticion, timeout=60) as respuesta:
                return respuesta.read()
        except urllib.error.HTTPError as redireccion:
            if redireccion.code not in (301, 302, 303, 307, 308):
                raise
            destino = redireccion.headers["Location"]
        with urllib.request.urlopen(urllib.request.Request(destino, headers={"User-Agent": "evidencia-quienlotiene"}), timeout=120) as respuesta:
            return respuesta.read()


def fecha(iso: str | None) -> str:
    if not iso:
        return "—"
    return datetime.fromisoformat(iso.replace("Z", "+00:00")).astimezone(ZONA_BOGOTA).strftime("%d/%m/%Y %H:%M")


def commit_de(github: Github, version: str) -> str:
    referencia = github.json(f"/git/ref/tags/{version}")["object"]
    if referencia["type"] == "tag":  # etiqueta anotada
        referencia = github.json(f"/git/tags/{referencia['sha']}")["object"]
    return referencia["sha"]


def corridas(github: Github, flujo: str, **filtros) -> list[dict]:
    consulta = "&".join(f"{k}={v}" for k, v in filtros.items())
    return github.json(f"/actions/workflows/{flujo}/runs?per_page=100&{consulta}")["workflow_runs"]


def tabla_jobs(github: Github, corrida: dict) -> list[str]:
    jobs = github.json(f"/actions/runs/{corrida['id']}/jobs")["jobs"]
    filas = ["| Job | Resultado | Terminó |", "|---|---|---|"]
    for job in jobs:
        filas.append(f"| [{job['name']}]({job['html_url']}) | {RESULTADO.get(job['conclusion'], job['conclusion'])} | {fecha(job['completed_at'])} |")
    return filas


def artefacto(github: Github, corrida: dict, nombre: str) -> bytes | None:
    for item in github.json(f"/actions/runs/{corrida['id']}/artifacts")["artifacts"]:
        if item["name"] == nombre and not item["expired"]:
            return github.binario(item["archive_download_url"])
    return None


def resumen_pruebas(zip_bytes: bytes) -> dict[str, dict[str, int]]:
    totales = {"unitarias": dict.fromkeys(("pruebas", "fallas", "errores", "omitidas"), 0),
               "integracion": dict.fromkeys(("pruebas", "fallas", "errores", "omitidas"), 0)}
    with zipfile.ZipFile(io.BytesIO(zip_bytes)) as archivo:
        for nombre in archivo.namelist():
            if not (nombre.endswith(".xml") and "TEST-" in nombre):
                continue
            suite = ET.fromstring(archivo.read(nombre))
            tipo = "integracion" if "failsafe" in nombre or suite.get("name", "").endswith("IT") else "unitarias"
            for clave, atributo in (("pruebas", "tests"), ("fallas", "failures"), ("errores", "errors"), ("omitidas", "skipped")):
                totales[tipo][clave] += int(suite.get(atributo, 0) or 0)
    return totales


def resumen_cobertura(zip_bytes: bytes) -> tuple[float, float] | None:
    with zipfile.ZipFile(io.BytesIO(zip_bytes)) as archivo:
        nombres = [n for n in archivo.namelist() if n.endswith("jacoco.csv")]
        if not nombres:
            return None
        filas = list(csv.DictReader(io.StringIO(archivo.read(nombres[0]).decode("utf-8"))))
    def porcentaje(seleccion):
        cubiertas = sum(int(f["LINE_COVERED"]) for f in seleccion)
        perdidas = sum(int(f["LINE_MISSED"]) for f in seleccion)
        return 100 * cubiertas / (cubiertas + perdidas) if cubiertas + perdidas else 0.0
    nucleo = [f for f in filas if re.search(r"\.(domain|application)(\.|$)", f["PACKAGE"])]
    return porcentaje(filas), porcentaje(nucleo)


def notas_de(version: str) -> str:
    registro = Path("docs/gestion/REGISTRO-DE-CAMBIOS.md")
    if not registro.exists():
        return ""
    coincidencia = re.search(rf"^## {re.escape(version)}\b[^\n]*\n(.*?)(?=^## |\Z)", registro.read_text(encoding="utf-8"), re.M | re.S)
    return coincidencia.group(1).strip() if coincidencia else "_La versión todavía no tiene notas en REGISTRO-DE-CAMBIOS.md._"


def main() -> int:
    argumentos = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    argumentos.add_argument("version")
    argumentos.add_argument("--salida", type=Path)
    argumentos.add_argument("--adjuntos", type=Path, help="Carpeta donde guardar los artefactos para la Release")
    opciones = argumentos.parse_args()
    token = os.environ.get("GITHUB_TOKEN")
    if not token:
        print("Falta GITHUB_TOKEN.", file=sys.stderr)
        return 1
    github = Github(token)
    version = opciones.version
    sha = commit_de(github, version)

    lineas = [f"## Notas de {version}", "", notas_de(version), "",
              f"## Evidencia de calidad de {version}", "",
              f"Commit `{sha[:7]}` · generado el {datetime.now(ZONA_BOGOTA).strftime('%d/%m/%Y %H:%M')} (hora de Bogotá) con `scripts/gestion/evidencia_version.py`.", ""]

    ci = [c for c in corridas(github, "ci.yml", head_sha=sha) if c["event"] == "push"] or corridas(github, "ci.yml", head_sha=sha)
    if ci:
        corrida = ci[0]
        lineas += [f"### Integración continua del backend · [corrida {corrida['run_number']}]({corrida['html_url']}) · {RESULTADO.get(corrida['conclusion'], corrida['conclusion'])}", ""]
        lineas += tabla_jobs(github, corrida) + [""]
        pruebas = artefacto(github, corrida, "resultados-pruebas")
        cobertura = artefacto(github, corrida, "reporte-jacoco")
        if pruebas:
            resumen = resumen_pruebas(pruebas)
            lineas += ["| Pruebas | Ejecutadas | Fallas | Errores | Omitidas |", "|---|---|---|---|---|"]
            for tipo, etiqueta in (("unitarias", "Unitarias (`*Test`)"), ("integracion", "Integración (`*IT`, Testcontainers)")):
                r = resumen[tipo]
                lineas.append(f"| {etiqueta} | {r['pruebas']} | {r['fallas']} | {r['errores']} | {r['omitidas']} |")
            lineas.append("")
        else:
            lineas += ["_El artefacto de resultados de pruebas ya venció o no existe; ver los registros del job._", ""]
        if cobertura and (valores := resumen_cobertura(cobertura)):
            lineas += [f"Cobertura de líneas (JaCoCo): {valores[0]:.1f} % total · {valores[1]:.1f} % en `domain` y `application` (meta ≥ 70 %, RNF-022).", ""]
        if opciones.adjuntos:
            opciones.adjuntos.mkdir(parents=True, exist_ok=True)
            for nombre, contenido in (("resultados-pruebas", pruebas), ("reporte-jacoco", cobertura), ("sbom-cyclonedx", artefacto(github, corrida, "sbom-cyclonedx"))):
                if contenido:
                    (opciones.adjuntos / f"{version}-{nombre}.zip").write_bytes(contenido)
    else:
        lineas += ["_No hay corrida de CI para este commit._", ""]

    frontend = [c for c in corridas(github, "frontend.yml", branch="main", event="push") if c["created_at"] <= (ci[0]["created_at"] if ci else "9999")]
    if frontend:
        corrida = frontend[0]
        mismo = "este commit" if corrida["head_sha"] == sha else f"último cambio del frontend incluido (`{corrida['head_sha'][:7]}`)"
        lineas += [f"### Integración continua del frontend · [corrida {corrida['run_number']}]({corrida['html_url']}) · {RESULTADO.get(corrida['conclusion'], corrida['conclusion'])}", "", f"Corresponde al {mismo}.", ""]

    despliegues = [c for c in corridas(github, "despliegue.yml") if c["head_branch"] == version or c["head_sha"] == sha]
    if despliegues:
        for corrida in despliegues:
            lineas += [f"### Despliegue · [corrida {corrida['run_number']}]({corrida['html_url']}) · {RESULTADO.get(corrida['conclusion'], corrida['conclusion'])} · {fecha(corrida['created_at'])}", ""]
            lineas += tabla_jobs(github, corrida) + [""]
    else:
        lineas += ["_La etiqueta no tiene corrida de despliegue._", ""]

    texto = "\n".join(lineas).rstrip() + "\n"
    if opciones.salida:
        opciones.salida.write_text(texto, encoding="utf-8")
    else:
        print(texto)
    return 0


if __name__ == "__main__":
    sys.exit(main())
