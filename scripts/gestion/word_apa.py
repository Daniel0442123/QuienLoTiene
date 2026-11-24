#!/usr/bin/env python3
"""Genera los Word de entrega con normas APA 7 y en español de Colombia, a partir de los Markdown.

El Markdown es la fuente (PROCEDIMIENTO-DOCUMENTAL.md, sección 1); el Word es la copia para entregar.
Formato APA 7 (trabajo de estudiante): portada con el escudo de la FUP, autores, institución, curso,
docente y fecha; Times New Roman 12 a doble espacio; márgenes de 2,54 cm; sangría de 1,27 cm en la
primera línea; número de página arriba a la derecha; títulos por niveles (1 centrado en negrita,
2 a la izquierda en negrita, 3 a la izquierda en negrita y cursiva); «Tabla N» en negrita con el
título en cursiva encima y las figuras igual. Las tablas van a espacio sencillo (APA lo permite) y
las de 7 columnas o más en una página horizontal. El idioma del documento es Español (Colombia).

Uso (desde la raíz del repositorio):

    python3 scripts/gestion/word_apa.py                  # todos los documentos de la lista
    python3 scripts/gestion/word_apa.py --solo DEF       # los que contienen «DEF» en el nombre
    python3 scripts/gestion/word_apa.py --md X.md --docx X.docx   # un Markdown cualquiera
    python3 scripts/gestion/word_apa.py --normalizar A.docx B.docx  # idioma y tipografía APA en un Word existente

Necesita python-docx y Pillow (pip install python-docx pillow).
"""

from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK, WD_LINE_SPACING
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Pt, RGBColor

sys.path.insert(0, str(Path(__file__).parent))
import informes_word  # noqa: E402  (lista de documentos de QA)

ESCUDO = Path(__file__).parent / "escudo-fup.png"
FUENTE, MONO = "Times New Roman", "Consolas"
NEGRO = RGBColor(0, 0, 0)
EQUIPO = [
    "Daniel Montenegro Almendra", "Brayan Grajales Escarraga", "Monica Anyi Cajas Mellizo", "Yanimar Burbano Piamba",
    "Wilson Javier Galindez Certuche", "Francisco José Castro Muñoz", "Deiner Stiven Ramírez Vente",
]
MESES = ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre",
         "noviembre", "diciembre"]
ANCHO_V, ANCHO_H = 16.5, 22.8  # ancho útil en cm (carta vertical y horizontal con márgenes de 2,54 cm)

QA = informes_word.SALIDA
DOCUMENTOS = [(f, (QA / n).resolve().relative_to(Path.cwd().resolve())) for f, n, _ in informes_word.DOCUMENTOS] + [
    (Path("docs/gestion/BITACORA-DEL-PROYECTO.md"), Path("docs/gestion/entregables/Bitacora-del-proyecto.docx")),
    (Path("docs/gestion/CONTROL-DE-CAMBIOS.md"), Path("docs/gestion/entregables/Control-de-cambios.docx")),
    (Path("docs/gestion/REGISTRO-DE-CAMBIOS.md"), Path("docs/gestion/entregables/Registro-de-cambios-versiones.docx")),
    (Path("docs/arquitectura/DECISIONES-BACKEND.md"), Path("docs/arquitectura/Decisiones-Backend.docx")),
    (Path("docs/arquitectura/DECISIONES-FRONTEND.md"), Path("docs/arquitectura/Decisiones-Frontend.docx")),
    (Path("docs/api/CONTRATO-SPRINT-3.md"), Path("docs/api/Contrato-API-Sprint-3.docx")),
    (Path("docs/despliegue/DESPLIEGUE.md"), Path("docs/despliegue/Despliegue-en-produccion.docx")),
]

ENLINEA = re.compile(r"(!\[[^\]]*\]\([^)]+\)|\[[^\]]+\]\([^)]+\)|\*\*[^*]+\*\*|`[^`]+`|<br\s*/?>|(?<![\w*])\*[^*\s][^*]*\*(?![\w*]))")


