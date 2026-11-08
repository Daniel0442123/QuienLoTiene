# Análisis de defectos y principios de las pruebas · Ciclo <N>

> Basado en el Cuaderno de Instrumentos (hojas «Error_Defecto_Falla» y «Principios_Checklist»). Se diligencia al cerrar cada ciclo. Guardar en `docs/pruebas/informes/<ciclo>/ANALISIS-DE-DEFECTOS-<ciclo>.md`.

## 1. Error → defecto → falla

| ID defecto (ref. libro) | Descripción del defecto | Error probable (¿qué pudo pensar quien programó?) | Defecto en el código (clase, método, línea) | Falla observada (comportamiento con datos concretos) | ¿Pudo prevenirse con una revisión o una prueba temprana? |
|---|---|---|---|---|---|
| DEF-nnn (CP-…) | | | | | Sí / No, por qué |

## 2. Principios de las pruebas · autoevaluación del equipo

| # | Principio | Pregunta de autoevaluación | Respuesta del equipo | Evidencia del ciclo |
|---|---|---|---|---|
| 1 | Las pruebas muestran la presencia de defectos, no su ausencia | ¿Alguna vez concluimos que un módulo «no tiene errores» porque todas sus pruebas pasaron? | | |
| 2 | Las pruebas exhaustivas son imposibles | ¿Cómo priorizamos qué probar con el tiempo disponible? | | |
| 3 | Las pruebas tempranas ahorran tiempo y dinero | ¿Escribimos las pruebas antes o junto con el código? ¿Qué defecto se evitó? | | |
| 4 | Los defectos se agrupan | ¿En qué módulo se concentraron los defectos? | | |
| 5 | Paradoja del pesticida | ¿Agregamos casos de naturaleza distinta o solo re-ejecutamos los mismos? | | |
| 6 | Las pruebas dependen del contexto | ¿Qué parte del sistema exige un tipo de prueba distinto (PostGIS, SSE, seguridad)? | | |
| 7 | La falacia de la ausencia de errores | ¿El sistema cumple lo que necesita el usuario, además de pasar las pruebas? | | |

## 3. Reflexión

| # | Pregunta | Respuesta del equipo |
|---|---|---|
| 1 | ¿Cómo ayudó priorizar las pruebas unitarias en este ciclo? | |
| 2 | ¿Qué cambiaríamos en el siguiente ciclo? | |
