#!/usr/bin/env python3
"""Genera los Word de entrega de QA a partir de los Markdown del repositorio.

El Markdown es la fuente (PROCEDIMIENTO-DOCUMENTAL.md, sección 1); el Word es la
copia para entregar. Usa como base el formato de los Word del corte del
Sprint 1 (portada, encabezado, pie con número de página, títulos y tablas con
los colores del proyecto) y escribe las capturas dentro del documento.

Uso (desde la raíz del repositorio):

    python3 scripts/gestion/informes_word.py            # todos los del ciclo C1
    python3 scripts/gestion/informes_word.py --solo DEF  # los que contienen «DEF»

Necesita python-docx y Pillow (pip install python-docx pillow).
"""

from __future__ import annotations

import argparse
import re
from pathlib import Path

from docx import Document
from docx.enum.text import WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor
from PIL import Image

INFORMES = Path("docs/pruebas/informes/C1")
DEFECTOS = Path("docs/pruebas/defectos")
SALIDA = Path("docs/pruebas/entregables/word/C1")
BASE = SALIDA / "Informe-de-Corte-Sprint-1-QuienLoTiene.docx"

AZUL, TURQUESA, GRIS, BORDE = "00486B", "02C2D4", "5F6B73", "C9D6DD"
FUENTE, MONO = "Calibri", "Consolas"

# (fuente Markdown, Word de salida, texto del encabezado de página)
DOCUMENTOS = [
    (INFORMES / "INFORME-DE-CIERRE-C1.md", "Informe-de-Cierre-C1-QuienLoTiene.docx",
     "Quién lo tiene · Informe de cierre · Ciclo 1"),
    (INFORMES / "EVIDENCIAS-C1.md", "Evidencias-de-Pruebas-C1-QuienLoTiene.docx",
     "Quién lo tiene · Evidencias de pruebas · Ciclo 1"),
    (INFORMES / "ANALISIS-DE-DEFECTOS-C1.md", "Analisis-de-Defectos-C1-QuienLoTiene.docx",
     "Quién lo tiene · Análisis de defectos y principios de las pruebas"),
    (INFORMES / "RECORRIDO-SPRINT-2-20261116.md", "Recorrido-Sprint-2-20261116-QuienLoTiene.docx",
     "Quién lo tiene · Recorrido de punta a punta · Sprint 2"),
    (INFORMES / "C-04-DIFERENCIAS-TIPOS.md", "C-04-Diferencias-de-Tipos-QuienLoTiene.docx",
     "Quién lo tiene · Compromiso C-04 · Tipos del frontend"),
] + [
    (DEFECTOS / f"DEF-{n:03d}.md", f"Reporte-de-Defecto-DEF-{n:03d}-QuienLoTiene.docx",
     "Quién lo tiene · Reporte de defecto")
    for n in range(1, 11)
]

TOKENS = re.compile(
    r"(!\[[^\]]*\]\([^)]+\)|\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|`[^`]+`|<br\s*/?>|(?<![\w*])\*[^*\s][^*]*\*(?![\w*]))"
)