# ---------------------------------------------------------------- utilidades de XML

def _sombra(celda, color):
    tcPr = celda._tc.get_or_add_tcPr()
    sh = OxmlElement("w:shd"); sh.set(qn("w:val"), "clear"); sh.set(qn("w:color"), "auto"); sh.set(qn("w:fill"), color)
    tcPr.append(sh)


def _campo(run, instruccion):
    for tipo, texto in (("begin", None), (None, instruccion), ("separate", None), (None, "1"), ("end", None)):
        if tipo:
            el = OxmlElement("w:fldChar"); el.set(qn("w:fldCharType"), tipo)
        elif texto == instruccion:
            el = OxmlElement("w:instrText"); el.set(qn("xml:space"), "preserve"); el.text = texto
        else:
            el = OxmlElement("w:t"); el.text = texto
        run._r.append(el)


def _sencillo(p, despues=0):
    pf = p.paragraph_format
    pf.line_spacing_rule = WD_LINE_SPACING.SINGLE; pf.space_after = Pt(despues); pf.space_before = Pt(0)
    pf.first_line_indent = Cm(0)


def _anchos(tabla, anchos):
    tabla.autofit = False
    for i, gc in enumerate(tabla._tbl.tblGrid.findall(qn("w:gridCol"))):
        gc.set(qn("w:w"), str(int(Cm(anchos[i]).twips)))
    for fila in tabla.rows:
        for i, w in enumerate(anchos):
            fila.cells[i].width = Cm(w)


def _lang(rpr):
    lang = rpr.find(qn("w:lang"))
    if lang is None:
        lang = OxmlElement("w:lang"); rpr.append(lang)
    lang.set(qn("w:val"), "es-CO"); lang.set(qn("w:eastAsia"), "es-CO"); lang.set(qn("w:bidi"), "ar-SA")


def idioma_es_co(doc):
    """Español (Colombia) en el idioma predeterminado, en los estilos y en cada texto del documento."""
    estilos = doc.styles.element
    dd = estilos.find(qn("w:docDefaults"))
    if dd is None:
        dd = OxmlElement("w:docDefaults"); estilos.insert(0, dd)
    rpd = dd.find(qn("w:rPrDefault"))
    if rpd is None:
        rpd = OxmlElement("w:rPrDefault"); dd.insert(0, rpd)
    rpr = rpd.find(qn("w:rPr"))
    if rpr is None:
        rpr = OxmlElement("w:rPr"); rpd.append(rpr)
    _lang(rpr)
    for rpr in estilos.iter(qn("w:rPr")):
        _lang(rpr)
    partes = [doc.element] + [s.header._element for s in doc.sections] + [s.footer._element for s in doc.sections]
    for parte in partes:
        for lang in parte.iter(qn("w:lang")):
            lang.set(qn("w:val"), "es-CO")
    doc.core_properties.language = "es-CO"


def fecha_larga(dd_mm_aaaa):
    d, m, a = (int(x) for x in dd_mm_aaaa.split("/"))
    return f"{d} de {MESES[m - 1]} de {a}"


# ---------------------------------------------------------------- documento APA

