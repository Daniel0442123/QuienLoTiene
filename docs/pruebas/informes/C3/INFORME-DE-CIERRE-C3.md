# Informe de Cierre — Ciclo 3 (C3)

> **Control del documento** · Versión 0.3 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de NF-DIS-018, de la decisión sobre SC-022, del triaje de DEF-012, DEF-016, DEF-017 y DEF-018 y de la firma de QA) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

**ASEGURAMIENTO DE LA CALIDAD** · Quién lo tiene · Liberación 1.0

> **Borrador del 22/11/2026, preparado con Claude Code.** Las cifras salen del libro de control (`libro/05-EJECUCION-C3.md`, `06-DEFECTOS.md` y `07-TRAZABILIDAD.md`). QA lo revisa, completa lo pendiente y lo firma.

| Campo | Valor |
|---|---|
| Campaña | Quién lo tiene — Liberación 1.0 |
| Ciclo / fechas | C3 (Cierre) · 22/11/2026 al 25/11/2026 (planeado del 23/11 al 25/11; se adelantó al 22/11 porque el C2 cerró ese día) |
| Versiones | Regresión: `v0.5.3` (`ff9ac6d`) · HU-015 y HU-022: rama del PR #89 · Producción, no funcionales y «Cómo llegar»: `v0.5.4` (`f22f20c`) · Punta a punta: pila local construida desde `v0.5.4` |
| Responsables de QA | Yanimar Burbano Piamba (funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad) |
| Alcance | Regresión completa de los 86 casos funcionales y de seguridad (incluidos los de las 4 historias Could del Cierre), 5 escenarios de punta a punta y 13 no funcionales sobre el VPS. HU-006 y HU-011 salieron por SC-021 y NF-USA-027 y NF-POR-029 por SC-022 (pendiente del comité), así que se planean 98 casos (104 − 6 «No aplica») |
| Instrumento | Libro de control (`libro/`) y evidencias (`evidencias/C3/2026-11-22/`) |

## 1. Resumen de ejecución

De los 98 casos planeados, 97 tienen resultado: 95 aprobados, 2 fallidos y 0 bloqueados. La tasa de aprobación sobre los concluyentes es 95 / 97 = **97,9 %**. La matriz de navegadores (NF-COM-026) se aprobó el 22/11 en un iPhone, un Android y Edge. La prueba con 5 usuarios (NF-USA-027) y el despliegue en un servidor limpio (NF-POR-029) no se ejecutan en esta liberación por SC-022, propuesta por Pedro y pendiente del comité. Falta la disponibilidad (NF-DIS-018), que se mide hasta el 26/11.

- **Regresión.** `./mvnw -B verify` sobre la rama de HU-015 y HU-022: 398 pruebas unitarias y 72 de integración, 0 fallas, y 361 pruebas del frontend con Vitest. Cobertura de líneas de domain y application del 95,5 % (2753 de 2884, incluye shared); el módulo más bajo es clientes con 84,4 %. Los 86 casos tienen resultado: 82 aprobados y 4 «No aplica» de HU-006 y HU-011 (SC-021).
- **Producción.** El 22/11 se comprobaron en `v0.5.4` «Tu resumen» de la tienda (HU-015) y la ampliación del radio de 3 a 5 km a los 11,9 minutos (HU-022) ([evidencia](../../evidencias/C3/2026-11-22/produccion-v0.5.4/README.md)). El recorrido manual de «Cómo llegar» (CP-ORD-026-01) pasó con el pedido 40233FE4 ([evidencia](../../evidencias/C3/2026-11-22/como-llegar/recorrido.txt)).
- **Punta a punta.** Los 5 escenarios pasan con Playwright, cada actor en su propio navegador y todo por la interfaz ([evidencia](../../evidencias/C3/2026-11-22/e2e/README.md)). E2E-05 corrió con los tiempos reales: el radio pasó a 5 km a los 11,0 minutos y la solicitud venció a los 30,0. En la primera corrida E2E-05 encontró DEF-016.
- **No funcionales.** 7 aprobados (rendimiento, difusión, memoria, Lighthouse, TLS, respaldo y despliegue) y 2 fallidos: NF-ESC-020 por DEF-018 (en 1 de 3 corridas se cerraron 8 de 50 conexiones SSE) y NF-USA-028 por DEF-017 (los botones + y − del mapa miden 30 px).

