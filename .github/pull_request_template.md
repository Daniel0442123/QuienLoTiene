## Historia

<!-- HU-xxx y enlace a la tarjeta del tablero -->

## Qué cambia

<!-- Dos o tres frases: qué hace este pull request y por qué -->

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

Aprobación requerida: arquitecto de software.
