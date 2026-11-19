# Backlog del Sprint 3 del backend · Quién lo tiene

> **Control del documento** · Versión 0.2 · 19/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: Propuesta para la planning · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

Propuesta para la planning del Sprint 3 (la ajustan Wilson como Product Owner y Brayan como Scrum Master).

Fuentes: alcance del Sprint 3 en `docs/pruebas/02-PLAN-DE-PRUEBAS-C2.md`, casos en `docs/pruebas/libro/02-CASOS.md`, matriz `docs/pruebas/libro/07-TRAZABILIDAD.md`, pendientes de SC-011 (puntos 5 a 9), SC-018 y C-15, y convenciones de `docs/arquitectura/DECISIONES-BACKEND.md`.

> **Ojo con las fechas:** el Sprint 3 (ciclo C2) va del 16/11 al 22/11/2026 y el código empieza el 19/11, porque los días 16 a 18 se fueron en el cierre del C1, `v0.3.0`, `v0.3.1` (DEF-011) y SC-017. Quedan cuatro días. Por eso el orden de la sección 3 pone primero lo Must y lo que desbloquea a otras historias, y deja de último lo que puede pasar al Cierre sin romper el flujo principal (HU-031 y la Could HU-008).

## 1. Objetivo del sprint

Que el cliente pueda cancelar su pedido, calificar la tienda y reportar un problema; que la recogida tenga reserva con vencimiento; que el cliente vea el detalle público de la tienda y el aviso de productos restringidos; y que el administrador gestione reportes, suspensiones, categorías y la lista de restringidos.

## 2. Qué ya existe y se reutiliza

| Pieza | Dónde | Sirve para |
|---|---|---|
| Estados `CANCELADO` y `VENCIDA` de la orden | `ordenes/domain/model/EstadoOrden` | HU-029 y HU-025: falta la transición y quién la dispara |
| Ubicación de la tienda en la orden | `OrdenVista.Ubicacion` | HU-026: el botón «Cómo llegar» es solo de interfaz |
| Estado `SUSPENDIDA` de la tienda y error IDE-009 al iniciar sesión | `tiendas`, `identidad/IniciarSesion` | HU-037: falta el caso de uso del administrador que suspende |
| Auditoría | `administracion/RegistroAuditoria` | HU-037 y HU-038: cada acción del administrador queda auditada |
| Tabla `catalogo.categoria` y `GET /api/v1/categorias` | `catalogo` (SC-007) | HU-038: falta activar y desactivar |
| Flujo SSE con `Last-Event-ID` | `notificaciones` | HU-033 (mensajes sin recargar), HU-029 y HU-025 (avisos) |
| Módulo `reputacion` | solo carpetas vacías | HU-034 y HU-035 lo estrenan |

## 3. Historias en orden de construcción

T-00 va primero: contrato `docs/api/contrato-sprint-3.yaml` (OpenAPI 3.1) y `CONTRATO-SPRINT-3.md` con rutas, cuerpos, códigos de error (`ORD-`, `REP-`, `ADM-`, `CAT-`, `MEN-`) y eventos SSE nuevos, igual que en el Sprint 2, para que Monica trabaje con MSW mientras el backend avanza.