| Módulo | Responsable | Casos | Aprob. | Fall. | Bloq. | No ejec. | Cobertura |
|---|---|---|---|---|---|---|---|
| administracion | Francisco / Yanimar | 4 | 4 | 0 | 0 | 0 | 97,9 % |
| catalogo | Yanimar | 2 | 2 | 0 | 0 | 0 | 100 % |
| clientes | Yanimar | 5 | 5 | 0 | 0 | 0 | 84,4 % |
| identidad | Francisco / Yanimar | 12 (2 No aplica) | 10 | 0 | 0 | 0 | 94,0 % |
| mensajeria | Yanimar | 4 | 4 | 0 | 0 | 0 | 100 % |
| notificaciones | Yanimar | 4 | 4 | 0 | 0 | 0 | 100 % |
| ordenes | Francisco / Yanimar | 13 | 13 | 0 | 0 | 0 | 96,8 % |
| reputacion | Francisco / Yanimar | 5 | 5 | 0 | 0 | 0 | 97,9 % |
| solicitudes | Francisco / Yanimar | 21 | 21 | 0 | 0 | 0 | 99,7 % |
| tiendas | Francisco / Yanimar | 16 (2 No aplica) | 14 | 0 | 0 | 0 | 90,5 % |
| **Funcionales y de seguridad** | | **86** (4 No aplica) | **82** | **0** | **0** | **0** | **95,5 %** |
| Punta a punta (Playwright) | Yanimar | 5 | 5 | 0 | 0 | 0 | — |
| No funcionales sobre el VPS | Francisco | 13 (2 No aplica) | 8 | 2 | 0 | 1 | — |
| **Total** | | **104 (6 No aplica)** | **95** | **2** | **0** | **1** | **95,5 %** |

## 2. Detalle por caso

