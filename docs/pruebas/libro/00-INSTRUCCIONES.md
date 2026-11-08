# Hoja «Instrucciones» · Libro de control de QA · Quién lo tiene

> Fuente en Markdown del libro de control. Cuando el contenido esté estable se pasa a la plantilla Excel del profesor (`Libro-Control-QA-QuienLoTiene.xlsx`), hoja por hoja, con los mismos nombres de columnas.

| Tema | Regla |
|---|---|
| Propósito | Registrar de forma controlada los casos de prueba, su ejecución por ciclo, los defectos, la trazabilidad y la bitácora diaria de la campaña de QA de Quién lo tiene. |
| Responsables | Yanimar Burbano Piamba (líder de QA, pruebas funcionales) y Francisco José Castro Muñoz (QA de pruebas no funcionales y seguridad). Los desarrolladores escriben las pruebas automatizadas; QA las ejecuta, registra y firma. |
| Cómo se usa | 1) QA completa «Parámetros y equipo». 2) Antes de cada ciclo revisa «Casos». 3) Durante el ciclo diligencia la hoja «Ejecución» del ciclo, «Defectos» y «Bitácora». 4) Al cerrar el ciclo actualiza «Trazabilidad» y «Tablero» y elabora el informe de cierre. |
| Celdas que se diligencian | Solo las columnas vacías de las hojas de ejecución, defectos, bitácora y trazabilidad. Los catálogos («Casos», «Listas») solo los cambia QA y el cambio se anota en el control de cambios del Plan Maestro. |
| Resultados de un caso | **Aprobado**: se cumple el resultado esperado. **Fallido**: no se cumple; se registra un defecto. **Bloqueado**: no se pudo ejecutar por una causa externa (ambiente, datos, dependencia); se escribe la causa. **No ejecutado**: aún no se corre. **No aplica**: el caso no corresponde a la versión (justificar). |
| Evidencia | Toda ejecución Fallida o Bloqueada lleva evidencia. Las pruebas automatizadas llevan la salida de `mvn` (resumen de Surefire) y el `jacoco.csv`. Las manuales llevan capturas numeradas. Ruta: `docs/pruebas/evidencias/<ciclo>/<sesión AAAA-MM-DD>/<ID del caso>/NN-descripcion.png`. |
| Pruebas automatizadas | Cada `@Test` lleva `@Tag("HU-xxx")` y `@Tag("CP-xxx")` con la referencia del SRS. Un caso del libro puede estar cubierto por varias pruebas: se listan en la columna «Prueba automatizada» de «Casos» como `Clase#metodo`. |
| Historial | No se borra nada. Si un caso cambia de resultado dentro del mismo ciclo, se anota el cambio en Observaciones con la fecha. |
| Defectos | Un defecto por fila en «Defectos», con ID consecutivo DEF-nnn y el ID del caso que lo evidencia (o `Exploratoria-AAAAMMDD`). Severidad S1–S4 y prioridad P1–P4 según la hoja «Listas». |
| Hojas | 01 Parámetros y equipo · 02 Casos · 03 Ejecución C1 · 04 Ejecución C2 · 05 Ejecución C3 · 06 Defectos · 07 Trazabilidad · 08 Bitácora · 09 Tablero · 10 Listas |
