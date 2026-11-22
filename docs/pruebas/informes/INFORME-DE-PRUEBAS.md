# Informe de pruebas de software · Quién lo tiene — Liberación 1.0

> **Control del documento** · Versión 0.1 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (se completa con los no funcionales del 24/11 y el 26/11 y se entrega con el acta de liberación) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> **Borrador del 22/11/2026, preparado con Claude Code.** Las cifras salen del libro de control (`docs/pruebas/libro/`) al 22/11/2026. QA lo revisa, completa lo pendiente y lo firma.

Ingeniería de Software III · Byte Force
Responsables de QA: Yanimar Burbano Piamba (pruebas funcionales) · Francisco José Castro Muñoz (no funcionales y seguridad)
Fecha: 22/11/2026 (borrador; entrega el 25/11/2026)

## Contenido

1. Introducción
2. Alcance de las pruebas
3. Plan de pruebas y responsables
4. Ejecución de las pruebas y matriz de requerimientos
5. Hallazgos y su prioridad
6. Documentación en el libro de control
7. Casos que no fue posible realizar y sus causas
8. Recomendaciones y conclusión
Anexo A. Matriz de trazabilidad detallada (86 casos)
Anexo B. Casos no realizados con su causa

## 1. Introducción

Quién lo tiene es una aplicación web progresiva (PWA) que conecta a quien necesita un producto con las tiendas de su barrio: el cliente publica qué busca, las tiendas cercanas que lo tienen responden en tiempo real y el cliente elige, recoge o pide a domicilio. El backend es un monolito modular en Spring Boot con PostgreSQL y PostGIS; la PWA es React. Corre en un VPS en `https://quienlotiene.tech`.

Este informe resume la campaña de pruebas de la Liberación 1.0: qué se probó, cómo, con qué resultado y qué queda abierto para decidir la liberación. Cubre los tres ciclos del Plan Maestro de Pruebas:

| Ciclo | Fechas | Versiones | Qué se probó |
|---|---|---|---|
| C1 | 02/11 al 20/11/2026 | v0.2.0, main `5e8ab98`, v0.3.0 y v0.3.1 | Historias de los Sprints 1 y 2 (53 casos) y 8 no funcionales que corren en CI |
| C2 | 16/11 al 22/11/2026 | v0.5.2 y v0.5.3 | Historias del Sprint 3 (22 casos más 3 de HU-008 por SC-018) y 9 no funcionales de integración y seguridad |
| C3 | 22/11 al 25/11/2026 | v0.5.3 y v0.5.4 | Regresión completa (86 casos), 5 escenarios de punta a punta y 13 no funcionales sobre el VPS |

## 2. Alcance de las pruebas

| Sección del SRS | Requisitos | Casos |
|---|---|---|
| 10.1 Identidad y cuentas | RF-001 a RF-006 | 14 (2 de RF-006 «No aplica» por SC-021) |
| 10.2 Cliente | RF-007, RF-008 | 5 |
| 10.3 Tiendas | RF-009 a RF-015 | 14 (2 de RF-011 «No aplica» por SC-021) |
| 10.4 Solicitudes y respuestas | RF-016 a RF-023 | 21 |
| 10.5 Órdenes | RF-024 a RF-029 | 13 |
| 10.6 Comunicación y notificaciones | RF-030 a RF-033 | 8 |
| 10.7 Reputación | RF-034, RF-035 | 5 |
| 10.8 Administración | RF-036 a RF-038 | 6 |
| 11 No funcionales | RNF-001 a RNF-030 | 30 |

RF-039 a RF-042 son Won't en esta liberación y no tienen casos.

| Tipo de prueba | Casos | Qué cubre |
|---|---|---|
| Unitaria | 70 | Reglas de dominio y casos de uso (JUnit 5); 11 de ellos son de seguridad |
| Integración | 11 | PostGIS (3), API (4), API con Spring Security (1) y SSE (3), con Testcontainers |
| Seguridad | 16 | Autenticación, autorización, privacidad (están contados también en su nivel: 11 unitarias y 5 de integración) |
| Manual de interfaz | 5 | WhatsApp, mapas, push y pantallas (3 de interfaz y 2 en navegador) |
| Extremo a extremo | 5 | Flujos completos con Playwright, un navegador por actor |
| No funcional | 30 | ISO/IEC 25010: rendimiento, seguridad, fiabilidad, disponibilidad, escalabilidad, mantenibilidad, compatibilidad, usabilidad y portabilidad |

## 3. Plan de pruebas y responsables

### 3.1 Plan aplicado

- Enfoque basado en requisitos y en riesgo, priorizando pruebas unitarias. Cada prueba automatizada lleva `@Tag` con el caso que cubre, así la corrida de Maven se lee caso por caso.
- Técnicas: basada en requisitos, valores límite, partición de equivalencia, transición de estados, tabla de decisión.
- Ciclos: C1 (Sprints 1 y 2), C2 (Sprint 3), C3 (Cierre).
- Criterios de liberación: 100 % Alta ejecutados, ≥ 95 % aprobados, 0 S1/S2, ≤ 5 S3 con plan, cobertura ≥ 70 %.
- Herramientas: JUnit 5, Testcontainers, JaCoCo, Spring Modulith y ArchUnit (en `./mvnw verify` y en CI), Vitest en el frontend, Playwright para punta a punta, k6 y un cliente SSE para carga, Lighthouse, SSL Labs y UptimeRobot.

Cambios al plan durante la campaña (todos en `docs/gestion/CONTROL-DE-CAMBIOS.md`): SC-015 movió el cierre del C1 al 20/11; SC-018 agregó CP-CLI-008-03 al C2; SC-021 sacó HU-006 y HU-011 del Cierre e incluyó HU-015 y HU-022.

### 3.2 Integrantes