| Caso | Título | Prior. | Nivel | Probador | Fecha | Resultado | Defecto |
|---|---|---|---|---|---|---|---|
| **ADMINISTRACION** | | | | | | | |
| CP-ADM-036-01 | Aprobar una tienda | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ADM-036-02 | Rechazar una tienda | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ADM-037-01 | Suspender una cuenta | Media | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-ADM-037-02 | Resolver un reporte | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **CATALOGO** | | | | | | | |
| CP-CAT-038-01 | Desactivar una categoría | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-CAT-038-02 | Agregar un producto restringido | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **CLIENTES** | | | | | | | |
| CP-CLI-007-01 | Guardar una dirección | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-CLI-007-02 | Dirección incompleta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-CLI-008-01 | Repetir una solicitud | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-CLI-008-02 | Historial vacío | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-CLI-008-03 | Volver a una pregunta abierta | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **IDENTIDAD** | | | | | | | |
| CP-IDE-001-01 | Registro exitoso de cliente | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-IDE-001-02 | Correo ya registrado | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-IDE-001-03 | Frase de contraseña débil | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-003-01 | Inicio de sesión correcto | Alta | Integración (API + Spring Security) | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-003-02 | Credenciales incorrectas | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-003-03 | Cuenta suspendida | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-004-01 | Administrador con segundo factor válido | Media | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-004-02 | Segundo factor inválido | Media | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-005-01 | Registro sin autorización de datos | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-005-02 | Autorización registrada | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-IDE-006-01 | Verificación de correo exitosa | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | No aplica | — |
| CP-IDE-006-02 | Enlace vencido | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | No aplica | — |
| **MENSAJERIA** | | | | | | | |
| CP-MEN-032-01 | Botón de WhatsApp con orden activa | Alta | Manual (interfaz) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-MEN-032-02 | Sin botón antes de elegir | Alta | Manual (interfaz) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-MEN-033-01 | Enviar un mensaje | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-MEN-033-02 | Chat cerrado | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **NOTIFICACIONES** | | | | | | | |
| CP-NOT-030-01 | Evento en tiempo real | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-NOT-030-02 | Solicitud urgente | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-NOT-031-01 | Push con la app cerrada | Media | Manual (navegador) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-NOT-031-02 | Sin autorización de notificaciones | Media | Manual (navegador) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **ORDENES** | | | | | | | |
| CP-ORD-024-01 | Crear orden con total correcto | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-024-02 | La tienda elegida ve los datos del cliente | Alta | Integración (API) | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-ORD-024-03 | Una tienda no elegida no ve los datos | Alta | Integración (API) | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-ORD-024-04 | Medio de pago no aceptado | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-025-01 | Reserva vencida | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-025-02 | Recogida a tiempo | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-026-01 | Cómo llegar | Media | Manual (interfaz) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-027-01 | Transición válida | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-027-02 | Transición inválida | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-028-01 | Confirmar recepción | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-028-02 | Confirmar una orden ajena | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-ORD-029-01 | Cancelar antes del despacho | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-ORD-029-02 | Cancelar después del despacho | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **REPUTACION** | | | | | | | |
| CP-REP-034-01 | Calificar una orden resuelta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-REP-034-02 | Calificar sin orden resuelta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-REP-034-03 | Calificar dos veces | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-REP-035-01 | Crear un reporte | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-REP-035-02 | Reportar una orden ajena | Media | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| **SOLICITUDES** | | | | | | | |
| CP-SOL-016-01 | Publicar una solicitud | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-016-02 | Solicitud incompleta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-016-03 | Límite de solicitudes | Alta | Unitaria | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-SOL-017-01 | Difusión solo a tiendas elegibles | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-017-02 | Categoría distinta | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-017-03 | Tienda suspendida | Alta | Integración (PostGIS) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-018-01 | Privacidad en la tarjeta de solicitud | Alta | Integración (API) | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-SOL-018-02 | Distancia aproximada | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-019-01 | Responder Sí lo tengo | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-019-02 | Alternativa sin descripción | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-019-03 | No lo tengo en un toque | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-019-04 | Respuesta duplicada | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-020-01 | Respuestas en tiempo real | Alta | Integración (SSE) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-020-02 | Ordenar por precio | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-020-03 | Alternativa marcada | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-021-01 | Vencimiento de la solicitud | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-021-02 | Aviso a tiendas no elegidas | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-022-01 | Ampliación del radio | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-022-02 | Sin ampliación si hay respuestas | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-023-01 | Producto restringido | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-SOL-023-02 | Restringido sin domicilio | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **TIENDAS** | | | | | | | |
| CP-TIE-002-01 | Registro de tienda queda pendiente | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-002-02 | Registro de tienda sin documento | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-009-01 | Horario fuera de atención | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-009-02 | Cambios de perfil visibles | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-010-01 | Tarifa fija en la respuesta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-010-02 | Tarifa inválida | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-011-01 | Domicilio gratis desde un monto | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | No aplica | — |
| CP-TIE-011-02 | Tarifa por distancia | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | No aplica | — |
| CP-TIE-012-01 | Solo medios activos | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-012-02 | Desactivar un medio de pago | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-013-01 | Tienda cerrada | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-013-02 | Tienda abierta | Alta | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-014-01 | Detalle público de la tienda | Media | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-014-02 | Documentos privados ocultos | Media | Integración (API) | Francisco Castro | 2026-11-22 | Aprobado | — |
| CP-TIE-015-01 | Indicadores correctos | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| CP-TIE-015-02 | Tienda sin actividad | Baja | Unitaria | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| **PUNTA A PUNTA** | | | | | | | |
| E2E-01 | Publicar y recibir respuesta | Alta | Punta a punta (Playwright) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| E2E-02 | Elegir y entregar a domicilio | Alta | Punta a punta (Playwright) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| E2E-03 | Aprobar una tienda con segundo factor | Alta | Punta a punta (Playwright) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| E2E-04 | Calificar después de la entrega | Media | Punta a punta (Playwright) | Yanimar Burbano | 2026-11-22 | Aprobado | — |
| E2E-05 | Solicitud sin respuestas que amplía el radio y vence | Media | Punta a punta (Playwright) | Yanimar Burbano | 2026-11-22 | Aprobado | DEF-016 |
| **NO FUNCIONALES** | | | | | | | |
| NF-REN-001 | Tiempo de respuesta de la API en operaciones de lectura y escritura | — | No funcional (k6) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-REN-002 | Tiempo desde que se publica una solicitud hasta que llega al panel de las tiendas elegibles | — | No funcional (k6 + cliente SSE) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-REN-004 | Consumo de memoria en el servidor | — | No funcional (docker stats) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-REN-005 | Carga del frontend en el celular | — | No funcional (Lighthouse (móvil)) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-SEG-010 | Cifrado del transporte | — | No funcional (SSL Labs) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-FIA-017 | Copias de seguridad y restauración | — | No funcional (pg_dump / restauración) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-DIS-018 | Disponibilidad del servicio en el periodo de evaluación | — | No funcional (Monitor externo (UptimeRobot)) | Francisco Castro | 2026-11-22 | No ejecutado | — |
| NF-DIS-019 | Despliegues sin interrumpir la demo | — | No funcional (Script de despliegue + monitor) | Francisco Castro | 2026-11-22 | Aprobado | — |
| NF-ESC-020 | Capacidad en el servidor actual | — | No funcional (k6) | Francisco Castro | 2026-11-22 | Fallido | DEF-018 |
| NF-COM-026 | Navegadores y dispositivos soportados | — | No funcional (Manual (matriz de navegadores)) | Pedro | 2026-11-22 | Aprobado | — |
| NF-USA-027 | Rapidez para publicar y responder | — | No funcional (Prueba con 5 usuarios) | — | 2026-11-22 | No aplica | — |
| NF-USA-028 | Accesibilidad para usuarios poco técnicos | — | No funcional (Lighthouse + axe + checklist WCAG) | Francisco Castro | 2026-11-22 | Fallido | DEF-017 |
| NF-POR-029 | Despliegue reproducible | — | No funcional (Manual de despliegue en servidor limpio) | — | 2026-11-22 | No aplica | — |

