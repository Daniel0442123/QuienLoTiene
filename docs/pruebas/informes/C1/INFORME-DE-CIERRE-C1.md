# Informe de Cierre — Ciclo 1 (C1)

> **Control del documento** · Versión 0.1 · 17/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: **Borrador** (las cifras son del 17/11; QA las revisa, corre la re-prueba de los defectos corregidos y firma el 20/11) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

**ASEGURAMIENTO DE LA CALIDAD** · Quién lo tiene · Liberación 1.0

| Campo | Valor |
|---|---|
| Campaña | Quién lo tiene — Liberación 1.0 |
| Ciclo / fechas | C1 · 02/11/2026 al 20/11/2026 (planeado al 15/11; SC-015, propuesta) |
| Versiones | Corte del Sprint 1: v0.2.0 (`ea7351d`) · Sprint 2: `main` 5e8ab98 |
| Responsables de QA | Yanimar Burbano Piamba (funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad) |
| Alcance | Historias de los Sprints 1 y 2: 53 casos funcionales y de seguridad, y 8 no funcionales |
| Instrumento | Libro de control (`libro/`) y evidencias (`evidencias/C1/2026-11-12/` y `evidencias/C1/2026-11-17/`) |

## 1. Resumen de ejecución

Los 53 casos funcionales y de seguridad y los 8 no funcionales tienen resultado: 61 aprobados, 0 fallidos y 0 bloqueados (100 % de aprobación sobre los concluyentes). La última corrida completa (`./mvnw verify` sobre `main` 5e8ab98) dio 278 pruebas unitarias y 49 de integración, todas en verde, con 95,5 % de líneas cubiertas en domain y application. Los casos del Sprint 2 se verificaron además de punta a punta con el backend real y la PWA (`informes/C1/RECORRIDO-SPRINT-2-20261117.md`).

| Módulo | Responsable | Casos | Aprob. | Fall. | Bloq. | No ejec. | Cobertura |
|---|---|---|---|---|---|---|---|
| administracion | Yanimar | 2 | 2 | 0 | 0 | 0 | 100 % |
| clientes | Yanimar | 2 | 2 | 0 | 0 | 0 | 84,4 % |
| identidad | Yanimar / Francisco | 10 | 10 | 0 | 0 | 0 | 93,0 % |
| mensajeria | Yanimar | 2 | 2 | 0 | 0 | 0 | 100 % |
| notificaciones | Yanimar | 2 | 2 | 0 | 0 | 0 | 100 % |
| ordenes | Yanimar / Francisco | 8 | 8 | 0 | 0 | 0 | 99,0 % |
| solicitudes | Yanimar / Francisco | 17 | 17 | 0 | 0 | 0 | 99,6 % |
| tiendas | Yanimar | 10 | 10 | 0 | 0 | 0 | 92,8 % |
| **Total** | | **53** | **53** | **0** | **0** | **0** | **95,5 %** |

## 2. Detalle por caso

