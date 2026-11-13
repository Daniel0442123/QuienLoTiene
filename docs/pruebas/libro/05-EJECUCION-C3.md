# Hoja «Ejecución C3» · Cierre (23/11 al 25/11) · Regresión y sistema integrado

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Libro de control de QA · Quién lo tiene · Byte Force. Fuente en Markdown del Excel «Libro-Control-QA-QuienLoTiene.xlsx». Solo se diligencian las columnas vacías; los catálogos los mantiene QA.

Alcance: casos de las historias Could que se hayan implementado (10), punta a punta (5), no funcionales sobre el VPS (13) y la regresión completa de los 85 casos. El «Resultado final» de cada caso es el de C3; es el que cuenta para el criterio de liberación (≥ 95 % aprobados).

## 1. Regresión completa y casos del Cierre

| ID del caso | HU | Título | Prioridad | Ciclo de origen | Resultado C1/C2 | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) | Resultado final |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CP-IDE-001-01 | HU-001 | Registro exitoso de cliente | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-001-02 | HU-001 | Correo ya registrado | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-001-03 | HU-001 | Frase de contraseña débil | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-002-01 | HU-002 | Registro de tienda queda pendiente | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-002-02 | HU-002 | Registro de tienda sin documento | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-003-01 | HU-003 | Inicio de sesión correcto | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-003-02 | HU-003 | Credenciales incorrectas | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-003-03 | HU-003 | Cuenta suspendida | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-004-01 | HU-004 | Administrador con segundo factor válido | Media | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-004-02 | HU-004 | Segundo factor inválido | Media | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-005-01 | HU-005 | Registro sin autorización de datos | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-005-02 | HU-005 | Autorización registrada | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-IDE-006-01 | HU-006 | Verificación de correo exitosa | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-IDE-006-02 | HU-006 | Enlace vencido | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-CLI-007-01 | HU-007 | Guardar una dirección | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-CLI-007-02 | HU-007 | Dirección incompleta | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-CLI-008-01 | HU-008 | Repetir una solicitud | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-CLI-008-02 | HU-008 | Historial vacío | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-TIE-009-01 | HU-009 | Horario fuera de atención | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-009-02 | HU-009 | Cambios de perfil visibles | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-010-01 | HU-010 | Tarifa fija en la respuesta | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-010-02 | HU-010 | Tarifa inválida | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-011-01 | HU-011 | Domicilio gratis desde un monto | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-TIE-011-02 | HU-011 | Tarifa por distancia | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-TIE-012-01 | HU-012 | Solo medios activos | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-012-02 | HU-012 | Desactivar un medio de pago | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-013-01 | HU-013 | Tienda cerrada | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-013-02 | HU-013 | Tienda abierta | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-TIE-014-01 | HU-014 | Detalle público de la tienda | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-TIE-014-02 | HU-014 | Documentos privados ocultos | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-TIE-015-01 | HU-015 | Indicadores correctos | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-TIE-015-02 | HU-015 | Tienda sin actividad | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-SOL-016-01 | HU-016 | Publicar una solicitud | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-016-02 | HU-016 | Solicitud incompleta | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-016-03 | HU-016 | Límite de solicitudes | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-017-01 | HU-017 | Difusión solo a tiendas elegibles | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-017-02 | HU-017 | Categoría distinta | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-017-03 | HU-017 | Tienda suspendida | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-018-01 | HU-018 | Privacidad en la tarjeta de solicitud | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-018-02 | HU-018 | Distancia aproximada | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-019-01 | HU-019 | Responder Sí lo tengo | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-019-02 | HU-019 | Alternativa sin descripción | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-019-03 | HU-019 | No lo tengo en un toque | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-019-04 | HU-019 | Respuesta duplicada | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-020-01 | HU-020 | Respuestas en tiempo real | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-020-02 | HU-020 | Ordenar por precio | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-020-03 | HU-020 | Alternativa marcada | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-021-01 | HU-021 | Vencimiento de la solicitud | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-021-02 | HU-021 | Aviso a tiendas no elegidas | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-SOL-022-01 | HU-022 | Ampliación del radio | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-SOL-022-02 | HU-022 | Sin ampliación si hay respuestas | Baja | C3 | — |  |  |  |  |  |  |  |  |
| CP-SOL-023-01 | HU-023 | Producto restringido | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-SOL-023-02 | HU-023 | Restringido sin domicilio | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ORD-024-01 | HU-024 | Crear orden con total correcto | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-024-02 | HU-024 | La tienda elegida ve los datos del cliente | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-024-03 | HU-024 | Una tienda no elegida no ve los datos | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-024-04 | HU-024 | Medio de pago no aceptado | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-025-01 | HU-025 | Reserva vencida | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ORD-025-02 | HU-025 | Recogida a tiempo | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ORD-026-01 | HU-026 | Cómo llegar | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ORD-027-01 | HU-027 | Transición válida | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-027-02 | HU-027 | Transición inválida | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-028-01 | HU-028 | Confirmar recepción | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-028-02 | HU-028 | Confirmar una orden ajena | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ORD-029-01 | HU-029 | Cancelar antes del despacho | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ORD-029-02 | HU-029 | Cancelar después del despacho | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-NOT-030-01 | HU-030 | Evento en tiempo real | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-NOT-030-02 | HU-030 | Solicitud urgente | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-NOT-031-01 | HU-031 | Push con la app cerrada | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-NOT-031-02 | HU-031 | Sin autorización de notificaciones | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-MEN-032-01 | HU-032 | Botón de WhatsApp con orden activa | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-MEN-032-02 | HU-032 | Sin botón antes de elegir | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-MEN-033-01 | HU-033 | Enviar un mensaje | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-MEN-033-02 | HU-033 | Chat cerrado | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-REP-034-01 | HU-034 | Calificar una orden resuelta | Alta | C2 |  |  |  |  |  |  |  |  |  |
| CP-REP-034-02 | HU-034 | Calificar sin orden resuelta | Alta | C2 |  |  |  |  |  |  |  |  |  |
| CP-REP-034-03 | HU-034 | Calificar dos veces | Alta | C2 |  |  |  |  |  |  |  |  |  |
| CP-REP-035-01 | HU-035 | Crear un reporte | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-REP-035-02 | HU-035 | Reportar una orden ajena | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ADM-036-01 | HU-036 | Aprobar una tienda | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ADM-036-02 | HU-036 | Rechazar una tienda | Alta | C1 |  |  |  |  |  |  |  |  |  |
| CP-ADM-037-01 | HU-037 | Suspender una cuenta | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-ADM-037-02 | HU-037 | Resolver un reporte | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-CAT-038-01 | HU-038 | Desactivar una categoría | Media | C2 |  |  |  |  |  |  |  |  |  |
| CP-CAT-038-02 | HU-038 | Agregar un producto restringido | Media | C2 |  |  |  |  |  |  |  |  |  |

