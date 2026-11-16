# Hoja de sesión exploratoria · Pantallas del Sprint 1

> Anexo F del Plan Maestro. Sesión manual sobre la PWA levantada en local con la versión del corte. Las capturas están en `docs/pruebas/evidencias/C1/2026-11-12/frontend/` (pantalla de celular de 412 × 915). Los defectos se registran con el caso `Exploratoria-20261112`.

| Campo | Valor |
|---|---|
| Responsable / fecha | Yanimar Burbano Piamba / 2026-11-12, 16:20 a 16:40 (hora de Colombia) |
| Misión (charter): explorar … con … para descubrir … | Explorar las pantallas del Sprint 1 (registro, sesión, perfil y direcciones, publicar solicitud, registro de tienda, panel de la tienda y revisión del administrador) con datos válidos e inválidos, para descubrir diferencias entre lo que muestra la interfaz y los criterios de aceptación de las historias |
| Duración / ambiente / versión / cuenta | 60 min · ambiente local de QA (backend `java -jar` en perfil dev + PostGIS 17-3.5 en Docker; frontend `vite` 5173 con proxy a la API; navegador Chromium en tamaño de celular, ubicación simulada en Popayán) · v0.2.0 (`ea7351d`) · cuentas de prueba: cliente `ana@correo.com`, tiendas `drogueria5@correo.com` y `ferreteria@correo.com`, administrador `admin.qa@quienlotiene.test` con TOTP |
| Notas de la sesión (qué se probó, ideas) | Se recorrieron 45 vistas y estados, que se detallan en la tabla de abajo. Todas responden como piden las historias: los errores de validación salen en español junto al campo y en un resumen, las reglas del backend (correo repetido, frase filtrada, credenciales, segundo factor y horario) llegan bien a la pantalla, y el flujo completo funciona. El cliente se registra, la tienda se registra y queda en revisión, el administrador la aprueba con segundo factor, la tienda configura y abre, y la solicitud del cliente llega a 1 tienda. Con la tienda cerrada o con otra categoría no llega a ninguna. Idea: en C3 automatizar este mismo recorrido con Playwright (E2E-01). |
| Defectos encontrados | DEF-005 (S4): en «Publicar solicitud» los errores salen por etapas. Con el producto vacío solo avisa ese campo y no muestra el resumen; la falta de categoría aparece en un segundo intento. |
| Preguntas o dudas para el equipo | 1) El mapa usa teselas de OpenStreetMap: en el ambiente de QA, sin salida a internet, el mapa sale gris pero «Usar mi ubicación» funciona. ¿Se acepta para la demo, o se necesita un respaldo? 2) La tienda aún no ve las solicitudes que recibe (HU-018, Sprint 2), así que la difusión solo se ve del lado del cliente («Le preguntamos a 1 tienda cercana»). |
| Cobertura lograda y lo que quedó por explorar | Se cubrieron las 13 historias del Sprint 1 desde la interfaz. Quedó por explorar: cierre de sesión con varias pestañas, límite de 10 solicitudes por hora desde la pantalla (cubierto por la prueba de integración), descarga de documentos por el administrador, accesibilidad con lector de pantalla (C3, NF-USA-028) y la misma sesión en https://quienlotiene.tech (pruebas de humo, C3). |

## Vistas y estados revisados

