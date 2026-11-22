# Informe de corte · Sprint 3 · Ciclo C2

> **Control del documento** · Versión 1.0 · 22/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador (pendiente de revisión y firma de QA) · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

> Anexo C del Plan Maestro (informe de avance). Preparado con Claude Code a partir de `libro/04-EJECUCION-C2.md`; QA lo revisa y lo firma.

| Campo | Valor |
|---|---|
| Fecha / ciclo / sprint / versión | 2026-11-22 / C2 / Sprint 3 / v0.5.2 (`8d64314`) |
| Elaboró | Yanimar Burbano Piamba |

## 1. Avance (del tablero)

| Responsable | Casos planeados | Ejecutados | Aprobados | Fallidos | Bloqueados | No ejecutados |
|---|---|---|---|---|---|---|
| Yanimar Burbano Piamba | 26 | 24 | 24 | 0 | 0 | 2 |
| Francisco José Castro Muñoz | 8 | 3 | 3 | 0 | 0 | 5 |
| **Total** | **34** | **27** | **27** | **0** | **0** | **7** |

Los 34 casos son los 22 del Sprint 3, los 3 de HU-008 que pasaron a este sprint por SC-018 y los 9 no funcionales del C2. No hay re-pruebas del C1: ningún caso quedó Fallido ni Bloqueado.

Los 7 «No ejecutado» son no funcionales:

- **NF-REN-003, NF-SEG-007, NF-SEG-008, NF-SEG-009, NF-SEG-012 y NF-FIA-015:** todavía no tienen una prueba etiquetada con su ID.
- **NF-SEG-013:** solo tiene cobertura parcial (la auditoría de la revisión de tiendas).

Avance del ciclo: 27 / 34 = 79,4 %. Aprobación sobre lo ejecutado: 100 %. Los 25 casos de las historias del sprint están aprobados, incluido el push, que se probó en un celular Android real.

## 2. Pruebas automatizadas

| Métrica | Valor |
|---|---|
| Comando | `./mvnw -B clean verify` sobre `v0.5.2` (Java 21, Docker con Testcontainers PostGIS) · [`mvn-verify.txt`](../../evidencias/C2/2026-11-22/mvn-verify.txt) |
| Tests run / Failures / Errors / Skipped | Unitarias 374 / 0 / 0 / 0 · Integración 60 / 0 / 0 / 0 · **Total 434 / 0 / 0 / 0** |
| Cobertura de líneas domain + application (total) | 95,2 % (2570 de 2700 líneas, incluye shared) · identidad 93,6 %, clientes 84,4 %, tiendas 89,2 %, catalogo 100 %, solicitudes 99,7 %, ordenes 96,7 %, mensajeria 100 %, reputacion 97,9 %, notificaciones 100 %, administracion 97,9 %, shared 77,4 % · [`jacoco.csv`](../../evidencias/C2/2026-11-22/jacoco.csv) |
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

### Recorrido en producción

El 21/11, de 9:40 p. m. a 10:48 p. m., Pedro y Claude Code recorrieron la app en https://quienlotiene.tech con usuario1, tienda1 y la cuenta de administrador. Cubrieron 3 pedidos y el push en un celular Android:

- **ECF43503 (recoger):** chat, reserva, recibido y calificación.
- **5E39F3E9 (domicilio):** cancelado por el cliente.
- **F5305EE7:** «En camino» sin poder cancelar; se reportó y el administrador resolvió el reporte.

Detalle y 49 capturas en [`evidencias/C2/2026-11-22/produccion/`](../../evidencias/C2/2026-11-22/produccion/README.md).

## 4. Defectos nuevos (S1 0 / S2 0 / S3 0 / S4 1)

| ID | Severidad | Prioridad | Estado | Caso | Qué falla |
|---|---|---|---|---|---|
| DEF-012 | S4 Baja | P4 Baja | Abierto | Humo producción (CP-ORD-029-01) | En «Mis respuestas» de la tienda, los pedidos ya cancelados o recibidos siguen diciendo «Elegida · Prepara el pedido.» |

Observaciones que no se registran como defecto:

- Al enviar un reporte no aparece un mensaje de confirmación.
- La cuenta regresiva de la reserva muestra 31 minutos en la tienda y 30 en el cliente.

## 5. Bloqueos y acciones

| Bloqueo | Casos afectados | Acción acordada | Responsable | Fecha |
|---|---|---|---|---|
| 7 no funcionales del C2 no tienen una prueba etiquetada con su ID | NF-REN-003, NF-SEG-007, NF-SEG-008, NF-SEG-009, NF-SEG-012, NF-SEG-013, NF-FIA-015 | Etiquetar o escribir sus pruebas y ejecutarlas en la regresión del C3 | Francisco Castro | 2026-11-24 |
| Para probar un sábado de noche se amplió el horario de Tienda Uno | Recorrido en producción | Hecho: se devolvió a lunes a sábado de 07:00 a 18:00 y domingo cerrado el 21/11 a las 10:52 p. m. | Pedro | 2026-11-21 |

## 6. Plan para el siguiente sprint

1. Triaje de DEF-012 y corrección en el C3.
2. Completar las pruebas de los 7 no funcionales pendientes y correrlas con la regresión completa del C3 (85 casos + 5 E2E + 13 NF).
3. Cerrar el C2 con la firma de QA de este informe y de `04-EJECUCION-C2.md`.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Corte del Sprint 3 sobre `v0.5.2`, preparado con Claude Code para que QA lo revise y firme |
