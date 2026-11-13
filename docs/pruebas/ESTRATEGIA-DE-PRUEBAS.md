# Estrategia de pruebas · Quién lo tiene

> **Control del documento** · Versión 2.2 · 13/11/2026 · Responsable: Yanimar Burbano Piamba (QA funcional) y Francisco José Castro Muñoz (QA no funcional y seguridad) · Estado: Vigente · Cambios: «Historial del documento» al final y `docs/gestion/CONTROL-DE-CAMBIOS.md`

## 1. Qué cambió y por qué

En la revisión del plan, el docente recomendó **priorizar las pruebas unitarias** y dejar las pruebas de punta a punta para el momento en que **varios módulos estén desplegados juntos**, porque con 4,5 semanas no alcanza el tiempo para automatizar todos los niveles desde el Sprint 1. También pidió **dejar documentada cada ejecución de pruebas**.

| Aspecto | Versión 1 (plan inicial) | Versión 2 (esta) |
|---|---|---|
| Prioridad | Los cuatro niveles en paralelo desde el Sprint 1 | Unitarias primero; los demás niveles entran por fases |
| Punta a punta (Playwright) | Automatización por historia en cada sprint | Solo 3 a 5 flujos críticos, cuando el flujo principal esté desplegado (Cierre) |
| Aceptación (Cucumber) | 85 escenarios automatizados | Los escenarios Gherkin se mantienen como especificación; se automatizan solo los de historias Must, en la fase de integración de módulos |
| Integración (Testcontainers) | Por historia | Solo donde la lógica depende de la base de datos (consultas por radio con PostGIS, adaptadores de persistencia) |
| Evidencia | Reportes de CI | Reportes de CI **más** el libro de control de QA por ciclo, con la estructura de las plantillas del docente (ver sección 7) |
| Definición de terminado | Pruebas unitarias e integración pasan | Pruebas unitarias pasan, cobertura ≥ 70 % y resultado registrado en el libro de control |

Los 85 casos de prueba del SRS no se pierden: siguen siendo la referencia de qué hay que verificar. Lo que cambia es **en qué nivel y en qué momento** se ejecuta cada uno.

## 2. Pirámide de pruebas actualizada

```
                 ┌───────────────────────────────┐
                 │ Punta a punta · Playwright     │  C3 · Cierre (23/11 al 25/11)    
                 │ 3 a 5 flujos críticos          │  cuando varios módulos estén desplegados
               ┌─┴───────────────────────────────┴─┐
               │ Aceptación · Cucumber (API)        │  C3 · historias Must  
               │ escenarios Gherkin de las HU Must  │
             ┌─┴───────────────────────────────────┴─┐
             │ Integración · Testcontainers (PostGIS)  │  C1 y C2 · solo persistencia
             │ consultas por radio y repositorios      │  y consultas geoespaciales
           ┌─┴───────────────────────────────────────┴─┐
           │ UNITARIAS · JUnit 5 + Mockito + AssertJ     │  C1 en adelante · PRIORIDAD
           │ reglas de dominio y casos de uso            │  en cada pull request, desde el Sprint 1
           │ + arquitectura (Spring Modulith, ArchUnit)  │
           └─────────────────────────────────────────────┘
```

## 3. Niveles de prueba

### 3.1 Unitarias (prioridad alta, obligatorias)

- **Qué se prueba:** entidades, objetos de valor y máquinas de estado de `domain/model/`, y los casos de uso de `application/usecase/` con sus puertos (`domain/port/`) simulados con Mockito.
- **Qué no se prueba aquí:** controladores, JPA, Spring, red ni base de datos. Una prueba unitaria no levanta el contexto de Spring y tarda milisegundos.
- **Herramientas:** JUnit 5, Mockito, AssertJ. Cobertura con JaCoCo.
- **Meta:** cobertura de líneas ≥ 70 % en `domain` y `application` de cada módulo. Las reglas de negocio críticas tienen al menos un caso válido, uno inválido y uno de límite.
- **Ubicación:** `src/test/java/com/quienlotiene/<modulo>/domain/...` y `.../<modulo>/application/...`, espejo del código.
- **Nombre de clase:** `<ClaseProbada>Test.java` (las ejecuta `mvn test`).
- **Quién:** el desarrollador que escribe el código (Daniel, Deiner, Monica en frontend). QA revisa en el pull request que existan y que cubran los criterios de aceptación.

