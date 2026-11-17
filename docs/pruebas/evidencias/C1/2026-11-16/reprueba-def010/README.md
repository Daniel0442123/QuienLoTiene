# Re-prueba de DEF-010 · 16/11/2026, 10:36 p. m. (hora de Colombia)

Versión: `main` e679ef3 (con #50 y #51). El backend no cambió desde 55ab5b6. Mismo ambiente de la re-prueba anterior (`../reprueba/README.md`): base PostGIS nueva, `java -jar` con el perfil dev, `vite` y los datos de `../scripts/setup.py`. Borrador preparado para que QA lo revise y firme.

| Texto | Dónde se vio | Texto leído en pantalla | Resultado | Evidencia |
|---|---|---|---|---|
| `yaRespondiste` | Droguería La 5 vuelve a la solicitud que ya respondió | «A las 10:36 p. m. respondiste esta solicitud con «Sí lo tengo», $ 7.000 por 1 unidad.» | Pasa | `capturas/01-def010-ya-respondiste.png` |
| `entregadoPregunta` | Ana, cuando La 5 marca el pedido como entregado | «A las 10:36 p. m., Droguería La 5 marcó tu pedido como entregado. ¿Ya lo tienes?» | Pasa | `capturas/02-def010-entregado-pregunta.png` |
| `plazoTerminado` | Luis, con la pantalla abierta, cuando su solicitud vence | «A las 11:06 p. m. terminó el plazo para responder.» | Pasa | `capturas/03-def010-plazo-terminado.png` |

En las tres pantallas ninguna línea tiene «..» (`reprueba-def010-salida.json`).

Observación de la prueba (no es defecto): el vencimiento se forzó en la base (`vence_en` en el pasado), así que la pantalla muestra la hora original del plazo (11:06 p. m.) aunque venció a las 10:37 p. m. En uso normal la solicitud vence a esa misma hora.

Script: `scripts/reprueba-def010.js` (Playwright, 390 × 844, escala 2).