| Orden | Historia | MoSCoW | Módulo | Casos | Qué hace el backend | Depende de |
|---|---|---|---|---|---|---|
| 1 | HU-029 Cancelar orden | Should | ordenes | CP-ORD-029-01, -02 | `POST /api/v1/ordenes/{id}/cancelacion` (cliente) desde CONFIRMADO o PREPARANDO; desde EN_CAMINO se rechaza con un error que sugiere reportar. Evento `OrdenCancelada` por SSE a la tienda; la tienda deja de ver los datos del cliente (SC-011 punto 8) | — |
| 2 | HU-025 Reservar para recogida | Should | ordenes | CP-ORD-025-01, -02 | Reserva de 30 min en órdenes de recogida (`reservada_hasta`); tarea programada con `Clock` que pasa a VENCIDA (CU-30) y avisa a ambos; recoger a tiempo sigue a RESUELTA | HU-029 (mismo módulo y migración) |
| 3 | HU-034 Calificar tienda | Must | reputacion | CP-REP-034-01, -02, -03 | `POST /api/v1/ordenes/{id}/resena` de 1 a 5 estrellas y comentario opcional; solo orden RESUELTA del cliente y una sola vez (índice único); promedio y conteo por tienda | — |
| 4 | HU-014 Ver detalle de tienda | Should | tiendas | CP-TIE-014-01, -02 | `GET /api/v1/tiendas/{id}` para el cliente: fotos, horario, ubicación, teléfono, medios de pago, promedio y últimas reseñas. Sin RUT ni documentos (prueba de seguridad) | HU-034 (reseñas) |
| 5 | HU-038 Categorías y restringidos | Should | catalogo | CP-CAT-038-01, -02 | Administrador: activar o desactivar categoría (las inactivas no salen al publicar) y alta, baja y lista de `catalogo.producto_restringido`, con auditoría. Fachada `CatalogoApi.esRestringido(texto)` sin tildes (`unaccent`) | — |
| 6 | HU-023 Advertir productos restringidos | Should | solicitudes | CP-SOL-023-01, -02 | La solicitud queda marcada `restringida` al publicarse y la respuesta trae el aviso «Este medicamento necesita fórmula médica» (SC-011); una solicitud restringida no admite domicilio al crear la orden | HU-038 |
| 7 | HU-035 Reportar problema | Should | reputacion | CP-REP-035-01, -02 | `POST /api/v1/ordenes/{id}/reportes` con motivo de la lista de SC-011 y detalle; solo quien participa en la orden; queda ABIERTO | — |
| 8 | HU-037 Gestionar reportes y suspender | Should | administracion | CP-ADM-037-01, -02 | Administrador: lista de reportes, resolver con nota, suspender y reactivar cuenta (la tienda suspendida no inicia sesión ni recibe solicitudes). Todo auditado | HU-035 |
| 9 | HU-033 Mini chat | Should | mensajeria | CP-MEN-033-01, -02 | `GET` y `POST /api/v1/ordenes/{id}/mensajes`; solo los dos participantes; entrega por SSE (`MensajeRecibido`); en orden RESUELTA, CANCELADO o VENCIDA queda en solo lectura (409) | — |
| 10 | HU-026 Ver cómo llegar | Should | ordenes / frontend | CP-ORD-026-01 | Nada nuevo en el backend: la orden ya trae la ubicación. Monica arma el enlace a la app de mapas | — |
| 11 | HU-031 Notificaciones push web | Should | notificaciones | CP-NOT-031-01, -02 | Suscripción `POST` y `DELETE /api/v1/notificaciones/suscripciones` y envío Web Push (VAPID) cuando llega una solicitud nueva a una tienda sin sesión abierta. Necesita una librería nueva y claves VAPID en el VPS: es la de más riesgo | — |
| 12 | HU-008 Historial y repetir + SC-018 «Mis preguntas» | Could | clientes / solicitudes | CP-CLI-008-01, -02 | `GET /api/v1/solicitudes/mias` y «Repetir». Solo si sobra tiempo y el comité aprueba SC-018; si no, pasa al Cierre | Decisión de SC-018 |

**Corte si no alcanza:** HU-031 y HU-008 pasan al Cierre; HU-033 es la siguiente candidata. Las historias 1 a 8 son el mínimo del sprint.

**Corte aplicado (19/11/2026):** las historias 1 a 10 entraron en el PR #63. Pedro decidió pasar HU-031 y HU-008 al Cierre.

## 4. Pruebas que acompañan cada pull request

- Cada `@Test` con `@Tag("HU-xxx")` y `@Tag("CP-xxx")`, y `@DisplayName` que empieza con el ID del caso (plan C2 §5).
- Unitarias sin Spring con `Clock` inyectado para HU-025; transición de estados para HU-029, HU-033, HU-037 y HU-038.
- Prueba negativa de autorización por objeto en cada endpoint nuevo con datos personales (NF-SEG-007): orden ajena en HU-029, HU-033, HU-034 y HU-035.
- HU-014 con prueba de integración (API) que verifica que no salen el RUT ni los documentos (CP-TIE-014-02).
- Cobertura ≥ 70 % en `domain` y `application` de `reputacion`, que hoy está vacío.

## 5. Pull requests previstos

Uno por par de historias cercanas, para que QA revise por partes: (1) contrato del Sprint 3; (2) HU-029 + HU-025; (3) HU-034 + HU-014; (4) HU-038 + HU-023; (5) HU-035 + HU-037; (6) HU-033; (7) HU-031; (8) HU-008 si entra.

## 6. Definición de terminado (recordatorio)

Pull request aprobado por el arquitecto de software, CI en verde (formato, unitarias, Modulith, ArchUnit, cobertura ≥ 70 %, integración y escaneos), criterios de aceptación verificados por QA y resultado anotado en el libro de control (`04-EJECUCION-C2.md`).

## 7. Pendiente fuera de este backlog

- PR #62 (cierre del C1) espera las firmas de Yanimar, Francisco y Wilson; no bloquea el Sprint 3.
- C-15: mockups del Sprint 3 (HU-014 con mapa, HU-023, HU-025 vencida, HU-029 en camino, HU-033 solo lectura, HU-008 vacío), a cargo de Monica.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| 0.1 | 19/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Propuesta del backlog del Sprint 3 del backend para la planning |
| 0.2 | 19/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Corte aplicado: HU-031 y HU-008 pasan al Cierre (decisión de Pedro) |