class Generador:
    def __init__(self, fuente: Path, encabezado: str) -> None:
        self.fuente = fuente
        self.doc = Document(BASE)
        cuerpo = self.doc.element.body
        for hijo in list(cuerpo):
            if hijo.tag != qn("w:sectPr"):
                cuerpo.remove(hijo)
        textos = self.doc.sections[0].header._element.findall(".//" + qn("w:t"))
        for i, texto in enumerate(textos):
            texto.text = encabezado if i == 0 else ""

    # ---------- texto en línea ----------

    def ruta(self, destino: str) -> Path:
        return (self.fuente.parent / destino).resolve()

    def corrido(self, parrafo, texto: str, *, negrita=False, cursiva=False, mono=False, color=None, tam=None):
        run = parrafo.add_run(texto)
        run.bold = negrita or None
        run.italic = cursiva or None
        run.font.name = MONO if mono else FUENTE
        run._element.get_or_add_rPr().get_or_add_rFonts().set(qn("w:eastAsia"), MONO if mono else FUENTE)
        if color:
            run.font.color.rgb = RGBColor.from_string(color)
        if tam:
            run.font.size = Pt(tam)
        return run

    def en_linea(self, parrafo, texto: str, *, tam=None, color=None, ancho_img=None, negrita=False) -> None:
        for parte in TOKENS.split(texto):
            if not parte:
                continue
            if parte.startswith("!["):
                destino = re.match(r"!\[[^\]]*\]\(([^)]+)\)", parte).group(1)
                self.imagen(parrafo, destino, ancho_img)
            elif parte.startswith("["):
                etiqueta, destino = re.match(r"\[([^\]]+)\]\(([^)]+)\)", parte).groups()
                self.en_linea(parrafo, etiqueta, tam=tam, color=color, negrita=negrita)
                if not destino.startswith(("http", "#")) and "." not in etiqueta and "/" not in etiqueta:
                    self.corrido(parrafo, f" ({self.relativa(destino)})", color=GRIS, tam=(tam or 11) - 1.5)
            elif parte.startswith("**"):
                self.corrido(parrafo, parte[2:-2], negrita=True, tam=tam, color=color)
            elif parte.startswith("`"):
                self.corrido(parrafo, parte[1:-1], mono=True, tam=(tam or 11) - 1, color=color, negrita=negrita)
            elif parte.startswith("<br"):
                parrafo.add_run().add_break(WD_BREAK.LINE)
            elif parte.startswith("*") and parte.endswith("*") and len(parte) > 2:
                self.corrido(parrafo, parte[1:-1], cursiva=True, tam=tam, color=color, negrita=negrita)
            else:
                self.corrido(parrafo, parte, tam=tam, color=color, negrita=negrita)

    def relativa(self, destino: str) -> str:
        try:
            return str(self.ruta(destino).relative_to(Path.cwd().resolve() / "docs/pruebas"))
        except ValueError:
            return destino

    def imagen(self, parrafo, destino: str, ancho_max_cm=None) -> None:
        archivo = self.ruta(destino)
        if not archivo.is_file():
            self.corrido(parrafo, f"[falta la imagen {destino}]", color="B00020")
            return
        ancho_px, alto_px = Image.open(archivo).size
        telefono = ancho_px < 1000
        ancho = ancho_max_cm or (6.5 if telefono else 16.0)
        if ancho_max_cm and not telefono:
            ancho = ancho_max_cm * 1.6
        alto = ancho * alto_px / ancho_px
        tope = 21.0 if not ancho_max_cm else 14.0
        if alto > tope:
            ancho, alto = ancho * tope / alto, tope
        parrafo.add_run().add_picture(str(archivo), width=Cm(ancho))

    # ---------- bloques ----------

    def parrafo(self, texto: str, estilo=None, **formato):
        """``estilo`` es el id del estilo en la base (Heading1, ListParagraph…)."""
        parrafo = self.doc.add_paragraph()
        if estilo:
            p_style = OxmlElement("w:pStyle")
            p_style.set(qn("w:val"), estilo)
            parrafo._p.get_or_add_pPr().insert(0, p_style)
        self.en_linea(parrafo, texto, **formato)
        return parrafo

    def portada(self, titulo: str, citas: list[str]) -> None:
        kicker = "ASEGURAMIENTO DE LA CALIDAD"
        p = self.doc.add_paragraph()
        self.corrido(p, kicker, negrita=True, color=TURQUESA, tam=11)
        p = self.doc.add_paragraph()
        self.en_linea(p, titulo, tam=20, color=AZUL, negrita=True)
        p = self.doc.add_paragraph()
        self.corrido(p, "Quién lo tiene · Liberación 1.0", color=GRIS, tam=12)
        for cita in citas:
            self.parrafo(cita, tam=9.5, color=GRIS)
        self.doc.add_paragraph()

    def celda(self, celda, texto: str, encabezado: bool) -> None:
        tc_pr = celda._tc.get_or_add_tcPr()
        bordes = OxmlElement("w:tcBorders")
        for lado in ("top", "left", "bottom", "right"):
            borde = OxmlElement(f"w:{lado}")
            borde.set(qn("w:val"), "single")
            borde.set(qn("w:sz"), "4")
            borde.set(qn("w:color"), BORDE)
            bordes.append(borde)
        tc_pr.append(bordes)
        if encabezado:
            sombra = OxmlElement("w:shd")
            sombra.set(qn("w:val"), "clear")
            sombra.set(qn("w:color"), "auto")
            sombra.set(qn("w:fill"), AZUL)
            tc_pr.append(sombra)
        margenes = OxmlElement("w:tcMar")
        for lado, valor in (("top", "50"), ("left", "90"), ("bottom", "50"), ("right", "90")):
            margen = OxmlElement(f"w:{lado}")
            margen.set(qn("w:w"), valor)
            margen.set(qn("w:type"), "dxa")
            margenes.append(margen)
        tc_pr.append(margenes)
        parrafo = celda.paragraphs[0]
        parrafo.paragraph_format.space_after = Pt(1)
        parrafo.paragraph_format.space_before = Pt(0)
        self.en_linea(parrafo, texto, tam=9.5, color="FFFFFF" if encabezado else None, negrita=encabezado,
                      ancho_img=5.0)

    def tabla(self, filas: list[list[str]]) -> None:
        columnas = max(len(f) for f in filas)
        tabla = self.doc.add_table(rows=len(filas), cols=columnas)
        tbl_pr = tabla._tbl.tblPr
        ancho = OxmlElement("w:tblW")
        ancho.set(qn("w:type"), "pct")
        ancho.set(qn("w:w"), "5000")
        tbl_pr.append(ancho)
        encabezado = tabla.rows[0]._tr.get_or_add_trPr()
        encabezado.append(OxmlElement("w:tblHeader"))
        for i, fila in enumerate(filas):
            for j in range(columnas):
                self.celda(tabla.cell(i, j), fila[j] if j < len(fila) else "", i == 0)
        self.doc.add_paragraph()

    def convertir(self, salida: Path) -> None:
        lineas = self.fuente.read_text(encoding="utf-8").splitlines()
        titulo, citas, i = "", [], 0
        while i < len(lineas) and not lineas[i].startswith("## ") and not lineas[i].startswith("|"):
            linea = lineas[i]
            if linea.startswith("# "):
                titulo = linea[2:].strip()
            elif linea.startswith(">"):
                citas.append(linea.lstrip("> ").strip())
            elif linea.strip() and titulo:
                break
            i += 1
        self.portada(titulo, citas)
        parrafo: list[str] = []
        codigo: list[str] | None = None

        def vaciar() -> None:
            if parrafo:
                self.parrafo(" ".join(parrafo))
                parrafo.clear()

        while i < len(lineas):
            linea = lineas[i]
            if codigo is not None:
                if linea.startswith("```"):
                    p = self.doc.add_paragraph()
                    self.corrido(p, "\n".join(codigo), mono=True, tam=8.5)
                    codigo = None
                else:
                    codigo.append(linea)
                i += 1
                continue
            if linea.startswith("```"):
                vaciar()
                codigo = []
            elif linea.startswith("|"):
                vaciar()
                filas = []
                while i < len(lineas) and lineas[i].startswith("|"):
                    celdas = [c.strip() for c in re.split(r"(?<!\\)\|", lineas[i].strip())[1:-1]]
                    if not all(re.fullmatch(r":?-+:?", c) for c in celdas):
                        filas.append([c.replace("\\|", "|") for c in celdas])
                    i += 1
                self.tabla(filas)
                continue
            elif m := re.match(r"(#{2,4}) (.*)", linea):
                vaciar()
                self.parrafo(m.group(2), f"Heading{len(m.group(1)) - 1}")
            elif re.match(r"\s*[-*] ", linea):
                vaciar()
                self.parrafo("• " + re.sub(r"\s*[-*] ", "", linea, count=1), "ListParagraph")
            elif re.match(r"\s*\d+\. ", linea):
                vaciar()
                self.parrafo(linea.strip())
            elif linea.startswith(">"):
                vaciar()
                self.parrafo(linea.lstrip("> ").strip(), tam=9.5, color=GRIS)
            elif not linea.strip() or linea.strip() == "---" or linea.strip().startswith("<!--"):
                vaciar()
            elif re.fullmatch(r"\s*!\[[^\]]*\]\([^)]+\)\s*", linea):
                vaciar()
                anterior = self.doc.paragraphs[-1] if self.doc.paragraphs else None
                if anterior is not None and anterior.text.strip():
                    anterior.paragraph_format.keep_with_next = True  # el pie de la captura va con su imagen
                self.parrafo(linea.strip())
            else:
                parrafo.append(linea.strip())
            i += 1
        vaciar()
        propiedades = self.doc.core_properties
        propiedades.title = re.sub(r"[*`]", "", titulo)
        propiedades.author = "Quién lo tiene · Aseguramiento de la calidad"
        propiedades.comments = f"Generado desde {self.fuente} con scripts/gestion/informes_word.py"
        self.doc.save(salida)


def main() -> int:
    argumentos = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    argumentos.add_argument("--solo", help="solo los documentos cuyo nombre contiene este texto")
    opciones = argumentos.parse_args()
    for fuente, nombre, encabezado in DOCUMENTOS:
        if opciones.solo and opciones.solo not in nombre:
            continue
        salida = SALIDA / nombre
        Generador(fuente, encabezado).convertir(salida)
        print(f"{fuente} → {salida}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
