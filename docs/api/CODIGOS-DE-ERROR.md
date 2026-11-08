# Códigos de error de la API · Quién lo tiene

Cada error de negocio responde con `ProblemDetail` (RFC 9457) y un `codigo` estable (DECISIONES-BACKEND, sección 8). Un código publicado no cambia nunca: el frontend y las pruebas se apoyan en él. Cada pull request que agregue un código lo registra aquí.

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
| `SOL-001` | 422 | Límite de solicitudes superado | El cliente intenta publicar más de 10 solicitudes en una hora | HU-016 |