## 3. Defectos por severidad y estado

Se listan los defectos abiertos y los nuevos del ciclo. DEF-001 a DEF-011 (C1) y DEF-013 a DEF-015 (C2) están cerrados y no se reabrieron en la regresión.

| ID | Severidad | Prioridad | Estado | Caso | Qué falla | Versión |
|---|---|---|---|---|---|---|
| DEF-012 | S4 Baja | P4 Baja | Abierto (sin triaje) | Humo 21/11 (CP-ORD-029-01) | En «Mis respuestas» de la tienda, las respuestas cuyo pedido ya se canceló o se recibió siguen diciendo «Elegida» | v0.5.2 |
| DEF-016 | S4 Baja | P4 Baja | Nuevo | E2E-05 | Al vencer la solicitud, la pantalla dice que venció y debajo «Te avisamos en esta pantalla apenas una tienda conteste» durante hasta un minuto | v0.5.4 |
| DEF-017 | S4 Baja | P4 Baja | Nuevo | NF-USA-028 | Los botones + y − del mapa miden 30 × 30 px, por debajo de los 44 × 44 px | v0.5.4 |
| DEF-018 | S3 Media | P3 Normal | Nuevo | NF-ESC-020 | En la primera corrida de carga con 100 usuarios, 8 de 50 conexiones SSE se cerraron antes de terminar (causa sin confirmar) | v0.5.4 |

Abiertos por severidad: S1 = 0 · S2 = 0 · S3 = 1 (DEF-018) · S4 = 3 (DEF-012, DEF-016, DEF-017)

## 4. Requisitos y trazabilidad

Resumen de `libro/07-TRAZABILIDAD.md`. De los 38 requisitos funcionales, 36 están verificados y 2 quedan fuera de alcance por SC-021 (RF-006 y RF-011). De los 30 no funcionales, 25 están verificados, 2 tienen fallas, 2 quedan fuera de alcance por SC-022 (RNF-027 y RNF-029) y 1 sigue sin ejecutar (RNF-018).

