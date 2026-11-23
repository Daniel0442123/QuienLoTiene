# Acta de liberación (go / no-go) · Quién lo tiene v1.0.0

> **Control del documento** · Versión 0.5 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador para el comité del 25/11/2026 (sin decisión ni firmas) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> **Borrador del 22/11/2026, preparado con Claude Code.** Los resultados salen del libro de control y de los informes `INFORME-DE-PRUEBAS.md` y `C3/INFORME-DE-CIERRE-C3.md`. La decisión, los defectos aceptados y las firmas los pone el comité el 25/11; este documento no decide ni firma en nombre de nadie.

| Campo | Valor |
|---|---|
| Fecha / asistentes | 25/11/2026 · por confirmar en el comité |
| Versión evaluada | `v0.5.5` (`2d6a71a`), en producción desde el 22/11/2026 a las 10:51 p. m., con las correcciones de DEF-016 a DEF-018 sobre `v0.5.4` (`f22f20c`), donde se ejecutó el C3 |

| Criterio de liberación (Plan Maestro 6.4) | Meta | Resultado (22/11) | ¿Cumple? |
|---|---|---|---|
| Casos de prioridad Alta ejecutados | 100 % | 54 de 54 funcionales y de seguridad, y E2E-01 a E2E-03 | Sí |
| Casos aprobados (resultado final) | ≥ 95 % (mín. 90 % con excepción) | 114 de 114 concluyentes = 100 % (campaña); 97 de 97 = 100 % en el C3 (NF-ESC-020 y NF-USA-028 aprobados en la re-prueba sobre `v0.5.5`). Falta NF-DIS-018; NF-USA-027 y NF-POR-029 fuera por SC-022 | Sí, por confirmar con NF-DIS-018 |
| Defectos S1 abiertos | 0 | 0 | Sí |
| Defectos S2 abiertos | 0 | 0 (DEF-011 cerrado en `v0.3.1`) | Sí |
| Defectos S3 abiertos | ≤ 5 con plan | 0 (DEF-018 corregido en `v0.5.5`; re-prueba aprobada, falta cerrarlo) | Sí |
| Cobertura domain + application | ≥ 70 % | 95,5 %; mínimo por módulo 84,4 % (clientes) | Sí |
| E2E obligatorios aprobados | 3 de 3 | 3 de 3 (y E2E-04 y E2E-05 también) | Sí |
| Vulnerabilidades críticas y altas sin justificar | 0 | 0: CI #274 sobre `f22f20c` en verde; Trivy y Dependency-Check fallan el build con críticas o altas | Sí |
| p95 de la API con 50 usuarios | ≤ 500 ms | 173,6 ms en producción (NF-REN-001, k6) | Sí |

| Campo | Valor |
|---|---|
| Defectos abiertos aceptados (S3/S4) con plan y responsable | Propuesta para el comité (responsables por confirmar): DEF-016, DEF-017 y DEF-018 salieron corregidos en `v0.5.5` (los tres con re-prueba aprobada en producción; falta cerrarlos) · **DEF-012** (S4, «Mis respuestas» no refleja el estado del pedido): aceptar con plan para la siguiente versión |
| Excepciones aprobadas y justificación | Para decidir en el comité: (1) NF-DIS-018 solo tiene medición desde el 22/11 a las 4:43 p. m., no desde el 16/11; (2) los E2E corrieron en una pila local construida desde `v0.5.4` y no en el VPS; (3) NF-ESC-020 se aprobó con 3 corridas seguidas con Caddy en 256 MiB, lanzadas desde la misma red que las anteriores; (4) SC-022, aprobada por el Product Owner el 22/11: NF-USA-027 (prueba con 5 usuarios) y NF-POR-029 (despliegue en un servidor limpio) no se ejecutan, así que RNF-027 y RNF-029 quedan sin verificar |
| Riesgos residuales | Caddy usa el 67 % de sus 256 MiB con 100 usuarios; despliegue de `v0.5.5` terminado a mano porque el flujo falló dos veces por la red; compatibilidad probada solo en la versión actual de cada navegador (NF-COM-026, 22/11); rapidez con usuarios reales sin medir (RNF-027) y reinstalación en otro servidor sin probar (RNF-029); usuarios con la PWA anterior hasta que aceptan la actualización; datos de prueba en producción hasta correr `limpiar.sh pruebas` |
| Pendientes antes del comité | cierre de DEF-016 a DEF-018 y triaje de DEF-012 (QA) · firma de los informes de cierre del C2 y del C3 · NF-DIS-018 y pruebas de humo después del despliegue final (26/11) |
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
| 0.3 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | SC-022 como excepción para el comité: NF-USA-027 y NF-POR-029 no se ejecutan |
| 0.4 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | SC-022 aprobada por el Product Owner |
| 0.5 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Versión evaluada `v0.5.5`: 100 % en la campaña y en el C3; 0 S3 abiertos |