## 2. Punta a punta (Playwright)

| ID | Escenario | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|
| E2E-01 | Publicar y recibir respuesta |  |  |  |  |  |  |  |
| E2E-02 | Elegir y entregar a domicilio |  |  |  |  |  |  |  |
| E2E-03 | Aprobar una tienda con segundo factor |  |  |  |  |  |  |  |
| E2E-04 | Calificar después de la entrega |  |  |  |  |  |  |  |
| E2E-05 | Solicitud sin respuestas que amplía el radio y vence |  |  |  |  |  |  |  |

## 3. No funcionales sobre el VPS

| ID del caso | Requisito | Criterio | Herramienta | Medido | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|---|---|---|
| NF-REN-001 | RNF-001 | p95 menor o igual a 500 ms en prueba de carga con k6 o JMeter. | k6 |  |  |  |  |  |  |  |  |
| NF-REN-002 | RNF-002 | p95 menor o igual a 3 s. | k6 + cliente SSE |  |  |  |  |  |  |  |  |
| NF-REN-004 | RNF-004 | API como máximo 640 MB y todo el stack como máximo 2 GB de los 4 GB del VPS. | docker stats |  |  |  |  |  |  |  |  |
| NF-REN-005 | RNF-005 | Mayor o igual a 80 y primera carga de 3 s o menos en 4G simulado. | Lighthouse (móvil) |  |  |  |  |  |  |  |  |
| NF-SEG-010 | RNF-010 | Solo TLS 1.2 y 1.3, HSTS activo y calificación A o superior. | SSL Labs |  |  |  |  |  |  |  |  |
| NF-FIA-017 | RNF-017 | RPO de 24 h, RTO de 4 h y una restauración probada antes de la entrega. | pg_dump / restauración |  |  |  |  |  |  |  |  |
| NF-DIS-018 | RNF-018 | Mayor o igual a 99 % entre el 16/11/2026 y el 26/11/2026. | Monitor externo (UptimeRobot) |  |  |  |  |  |  |  |  |
| NF-DIS-019 | RNF-019 | Como máximo 60 s por despliegue. | Script de despliegue + monitor |  |  |  |  |  |  |  |  |
| NF-ESC-020 | RNF-020 | 100 usuarios concurrentes y 50 conexiones SSE con tasa de error menor a 1 %. | k6 |  |  |  |  |  |  |  |  |
| NF-COM-026 | RNF-026 | Funciona en las dos últimas versiones de Chrome, Edge, Firefox y Safari, en Android 10+ e iOS 16.4+. | Manual (matriz de navegadores) |  |  |  |  |  |  |  |  |
| NF-USA-027 | RNF-027 | Publicar una solicitud en 60 s o menos; responder Sí en 3 toques o menos; No lo tengo en 1 toque. | Prueba con 5 usuarios |  |  |  |  |  |  |  |  |
| NF-USA-028 | RNF-028 | Mayor o igual a 90, texto base de 16 px o más, botones de 44 × 44 px o más y contraste WCAG 2.2 AA. | Lighthouse + axe + checklist WCAG |  |  |  |  |  |  |  |  |
| NF-POR-029 | RNF-029 | 15 minutos o menos siguiendo el manual de despliegue. | Manual de despliegue en servidor limpio |  |  |  |  |  |  |  |  |

## 4. Pruebas de humo en producción (26/11)

Se diligencian con la plantilla `plantillas/PRUEBAS-DE-HUMO.md` después del despliegue final.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(front): andamiaje del frontend y decisiones de usabilidad y accesibilidad (#17) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
