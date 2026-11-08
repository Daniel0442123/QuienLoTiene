# Hoja «Ejecución C1» · Sprints 1 y 2 (02/11 al 15/11)

> Libro de control de QA · Quién lo tiene · Byte Force. Fuente en Markdown del Excel «Libro-Control-QA-QuienLoTiene.xlsx». Solo se diligencian las columnas vacías; los catálogos los mantiene QA.

Alcance: casos de las historias de los Sprints 1 y 2 (53) y los no funcionales que corren en CI desde el inicio (8). Corte parcial el 08/11 (fin del Sprint 1) y cierre del ciclo el 15/11. Resultado: Aprobado · Fallido · Bloqueado · No ejecutado · No aplica. Todo Fallido lleva su DEF-nnn y todo Bloqueado su causa en Observaciones.

## Funcionales y de seguridad

| ID del caso | Tipo | Módulo | HU | Título | Prioridad | Nivel | Sprint | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CP-IDE-001-01 | Funcional | identidad | HU-001 | Registro exitoso de cliente | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-001-02 | Funcional | identidad | HU-001 | Correo ya registrado | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-001-03 | Seguridad | identidad | HU-001 | Frase de contraseña débil | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-002-01 | Funcional | tiendas | HU-002 | Registro de tienda queda pendiente | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-002-02 | Funcional | tiendas | HU-002 | Registro de tienda sin documento | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-003-01 | Seguridad | identidad | HU-003 | Inicio de sesión correcto | Alta | Integración (API + Spring Security) | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-003-02 | Seguridad | identidad | HU-003 | Credenciales incorrectas | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-003-03 | Seguridad | identidad | HU-003 | Cuenta suspendida | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-004-01 | Seguridad | identidad | HU-004 | Administrador con segundo factor válido | Media | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-004-02 | Seguridad | identidad | HU-004 | Segundo factor inválido | Media | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-005-01 | Seguridad | identidad | HU-005 | Registro sin autorización de datos | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-IDE-005-02 | Seguridad | identidad | HU-005 | Autorización registrada | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-CLI-007-01 | Funcional | clientes | HU-007 | Guardar una dirección | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-CLI-007-02 | Funcional | clientes | HU-007 | Dirección incompleta | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-009-01 | Funcional | tiendas | HU-009 | Horario fuera de atención | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-009-02 | Funcional | tiendas | HU-009 | Cambios de perfil visibles | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-010-01 | Funcional | tiendas | HU-010 | Tarifa fija en la respuesta | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-010-02 | Funcional | tiendas | HU-010 | Tarifa inválida | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-012-01 | Funcional | tiendas | HU-012 | Solo medios activos | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-012-02 | Funcional | tiendas | HU-012 | Desactivar un medio de pago | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-013-01 | Funcional | tiendas | HU-013 | Tienda cerrada | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-TIE-013-02 | Funcional | tiendas | HU-013 | Tienda abierta | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-016-01 | Funcional | solicitudes | HU-016 | Publicar una solicitud | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-016-02 | Funcional | solicitudes | HU-016 | Solicitud incompleta | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-016-03 | Seguridad | solicitudes | HU-016 | Límite de solicitudes | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-017-01 | Funcional | solicitudes | HU-017 | Difusión solo a tiendas elegibles | Alta | Integración (PostGIS) | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-017-02 | Funcional | solicitudes | HU-017 | Categoría distinta | Alta | Integración (PostGIS) | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-017-03 | Funcional | solicitudes | HU-017 | Tienda suspendida | Alta | Integración (PostGIS) | Sprint 1 |  |  |  |  |  |  |  |
| CP-SOL-018-01 | Seguridad | solicitudes | HU-018 | Privacidad en la tarjeta de solicitud | Alta | Integración (API) | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-018-02 | Funcional | solicitudes | HU-018 | Distancia aproximada | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-019-01 | Funcional | solicitudes | HU-019 | Responder Sí lo tengo | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-019-02 | Funcional | solicitudes | HU-019 | Alternativa sin descripción | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-019-03 | Funcional | solicitudes | HU-019 | No lo tengo en un toque | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-019-04 | Funcional | solicitudes | HU-019 | Respuesta duplicada | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-020-01 | Funcional | solicitudes | HU-020 | Respuestas en tiempo real | Alta | Integración (SSE) | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-020-02 | Funcional | solicitudes | HU-020 | Ordenar por precio | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-020-03 | Funcional | solicitudes | HU-020 | Alternativa marcada | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-021-01 | Funcional | solicitudes | HU-021 | Vencimiento de la solicitud | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-SOL-021-02 | Funcional | solicitudes | HU-021 | Aviso a tiendas no elegidas | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-024-01 | Funcional | ordenes | HU-024 | Crear orden con total correcto | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-024-02 | Seguridad | ordenes | HU-024 | La tienda elegida ve los datos del cliente | Alta | Integración (API) | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-024-03 | Seguridad | ordenes | HU-024 | Una tienda no elegida no ve los datos | Alta | Integración (API) | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-024-04 | Funcional | ordenes | HU-024 | Medio de pago no aceptado | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-027-01 | Funcional | ordenes | HU-027 | Transición válida | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-027-02 | Funcional | ordenes | HU-027 | Transición inválida | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-028-01 | Funcional | ordenes | HU-028 | Confirmar recepción | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-ORD-028-02 | Seguridad | ordenes | HU-028 | Confirmar una orden ajena | Alta | Unitaria | Sprint 2 |  |  |  |  |  |  |  |
| CP-NOT-030-01 | Funcional | notificaciones | HU-030 | Evento en tiempo real | Alta | Integración (SSE) | Sprint 2 |  |  |  |  |  |  |  |
| CP-NOT-030-02 | Funcional | notificaciones | HU-030 | Solicitud urgente | Alta | Integración (SSE) | Sprint 2 |  |  |  |  |  |  |  |
| CP-MEN-032-01 | Funcional | mensajeria | HU-032 | Botón de WhatsApp con orden activa | Alta | Manual (interfaz) | Sprint 2 |  |  |  |  |  |  |  |
| CP-MEN-032-02 | Funcional | mensajeria | HU-032 | Sin botón antes de elegir | Alta | Manual (interfaz) | Sprint 2 |  |  |  |  |  |  |  |
| CP-ADM-036-01 | Funcional | administracion | HU-036 | Aprobar una tienda | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |
| CP-ADM-036-02 | Funcional | administracion | HU-036 | Rechazar una tienda | Alta | Unitaria | Sprint 1 |  |  |  |  |  |  |  |

