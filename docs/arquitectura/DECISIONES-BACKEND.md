# Decisiones técnicas y convenciones del backend · Quién lo tiene

Versión 1.1 (aprobada por Pedro) · 07/11/2026

Este documento reúne las decisiones que el equipo necesita tener claras para que todos escriban el backend de la misma forma. Todas las decisiones de la tabla están aprobadas; las que vienen del documento de arquitectura se marcan como tales. Lo poco que falta está en la sección 20.

Documentos relacionados: `docs/arquitectura/Arquitectura-de-software-QuienLoTiene` (registro DA-01 a DA-11, que manda sobre este documento), `docs/ESTRUCTURA.md`, `docs/pruebas/ESTRATEGIA-DE-PRUEBAS.md`, `docs/diagramas/` (en especial `07-componentes`, `09-entidad-relacion` y las secuencias por caso de uso).

---

## 1. Resumen de decisiones

| # | Tema | Decisión | Estado |
|---|---|---|---|
| D-01 | Lenguaje y plataforma | Java 21, Spring Boot 4.1.2, Maven con `./mvnw` | Decidida |
| D-02 | Estilo arquitectónico | Monolito modular (Spring Modulith) con Clean Architecture dentro de cada módulo | Decidida |
| D-03 | Idioma del código | Conceptos del negocio en español; sufijos técnicos de patrones en inglés (`Controller`, `Repository`, `Test`) | Decidida (Pedro, 07/11) |
| D-04 | Nombres nemotécnicos | Nombres descriptivos que digan qué representa la variable, sin prefijos de tipo | Decidida (Pedro, 07/11) |
| D-05 | Paquete base | `com.quienlotiene`, `groupId` `com.quienlotiene` | Decidida (aplicada) |
| D-06 | Comunicación entre módulos | Solo por la fachada `api/` del otro módulo o por eventos de Spring Modulith | Decidida |
| D-07 | DTOs | `record` de Java, uno de entrada y uno de salida por caso de uso | Decidida |
| D-08 | Mapeo | MapStruct, una interfaz `*Mapper` por módulo y capa | Decidida (arquitectura) |
| D-09 | Errores | `ProblemDetail` (RFC 9457) con un código de error propio y estable | Decidida |
| D-10 | Validación | Bean Validation en los DTOs de entrada; reglas de negocio dentro del dominio | Decidida |
| D-11 | Base de datos | PostgreSQL 17 + PostGIS 3.5, un esquema por módulo, `ddl-auto=validate` | Decidida |
| D-12 | Migraciones | Flyway, versión con fecha y hora: `V202611081030__identidad_crear_tabla_usuario.sql` | Decidida |
| D-13 | Identificadores | `UUID` generado por la aplicación | Decidida |
| D-14 | Fechas y dinero | Guardar en UTC (`timestamptz`, `Instant`); mostrar en `America/Bogota`. Dinero en pesos enteros (`long`) | Decidida |
| D-15 | Autenticación | JWT de 15 min + token de refresco opaco y rotativo en cookie HttpOnly, frase ≥ 15 caracteres con Argon2id, TOTP para el administrador (DA-10) | Decidida |
| D-16 | API REST | `/api/v1`, recursos en español, plural y kebab-case, JSON en camelCase | Decidida |
| D-17 | Documentación de la API | OpenAPI con springdoc, publicada en `/swagger-ui` solo fuera de producción | Decidida |
| D-18 | Tiempo real | SSE (`/api/v1/eventos`) para la app y Web Push para la tienda | Decidida |
| D-19 | Tareas programadas | `@Scheduled` con `FOR UPDATE SKIP LOCKED` y un `Clock` inyectado para poder probar los vencimientos | Decidida |
| D-20 | Registros (logs) | SLF4J, logs JSON en producción, mensajes en español, sin datos personales | Decidida (detalle propuesto) |
| D-21 | Pruebas | JUnit 5 + Mockito + AssertJ, `*Test` unitarias y `*IT` integración, JaCoCo ≥ 70 % | Decidida |
| D-22 | Flujo de Git | Ramas cortas desde `main`, Conventional Commits en español, pull request aprobado por el arquitecto de software y CI en verde | Decidida |
| D-23 | Acceso a datos | Sin procedimientos almacenados: Spring Data JPA y consultas nativas parametrizadas solo para PostGIS, `SKIP LOCKED` y `pg_trgm` | Decidida (Pedro, 07/11) |

---

## 2. Idioma del código (D-03)

El dominio del negocio está en español (solicitud, tienda, orden, reseña), los requisitos, las historias y los diagramas también. Escribir el código en español evita traducir a cada rato y hace que el código se lea igual que los requisitos.

