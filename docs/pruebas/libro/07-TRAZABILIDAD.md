# Hoja «Trazabilidad» · Matriz de trazabilidad de requisitos (RTM)

> Libro de control de QA · Quién lo tiene · Byte Force. Fuente en Markdown del Excel «Libro-Control-QA-QuienLoTiene.xlsx». Solo se diligencian las columnas vacías; los catálogos los mantiene QA.

Requisito → historia → casos → pruebas automatizadas → resultado. Estado del requisito: Verificado (todos aprobados) · Verificado parcialmente (aprobados y bloqueados) · Con fallas (al menos un fallido) · Bloqueado (sin verificar) · Sin ejecutar. «¿Cobertura completa?» = Sí cuando cada criterio de aceptación tiene al menos una prueba automatizada o manual registrada.

## Requisitos funcionales

| Requisito | Nombre | HU | MoSCoW | Módulo | Sprint | Casos | Ref. SRS | N.º casos | Pruebas JUnit / Cucumber | ¿Cobertura completa? | Aprobados | Fallidos | Bloqueados | Defectos abiertos | Estado del requisito |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| RF-001 | Registrar cliente | HU-001 | Must | identidad | Sprint 1 | CP-IDE-001-01, CP-IDE-001-02, CP-IDE-001-03 | CP-001, CP-002, CP-003 | 3 |  |  |  |  |  |  |  |
| RF-002 | Registrar tienda | HU-002 | Must | tiendas | Sprint 1 | CP-TIE-002-01, CP-TIE-002-02 | CP-004, CP-005 | 2 |  |  |  |  |  |  |  |
| RF-003 | Iniciar y cerrar sesión | HU-003 | Must | identidad | Sprint 1 | CP-IDE-003-01, CP-IDE-003-02, CP-IDE-003-03 | CP-006, CP-007, CP-008 | 3 |  |  |  |  |  |  |  |
| RF-004 | Segundo factor del administrador | HU-004 | Should | identidad | Sprint 1 | CP-IDE-004-01, CP-IDE-004-02 | CP-009, CP-010 | 2 |  |  |  |  |  |  |  |
| RF-005 | Aceptar política de datos | HU-005 | Must | identidad | Sprint 1 | CP-IDE-005-01, CP-IDE-005-02 | CP-011, CP-012 | 2 |  |  |  |  |  |  |  |
| RF-006 | Verificar correo | HU-006 | Could | identidad | Cierre | CP-IDE-006-01, CP-IDE-006-02 | CP-013, CP-014 | 2 |  |  |  |  |  |  |  |
| RF-007 | Gestionar perfil y direcciones | HU-007 | Must | clientes | Sprint 1 | CP-CLI-007-01, CP-CLI-007-02 | CP-015, CP-016 | 2 |  |  |  |  |  |  |  |
| RF-008 | Consultar historial y repetir | HU-008 | Could | clientes | Cierre | CP-CLI-008-01, CP-CLI-008-02 | CP-017, CP-018 | 2 |  |  |  |  |  |  |  |
| RF-009 | Configurar perfil de tienda | HU-009 | Must | tiendas | Sprint 1 | CP-TIE-009-01, CP-TIE-009-02 | CP-019, CP-020 | 2 |  |  |  |  |  |  |  |
| RF-010 | Configurar domicilio con tarifa fija | HU-010 | Must | tiendas | Sprint 1 | CP-TIE-010-01, CP-TIE-010-02 | CP-021, CP-022 | 2 |  |  |  |  |  |  |  |
| RF-011 | Tarifas de domicilio avanzadas | HU-011 | Could | tiendas | Cierre | CP-TIE-011-01, CP-TIE-011-02 | CP-023, CP-024 | 2 |  |  |  |  |  |  |  |
| RF-012 | Configurar medios de pago | HU-012 | Must | tiendas | Sprint 1 | CP-TIE-012-01, CP-TIE-012-02 | CP-025, CP-026 | 2 |  |  |  |  |  |  |  |
| RF-013 | Abrir o cerrar tienda | HU-013 | Must | tiendas | Sprint 1 | CP-TIE-013-01, CP-TIE-013-02 | CP-027, CP-028 | 2 |  |  |  |  |  |  |  |
| RF-014 | Ver detalle de tienda | HU-014 | Should | tiendas | Sprint 3 | CP-TIE-014-01, CP-TIE-014-02 | CP-029, CP-030 | 2 |  |  |  |  |  |  |  |
| RF-015 | Ver resumen de indicadores | HU-015 | Could | tiendas | Cierre | CP-TIE-015-01, CP-TIE-015-02 | CP-031, CP-032 | 2 |  |  |  |  |  |  |  |
| RF-016 | Publicar solicitud | HU-016 | Must | solicitudes | Sprint 1 | CP-SOL-016-01, CP-SOL-016-02, CP-SOL-016-03 | CP-033, CP-034, CP-035 | 3 |  |  |  |  |  |  |  |
| RF-017 | Difundir a tiendas elegibles | HU-017 | Must | solicitudes | Sprint 1 | CP-SOL-017-01, CP-SOL-017-02, CP-SOL-017-03 | CP-036, CP-037, CP-038 | 3 |  |  |  |  |  |  |  |
| RF-018 | Ver solicitudes nuevas | HU-018 | Must | solicitudes | Sprint 2 | CP-SOL-018-01, CP-SOL-018-02 | CP-039, CP-040 | 2 |  |  |  |  |  |  |  |
| RF-019 | Responder solicitud | HU-019 | Must | solicitudes | Sprint 2 | CP-SOL-019-01, CP-SOL-019-02, CP-SOL-019-03, CP-SOL-019-04 | CP-041, CP-042, CP-043, CP-044 | 4 |  |  |  |  |  |  |  |
| RF-020 | Ver y ordenar respuestas | HU-020 | Must | solicitudes | Sprint 2 | CP-SOL-020-01, CP-SOL-020-02, CP-SOL-020-03 | CP-045, CP-046, CP-047 | 3 |  |  |  |  |  |  |  |
| RF-021 | Vencer y cerrar solicitudes | HU-021 | Must | solicitudes | Sprint 2 | CP-SOL-021-01, CP-SOL-021-02 | CP-048, CP-049 | 2 |  |  |  |  |  |  |  |
| RF-022 | Ampliar radio automáticamente | HU-022 | Could | solicitudes | Cierre | CP-SOL-022-01, CP-SOL-022-02 | CP-050, CP-051 | 2 |  |  |  |  |  |  |  |
| RF-023 | Advertir productos restringidos | HU-023 | Should | solicitudes | Sprint 3 | CP-SOL-023-01, CP-SOL-023-02 | CP-052, CP-053 | 2 |  |  |  |  |  |  |  |
| RF-024 | Elegir respuesta y crear orden | HU-024 | Must | ordenes | Sprint 2 | CP-ORD-024-01, CP-ORD-024-02, CP-ORD-024-03, CP-ORD-024-04 | CP-054, CP-055, CP-056, CP-057 | 4 |  |  |  |  |  |  |  |
| RF-025 | Reservar para recogida | HU-025 | Should | ordenes | Sprint 3 | CP-ORD-025-01, CP-ORD-025-02 | CP-058, CP-059 | 2 |  |  |  |  |  |  |  |
| RF-026 | Ver cómo llegar | HU-026 | Should | ordenes | Sprint 3 | CP-ORD-026-01 | CP-060 | 1 |  |  |  |  |  |  |  |
| RF-027 | Actualizar estado de la orden | HU-027 | Must | ordenes | Sprint 2 | CP-ORD-027-01, CP-ORD-027-02 | CP-061, CP-062 | 2 |  |  |  |  |  |  |  |
| RF-028 | Confirmar recepción | HU-028 | Must | ordenes | Sprint 2 | CP-ORD-028-01, CP-ORD-028-02 | CP-063, CP-064 | 2 |  |  |  |  |  |  |  |
| RF-029 | Cancelar orden | HU-029 | Should | ordenes | Sprint 3 | CP-ORD-029-01, CP-ORD-029-02 | CP-065, CP-066 | 2 |  |  |  |  |  |  |  |
| RF-030 | Notificar en tiempo real | HU-030 | Must | notificaciones | Sprint 2 | CP-NOT-030-01, CP-NOT-030-02 | CP-067, CP-068 | 2 |  |  |  |  |  |  |  |
| RF-031 | Notificaciones push web | HU-031 | Should | notificaciones | Sprint 3 | CP-NOT-031-01, CP-NOT-031-02 | CP-069, CP-070 | 2 |  |  |  |  |  |  |  |
| RF-032 | Contactar por WhatsApp | HU-032 | Must | mensajeria | Sprint 2 | CP-MEN-032-01, CP-MEN-032-02 | CP-071, CP-072 | 2 |  |  |  |  |  |  |  |
| RF-033 | Mini chat | HU-033 | Should | mensajeria | Sprint 3 | CP-MEN-033-01, CP-MEN-033-02 | CP-073, CP-074 | 2 |  |  |  |  |  |  |  |
| RF-034 | Calificar tienda | HU-034 | Must | reputacion | Sprint 3 | CP-REP-034-01, CP-REP-034-02, CP-REP-034-03 | CP-075, CP-076, CP-077 | 3 |  |  |  |  |  |  |  |
| RF-035 | Reportar problema | HU-035 | Should | reputacion | Sprint 3 | CP-REP-035-01, CP-REP-035-02 | CP-078, CP-079 | 2 |  |  |  |  |  |  |  |
| RF-036 | Aprobar o rechazar tiendas | HU-036 | Must | administracion | Sprint 1 | CP-ADM-036-01, CP-ADM-036-02 | CP-080, CP-081 | 2 |  |  |  |  |  |  |  |
| RF-037 | Gestionar reportes y suspender | HU-037 | Should | administracion | Sprint 3 | CP-ADM-037-01, CP-ADM-037-02 | CP-082, CP-083 | 2 |  |  |  |  |  |  |  |
| RF-038 | Gestionar categorías y restringidos | HU-038 | Should | catalogo | Sprint 3 | CP-CAT-038-01, CP-CAT-038-02 | CP-084, CP-085 | 2 |  |  |  |  |  |  |  |

