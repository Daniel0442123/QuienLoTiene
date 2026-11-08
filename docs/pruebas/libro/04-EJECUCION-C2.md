# Hoja «Ejecución C2» · Sprint 3 (16/11 al 22/11)

> Libro de control de QA · Quién lo tiene · Byte Force. Fuente en Markdown del Excel «Libro-Control-QA-QuienLoTiene.xlsx». Solo se diligencian las columnas vacías; los catálogos los mantiene QA.

Alcance: (1) casos nuevos de las historias del Sprint 3 (22); (2) no funcionales de integración y seguridad (9); (3) re-prueba obligatoria de todo caso Fallido o Bloqueado en C1 (se copia la fila desde «Ejecución C1»).

## 1. Casos nuevos del Sprint 3

| ID del caso | Tipo | Módulo | HU | Título | Prioridad | Nivel | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CP-TIE-014-01 | Funcional | tiendas | HU-014 | Detalle público de la tienda | Media | Unitaria |  |  |  |  |  |  |  |
| CP-TIE-014-02 | Seguridad | tiendas | HU-014 | Documentos privados ocultos | Media | Integración (API) |  |  |  |  |  |  |  |
| CP-SOL-023-01 | Funcional | solicitudes | HU-023 | Producto restringido | Media | Unitaria |  |  |  |  |  |  |  |
| CP-SOL-023-02 | Funcional | solicitudes | HU-023 | Restringido sin domicilio | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ORD-025-01 | Funcional | ordenes | HU-025 | Reserva vencida | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ORD-025-02 | Funcional | ordenes | HU-025 | Recogida a tiempo | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ORD-026-01 | Funcional | ordenes | HU-026 | Cómo llegar | Media | Manual (interfaz) |  |  |  |  |  |  |  |
| CP-ORD-029-01 | Funcional | ordenes | HU-029 | Cancelar antes del despacho | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ORD-029-02 | Funcional | ordenes | HU-029 | Cancelar después del despacho | Media | Unitaria |  |  |  |  |  |  |  |
| CP-NOT-031-01 | Funcional | notificaciones | HU-031 | Push con la app cerrada | Media | Manual (navegador) |  |  |  |  |  |  |  |
| CP-NOT-031-02 | Funcional | notificaciones | HU-031 | Sin autorización de notificaciones | Media | Manual (navegador) |  |  |  |  |  |  |  |
| CP-MEN-033-01 | Funcional | mensajeria | HU-033 | Enviar un mensaje | Media | Unitaria |  |  |  |  |  |  |  |
| CP-MEN-033-02 | Funcional | mensajeria | HU-033 | Chat cerrado | Media | Unitaria |  |  |  |  |  |  |  |
| CP-REP-034-01 | Funcional | reputacion | HU-034 | Calificar una orden resuelta | Alta | Unitaria |  |  |  |  |  |  |  |
| CP-REP-034-02 | Funcional | reputacion | HU-034 | Calificar sin orden resuelta | Alta | Unitaria |  |  |  |  |  |  |  |
| CP-REP-034-03 | Funcional | reputacion | HU-034 | Calificar dos veces | Alta | Unitaria |  |  |  |  |  |  |  |
| CP-REP-035-01 | Funcional | reputacion | HU-035 | Crear un reporte | Media | Unitaria |  |  |  |  |  |  |  |
| CP-REP-035-02 | Seguridad | reputacion | HU-035 | Reportar una orden ajena | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ADM-037-01 | Seguridad | administracion | HU-037 | Suspender una cuenta | Media | Unitaria |  |  |  |  |  |  |  |
| CP-ADM-037-02 | Funcional | administracion | HU-037 | Resolver un reporte | Media | Unitaria |  |  |  |  |  |  |  |
| CP-CAT-038-01 | Funcional | catalogo | HU-038 | Desactivar una categoría | Media | Unitaria |  |  |  |  |  |  |  |
| CP-CAT-038-02 | Funcional | catalogo | HU-038 | Agregar un producto restringido | Media | Unitaria |  |  |  |  |  |  |  |

## 2. No funcionales

| ID del caso | Requisito | Criterio | Herramienta | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) |
|---|---|---|---|---|---|---|---|---|---|---|
| NF-REN-003 | RNF-003 | Menor o igual a 200 ms usando índice GiST. | Testcontainers (PostGIS) + EXPLAIN |  |  |  |  |  |  |  |
| NF-SEG-007 | RNF-007 | 0 accesos indebidos; cada endpoint con datos personales tiene prueba negativa. | JUnit + pruebas de autorización API |  |  |  |  |  |  |  |
| NF-SEG-008 | RNF-008 | JWT de 15 minutos; refresco rotativo en cookie HttpOnly, Secure y SameSite=Strict. | Pruebas de API (Spring Security Test) |  |  |  |  |  |  |  |
| NF-SEG-009 | RNF-009 | Máximo 5 intentos de inicio de sesión por minuto por cuenta y 10 solicitudes por hora por cliente; el exceso recibe HTTP 429. | Pruebas de API / k6 |  |  |  |  |  |  |  |
| NF-SEG-012 | RNF-012 | Solo JPEG, PNG, WebP y PDF de hasta 5 MB, tipo verificado por contenido y metadatos EXIF eliminados. | JUnit + pruebas de API |  |  |  |  |  |  |  |
| NF-SEG-013 | RNF-013 | 100 % registrados con usuario, fecha y acción, sin datos personales en los logs. | Pruebas de integración |  |  |  |  |  |  |  |
| NF-FIA-015 | RNF-015 | 0 eventos perdidos en la prueba de reinicio. | Spring Modulith (event publication registry) |  |  |  |  |  |  |  |
| NF-MAN-024 | RNF-024 | 100 % de endpoints documentados. | springdoc OpenAPI |  |  |  |  |  |  |  |
| NF-COM-025 | RNF-025 | JSON sobre HTTPS con prefijo /api/v1. | Pruebas de API |  |  |  |  |  |  |  |

## 3. Re-prueba de C1 (Fallidos y Bloqueados)

| ID del caso | Resultado C1 | Defecto C1 | Probador | Fecha | Resultado | Defecto(s) | Evidencia (enlace) | Observaciones | Duración (min) | Resultado final |
|---|---|---|---|---|---|---|---|---|---|---|
|  |  |  |  |  |  |  |  |  |  |  |