| Requisito | Nombre | Casos | Apr. | Fall. | Bloq. | Defectos | Estado |
|---|---|---|---|---|---|---|---|
| RF-001 a RF-038 (salvo RF-006 y RF-011) | 36 requisitos funcionales | 82 | 82 | 0 | 0 | 2 abiertos S4 (DEF-012 en RF-029, DEF-016 en RF-021) | Verificado |
| RF-006 | Verificar correo | 2 | 0 | 0 | 0 | — | Fuera de alcance (SC-021) |
| RF-011 | Tarifas de domicilio avanzadas | 2 | 0 | 0 | 0 | — | Fuera de alcance (SC-021) |
| RF-015 | Ver resumen de indicadores | 2 | 2 | 0 | 0 | — | Verificado (nuevo en el C3, comprobado en producción) |
| RF-022 | Ampliar radio automáticamente | 2 + E2E-05 | 3 | 0 | 0 | — | Verificado (nuevo en el C3, comprobado en producción) |
| RF-026 | Ver cómo llegar | 1 | 1 | 0 | 0 | — | Verificado (recorrido manual en producción) |
| RNF-001, 002, 004, 005, 010, 017 y 019 | No funcionales del C3 | 7 | 7 | 0 | 0 | — | Verificado |
| RNF-020 | Escalabilidad (100 usuarios y 50 SSE) | 1 | 0 | 1 | 0 | DEF-018 (S3) | Con fallas |
| RNF-028 | Accesibilidad | 1 | 0 | 1 | 0 | DEF-017 (S4) | Con fallas |
| RNF-018, 026, 027 y 029 | Disponibilidad, compatibilidad, usabilidad y portabilidad | 4 | 0 | 0 | 0 | — | Sin ejecutar |
| 17 RNF del C1 y del C2 | Seguridad, mantenibilidad, fiabilidad y otros | 17 | 17 | 0 | 0 | — | Verificado (la regresión los repite en verde) |

## 5. Resultados no funcionales

| Caso | Requisito | Meta | Medido | Resultado |
|---|---|---|---|---|
| NF-REN-001 | RNF-001 | p95 menor o igual a 500 ms en prueba de carga con k6 o JMeter. | p95 de la API 173,6 ms con 50 usuarios durante 3 min; 0 % de error (54.733 peticiones) | Aprobado |
| NF-REN-002 | RNF-002 | p95 menor o igual a 3 s. | p95 117 ms y máximo 187 ms entre publicar y recibir «solicitud-nueva» en la tienda (20 de 20 llegaron) | Aprobado |
| NF-REN-004 | RNF-004 | API como máximo 640 MB y todo el stack como máximo 2 GB de los 4 GB del VPS. | Pico: API 606,9 MiB; stack ≈ 833 MiB (API 598 + Caddy 126,5 + base 108); VPS 1.460 de 3.920 MiB | Aprobado |
| NF-REN-005 | RNF-005 | Mayor o igual a 80 y primera carga de 3 s o menos en 4G simulado. | Rendimiento 97 a 98; primera carga (LCP) 2,0 a 2,2 s; FCP 1,7 a 1,8 s | Aprobado |
| NF-SEG-010 | RNF-010 | Solo TLS 1.2 y 1.3, HSTS activo y calificación A o superior. | Solo TLS 1.2 y 1.3 (1.0 y 1.1 rechazados con alerta protocol_version); HSTS max-age=31536000; SSL Labs A+ en IPv4 e IPv6 | Aprobado |
| NF-FIA-017 | RNF-017 | RPO de 24 h, RTO de 4 h y una restauración probada antes de la entrega. | Respaldo diario (último 13 h antes); restauración de prueba de la base en 22 s, 11 esquemas y 24 documentos | Aprobado |
| NF-DIS-018 | RNF-018 | Mayor o igual a 99 % entre el 16/11/2026 y el 26/11/2026. | Monitor creado el 22/11 a las 4:43 p. m.: 100 % hasta ahora | No ejecutado |
| NF-DIS-019 | RNF-019 | Como máximo 60 s por despliegue. | 28,3 s sin API al recrear la API y Caddy (la PWA siguió cargando) | Aprobado |
| NF-ESC-020 | RNF-020 | 100 usuarios concurrentes y 50 conexiones SSE con tasa de error menor a 1 %. | 3 corridas con 100 usuarios y 50 SSE: HTTP 0,006 %, 0 % y 0 % de error; SSE 16 %, 0 % y 0 % | Fallido |
| NF-COM-026 | RNF-026 | Funciona en las dos últimas versiones de Chrome, Edge, Firefox y Safari, en Android 10+ e iOS 16.4+. | iPhone 17 Pro Max (iOS 27.2.1) con Safari y Chrome, Galaxy A57 5G (Android 17) con Chrome y Firefox, y Edge en Windows: publicar y respuesta en vivo en los 5 (versión actual de cada navegador) | Aprobado |
| NF-USA-027 | RNF-027 | Publicar una solicitud en 60 s o menos; responder Sí en 3 toques o menos; No lo tengo en 1 toque. | Fuera de alcance por SC-022 | No aplica |
| NF-USA-028 | RNF-028 | Mayor o igual a 90, texto base de 16 px o más, botones de 44 × 44 px o más y contraste WCAG 2.2 AA. | Lighthouse accesibilidad 100 en 3 páginas; texto base 16 px; botones de 44 px salvo + y − del mapa (30 px) | Fallido |
| NF-POR-029 | RNF-029 | 15 minutos o menos siguiendo el manual de despliegue. | Fuera de alcance por SC-022 | No aplica |