Regla:

1. **Todo lo que es del negocio va en español**: clases, atributos, métodos, variables, tablas, columnas, rutas de la API y campos JSON.
2. **Los sufijos de patrones técnicos van en inglés**, porque son los nombres que usa Spring y la literatura: `Controller`, `Repository`, `Entity` (JPA), `Mapper`, `Config`, `Exception`, `Test`, `IT`.
3. **No se mezclan idiomas dentro de una misma palabra del negocio.** Se escribe `calcularTarifaDomicilio`, no `calculateTarifaDomicilio` ni `calcularDeliveryFee`.
4. **Sin tildes ni eñes en identificadores** (Java las permite, pero dan problemas en Windows, Git y en la base de datos): `anio`, `numeroDeTelefono`, `resena`, `direccion`. En los textos para el usuario sí se usan tildes.

Ejemplos de la mezcla correcta: `SolicitudController`, `SolicitudRepository`, `SolicitudEntity`, `PublicarSolicitud`, `SolicitudVencidaEvent`, `PublicarSolicitudTest`.

---

## 3. Nombres nemotécnicos (D-04)

"Nemotécnico" aquí significa que **el nombre recuerda por sí solo qué representa la cosa y para qué sirve**, sin tener que leer el código alrededor. Esta es la regla más importante del documento: si un nombre necesita un comentario para entenderse, el nombre está mal.

### 3.1 Reglas generales

| Regla | Mal | Bien |
|---|---|---|
| El nombre dice qué es, no qué tipo tiene | `str`, `lista`, `obj`, `dato` | `nombreTienda`, `respuestasRecibidas` |
| Sin abreviaturas inventadas | `cantSol`, `tndAbr`, `fecVen` | `cantidadSolicitudes`, `tiendasAbiertas`, `fechaVencimiento` |
| Incluye la unidad cuando es un número con unidad | `radio`, `tiempo`, `tarifa` | `radioEnMetros`, `minutosParaVencer`, `tarifaEnPesos` |
| Colecciones en plural | `respuesta` (si es una lista) | `respuestas` |
| Booleanos como pregunta de sí o no | `abierta`, `flag`, `estado` | `estaAbierta`, `tieneDomicilio`, `puedeCalificar`, `fueAmpliado` |
| Sin números mágicos: constantes con nombre | `if (minutos > 30)` | `if (minutos > MINUTOS_VIGENCIA_SOLICITUD)` |
| Una sola palabra por concepto en todo el proyecto | `cliente`, `usuario`, `comprador` para lo mismo | usar siempre la del glosario (sección 3.4) |
| Nombres de una letra solo en lambdas cortas e índices | `for (Orden o : x)` | `for (Orden orden : ordenesActivas)` |

### 3.2 Por tipo de elemento

| Elemento | Forma | Ejemplos |
|---|---|---|
| Paquete | minúsculas, una palabra | `solicitudes`, `infrastructure.persistence` |
| Clase de dominio | sustantivo en singular, `PascalCase` | `Solicitud`, `Tienda`, `RespuestaSolicitud`, `HorarioAtencion` |
| Objeto de valor | sustantivo del concepto | `Correo`, `FraseContrasena`, `Ubicacion`, `Dinero`, `RadioBusqueda` |
| Enum | sustantivo singular; valores en `MAYUSCULAS_CON_GUION_BAJO` | `EstadoOrden.EN_CAMINO`, `TipoRespuesta.PARECIDO` |
| Caso de uso | verbo en infinitivo + objeto (la acción del usuario) | `PublicarSolicitud`, `ResponderSolicitud`, `CancelarOrden`, `AprobarTienda` |
| Método del caso de uso | `ejecutar(...)` | `publicarSolicitud.ejecutar(comando)` |
| Puerto (interfaz) | lo que ofrece, sin prefijo `I` | `SolicitudRepository`, `ServicioCorreo`, `AlmacenArchivos` |
| Adaptador | tecnología + puerto | `JpaSolicitudRepository`, `SmtpServicioCorreo` |
| Controlador | recurso + `Controller` | `SolicitudController`, `OrdenController` |
| DTO de entrada | acción + `Request` o `Comando` (ver 6) | `PublicarSolicitudRequest` |
| DTO de salida | recurso + `Response` | `SolicitudResponse`, `TiendaDetalleResponse` |
| Evento de dominio | hecho en pasado + `Event` | `SolicitudPublicadaEvent`, `OrdenEntregadaEvent` |
| Excepción | lo que pasó + `Exception` | `LimiteSolicitudesSuperadoException`, `TiendaCerradaException` |
| Método que consulta | `obtener`, `buscar`, `listar`, `contar`, `existe` | `buscarPorId`, `listarAbiertasDentroDe`, `contarPublicadasDesde` |
| Método que cambia el estado | verbo del negocio, no `set` | `orden.marcarEnCamino()`, `tienda.abrir()`, `solicitud.ampliarRadio()` |
| Método booleano | `es`, `esta`, `tiene`, `puede` | `estaVencida()`, `puedeSerCancelada()` |
| Constante | `MAYUSCULAS_CON_GUION_BAJO` con unidad | `MAXIMO_SOLICITUDES_POR_HORA = 10`, `RADIO_INICIAL_METROS = 3_000` |
| Variable y atributo | `camelCase`, sustantivo | `fechaPublicacion`, `tiendaElegida` |
| Prueba | lo que debe pasar, en `camelCase` | `rechazaSolicitudCuandoSuperaElLimitePorHora` |
| Tabla y columna | `snake_case`, singular | `solicitud`, `respuesta_solicitud`, `fecha_vencimiento` |
| Ruta REST | plural, `kebab-case` | `/api/v1/tiendas/me/medios-pago` |
| Campo JSON | `camelCase` igual que el DTO | `"tarifaEnPesos": 4000` |

