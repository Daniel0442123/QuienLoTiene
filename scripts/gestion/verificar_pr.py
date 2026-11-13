#!/usr/bin/env python3
"""Revisa que la descripción de un pull request cumpla el control de cambios.

Lo corre el flujo .github/workflows/gestion.yml en cada pull request. Lee la
descripción de la variable de entorno PR_BODY (nunca se interpola en el shell)
y comprueba:

1. Que la sección «Historia» diga qué HU, tarea o motivo atiende.
2. Que la sección «Control de cambios» marque exactamente una opción:
   «No se desvía del plan» o «Se desvía del plan».
3. Que, si se desvía, cite al menos una solicitud SC-nnn y que cada SC citada
   exista en docs/gestion/CONTROL-DE-CAMBIOS.md.
4. Que la sección «Documentos actualizados» no esté vacía.

Procedimiento: docs/gestion/CONTROL-DE-CAMBIOS.md.
"""

from __future__ import annotations

import os
import re
import sys
from pathlib import Path

REGISTRO = Path("docs/gestion/CONTROL-DE-CAMBIOS.md")
MARCA = r"-\s*\[[xX]\]\s*"


def seccion(cuerpo: str, titulo: str) -> str | None:
    coincidencia = re.search(
        rf"^##\s+{re.escape(titulo)}[^\n]*\n(.*?)(?=^##\s|\Z)", cuerpo, re.MULTILINE | re.DOTALL
    )
    if not coincidencia:
        return None
    sin_comentarios = re.sub(r"<!--.*?-->", "", coincidencia.group(1), flags=re.DOTALL)
    sin_vinetas_vacias = re.sub(r"^\s*[-*]\s*$", "", sin_comentarios, flags=re.MULTILINE)
    return sin_vinetas_vacias.strip()


def main() -> int:
    cuerpo = os.environ.get("PR_BODY", "")
    errores: list[str] = []

    historia = seccion(cuerpo, "Historia")
    if not historia:
        errores.append("La sección «## Historia» falta o está vacía: indique la HU, la tarea (T-nn) o el motivo.")

    control = seccion(cuerpo, "Control de cambios")
    if control is None:
        errores.append("Falta la sección «## Control de cambios» de la plantilla del pull request.")
    else:
        no_se_desvia = re.search(MARCA + r"No se desvía del plan", control) is not None
        se_desvia = re.search(MARCA + r"Se desvía del plan", control) is not None
        if no_se_desvia == se_desvia:
            errores.append("En «Control de cambios» marque exactamente una opción: «No se desvía del plan» o «Se desvía del plan».")
        if se_desvia:
            citadas = sorted(set(re.findall(r"SC-\d{3}", control)))
            if not citadas:
                errores.append("El PR se desvía del plan pero no cita ninguna solicitud de cambio (SC-nnn).")
            registro = REGISTRO.read_text(encoding="utf-8") if REGISTRO.exists() else ""
            for solicitud in citadas:
                if not re.search(rf"^\|\s*{solicitud}\s*\|", registro, re.MULTILINE):
                    errores.append(f"{solicitud} no está registrada en {REGISTRO}. Regístrela en este mismo PR.")

    documentos = seccion(cuerpo, "Documentos actualizados")
    if not documentos:
        errores.append("La sección «## Documentos actualizados» falta o está vacía (escriba «Ninguno» si no aplica).")

    if errores:
        print("El pull request no cumple el control de cambios:\n")
        for error in errores:
            print(f"  - {error}")
        print("\nProcedimiento: docs/gestion/CONTROL-DE-CAMBIOS.md")
        return 1
    print("Control de cambios del pull request: correcto.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