## 6. Casos bloqueados

Ninguno. Los no funcionales que no se ejecutan no están bloqueados por un defecto: necesitan personas, equipos o tiempo que el proyecto no tiene.

| Causa | Casos | Detalle | Qué se habría requerido |
|---|---|---|---|
| Periodo de medición | NF-DIS-018 | No había monitor externo antes del 22/11; UptimeRobot se creó el 22/11 a las 4:43 p. m. y lleva 100 % | Captura del porcentaje el 26/11. El periodo del 16/11 al 22/11 no tiene medición, así que el comité decide si acepta el periodo medido |
| Personas (SC-022) | NF-USA-027 | Prueba de tareas con 5 usuarios (publicar en 60 s, responder Sí en 3 toques, No lo tengo en 1). No se ejecuta: el resultado depende de conseguir usuarios externos | 5 participantes y una sesión de 1 hora, en una liberación posterior |
| Servidor limpio (SC-022) | NF-POR-029 | Seguir el manual de despliegue en un servidor nuevo y medir que tarda 15 minutos o menos. No se ejecuta por el costo del servidor y el subdominio de prueba | Un VPS o una máquina virtual vacía y una sección de instalación desde cero en `DESPLIEGUE.md` |

## 7. Observaciones

- Los escenarios de punta a punta corrieron contra una pila local (PostGIS, la API y la PWA construidas desde la etiqueta `v0.5.4`) y no contra el VPS. E2E-03 necesita el segundo factor de un administrador, y el de producción es personal; así tampoco quedan pedidos ni calificaciones de prueba en producción. El catálogo (`02-CASOS.md` §3) pedía el VPS: se registra como desviación para que QA la acepte o pida repetirlos allí.
- Las pruebas de carga se lanzaron desde el entorno de pruebas contra `https://quienlotiene.tech`, así que los tiempos incluyen la red hasta el VPS. Con 100 usuarios el p95 de la API varió entre 280 y 547 ms en las 3 corridas.
- Caddy llegó a 126,5 de sus 128 MiB de límite de memoria durante la carga (NF-REN-004). Conviene subir su `mem_limit` antes de repetir NF-ESC-020 (DEF-018).
- La PWA usa `registerType: 'prompt'`: un navegador sigue con la versión anterior hasta que el usuario acepta el aviso de actualización. Al comprobar `v0.5.4` en producción hubo que activar el service worker nuevo.
- En producción quedaron datos de prueba: cuentas `@prueba.quienlotiene.tech` (50 clientes de carga y los de las comprobaciones), «Tienda de prueba de carga», solicitudes abiertas y el pedido 40233FE4. Se borran con `limpiar.sh pruebas` cuando QA lo indique.
- Los guiones quedaron en el repositorio para repetir las pruebas: `e2e/` (Playwright), `scripts/carga/` (k6 y SSE) y `scripts/usabilidad/` (texto y botones a 375 px).
- Las horas de la bitácora son estimadas.

