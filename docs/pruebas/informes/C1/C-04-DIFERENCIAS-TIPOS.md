# C-04 · Tipos generados desde /v3/api-docs · diferencias para Monica

> **Control del documento** · Versión 1.0 · 17/11/2026 · Responsable: Yanimar Burbano Piamba (líder de QA) · Estado: Borrador · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

Compromiso C-04 de la bitácora del proyecto: generar `frontend/src/services/api-tipos.ts` desde el backend y borrar `api-tipos-provisionales.ts` y `api-tipos-sprint-2.ts`.

## Qué se hizo

1. Backend `main` (`5e8ab98`, con #35 a #45) en perfil dev con PostGIS. `GET /v3/api-docs` responde 200 ([copia](../../evidencias/C1/2026-11-17/contrato/api-docs-20261117.json)).
2. `npm run api:tipos` genera `api-tipos.ts` sin errores: 2.279 líneas ([copia](../../evidencias/C1/2026-11-17/contrato/api-tipos-generado.ts.txt)).
3. En una copia aparte del frontend se cambiaron los tipos provisionales y los del contrato por los generados, y se corrió `npm run tipos`: **35 errores** ([salida de tsc](../../evidencias/C1/2026-11-17/contrato/tsc-solo-tipos-generados.txt)).
4. Además se comparó el contrato `docs/api/contrato-sprint-2.yaml` con lo que publica el backend ([diferencias](../../evidencias/C1/2026-11-17/contrato/diferencias-contrato-vs-backend.txt)): las 17 operaciones del contrato existen en el backend con la misma ruta y método.

**Conclusión:** C-04 todavía no se puede cerrar con un solo cambio en el frontend. Unas diferencias se corrigen en el backend (DEF-006) y otras en el frontend. Mientras tanto, el frontend funciona contra el backend real con sus tipos actuales: el recorrido de punta a punta del 17/11 pasó (ver `RECORRIDO-SPRINT-2-20261117.md`). `api-tipos.ts` no se sube regenerado en este PR para no romper `npm run tipos`.

## A. Se corrigen en el backend (DEF-006, Daniel)

| # | Diferencia | Dónde falla en el frontend | Qué se propone |
|---|---|---|---|
| A1 | 9 operaciones no declaran su respuesta 2xx, solo los errores: `POST /admin/tiendas/{id}/aprobacion` y `/rechazo`, `GET /tiendas/me`, `PATCH /tiendas/me/apertura`, `PUT /tiendas/me/perfil`, `PUT /tiendas/me/domicilio`, `PUT /tiendas/me/medios-pago`, `GET /tiendas/me/domicilio/simulacion`, `PUT /clientes/me/direcciones/{id}`. El tipo generado de su éxito queda `undefined` | `administracion/api.ts` (55, 63), `PanelTiendaPagina` (79, 80), `PerfilTiendaPagina` (18), `InterruptorAperturaTienda` (48), `SimuladorDomicilio` (34), `tiendas/FormularioDomicilio` (68) | Agregar `@ApiResponse(responseCode = "200", …)` con el esquema en esas operaciones. Springdoc deja de poner el 200 por defecto cuando el método trae `@ApiResponses` |
| A2 | Todas las respuestas salen con tipo `*/*` | Ninguno hoy (openapi-typescript las lee), pero RNF-024 pide `application/json` y `application/problem+json` | `produces` en los controladores o `springdoc.default-produces-media-type=application/json` |
| A3 | `OrdenResponse.historial[].estado` es `string` sin enum | `ordenes/LineaDeTiempo` (42), `ordenes/useAnunciarCambiosDeEstado` (14) | `allowableValues` o el tipo `EstadoOrden` en el record del historial |
| A4 | Los eventos SSE (`EventoSolicitudNueva`, `EventoRespuestaRecibida`, `EventoSolicitudVencida`, `EventoOrdenCreada`…) no están en `/v3/api-docs` | `notificaciones/useTiempoReal` (25, 37, 47) | Publicarlos como esquemas (por ejemplo con un `@Schema` en `EventosTiempoReal`) o dejar a propósito un archivo pequeño de tipos de eventos en el frontend y anotarlo en DECISIONES-FRONTEND |
| A5 | `DatosDireccion.id` sale `string \| null` | `clientes/FormularioDireccion` (93), `ListaDirecciones` (116, 134, 139), `solicitudes/UbicacionSolicitud` (30, 75) | Una dirección guardada siempre tiene id: quitar el `@Nullable` del id en la respuesta |
| A6 | No hay esquemas con nombre para `EstadoOrden`, `MedioPago`, `EstadoRespuesta` ni `DiaSemana` (van en línea) | `ordenes/api.ts` (11, 12), `solicitudes/api.ts` (14) | Con springdoc se pueden publicar con `@Schema(enumAsRef = true)`. Si no, el frontend los saca del campo (por ejemplo `OrdenResponse['estado']`) |

## B. Se corrigen en el frontend (Monica)

| # | Diferencia | Archivo | Qué cambiar |
|---|---|---|---|
| B1 | `CrearOrdenRequest.direccionId` no admite `null`; el formulario lo manda en `null` cuando es recoger | `ordenes/api.ts` (32) | Omitir `direccionId` si la modalidad es RECOGER |
| B2 | `DireccionRequest.indicaciones` no admite `null` | `clientes/api.ts` (57, 68) | Omitir el campo si está vacío |
| B3 | `ResponderSolicitudRequest`: el cuerpo de «No lo tengo» no encaja con el tipo generado | `solicitudes/api.ts` (117) | Mandar solo `{ tipo: 'NO' }` o tipar el cuerpo con el esquema generado |
| B4 | `Domicilio.tipoTarifa` es el enum `'FIJA' \| null`; los datos de prueba usan `string` | `tiendas/FormularioDomicilio.test.tsx` (7 líneas) | Usar `'FIJA' as const` en los datos de prueba |
| B5 | El mapa de estados de «Mis respuestas» no cubre el tipo generado | `solicitudes/TarjetaMiRespuesta` (16) | Tipar el mapa con `Record<RespuestaDeTiendaResponse['estado'], …>` |
| B6 | El multipart del registro de tienda: openapi-typescript tipa `datos` como objeto y los archivos como `string` | `tiendas/api.ts` (40–43) | Limitación conocida de openapi-typescript: mantener el `FormData` con una conversión explícita y un comentario |
| B7 | `clienteApi` usa `paths & PathsProvisionales & PathsSprint2`; con el `api-tipos.ts` generado, las intersecciones chocan (por ejemplo `historial`) | `services/clienteApi.ts` | Al cerrar C-04, dejar solo `paths` del archivo generado y borrar los dos archivos provisionales |

## C. Contrato del Sprint 2 frente al backend

Las 17 operaciones del contrato coinciden en ruta y método. Las diferencias de los esquemas son de forma: el contrato usa enums con nombre (`$ref`) y el backend en línea; el backend agrega `format: int32` en los enteros; y en `ResponderSolicitudRequest` y `CrearOrdenRequest` el contrato marca como `nullable` campos que el backend deja opcionales sin `null` (de ahí B1 a B3). Ninguna cambia lo que el frontend manda o recibe en el recorrido.

## Orden sugerido

1. Daniel corrige A1, A3, A5 y, si se decide, A4 y A6 (DEF-006).
2. Monica regenera con `npm run api:tipos`, corrige B1 a B7, borra los dos archivos provisionales y cierra C-04.
3. QA vuelve a correr `npm run tipos` y el recorrido de punta a punta.

Para levantar el backend sin Docker Desktop en el equipo de Monica, los pasos que se usaron están en `evidencias/C1/2026-11-17/scripts/` (base PostGIS en un contenedor y `java -jar` con el perfil dev).

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 1.0 | 17/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Primera versión, del recorrido del Sprint 2 (C1) |
