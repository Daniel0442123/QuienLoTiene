#!/usr/bin/env python3
"""Pasa los resultados del libro de QA en Markdown al libro Excel de entrega.

El Markdown de ``docs/pruebas/libro/`` es la fuente (PROCEDIMIENTO-DOCUMENTAL.md,
sección 1). Este script copia sus datos en
``docs/pruebas/entregables/Libro-Control-QA-QuienLoTiene-v1.0.xlsx`` sin tocar
las fórmulas, listas ni formatos del libro:

- «Ejecución C<n>»: columnas de resultado de cada caso (Probador a Duración),
  buscando la fila por el ID del caso.
- «Defectos»: una fila por DEF-nnn (la fila 4 + n), menos la columna ID y la
  columna «Abierto», que son fórmulas.
- «Bitácora»: las sesiones del ciclo, desde la fila 5.

Uso (desde la raíz del repositorio):

    python3 scripts/gestion/libro_qa_excel.py --ciclo C1

Necesita openpyxl (pip install openpyxl).
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

from openpyxl import load_workbook

LIBRO_MD = Path("docs/pruebas/libro")
LIBRO_XLSX = Path("docs/pruebas/entregables/Libro-Control-QA-QuienLoTiene-v1.0.xlsx")
FILA_ENCABEZADO, PRIMERA_FILA = 4, 5


def limpiar(texto: str) -> str:
    """Quita el formato Markdown. Los enlaces quedan como su ruta desde la raíz del repositorio."""

    def ruta(coincidencia: re.Match) -> str:
        destino = coincidencia.group(2)
        if destino.startswith("http"):
            return destino
        partes = []
        for parte in ("docs/pruebas/libro/" + destino).split("/"):
            if parte == "..":
                partes.pop()
            elif parte != ".":
                partes.append(parte)
        return "/".join(partes)

    texto = re.sub(r"\[([^\]]+)\]\(([^)]+)\)", ruta, texto)
    texto = texto.replace("`", "").replace("**", "").replace("\\|", "|").replace("<br>", "\n").strip()
    return "" if texto in ("—", "-") else texto


def tablas(archivo: Path) -> list[list[dict[str, str]]]:
    """Tablas del archivo como listas de filas {encabezado: valor}, sin la del historial."""
    resultado, encabezado, filas = [], None, []
    for linea in archivo.read_text(encoding="utf-8").splitlines() + [""]:
        if linea.startswith("## Historial"):
            break
        if not linea.startswith("|"):
            if encabezado and filas:
                resultado.append(filas)
            encabezado, filas = None, []
            continue
        celdas = [c.strip() for c in re.split(r"(?<!\\)\|", linea.strip())[1:-1]]
        if all(re.fullmatch(r":?-+:?", c) for c in celdas):
            continue
        if encabezado is None:
            encabezado = celdas
        else:
            filas.append(dict(zip(encabezado, celdas)))
    return resultado


def numero(texto: str):
    """«0,5» → 0.5, «97,2 %» → 0.972, «25» → 25; lo demás queda como texto."""
    valor = texto.replace(" ", " ").strip()
    porcentaje = valor.endswith("%")
    valor = valor.rstrip("%").strip().replace(",", ".")
    if re.fullmatch(r"-?\d+(\.\d+)?", valor):
        cifra = float(valor) / (100 if porcentaje else 1)
        return int(cifra) if cifra.is_integer() and not porcentaje else round(cifra, 4)
    conteo = re.match(r"(\d+) \D", texto)  # «44 vistas», «5 re-pruebas»: el número para los totales
    return int(conteo.group(1)) if conteo else texto


def columnas(hoja) -> dict[str, int]:
    return {c.value: c.column for c in hoja[FILA_ENCABEZADO] if c.value}


def escribir(celda, valor) -> None:
    if isinstance(celda.value, str) and celda.value.startswith("="):
        return
    celda.value = valor if valor != "" else None


def ejecucion(libro, ciclo: str) -> int:
    hoja = libro[f"Ejecución {ciclo}"]
    mapa = columnas(hoja)
    filas_por_id = {hoja.cell(f, 1).value: f for f in range(PRIMERA_FILA, hoja.max_row + 1) if hoja.cell(f, 1).value}
    cambiadas = 0
    archivo = LIBRO_MD / f"0{int(ciclo[1:]) + 2}-EJECUCION-{ciclo}.md"
    for tabla in tablas(archivo):
        for fila in tabla:
            caso = limpiar(fila.get("ID del caso") or fila.get("ID", ""))  # la tabla de punta a punta usa «ID»
            if caso not in filas_por_id:
                continue
            for campo in ("Probador", "Fecha", "Resultado", "Defecto(s)", "Evidencia (enlace)", "Observaciones",
                          "Duración (min)"):
                if campo in fila and campo in mapa:
                    escribir(hoja.cell(filas_por_id[caso], mapa[campo]), limpiar(fila[campo]))
            cambiadas += 1
    return cambiadas


def defectos(libro) -> int:
    hoja = libro["Defectos"]
    mapa = columnas(hoja)
    cambiadas = 0
    for tabla in tablas(LIBRO_MD / "06-DEFECTOS.md"):
        for fila in tabla:
            ident = fila.get("ID", "")
            if not re.fullmatch(r"DEF-\d{3}", ident):
                continue
            n_fila = FILA_ENCABEZADO + int(ident[4:])
            for campo, valor in fila.items():
                if campo in mapa and campo not in ("ID", "Abierto"):
                    celda = hoja.cell(n_fila, mapa[campo])
                    texto = limpiar(valor)
                    if campo in ("Requisito", "Módulo"):
                        celda.value = texto or None  # el dato del Markdown manda sobre la búsqueda en «Casos»
                    else:
                        escribir(celda, texto)
            cambiadas += 1
    return cambiadas


def bitacora(libro, ciclo: str) -> int:
    hoja = libro["Bitácora"]
    mapa = columnas(hoja)
    numericos = {"Horas", "Casos ejecutados", "Aprobados", "Fallidos", "Bloqueados", "Cobertura domain+application"}
    sesiones = [f for t in tablas(LIBRO_MD / "08-BITACORA.md") for f in t if f.get("Ciclo") == ciclo]
    # Las filas del ciclo empiezan en la primera fila libre o en la primera del mismo ciclo
    inicio = next((f for f in range(PRIMERA_FILA, hoja.max_row + 1)
                   if hoja.cell(f, mapa["Ciclo"]).value in (ciclo, None)), hoja.max_row + 1)
    for desplazamiento, sesion in enumerate(sesiones):
        for campo, valor in sesion.items():
            if campo in mapa:
                texto = limpiar(valor)
                escribir(hoja.cell(inicio + desplazamiento, mapa[campo]), numero(texto) if campo in numericos else texto)
    return len(sesiones)


def main() -> int:
    argumentos = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    argumentos.add_argument("--ciclo", default="C1")
    opciones = argumentos.parse_args()
    libro = load_workbook(LIBRO_XLSX)
    casos = ejecucion(libro, opciones.ciclo)
    defs = defectos(libro)
    sesiones = bitacora(libro, opciones.ciclo)
    libro.calculation.fullCalcOnLoad = True  # el tablero y la trazabilidad se recalculan al abrir
    libro.save(LIBRO_XLSX)
    print(f"{LIBRO_XLSX}: {casos} casos, {defs} defectos y {sesiones} sesiones de {opciones.ciclo}.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
