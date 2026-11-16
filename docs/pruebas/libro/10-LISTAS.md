# Hoja «Listas» · Valores permitidos

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

| Lista | Valores |
|---|---|
| Resultado | Aprobado · Fallido · Bloqueado · No ejecutado · No aplica |
| Tipo de caso | Funcional · Seguridad · Extremo a extremo · Rendimiento · Fiabilidad · Disponibilidad · Escalabilidad · Mantenibilidad · Compatibilidad · Usabilidad · Accesibilidad · Móvil · Portabilidad · Humo |
| Nivel | Unitaria · Integración (PostGIS / API / SSE) · Aceptación (Cucumber) · Extremo a extremo (Playwright) · Manual · No funcional |
| Ciclo | C1 · C2 · C3 · Producción (humo) |
| Prioridad del caso | Alta (Must) · Media (Should) · Baja (Could) |
| Técnica | Basada en requisitos · Partición de equivalencia · Valores límite · Tabla de decisión · Transición de estados · Caja gris · Exploratoria |
| Severidad | S1 Crítica · S2 Alta · S3 Media · S4 Baja |
| Prioridad del defecto | P1 Inmediata · P2 Alta · P3 Normal · P4 Baja |
| Estado del defecto | Nuevo · Confirmado · Asignado · En corrección · Corregido · En verificación · Cerrado · Reabierto · Rechazado · Diferido |
| Estado del requisito | Verificado · Verificado parcialmente · Con fallas · Bloqueado · Sin ejecutar |
| Responsable QA | Yanimar Burbano · Francisco Castro |

## Severidad

| Severidad | Definición | Ejemplos en Quién lo tiene | Atención |
|---|---|---|---|
| S1 Crítica | Pérdida o corrupción de datos, fuga de datos personales, falla de seguridad o caída del flujo principal sin alternativa | Una tienda no elegida ve la dirección del cliente; no se puede publicar ninguna solicitud; un usuario suspendido entra | Corrección antes de 1 día; bloquea la liberación |
| S2 Alta | Función principal no disponible o con resultado incorrecto, sin alternativa razonable | El total de la orden suma mal el domicilio; la difusión llega a tiendas a 5 km; la orden acepta una transición inválida | Antes de 2 días; bloquea la liberación |
| S3 Media | Falla con alternativa, o en una función secundaria | El orden por cercanía no aplica; el mini chat no actualiza sin recargar | En el ciclo o con plan aprobado |
| S4 Baja | Cosmético o de usabilidad menor | Texto mal escrito; botón de 40 px en lugar de 44 px | Con plan; no bloquea |

Por defecto S1 → P1, S2 → P2, S3 → P3 y S4 → P4. El triaje puede cambiar la prioridad justificándolo (por ejemplo, un error ortográfico S4 en la pantalla de inicio de la demo puede ser P2).

## Matriz severidad × prioridad (referencia)

| Severidad \ Prioridad | P1 Inmediata | P2 Alta | P3 Normal | P4 Baja |
|---|---|---|---|---|
| S1 Crítica | Corregir de inmediato: el sistema no se puede liberar | Poco común: justificar por qué no es inmediata | No debería ocurrir | No debería ocurrir |
| S2 Alta | Falla una función principal en la demo | Caso típico: función importante sin alternativa | Función importante con alternativa temporal | Función importante que nadie usa en la demo |
| S3 Media | Defecto moderado muy visible para el profesor | Función secundaria con impacto en la demo | Caso más común | Puede esperar a la siguiente versión |
| S4 Baja | Algo cosmético muy visible | Revisar por qué algo menor es urgente | Detalle menor | Caso típico: cosmético |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
