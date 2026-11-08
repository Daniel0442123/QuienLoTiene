# Códigos de error de la API · Quién lo tiene

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
| `SOL-001` | 429 | Límite de solicitudes superado | El cliente intenta publicar más de 10 solicitudes en una hora; la cabecera `Retry-After` dice en cuántos segundos puede volver a intentar (CP-SOL-016-03) | HU-016 |
| `SOL-002` | 400 | Solicitud incompleta | Falta el producto, la categoría, la modalidad o la ubicación, o la cantidad no está entre 1 y 999 | HU-016 |
| `GEN-009` | 400 | Ubicación inválida | La latitud no está entre -90 y 90 o la longitud no está entre -180 y 180 | HU-016 |