### 3.3 Ejemplo completo

```java
public class Solicitud {

    private static final int MAXIMO_SOLICITUDES_POR_HORA = 10;
    private static final Duration VIGENCIA = Duration.ofMinutes(30);
    private static final int RADIO_AMPLIADO_METROS = 5_000;

    private final UUID id;
    private final UUID clienteId;
    private final String descripcionProducto;
    private final Instant fechaPublicacion;
    private RadioBusqueda radioBusqueda;
    private EstadoSolicitud estado;
    private boolean fueAmpliado;

    public boolean estaVencida(Instant ahora) {
        return ahora.isAfter(fechaPublicacion.plus(VIGENCIA));
    }

    public void ampliarRadio() {
        if (fueAmpliado) {
            throw new RadioYaAmpliadoException(id);
        }
        this.radioBusqueda = RadioBusqueda.deMetros(RADIO_AMPLIADO_METROS);
        this.fueAmpliado = true;
    }
}
```

### 3.4 Glosario obligatorio

Un concepto, una palabra. Si aparece un término nuevo, se agrega aquí en el mismo pull request.

| Término en el código | Significa | No usar |
|---|---|---|
| `usuario` | Cuenta que inicia sesión (cliente, tienda o administrador) | cuenta, user |
| `cliente` | Persona que publica solicitudes | comprador, consumidor |
| `tienda` | Negocio que responde solicitudes | comercio, vendedor, local |
| `administrador` | Usuario con rol de administración | admin (solo como valor del rol `ADMIN`) |
| `solicitud` | Lo que el cliente publica: "¿quién tiene X?" | pedido, búsqueda, request |
| `respuesta` | Lo que la tienda contesta: `SI`, `PARECIDO`, `NO` | oferta, propuesta |
| `orden` | Lo que nace cuando el cliente elige una respuesta | pedido, compra |
| `reserva` | Orden para recoger en la tienda | apartado |
| `domicilio` | Entrega a la dirección del cliente | envío, delivery |
| `resena` | Calificación del cliente a la tienda | review, valoración |
| `reporte` | Problema reportado al administrador | queja, denuncia |
| `radioBusqueda` | Distancia de difusión de una solicitud | rango, alcance |
| `medioPago` | Forma de pago aceptada (solo se registra) | método de pago, payment |

Cuando el glosario del SRS use otra palabra, manda el SRS y se corrige esta tabla.

---

## 4. Estructura de paquetes (D-05, D-06)

### 4.1 Paquete base

El paquete base es `com.quienlotiene` y el `groupId` del `pom.xml` es `com.quienlotiene` (ya aplicado; se dejó atrás el `com.example` de Spring Initializr). Todo el código nuevo va debajo de `com.quienlotiene.<modulo>`.

### 4.2 Módulos y capas

Se mantiene lo de `ESTRUCTURA.md`: los módulos `identidad`, `clientes`, `tiendas`, `catalogo`, `solicitudes`, `ordenes`, `mensajeria`, `reputacion`, `notificaciones`, `administracion` y el núcleo `shared`, cada uno con `api/`, `domain/`, `application/` e `infrastructure/`.

Reglas que no se negocian (las verifica la prueba de arquitectura en cada pull request):