Reglas de negocio que deben tener prueba unitaria sí o sí (vienen del SRS y de los valores aprobados en la Fase 9):

| Módulo | Regla | HU |
|---|---|---|
| identidad | Frase de contraseña de mínimo 15 caracteres; TOTP del administrador | HU-001, HU-003, HU-004 |
| identidad | Aceptación obligatoria de la política de datos (Ley 1581) | HU-005 |
| tiendas | Solo una tienda aprobada y abierta es elegible; transiciones de aprobación | HU-013, HU-036 |
| solicitudes | Máximo 10 solicitudes por hora por cliente | HU-016 |
| solicitudes | Vencimiento a los 30 minutos; ampliación de radio 3 → 5 km a los 10 minutos sin respuestas, una sola vez | HU-021, HU-022 |
| solicitudes | Respuesta Sí, Parecido o No; la tienda puede ofrecer menos unidades | HU-019 |
| ordenes | Máquina de estados de domicilio y de recogida; cancelación; reserva vencida | HU-024 a HU-029 |
| reputacion | Calificación solo después de una orden entregada | HU-034 |
| catalogo | Advertencia de producto restringido | HU-023, HU-038 |

### 3.2 Arquitectura (corren junto con las unitarias)

- Spring Modulith (`ApplicationModules.of(...).verify()`) y ArchUnit en `src/test/java/com/quienlotiene/arquitectura/`.
- Son rápidas y protegen las fronteras entre módulos, por eso se ejecutan en cada pull request desde el Sprint 1.

### 3.3 Integración (prioridad media, selectiva)

- **Qué se prueba:** adaptadores de `infrastructure/persistence/` y, sobre todo, las consultas geoespaciales de difusión por radio contra la misma imagen `postgis/postgis:17-3.5-alpine` del `compose.yaml`; migraciones Flyway por esquema.
- **Cuándo:** cuando la persistencia de un módulo esté lista (ciclos C1 y C2).
- **Nombre de clase:** `<Clase>IT.java` (las ejecuta `mvn verify` con Failsafe, no `mvn test`), para que las unitarias sigan siendo rápidas.

### 3.4 Aceptación (prioridad media, ciclo C3)

- Los escenarios Gherkin en español de las 38 historias se mantienen como especificación y como guía para escribir las pruebas unitarias.
- Se automatizan con Cucumber, contra la API REST, **solo los escenarios de historias Must**, cuando los módulos que participan en la historia estén integrados.
- Los demás escenarios se verifican manualmente en la revisión del sprint y se registran con la misma plantilla.

#### 3.5 Punta a punta (prioridad baja hasta el despliegue integrado)

- **Condición de entrada:** el flujo principal está desplegado en el VPS con, como mínimo, los módulos `identidad`, `tiendas`, `solicitudes`, `ordenes` y `notificaciones` funcionando juntos. Se espera al final del Sprint 3 o en el Cierre (23/11 al 26/11).
- **Alcance:** 3 a 5 flujos críticos con Playwright:
  1. Cliente se registra, publica una solicitud y una tienda responde "Sí lo tengo".
  2. Cliente elige una respuesta, se crea la orden y la tienda la lleva hasta entregada.
  3. Administrador inicia sesión con segundo factor y aprueba una tienda.
  4. (Si hay tiempo) Cliente califica la tienda después de la entrega.
  5. (Si hay tiempo) Solicitud sin respuestas que amplía el radio y luego vence.
- Antes de esa fecha, los flujos se prueban **manualmente** en cada revisión de sprint y se dejan en el registro de ejecuciones.

### 3.6 No funcionales y seguridad (ciclo C3, Francisco Castro)

- k6 (carga), Trivy y OWASP Dependency-Check (seguridad), pruebas de autorización por objeto (riesgo R-03).
- El escaneo de dependencias puede ir en CI desde el inicio porque no consume tiempo del equipo; la carga se ejecuta sobre el VPS en el Cierre.

