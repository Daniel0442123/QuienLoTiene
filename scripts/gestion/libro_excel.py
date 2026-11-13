#!/usr/bin/env python3
"""Genera el libro Excel de control de cambios desde los Markdown de docs/gestion.

El Markdown es la fuente (PROCEDIMIENTO-DOCUMENTAL.md, sección 1); este libro es
la copia para entregar. Se regenera después de cada cambio en la bitácora o en
el registro de solicitudes de cambio:

    python3 scripts/gestion/libro_excel.py

Necesita openpyxl (pip install openpyxl).
"""

from __future__ import annotations

import re
from datetime import datetime, timedelta, timezone
from pathlib import Path

from openpyxl import Workbook
from openpyxl.styles import Alignment, Font, PatternFill
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.datavalidation import DataValidation

GESTION = Path("docs/gestion")
SALIDA = GESTION / "entregables" / "Control-de-Cambios-QuienLoTiene.xlsx"
AZUL, GRIS, FUENTE = "FF00486B", "FF5F6B73", "Arial"


def limpiar(texto: str) -> str:
    texto = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", texto)
    return texto.replace("`", "").replace("**", "").replace("\\|", "|").strip()


def tablas_de(seccion_md: str) -> list[list[list[str]]]:
    tablas, actual = [], []
    for linea in seccion_md.splitlines():
        if linea.startswith("|"):
            celdas = [limpiar(c) for c in re.split(r"(?<!\\)\|", linea.strip())[1:-1]]
            if not all(re.fullmatch(r"-+", c) for c in celdas):
                actual.append(celdas)
        elif actual:
            tablas.append(actual)
            actual = []
    if actual:
        tablas.append(actual)
    return tablas


def seccion(documento: str, titulo: str) -> str:
    coincidencia = re.search(rf"^##+ {re.escape(titulo)}[^\n]*\n(.*?)(?=^## |\Z)", documento, re.M | re.S)
    return coincidencia.group(1) if coincidencia else ""


def hoja(libro: Workbook, nombre: str, titulo: str, nota: str, filas: list[list[str]], anchos: dict[int, int] | None = None):
    ws = libro.create_sheet(nombre)
    ws["A1"] = titulo
    ws["A1"].font = Font(name=FUENTE, size=14, bold=True, color=AZUL)
    ws["A2"] = nota
    ws["A2"].font = Font(name=FUENTE, size=10, color=GRIS)
    for numero_fila, fila in enumerate(filas, start=4):
        for numero_columna, valor in enumerate(fila, start=1):
            celda = ws.cell(row=numero_fila, column=numero_columna, value=valor)
            celda.alignment = Alignment(wrap_text=True, vertical="top")
            if numero_fila == 4:
                celda.font = Font(name=FUENTE, size=10, bold=True, color="FFFFFFFF")
                celda.fill = PatternFill("solid", fgColor=AZUL)
            else:
                celda.font = Font(name=FUENTE, size=10)
    columnas = max(len(f) for f in filas)
    for columna in range(1, columnas + 1):
        ancho = (anchos or {}).get(columna)
        if ancho is None:
            largo = max(len(str(f[columna - 1])) if columna - 1 < len(f) else 0 for f in filas)
            ancho = min(max(12, largo * 0.9), 60)
        ws.column_dimensions[get_column_letter(columna)].width = ancho
    ws.freeze_panes = "B5"
    ultima = 4 + max(len(filas) - 1, 0) + 50
    ws.auto_filter.ref = f"A4:{get_column_letter(columnas)}{ultima}"
    return ws, ultima


def lista(ws, columna: int, desde: int, hasta: int, rango: str):
    validacion = DataValidation(type="list", formula1=rango, allow_blank=True)
    letra = get_column_letter(columna)
    validacion.add(f"{letra}{desde}:{letra}{hasta}")
    ws.add_data_validation(validacion)