| # | Captura | Historia · caso | Qué se comprobó | Resultado |
|---|---|---|---|---|
| 1 | [01-inicio](../../evidencias/C1/2026-11-12/frontend/01-inicio.png) | — | Portada con «Crear una cuenta», «Entrar» y «Registra tu tienda» | Como se espera |
| 2 | [02-crear-cuenta-formulario](../../evidencias/C1/2026-11-12/frontend/02-crear-cuenta-formulario.png) | HU-001 | Formulario de registro con la ayuda de la frase (mínimo 15) y la autorización de datos | Como se espera |
| 3 | [03-crear-cuenta-campos-vacios](../../evidencias/C1/2026-11-12/frontend/03-crear-cuenta-campos-vacios.png) | HU-001 | Resumen de errores y error en cada campo obligatorio | Como se espera |
| 4 | [04-crear-cuenta-frase-corta](../../evidencias/C1/2026-11-12/frontend/04-crear-cuenta-frase-corta.png) | HU-001 · CP-IDE-001-03 | Frase de 14 caracteres rechazada («Llevas 14») | Como se espera |
| 5 | [05-crear-cuenta-sin-autorizacion](../../evidencias/C1/2026-11-12/frontend/05-crear-cuenta-sin-autorizacion.png) | HU-005 · CP-IDE-005-01 | Sin marcar la autorización no deja crear la cuenta | Como se espera |
| 6 | [06-crear-cuenta-exitosa](../../evidencias/C1/2026-11-12/frontend/06-crear-cuenta-exitosa.png) | HU-001 · CP-IDE-001-01 / HU-005 · CP-IDE-005-02 | Cuenta creada; lleva a «Entrar» con el correo ya escrito | Como se espera |
| 7 | [07-crear-cuenta-correo-repetido](../../evidencias/C1/2026-11-12/frontend/07-crear-cuenta-correo-repetido.png) | HU-001 · CP-IDE-001-02 | «Este correo ya tiene una cuenta» con enlace para entrar | Como se espera |
| 8 | [08-crear-cuenta-frase-filtrada](../../evidencias/C1/2026-11-12/frontend/08-crear-cuenta-frase-filtrada.png) | HU-001 · CP-IDE-001-03 | Frase común («123456789012345») rechazada por la lista de filtradas | Como se espera |
| 9 | [09-entrar-credenciales-incorrectas](../../evidencias/C1/2026-11-12/frontend/09-entrar-credenciales-incorrectas.png) | HU-003 · CP-IDE-003-02 | Mensaje genérico que no revela si el correo existe | Como se espera |
| 10 | [10-entrar-cliente-exitoso](../../evidencias/C1/2026-11-12/frontend/10-entrar-cliente-exitoso.png) | HU-003 · CP-IDE-003-01 | El cliente entra y llega a «¿Qué estás buscando?» con «Mi cuenta» y «Cerrar sesión» | Como se espera |
| 11 | [11-mi-cuenta](../../evidencias/C1/2026-11-12/frontend/11-mi-cuenta.png) | HU-007 | Datos del cliente, correo de solo lectura y direcciones vacías | Como se espera |
| 12 | [12-direccion-sin-ubicacion](../../evidencias/C1/2026-11-12/frontend/12-direccion-sin-ubicacion.png) | HU-007 · CP-CLI-007-02 | Dirección sin ubicación en el mapa rechazada | Como se espera |
| 13 | [13-direccion-guardada](../../evidencias/C1/2026-11-12/frontend/13-direccion-guardada.png) | HU-007 · CP-CLI-007-01 | Dirección «Casa» guardada con indicaciones y celular | Como se espera |
| 14 | [14-solicitud-incompleta](../../evidencias/C1/2026-11-12/frontend/14-solicitud-incompleta.png) | HU-016 · CP-SOL-016-02 | Rechaza la solicitud sin producto, pero no avisa de la categoría ni muestra el resumen | Funciona, con DEF-005 |
| 15 | [45-solicitud-sin-categoria](../../evidencias/C1/2026-11-12/frontend/45-solicitud-sin-categoria.png) | HU-016 · CP-SOL-016-02 | En el segundo intento sí aparece «Elige dónde buscamos» con el resumen | Como se espera |
| 16 | [15-solicitud-formulario-lleno](../../evidencias/C1/2026-11-12/frontend/15-solicitud-formulario-lleno.png) | HU-016 | La dirección guardada «Casa» sale elegida por defecto | Como se espera |
| 17 | [16-solicitud-publicada](../../evidencias/C1/2026-11-12/frontend/16-solicitud-publicada.png) | HU-016 · CP-SOL-016-01 / HU-017 | Publicada sin tiendas abiertas: avisa que no hay tiendas cerca | Como se espera |
| 18 | [17-registrar-tienda-formulario](../../evidencias/C1/2026-11-12/frontend/17-registrar-tienda-formulario.png) | HU-002 | Formulario con datos, categoría, ubicación, documentos y autorización | Como se espera |
| 19 | [18-registrar-tienda-sin-documentos](../../evidencias/C1/2026-11-12/frontend/18-registrar-tienda-sin-documentos.png) | HU-002 · CP-TIE-002-02 | Sin foto ni RUT, el registro se rechaza y nombra los dos documentos | Como se espera |
| 20 | [19-registrar-tienda-pendiente](../../evidencias/C1/2026-11-12/frontend/19-registrar-tienda-pendiente.png) | HU-002 · CP-TIE-002-01 | «Tu tienda está en revisión» | Como se espera |
| 21 | [20-tienda-pendiente-panel](../../evidencias/C1/2026-11-12/frontend/20-tienda-pendiente-panel.png) | HU-002 / HU-013 | Panel «En revisión» con el interruptor bloqueado | Como se espera |
| 22 | [21-admin-pide-segundo-factor](../../evidencias/C1/2026-11-12/frontend/21-admin-pide-segundo-factor.png) | HU-004 | El administrador pasa a «Código de verificación» | Como se espera |
| 23 | [22-admin-codigo-incorrecto](../../evidencias/C1/2026-11-12/frontend/22-admin-codigo-incorrecto.png) | HU-004 · CP-IDE-004-02 | Código 000000 rechazado | Como se espera |
| 24 | [23-admin-tiendas-pendientes](../../evidencias/C1/2026-11-12/frontend/23-admin-tiendas-pendientes.png) | HU-004 · CP-IDE-004-01 / HU-036 | Con el código TOTP vigente entra y ve las 2 tiendas pendientes con sus documentos | Como se espera |
| 25 | [24-admin-rechazar-pide-motivo](../../evidencias/C1/2026-11-12/frontend/24-admin-rechazar-pide-motivo.png) | HU-036 | Rechazar pide el motivo (máximo 300 caracteres) | Como se espera |
| 26 | [25-admin-tienda-rechazada](../../evidencias/C1/2026-11-12/frontend/25-admin-tienda-rechazada.png) | HU-036 · CP-ADM-036-02 | Ferretería rechazada con «Documento ilegible» | Como se espera |
| 27 | [26-admin-aprobar-confirmacion](../../evidencias/C1/2026-11-12/frontend/26-admin-aprobar-confirmacion.png) | HU-036 | Aprobar pide confirmación | Como se espera |
| 28 | [27-admin-tienda-aprobada](../../evidencias/C1/2026-11-12/frontend/27-admin-tienda-aprobada.png) | HU-036 · CP-ADM-036-01 | «Aprobaste Droguería La 5. Ya puede abrir y recibir solicitudes» | Como se espera |
| 29 | [28-admin-lista-aprobadas](../../evidencias/C1/2026-11-12/frontend/28-admin-lista-aprobadas.png) · [29-admin-lista-rechazadas](../../evidencias/C1/2026-11-12/frontend/29-admin-lista-rechazadas.png) | HU-036 | Filtros por estado; la rechazada muestra fecha de revisión y motivo | Como se espera |
| 30 | [30-tienda-aprobada-panel-cerrada](../../evidencias/C1/2026-11-12/frontend/30-tienda-aprobada-panel-cerrada.png) | HU-013 | Panel «Aprobada», interruptor habilitado y en Cerrada | Como se espera |
| 31 | [31-tienda-perfil-formulario](../../evidencias/C1/2026-11-12/frontend/31-tienda-perfil-formulario.png) | HU-009 | Perfil y horario por día | Como se espera |
| 32 | [32-tienda-horario-invalido](../../evidencias/C1/2026-11-12/frontend/32-tienda-horario-invalido.png) | HU-009 | Cierre antes de la apertura rechazado | Como se espera |
| 33 | [33-tienda-perfil-horario-guardado](../../evidencias/C1/2026-11-12/frontend/33-tienda-perfil-horario-guardado.png) | HU-009 · CP-TIE-009-02 | Celular y horario de 7:00 a 22:00 todos los días guardados | Como se espera |
| 34 | [34-tienda-domicilio-tarifa-invalida](../../evidencias/C1/2026-11-12/frontend/34-tienda-domicilio-tarifa-invalida.png) | HU-010 · CP-TIE-010-02 | Tarifa -5000 rechazada | Como se espera |
| 35 | [35-tienda-domicilio-guardado](../../evidencias/C1/2026-11-12/frontend/35-tienda-domicilio-guardado.png) | HU-010 · CP-TIE-010-01 | Domicilio a 3 km con tarifa fija de $ 5.000 | Como se espera |
| 36 | [39-tienda-domicilio-simulacion-1km](../../evidencias/C1/2026-11-12/frontend/39-tienda-domicilio-simulacion-1km.png) | HU-010 · CP-TIE-010-01 | Simulación: «El cliente pagaría $ 5.000 de domicilio» | Como se espera |
| 37 | [40-tienda-domicilio-fuera-de-cobertura](../../evidencias/C1/2026-11-12/frontend/40-tienda-domicilio-fuera-de-cobertura.png) | HU-010 | A 5 km: fuera de cobertura | Como se espera |
| 38 | [36-tienda-medios-ninguno](../../evidencias/C1/2026-11-12/frontend/36-tienda-medios-ninguno.png) | HU-012 | Sin ningún medio de pago: «Elige al menos un medio de pago» | Como se espera |
| 39 | [37-tienda-medios-guardados](../../evidencias/C1/2026-11-12/frontend/37-tienda-medios-guardados.png) | HU-012 · CP-TIE-012-01 / CP-TIE-012-02 | Solo efectivo y transferencia activos; datáfono desactivado | Como se espera |
| 40 | [38-tienda-abierta](../../evidencias/C1/2026-11-12/frontend/38-tienda-abierta.png) | HU-013 · CP-TIE-013-02 | Interruptor en «Abierta» | Como se espera |
| 41 | [41-solicitud-llega-a-tienda-abierta](../../evidencias/C1/2026-11-12/frontend/41-solicitud-llega-a-tienda-abierta.png) | HU-017 · CP-SOL-017-01 | Con la farmacia abierta: «Le preguntamos a 1 tienda cercana», con vencimiento a 30 min y radio de 3 km | Como se espera |
| 42 | [42-solicitud-otra-categoria-sin-tiendas](../../evidencias/C1/2026-11-12/frontend/42-solicitud-otra-categoria-sin-tiendas.png) | HU-017 · CP-SOL-017-02 | Una solicitud de Ferretería no llega a la farmacia (la ferretería está rechazada) | Como se espera |
| 43 | [43-tienda-cerrada](../../evidencias/C1/2026-11-12/frontend/43-tienda-cerrada.png) | HU-013 · CP-TIE-013-01 | La tienda vuelve a Cerrada | Como se espera |
| 44 | [44-solicitud-tienda-cerrada-no-la-recibe](../../evidencias/C1/2026-11-12/frontend/44-solicitud-tienda-cerrada-no-la-recibe.png) | HU-013 · CP-TIE-013-01 | Con la farmacia cerrada, la solicitud no llega a ninguna tienda | Como se espera |
