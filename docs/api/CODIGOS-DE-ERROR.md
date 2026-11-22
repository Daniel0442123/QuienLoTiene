# Códigos de error de la API · Quién lo tiene

> **Control del documento** · Versión 1.3 · 22/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

Cada error de negocio responde con `ProblemDetail` (RFC 9457) y un `codigo` estable (DECISIONES-BACKEND, sección 8). Todas las respuestas de error traen además `idCorrelacion`, el mismo valor de la cabecera `X-Correlacion-Id`, para buscar la petición en los logs. Un código publicado no cambia nunca: el frontend y las pruebas se apoyan en él. Cada pull request que agregue un código lo registra aquí.

| Prefijo | Módulo |
|---|---|
| `GEN-` | Genéricos |
| `IDE-` | identidad |
| `CLI-` | clientes |
| `TIE-` | tiendas |
| `CAT-` | catalogo |
| `SOL-` | solicitudes |
| `ORD-` | ordenes |
| `MEN-` | mensajeria |
| `REP-` | reputacion |
| `NOT-` | notificaciones |
| `ADM-` | administracion |

## Códigos

| Código | HTTP | Título | Cuándo ocurre | Historia |
|---|---|---|---|---|
| `GEN-001` | 400 | Datos inválidos | Falla la validación de forma del `Request`; trae la lista `errores` | — |
| `GEN-002` | 400 | Petición mal formada | El cuerpo no es JSON válido, falta un parámetro o un valor no tiene el tipo esperado | T-01 |
| `GEN-003` | 401 | Sesión requerida | La petición no trae un token válido o el token venció | T-01 |
| `GEN-004` | 403 | Sin permiso | El usuario no tiene el rol que pide la operación | T-01 |
| `GEN-005` | 404 | Recurso no encontrado | La ruta no existe | T-01 |
| `GEN-006` | 405 | Método no permitido | La ruta existe pero no acepta ese método HTTP | T-01 |
| `GEN-007` | 415 / 406 | Formato no soportado | El `Content-Type` enviado o el `Accept` pedido no están soportados | T-01 |
| `GEN-008` | 500 | Error interno | Falla inesperada; el detalle técnico queda solo en el log con el `idCorrelacion` | T-01 |
| `IDE-001` | 409 | Correo ya registrado | Alguien intenta registrarse con un correo que ya tiene cuenta | HU-001 |
| `IDE-002` | 400 | Frase de contraseña muy corta | La frase tiene menos de 15 caracteres | HU-001 |
| `IDE-003` | 400 | Frase de contraseña común | La frase está en la lista de contraseñas comunes o filtradas, o es un mismo carácter repetido | HU-001 |
| `IDE-004` | 400 | Frase de contraseña muy larga | La frase tiene más de 128 caracteres | HU-001 |
| `IDE-005` | 400 | Correo inválido | El correo no tiene la forma nombre@dominio.tld o supera 254 caracteres | HU-001 |
| `IDE-006` | 400 | Autorización de datos requerida | El registro llega sin aceptar la política de tratamiento de datos | HU-005 |
| `IDE-007` | 409 | Versión de la política desactualizada | La versión aceptada no es la vigente; la pantalla debe mostrar la nueva | HU-005 |
| `IDE-008` | 401 | Credenciales incorrectas | El correo no tiene cuenta o la frase no coincide; el mensaje no dice cuál de los dos falló | HU-003 |
| `IDE-009` | 403 | Cuenta suspendida | Las credenciales son correctas pero el administrador suspendió la cuenta | HU-003 |
| `IDE-010` | 401 | Sesión vencida | Al renovar o cerrar sesión: falta la cookie, el token de refresco venció o fue revocado | HU-003 |
| `IDE-011` | 401 | Segundo factor requerido | Un administrador sin secreto TOTP configurado intenta iniciar sesión | HU-003, HU-004 |
| `IDE-012` | 401 | Código incorrecto | El código TOTP no es válido o ya venció (CP-IDE-004-02) | HU-004 |
| `IDE-013` | 401 | Verificación vencida | El token del segundo factor no existe, venció (5 minutos), ya se usó o agotó sus 5 intentos | HU-004 |
| `IDE-014` | 400 | Secreto TOTP inválido | El secreto del administrador inicial no está en Base32 o tiene menos de 26 caracteres | HU-004 |
| `IDE-015` | 429 | Demasiados intentos | La misma cuenta hizo más de 5 intentos de inicio de sesión en un minuto (RNF-009, NF-SEG-009); va con `Retry-After` | HU-003 |
| `TIE-001` | 400 | Tienda incompleta | Falta el nombre, la categoría, el teléfono o la ubicación de la tienda | HU-002 |
| `TIE-002` | 400 | Documento faltante | El registro llega sin la foto del local o sin el RUT (CP-TIE-002-02) | HU-002 |
| `TIE-003` | 400 | Archivo no permitido | Un documento pesa más de 5 MB o no es JPEG, PNG, WebP ni PDF (revisado por contenido) | HU-002 |
| `TIE-004` | 409 | Cambio de estado no permitido | Se intenta aprobar o rechazar una tienda que no está PENDIENTE, suspender una que no está APROBADA o reactivar una que no está SUSPENDIDA | HU-036, HU-037 |
| `TIE-005` | 400 | Motivo de rechazo inválido | El rechazo llega sin motivo o con más de 300 caracteres | HU-036 |
| `TIE-006` | 404 | Tienda no encontrada | La tienda o el documento pedido no existe; en el detalle público, también si la tienda no está APROBADA | HU-036, HU-014 |
| `TIE-007` | 400 | Horario inválido | Un día aparece dos veces o una franja abre después de cerrar | HU-009 |
| `TIE-008` | 409 | Tienda no aprobada | La tienda intenta abrir o cerrar antes de que el administrador la apruebe | HU-013 |
| `TIE-009` | 400 | Domicilio inválido | Tarifa negativa, radio fuera de 500 a 10.000 m o domicilio activo sin radio o tarifa (CP-TIE-010-02) | HU-010 |
| `TIE-010` | 422 | Fuera de cobertura | La tienda no hace domicilio o el cliente está más lejos que su radio de cobertura | HU-010 |
| `TIE-011` | 400 | Medios de pago inválidos | La tienda no elige ningún medio de pago o envía uno desconocido | HU-012 |
| `CLI-001` | 400 | Dirección incompleta | Falta el nombre, la ubicación en el mapa, la dirección escrita o el teléfono (CP-CLI-007-02) | HU-007 |
| `CLI-002` | 422 | Límite de direcciones | El cliente ya tiene 10 direcciones guardadas | HU-007 |
| `CLI-003` | 404 | Dirección no encontrada | La dirección no existe o es de otro cliente | HU-007 |
| `GEN-010` | 413 | Archivo muy grande | La petición trae un archivo de más de 5 MB; la corta el servidor antes de llegar al caso de uso | HU-002 |
| `SOL-001` | 429 | Límite de solicitudes superado | El cliente intenta publicar más de 10 solicitudes en una hora; la cabecera `Retry-After` dice en cuántos segundos puede volver a intentar (CP-SOL-016-03) | HU-016 |
| `SOL-002` | 400 | Solicitud incompleta | Falta el producto, la categoría, la modalidad o la ubicación, o la cantidad no está entre 1 y 999 | HU-016 |
| `GEN-009` | 400 | Ubicación inválida | La latitud no está entre -90 y 90 o la longitud no está entre -180 y 180 | HU-016 |
| `SOL-003` | 404 | Solicitud no encontrada | La solicitud no existe, es de otro cliente o no llegó a la tienda que la pide | HU-018, HU-019, HU-020 |
| `SOL-004` | 409 | Solicitud cerrada | La solicitud ya no está ABIERTA, venció o ya tiene una respuesta elegida: no acepta más respuestas ni elecciones (CP-SOL-021-01) | HU-019, HU-021, HU-024 |
| `SOL-005` | 409 | Respuesta duplicada | La tienda ya respondió esta solicitud (CP-SOL-019-04) | HU-019 |
| `SOL-006` | 400 | Respuesta incompleta | Falta el precio o las unidades con SI o PARECIDO, falta la descripción de la alternativa con PARECIDO (CP-SOL-019-02) o las unidades superan las pedidas | HU-019 |
| `SOL-007` | 404 | Respuesta no encontrada | La respuesta elegida no existe o no es de una solicitud del cliente | HU-024 |
| `SOL-008` | 422 | Respuesta no elegible | El cliente intenta elegir una respuesta «No lo tengo» | HU-024 |
| `ORD-001` | 404 | Orden no encontrada | La orden no existe o no es del cliente ni de la tienda que la pide (CP-ORD-024-03, CP-ORD-028-02) | HU-024, HU-027, HU-028 |
| `ORD-002` | 409 | Cambio de estado no permitido | La orden no puede pasar a ese estado desde el actual, por ejemplo de CONFIRMADO a ENTREGADO (CP-ORD-027-02) o confirmar recepción antes de ENTREGADO | HU-027, HU-028 |
| `ORD-003` | 422 | Medio de pago no aceptado | El cliente elige un medio de pago que la tienda no tiene activo (CP-ORD-024-04) | HU-024 |
| `ORD-004` | 400 | Orden incompleta | Pide domicilio sin elegir la dirección de entrega | HU-024 |
| `MEN-001` | 409 | Contacto no disponible | Se pide el WhatsApp de una orden que ya no está activa (resuelta, cancelada o vencida) | HU-032 |
| `ORD-005` | 409 | La orden ya fue despachada | El cliente intenta cancelar una orden EN_CAMINO o ENTREGADO; se le sugiere escribir a la tienda o reportar (CP-ORD-029-02) | HU-029 |
| `ORD-006` | 422 | Domicilio no disponible | Se pide domicilio en una orden de un producto restringido (CP-SOL-023-02) | HU-023 |
| `MEN-002` | 409 | Chat en solo lectura | Se escribe en el chat de una orden que ya terminó (resuelta, cancelada o vencida) (CP-MEN-033-02) | HU-033 |
| `MEN-003` | 400 | Mensaje inválido | El mensaje está vacío o tiene más de 1000 caracteres | HU-033 |
| `REP-001` | 422 | Calificación no permitida | Se califica una orden que no está RESUELTA o que no es del cliente (CP-REP-034-02) | HU-034 |
| `REP-002` | 409 | Ya calificaste | El cliente califica dos veces la misma orden (CP-REP-034-03) | HU-034 |
| `REP-003` | 404 | No encontrado | La orden todavía no tiene reseña, o el reporte no existe | HU-034, HU-037 |
| `REP-004` | 400 | Datos inválidos | Estrellas fuera de 1 a 5, comentario de más de 500, motivo de reporte desconocido, OTRO sin descripción, descripción de más de 1000, nota de resolución fuera de 3 a 500 o estado de bandeja desconocido | HU-034, HU-035, HU-037 |
| `REP-005` | 409 | Reporte ya resuelto | El administrador resuelve un reporte que ya estaba RESUELTO | HU-037 |
| `CAT-001` | 404 | No encontrado | La categoría o la palabra restringida no existe | HU-038 |
| `CAT-002` | 409 | Ya está en la lista | La palabra restringida ya estaba en la lista (sin importar mayúsculas ni tildes) | HU-038 |
| `CAT-003` | 400 | Palabra inválida | La palabra restringida tiene menos de 2 o más de 80 caracteres | HU-038 |
| `NOT-001` | 400 | Suscripción inválida | La suscripción push trae un endpoint que no es HTTPS de un servicio push de navegador conocido, o claves `p256dh` y `auth` de otro tamaño | HU-031 |

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(shared): base común de errores, correlación y reloj (T-01) (#2) |
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(identidad): registrar cliente (HU-001) (#4) |
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(identidad): exige y guarda la autorización de datos en el registro (#5) |
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(identidad): inicia, renueva y cierra sesión con refresco rotativo (#6) |
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(solicitudes): publica solicitudes con límite de 10 por hora (#7) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(identidad): agrega segundo factor TOTP para el administrador (#8) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(tiendas): registra tiendas con documentos y cuenta del dueño (#9) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(administracion): aprueba o rechaza tiendas con auditoría (#10) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(tiendas): perfil y horario de la tienda (HU-009) (#11) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(tiendas): abre o cierra la tienda y expone TiendasApi para la difusión (#12) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(tiendas): configura domicilio con tarifa fija (#14) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(tiendas): configura los medios de pago de la tienda (#15) |
| — | 08/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | feat(clientes): gestiona las direcciones y el perfil del cliente (#16) |
| 1.0 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (SC-009) |
| 1.1 | 16/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Códigos del Sprint 2: `SOL-003` a `SOL-008`, `ORD-001` a `ORD-004` y `MEN-001` (contrato del Sprint 2) |
| 1.2 | 19/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Códigos del Sprint 3: `ORD-005`, `ORD-006`, `MEN-002`, `MEN-003`, `REP-001` a `REP-005` y `CAT-001` a `CAT-003` y `NOT-001`; `TIE-004` y `TIE-006` cubren la suspensión y el detalle público |
| 1.3 | 22/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | `IDE-015` (429) para el límite de intentos de inicio de sesión de RNF-009 (DEF-013) |