1. `domain` no importa nada de Spring, JPA, Jackson ni de `infrastructure`. Solo Java y `shared.domain`.
2. Un módulo **nunca** importa clases de `domain`, `application` o `infrastructure` de otro módulo. Usa la fachada `api/` del otro módulo (consulta síncrona) o escucha sus eventos (reacción asíncrona).
3. Los controladores solo llaman casos de uso. No tienen lógica de negocio ni llaman repositorios.
4. Un caso de uso por acción del usuario. Si un caso de uso necesita otro, se extrae la regla al dominio.

### 4.3 Eventos entre módulos

- Los eventos que salen del módulo se declaran en `api/` (son parte del contrato público) como `record` inmutables con datos simples (ids, valores), nunca entidades.
- Se escuchan con `@ApplicationModuleListener` de Spring Modulith.
- **Decidido**: agregar `spring-modulith-starter-jpa` para que los eventos queden registrados en la base de datos y no se pierdan si la app se reinicia a mitad del proceso.

---

## 5. Dominio

- Las **entidades de dominio** son clases Java simples, con constructor o fábrica estática (`Solicitud.publicar(...)`) que valida que el objeto nazca correcto. No tienen setters públicos; el estado cambia con métodos del negocio (`orden.cancelar(motivo, ahora)`).
- Las **entidades JPA** viven aparte, en `infrastructure/persistence` (`SolicitudEntity`), y el adaptador las convierte al modelo de dominio. Cuesta un poco más de código, pero es lo que permite probar el dominio sin base de datos, como pide la estrategia de pruebas.
- Los **objetos de valor** se escriben como `record` con validación en el constructor compacto: `Correo`, `FraseContrasena`, `Ubicacion`, `Dinero`.
- Las **máquinas de estado** (orden, solicitud, tienda) son un `enum` con las transiciones permitidas, y la entidad lanza una excepción si la transición no es válida.
- La hora actual nunca se pide con `Instant.now()` dentro del dominio: se recibe como parámetro (`ahora`) o se inyecta un `Clock`. Así las pruebas de vencimiento son exactas.

### Uso de Lombok

Lombok está en el `pom.xml`. Para que no oculte errores:

- **Sí** en `infrastructure` (entidades JPA, configuración): `@Getter`, `@RequiredArgsConstructor`, `@Slf4j`.
- **No** `@Data` ni `@Setter` en ninguna parte, y **no** Lombok en `domain`.
- Para DTOs y objetos de valor se usan `record` de Java, que no necesitan Lombok.

---

## 6. DTOs y mapeo (D-07, D-08)

- DTO de entrada desde la API: `<Accion><Recurso>Request` (por ejemplo `PublicarSolicitudRequest`), con las anotaciones de validación.
- DTO de salida hacia la API: `<Recurso>Response` o `<Recurso><Vista>Response` (`TiendaDetalleResponse`, `TiendaResumenResponse`).
- Entre el controlador y el caso de uso viaja un `<Accion><Recurso>Comando` (escritura) o una `<Recurso>Consulta` (lectura) que vive en `application/dto`. El caso de uso no conoce los `Request` HTTP.
- Nunca se devuelve una entidad de dominio ni una entidad JPA en la respuesta HTTP.
- El mapeo se hace con MapStruct (decidido en el documento de arquitectura): interfaces `@Mapper(componentModel = "spring")` como `SolicitudWebMapper` (Request/Response ↔ comando/dominio) y `SolicitudPersistenceMapper` (dominio ↔ `SolicitudEntity`). Los métodos se llaman `aDominio`, `aEntity`, `aResponse`, `aComando`. Se configura `unmappedTargetPolicy = ReportingPolicy.ERROR` para que un campo olvidado rompa la compilación y no llegue en blanco a producción.
- MapStruct necesita agregarse al `pom.xml` (dependencia y procesador de anotaciones junto a Lombok, con `lombok-mapstruct-binding`).

---

## 7. Convenciones de la API REST (D-16, D-17)

Ya decidido en los diagramas de secuencia: prefijo `/api/v1`, JSON y JWT. Reglas para todos los controladores:

| Tema | Regla | Ejemplo |
|---|---|---|
| Recursos | sustantivos en español, plural, `kebab-case` | `/api/v1/solicitudes`, `/api/v1/tiendas/me/medios-pago` |
| El usuario actual | `me` en la ruta | `GET /api/v1/clientes/me/direcciones` |
| Acciones que no son CRUD | subrecurso con sustantivo | `POST /api/v1/ordenes/{id}/cancelacion`, `POST /api/v1/ordenes/{id}/recepcion` |
| Crear | `POST` → `201 Created` con cabecera `Location` y el recurso | |
| Reemplazar | `PUT` → `200 OK` | `PUT /api/v1/tiendas/me/perfil` |
| Cambiar una parte | `PATCH` → `200 OK` | `PATCH /api/v1/ordenes/{id}/estado` |
| Borrar | `DELETE` → `204 No Content` | |
| Paginación | `?page=0&size=20&sort=fechaPublicacion,desc`; respuesta con `contenido`, `pagina`, `tamano`, `totalElementos`, `totalPaginas` | |
| Ordenamiento propio | parámetro con nombre del negocio | `?orden=precio` |
| Fechas | ISO-8601 en UTC con `Z` | `"fechaPublicacion": "2026-11-08T15:30:00Z"` |
| Dinero | entero en pesos colombianos, con el nombre de la moneda | `"tarifaEnPesos": 4000` |
| Coordenadas | `latitud` y `longitud` en grados decimales | `{"latitud": 2.4448, "longitud": -76.6147}` |
| Nulos | los campos sin valor no se envían | |
| Archivos | `multipart/form-data` solo en el registro de tienda | `POST /api/v1/tiendas/registro` |

Códigos de estado que usa la API: `200`, `201`, `204`, `400` (datos inválidos), `401` (sin sesión), `403` (sin permiso), `404` (no existe o no es tuyo), `409` (conflicto de estado, por ejemplo cancelar una orden entregada), `422` (regla de negocio, por ejemplo el límite de solicitudes por hora), `429` (demasiados intentos de inicio de sesión), `500`.

Documentación: **propuesta** agregar `springdoc-openapi-starter-webmvc-ui` (la versión 3.x es la que soporta Spring Boot 4; confirmar la versión exacta al agregarla). Cada controlador lleva `@Tag` y cada operación `@Operation` en español. Swagger UI queda habilitado en `dev` y deshabilitado en `prod`.

---

## 8. Manejo de errores (D-09)

- Todas las respuestas de error usan `ProblemDetail` (RFC 9457), que Spring ya trae:

```json
{
  "type": "https://quienlotiene.app/errores/limite-solicitudes-superado",
  "title": "Límite de solicitudes superado",
  "status": 422,
  "detail": "Puedes publicar hasta 10 solicitudes por hora. Intenta de nuevo en 12 minutos.",
  "instance": "/api/v1/solicitudes",
  "codigo": "SOL-001",
  "errores": []
}
```

- Cada módulo tiene sus códigos con prefijo: `IDE-` identidad, `CLI-` clientes, `TIE-` tiendas, `CAT-` catálogo, `SOL-` solicitudes, `ORD-` órdenes, `MEN-` mensajería, `REP-` reputación, `NOT-` notificaciones, `ADM-` administración, `GEN-` genéricos. El código no cambia nunca; el frontend y las pruebas se apoyan en él. Se listan en `docs/api/CODIGOS-DE-ERROR.md`.
- El dominio lanza excepciones propias que heredan de `ReglaNegocioException` (en `shared.domain`), con su código. Un único `ManejadorGlobalErrores` (`@RestControllerAdvice` en `shared/infrastructure/web`) las convierte en `ProblemDetail`.
- Los errores de validación devuelven `400` con la lista `errores: [{ "campo": "descripcionProducto", "mensaje": "..." }]`.
- Nunca se envía la traza del error al cliente. En inicio de sesión el mensaje no dice si falló el correo o la frase (como ya indica el diagrama 11).
- Si el recurso existe pero es de otro usuario se responde `404`, no `403`, para no revelar que existe (riesgo R-03 de autorización por objeto).

---

## 9. Validación (D-10)

Dos niveles, sin duplicar:

1. **Forma de los datos** en el `Request` con Bean Validation (`@NotBlank`, `@Size`, `@Email`, `@Positive`) y `@Valid` en el controlador. Los mensajes van en español en `ValidationMessages.properties`.
2. **Reglas del negocio** en el dominio y en el caso de uso: frase de contraseña ≥ 15 caracteres, máximo 10 solicitudes por hora, solo tiendas aprobadas y abiertas, calificar solo órdenes entregadas, etc. Estas reglas son las que llevan prueba unitaria obligatoria.

---

## 10. Persistencia y migraciones (D-11, D-12, D-13, D-14)