def main() -> None:
    bitacora = (GESTION / "BITACORA-DEL-PROYECTO.md").read_text(encoding="utf-8")
    control = (GESTION / "CONTROL-DE-CAMBIOS.md").read_text(encoding="utf-8")
    cambios = (GESTION / "REGISTRO-DE-CAMBIOS.md").read_text(encoding="utf-8")

    libro = Workbook()
    instrucciones = libro.active
    instrucciones.title = "Instrucciones"
    generado = datetime.now(timezone(timedelta(hours=-5))).strftime("%d/%m/%Y %H:%M")
    textos = [
        ("Libro de control de cambios · Quién lo tiene", Font(name=FUENTE, size=14, bold=True, color=AZUL)),
        (f"Generado el {generado} (hora de Bogotá) desde docs/gestion/*.md con scripts/gestion/libro_excel.py. El Markdown manda: si hay diferencias, se corrige el MD y se regenera este libro.", Font(name=FUENTE, size=10, color=GRIS)),
        ("", None),
        ("Bitácora PR: qué se hizo, cuándo y quién, un pull request por fila (BITACORA-DEL-PROYECTO.md, sección 2).", None),
        ("Hechos: decisiones, aprobaciones, etiquetas y despliegues fuera de los PR (sección 3).", None),
        ("Compromisos: lo que quedó pendiente, con responsable y fecha (sección 4).", None),
        ("Hallazgos: lo que encontró la reconstrucción del 13/11/2026.", None),
        ("Solicitudes de cambio: toda desviación del plan, con motivo, impacto, aprobación y documentos ajustados (CONTROL-DE-CAMBIOS.md, sección 5).", None),
        ("Versiones: notas de cada etiqueta v* (REGISTRO-DE-CAMBIOS.md).", None),
        ("Listas: valores permitidos para los estados y las clases.", None),
        ("", None),
        ("Normas: ISO 10007:2017, ISO/IEC/IEEE 12207:2017 (6.3.2, 6.3.5, 6.3.6), ISO 9001:2015 (6.3, 7.5.3, 8.5.6, 9.1), ISO/IEC/IEEE 29119-3.", Font(name=FUENTE, size=10, color=GRIS)),
    ]
    for fila, (texto, fuente) in enumerate(textos, start=1):
        celda = instrucciones.cell(row=fila, column=1, value=texto)
        celda.font = fuente or Font(name=FUENTE, size=10)
        celda.alignment = Alignment(wrap_text=True, vertical="top")
    instrucciones.column_dimensions["A"].width = 120

    pr = tablas_de(seccion(bitacora, "2. Pull requests"))[0]
    hoja(libro, "Bitácora PR", "Bitácora de pull requests", "Una fila por PR. Horas en America/Bogota.", pr)
    hoja(libro, "Hechos", "Hechos fuera de pull requests", "Una fila por hecho, en orden cronológico.", tablas_de(seccion(bitacora, "3. Hechos fuera de pull requests"))[0])
    ws, ultima = hoja(libro, "Compromisos", "Compromisos abiertos", "Se cierran con fecha y PR; nunca se borra la fila.", tablas_de(seccion(bitacora, "4. Compromisos abiertos"))[0])
    lista(ws, 6, 5, ultima, "Listas!$B$2:$B$4")
    hoja(libro, "Hallazgos", "Hallazgos de la reconstrucción (13/11/2026)", "Evidencia y tratamiento de cada hallazgo.", tablas_de(seccion(bitacora, "Hallazgos de la reconstrucción"))[0])
    ws, ultima = hoja(libro, "Solicitudes de cambio", "Registro de solicitudes de cambio (SC)", "Toda desviación del plan, por cualquier motivo.", tablas_de(seccion(control, "5. Registro de solicitudes de cambio"))[0])
    lista(ws, 6, 5, ultima, "Listas!$A$2:$A$4")
    lista(ws, 11, 5, ultima, "Listas!$C$2:$C$11")

    versiones = [["Versión", "Fecha", "Tipo", "Cambio"]]
    for bloque in re.split(r"^## ", cambios, flags=re.M)[1:]:
        encabezado, _, cuerpo = bloque.partition("\n")
        if encabezado.startswith("Historial"):
            continue
        partes = [p.strip() for p in encabezado.split("·")]
        tipo = ""
        for linea in cuerpo.splitlines():
            if linea.startswith("### "):
                tipo = linea[4:].strip()
            elif linea.startswith("- "):
                versiones.append([partes[0], partes[1] if len(partes) > 1 else "", tipo, limpiar(linea[2:])])
    hoja(libro, "Versiones", "Registro de cambios por versión", "Notas de cada etiqueta v*.", versiones, {4: 100})

    listas = libro.create_sheet("Listas")
    columnas = {
        "A": ["Clase", "Menor", "Mayor", "Urgente"],
        "B": ["Estado del compromiso", "Abierto", "Cerrado", "Cancelado"],
        "C": ["Estado de la SC", "Propuesta", "En análisis", "Aprobada", "Aprobada parcialmente", "Rechazada", "Diferida", "Implementada · por ratificar", "Implementada", "Cerrada"],
    }
    for letra, valores in columnas.items():
        for fila, valor in enumerate(valores, start=1):
            celda = listas[f"{letra}{fila}"]
            celda.value = valor
            celda.font = Font(name=FUENTE, size=10, bold=fila == 1)
        listas.column_dimensions[letra].width = 30

    SALIDA.parent.mkdir(parents=True, exist_ok=True)
    libro.save(SALIDA)
    print(f"Libro generado en {SALIDA}.")


if __name__ == "__main__":
    main()