## 8. Criterios de salida del ciclo

| Criterio (Plan Maestro 6.3) | Estado (22/11) |
|---|---|
| 100 % de casos del ciclo ejecutados | No cumple todavía: 97 de 98. Falta NF-DIS-018 (26/11); NF-USA-027 y NF-POR-029 salen por SC-022 si el comité la aprueba |
| Todo Fallido con defecto registrado | Cumple: NF-ESC-020 con DEF-018 y NF-USA-028 con DEF-017 |
| Cobertura ≥ 70 % en los módulos del ciclo | Cumple: mínimo 84,4 % (clientes); total 95,5 % |
| 0 defectos S1 y S2 abiertos | Cumple |
| (C3) ≥ 95 % aprobados en el resultado final | Cumple hoy: 97,9 % (95 de 97). Con NF-DIS-018 queda en 96 de 98 (98,0 %) si se aprueba o 95 de 98 (96,9 %) si falla |
| (C3) E2E-01 a E2E-03 aprobados | Cumple: los 5 escenarios aprobados |

## 9. Conclusión y recomendación

El C3 cumple hoy los criterios de calidad (97,9 % de aprobación, 0 S1 y S2, cobertura de 95,5 % y los 5 escenarios de punta a punta aprobados), pero no se puede cerrar todavía: faltan NF-DIS-018, la decisión del comité sobre SC-022, el triaje de 4 defectos y la firma de QA.

Recomendaciones para el equipo:

1. QA hace el triaje de DEF-012, DEF-016, DEF-017 y DEF-018 antes del comité. DEF-016 y DEF-017 son correcciones pequeñas de la interfaz que pueden salir en una `v0.5.5` antes del 26/11; si no, se aceptan en el acta como S4 con plan.
2. Para DEF-018: subir el `mem_limit` de Caddy, revisar su log durante la carga y repetir NF-ESC-020 desde otra red. Si las 3 corridas cumplen, NF-ESC-020 pasa a Aprobado y DEF-018 se cierra.
3. El comité decide SC-022 (NF-USA-027 y NF-POR-029 fuera de esta liberación), y el 26/11 se toma la captura de UptimeRobot para NF-DIS-018.
4. Las pruebas de humo del 26/11 se hacen después del despliegue final con `plantillas/PRUEBAS-DE-HUMO.md`, y después se borran los datos de prueba de producción.
5. QA (Yanimar Burbano y Francisco Castro) revisa este borrador y lo firma; el PO (Wilson Galindez) lo aprueba. Este documento no lleva firmas en su nombre.

| Elaborado por | Revisado / aprobado por |
|---|---|
| Yanimar Burbano Piamba · Líder de QA · Firma: | Wilson Javier Galindez Certuche · Product Owner · Firma: |
| Francisco José Castro Muñoz · QA no funcional · Firma: | |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador con las cifras del 22/11 sobre `v0.5.4`: 94 de 96 aprobados, DEF-016 a DEF-018 nuevos y 4 no funcionales pendientes. Pendiente: triaje de defectos y firma de QA |
| 0.2 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | NF-COM-026 aprobado con la matriz de navegadores (iPhone, Android y Edge): 95 de 97 aprobados y 3 no funcionales pendientes |
| 0.3 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | SC-022 (propuesta): NF-USA-027 y NF-POR-029 «No aplica»; 98 casos planeados y solo NF-DIS-018 pendiente |