## 4. Calendario por ciclos

| Ciclo | Sprint | Niveles que se ejecutan | Disparador | Documentación terminada |
|---|---|---|---|---|
| C1 | Sprints 1 y 2 (02/11 al 15/11) | Unitarias + arquitectura + escaneos; integración solo en PostGIS, API y SSE | Cada pull request y cada historia terminada | Corte del Sprint 1 (08/11) y cierre C1 (15/11) |
| C2 | Sprint 3 (16/11 al 22/11) | Lo anterior + integración y seguridad entre módulos + re-prueba de C1 | Persistencia y API de los módulos listas | Cierre C2 (22/11) |
| C3 | Cierre (23/11 al 25/11) | Regresión completa + aceptación Must + punta a punta + carga, accesibilidad y compatibilidad | Varios módulos desplegados juntos en el VPS | Cierre C3, informe final y acta de liberación (25/11) |
| Producción | 26/11 | Pruebas de humo | Despliegue de v1.0.0 | Registro de humo |

La meta para liberar es ≥ 95 % de casos aprobados en el resultado final (mínimo 90 % con excepción justificada), 0 defectos S1 y S2 abiertos y cobertura ≥ 70 %. El detalle está en el Plan Maestro, sección 6.

## 5. Etiquetas y trazabilidad (@HU · @CP)

Cada prueba lleva la etiqueta de su historia y de su caso de prueba del SRS, para poder ejecutarla, filtrarla y reportarla.

```java
@Tag("HU-016")
@Tag("CP-041")   // número de ejemplo: usar el CP real del SRS
@DisplayName("CP-041 · Rechaza la solicitud número 11 dentro de la misma hora")
@Test
void rechazaSolicitudCuandoSuperaElLimitePorHora() { ... }
```

En Cucumber se usan las etiquetas `@HU-016 @CP-041` sobre el escenario.

Comandos útiles:

| Para qué | Comando |
|---|---|
| Todas las unitarias y de arquitectura | `./mvnw test` |
| Unitarias de un módulo | `./mvnw test -Dtest="com.quienlotiene.solicitudes.**.*Test"` |
| Unitarias de una historia | `./mvnw test -Dgroups="HU-016"` |
| Unitarias + integración + reporte de cobertura | `./mvnw verify` |
| Reporte de cobertura | `target/site/jacoco/index.html` (resumen en `target/site/jacoco/jacoco.csv`) |

> Nota: el `pom.xml` todavía no tiene JaCoCo ni Failsafe. En el anexo A está el fragmento para agregarlos; conviene hacerlo en un pull request propio antes de cerrar el Sprint 1.

## 6. Definición de terminado (actualizada)

Una historia está terminada cuando:

1. El código fue revisado por otra persona mediante pull request.
2. **Las pruebas unitarias de la historia pasan** y cada criterio de aceptación tiene al menos una prueba con su etiqueta `@HU`/`@CP`.
3. **La cobertura de `domain` y `application` del módulo es ≥ 70 %**.
4. Las pruebas de arquitectura pasan.
5. **QA registró el resultado** de los casos de la historia en la hoja «Ejecución» del ciclo del libro de control (sección 7).
6. La documentación de la API está actualizada y el cambio está desplegado en el ambiente de pruebas.

Las pruebas de integración, aceptación y punta a punta dejan de ser requisito para cerrar una historia y pasan a ser requisito para cerrar la fase correspondiente.

## 7. Documentación de las pruebas

Toda la documentación se escribe primero en Markdown dentro de `docs/pruebas/`, con la misma estructura y los mismos campos de las plantillas del docente, y después se pasa a esas plantillas (Word y Excel). Responsables: Yanimar Burbano Piamba y Francisco José Castro Muñoz.

