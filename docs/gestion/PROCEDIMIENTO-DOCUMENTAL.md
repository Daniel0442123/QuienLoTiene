# Procedimiento documental · Quién lo tiene

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Wilson Javier Galindez Certuche (Product Owner · documentador) · Aprobó: Pedro (dueño del proyecto), 13/11/2026 · Estado: Vigente

Cómo se identifica, versiona, revisa y conserva cada documento del proyecto, en Markdown y en sus versiones Word, Excel y PDF, y qué norma cubre cada parte.

Normas que atiende: ISO 9001:2015 (7.5 información documentada), ISO/IEC/IEEE 12207:2017 (6.3.6 gestión de la información), ISO/IEC/IEEE 15289:2019 (contenido de los elementos de información del ciclo de vida), ISO 10007:2017.

---

## 1. Fuente de verdad

- **El Markdown en Git es la fuente.** Los Word, Excel y PDF de `entregables/` son copias para entregar o imprimir y deben decir la misma versión que su MD.
- Si un cambio toca un MD que tiene versión Word o Excel, se actualizan **en el mismo PR**. Si no se puede (por ejemplo, exportar un PDF), queda un compromiso abierto en la bitácora con responsable y fecha.
- Nada se borra del historial: un documento que deja de usarse queda con estado «Obsoleto» y el motivo.

## 2. Encabezado de control

Todo documento en `docs/` empieza, después del título, con este bloque:

```markdown
> **Control del documento** · Versión 1.2 · 13/11/2026 · Responsable: <nombre (rol)> · Estado: Vigente
```

Opcionales: `Aprobó: <nombre>, <fecha>` y `Normas: …`. En una plantilla el bloque dice «Plantilla versión»; al copiarla para un documento nuevo, se reemplazan su encabezado y su historial por los del documento nuevo. Estados: `Borrador`, `En revisión`, `Vigente`, `Obsoleto`. En los Word, el mismo bloque va en la portada o en la tabla de control del documento.

## 3. Historial del documento

Todo documento termina con:

```markdown
## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 07/11/2026 | Nombre (@cuenta) | Qué cambió y por qué, con la SC o el PR si aplica |
```

- Una fila por cambio fusionado en `main`. La más reciente al final.
- Los historiales que se agregaron el 13/11/2026 se reconstruyeron desde `git log` del archivo: cada commit que lo tocó es una fila, con su PR.

## 4. Numeración de versiones

| Cambio | Versión | Ejemplo |
|---|---|---|
| Corrección de forma, enlaces u ortografía | No cambia (se anota en el historial) | 1.2 |
| Contenido nuevo o ajustado sin cambiar lo aprobado | Sube el menor | 1.2 → 1.3 |
| Cambia algo aprobado (con SC) o se reestructura | Sube el mayor y requiere aprobación | 1.3 → 2.0 |
| Borrador antes de la primera aprobación | 0.x | 0.1, 0.2 |

## 5. Revisión y aprobación

- Todo cambio de documento entra por PR con la plantilla del repositorio y queda aprobado con una revisión «Approve» en GitHub (SC-010).
- Documentos que aprueba el dueño del proyecto: decisiones de arquitectura (`DECISIONES-*.md`), plan de proyecto, este procedimiento y el control de cambios.
- Documentos de pruebas: los aprueba el líder de QA; el Plan Maestro, además, el Product Owner.

## 6. Versiones del producto y etiquetas

- SemVer con etiquetas **anotadas**: `git tag -a v0.3.0 -m "Sprint 2: …"` y `git push origin v0.3.0`.
- Antes de etiquetar: actualizar `REGISTRO-DE-CAMBIOS.md` y regenerar la bitácora.

## 7. Conservación

- Git conserva todas las versiones de los MD y de los binarios.
- Las evidencias de pruebas se guardan en `docs/pruebas/evidencias/Cn/<fecha>/` y no se modifican después de citarlas en un informe.
- Datos personales: nunca en documentos ni evidencias (Ley 1581 de 2012).

## 8. Matriz de normas

Qué norma respalda cada parte del seguimiento, y dónde queda la evidencia.

| Norma | Qué pide | Dónde se cumple |
|---|---|---|
| ISO/IEC/IEEE 12207:2017, 6.3.2 | Seguimiento y control del proyecto | `BITACORA-DEL-PROYECTO.md`, informes de corte |
| ISO/IEC/IEEE 12207:2017, 6.3.5 · ISO 10007:2017 | Identificación, control de cambios, registro del estado y auditoría de la configuración | `CONTROL-DE-CAMBIOS.md`, etiquetas `v*`, `REGISTRO-DE-CAMBIOS.md`, protección de `main` |
| ISO/IEC/IEEE 12207:2017, 6.3.6 · ISO/IEC/IEEE 15289:2019 | Gestión de la información y contenido de los documentos | Este procedimiento, encabezados e historiales |
| ISO 9001:2015, 6.3 y 8.5.6 | Planificar y controlar los cambios | `CONTROL-DE-CAMBIOS.md` |
| ISO 9001:2015, 7.5.3 | Control de la información documentada (versión, aprobación, conservación) | Secciones 2 a 7 de este documento |
| ISO/IEC/IEEE 29119-2 y 29119-3 · IEEE 829 | Proceso y documentación de pruebas | `docs/pruebas/` (Plan Maestro, planes por ciclo, libro, plantillas) |
| ISO/IEC 25010 | Modelo de calidad del producto | Requisitos no funcionales y casos NF |
| ISO/IEC/IEEE 29148:2018 | Requisitos | SRS, trazabilidad |
| ISO 9241-11 · WCAG 2.2 AA | Usabilidad y accesibilidad | `DECISIONES-FRONTEND.md`, checklist de accesibilidad |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión (SC-009) |
