# Pruebas de humo posteriores al despliegue

> Anexo H del Plan Maestro. En producción el 26/11/2026, después de desplegar v1.0.0. Si una falla, reversión inmediata a la versión anterior. Responsables: Yanimar Burbano Piamba y Francisco José Castro Muñoz.

Fecha y hora: · Versión: · URL:

| # | Verificación | OK | Observación |
|---|---|---|---|
| 1 | `/actuator/health` responde UP y la versión es v1.0.0 | ☐ | |
| 2 | HTTPS válido con TLS 1.2/1.3 y HSTS | ☐ | |
| 3 | La PWA carga en un celular Android y en un escritorio | ☐ | |
| 4 | Ingreso de un administrador con segundo factor | ☐ | |
| 5 | Ingreso de un cliente y de una tienda de prueba | ☐ | |
| 6 | Un cliente publica una solicitud y una tienda cercana la ve en tiempo real | ☐ | |
| 7 | La tienda responde y el cliente ve la respuesta sin recargar | ☐ | |
| 8 | El cliente elige y se crea la orden con el total correcto | ☐ | |
| 9 | Se sube una imagen válida y se rechaza un archivo no permitido | ☐ | |
| 10 | El respaldo automático de la base quedó programado y el monitor de disponibilidad está activo | ☐ | |

Resultado: ☐ Liberación confirmada · ☐ Reversión ejecutada (motivo):

| Rol | Nombre | Firma |
|---|---|---|
| Líder de QA | Yanimar Burbano Piamba | |
| QA no funcional y seguridad | Francisco José Castro Muñoz | |
| Administrador del ambiente | Daniel Montenegro Almendra | |