```
docs/pruebas/
├── README.md                         ← índice y cómo pasar a Word/Excel
├── ESTRATEGIA-DE-PRUEBAS.md          ← este documento
├── 01-PLAN-MAESTRO-DE-PRUEBAS.md     ← Plan Maestro (IEEE 829 / ISO 29119-3)
├── 02-PLAN-DE-PRUEBAS-C1.md          ← plan de cada ciclo
├── 02-PLAN-DE-PRUEBAS-C2.md
├── 02-PLAN-DE-PRUEBAS-C3.md
├── libro/                            ← Libro de control de QA (una hoja por archivo)
├── plantillas/                       ← informes, actas, evidencias, defecto, análisis
├── informes/C1 · C2 · C3/            ← informes y actas diligenciados de cada ciclo
├── defectos/                         ← DEF-nnn.md con el detalle de cada defecto
└── evidencias/C1 · C2 · C3/<sesión>/<caso>/
```

Cuándo se registra:

| Momento | Qué se registra | Dónde | Quién |
|---|---|---|---|
| Al cerrar cada historia | Resultado de sus casos (Aprobado, Fallido, Bloqueado) | `libro/03-…05-EJECUCION-Cn.md` | Yanimar o Francisco, según el caso |
| Cada día con pruebas | Una fila por responsable | `libro/08-BITACORA.md` | Yanimar, Francisco |
| Cada defecto | Una fila y, si hace falta, su ficha | `libro/06-DEFECTOS.md` y `defectos/` | Quien lo encuentra |
| Último día de cada sprint | Tablero, trazabilidad, evidencias e informe de corte o de cierre | `libro/09-TABLERO.md`, `informes/Cn/` | Yanimar |
| 25/11 y 26/11 | Informe final, acta de liberación y pruebas de humo | `informes/` | Yanimar, Francisco, Product Owner |

Las ejecuciones automáticas de GitHub Actions en cada push no necesitan registro manual. Si se usan como evidencia, se enlazan en el libro.

## 8. Métricas que se reportan en cada revisión de sprint

- Pruebas unitarias: total, pasan, fallan, omitidas.
- Cobertura de líneas y de ramas por módulo (`domain` + `application`), contra la meta del 70 %.
- Historias terminadas con todas sus pruebas etiquetadas / historias del sprint.
- Casos de prueba del SRS cubiertos por al menos una prueba (de 85).
- Defectos abiertos y cerrados por severidad.

## 9. Texto sugerido para la diapositiva 16 ("Estrategia de pruebas")

- Base (resaltada): **Unitarias · JUnit 5 + Mockito · prioridad: en cada pull request desde el Sprint 1**.
- **Integración · Testcontainers · solo PostGIS y persistencia (Sprints 2 y 3)**.
- **Aceptación · Cucumber · escenarios de historias Must, al integrar módulos**.
- Cima: **Punta a punta · Playwright · 3 a 5 flujos, al desplegar varios módulos (Cierre)**.
- Tarjetas laterales: "≥ 70 % cobertura en dominio y aplicación · JaCoCo", "@HU · @CP en cada prueba" y una nueva: **"Cada ejecución queda registrada · libro de control de QA por ciclo (C1, C2, C3)"**.

## Anexo A · Fragmento para el `pom.xml`

```xml
<!-- Dentro de <dependencies>: AssertJ y Mockito ya vienen con spring-boot-starter-test.
     Si no está, agregar: -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- Dentro de <build><plugins> -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-failsafe-plugin</artifactId>
    <!-- versión gestionada por spring-boot-starter-parent; ejecuta *IT.java en mvn verify -->
</plugin>
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.13</version>
    <executions>
        <execution>
            <id>preparar-agente</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>reporte</id>
            <phase>test</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>verificar-cobertura</id>
            <goals><goal>check</goal></goals>
            <configuration>
                <includes>
                    <include>com/quienlotiene/*/domain/**</include>
                    <include>com/quienlotiene/*/application/**</include>
                </includes>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Con esto, `./mvnw verify` falla si la cobertura de dominio y aplicación baja del 70 %, y el pipeline de GitHub Actions lo hace cumplir automáticamente.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
|---|---|---|---|
| — | 07/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | chore: base técnica del Sprint 0 (#1) |
| 2.2 | 13/11/2026 | Daniel Montenegro Almendra (@Daniel0442123) | Se agrega el control del documento (encabezado e historial). Las filas anteriores se reconstruyeron desde Git; esos cambios no tenían número de versión (el documento declaraba la versión 2.1) (SC-009) |
