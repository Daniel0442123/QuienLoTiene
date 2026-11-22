# Informe de corte · Sprint 3 · Ciclo C2

> **Control del documento** · Versión 1.3 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión y firma de QA) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Anexo C del Plan Maestro (informe de avance). Preparado con Claude Code a partir de `libro/04-EJECUCION-C2.md`; QA lo revisa y lo firma.

| Campo | Valor |
|---|---|
| Fecha / ciclo / sprint / versión | 2026-11-22 / C2 / Sprint 3 / v0.5.2 (`8d64314`) |
| Elaboró | Yanimar Burbano Piamba |

## 1. Avance (del tablero)

| Responsable | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | No ejecutados |
|---|---|---|---|---|---|---|
| Yanimar Burbano Piamba | 26 | 26 | 26 | 0 | 0 | 0 |
| Francisco José Castro Muñoz | 8 | 8 | 8 | 0 | 0 | 0 |
| **Total** | **34** | **34** | **34** | **0** | **0** | **0** |

Los 34 casos son los 22 del Sprint 3, los 3 de HU-008 que pasaron a este sprint por SC-018 y los 9 no funcionales del C2. No hay re-pruebas del C1: ningún caso quedó Fallido ni Bloqueado.

Los 7 no funcionales que el 21/11 no tenían una prueba etiquetada (NF-REN-003, NF-SEG-007, 008, 009, 012, 013 y NF-FIA-015) se ejecutaron el 22/11 con pruebas nuevas o etiquetadas, sobre main `d92847a` más las correcciones de DEF-013, DEF-014 y DEF-015. Los tres de seguridad fallaban en v0.5.2, pasan con la corrección y el 22/11 se re-probaron en producción sobre `v0.5.3` ([evidencia](../../evidencias/C2/2026-11-22/produccion-v0.5.3/)). En producción, el sexto inicio de sesión en un minuto recibe 429. La foto guardada ya no trae EXIF. El log «auditoria» registra cuándo la tienda ve los datos del cliente o pide su contacto, solo con ids.

Avance del ciclo: 34 / 34 = 100 %. Aprobación sobre lo ejecutado: 100 %. Los 25 casos de las historias del sprint están aprobados, incluido el push, que se probó en un celular Android real.

## 2. Pruebas automatizadas

