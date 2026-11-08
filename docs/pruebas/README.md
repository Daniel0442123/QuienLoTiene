# Documentación de pruebas · Quién lo tiene

Responsables de QA: **Yanimar Burbano Piamba** (líder de QA, pruebas funcionales) y **Francisco José Castro Muñoz** (no funcionales y seguridad).

Todo se escribe primero aquí en Markdown, con la estructura y los campos de las plantillas del docente, y después se pasa a esas plantillas (Word y Excel). El Markdown es la fuente: si algo cambia, se cambia aquí y se vuelve a generar el Word o el Excel.

## Índice

| Documento | Qué es | Plantilla del docente de la que sale |
|---|---|---|
| [ESTRATEGIA-DE-PRUEBAS.md](ESTRATEGIA-DE-PRUEBAS.md) | Pirámide, prioridad de las unitarias, etiquetas, definición de terminado | (propia del proyecto) |
| [01-PLAN-MAESTRO-DE-PRUEBAS.md](01-PLAN-MAESTRO-DE-PRUEBAS.md) | Reglas de toda la campaña: modelo, equipo, ambiente, calendario, criterios, defectos, instrumentos | Plan-Maestro-de-Pruebas (.docx) |
| [02-PLAN-DE-PRUEBAS-C1.md](02-PLAN-DE-PRUEBAS-C1.md) · [C2](02-PLAN-DE-PRUEBAS-C2.md) · [C3](02-PLAN-DE-PRUEBAS-C3.md) | Plan de cada ciclo: objetivos, elementos priorizados, técnicas, criterios de salida, entregables, cronograma, riesgos | Plan_de_Pruebas (.docx) |
| [libro/](libro/00-INSTRUCCIONES.md) | Libro de control: instrucciones, parámetros y equipo, casos, ejecución C1/C2/C3, defectos, trazabilidad (RTM), bitácora, tablero, listas | Libro-Control-QA (.xlsx) + RTM del Cuaderno de Instrumentos |
| [plantillas/INFORME-DE-CORTE.md](plantillas/INFORME-DE-CORTE.md) | Avance al final de cada sprint | Anexo C del Plan Maestro |
| [plantillas/INFORME-DE-CIERRE-DE-CICLO.md](plantillas/INFORME-DE-CIERRE-DE-CICLO.md) | Cierre de C1, C2 y C3 | Informe-Cierre-C1 (.docx) |
| [plantillas/EVIDENCIAS-DE-PRUEBAS.md](plantillas/EVIDENCIAS-DE-PRUEBAS.md) | Ficha y capturas por caso | Evidencias-Pruebas-C1 (.docx) |
| [plantillas/INFORME-DE-PRUEBAS.md](plantillas/INFORME-DE-PRUEBAS.md) | Informe final de la campaña | Informe-Pruebas (.docx) |
| [plantillas/REPORTE-DE-DEFECTO.md](plantillas/REPORTE-DE-DEFECTO.md) | Ficha de un defecto con severidad y prioridad justificadas | Cuaderno_Gestion_Defectos (.xlsx) + Anexo A |
| [plantillas/ANALISIS-DE-DEFECTOS.md](plantillas/ANALISIS-DE-DEFECTOS.md) | Error → defecto → falla y principios de las pruebas | Cuaderno_Instrumentos (.xlsx) |
| [plantillas/ACTA-DE-INICIO.md](plantillas/ACTA-DE-INICIO.md) · [ACTA-DE-LIBERACION.md](plantillas/ACTA-DE-LIBERACION.md) | Inicio de ciclo y decisión go / no-go | Anexos B y E |
| [plantillas/SESION-EXPLORATORIA.md](plantillas/SESION-EXPLORATORIA.md) · [CHECKLIST-ACCESIBILIDAD.md](plantillas/CHECKLIST-ACCESIBILIDAD.md) · [PRUEBAS-DE-HUMO.md](plantillas/PRUEBAS-DE-HUMO.md) | Exploración, WCAG 2.1 AA y humo en producción | Anexos F, G y H |

## Números de la campaña

- 85 casos funcionales (uno por escenario Gherkin del SRS), de los cuales 16 son de seguridad: 69 a cargo de Yanimar y 16 de Francisco.
- 30 casos no funcionales (uno por RNF) y 5 escenarios de punta a punta.
- Ciclos: **C1** Sprints 1 y 2 (61 casos), **C2** Sprint 3 (31 casos más las re-pruebas de C1), **C3** Cierre (103: regresión de los 85 más E2E y no funcionales).
- Meta para liberar el 26/11: ≥ 95 % de aprobados (mínimo 90 % con excepción en el acta), 0 defectos S1 y S2, cobertura ≥ 70 %.

## Calendario de entregas de documentación

| Fecha | Qué debe quedar terminado |
|---|---|
| 08/11 | Acta de inicio C1, corte del Sprint 1 en el libro, informe de corte |
| 15/11 | Ejecución C1 completa, evidencias C1, informe de cierre C1, análisis de defectos C1 |
| 22/11 | Ejecución C2 y re-pruebas, evidencias C2, informe de cierre C2, análisis de defectos C2 |
| 25/11 | Ejecución C3, evidencias C3, informe de cierre C3, informe final de pruebas, acta de liberación |
| 26/11 | Pruebas de humo en producción |

## Entregables en formato del docente (`entregables/`)

| Archivo | Sale de |
|---|---|
| `entregables/Libro-Control-QA-QuienLoTiene-v1.0.xlsx` | `libro/*.md` · plantilla Libro-Control-QA (hojas, colores, celdas crema, tablero con fórmulas y criterios de liberación) |
| `entregables/word/Plan-Maestro-de-Pruebas-QuienLoTiene-v1.0.docx` | `01-PLAN-MAESTRO-DE-PRUEBAS.md` · plantilla Plan-Maestro |
| `entregables/word/Plan-de-Pruebas-C1/C2/C3-QuienLoTiene.docx` | `02-PLAN-DE-PRUEBAS-Cn.md` · plantilla Plan_de_Pruebas |
| `entregables/word/Plantilla-Informe-de-Cierre-de-Ciclo-QuienLoTiene.docx` | plantilla Informe-Cierre-C1 |
| `entregables/word/Plantilla-Evidencias-de-Pruebas-QuienLoTiene.docx` | plantilla Evidencias-Pruebas-C1 |
| `entregables/word/Plantilla-Informe-de-Pruebas-QuienLoTiene.docx` | plantilla Informe-Pruebas |
| `entregables/word/Formato-*.docx` y `Estrategia-de-Pruebas-QuienLoTiene.docx` | anexos A a H del Plan Maestro, con su estilo |

En Word, el índice («Contenido») se actualiza con clic derecho → Actualizar campo.

## Paso a las plantillas del docente

1. **Libro de control (Excel):** cada archivo de `libro/` corresponde a una hoja. Las tablas se copian a la hoja del mismo nombre del libro del docente, conservando sus fórmulas del tablero y la trazabilidad, adaptadas de parejas a responsables de QA y de dos ciclos a tres.
2. **Documentos (Word):** cada Markdown se vuelca en la plantilla Word correspondiente, conservando portada, estilos, tablas y numeración, y cambiando UNIVIDA por Quién lo tiene.
3. Se regenera al cierre de cada sprint, después de actualizar el Markdown.

## Cómo regenerar el catálogo de casos

El catálogo (`libro/02-CASOS.md`) y las hojas de ejecución y trazabilidad se generaron a partir de los escenarios Gherkin del SRS. Si el SRS cambia, se actualizan aquí a mano o se vuelven a generar, conservando los resultados ya registrados.