| Caso | Título | Prior. | Nivel | Probador | Fecha | Resultado | Defecto |
|---|---|---|---|---|---|---|---|
| CP-IDE-001-01 | Registro exitoso de cliente | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-IDE-001-02 | Correo ya registrado | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-IDE-001-03 | Frase de contraseña débil | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-TIE-002-01 | Registro de tienda queda pendiente | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-002-02 | Registro de tienda sin documento | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-IDE-003-01 | Inicio de sesión correcto | Alta | Integración (API + Spring Security) | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-003-02 | Credenciales incorrectas | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-003-03 | Cuenta suspendida | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-004-01 | Administrador con segundo factor válido | Media | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-004-02 | Segundo factor inválido | Media | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-005-01 | Registro sin autorización de datos | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-IDE-005-02 | Autorización registrada | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-CLI-007-01 | Guardar una dirección | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-CLI-007-02 | Dirección incompleta | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-009-01 | Horario fuera de atención | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-009-02 | Cambios de perfil visibles | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-010-01 | Tarifa fija en la respuesta | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-010-02 | Tarifa inválida | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-012-01 | Solo medios activos | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | DEF-007 |
| CP-TIE-012-02 | Desactivar un medio de pago | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-013-01 | Tienda cerrada | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-TIE-013-02 | Tienda abierta | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-016-01 | Publicar una solicitud | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-016-02 | Solicitud incompleta | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-016-03 | Límite de solicitudes | Alta | Unitaria | Francisco Castro | 2026-11-12 | Aprobado | — |
| CP-SOL-017-01 | Difusión solo a tiendas elegibles | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-017-02 | Categoría distinta | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-017-03 | Tienda suspendida | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-SOL-018-01 | Privacidad en la tarjeta de solicitud | Alta | Integración (API) | Francisco Castro | 2026-11-17 | Aprobado | — |
| CP-SOL-018-02 | Distancia aproximada | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-019-01 | Responder Sí lo tengo | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-019-02 | Alternativa sin descripción | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-019-03 | No lo tengo en un toque | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-019-04 | Respuesta duplicada | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-020-01 | Respuestas en tiempo real | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-020-02 | Ordenar por precio | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-020-03 | Alternativa marcada | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-SOL-021-01 | Vencimiento de la solicitud | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | DEF-008 |
| CP-SOL-021-02 | Aviso a tiendas no elegidas | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ORD-024-01 | Crear orden con total correcto | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ORD-024-02 | La tienda elegida ve los datos del cliente | Alta | Integración (API) | Francisco Castro | 2026-11-17 | Aprobado | — |
| CP-ORD-024-03 | Una tienda no elegida no ve los datos | Alta | Integración (API) | Francisco Castro | 2026-11-17 | Aprobado | — |
| CP-ORD-024-04 | Medio de pago no aceptado | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | DEF-007 |
| CP-ORD-027-01 | Transición válida | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ORD-027-02 | Transición inválida | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ORD-028-01 | Confirmar recepción | Alta | Unitaria | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ORD-028-02 | Confirmar una orden ajena | Alta | Unitaria | Francisco Castro | 2026-11-17 | Aprobado | — |
| CP-NOT-030-01 | Evento en tiempo real | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-NOT-030-02 | Solicitud urgente | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-MEN-032-01 | Botón de WhatsApp con orden activa | Alta | Manual (interfaz) | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-MEN-032-02 | Sin botón antes de elegir | Alta | Manual (interfaz) | Yanimar Burbano | 2026-11-17 | Aprobado | — |
| CP-ADM-036-01 | Aprobar una tienda | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |
| CP-ADM-036-02 | Rechazar una tienda | Alta | Unitaria | Yanimar Burbano | 2026-11-12 | Aprobado | — |

## 3. Defectos por severidad y estado

| ID | Severidad | Prioridad | Estado (17/11) | Caso | Qué falla | Versión |
|---|---|---|---|---|---|---|
| DEF-001 | S3 | P3 | En verificación | Exploratoria-20261112 | Contrato OpenAPI sin required, enum ni nullable | v0.2.0 → corregido en #35 |
| DEF-002 | S3 | P3 | En verificación | NF-SEG-011 | Faltaba OWASP Dependency-Check en el CI | v0.2.0 → corregido en #36 |
| DEF-003 | S4 | P4 | En verificación | NF-MAN-022 | Cobertura medida sobre todo el proyecto y no por módulo | v0.2.0 → corregido en #35 |
| DEF-004 | S4 | P4 | En verificación | NF-FIA-016 | Faltaba la etiqueta NF-FIA-016 | v0.2.0 → corregido en #35 |
| DEF-005 | S4 | P4 | Asignado | Exploratoria-20261112 | Publicar solicitud muestra los errores por etapas | v0.2.0, sigue en 5e8ab98 |
| DEF-006 | S3 | P2 | Nuevo | CP-ORD-024-01 (C-04) | /v3/api-docs sin respuesta 2xx en 9 operaciones | 5e8ab98 |
| DEF-007 | S3 | P2 | Nuevo | CP-ORD-024-04 · CP-TIE-012-01 | «Confirmar pedido» ofrece medios de pago que la tienda no acepta | 5e8ab98 |
| DEF-008 | S4 | P3 | Nuevo | CP-SOL-021-01 | La solicitud vencida sigue titulada «Esperando respuestas» | 5e8ab98 |
| DEF-009 | S4 | P4 | Nuevo | CP-ORD-024-02 | La tienda lee «Pagas con efectivo» en su pedido | 5e8ab98 |

