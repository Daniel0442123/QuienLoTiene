# Quién lo tiene · Notas de levantamiento (Fase 1)

Fuentes: idea-negocio.pdf (Byte Force, Ingeniería del Software 3) y documento de arquitectura
(https://claude.ai/artifact/685QgQCQEEASFZfMkQSApV).

## Respuestas de Pedro
- Equipo: 7 personas. Roles pendientes (Pedro los enviará).
- Fecha de entrega final: 26/11/2026 (aprox. 4,5 semanas desde el 26/10/2026).
- Entregable del 26/11: app funcional + documentación completa + batería de pruebas, todo funcionando.
- Uso: demo con datos de prueba ante el profesor (no piloto con tiendas reales por ahora).
- Producción: Hostinger KVM 1, COP 57.900 (el plan de menor costo). Verificar especificaciones exactas y si el precio es mensual con permanencia.
- El profesor exige diagramas UML: casos de uso, clases, secuencia, despliegue y otros ("etc."). Pedir la lista completa o la rúbrica.
- Ley 1581: por ahora basta documentar la política de tratamiento de datos y la casilla de autorización al registrarse (supresión de cuenta queda fuera del MVP).
- Monetización: gratis al inicio. Si la idea funciona, plan de pago para empresas/tiendas (futuro). Pagos dentro de la app y plan de pago quedan FUERA de esta entrega.
- Aclaración: solo el cobro dentro de la app sale de la entrega. Se mantiene que la tienda configure métodos aceptados y que el cliente elija uno al confirmar (solo se registra).
- Autenticación: opción 1, frase de contraseña de mínimo 15 caracteres (NIST 800-63B). Admin con TOTP. Login sin contraseña por código al correo = mejora futura.
- Chat: botón a WhatsApp (Must) + mini chat interno de solo texto ligado a la orden (Should), sin adjuntos, sin "leído", actualización por SSE ya existente.
- Valores aprobados (Fase 9): solicitud vence a los 30 min; tienda puede ofrecer menos unidades; radio 3→5 km a los 10 min sin respuestas (una vez); máx. 10 solicitudes/hora por cliente; metas piloto: 70 % con respuesta y 10 min de respuesta media.

## Entregables
- SRS completo (21 secciones + anexos): https://claude.ai/artifact/QgeXSyJwMLhgsNJNmU3V2T
- Diagramas .drawio: /mnt/project-files/requisitos/diagramas/ (00 a 09)
- Pendiente de Pedro: roles de las 7 personas y rúbrica del profesor.