class APA:
    def __init__(self, titulo, subtitulo, control, fecha):
        self.d = d = Document()
        self.tablas = self.figuras = 0
        self.horizontal = False
        sec = d.sections[0]
        sec.page_width, sec.page_height = Cm(21.59), Cm(27.94)
        for m in ("left_margin", "right_margin", "top_margin", "bottom_margin"):
            setattr(sec, m, Cm(2.54))
        normal = d.styles["Normal"]
        normal.font.name = FUENTE; normal.font.size = Pt(12); normal.font.color.rgb = NEGRO
        normal.element.rPr.rFonts.set(qn("w:eastAsia"), FUENTE)
        pf = normal.paragraph_format
        pf.line_spacing_rule = WD_LINE_SPACING.DOUBLE; pf.space_after = Pt(0); pf.space_before = Pt(0)
        for nivel in (1, 2, 3):
            h = d.styles[f"Heading {nivel}"]
            h.font.name = FUENTE; h.font.size = Pt(12); h.font.bold = True; h.font.color.rgb = NEGRO
            h.font.italic = nivel == 3
            for k in ("w:ascii", "w:hAnsi", "w:eastAsia", "w:cs"):
                h.element.rPr.rFonts.set(qn(k), FUENTE)
            for k in ("w:asciiTheme", "w:hAnsiTheme", "w:eastAsiaTheme", "w:cstheme"):
                h.element.rPr.rFonts.attrib.pop(qn(k), None)
            hp = h.paragraph_format
            hp.line_spacing_rule = WD_LINE_SPACING.DOUBLE; hp.space_before = Pt(0); hp.space_after = Pt(0)
            hp.keep_with_next = True; hp.first_line_indent = Cm(0)
            hp.alignment = WD_ALIGN_PARAGRAPH.CENTER if nivel == 1 else WD_ALIGN_PARAGRAPH.LEFT
        for nombre in ("List Bullet", "List Number"):
            d.styles[nombre].font.name = FUENTE; d.styles[nombre].font.size = Pt(12)
        idioma_es_co(d)
        enc = sec.header.paragraphs[0]; enc.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        r = enc.add_run(); r.font.name = FUENTE; r.font.size = Pt(12); _campo(r, "PAGE")
        # Portada (trabajo de estudiante)
        p = d.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        if ESCUDO.exists():
            p.add_run().add_picture(str(ESCUDO), width=Cm(4))
        d.add_paragraph()
        p = d.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        self.en_linea(p, titulo, negrita=True)
        if subtitulo:
            p = d.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER; p.add_run(subtitulo)
        d.add_paragraph()
        for n in EQUIPO:
            p = d.add_paragraph(n); p.alignment = WD_ALIGN_PARAGRAPH.CENTER; _sencillo(p)
        d.add_paragraph()
        for t in ("Equipo Byte Force", "Fundación Universitaria de Popayán", "Facultad de Ingeniería de Sistemas",
                  "Ingeniería de Software III", "Docente: Andres Felipe Arboleda", fecha):
            p = d.add_paragraph(t); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        self.salto()
        if control:
            self.titulo(1, "Control del Documento")
            self.tabla(["Campo", "Valor"], control, titulo="Datos de control del documento", anchos=[4.5, 12.0])
            self.salto()
        self._titulo_tabla = "Detalle"

    # ---- estructura
    def salto(self):
        p = self.d.add_paragraph(); _sencillo(p); p.add_run().add_break(WD_BREAK.PAGE)

    def orientacion(self, horizontal):
        if horizontal == self.horizontal:
            return
        sec = self.d.add_section(WD_SECTION.NEW_PAGE)
        sec.orientation = WD_ORIENT.LANDSCAPE if horizontal else WD_ORIENT.PORTRAIT
        a, b = Cm(27.94), Cm(21.59)
        sec.page_width, sec.page_height = (a, b) if horizontal else (b, a)
        self.horizontal = horizontal

    def ancho(self):
        return ANCHO_H if self.horizontal else ANCHO_V

    def titulo(self, nivel, texto):
        p = self.d.add_heading("", min(nivel, 3))
        self.en_linea(p, texto)
        self._titulo_tabla = re.sub(r"[*`]", "", re.sub(r"^[\d.]+\s*", "", texto))

    def parrafo(self, texto, sangria=True, bloque=False):
        p = self.d.add_paragraph()
        if bloque:  # cita en bloque o nota: sangría izquierda de 1,27 cm, sin sangría de primera línea
            p.paragraph_format.left_indent = Cm(1.27)
        elif sangria:
            p.paragraph_format.first_line_indent = Cm(1.27)
        self.en_linea(p, texto)
        return p

    def vinetas(self, items, numerada=False):
        for n, (nivel, texto) in enumerate(items, 1):
            if numerada:
                p = self.d.add_paragraph()
                p.paragraph_format.left_indent = Cm(1.27 * (nivel + 1)); p.paragraph_format.first_line_indent = Cm(-0.63)
                p.add_run(f"{n}. ")
            else:
                p = self.d.add_paragraph(style="List Bullet")
                if nivel:
                    p.paragraph_format.left_indent = Cm(1.27 * (nivel + 1))
            self.en_linea(p, texto)

    def rotulo(self, tipo, numero, titulo):
        p = self.d.add_paragraph(); p.paragraph_format.keep_with_next = True; _sencillo(p, 0)
        p.paragraph_format.space_before = Pt(6)
        p.add_run(f"{tipo} {numero}").bold = True
        p = self.d.add_paragraph(); p.paragraph_format.keep_with_next = True; _sencillo(p, 6)
        p.add_run(titulo).italic = True

    # ---- texto en línea
    def en_linea(self, p, texto, tam=None, negrita=False, ancho_img=None):
        for parte in ENLINEA.split(texto):
            if not parte:
                continue
            if parte.startswith("!["):
                alt, destino = re.match(r"!\[([^\]]*)\]\(([^)]+)\)", parte).groups()
                self.imagen_en(p, destino, ancho_img or 12)
                continue
            if parte.startswith("["):
                etiqueta, _ = re.match(r"\[([^\]]+)\]\(([^)]+)\)", parte).groups()
                self.en_linea(p, etiqueta, tam=tam, negrita=negrita)
                continue
            if re.match(r"<br\s*/?>", parte):
                p.add_run().add_break(); continue
            if parte.startswith("**"):
                r = p.add_run(parte[2:-2]); r.bold = True
            elif parte.startswith("`"):
                r = p.add_run(parte[1:-1]); r.font.name = MONO
                r._element.get_or_add_rPr().get_or_add_rFonts().set(qn("w:eastAsia"), MONO)
                r.font.size = Pt((tam or 12) - 2)
                r.bold = negrita or None
                continue
            elif parte.startswith("*") and parte.endswith("*") and len(parte) > 2:
                r = p.add_run(parte[1:-1]); r.italic = True
            else:
                r = p.add_run(parte)
            if negrita:
                r.bold = True
            if tam:
                r.font.size = Pt(tam)

    def imagen_en(self, p, destino, ancho_cm):
        ruta = (self.base / destino).resolve() if hasattr(self, "base") else Path(destino)
        if not ruta.exists() or ruta.suffix.lower() not in (".png", ".jpg", ".jpeg", ".gif"):
            p.add_run(Path(destino).name).italic = True
            return
        from PIL import Image
        w, h = Image.open(ruta).size
        ancho = min(ancho_cm, 16.0 * w / h)  # alto máximo de unos 16 cm
        p.add_run().add_picture(str(ruta), width=Cm(ancho))

    # ---- bloques
    def tabla(self, cab, filas, titulo=None, anchos=None, tam=None):
        n = len(cab)
        tam = tam or (10 if n <= 4 else 9 if n <= 6 else 8)
        if anchos is None:
            anchos = self.calcular_anchos(cab, filas, tam)
        self.tablas += 1
        self.rotulo("Tabla", self.tablas, titulo or self._titulo_tabla)
        t = self.d.add_table(rows=1, cols=n); t.alignment = WD_TABLE_ALIGNMENT.CENTER; t.style = "Table Grid"
        for i, x in enumerate(cab):
            c = t.rows[0].cells[i]; _sombra(c, "E7E6E6")
            pp = c.paragraphs[0]; _sencillo(pp); self.en_linea(pp, x, tam=tam, negrita=True)
        trPr = t.rows[0]._tr.get_or_add_trPr(); h = OxmlElement("w:tblHeader"); h.set(qn("w:val"), "true"); trPr.append(h)
        for fila in filas:
            celdas = t.add_row().cells
            for i, x in enumerate(fila):
                pp = celdas[i].paragraphs[0]; _sencillo(pp)
                self.en_linea(pp, x, tam=tam, ancho_img=anchos[i] - 0.3)
        _anchos(t, anchos)
        p = self.d.add_paragraph(); _sencillo(p, 6)

    def calcular_anchos(self, cab, filas, tam):
        n = len(cab)
        limpio = lambda s: re.sub(r"!\[[^\]]*\]\([^)]+\)", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
                                  re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", s)).replace("**", "").replace("`", "")
        todas = [[limpio(c) for c in f] for f in [cab] + filas]
        cm_car = 0.23 * tam / 10
        total = self.ancho()
        minimo = [min(max(len(w) for f in todas for w in (f[k].split() or [""])) * cm_car + 0.5, total / 2.5) for k in range(n)]
        promedio = [sum(len(f[k]) for f in todas[1:]) / max(len(todas) - 1, 1) + 1 for k in range(n)]
        resto = max(total - sum(minimo), 0)
        anchos = [minimo[k] + resto * promedio[k] / sum(promedio) for k in range(n)]
        if sum(anchos) > total:
            anchos = [a * total / sum(anchos) for a in anchos]
        return anchos

    def codigo(self, texto):
        t = self.d.add_table(rows=1, cols=1); t.alignment = WD_TABLE_ALIGNMENT.CENTER; _anchos(t, [self.ancho()])
        c = t.rows[0].cells[0]; _sombra(c, "F2F2F2"); p = c.paragraphs[0]; _sencillo(p)
        lineas = texto.strip("\n").split("\n")
        for i, linea in enumerate(lineas):
            r = p.add_run(linea); r.font.name = MONO; r.font.size = Pt(9)
            r._element.get_or_add_rPr().get_or_add_rFonts().set(qn("w:eastAsia"), MONO)
            if i < len(lineas) - 1:
                r.add_break()
        p = self.d.add_paragraph(); _sencillo(p, 6)

    def figura(self, alt, destino):
        self.figuras += 1
        self.rotulo("Figura", self.figuras, alt or Path(destino).stem)
        p = self.d.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER; _sencillo(p, 6)
        self.imagen_en(p, destino, self.ancho() - 1)

    # ---- Markdown
    def markdown(self, md, base):
        self.base = base
        lineas = md.split("\n")
        i = 0
        while i < len(lineas):
            l = lineas[i].rstrip()
            if l.startswith("```"):
                j = i + 1
                while j < len(lineas) and not lineas[j].startswith("```"):
                    j += 1
                self.orientacion(False); self.codigo("\n".join(lineas[i + 1:j])); i = j + 1; continue
            m = re.match(r"^(#{2,6}) (.*)", l)
            if m:
                self.orientacion(False)
                self.titulo(len(m.group(1)) - 1, m.group(2)); i += 1; continue
            if l.startswith("|"):
                bloque = []
                while i < len(lineas) and lineas[i].startswith("|"):
                    bloque.append(lineas[i].rstrip()); i += 1
                filas = [self.celdas(b) for b in bloque if not re.match(r"^\|\s*:?-{3}", b)]
                cab, cuerpo = filas[0], filas[1:]
                n = len(cab)
                cuerpo = [f + [""] * (n - len(f)) if len(f) < n else f[:n] for f in cuerpo]
                self.orientacion(n >= 7)
                self.tabla(cab, cuerpo)
                continue
            if re.match(r"^\s*[-*+] ", l):
                items = []
                while i < len(lineas) and (re.match(r"^\s*[-*+] ", lineas[i]) or (lineas[i].startswith("  ") and lineas[i].strip() and items)):
                    t = lineas[i]
                    if re.match(r"^\s*[-*+] ", t):
                        items.append([(len(t) - len(t.lstrip())) // 2, re.sub(r"^\s*[-*+] ", "", t).strip()])
                    else:
                        items[-1][1] += " " + t.strip()
                    i += 1
                self.orientacion(False); self.vinetas(items); continue
            if re.match(r"^\s*\d+[.)] ", l):
                items = []
                while i < len(lineas) and (re.match(r"^\s*\d+[.)] ", lineas[i]) or (lineas[i].startswith("   ") and lineas[i].strip() and items)):
                    t = lineas[i]
                    if re.match(r"^\s*\d+[.)] ", t):
                        items.append([(len(t) - len(t.lstrip())) // 3, re.sub(r"^\s*\d+[.)] ", "", t).strip()])
                    else:
                        items[-1][1] += " " + t.strip()
                    i += 1
                self.orientacion(False); self.vinetas(items, numerada=True); continue
            if l.startswith(">"):
                texto = []
                while i < len(lineas) and lineas[i].startswith(">"):
                    texto.append(lineas[i].lstrip(">").strip()); i += 1
                self.orientacion(False); self.parrafo(" ".join(t for t in texto if t), bloque=True); continue
            if re.match(r"^\s*(-{3,}|\*{3,})\s*$", l) or l.strip().startswith("<!--"):
                i += 1; continue
            if re.match(r"^!\[[^\]]*\]\([^)]+\)\s*$", l.strip()):
                alt, destino = re.match(r"^!\[([^\]]*)\]\(([^)]+)\)", l.strip()).groups()
                self.orientacion(False); self.figura(alt, destino); i += 1; continue
            if l.strip():
                texto = [l.strip()]
                i += 1
                while i < len(lineas) and lineas[i].strip() and not re.match(r"^(#|\||>|```|\s*[-*+] |\s*\d+[.)] |!\[)", lineas[i]):
                    texto.append(lineas[i].strip()); i += 1
                self.orientacion(False); self.parrafo(" ".join(texto)); continue
            i += 1
        self.orientacion(False)

    @staticmethod
    def celdas(linea):
        s = linea.strip()
        s = s[1:] if s.startswith("|") else s
        s = s[:-1] if s.endswith("|") and not s.endswith("\\|") else s
        partes, actual, codigo = [], "", False
        for ch in s:  # respeta las barras dentro de `código` y las escapadas
            if ch == "`":
                codigo = not codigo
            if ch == "|" and not codigo and not actual.endswith("\\"):
                partes.append(actual.strip()); actual = ""
            else:
                actual += ch
        partes.append(actual.strip())
        return [p.replace("\\|", "|") for p in partes]

    def guardar(self, ruta, titulo, fuente):
        cp = self.d.core_properties
        cp.title = titulo; cp.author = "Equipo Byte Force · ¿Quién lo tiene?"
        cp.comments = f"Generado desde {fuente} con scripts/gestion/word_apa.py (APA 7, es-CO)"
        Path(ruta).parent.mkdir(parents=True, exist_ok=True)
        self.d.save(ruta)


def control_desde(linea):
    """Convierte la línea «> **Control del documento** · Versión 1.0 · 24/11/2026 · Responsable: … » en filas."""
    partes = [p.strip() for p in re.sub(r"^>\s*\*\*Control del documento\*\*\s*·?", "", linea).split(" · ")]
    filas, fecha = [], None
    for p in partes:
        if not p:
            continue
        if p.startswith("Versión "):
            filas.append(["Versión", p[8:]])
        elif re.fullmatch(r"\d{2}/\d{2}/\d{4}", p):
            filas.append(["Fecha", p]); fecha = p
        elif ":" in p:
            k, v = p.split(":", 1)
            filas.append([k.strip(), v.strip()])
        else:
            filas.append(["Nota", p])
    return filas, fecha


def generar(fuente: Path, salida: Path, subtitulo="Proyecto ¿Quién lo tiene?"):
    md = fuente.read_text(encoding="utf-8")
    lineas = md.split("\n")
    titulo = next((l[2:].strip() for l in lineas if l.startswith("# ")), fuente.stem)
    cuerpo, control, fecha = [], [], None
    encontrado_titulo = False
    for l in lineas:
        if l.startswith("# ") and not encontrado_titulo:
            encontrado_titulo = True; continue
        if l.startswith("> **Control del documento**") and not control:
            control, fecha = control_desde(l); continue
        cuerpo.append(l)
    doc = APA(titulo, subtitulo, control, fecha_larga(fecha) if fecha else "24 de noviembre de 2026")
    doc.markdown("\n".join(cuerpo), fuente.parent)
    doc.guardar(salida, re.sub(r"[*`]", "", titulo), fuente)


# ---------------------------------------------------------------- Word existentes

def normalizar(ruta: Path):
    """Lleva un Word existente a la tipografía APA (Times New Roman 12, negro, márgenes de 2,54 cm,
    número de página arriba a la derecha) y al idioma Español (Colombia), sin cambiar su contenido."""
    doc = Document(ruta)
    mono = ("consolas", "courier", "courier new", "menlo", "monaco", "liberation mono", "dejavu sans mono")
    for st in doc.styles.element.iter(qn("w:rFonts")):
        if (st.get(qn("w:ascii")) or "").lower() not in mono:
            for k in ("w:ascii", "w:hAnsi", "w:eastAsia", "w:cs"):
                st.set(qn(k), FUENTE)
            for k in ("w:asciiTheme", "w:hAnsiTheme", "w:eastAsiaTheme", "w:cstheme"):
                if st.get(qn(k)) is not None:
                    del st.attrib[qn(k)]
    for nombre in ("Normal", "Heading 1", "Heading 2", "Heading 3", "Title", "Subtitle"):
        try:
            s = doc.styles[nombre]
        except KeyError:
            continue
        s.font.name = FUENTE; s.font.color.rgb = NEGRO
        s.font.size = Pt(12) if nombre != "Title" else Pt(14)
        if nombre.startswith("Heading"):
            s.font.bold = True; s.font.italic = nombre == "Heading 3"
    for sec in doc.sections:
        for m in ("left_margin", "right_margin", "top_margin", "bottom_margin"):
            setattr(sec, m, Cm(2.54))
        enc = sec.header
        if not enc.is_linked_to_previous:
            for p in enc.paragraphs:
                for r in p.runs:
                    r.font.name = FUENTE; r.font.color.rgb = NEGRO
            if "PAGE" not in enc._element.xml:
                p = enc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
                r = p.add_run(); r.font.name = FUENTE; r.font.size = Pt(12); _campo(r, "PAGE")
    for r in doc.element.body.iter(qn("w:r")):
        rpr = r.find(qn("w:rPr"))
        if rpr is None:
            continue
        f = rpr.find(qn("w:rFonts"))
        if f is not None and (f.get(qn("w:ascii")) or "").lower() not in mono:
            for k in ("w:ascii", "w:hAnsi", "w:eastAsia", "w:cs"):
                f.set(qn(k), FUENTE)
    # Texto en negro (APA), salvo el blanco sobre los encabezados de tabla con fondo oscuro
    partes = [doc.element.body, doc.styles.element] + [s.header._element for s in doc.sections]
    for parte in partes:
        for c in parte.iter(qn("w:color")):
            if (c.get(qn("w:val")) or "").upper() == "FFFFFF":
                continue
            c.set(qn("w:val"), "000000")
            for k in ("w:themeColor", "w:themeShade", "w:themeTint"):
                c.attrib.pop(qn(k), None)
    idioma_es_co(doc)
    doc.save(ruta)


def main() -> int:
    a = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    a.add_argument("--solo", help="solo los documentos cuyo nombre contiene este texto")
    a.add_argument("--md", type=Path); a.add_argument("--docx", type=Path)
    a.add_argument("--normalizar", nargs="+", type=Path)
    o = a.parse_args()
    if o.normalizar:
        for r in o.normalizar:
            normalizar(r); print(f"normalizado {r}")
        return 0
    if o.md:
        generar(o.md, o.docx or o.md.with_suffix(".docx")); print(f"generado {o.docx}")
        return 0
    for fuente, salida in DOCUMENTOS:
        if o.solo and o.solo not in str(salida):
            continue
        generar(fuente, salida); print(f"generado {salida}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
