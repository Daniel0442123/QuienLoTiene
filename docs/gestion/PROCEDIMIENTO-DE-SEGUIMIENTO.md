# Procedimiento de seguimiento · Quién lo tiene

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Brayan Grajales Escarraga (Scrum Master) · Aprobó: Pedro (dueño del proyecto), 13/11/2026 · Estado: Vigente

Qué registra cada integrante, cuándo y dónde, para que siempre se pueda responder **qué se hizo, cuándo, quién lo hizo, cómo se hizo y qué falta**.

Normas que atiende: ISO/IEC/IEEE 12207:2017 (6.3.2 evaluación y control del proyecto, 6.3.7 medición), ISO 9001:2015 (9.1 seguimiento, medición, análisis y evaluación).

---

## 1. Dónde queda cada cosa

| Pregunta | Dónde se responde | Quién lo registra |
|---|---|---|
| ¿Qué se hizo y cómo? | Descripción del PR (plantilla) y commit con Conventional Commits | Autor del PR |
| ¿Cuándo y quién? | `BITACORA-DEL-PROYECTO.md`, sección 2 (generada desde GitHub) | Scrum Master, con `scripts/gestion/bitacora.py` |
| ¿Quién lo aprobó? | Revisión «Approve» del PR; columna «Aprobación formal» de la bitácora | Arquitecto de software (o el segundo revisor, SC-010) |
| ¿Se salió del plan? | `CONTROL-DE-CAMBIOS.md` (SC-nnn) y sección «Control de cambios» del PR | Quien detecta la desviación |
| ¿Qué se decidió fuera de GitHub? | `BITACORA-DEL-PROYECTO.md`, sección 3 | Scrum Master |
| ¿Qué quedó pendiente? | `BITACORA-DEL-PROYECTO.md`, sección 4 | Autor del PR o Scrum Master |
| ¿Qué trae cada versión? | `REGISTRO-DE-CAMBIOS.md` | Quien crea la etiqueta |
| ¿Cómo van las pruebas? | `docs/pruebas/libro/` y su Excel, informes de corte | QA |
| ¿Qué versión tiene cada documento? | Encabezado e historial de cada documento | Autor del cambio |

## 2. Rutina

| Momento | Qué se hace | Responsable |
|---|---|---|
| Al abrir un PR | Llenar la plantilla completa: historia, qué cambia, control de cambios, documentos actualizados, pruebas. Asignarse el PR, ponerle la etiqueta del tipo y el hito del sprint | Autor |
| Al revisar un PR | Comprobar que la SC exista y que los documentos afectados subieron de versión; aprobar con «Approve» citando la SC si la hay | Arquitecto (o segundo revisor) |
| Al fusionar | Solo con la revisión aprobada y los chequeos en verde (CI, CI frontend, Control de cambios) | Quien aprobó |
| Al terminar el día (si hubo fusiones) | Regenerar la bitácora, anotar los hechos fuera de PR y actualizar compromisos | Scrum Master |
| En la daily | Repasar los compromisos abiertos vencidos y las SC en análisis | Scrum Master |
| Antes de etiquetar una versión | Actualizar `REGISTRO-DE-CAMBIOS.md`, regenerar la bitácora, etiqueta anotada | Arquitecto |
| En el corte de sprint | Informe de corte (QA), revisión de SC abiertas con el comité, cierre de compromisos | Scrum Master, líder de QA, Product Owner |

## 3. Regenerar la bitácora

```bash
# Token de GitHub de solo lectura (permiso «Pull requests: read» y «Contents: read»)
export GITHUB_TOKEN=...
python3 scripts/gestion/bitacora.py
git switch -c docs/bitacora-AAAAMMDD
git commit -am "docs(gestion): actualiza la bitácora al DD/MM"
```

El script reemplaza solo la tabla entre los marcadores `inicio-bitacora` y `fin-bitacora`. Las secciones 3 y 4 se editan a mano.

## 4. Etiquetas, hitos y responsables en GitHub

- **Hito** por sprint: `Sprint 1`, `Sprint 2`, `Sprint 3`, `Cierre`.
- **Etiquetas**: `backend`, `frontend`, `documentacion`, `despliegue`, `pruebas`, `seguridad` y `desviacion` (cuando el PR cita una SC).
- **Responsable** (*assignee*): el autor. **Revisor solicitado**: el arquitecto o el segundo revisor (SC-010).

## 5. Equipo y cuentas

| Integrante | Rol | Cuenta de GitHub |
|---|---|---|
| Daniel Montenegro Almendra | Arquitecto de software · backend | @Daniel0442123 (también @daniel0442 en los primeros commits) |
| Monica Anyi Cajas Mellizo | UX · frontend | @monicacajas-art |
| Deiner Stiven Ramírez Vente | Backend | Por registrar |
| Yanimar Burbano Piamba | Líder de QA | Por registrar |
| Francisco José Castro Muñoz | QA no funcional y seguridad | Por registrar |
| Wilson Javier Galindez Certuche | Product Owner · documentador | Por registrar |
| Brayan Grajales Escarraga | Scrum Master | Por registrar |

Cuando un integrante empiece a usar GitHub, se agrega su cuenta aquí y en `INTEGRANTES` de `scripts/gestion/bitacora.py`.

## 6. Indicadores

Se revisan en cada corte con la bitácora y el control de cambios:

| Indicador | Meta |
|---|---|
| PR fusionados con aprobación registrada | 100 % desde el 13/11/2026 |
| PR con la sección «Control de cambios» completa | 100 % (lo exige el chequeo de CI) |
| SC en estado «por ratificar» al cierre de cada sprint | 0 |
| Compromisos abiertos vencidos | 0 |
| Documentos con encabezado e historial | 100 % de `docs/` |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión (SC-009) |