Abiertos por severidad (sin contar los que están en verificación): S1 = 0 · S2 = 0 · S3 = 2 · S4 = 3

## 4. Requisitos y trazabilidad

Ver `libro/07-TRAZABILIDAD.md` (versión 1.1). Todos los requisitos de los Sprints 1 y 2 quedan verificados; RF-012, RF-021 y RF-024 con defectos abiertos de severidad S3 o S4.

## 5. Resultados no funcionales

| Caso | Requisito | Meta | Medido (17/11) | Resultado |
|---|---|---|---|---|
| NF-SEG-006 | RNF-006 | ≥ 15 caracteres, Argon2id, lista de filtradas | Sin cambios desde el corte | Aprobado |
| NF-SEG-011 | RNF-011 | 0 críticas y 0 altas sin justificar | CI #146: Trivy y OWASP Dependency-Check en verde | Aprobado |
| NF-SEG-014 | RNF-014 | 100 % con fecha y versión de la política | Sin cambios desde el corte | Aprobado |
| NF-FIA-016 | RNF-016 | 0 transiciones inválidas aceptadas | Tienda, solicitud y orden: 0 | Aprobado |
| NF-ESC-021 | RNF-021 | 0 violaciones entre módulos | Spring Modulith verify(): 0 | Aprobado |
| NF-MAN-022 | RNF-022 | ≥ 70 % por módulo | 95,5 % total; mínimo clientes 84,4 % | Aprobado |
| NF-MAN-023 | RNF-023 | 0 violaciones ArchUnit | 0 | Aprobado |
| NF-POR-030 | RNF-030 | 0 secretos | Gitleaks en verde en CI #146 | Aprobado |

## 6. Casos bloqueados

Ninguno.

## 7. Observaciones

- Los casos de nivel «Manual (interfaz)» e «Integración (SSE)» del Sprint 2 se vieron en pantalla con el frontend de Monica contra el backend real.
- El compromiso C-04 (tipos del frontend generados desde el backend) sigue abierto: ver `informes/C1/C-04-DIFERENCIAS-TIPOS.md` y DEF-006.
- Cuatro observaciones menores del recorrido quedaron sin registrar como defecto, para el triaje de QA (sección «Observaciones» del recorrido).
- Las horas de la bitácora son estimadas.

## 8. Criterios de salida del ciclo

| Criterio (Plan Maestro 6.3 y plan C1 §6) | Estado (17/11) |
|---|---|
| 100 % de casos del ciclo ejecutados | Cumple: 53 de 53 y 8 de 8 |
| Todo Fallido con defecto registrado | Cumple: no hay fallidos; los defectos del recorrido están registrados |
| Cobertura ≥ 70 % en los módulos del ciclo | Cumple: mínimo 84,4 % (clientes) |
| 0 defectos S1 y S2 abiertos | Cumple |
| Pipeline en verde (Modulith, ArchUnit, Trivy, Dependency-Check, secretos) | Cumple: CI #146 |
| Informes con datos reales del libro | Pendiente: revisión y firma de QA |

## 9. Conclusión y recomendación

Borrador: el C1 cumple sus criterios de salida. Se recomienda cerrarlo el 20/11 después de re-probar lo que se corrija entre el 18 y el 19/11.

Recomendaciones para el equipo:

1. Corregir DEF-006 y DEF-007 (S3, P2) antes del cierre, porque afectan C-04 y el flujo de pago de la demo.
2. Corregir DEF-005, DEF-008 y DEF-009 (S4) en el Sprint 3 si no alcanzan antes del 20/11.
3. QA cierra DEF-001 a DEF-004 después de revisar la re-prueba del 17/11.

| Elaborado por | Revisado / aprobado por |
|---|---|
| Yanimar Burbano Piamba · Líder de QA · Firma: | Wilson Javier Galindez Certuche · Product Owner · Firma: |
| Francisco José Castro Muñoz · QA no funcional · Firma: | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador con las cifras del 17/11; pendiente de la re-prueba final y la firma de QA |
