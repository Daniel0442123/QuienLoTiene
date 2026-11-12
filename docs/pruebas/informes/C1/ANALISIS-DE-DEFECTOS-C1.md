# Análisis de defectos y principios de las pruebas · Ciclo 1

> Versión preliminar con el corte del Sprint 1 (12/11/2026, v0.2.0). Se completa al cerrar el C1 con los defectos del Sprint 2.

## 1. Error → defecto → falla

| ID defecto (ref. libro) | Descripción del defecto | Error probable (¿qué pudo pensar quien programó?) | Defecto en el código (clase, método, línea) | Falla observada (comportamiento con datos concretos) | ¿Pudo prevenirse con una revisión o una prueba temprana? |
|---|---|---|---|---|---|
| DEF-001 (Exploratoria-20261112) | El contrato OpenAPI de /v3/api-docs no marca required, enum ni nullable en los esquemas | Se asumió que springdoc deduce required y enum de los records de Java; no lo hace sin anotaciones o un OpenApiCustomizer. | `com.quienlotiene.shared (configuración de springdoc) y DTOs de cada módulo` | Ningún esquema de respuesta trae required; estado, rol, modalidad, tipo, diaSemana y mediosPago salen como string; no hay nullable; faltan DiaSemana, MedioPago y Ubicacion; la descarga solo declara 404; en el multipart del registro de tienda solo datos es obligatorio. | Sí: una prueba de contrato que genere los tipos del frontend en el CI lo habría detectado en la primera historia. |
| DEF-002 (NF-SEG-011) | El pipeline de CI no ejecuta OWASP Dependency-Check, que NF-SEG-011 exige junto con Trivy | Se priorizó Trivy por velocidad y se dejó Dependency-Check para después, sin registrarlo. | `.github/workflows/ci.yml (job seguridad)` | El job corre SpotBugs/FindSecBugs, Trivy (0 críticas, 0 altas) y Gitleaks; no hay paso de OWASP Dependency-Check. | Sí: una revisión del ci.yml contra la tabla de no funcionales del plan C1. |
| DEF-003 (NF-MAN-022) | jacoco:check valida el 70 % sobre todo el proyecto (BUNDLE) y no por módulo como pide el plan | Se configuró la regla mínima del ejemplo de JaCoCo sin desglosar por paquete. | `pom.xml · jacoco-maven-plugin · execution verificar-cobertura` | La regla usa <element>BUNDLE</element>: un módulo con poca cobertura pasaría si el total supera el 70 %. Hoy todos los módulos están por encima (mínimo identidad 94,1 %). | Sí: revisando la regla de JaCoCo contra el criterio 3 del plan C1 al configurar el pom.xml. |
| DEF-004 (NF-FIA-016) | La prueba de transiciones inválidas de la tienda no lleva @Tag("NF-FIA-016") | La prueba se escribió para HU-036 y no se pensó en el caso no funcional. | `com.quienlotiene.tiendas.domain.model.RevisionTiendaTest#rechazaTransicionesInvalidas` | La prueba pasa (APROBADA, RECHAZADA y SUSPENDIDA lanzan TIE-004) pero solo la clase lleva @Tag("HU-036"); no se puede filtrar por NF-FIA-016. | Sí: la revisión de etiquetas en el pull request (riesgo R4 del plan). |

## 2. Principios de las pruebas · autoevaluación del equipo

| # | Principio | Pregunta de autoevaluación | Respuesta del equipo | Evidencia del ciclo |
|---|---|---|---|---|
| 1 | Las pruebas muestran la presencia de defectos, no su ausencia | ¿Alguna vez concluimos que un módulo «no tiene errores» porque todas sus pruebas pasaron? | No. Las 256 pruebas pasan, pero el corte encontró 4 defectos que ninguna prueba detecta: están en el contrato de la API, el pipeline y la configuración. | DEF-001 a DEF-004 con 0 pruebas en rojo |
| 2 | Las pruebas exhaustivas son imposibles | ¿Cómo priorizamos qué probar con el tiempo disponible? | Por prioridad MoSCoW y riesgo: primero las reglas de negocio de las historias Must (dominio y casos de uso), después la integración donde hay base de datos (PostGIS) o seguridad. | 28 de 30 casos del Sprint 1 son Alta; cobertura 97,2 % en domain + application |
| 3 | Las pruebas tempranas ahorran tiempo y dinero | ¿Escribimos las pruebas antes o junto con el código? ¿Qué defecto se evitó? | Junto con el código, en el mismo pull request. Los límites (frase de 15 caracteres, límite de 10 solicitudes por hora) se fijaron con pruebas de valores límite antes de fusionar. | CP-IDE-001-03 y CP-SOL-016-03 en verde desde su PR |
| 4 | Los defectos se agrupan | ¿En qué módulo se concentraron los defectos? | En lo transversal (pipeline, configuración, contrato de la API), no en los módulos de negocio. | 3 de 4 defectos son transversales |
| 5 | Paradoja del pesticida | ¿Agregamos casos de naturaleza distinta o solo re-ejecutamos los mismos? | Además de las unitarias se revisaron el contrato OpenAPI, el pipeline y la configuración de JaCoCo; de ahí salieron los defectos. | DEF-001 salió de una revisión, no de una prueba |
| 6 | Las pruebas dependen del contexto | ¿Qué parte del sistema exige un tipo de prueba distinto (PostGIS, SSE, seguridad)? | La difusión por radio (PostGIS) y la seguridad de la API solo se prueban bien con integración y Testcontainers; el resto con unitarias. | 47 pruebas de integración, entre ellas DifusionSolicitudIT y TiendasElegiblesIT |
| 7 | La falacia de la ausencia de errores | ¿El sistema cumple lo que necesita el usuario, además de pasar las pruebas? | Falta verificarlo de punta a punta; eso queda para C3 con Playwright y las pruebas de humo en producción. | E2E y humo fuera del alcance del C1 |

## 3. Reflexión

| # | Pregunta | Respuesta del equipo |
|---|---|---|
| 1 | ¿Cómo ayudó priorizar las pruebas unitarias en este ciclo? | Cada historia llegó con sus pruebas etiquetadas, así que el corte se ejecutó en una sola corrida de `./mvnw verify` y cada caso del libro tiene su prueba trazable. |
| 2 | ¿Qué cambiaríamos en el siguiente ciclo? | Revisar en cada PR también lo que no es código de negocio (pipeline, contrato OpenAPI, etiquetas NF) y hacer el corte en la fecha del plan. |