| Integrante | Responsabilidad | Qué hizo |
|---|---|---|
| Yanimar Burbano Piamba | Líder de QA · funcionales | Responsable de los casos funcionales de los tres ciclos, la exploratoria del 12/11, los recorridos de punta a punta del 16/11 y del 21/11, los 5 escenarios de Playwright y el recorrido de «Cómo llegar». Revisa y firma los informes |
| Francisco José Castro Muñoz | QA no funcional y seguridad | Responsable de los casos de seguridad y de los 30 no funcionales: análisis de CI, pruebas negativas por API, carga con k6, Lighthouse, TLS, respaldo, despliegue y disponibilidad. Tiene asignados NF-COM-026, NF-USA-027 y NF-POR-029 para el 24/11 |

Las ejecuciones las preparó Pedro (dueño del proyecto) con Claude Code y quedaron en el libro como borradores para la revisión y firma de QA. Los comandos del VPS los corrió Pedro.

## 4. Ejecución de las pruebas y matriz de requerimientos

### 4.1 Sesiones de ejecución

Una fila por sesión de `libro/08-BITACORA.md`.

| Sesión | Ciclo | Versión | Responsable | Resultado |
|---|---|---|---|---|
| 12/11 · Automatizada | C1 | v0.2.0 · `ea7351d` | Yanimar Burbano | Casos 25 · aprob. 25 · fall. 0 · defectos nuevos: 3 (DEF-001, DEF-003, DEF-004) |
| 12/11 · Automatizada | C1 | v0.2.0 · `ea7351d` | Francisco Castro | Casos 13 · aprob. 13 · fall. 0 · defectos nuevos: 1 (DEF-002) |
| 12/11 · Exploratoria | C1 | v0.2.0 · `ea7351d` | Yanimar Burbano | Casos 44 vistas · aprob. 44 · fall. 0 · defectos nuevos: 1 (DEF-005) |
| 16/11 · Automatizada | C1 | main · `5e8ab98` | Yanimar Burbano | Casos 19 · aprob. 19 · fall. 0 |
| 16/11 · Automatizada | C1 | main · `5e8ab98` | Francisco Castro | Casos 4 · aprob. 4 · fall. 0 |
| 16/11 · Recorrido de punta a punta | C1 | main · `5e8ab98` | Yanimar Burbano | Casos 23 · aprob. 23 · fall. 0 · defectos nuevos: 4 (DEF-006 a DEF-009) |
| 16/11 · Re-prueba | C1 | main · `55ab5b6` (#46, #47, #48) | Yanimar Burbano | Casos 5 re-pruebas · aprob. 5 · fall. 0 · defectos nuevos: 1 (DEF-010) |
| 16/11 · Re-prueba | C1 | main · `e679ef3` (#50, #51) | Yanimar Burbano | Casos 1 re-prueba (3 textos) · aprob. 1 · fall. 0 |
| 21/11 · Automatizada | C2 | v0.5.2 · `8d64314` | Yanimar Burbano | Casos 20 · aprob. 20 · fall. 0 |
| 21/11 · Automatizada | C2 | v0.5.2 · `8d64314` | Francisco Castro | Casos 3 · aprob. 3 · fall. 0 |
| 21/11 · Recorrido de punta a punta | C2 | v0.5.2 · `8d64314` (producción) | Yanimar Burbano | Casos 20 · aprob. 20 · fall. 0 · defectos nuevos: 1 (DEF-012) |
| 22/11 · Automatizada | C2 | main `d92847a` + correcciones del PR | Francisco Castro | Casos 5 · aprob. 5 · fall. 0 · defectos nuevos: 3 (DEF-013, DEF-014, DEF-015) |
| 22/11 · Automatizada | C2 | main `d92847a` + correcciones del PR | Yanimar Burbano | Casos 2 · aprob. 2 · fall. 0 |
| 22/11 · Re-prueba en producción | C2 | v0.5.3 · `ff9ac6d` (producción) | Francisco Castro | Casos 3 · aprob. 3 · fall. 0 |
| 22/11 · Automatizada | C3 | v0.5.3 · `ff9ac6d` | Yanimar Burbano | Casos 77 · aprob. 77 · fall. 0 |
| 22/11 · Automatizada | C3 | rama de HU-015 y HU-022 (PR #89) | Yanimar Burbano | Casos 4 · aprob. 4 · fall. 0 |
| 22/11 · Recorrido en producción | C3 | v0.5.4 · `f22f20c` (producción) | Yanimar Burbano | Casos 1 (CP-ORD-026-01) y 2 comprobaciones · aprob. 1 · fall. 0 |
| 22/11 · No funcional | C3 | v0.5.4 · `f22f20c` (producción) | Francisco Castro | Casos 9 · aprob. 7 · fall. 2 · defectos nuevos: 2 (DEF-017, DEF-018) |
| 22/11 · Punta a punta (Playwright) | C3 | pila local desde v0.5.4 · `f22f20c` | Yanimar Burbano | Casos 5 · aprob. 5 · fall. 0 · defectos nuevos: 1 (DEF-016) |

### 4.2 Resultados

Resultado final de cada caso, que es el de su última ejecución (C3 para los 86 casos funcionales y de seguridad; C1, C2 o C3 para los no funcionales). Cobertura de líneas de domain y application del 22/11 (JaCoCo).

| Módulo | Casos | Aprob. | Fall. | Bloq. | Cobertura |
|---|---|---|---|---|---|
| administracion | 4 | 4 | 0 | 0 | 97,9 % |
| catalogo | 2 | 2 | 0 | 0 | 100 % |
| clientes | 5 | 5 | 0 | 0 | 84,4 % |
| identidad | 12 (2 No aplica) | 10 | 0 | 0 | 94,0 % |
| mensajeria | 4 | 4 | 0 | 0 | 100 % |
| notificaciones | 4 | 4 | 0 | 0 | 100 % |
| ordenes | 13 | 13 | 0 | 0 | 96,8 % |
| reputacion | 5 | 5 | 0 | 0 | 97,9 % |
| solicitudes | 21 | 21 | 0 | 0 | 99,7 % |
| tiendas | 16 (2 No aplica) | 14 | 0 | 0 | 90,5 % |
| Punta a punta | 5 | 5 | 0 | 0 | — |
| No funcionales | 30 (4 sin ejecutar) | 24 | 2 | 0 | — |
| **Total** | **121 (4 No aplica, 4 sin ejecutar)** | **111** | **2** | **0** | **95,5 %** (incluye shared, 77,4 %) |

Tasa de aprobación de la campaña: 111 / 113 concluyentes = **98,2 %**. Casos de prioridad Alta: 54 de 54 ejecutados y aprobados. Pruebas automatizadas en la última corrida: 398 unitarias y 72 de integración del backend, 361 del frontend y 5 escenarios de Playwright, todas en verde.

### 4.3 Matriz de requerimientos (trazabilidad)

Fuente: `libro/07-TRAZABILIDAD.md`. El detalle caso por caso está en el Anexo A.

| Requisito | Nombre | Secc. SRS | Casos | Apr. | Fall. | Bloq. | Defectos | Estado |
|---|---|---|---|---|---|---|---|---|
| RF-001 | Registrar cliente | 10.1 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-002 | Registrar tienda | 10.1 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-003 | Iniciar y cerrar sesión | 10.1 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-004 | Segundo factor del administrador | 10.1 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-005 | Aceptar política de datos | 10.1 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-006 | Verificar correo | 10.1 | 2 | 0 | 0 | 0 | 0 | Fuera de alcance (SC-021) |
| RF-007 | Gestionar perfil y direcciones | 10.2 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-008 | Consultar historial y repetir | 10.2 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-009 | Configurar perfil de tienda | 10.3 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-010 | Configurar domicilio con tarifa fija | 10.3 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-011 | Tarifas de domicilio avanzadas | 10.3 | 2 | 0 | 0 | 0 | 0 | Fuera de alcance (SC-021) |
| RF-012 | Configurar medios de pago | 10.3 | 2 | 2 | 0 | 0 | 1 (DEF-007, S3; cerrado el 16/11, la re-prueba sobre 55ab5b6 pasa) | Verificado |
| RF-013 | Abrir o cerrar tienda | 10.3 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-014 | Ver detalle de tienda | 10.3 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-015 | Ver resumen de indicadores | 10.3 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-016 | Publicar solicitud | 10.4 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-017 | Difundir a tiendas elegibles | 10.4 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-018 | Ver solicitudes nuevas | 10.4 | 2 | 2 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-019 | Responder solicitud | 10.4 | 4 | 4 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-020 | Ver y ordenar respuestas | 10.4 | 3 | 3 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-021 | Vencer y cerrar solicitudes | 10.4 | 2 | 2 | 0 | 0 | 1 (S4) DEF-016 (E2E-05, 22/11); DEF-008 y DEF-010 cerrados el 16/11 | Verificado |
| RF-022 | Ampliar radio automáticamente | 10.4 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-023 | Advertir productos restringidos | 10.4 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-024 | Elegir respuesta y crear orden | 10.5 | 4 | 4 | 0 | 0 | 2 (DEF-007 S3, DEF-009 S4; cerrados el 16/11, la re-prueba pasa) | Verificado |
| RF-025 | Reservar para recogida | 10.5 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-026 | Ver cómo llegar | 10.5 | 1 | 1 | 0 | 0 | 0 | Verificado |
| RF-027 | Actualizar estado de la orden | 10.5 | 2 | 2 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-028 | Confirmar recepción | 10.5 | 2 | 2 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-029 | Cancelar orden | 10.5 | 2 | 2 | 0 | 0 | 1 (S4) DEF-012 | Verificado |
| RF-030 | Notificar en tiempo real | 10.6 | 2 | 2 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-031 | Notificaciones push web | 10.6 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-032 | Contactar por WhatsApp | 10.6 | 2 | 2 | 0 | 0 | 0 | Verificado (borrador 16/11) |
| RF-033 | Mini chat | 10.6 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-034 | Calificar tienda | 10.7 | 3 | 3 | 0 | 0 | 0 | Verificado |
| RF-035 | Reportar problema | 10.7 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-036 | Aprobar o rechazar tiendas | 10.8 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-037 | Gestionar reportes y suspender | 10.8 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RF-038 | Gestionar categorías y restringidos | 10.8 | 2 | 2 | 0 | 0 | 0 | Verificado |
| RNF-001 | Rendimiento: Tiempo de respuesta de la API en operaciones de lectura y escritura | 11 | 1 (NF-REN-001) | 1 | 0 | 0 | 0 | Verificado (producción, v0.5.4) |
| RNF-002 | Rendimiento: Tiempo desde que se publica una solicitud hasta que llega al panel de las tiendas elegibles | 11 | 1 (NF-REN-002) | 1 | 0 | 0 | 0 | Verificado (producción, v0.5.4) |
| RNF-003 | Rendimiento: Consulta geoespacial de tiendas elegibles | 11 | 1 (NF-REN-003) | 1 | 0 | 0 | 0 | Verificado |
| RNF-004 | Rendimiento: Consumo de memoria en el servidor | 11 | 1 (NF-REN-004) | 1 | 0 | 0 | 0 | Verificado (Caddy cerca de su límite, ver DEF-018) |
| RNF-005 | Rendimiento: Carga del frontend en el celular | 11 | 1 (NF-REN-005) | 1 | 0 | 0 | 0 | Verificado (producción, v0.5.4) |
| RNF-006 | Seguridad: Almacenamiento y política de contraseñas según NIST SP 800-63B | 11 | 1 (NF-SEG-006) | 1 | 0 | 0 | 0 | Verificado |
| RNF-007 | Seguridad: Autorización por objeto: ninguna tienda accede a datos personales de un cliente que no la eligió | 11 | 1 (NF-SEG-007) | 1 | 0 | 0 | 0 | Verificado |
| RNF-008 | Seguridad: Manejo de sesiones | 11 | 1 (NF-SEG-008) | 1 | 0 | 0 | 0 | Verificado |
| RNF-009 | Seguridad: Límites de uso contra abuso y spam | 11 | 1 (NF-SEG-009) | 1 | 0 | 0 | 0 | Verificado (también en producción sobre v0.5.3) |
| RNF-010 | Seguridad: Cifrado del transporte | 11 | 1 (NF-SEG-010) | 1 | 0 | 0 | 0 | Verificado (SSL Labs A+) |
| RNF-011 | Seguridad: Seguridad de dependencias e imágenes | 11 | 1 (NF-SEG-011) | 1 | 0 | 0 | 1 (S3) | Verificado |
| RNF-012 | Seguridad: Validación de archivos subidos | 11 | 1 (NF-SEG-012) | 1 | 0 | 0 | 0 | Verificado (también en producción sobre v0.5.3) |
| RNF-013 | Seguridad: Auditoría de acciones sensibles | 11 | 1 (NF-SEG-013) | 1 | 0 | 0 | 0 | Verificado (también en producción sobre v0.5.3) |
| RNF-014 | Seguridad: Cumplimiento de la Ley 1581: autorización de tratamiento | 11 | 1 (NF-SEG-014) | 1 | 0 | 0 | 0 | Verificado |
| RNF-015 | Fiabilidad: No se pierden eventos de dominio ante un reinicio | 11 | 1 (NF-FIA-015) | 1 | 0 | 0 | 0 | Verificado |
| RNF-016 | Fiabilidad: Las máquinas de estado rechazan transiciones inválidas | 11 | 1 (NF-FIA-016) | 1 | 0 | 0 | 1 (S4) | Verificado parcialmente |
| RNF-017 | Fiabilidad: Copias de seguridad y restauración | 11 | 1 (NF-FIA-017) | 1 | 0 | 0 | 0 | Verificado (restauración de prueba en 22 s) |
| RNF-018 | Disponibilidad: Disponibilidad del servicio en el periodo de evaluación | 11 | 1 (NF-DIS-018) | 0 | 0 | 0 | 0 | Sin ejecutar (monitor desde el 22/11; se cierra el 26/11) |
| RNF-019 | Disponibilidad: Despliegues sin interrumpir la demo | 11 | 1 (NF-DIS-019) | 1 | 0 | 0 | 0 | Verificado (28,3 s) |
| RNF-020 | Escalabilidad: Capacidad en el servidor actual | 11 | 1 (NF-ESC-020) | 0 | 1 | 0 | 1 (S3) DEF-018 | Con fallas |
| RNF-021 | Escalabilidad: Fronteras de módulos que permitan extraer servicios en el futuro | 11 | 1 (NF-ESC-021) | 1 | 0 | 0 | 0 | Verificado |
| RNF-022 | Mantenibilidad: Cobertura de pruebas automatizadas | 11 | 1 (NF-MAN-022) | 1 | 0 | 0 | 1 (S4) | Verificado |
| RNF-023 | Mantenibilidad: Respeto de la arquitectura limpia | 11 | 1 (NF-MAN-023) | 1 | 0 | 0 | 0 | Verificado |
| RNF-024 | Mantenibilidad: Documentación de la API | 11 | 1 (NF-MAN-024) | 1 | 0 | 0 | 0 | Verificado |
| RNF-025 | Compatibilidad: API REST versionada | 11 | 1 (NF-COM-025) | 1 | 0 | 0 | 0 | Verificado |
| RNF-026 | Compatibilidad: Navegadores y dispositivos soportados | 11 | 1 (NF-COM-026) | 0 | 0 | 0 | 0 | Sin ejecutar (asignado para el 24/11) |
| RNF-027 | Usabilidad: Rapidez para publicar y responder | 11 | 1 (NF-USA-027) | 0 | 0 | 0 | 0 | Sin ejecutar (asignado para el 24/11) |
| RNF-028 | Usabilidad: Accesibilidad para usuarios poco técnicos | 11 | 1 (NF-USA-028) | 0 | 1 | 0 | 1 (S4) DEF-017 | Con fallas |
| RNF-029 | Portabilidad: Despliegue reproducible | 11 | 1 (NF-POR-029) | 0 | 0 | 0 | 0 | Sin ejecutar (asignado para el 24/11) |
| RNF-030 | Portabilidad: Configuración por entorno sin secretos en el código | 11 | 1 (NF-POR-030) | 1 | 0 | 0 | 0 | Verificado |

## 5. Hallazgos y su prioridad

Se registraron 18 defectos: 11 en el C1, 4 en el C2 y 3 en el C3. Ninguno es S1. El único S2 (DEF-011) se encontró en producción, se corrigió en `v0.3.1` y está cerrado. Quedan 4 abiertos: 1 S3 y 3 S4.

| ID | Severidad | Prioridad | Estado | Caso | Qué falla | Versión |
|---|---|---|---|---|---|---|
| DEF-001 | S3 Media | P3 Normal | Cerrado | Exploratoria-20261112 | El contrato OpenAPI de /v3/api-docs no marca required, enum ni nullable en los esquemas | v0.2.0 |
| DEF-002 | S3 Media | P3 Normal | Cerrado | NF-SEG-011 | El pipeline de CI no ejecuta OWASP Dependency-Check, que NF-SEG-011 exige junto con Trivy | v0.2.0 |
| DEF-003 | S4 Baja | P4 Baja | Cerrado | NF-MAN-022 | jacoco:check valida el 70 % sobre todo el proyecto (BUNDLE) y no por módulo como pide el plan | v0.2.0 |
| DEF-004 | S4 Baja | P4 Baja | Cerrado | NF-FIA-016 | La prueba de transiciones inválidas de la tienda no lleva @Tag("NF-FIA-016") | v0.2.0 |
| DEF-005 | S4 Baja | P4 Baja | Cerrado | Exploratoria-20261112 | Publicar solicitud muestra los errores por etapas: con el producto vacío no avisa que falta la categoría ni muestra el resumen | v0.2.0 |
| DEF-006 | S3 Media | P2 Alta | Cerrado | CP-ORD-024-01 (C-04) | /v3/api-docs no declara la respuesta 2xx de 9 operaciones y publica todas como */* | main 5e8ab98 |
| DEF-007 | S3 Media | P2 Alta | Cerrado | CP-ORD-024-04 · CP-TIE-012-01 | «Confirmar pedido» ofrece efectivo, transferencia y datáfono aunque la tienda solo acepta algunos | main 5e8ab98 |
| DEF-008 | S4 Baja | P3 Normal | Cerrado | CP-SOL-021-01 | Con la solicitud vencida, la pantalla del cliente sigue titulada «Esperando respuestas» y dice que las tiendas pueden responder | main 5e8ab98 |
| DEF-009 | S4 Baja | P4 Baja | Cerrado | CP-ORD-024-02 | En el pedido que ve la tienda el pago dice «Pagas con efectivo a la tienda» | main 5e8ab98 |
| DEF-010 | S4 Baja | P4 Baja | Cerrado | CP-SOL-021-01 | Punto doble después de la hora en tres textos («10:17 p. m..») | main 55ab5b6 |
| DEF-011 | S2 Alta | P1 Urgente | Cerrado | Exploratoria-20261117 | Al recargar la pantalla «Esperando respuestas» se pierden la solicitud y las respuestas de las tiendas | v0.3.0 (producción) |
| DEF-012 | S4 Baja | P4 Baja | Abierto | Humo producción 21/11 (CP-ORD-029-01) | En «Mis respuestas» de la tienda, las respuestas cuyo pedido ya se canceló o se recibió siguen diciendo «Elegida · El cliente te eligió. Prepara el pedido.» | v0.5.2 (producción) |
| DEF-013 | S3 Media | P2 Alta | Cerrado | NF-SEG-009 | El inicio de sesión no limita los intentos por cuenta: 6 intentos seguidos de la misma cuenta en un minuto siguen respondiendo 401 o 200 | v0.5.2 (main d92847a) |
| DEF-014 | S3 Media | P2 Alta | Cerrado | NF-SEG-012 | La foto del local se guarda con sus metadatos EXIF (GPS, modelo del celular, fecha) | v0.5.2 (main d92847a) |
| DEF-015 | S3 Media | P3 Normal | Cerrado | NF-SEG-013 | Cuando la tienda ve el nombre y el celular del cliente de una orden no queda ningún registro de auditoría | v0.5.2 (main d92847a) |
| DEF-016 | S4 Baja | P4 Baja | Nuevo | E2E-05 | Al vencer la solicitud, «Esperando respuestas» muestra «Tu solicitud venció» y debajo «Te avisamos en esta pantalla apenas una tienda conteste» durante hasta un minuto | v0.5.4 |
| DEF-017 | S4 Baja | P4 Baja | Nuevo | NF-USA-028 | Los botones + y − del mapa miden 30 × 30 px, por debajo de los 44 × 44 px del RNF-028 | v0.5.4 |
| DEF-018 | S3 Media | P3 Normal | Nuevo | NF-ESC-020 | En la primera corrida de carga con 100 usuarios, 8 de 50 conexiones de tiempo real (SSE) se cerraron antes de terminar | v0.5.4 |

Orden de atención recomendado y justificación:

| # | Defecto | Sev. / Prior. | Por qué esa clasificación | Acción sugerida |
|---|---|---|---|---|
| 1 | DEF-018 | S3 / P3 | Toca un requisito de escalabilidad (RNF-020) y puede volver a pasar con más carga; la causa no está confirmada | Subir el `mem_limit` de Caddy, revisar su log y repetir NF-ESC-020 desde otra red antes del comité |
| 2 | DEF-017 | S4 / P4 | Los botones + y − del mapa de 30 px dificultan tocarlos en el celular (RNF-028); el mapa también se mueve con los dedos | Agrandar los controles de Leaflet a 44 px con CSS; corrección pequeña para una `v0.5.5` |
| 3 | DEF-016 | S4 / P4 | Mensaje contradictorio durante menos de un minuto al vencer una solicitud; el estado final es correcto | Que la lista trate como vencida la solicitud cuyo plazo ya pasó, como el título |
| 4 | DEF-012 | S4 / P4 | La tienda ve «Elegida» en respuestas cuyo pedido ya se canceló o se recibió; no cambia lo que puede hacer | Mostrar el estado del pedido en «Mis respuestas», o aceptarlo en el acta con plan |

Observaciones sin defecto:

- Caddy llegó a 126,5 de sus 128 MiB de límite durante la carga de 100 usuarios (NF-REN-004 aprobado).
- La PWA mantiene la versión anterior hasta que el usuario acepta el aviso de actualización (`registerType: 'prompt'`).
- Las pistas de los campos usan 14 px; el texto base es de 16 px como pide RNF-028.
- La búsqueda completa de tiendas tardó 220 ms con 79 tiendas en el radio (NF-REN-003 mide la consulta geoespacial, que cumple).

## 6. Documentación en el libro de control

| Hoja | Qué se registró | Columnas diligenciadas |
|---|---|---|
| 01 Parámetros y equipo | Proyecto, versiones, equipo y fechas de los ciclos | Todas |
| 02 Casos | 86 casos funcionales y de seguridad, 30 no funcionales y 5 de punta a punta, con su prueba automatizada | Todas; CP-CLI-008-03 agregado por SC-018 |
| 03 Ejecución C1 | 61 casos de los Sprints 1 y 2 | Probador, fecha, resultado, defecto, evidencia, observaciones, duración |
| 04 Ejecución C2 | 34 casos del Sprint 3 | Igual que C1 |
| 05 Ejecución C3 | Regresión de 86 casos, 5 de punta a punta y 13 no funcionales con lo medido | Igual que C1 más «Resultado final» y «Medido» |
| 06 Defectos | DEF-001 a DEF-018 con severidad, prioridad, estado, versión, PR y evidencia | Todas; estados según el flujo de la hoja |
| 07 Trazabilidad | 38 RF y 30 RNF con sus casos, resultados y defectos | Todas |
| 08 Bitácora | Una fila por responsable y día de pruebas | Todas |
| 09 Tablero | Avance, aprobación, cobertura y defectos al cerrar cada sprint y ciclo | Todas |

Convenciones usadas: resultados de la lista del libro; todo Fallido con DEF-nnn; todo Bloqueado con causa; historial conservado; evidencias en `evidencias/<ciclo>/<sesión>/<caso>/`. El Excel `Libro-Control-QA-QuienLoTiene.xlsx` se genera desde estas hojas con `scripts/gestion/libro_qa_excel.py`.

## 7. Casos que no fue posible realizar y sus causas

| Causa | Casos | Detalle | Qué se habría requerido |
|---|---|---|---|
| Cambio de alcance (SC-021) | CP-IDE-006-01, CP-IDE-006-02, CP-TIE-011-01, CP-TIE-011-02 | HU-006 (verificar correo) y HU-011 (tarifas de domicilio avanzadas) salieron del Cierre por decisión del Product Owner del 22/11 | Implementar las dos historias en una liberación posterior |
| Periodo de medición | NF-DIS-018 | No había monitor externo antes del 22/11; UptimeRobot mide desde el 22/11 a las 4:43 p. m. | Un monitor desde el 16/11. Se cierra el 26/11 con el periodo medido |
| Equipos reales | NF-COM-026 | Los E2E corren en Chromium; faltan Edge, Firefox, Safari, Android e iOS | La matriz manual con los equipos del grupo (24/11) |
| Personas | NF-USA-027 | Prueba de tareas con 5 usuarios | 5 participantes y una sesión de 1 hora (24/11) |
| Servidor limpio | NF-POR-029 | Seguir el manual de despliegue en un servidor nuevo | Un VPS o una máquina virtual vacía (24/11) |

## 8. Recomendaciones y conclusión

### Para el producto (desarrollo)

- Corregir DEF-016 y DEF-017 antes del 26/11; son cambios pequeños de la interfaz.
- Subir el límite de memoria de Caddy y confirmar la causa de DEF-018 con una nueva corrida de NF-ESC-020.
- Mostrar el estado del pedido en «Mis respuestas» de la tienda (DEF-012).
- Mejorar la búsqueda completa de tiendas, que carga cada tienda una por una.

### Para la documentación

- QA revisa y firma los borradores del C1 al C3 que quedaron preparados con Claude Code.
- El catálogo de punta a punta pide correrlos en el VPS; si se acepta la pila local, actualizar `02-CASOS.md` §3.

### Para una próxima ejecución de pruebas

- Crear el monitor externo de disponibilidad desde el primer despliegue.
- Correr las pruebas de carga desde una red distinta a la del entorno de pruebas, para separar la red del servidor.
- Agendar desde el inicio del ciclo las pruebas que necesitan personas o equipos (compatibilidad y usabilidad).
- Correr los guiones de `e2e/` en CI contra un entorno de pruebas.

### Lecciones aprendidas

- Etiquetar cada prueba con su caso hizo posible la regresión completa en una sola corrida de Maven.
- Los recorridos en producción encontraron defectos que las pruebas automatizadas no veían (DEF-011, DEF-012).
- Escribir las pruebas de los no funcionales antes del C3 encontró tres faltas de seguridad (DEF-013 a DEF-015) a tiempo para corregirlas.
- Los escenarios con tiempos reales (E2E-05, 32 minutos) encuentran detalles de sincronización que un tiempo acortado esconde (DEF-016).

### Conclusión

Con las cifras del 22/11, la Liberación 1.0 cumple los criterios de calidad del Plan Maestro: 54 de 54 casos Alta aprobados, 98,2 % de aprobación, 0 defectos S1 y S2 abiertos, 1 S3 abierto (límite 5), cobertura de 95,5 % y E2E-01 a E2E-03 aprobados. La recomendación de QA para el comité se completa cuando estén los 4 no funcionales pendientes y el triaje de los 4 defectos abiertos. La decisión la toma el comité en el acta de liberación (`ACTA-DE-LIBERACION.md`).

## Anexo A. Matriz de trazabilidad detallada

| Requisito | Secc. SRS | Caso | Título | Prior. | Resultado final | Defecto |
|---|---|---|---|---|---|---|
| RF-001 | 10.1 | CP-IDE-001-01 | Registro exitoso de cliente | Alta | Aprobado | — |
| RF-001 | 10.1 | CP-IDE-001-02 | Correo ya registrado | Alta | Aprobado | — |
| RF-001 | 10.1 | CP-IDE-001-03 | Frase de contraseña débil | Alta | Aprobado | — |
| RF-002 | 10.1 | CP-TIE-002-01 | Registro de tienda queda pendiente | Alta | Aprobado | — |
| RF-002 | 10.1 | CP-TIE-002-02 | Registro de tienda sin documento | Alta | Aprobado | — |
| RF-003 | 10.1 | CP-IDE-003-01 | Inicio de sesión correcto | Alta | Aprobado | — |
| RF-003 | 10.1 | CP-IDE-003-02 | Credenciales incorrectas | Alta | Aprobado | — |
| RF-003 | 10.1 | CP-IDE-003-03 | Cuenta suspendida | Alta | Aprobado | — |
| RF-004 | 10.1 | CP-IDE-004-01 | Administrador con segundo factor válido | Media | Aprobado | — |
| RF-004 | 10.1 | CP-IDE-004-02 | Segundo factor inválido | Media | Aprobado | — |
| RF-005 | 10.1 | CP-IDE-005-01 | Registro sin autorización de datos | Alta | Aprobado | — |
| RF-005 | 10.1 | CP-IDE-005-02 | Autorización registrada | Alta | Aprobado | — |
| RF-006 | 10.1 | CP-IDE-006-01 | Verificación de correo exitosa | Baja | No aplica | — |
| RF-006 | 10.1 | CP-IDE-006-02 | Enlace vencido | Baja | No aplica | — |
| RF-007 | 10.2 | CP-CLI-007-01 | Guardar una dirección | Alta | Aprobado | — |
| RF-007 | 10.2 | CP-CLI-007-02 | Dirección incompleta | Alta | Aprobado | — |
| RF-008 | 10.2 | CP-CLI-008-01 | Repetir una solicitud | Baja | Aprobado | — |
| RF-008 | 10.2 | CP-CLI-008-02 | Historial vacío | Baja | Aprobado | — |
| RF-008 | 10.2 | CP-CLI-008-03 | Volver a una pregunta abierta | Baja | Aprobado | — |
| RF-009 | 10.3 | CP-TIE-009-01 | Horario fuera de atención | Alta | Aprobado | — |
| RF-009 | 10.3 | CP-TIE-009-02 | Cambios de perfil visibles | Alta | Aprobado | — |
| RF-010 | 10.3 | CP-TIE-010-01 | Tarifa fija en la respuesta | Alta | Aprobado | — |
| RF-010 | 10.3 | CP-TIE-010-02 | Tarifa inválida | Alta | Aprobado | — |
| RF-011 | 10.3 | CP-TIE-011-01 | Domicilio gratis desde un monto | Baja | No aplica | — |
| RF-011 | 10.3 | CP-TIE-011-02 | Tarifa por distancia | Baja | No aplica | — |
| RF-012 | 10.3 | CP-TIE-012-01 | Solo medios activos | Alta | Aprobado | — |
| RF-012 | 10.3 | CP-TIE-012-02 | Desactivar un medio de pago | Alta | Aprobado | — |
| RF-013 | 10.3 | CP-TIE-013-01 | Tienda cerrada | Alta | Aprobado | — |
| RF-013 | 10.3 | CP-TIE-013-02 | Tienda abierta | Alta | Aprobado | — |
| RF-014 | 10.3 | CP-TIE-014-01 | Detalle público de la tienda | Media | Aprobado | — |
| RF-014 | 10.3 | CP-TIE-014-02 | Documentos privados ocultos | Media | Aprobado | — |
| RF-015 | 10.3 | CP-TIE-015-01 | Indicadores correctos | Baja | Aprobado | — |
| RF-015 | 10.3 | CP-TIE-015-02 | Tienda sin actividad | Baja | Aprobado | — |
| RF-016 | 10.4 | CP-SOL-016-01 | Publicar una solicitud | Alta | Aprobado | — |
| RF-016 | 10.4 | CP-SOL-016-02 | Solicitud incompleta | Alta | Aprobado | — |
| RF-016 | 10.4 | CP-SOL-016-03 | Límite de solicitudes | Alta | Aprobado | — |
| RF-017 | 10.4 | CP-SOL-017-01 | Difusión solo a tiendas elegibles | Alta | Aprobado | — |
| RF-017 | 10.4 | CP-SOL-017-02 | Categoría distinta | Alta | Aprobado | — |
| RF-017 | 10.4 | CP-SOL-017-03 | Tienda suspendida | Alta | Aprobado | — |
| RF-018 | 10.4 | CP-SOL-018-01 | Privacidad en la tarjeta de solicitud | Alta | Aprobado | — |
| RF-018 | 10.4 | CP-SOL-018-02 | Distancia aproximada | Alta | Aprobado | — |
| RF-019 | 10.4 | CP-SOL-019-01 | Responder Sí lo tengo | Alta | Aprobado | — |
| RF-019 | 10.4 | CP-SOL-019-02 | Alternativa sin descripción | Alta | Aprobado | — |
| RF-019 | 10.4 | CP-SOL-019-03 | No lo tengo en un toque | Alta | Aprobado | — |
| RF-019 | 10.4 | CP-SOL-019-04 | Respuesta duplicada | Alta | Aprobado | — |
| RF-020 | 10.4 | CP-SOL-020-01 | Respuestas en tiempo real | Alta | Aprobado | — |
| RF-020 | 10.4 | CP-SOL-020-02 | Ordenar por precio | Alta | Aprobado | — |
| RF-020 | 10.4 | CP-SOL-020-03 | Alternativa marcada | Alta | Aprobado | — |
| RF-021 | 10.4 | CP-SOL-021-01 | Vencimiento de la solicitud | Alta | Aprobado | — |
| RF-021 | 10.4 | CP-SOL-021-02 | Aviso a tiendas no elegidas | Alta | Aprobado | — |
| RF-022 | 10.4 | CP-SOL-022-01 | Ampliación del radio | Baja | Aprobado | — |
| RF-022 | 10.4 | CP-SOL-022-02 | Sin ampliación si hay respuestas | Baja | Aprobado | — |
| RF-023 | 10.4 | CP-SOL-023-01 | Producto restringido | Media | Aprobado | — |
| RF-023 | 10.4 | CP-SOL-023-02 | Restringido sin domicilio | Media | Aprobado | — |
| RF-024 | 10.5 | CP-ORD-024-01 | Crear orden con total correcto | Alta | Aprobado | — |
| RF-024 | 10.5 | CP-ORD-024-02 | La tienda elegida ve los datos del cliente | Alta | Aprobado | — |
| RF-024 | 10.5 | CP-ORD-024-03 | Una tienda no elegida no ve los datos | Alta | Aprobado | — |
| RF-024 | 10.5 | CP-ORD-024-04 | Medio de pago no aceptado | Alta | Aprobado | — |
| RF-025 | 10.5 | CP-ORD-025-01 | Reserva vencida | Media | Aprobado | — |
| RF-025 | 10.5 | CP-ORD-025-02 | Recogida a tiempo | Media | Aprobado | — |
| RF-026 | 10.5 | CP-ORD-026-01 | Cómo llegar | Media | Aprobado | — |
| RF-027 | 10.5 | CP-ORD-027-01 | Transición válida | Alta | Aprobado | — |
| RF-027 | 10.5 | CP-ORD-027-02 | Transición inválida | Alta | Aprobado | — |
| RF-028 | 10.5 | CP-ORD-028-01 | Confirmar recepción | Alta | Aprobado | — |
| RF-028 | 10.5 | CP-ORD-028-02 | Confirmar una orden ajena | Alta | Aprobado | — |
| RF-029 | 10.5 | CP-ORD-029-01 | Cancelar antes del despacho | Media | Aprobado | — |
| RF-029 | 10.5 | CP-ORD-029-02 | Cancelar después del despacho | Media | Aprobado | — |
| RF-030 | 10.6 | CP-NOT-030-01 | Evento en tiempo real | Alta | Aprobado | — |
| RF-030 | 10.6 | CP-NOT-030-02 | Solicitud urgente | Alta | Aprobado | — |
| RF-031 | 10.6 | CP-NOT-031-01 | Push con la app cerrada | Media | Aprobado | — |
| RF-031 | 10.6 | CP-NOT-031-02 | Sin autorización de notificaciones | Media | Aprobado | — |
| RF-032 | 10.6 | CP-MEN-032-01 | Botón de WhatsApp con orden activa | Alta | Aprobado | — |
| RF-032 | 10.6 | CP-MEN-032-02 | Sin botón antes de elegir | Alta | Aprobado | — |
| RF-033 | 10.6 | CP-MEN-033-01 | Enviar un mensaje | Media | Aprobado | — |
| RF-033 | 10.6 | CP-MEN-033-02 | Chat cerrado | Media | Aprobado | — |
| RF-034 | 10.7 | CP-REP-034-01 | Calificar una orden resuelta | Alta | Aprobado | — |
| RF-034 | 10.7 | CP-REP-034-02 | Calificar sin orden resuelta | Alta | Aprobado | — |
| RF-034 | 10.7 | CP-REP-034-03 | Calificar dos veces | Alta | Aprobado | — |
| RF-035 | 10.7 | CP-REP-035-01 | Crear un reporte | Media | Aprobado | — |
| RF-035 | 10.7 | CP-REP-035-02 | Reportar una orden ajena | Media | Aprobado | — |
| RF-036 | 10.8 | CP-ADM-036-01 | Aprobar una tienda | Alta | Aprobado | — |
| RF-036 | 10.8 | CP-ADM-036-02 | Rechazar una tienda | Alta | Aprobado | — |
| RF-037 | 10.8 | CP-ADM-037-01 | Suspender una cuenta | Media | Aprobado | — |
| RF-037 | 10.8 | CP-ADM-037-02 | Resolver un reporte | Media | Aprobado | — |
| RF-038 | 10.8 | CP-CAT-038-01 | Desactivar una categoría | Media | Aprobado | — |
| RF-038 | 10.8 | CP-CAT-038-02 | Agregar un producto restringido | Media | Aprobado | — |

## Anexo B. Casos no realizados con su causa

| Caso | Título | Causa |
|---|---|---|
| CP-IDE-006-01 | Verificación de correo exitosa | No aplica: HU-006 sale del Sprint de Cierre por SC-021 (aprobada por el Product Owner el 22/11) |
| CP-IDE-006-02 | Enlace vencido | No aplica: HU-006 sale del Sprint de Cierre por SC-021 (aprobada por el Product Owner el 22/11) |
| CP-TIE-011-01 | Domicilio gratis desde un monto | No aplica: HU-011 sale del Sprint de Cierre por SC-021 (aprobada por el Product Owner el 22/11) |
| CP-TIE-011-02 | Tarifa por distancia | No aplica: HU-011 sale del Sprint de Cierre por SC-021 (aprobada por el Product Owner el 22/11) |
| NF-DIS-018 | Disponibilidad del servicio en el periodo de evaluación | No ejecutado: No había monitor externo antes del 22/11, así que el periodo del 16/11 al 22/11 no tiene medición. Se cierra el 26/11 con la captura del porcentaje |
| NF-COM-026 | Navegadores y dispositivos soportados | No ejecutado: Pendiente: matriz manual de navegadores (Chrome, Edge, Firefox, Safari, Android, iOS) asignada a Francisco para el 24/11. Los E2E corren en Chromium |
| NF-USA-027 | Rapidez para publicar y responder | No ejecutado: Pendiente: prueba con 5 usuarios asignada a Francisco para el 24/11 |
| NF-POR-029 | Despliegue reproducible | No ejecutado: Pendiente: requiere un servidor limpio para seguir el manual de despliegue; asignada a Francisco para el 24/11 |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Borrador con las cifras del 22/11: C1, C2 y C3 sobre `v0.5.4`, DEF-001 a DEF-018 y 4 no funcionales pendientes. Pendiente: revisión y firma de QA |