- Un esquema de PostgreSQL por módulo, con el mismo nombre del módulo (`identidad`, `solicitudes`, ...). Ningún módulo hace `JOIN` contra tablas de otro esquema: si necesita un dato, lo pide por la fachada `api/` o guarda su propia copia a partir de un evento.
- `spring.jpa.hibernate.ddl-auto=validate` en todos los perfiles. La base solo se cambia con Flyway.
- `spring.jpa.open-in-view=false`.
- Tablas y columnas en `snake_case` y singular. Toda tabla tiene `id uuid primary key`, `creado_en timestamptz not null` y `actualizado_en timestamptz not null`. Las tablas que se editan en paralelo llevan `version bigint` para bloqueo optimista (`@Version`), en especial `orden` y `solicitud`.
- Identificadores `UUID` generados por la aplicación al crear el objeto de dominio (así el dominio tiene id antes de guardarse y los ids no se pueden adivinar en las URL).
- Ubicaciones como `geography(Point, 4326)` con índice GiST; las consultas por radio usan `ST_DWithin` en metros.
- Fechas en `timestamptz` y `Instant` en Java, siempre en UTC. La zona `America/Bogota` solo se usa para mostrar y para el horario de atención de las tiendas (`LocalTime` + zona).
- Dinero en `bigint` (pesos enteros, el peso colombiano no usa centavos en la práctica).
- **Migraciones**: como Flyway lleva una sola tabla de historial para todas las carpetas, dos personas podrían crear la misma `V3` en módulos distintos. Para evitarlo la versión es la fecha y hora: `V<AAAAMMDDHHmm>__<modulo>_<descripcion>.sql`, por ejemplo `V202611081030__solicitudes_crear_tabla_solicitud.sql`. Una migración publicada en `main` no se modifica nunca; se corrige con otra nueva.
- Datos de prueba para la demo: migraciones `R__datos_demo_<modulo>.sql` en una carpeta `db/demo` que solo se carga con el perfil `demo`.

### 10.1 Acceso a datos sin procedimientos almacenados (D-23)

- **No se usan procedimientos almacenados ni funciones con lógica en PL/pgSQL.** Las reglas de negocio viven solo en `domain` (DA-02), donde se prueban con pruebas unitarias rápidas (estrategia de pruebas, meta JaCoCo ≥ 70 %). Con el tiempo y el tamaño del equipo, otro lenguaje que versionar y depurar no compensa.
- **Spring Data JPA** para el CRUD normal, desde los adaptadores de `infrastructure/persistence`.
- **Consultas nativas** (`@Query(nativeQuery = true)` o `JdbcClient`) solo donde se necesita SQL propio de PostgreSQL: búsqueda de tiendas por radio con `ST_DWithin`, toma de filas con `FOR UPDATE SKIP LOCKED` en las tareas programadas y búsqueda de texto con `pg_trgm` y `unaccent`. Cada una lleva prueba de integración (`*IT`) con Testcontainers.
- **Siempre con parámetros** (`:radioEnMetros`), nunca SQL armado concatenando texto.
- **Excepción permitida**: una **vista** de solo lectura para consultas pesadas de indicadores (CU-23), creada con Flyway y sin reglas de negocio. Cualquier otra excepción la aprueba el arquitecto de software.

---

## 11. Seguridad (D-15)

- **Sesión sin estado con JWT propio** (DA-10): la API firma el token de acceso de 15 minutos y lo valida con el resource server de Spring Security (dependencia que ya está en el `pom.xml`). El token de refresco es opaco y rotativo, se guarda como hash en la tabla `token_refresco` y viaja en una cookie `HttpOnly`, `Secure` y `SameSite=Strict`; cerrar sesión lo revoca. **Decidido**: firmar con RS256, llaves leídas de variables de entorno y con identificador (`kid`) para poder rotarlas.
- **Frases de contraseña**: mínimo 15 caracteres, sin reglas de composición, verificadas contra una lista de contraseñas comunes (NIST 800-63B). Se guardan con Argon2id (`Argon2PasswordEncoder`, que necesita la dependencia de Bouncy Castle).
- **Administrador con TOTP**: librería `dev.samstevens.totp`. El secreto se guarda cifrado.
- **Roles**: `CLIENTE`, `TIENDA`, `ADMIN` (`hasRole(...)` en la configuración y `@PreAuthorize` en los casos que lo necesiten).
- **Autorización por objeto**: todo caso de uso que recibe un id comprueba que el recurso pertenezca al usuario actual. Es el riesgo R-03 y tiene pruebas específicas.
- Límites de uso con Bucket4j y Caffeine en memoria: intentos de inicio de sesión (`429`), 10 solicitudes por hora por cliente (`422`, código `SOL-001`) y respuestas por tienda.
- CORS solo para el dominio del frontend. HTTPS lo da Caddy.
- Ningún secreto en el repositorio: todo sale de variables de entorno (`QLT_JWT_LLAVE_PRIVADA`, `QLT_DB_CLAVE`, ...). Hay un `.env.ejemplo` sin valores reales.
- **Decidido**: quitar `spring-boot-starter-security-oauth2-client` si no se va a ofrecer inicio de sesión con Google u otro proveedor (no está en el alcance del MVP).