## No funcionales

| ID del caso | Requisito | Criterio | Herramienta | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|---|---|
| NF-SEG-006 | RNF-006 | Mínimo 15 caracteres, hash Argon2id o bcrypt, rechazo de contraseñas de la lista de filtradas. | JUnit |  |  |  |  |  |  |  |
| NF-SEG-011 | RNF-011 | 0 críticas y 0 altas sin justificar en el build de entrega. | Trivy + OWASP Dependency-Check (CI) |  |  |  |  |  |  |  |
| NF-SEG-014 | RNF-014 | 100 % con fecha y versión de la política aceptada. | JUnit |  |  |  |  |  |  |  |
| NF-FIA-016 | RNF-016 | 0 aceptadas; todas las combinaciones probadas. | JUnit (máquinas de estado) |  |  |  |  |  |  |  |
| NF-ESC-021 | RNF-021 | 0 violaciones de dependencias entre módulos. | Spring Modulith verify() |  |  |  |  |  |  |  |
| NF-MAN-022 | RNF-022 | Mayor o igual a 70 %. | JaCoCo |  |  |  |  |  |  |  |
| NF-MAN-023 | RNF-023 | 0 violaciones en el pipeline. | ArchUnit |  |  |  |  |  |  |  |
| NF-POR-030 | RNF-030 | 0 secretos en el repositorio; toda la configuración por variables de entorno. | gitleaks / escaneo de secretos (CI) |  |  |  |  |  |  |  |
