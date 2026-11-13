## Historia

<!-- HU-xxx o tarea T-xx, y enlace a la tarjeta del tablero. Si no hay historia, diga el motivo (defecto DEF-nnn, alerta de seguridad, despliegue…) -->

## Qué cambia

<!-- Dos o tres frases: qué hace este pull request y por qué -->

## Control de cambios

<!-- Marque exactamente una opción. Se desvía del plan cuando cambia algo ya aprobado: alcance o criterios de una HU, una decisión D-xx/F-xx/DA-xx, el contrato de la API o un código de error, el modelo de datos frente al diagrama 09, el cronograma o el sprint de una historia, una dependencia o herramienta no prevista, o la infraestructura. Procedimiento: docs/gestion/CONTROL-DE-CAMBIOS.md -->

- [ ] No se desvía del plan
- [ ] Se desvía del plan: solicitud de cambio SC-___ registrada en `docs/gestion/CONTROL-DE-CAMBIOS.md` (estado y aprobador)

## Documentos actualizados

<!-- Liste cada documento, plantilla, diagrama o Word/Excel que cambió con su nueva versión (por ejemplo «DECISIONES-BACKEND.md 1.2»), o escriba «Ninguno». Si un documento afectado no se actualiza aquí, deje la acción pendiente en la bitácora del proyecto. -->

- 

## Pruebas

- [ ] Pruebas unitarias nuevas o ajustadas, con `@Tag("HU-xxx")` y `@Tag("CP-xxx")`
- [ ] `./mvnw verify` en verde en mi equipo
- [ ] Cobertura ≥ 70 % en `domain` y `application` del módulo
- [ ] Resultado anotado en el libro de control (`docs/pruebas/libro/`)

## Lista de revisión (DECISIONES-BACKEND)

- [ ] Nombres descriptivos en español y términos del glosario (secciones 2 y 3)
- [ ] El dominio no importa Spring, JPA ni Lombok; los controladores solo llaman casos de uso
- [ ] Ningún módulo usa clases internas de otro (solo `api/` o eventos)
- [ ] Migraciones nuevas con versión `V<AAAAMMDDHHmm>__<modulo>_<descripcion>.sql`; ninguna migración publicada fue modificada
- [ ] Sin procedimientos almacenados; SQL nativo solo para PostGIS, `SKIP LOCKED` o `pg_trgm`, siempre con parámetros
- [ ] Sin secretos ni datos personales en el código, los logs o las pruebas
- [ ] `./mvnw spotless:apply` ejecutado
- [ ] Códigos de error nuevos agregados a `docs/api/CODIGOS-DE-ERROR.md`

## Trazabilidad

- [ ] Encabezado e «Historial del documento» actualizados en cada documento que cambió (`docs/gestion/PROCEDIMIENTO-DOCUMENTAL.md`)
- [ ] Si quedó algo pendiente, está en «Compromisos abiertos» de `docs/gestion/BITACORA-DEL-PROYECTO.md`
- [ ] Si el PR usó asistencia de IA, lo dice al final de esta descripción

Aprobación requerida: arquitecto de software, registrada como revisión «Approve» en GitHub (no basta con fusionar).