---

## 12. Tiempo real, tareas programadas y archivos (D-18, D-19)

- **SSE**: `GET /api/v1/eventos` con `SseEmitter`, una conexión por usuario, latido cada 25 segundos para que Caddy no corte la conexión. El módulo `notificaciones` escucha los eventos de los otros módulos y los envía.
- **Web Push** para la tienda (librería `nl.martijndwars:web-push` o equivalente, a confirmar al implementarlo).
- **Tareas del reloj del sistema** (CU-29, CU-30, CU-31): `@Scheduled(fixedDelay = ...)` cada minuto, cada una en su módulo. Las filas a procesar se toman con `SELECT ... FOR UPDATE SKIP LOCKED` (como dice el documento de arquitectura), así dos ejecuciones nunca procesan la misma solicitud aunque se solapen. Todas reciben un `Clock` inyectado.
- **Archivos** (documentos de la tienda): en disco del VPS detrás del puerto `AlmacenArchivos` (`shared/infrastructure/storage`), con nombre generado (UUID), tipo real validado por contenido, tamaño máximo y sin metadatos EXIF en las fotos. Nunca se sirven por una ruta pública; solo el administrador los descarga por la API.

---

## 13. Configuración y perfiles

- `application.yml` (común) más `application-dev.yml`, `application-test.yml`, `application-demo.yml` y `application-prod.yml`.
- Las propiedades propias van bajo el prefijo `quienlotiene.` en clases `@ConfigurationProperties` validadas, no con `@Value` sueltos. Ejemplo: `quienlotiene.solicitudes.minutos-vigencia=30`, `quienlotiene.solicitudes.maximo-por-hora=10`.
- Los valores aprobados en la Fase 9 (30 min, 3 → 5 km, 10 min, 10 por hora) quedan en configuración con esos valores por defecto, para poder cambiarlos en la demo sin recompilar.
- **Decidido**: en `compose.yaml` cambiar `mydatabase`, `myuser` y `secret` por `quienlotiene` y variables de entorno.

---

## 14. Registros (logs) (D-20)

- `@Slf4j` (SLF4J). Mensajes en español, con parámetros: `log.info("Solicitud {} publicada por el cliente {}", solicitudId, clienteId)`.
- Formato JSON en el perfil `prod` (salida estructurada de Spring Boot) y texto legible en `dev`.
- Las aprobaciones, suspensiones y accesos a datos de clientes se registran además como auditoría.
- Niveles: `ERROR` algo falló y hay que revisarlo, `WARN` algo raro pero controlado, `INFO` hechos del negocio (publicada, vencida, aprobada), `DEBUG` detalle técnico (apagado en producción).
- **Nunca** se registran frases de contraseña, tokens, secretos TOTP, correos completos, teléfonos ni direcciones (Ley 1581). Se registran ids.
- Un id de correlación por petición (filtro en `shared/infrastructure/web` que lo pone en el MDC y lo devuelve en la cabecera `X-Correlacion-Id`).

---

## 15. Pruebas (D-21)

Ya está decidido en `docs/pruebas/ESTRATEGIA-DE-PRUEBAS.md`. Lo que este documento agrega para el código:

- Clase de prueba: `<ClaseProbada>Test` (unitaria) o `<ClaseProbada>IT` (integración), en el mismo paquete que la clase probada.
- Método de prueba: describe el comportamiento esperado, en español y `camelCase`, sin la palabra `test`: `rechazaSolicitudCuandoSuperaElLimitePorHora`, `ampliaElRadioUnaSolaVez`.
- Cuerpo en tres bloques comentados: `// Dado`, `// Cuando`, `// Entonces`.
- `@DisplayName` con el código del caso de prueba y `@Tag("HU-xxx")`, `@Tag("CP-xxx")`.
- Datos de prueba con fábricas legibles en `src/test/.../<modulo>/fixtures` (`SolicitudFixture.unaSolicitudPublicada()`), no objetos armados a mano en cada prueba.

Dependencias que faltan en el `pom.xml` para cumplir la estrategia (ver sección 19): JaCoCo, ArchUnit, Testcontainers y Failsafe.

---

## 16. Estilo de código

