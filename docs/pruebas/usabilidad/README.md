# Prueba de usabilidad de caja negra · Quién lo tiene

> **Control del documento** · Versión 1.0 · 24/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión y firma de QA) · Cambios: [SC-023](../../gestion/solicitudes/SC-023.md)

Prueba pedida por el docente: recorrer, por cada rol, la ruta feliz y las excepciones, y comprobar que la aplicación explica cada error, conserva lo escrito y deja intentar de nuevo.

| Archivo | Contenido |
|---|---|
| `Guion-Prueba-Usabilidad-QuienLoTiene` (.docx y .pdf) | QLT-PRU-UP-01: objetivo, perfiles, 14 tareas con su ruta feliz, 30 excepciones, métricas (éxito, tiempo, toques, errores), cuestionario SUS y criterios de aceptación |
| `Formato-Registro-Prueba-Usabilidad-QuienLoTiene` (.docx y .pdf) | QLT-PRU-UP-02: resultado de cada tarea y excepción con su captura, observaciones y hojas en blanco para las sesiones con usuarios |
| `evidencias/capturas/` | 47 capturas de la ejecución del 23/11/2026 |
| `evidencias/resultados.json`, `evidencias/todo.json` | Resultado de cada paso, tal como lo registró el script |
| [`e2e/usabilidad/`](../../../e2e/usabilidad/) | Script de Playwright que ejecuta la prueba |

## Resultado de la ejecución del 23/11/2026

Sobre `main` df2325a en una pila local (base de datos nueva, PWA compilada, Chromium; el cliente en vista de celular): **14 de 14 tareas y 30 de 30 excepciones aprobadas, sin defectos nuevos**. «Sí lo tengo» se responde en 3 toques y «No lo tengo» en 1 (RNF-027). El tiempo de publicar con personas y el SUS se miden en las sesiones con usuarios (sección 6 del formato).

## Cómo repetirla

Con la API en `localhost:8080` y la PWA compilada servida en `localhost:4173` (ver `e2e/README.md`), y con la solicitud configurada para vencer en 3 minutos (`QUIENLOTIENE_SOLICITUDES_VIGENCIA=3m`) para la excepción UP-CLI-04-E2:

```bash
cd e2e
npx playwright test -c usabilidad/up.config.ts
```

Las capturas y `resultados.json` quedan en esta carpeta (`evidencias/`), o donde diga la variable `UP_DIR`.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 24/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Prueba ejecutada y documentada a pedido de Daniel Montenegro; preparada con Claude Code (SC-023) |
