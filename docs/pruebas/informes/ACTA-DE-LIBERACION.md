# Acta de liberación (go / no-go) · Quién lo tiene v1.0.0

> **Control del documento** · Versión 0.2 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador para el comité del 25/11/2026 (sin decisión ni firmas) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> **Borrador del 22/11/2026, preparado con Claude Code.** Los resultados salen del libro de control y de los informes `INFORME-DE-PRUEBAS.md` y `C3/INFORME-DE-CIERRE-C3.md`. La decisión, los defectos aceptados y las firmas los pone el comité el 25/11; este documento no decide ni firma en nombre de nadie.

| Campo | Valor |
|---|---|
| Fecha / asistentes | 25/11/2026 · por confirmar en el comité |
| Versión evaluada | `v0.5.4` (`f22f20c`), en producción desde el 22/11/2026. Si sale una `v0.5.5` con correcciones antes del comité, se evalúa esa y se actualiza esta fila |

| Criterio de liberación (Plan Maestro 6.4) | Meta | Resultado (22/11) | ¿Cumple? |
|---|---|---|---|
| Casos de prioridad Alta ejecutados | 100 % | 54 de 54 funcionales y de seguridad, y E2E-01 a E2E-03 | Sí |
| Casos aprobados (resultado final) | ≥ 95 % (mín. 90 % con excepción) | 112 de 114 concluyentes = 98,2 % (campaña); 95 de 97 = 97,9 % en el C3. Faltan 3 no funcionales | Sí, por confirmar con los 3 pendientes |
| Defectos S1 abiertos | 0 | 0 | Sí |
| Defectos S2 abiertos | 0 | 0 (DEF-011 cerrado en `v0.3.1`) | Sí |
| Defectos S3 abiertos | ≤ 5 con plan | 1 (DEF-018), plan propuesto abajo | Sí, si el comité aprueba el plan |
| Cobertura domain + application | ≥ 70 % | 95,5 %; mínimo por módulo 84,4 % (clientes) | Sí |
| E2E obligatorios aprobados | 3 de 3 | 3 de 3 (y E2E-04 y E2E-05 también) | Sí |
| Vulnerabilidades críticas y altas sin justificar | 0 | 0: CI #274 sobre `f22f20c` en verde; Trivy y Dependency-Check fallan el build con críticas o altas | Sí |
| p95 de la API con 50 usuarios | ≤ 500 ms | 173,6 ms en producción (NF-REN-001, k6) | Sí |

| Campo | Valor |
|---|---|
| Defectos abiertos aceptados (S3/S4) con plan y responsable | Propuesta para el comité (responsables por confirmar): **DEF-018** (S3, conexiones SSE cerradas en 1 de 3 corridas): subir el `mem_limit` de Caddy y repetir NF-ESC-020 antes del 26/11 · **DEF-017** (S4, botones del mapa de 30 px) y **DEF-016** (S4, mensaje al vencer): corregir en una `v0.5.5` o aceptar con plan para la siguiente versión · **DEF-012** (S4, «Mis respuestas» no refleja el estado del pedido): aceptar con plan para la siguiente versión |
| Excepciones aprobadas y justificación | Para decidir en el comité: (1) NF-DIS-018 solo tiene medición desde el 22/11 a las 4:43 p. m., no desde el 16/11; (2) los E2E corrieron en una pila local construida desde `v0.5.4` y no en el VPS; (3) NF-ESC-020 queda Fallido mientras no se repita (2 de 3 corridas cumplen) |
| Riesgos residuales | Caddy cerca de su límite de memoria con 100 usuarios; compatibilidad probada solo en la versión actual de cada navegador (NF-COM-026, 22/11); usuarios con la PWA anterior hasta que aceptan la actualización; datos de prueba en producción hasta correr `limpiar.sh pruebas` |
| Pendientes antes del comité | NF-USA-027 y NF-POR-029 (Francisco, 24/11) · triaje de DEF-012, DEF-016, DEF-017 y DEF-018 (QA) · firma de los informes de cierre del C2 y del C3 · NF-DIS-018 y pruebas de humo después del despliegue final (26/11) |
| **Decisión** | ☐ GO: se libera v1.0.0 el 26/11/2026 · ☐ NO-GO: motivo y nueva fecha |

| Rol | Nombre | Firma |
|---|---|---|
| Product Owner | Wilson Javier Galindez Certuche | |
| Líder de QA | Yanimar Burbano Piamba | |
| QA no funcional y seguridad | Francisco José Castro Muñoz | |
| Arquitecto de software | Daniel Montenegro Almendra | |
| Scrum Master | Brayan Grajales Escarraga | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador con los resultados del 22/11 sobre `v0.5.4` para el comité del 25/11. Sin decisión ni firmas |
| 0.2 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | NF-COM-026 aprobado: 112 de 114 en la campaña y 95 de 97 en el C3 |