RF-039 a RF-042 son Won't: sin casos en esta entrega.

## Requisitos no funcionales

| Requisito | Caso | Criterio | Ciclo | Resultado | Defectos abiertos | Estado |
|---|---|---|---|---|---|---|
| RNF-001 | NF-REN-001 | p95 menor o igual a 500 ms en prueba de carga con k6 o JMeter. | C3 |  |  |  |
| RNF-002 | NF-REN-002 | p95 menor o igual a 3 s. | C3 |  |  |  |
| RNF-003 | NF-REN-003 | Menor o igual a 200 ms usando índice GiST. | C2 |  |  |  |
| RNF-004 | NF-REN-004 | API como máximo 640 MB y todo el stack como máximo 2 GB de los 4 GB del VPS. | C3 |  |  |  |
| RNF-005 | NF-REN-005 | Mayor o igual a 80 y primera carga de 3 s o menos en 4G simulado. | C3 |  |  |  |
| RNF-006 | NF-SEG-006 | Mínimo 15 caracteres, hash Argon2id o bcrypt, rechazo de contraseñas de la lista de filtradas. | C1 |  |  |  |
| RNF-007 | NF-SEG-007 | 0 accesos indebidos; cada endpoint con datos personales tiene prueba negativa. | C2 |  |  |  |
| RNF-008 | NF-SEG-008 | JWT de 15 minutos; refresco rotativo en cookie HttpOnly, Secure y SameSite=Strict. | C2 |  |  |  |
| RNF-009 | NF-SEG-009 | Máximo 5 intentos de inicio de sesión por minuto por cuenta y 10 solicitudes por hora por cliente; el exceso recibe HTTP 429. | C2 |  |  |  |
| RNF-010 | NF-SEG-010 | Solo TLS 1.2 y 1.3, HSTS activo y calificación A o superior. | C3 |  |  |  |
| RNF-011 | NF-SEG-011 | 0 críticas y 0 altas sin justificar en el build de entrega. | C1 |  |  |  |
| RNF-012 | NF-SEG-012 | Solo JPEG, PNG, WebP y PDF de hasta 5 MB, tipo verificado por contenido y metadatos EXIF eliminados. | C2 |  |  |  |
| RNF-013 | NF-SEG-013 | 100 % registrados con usuario, fecha y acción, sin datos personales en los logs. | C2 |  |  |  |
| RNF-014 | NF-SEG-014 | 100 % con fecha y versión de la política aceptada. | C1 |  |  |  |
| RNF-015 | NF-FIA-015 | 0 eventos perdidos en la prueba de reinicio. | C2 |  |  |  |
| RNF-016 | NF-FIA-016 | 0 aceptadas; todas las combinaciones probadas. | C1 |  |  |  |
| RNF-017 | NF-FIA-017 | RPO de 24 h, RTO de 4 h y una restauración probada antes de la entrega. | C3 |  |  |  |
| RNF-018 | NF-DIS-018 | Mayor o igual a 99 % entre el 16/11/2026 y el 26/11/2026. | C3 |  |  |  |
| RNF-019 | NF-DIS-019 | Como máximo 60 s por despliegue. | C3 |  |  |  |
| RNF-020 | NF-ESC-020 | 100 usuarios concurrentes y 50 conexiones SSE con tasa de error menor a 1 %. | C3 |  |  |  |
| RNF-021 | NF-ESC-021 | 0 violaciones de dependencias entre módulos. | C1 |  |  |  |
| RNF-022 | NF-MAN-022 | Mayor o igual a 70 %. | C1 |  |  |  |
| RNF-023 | NF-MAN-023 | 0 violaciones en el pipeline. | C1 |  |  |  |
| RNF-024 | NF-MAN-024 | 100 % de endpoints documentados. | C2 |  |  |  |
| RNF-025 | NF-COM-025 | JSON sobre HTTPS con prefijo /api/v1. | C2 |  |  |  |
| RNF-026 | NF-COM-026 | Funciona en las dos últimas versiones de Chrome, Edge, Firefox y Safari, en Android 10+ e iOS 16.4+. | C3 |  |  |  |
| RNF-027 | NF-USA-027 | Publicar una solicitud en 60 s o menos; responder Sí en 3 toques o menos; No lo tengo en 1 toque. | C3 |  |  |  |
| RNF-028 | NF-USA-028 | Mayor o igual a 90, texto base de 16 px o más, botones de 44 × 44 px o más y contraste WCAG 2.1 AA. | C3 |  |  |  |
| RNF-029 | NF-POR-029 | 15 minutos o menos siguiendo el manual de despliegue. | C3 |  |  |  |
| RNF-030 | NF-POR-030 | 0 secretos en el repositorio; toda la configuración por variables de entorno. | C1 |  |  |  |