- Sangría de 4 espacios, líneas de máximo 120 caracteres, finales de línea LF. Se fija en un `.editorconfig` en la raíz para que IntelliJ, VS Code y Windows se comporten igual.
- **Decidido**: formateo automático con el plugin Spotless (`palantir-java-format`) y `./mvnw spotless:apply` antes de cada commit; CI falla si el código no está formateado.
- Importaciones explícitas, sin comodines (`import java.util.*`).
- Javadoc corto en español en los puertos, las fachadas `api/` y los casos de uso. En el resto, el nombre debe bastar (sección 3).
- Comentarios solo para explicar **por qué**, no **qué**.
- Sin `System.out.println`, sin código comentado y sin `TODO` sin número de historia (`// TODO HU-022: ...`).
- `Optional` solo como tipo de retorno, nunca como parámetro ni atributo.
- Inyección por constructor (con `final`), nunca `@Autowired` en atributos.

---

## 17. Flujo de trabajo con Git (D-22)

- Rama principal `main`, siempre desplegable y protegida (no se hace push directo).
- Ramas cortas desde `main`, con la historia en el nombre: `feature/HU-016-limite-solicitudes`, `fix/HU-027-estado-orden`, `docs/convenciones-backend`, `test/HU-021-vencimiento`.
- Commits con **Conventional Commits en español**: `feat(solicitudes): limita a 10 solicitudes por hora`, `fix(ordenes): impide cancelar una orden entregada`, `test(identidad): ...`, `docs: ...`, `refactor: ...`, `chore: ...`.
- Pull request pequeño (idealmente menos de 400 líneas), con la plantilla del repositorio, aprobado por el **arquitecto de software** (único aprobador de los pull requests del backend), CI en verde (compilación, unitarias, arquitectura, cobertura ≥ 70 % y formato) y fusión con *squash*.
- Versiones con SemVer y etiqueta en Git: `v0.1.0` al final de cada sprint, `v1.0.0` el 26/11.

---

## 18. Integración continua

Un flujo de GitHub Actions en `.github/workflows/ci.yml` que corre en cada pull request:

1. `./mvnw -B verify` (compila, unitarias, arquitectura, integración con Testcontainers, JaCoCo con umbral 70 %).
2. Revisión de formato (Spotless).
3. Escaneo de seguridad: Trivy, OWASP Dependency-Check, SpotBugs con FindSecBugs y SBOM (CycloneDX), como ya dicen la arquitectura y la estrategia de pruebas.

---

## 19. Cambios propuestos al `pom.xml` y al proyecto

| Cambio | Motivo |
|---|---|
| `groupId` y paquete a `com.quienlotiene` | Coincidir con `ESTRUCTURA.md` y la estrategia de pruebas |
| Quitar `native-maven-plugin` (GraalVM) | No se va a compilar a imagen nativa; solo alarga el build y confunde |
| Quitar `spring-boot-starter-security-oauth2-client` y su starter de pruebas | No hay inicio de sesión con proveedores externos en el MVP |
| Agregar `spring-modulith-starter-jpa` | Registro persistente de eventos entre módulos |
| Agregar `springdoc-openapi-starter-webmvc-ui` 3.x | Documentación de la API |
| Agregar MapStruct (+ `lombok-mapstruct-binding`) | Mapeo decidido en la arquitectura |
| Agregar Bucket4j, Caffeine y Bouncy Castle (Argon2id) | Límites de uso y hash de frases |
| Agregar SpotBugs + FindSecBugs y CycloneDX | Desarrollo seguro (arquitectura) |
| Agregar `jacoco-maven-plugin` con regla de 70 % | Definición de terminado |
| Agregar `archunit-junit5` | Pruebas de fronteras de capas |
| Agregar `spring-boot-testcontainers` y `testcontainers-postgresql` | Integración con PostGIS |
| Configurar `maven-failsafe-plugin` para `*IT` | Separar unitarias de integración |
| Agregar Spotless (`palantir-java-format`) | Formato uniforme |
| Agregar `.editorconfig`, `.env.ejemplo` y plantilla de pull request | Secciones 13, 16 y 17 |

Las versiones exactas se fijan al agregarlas, revisando que sean compatibles con Spring Boot 4.1.

---

## 20. Decisiones pendientes

### 20.1 Para Pedro

Ninguna. Pedro aprobó todas las decisiones el 07/11/2026.

### 20.2 Para el equipo de backend (se pueden decidir al empezar el Sprint 1)

1. Librería de Web Push.
2. Duración exacta del token de refresco.

---

## Historial

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1 | 07/11/2026 | Primera propuesta |
| 0.2 | 07/11/2026 | Pedro aprueba nombres descriptivos en español (D-03 y D-04); no se usan prefijos de tipo |
| 1.1 | 07/11/2026 | D-23: sin procedimientos almacenados (sección 10.1) |
| 1.0 | 07/11/2026 | Pedro aprueba todas las propuestas; el arquitecto de software aprueba los pull requests; paquete `com.quienlotiene` aplicado |
