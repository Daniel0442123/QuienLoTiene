# Hoja «Defectos» · Registro de defectos

> Un defecto por fila. ID consecutivo `DEF-nnn`. El detalle completo (pasos, evidencia, justificación) puede ir además en `docs/pruebas/defectos/DEF-nnn.md` con la plantilla `plantillas/REPORTE-DE-DEFECTO.md`. Título: «Qué falla + dónde + con qué dato».

| ID | Fecha de reporte | Reportado por | Ciclo | ID del caso | Requisito | Módulo | Título (qué falla) | Pasos para reproducir | Resultado esperado | Resultado obtenido | Severidad | Justificación severidad | Prioridad | Justificación prioridad | Estado | Asignado a | Versión donde se detectó | Versión de corrección | Pull request | Fecha de cierre | Evidencia | Observaciones |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| DEF-001 | | | | | | | | | | | | | | | Nuevo | | | | | | | |

## Flujo de estados

```
Nuevo → Confirmado → Asignado → En corrección → Corregido → En verificación → Cerrado
                                                                  │
                                                                  └→ Reabierto → Asignado
Nuevo → Rechazado (No es un defecto / Duplicado)
Confirmado → Diferido (solo S3/S4, con aprobación del Product Owner en el acta)
```

- **Nuevo**: lo registra QA. **Confirmado**: el triaje lo reproduce. **Asignado**: tiene desarrollador. **Corregido**: el pull request está fusionado. **En verificación**: la corrección está desplegada en pruebas y QA re-prueba con el mismo caso. **Cerrado**: la re-prueba pasa. **Reabierto**: la re-prueba falla.
