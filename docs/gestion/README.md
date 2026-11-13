# Gestión del proyecto · Seguimiento y control de cambios

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Brayan Grajales Escarraga (Scrum Master) · Aprobó: Pedro (dueño del proyecto), 13/11/2026 · Estado: Vigente

Aquí se responde, para todo el proyecto, **qué se hizo, cuándo, quién lo hizo, cómo lo hizo y qué se apartó del plan**. Lo que es propio de las pruebas sigue en `docs/pruebas/`.

| Documento | Para qué | Norma principal |
|---|---|---|
| [BITACORA-DEL-PROYECTO.md](BITACORA-DEL-PROYECTO.md) | Qué se hizo, cuándo y quién: un PR por fila, hechos fuera de los PR y compromisos abiertos | ISO/IEC/IEEE 12207, 6.3.2 |
| [CONTROL-DE-CAMBIOS.md](CONTROL-DE-CAMBIOS.md) | Procedimiento y registro de las solicitudes de cambio (desviaciones del plan) | ISO 10007 · ISO 9001, 6.3 y 8.5.6 |
| [REGISTRO-DE-CAMBIOS.md](REGISTRO-DE-CAMBIOS.md) | Notas de cada versión `v*` del producto | ISO/IEC/IEEE 12207, 6.3.5 |
| [PROCEDIMIENTO-DE-SEGUIMIENTO.md](PROCEDIMIENTO-DE-SEGUIMIENTO.md) | Rutina: quién registra qué y cuándo; equipo y cuentas; indicadores | ISO 9001, 9.1 |
| [PROCEDIMIENTO-DOCUMENTAL.md](PROCEDIMIENTO-DOCUMENTAL.md) | Encabezado, historial, versiones, aprobación y conservación de los documentos; matriz de normas | ISO 9001, 7.5 · ISO/IEC/IEEE 15289 |
| [plantillas/SOLICITUD-DE-CAMBIO.md](plantillas/SOLICITUD-DE-CAMBIO.md) | Plantilla de solicitud de cambio mayor | ISO 10007 |
| [solicitudes/](solicitudes/) | Una solicitud de cambio mayor por archivo | ISO 10007 |
| `entregables/Formato-Solicitud-de-Cambio.docx` | La plantilla en Word, con el mismo estilo de los formatos de QA | — |
| `entregables/Control-de-Cambios-QuienLoTiene.xlsx` | Bitácora, hechos, compromisos, hallazgos, solicitudes y versiones en Excel | — |

## Automatización

| Herramienta | Qué hace |
|---|---|
| `.github/pull_request_template.md` | Pide en cada PR: historia, qué cambia, control de cambios (SC), documentos actualizados, pruebas y trazabilidad |
| `.github/workflows/gestion.yml` + `scripts/gestion/verificar_pr.py` | Falla el PR si falta la historia, si no marca si se desvía del plan, si cita una SC que no está registrada o si no dice qué documentos actualizó |
| `scripts/gestion/bitacora.py` | Regenera la tabla de PR de la bitácora desde GitHub |
| `scripts/gestion/libro_excel.py` | Regenera el libro Excel desde estos Markdown |

## Flujo en una línea

Abrir PR con la plantilla → si se desvía, registrar SC-nnn → actualizar los documentos afectados en el mismo PR → revisión «Approve» → fusionar con los chequeos en verde → regenerar bitácora y Excel → al etiquetar, notas de versión.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión (SC-009) |
