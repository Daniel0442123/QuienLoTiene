# Códigos de error de la API · Quién lo tiene

> **Control del documento** · Versión 1.0 · 13/11/2026 · Responsable: Daniel Montenegro Almendra (arquitecto de software) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

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
| `TIE-001` | 400 | Tienda incompleta | Falta el nombre, la categoría, el teléfono o la ubicación de la tienda | HU-002 |
| `TIE-002` | 400 | Documento faltante | El registro llega sin la foto del local o sin el RUT (CP-TIE-002-02) | HU-002 |
| `TIE-003` | 400 | Archivo no permitido | Un documento pesa más de 5 MB o no es JPEG, PNG, WebP ni PDF (revisado por contenido) | HU-002 |
| `TIE-004` | 409 | Cambio de estado no permitido | Se intenta aprobar o rechazar una tienda que no está PENDIENTE | HU-036 |
| `TIE-005` | 400 | Motivo de rechazo inválido | El rechazo llega sin motivo o con más de 300 caracteres | HU-036 |
| `TIE-006` | 404 | Tienda no encontrada | La tienda o el documento pedido no existe | HU-036 |
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