| Métrica | Valor |
|---|---|
| Comando | `./mvnw -B clean verify` sobre `v0.5.2` (Java 21, Docker con Testcontainers PostGIS) · [`mvn-verify.txt`](../../evidencias/C2/2026-11-22/mvn-verify.txt) |
| Tests run / Failures / Errors / Skipped | Unitarias 374 / 0 / 0 / 0 · Integración 60 / 0 / 0 / 0 · **Total 434 / 0 / 0 / 0** |
| Cobertura de líneas domain + application (total) | 95,2 % (2570 de 2700 líneas, incluye shared) · identidad 93,6 %, clientes 84,4 %, tiendas 89,2 %, catalogo 100 %, solicitudes 99,7 %, ordenes 96,7 %, mensajeria 100 %, reputacion 97,9 %, notificaciones 100 %, administracion 97,9 %, shared 77,4 % · [`jacoco.csv`](../../evidencias/C2/2026-11-22/jacoco.csv) |
| No funcionales (22/11) | `./mvnw -B verify` sobre la rama de las correcciones: unitarias 386 / 0 / 0 / 0 · integración 67 / 0 / 0 / 0 · **total 453 / 0 / 0 / 0** · cobertura 95,3 % (2686 de 2817; identidad 94,0 %, tiendas 90,5 %, ordenes 96,8 %) · [`mvn-verify-no-funcionales.txt`](../../evidencias/C2/2026-11-22/mvn-verify-no-funcionales.txt), [`jacoco-no-funcionales.csv`](../../evidencias/C2/2026-11-22/jacoco-no-funcionales.csv) |
| Módulos por debajo del 70 % | Ninguno |
| Pipeline de GitHub Actions (enlace) | [CI run #253 sobre `8d64314`](https://github.com/Daniel0442123/QuienLoTiene/actions/runs/44976316882): en verde |

## 3. Historias del sprint

| Historia | Casos | Pruebas automatizadas | Resultado | ¿Terminada según la definición de terminado? |
|---|---|---|---|---|
| HU-008 (SC-018) | CP-CLI-008-01, CP-CLI-008-02, CP-CLI-008-03 | 3 en verde | 3 de 3 aprobados; Repetir y Mis preguntas también en producción | Sí |
| HU-014 | CP-TIE-014-01, CP-TIE-014-02 | 3 en verde | 2 de 2 aprobados; detalle sin fotos, como dejan los tableros 11 a 13 | Sí |
| HU-023 | CP-SOL-023-01, CP-SOL-023-02 | 4 en verde | 2 de 2 aprobados, también en producción | Sí |
| HU-025 | CP-ORD-025-01, CP-ORD-025-02 | 3 en verde | 2 de 2 aprobados; el vencimiento solo en pruebas automatizadas | Sí |
| HU-026 | CP-ORD-026-01 | Manual | 1 de 1 aprobado en producción | Sí |
| HU-029 | CP-ORD-029-01, CP-ORD-029-02 | 4 en verde | 2 de 2 aprobados, también en producción | Sí |
| HU-031 | CP-NOT-031-01, CP-NOT-031-02 | 2 en verde | 2 de 2 aprobados en un Android real: llega el aviso con la app cerrada y lo abre; bloqueado no llega y el panel lo explica | Sí |
| HU-033 | CP-MEN-033-01, CP-MEN-033-02 | 3 en verde | 2 de 2 aprobados, también en producción | Sí |
| HU-034 | CP-REP-034-01, CP-REP-034-02, CP-REP-034-03 | 4 en verde | 3 de 3 aprobados | Sí |
| HU-035 | CP-REP-035-01, CP-REP-035-02 | 3 en verde | 2 de 2 aprobados | Sí |
| HU-037 | CP-ADM-037-01, CP-ADM-037-02 | 7 en verde | 2 de 2 aprobados; la suspensión solo en pruebas automatizadas | Sí |
| HU-038 | CP-CAT-038-01, CP-CAT-038-02 | 4 en verde | 2 de 2 aprobados | Sí |
| SC-020 (menú por rol) | Humo | — | Cada rol ve su menú en producción | Sí |

### No funcionales del C2

| Caso | Prueba | Resultado |
|---|---|---|
| NF-REN-003 | RendimientoBusquedaTiendasIT | Aprobado: 2 ms con 1.000 tiendas, usa el índice GiST |
| NF-SEG-007 | AutorizacionPorObjetoIT, DireccionesIT | Aprobado: 403 o 404 en 14 endpoints de órdenes y solicitudes, sin datos del cliente |
| NF-SEG-008 | SesionControllerIT | Aprobado: token de 15 minutos y cookie HttpOnly, Secure, SameSite=Strict que rota |
| NF-SEG-009 | IntentosInicioSesionTest, SesionesTest, SesionControllerIT, SolicitudControllerIT | Aprobado con la corrección de DEF-013 |
| NF-SEG-012 | LimpiadorMetadatosTest, TiendaTest, AdministracionTiendasIT | Aprobado con la corrección de DEF-014 |
| NF-SEG-013 | SesionControllerIT, AutorizacionPorObjetoIT y las pruebas de administración | Aprobado con la corrección de DEF-015 |
| NF-FIA-015 | RegistroDeEventosIT | Aprobado: el evento que falla queda pendiente y se procesa al reenviarlo, 0 perdidos |

### Recorrido en producción

El 21/11, de 9:40 p. m. a 10:48 p. m., Pedro y Claude Code recorrieron la app en https://quienlotiene.tech con usuario1, tienda1 y la cuenta de administrador. Cubrieron 3 pedidos y el push en un celular Android:

- **ECF43503 (recoger):** chat, reserva, recibido y calificación.
- **5E39F3E9 (domicilio):** cancelado por el cliente.
- **F5305EE7:** «En camino» sin poder cancelar; se reportó y el administrador resolvió el reporte.

Detalle y 49 capturas en [`evidencias/C2/2026-11-22/produccion/`](../../evidencias/C2/2026-11-22/produccion/README.md).

## 4. Defectos nuevos (S1 0 / S2 0 / S3 3 / S4 1)

| ID | Severidad | Prioridad | Estado | Caso | Qué falla |
|---|---|---|---|---|---|
| DEF-012 | S4 Baja | P4 Baja | Abierto | Humo producción (CP-ORD-029-01) | En «Mis respuestas» de la tienda, los pedidos ya cancelados o recibidos siguen diciendo «Elegida · Prepara el pedido.» |
| DEF-013 | S3 Media | P2 Alta | Cerrado (corregido en v0.5.3, 22/11) | NF-SEG-009 | El inicio de sesión no limita los intentos por cuenta (RNF-009 pide 5 por minuto) |
| DEF-014 | S3 Media | P2 Alta | Cerrado (corregido en v0.5.3, 22/11) | NF-SEG-012 | La foto del local se guarda con su EXIF (GPS y modelo del celular) |
| DEF-015 | S3 Media | P3 Normal | Cerrado (corregido en v0.5.3, 22/11) | NF-SEG-013 | Cuando la tienda ve los datos del cliente de una orden no queda registro de auditoría |

Observaciones que no se registran como defecto:

- Al enviar un reporte no aparece un mensaje de confirmación.
- La cuenta regresiva de la reserva muestra 31 minutos en la tienda y 30 en el cliente.
- En producción el formulario reduce la foto en el navegador y eso ya le quita el EXIF, así que la re-prueba de NF-SEG-012 no llega al limpiador del servidor. Ese limpiador queda cubierto por las pruebas automáticas.
- La búsqueda completa de tiendas tardó 220 ms con 79 tiendas en el radio porque carga cada tienda una por una. RNF-003 mide la consulta (2 ms) y pasa, pero conviene revisarlo en la prueba de carga del C3.

## 5. Bloqueos y acciones

| Bloqueo | Casos afectados | Acción acordada | Responsable | Fecha |
|---|---|---|---|---|
| Para probar un sábado de noche se amplió el horario de Tienda Uno | Recorrido en producción | Hecho: se devolvió a lunes a sábado de 07:00 a 18:00 y domingo cerrado el 21/11 a las 10:52 p. m. | Pedro | 2026-11-21 |

## 6. Plan para el siguiente sprint

1. Triaje de DEF-012 y corrección en el C3.
2. DEF-013, DEF-014 y DEF-015 quedaron cerrados el 22/11 con el visto bueno de Pedro, después de pasar la re-prueba en producción sobre `v0.5.3`. QA lo revisa al firmar este informe.
3. Correr la regresión completa del C3 (85 casos + 5 E2E + 13 NF), con los 7 no funcionales ya etiquetados.
4. Cerrar el C2 con la firma de QA de este informe y de `04-EJECUCION-C2.md`.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Corte del Sprint 3 sobre `v0.5.2`, preparado con Claude Code para que QA lo revise y firme |
| 1.1 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Los 7 no funcionales que faltaban, ejecutados y aprobados el 22/11; DEF-013 a DEF-015 en corrección; observación sobre la búsqueda completa |
| 1.2 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Re-prueba en producción de NF-SEG-009, 012 y 013 sobre `v0.5.3`: pasan; DEF-013 a DEF-015 en verificación |
| 1.3 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | DEF-013 a DEF-015 cerrados el 22/11 con el visto bueno de Pedro |
