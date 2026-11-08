# Plan Maestro de Pruebas y Modelo de QA · Quién lo tiene

**ASEGURAMIENTO DE LA CALIDAD**
Quién lo tiene · Primera liberación del producto (v1.0.0)
Normas de referencia: IEEE Std 829-2008 (documentación de pruebas) e ISO/IEC/IEEE 29119-2 y 29119-3
Versión en pruebas: v0.1.0 (se actualiza con cada versión desplegada)
Versión del documento: 1.0 · Fecha: 07 de noviembre de 2026
Instrumentos asociados: Libro de control de QA (`libro/`), plan de pruebas de cada ciclo (`02-PLAN-DE-PRUEBAS-C1.md`, `C2`, `C3`), plantillas de informes y actas (`plantillas/`), SRS de Quién lo tiene, estrategia de pruebas (`ESTRATEGIA-DE-PRUEBAS.md`).

> Documento controlado. Toda modificación se registra en el control de cambios y se comunica al equipo.

## Control del documento

| Versión | Fecha | Autor | Cambios |
|---|---|---|---|
| 1.0 | 07/11/2026 | Yanimar Burbano Piamba, Francisco José Castro Muñoz | Primera versión: modelo de pruebas priorizando unitarias, tres ciclos ligados a los sprints, gestión de defectos, instrumentos y criterios de liberación. |

| Rol | Nombre | Firma | Fecha |
|---|---|---|---|
| Elaboró (líder de QA) | Yanimar Burbano Piamba | | |
| Elaboró (QA no funcional y seguridad) | Francisco José Castro Muñoz | | |
| Revisó (arquitecto de software) | Daniel Montenegro Almendra | | |
| Aprobó (Product Owner) | Wilson Javier Galindez Certuche | | |

## 1. Introducción

### 1.1 Propósito

Este plan define cómo se probará Quién lo tiene antes de su primera liberación en producción (v1.0.0, 26/11/2026): el modelo de pruebas, la organización de QA, el ambiente y los datos, el calendario de tres ciclos ligados a los sprints, la gestión de defectos, los instrumentos de control y los criterios con los que se decide la liberación.

### 1.2 Alcance de la campaña

- **Funcionales:** los 38 requerimientos funcionales de la entrega (RF-001 a RF-038), con un caso por cada uno de sus 85 criterios de aceptación en Gherkin (SRS, sección 14). La mayoría se verifican con **pruebas unitarias** de dominio y casos de uso.
- **Seguridad:** 16 de esos casos se tratan como pruebas de seguridad (autenticación, segundo factor, autorización por objeto, privacidad del cliente, límites de uso).
- **No funcionales:** un caso por cada uno de los 30 RNF (ISO/IEC 25010): rendimiento, seguridad, fiabilidad, disponibilidad, escalabilidad, mantenibilidad, compatibilidad, usabilidad y accesibilidad, portabilidad.
- **Extremo a extremo:** 3 escenarios obligatorios y 2 opcionales que cruzan módulos, con Playwright, cuando el flujo principal esté desplegado.
- **Producción:** pruebas de humo después del despliegue final.

Fuera de alcance: RF-039 a RF-042 (Won't: pagos dentro de la app, plan de pago, supresión de cuenta, inicio sin contraseña), pruebas de penetración y auditoría OWASP ASVS completa, pruebas con tiendas o clientes reales.

### 1.3 Referencias

- Especificación de Requisitos de Software (SRS) de Quién lo tiene, con criterios de aceptación en Gherkin.
- Documento de Arquitectura de Software de Quién lo tiene.
- Plan de proyecto de Quién lo tiene (sprints, roles, definición de terminado).
- IEEE Std 829-2008; ISO/IEC/IEEE 29119-2 y 29119-3; ISO/IEC 25010; Ley 1581 de 2012; NIST SP 800-63B.

### 1.4 Definiciones

| Término | Definición |
|---|---|
| Caso de prueba (CP) | Condición verificable con precondición, pasos y resultado esperado. ID `CP-<MÓDULO>-<HU>-<nn>`; conserva el número del SRS como referencia. |
| Prueba automatizada | Método `@Test` (JUnit) o escenario Cucumber que verifica uno o más casos. Lleva las etiquetas `@HU-xxx` y `@CP-xxx`. |
| Ciclo | Ejecución planificada sobre una versión. Hay tres: C1 (Sprints 1 y 2), C2 (Sprint 3) y C3 (Cierre). |
| Corte de sprint | Registro parcial de la ejecución al terminar un sprint, dentro de un ciclo. |
| Re-prueba | Nueva ejecución de un caso fallido o bloqueado después de la corrección. |
| Regresión | Nueva ejecución de casos ya aprobados para comprobar que un cambio no rompió nada. |
| Defecto | Diferencia entre el resultado esperado y el obtenido, registrada como DEF-nnn. |
| Bloqueado | Caso que no se pudo ejecutar por una causa externa al producto. |

## 2. Modelo de pruebas

### 2.1 Enfoque

La campaña combina un enfoque **basado en requisitos** (cada criterio de aceptación del SRS tiene su caso) con un enfoque **basado en riesgo** (la prioridad de ejecución y regresión sigue el impacto de una falla). Por recomendación del docente, se **prioriza la prueba unitaria**: cada historia se cierra con sus pruebas unitarias en verde y su registro, y las pruebas de punta a punta se hacen cuando varios módulos estén desplegados juntos. Las pruebas se diseñan antes o junto con el código (shift-left): se escribe el caso, se ve fallar y luego se implementa.

### 2.2 Niveles y tipos de prueba

| Nivel / tipo | Objetivo | Quién lo escribe | Quién lo ejecuta y registra | Cuándo | Casos |
|---|---|---|---|---|---|
| Unitarias (JUnit 5 + Mockito) | Reglas de dominio y casos de uso | Desarrolladores | Yanimar / Francisco | Cada pull request; registro al cerrar cada historia | 69 funcionales |
| Arquitectura (Spring Modulith, ArchUnit) | Fronteras entre módulos y capas | Arquitecto | Yanimar | Cada pull request | NF-ESC-021, NF-MAN-023 |
| Integración (Testcontainers + PostGIS, API, SSE) | Persistencia, consultas por radio, seguridad web, tiempo real | Desarrolladores | Yanimar / Francisco | C1 y C2, cuando la persistencia del módulo esté lista | 11 funcionales + NF de C2 |
| Seguridad | Autenticación, autorización por objeto, privacidad, límites, escaneos | Desarrolladores + Francisco | Francisco | C1 (escaneos), C2 y C3 | 16 funcionales + NF-SEG |
| Manual de interfaz | Botón de WhatsApp, cómo llegar, push del navegador | — | Yanimar | Al terminar la historia y en C3 | 5 |
| Aceptación (Cucumber) | Escenarios Gherkin de historias Must sobre la API | QA con apoyo de desarrollo | Yanimar | C3 | Historias Must |
| Extremo a extremo (Playwright) | Flujos completos entre roles | QA | Yanimar | C3, con el flujo principal desplegado | 5 |
| No funcionales (k6, Lighthouse, axe, monitor) | Rendimiento, disponibilidad, accesibilidad, compatibilidad | Francisco | Francisco | C3 sobre el VPS | 13 |
| Humo | Verificar el despliegue en producción | — | Yanimar y Francisco | 26/11 | 10 verificaciones |

### 2.3 Técnicas de diseño

| Técnica | Dónde se aplica (ejemplos) |
|---|---|
| Basada en requisitos | Un caso por criterio de aceptación del SRS (85). |
| Valores límite | Frase de contraseña 14 / 15 caracteres; 10 / 11 solicitudes por hora; radio 3 km / 5 km; vencimiento a los 30 minutos; ampliación a los 10 minutos; reserva de 30 a 60 minutos. |
| Partición de equivalencia | Correo nuevo / repetido; medio de pago aceptado / no aceptado; tipos de archivo JPEG, PNG, WebP, PDF válidos frente a otros. |
| Transición de estados | Máquinas de estado de la orden (domicilio y recogida), de la tienda (Pendiente, Aprobada, Rechazada, Suspendida) y de la solicitud (Abierta, Cerrada, Vencida). |
| Tabla de decisión | Elegibilidad de una tienda: aprobada × abierta × en horario × categoría × no suspendida × distancia. |
| Caja gris | Verificar el estado interno después de un caso de uso (eventos publicados, contadores, auditoría). |
| Exploratoria | Sesiones de 60 minutos con misión (anexo F) en C2 y C3. |

### 2.4 Riesgos del producto y prioridad

La prioridad de cada caso hereda la MoSCoW de su historia (Must → Alta, Should → Media, Could → Baja). Los riesgos siguientes guían la regresión y el triaje:

| Riesgo del producto | Impacto | Módulos | Prioridad |
|---|---|---|---|
| Una tienda no elegida ve datos personales del cliente | Legal (Ley 1581) y reputacional | ordenes, solicitudes | Alta |
| La solicitud llega a tiendas fuera del radio, cerradas o suspendidas | Propuesta de valor | solicitudes, tiendas | Alta |
| El total de la orden o el estado de la orden es incorrecto | Económico y de confianza | ordenes | Alta |
| Un usuario suspendido o sin segundo factor entra al sistema | Seguridad | identidad | Alta |
| Las respuestas no llegan en tiempo real | Experiencia principal | notificaciones | Alta |
| Calificaciones o reportes sobre órdenes inexistentes | Confianza | reputacion | Media |
| La tienda no puede configurar horario, domicilio o medios de pago | Participación de tiendas | tiendas | Media |
| El servidor de bajos recursos no soporta la carga de la demo | Disponibilidad | todos | Media |

### 2.5 Cobertura

| Ciclo | Historias | Funcionales | Seguridad | Unitaria | Integración | Manual | No funcionales | E2E | Total |
|---|---|---|---|---|---|---|---|---|---|
| C1 (Sprints 1 y 2) | 22 historias de los Sprints 1 y 2 | 40 | 13 | 41 | 10 | 2 | 8 | — | 61 |
| C2 (Sprint 3) | 11 historias del Sprint 3 | 19 | 3 | 18 | 1 | 3 | 9 | — | 31 + re-pruebas de C1 |
| C3 (Cierre) | 5 historias Could + regresión de las 38 | 10 nuevos + 75 en regresión | — | 10 nuevos | — | — | 13 | 5 | 103 |

El detalle por caso (nivel, técnica, responsable) está en `libro/02-CASOS.md`. Cada requisito funcional tiene al menos un caso; la hoja «Trazabilidad» muestra su estado.

## 3. Organización del equipo

### 3.1 Roles y responsabilidades

| Rol | Persona | Responsabilidades |
|---|---|---|
| Product Owner | Wilson Javier Galindez Certuche | Aprueba este plan, prioriza y difiere defectos, firma el acta de liberación. |
| Líder de QA · pruebas funcionales | Yanimar Burbano Piamba | Prepara cada ciclo, verifica criterios de entrada, ejecuta y registra los casos funcionales, manuales y E2E, conduce el triaje, elabora los informes de cierre y el acta de liberación. |
| QA no funcional y seguridad | Francisco José Castro Muñoz | Ejecuta y registra los casos de seguridad y los no funcionales, mantiene los escaneos del pipeline y las pruebas de carga, revisa accesibilidad. |
| Desarrolladores | Daniel Montenegro Almendra, Deiner Stiven Ramírez Vente, Monica Anyi Cajas Mellizo | Escriben las pruebas unitarias e integración de su código con las etiquetas `@HU`/`@CP`, corrigen defectos, entregan versiones con notas. |
| Administrador del ambiente | Daniel Montenegro Almendra | Despliega versiones en el VPS, prepara los datos de prueba, respalda y restaura la base. |
| Scrum Master | Brayan Grajales Escarraga | Facilita el triaje y elimina impedimentos. |

### 3.2 Distribución de casos

| Responsable QA | Funcionales | Seguridad | No funcionales | E2E | Total |
|---|---|---|---|---|---|
| Yanimar Burbano Piamba | 69 | — | 4 (arquitectura, cobertura, máquinas de estado) | 5 | 78 |
| Francisco José Castro Muñoz | — | 16 | 26 | — | 42 |

### 3.3 Comunicación

- Daily de Scrum (15 min): avance de pruebas, bloqueos y defectos nuevos. Se anota en la hoja «Bitácora».
- Triaje de defectos (15 min, después de la daily, solo si hay defectos nuevos): líder de QA y desarrollador del módulo confirman, clasifican y asignan.
- Corte de sprint (domingo): QA actualiza el libro, el tablero y el informe del sprint antes de la revisión.
- Los defectos solo se reportan en el libro (y en un issue de GitHub enlazado); las dudas van al grupo del equipo.

## 4. Ambiente y datos de prueba

### 4.1 Ambiente

| Componente | Ambiente de pruebas |
|---|---|
| Pruebas unitarias | Equipo del desarrollador y GitHub Actions; Java 21, `./mvnw test`; sin base de datos ni red. |
| Pruebas de integración | Testcontainers con la imagen `postgis/postgis:17-3.5-alpine` (la misma del `compose.yaml`); `./mvnw verify`. |
| Aplicación desplegada | VPS Hostinger KVM 1, Docker Compose, Caddy con HTTPS; URL del ambiente de pruebas por definir. |
| Navegadores | Chrome, Edge, Firefox y Safari (dos últimas versiones), Android 10+ e iOS 16+ (RNF-026). |
| Herramientas | JUnit 5, Mockito, AssertJ, JaCoCo, Testcontainers, Cucumber, Playwright, k6, Lighthouse, axe DevTools, Trivy, OWASP Dependency-Check, gitleaks. |

### 4.2 Preparación de datos

1. Respaldar la base del ambiente de pruebas antes de cada ciclo (`pg_dump`).
2. Desplegar la versión del ciclo con sus notas de versión.
3. Cargar los datos semilla de pruebas (script de Flyway o `scripts/`): categorías Farmacias y Ferreterías, lista de restringidos, cuentas de prueba y tiendas ubicadas a distancias conocidas.
4. Verificar el ingreso con una cuenta de cada rol.
5. Guardar un respaldo «línea base» para restaurar si los datos quedan inutilizables.

### 4.3 Cuentas y escenarios de prueba

| Cuenta | Situación preparada | Qué se prueba |
|---|---|---|
| cliente-ana | Cliente activo con ubicación en el centro | Publicar, elegir, confirmar, calificar |
| cliente-nuevo | Sin cuenta (se registra en la prueba) | Registro, política de datos |
| cliente-limite | Cliente con 10 solicitudes en la última hora | Límite de solicitudes (HTTP 429) |
| farmacia-1km, farmacia-2km, farmacia-5km | Farmacias aprobadas y abiertas a 1, 2 y 5 km | Difusión por radio y ampliación |
| ferreteria-500m | Ferretería abierta a 500 m | Categoría distinta |
| farmacia-suspendida | Farmacia suspendida a 1 km | Tienda suspendida |
| tienda-pendiente | Tienda registrada sin aprobar | Aprobación y rechazo |
| tienda-cerrada | Tienda aprobada con interruptor en Cerrado | Tienda cerrada |
| admin | Administrador con TOTP configurado | Segundo factor, aprobación, reportes, categorías |

### 4.4 Reglas de uso del ambiente

- No se usan datos personales reales: nombres, teléfonos y documentos ficticios.
- Los vencimientos (30 min, 10 min, reservas) se prueban con un reloj inyectable (`Clock`) en las unitarias; en el ambiente desplegado, con parámetros de prueba, nunca cambiando la hora del servidor.
- Los cambios de configuración se revierten al terminar el caso.
- Las cuentas de administrador usan su propio TOTP; los códigos se guardan en el gestor de contraseñas del equipo.

## 5. Calendario

| Fase | Fechas | Actividades | Entregable |
|---|---|---|---|
| Preparación | 07/11 al 08/11 | Plan maestro, libro de control, plan del C1, JaCoCo y Failsafe en el `pom.xml` | Acta de inicio (anexo B) |
| C1 · corte Sprint 1 | 02/11 al 08/11 | Unitarias de identidad, tiendas, solicitudes, administración; escaneos en CI | Informe de corte del Sprint 1 |
| C1 · Sprint 2 | 09/11 al 15/11 | Unitarias e integración de respuestas, órdenes, notificaciones y WhatsApp | Informe de cierre C1 (anexo D) y evidencias |
| C2 · Sprint 3 | 16/11 al 22/11 | Casos del Sprint 3, integración y seguridad, re-prueba de C1 | Informe de cierre C2 y evidencias |
| C3 · Cierre | 23/11 al 25/11 | Regresión completa, aceptación, E2E, carga, accesibilidad, compatibilidad | Informe de cierre C3, informe final de pruebas |
| Comité de liberación | 25/11 | Revisión de criterios go / no-go | Acta de liberación (anexo E) |
| Liberación | 26/11 | Despliegue en producción y pruebas de humo | Registro de humo (anexo H) |

La documentación de cada sprint queda terminada el último día del sprint: libro actualizado, evidencias guardadas e informe firmado.

## 6. Criterios

### 6.1 Criterios de entrada al ciclo

- Versión desplegada en el ambiente de pruebas con notas de versión, y pruebas automatizadas en verde en GitHub Actions.
- Datos de prueba cargados y verificados (4.2).
- Libro de control con «Parámetros y equipo» completos y carpeta de evidencias del ciclo creada.
- Para C2 y C3: defectos S1 y S2 del ciclo anterior corregidos o diferidos con aprobación.

### 6.2 Suspensión y reanudación

| Se suspende si… | Se reanuda cuando… |
|---|---|
| El ambiente no está disponible más de 2 horas | Se restablece y se verifica con las pruebas automatizadas |
| Un defecto S1 bloquea más del 30 % de los casos del ciclo | Se despliega la corrección |
| Los datos de prueba quedan inutilizables | Se restaura la línea base |
| La compilación del pipeline está en rojo | El pipeline vuelve a verde |

### 6.3 Criterios de salida de cada ciclo

- **C1:** 100 % de los casos del ciclo ejecutados (o No aplica justificado); todo Fallido con defecto; cobertura ≥ 70 % en los módulos del ciclo; informe de cierre C1.
- **C2:** 100 % de los casos del ciclo y de las re-pruebas ejecutados; 0 S1 y S2 abiertos; informe de cierre C2.
- **C3:** regresión completa ejecutada; E2E obligatorios (E2E-01 a E2E-03) aprobados; no funcionales medidos en el VPS; informe de cierre C3.

### 6.4 Criterios de liberación (go / no-go)

| Criterio | Meta |
|---|---|
| Casos de prioridad Alta ejecutados | 100 % |
| Casos aprobados (resultado final, sin «No aplica») | ≥ 95 % (mínimo 90 % con excepción justificada en el acta) |
| Defectos S1 Crítica abiertos | 0 |
| Defectos S2 Alta abiertos | 0 |
| Defectos S3 Media abiertos | ≤ 5, con plan de corrección |
| Cobertura de líneas en domain y application | ≥ 70 % |
| Escenarios E2E obligatorios aprobados | 3 de 3 |
| Vulnerabilidades críticas y altas sin justificar (Trivy, Dependency-Check) | 0 |
| Rendimiento: p95 de la API con 50 usuarios | ≤ 500 ms |

## 7. Gestión de defectos

### 7.1 Severidad y prioridad

Ver la hoja `libro/10-LISTAS.md`: severidad S1 Crítica a S4 Baja con definición y ejemplos del producto, prioridad P1 Inmediata a P4 Baja y matriz de referencia. Por defecto S1 → P1, S2 → P2, S3 → P3 y S4 → P4; el triaje puede cambiar la prioridad con justificación.

### 7.2 Flujo de estados

Nuevo → Confirmado → Asignado → En corrección → Corregido → En verificación → Cerrado. Si la re-prueba falla, Reabierto → Asignado. Un defecto puede quedar Rechazado (no es un defecto o duplicado) o Diferido (solo S3 y S4, con aprobación del Product Owner).

### 7.3 Reglas de registro

- Un defecto por fila, con el ID del caso que lo evidencia (o `Exploratoria-AAAAMMDD`).
- Título en la forma «Qué falla + dónde + con qué dato» (por ejemplo: «La difusión envía la solicitud a una farmacia a 5 km con radio de 3 km»).
- Pasos numerados reproducibles, resultado esperado (del caso) y obtenido, versión o commit y hora.
- Severidad y prioridad con su justificación.
- Evidencia obligatoria: salida de la prueba que falla o captura, en la carpeta del ciclo.
- Para defectos de pruebas unitarias: nombre de la prueba (`Clase#metodo`) y mensaje de la aserción.

### 7.4 Re-prueba y regresión

Un defecto pasa a «En verificación» cuando su corrección está fusionada y desplegada; QA lo re-prueba con el mismo caso y lo cierra o lo reabre. Toda corrección debe dejar una prueba automatizada que la cubra. En cada ciclo se ejecuta la suite automatizada completa como regresión; en C3 también la regresión manual de los casos de prioridad Alta.

### 7.5 Análisis de los defectos

Al cerrar cada ciclo se analiza cada defecto con la ficha error → defecto → falla y el checklist de principios de las pruebas (plantilla `plantillas/ANALISIS-DE-DEFECTOS.md`).

## 8. Instrumentos de QA

| Instrumento | Propósito | Responsable | Frecuencia | Ubicación |
|---|---|---|---|---|
| Libro de control de QA | Casos, ejecución por ciclo, defectos, trazabilidad, bitácora, tablero | Yanimar / Francisco | En cada sesión de pruebas | `docs/pruebas/libro/` (fuente) y Excel |
| Plan maestro (este documento) | Reglas de la campaña | Yanimar | Al inicio y ante cambios | `docs/pruebas/` |
| Plan de pruebas por ciclo | Alcance, objetivos, casos y cronograma del ciclo | Yanimar | Al inicio de cada ciclo | `02-PLAN-DE-PRUEBAS-C1.md`, `C2`, `C3` |
| Acta de inicio (anexo B) | Confirma criterios de entrada | Yanimar + Product Owner | Una vez por ciclo | `plantillas/ACTA-DE-INICIO.md` |
| Informe de corte de sprint / diario (anexo C) | Avance, defectos y bloqueos | Yanimar | Al cerrar cada sprint | `plantillas/INFORME-DE-CORTE.md` |
| Informe de cierre de ciclo (anexo D) | Resultado del ciclo frente a los criterios de salida | Yanimar | Al cerrar cada ciclo | `plantillas/INFORME-DE-CIERRE-DE-CICLO.md` |
| Documento de evidencias | Ficha y capturas por caso | Yanimar / Francisco | Por ciclo | `plantillas/EVIDENCIAS-DE-PRUEBAS.md` |
| Informe de pruebas | Informe final de la campaña | Yanimar | Al final (25/11) | `plantillas/INFORME-DE-PRUEBAS.md` |
| Reporte de defecto (anexo A) | Detalle de cada defecto | Quien lo encuentra | Por defecto | `plantillas/REPORTE-DE-DEFECTO.md` |
| Análisis de defectos | Error, defecto, falla y principios | Yanimar | Al cerrar cada ciclo | `plantillas/ANALISIS-DE-DEFECTOS.md` |
| Acta de liberación (anexo E) | Decisión go / no-go | Product Owner + QA | 25/11 | `plantillas/ACTA-DE-LIBERACION.md` |
| Sesión exploratoria (anexo F) | Registro de exploración | QA | C2 y C3 | `plantillas/SESION-EXPLORATORIA.md` |
| Lista de accesibilidad (anexo G) | WCAG 2.1 AA | Francisco | C3 | `plantillas/CHECKLIST-ACCESIBILIDAD.md` |
| Pruebas de humo (anexo H) | Verificación en producción | Yanimar y Francisco | 26/11 | `plantillas/PRUEBAS-DE-HUMO.md` |
| Reportes automáticos | Surefire, Failsafe, JaCoCo, Trivy | CI | Cada pull request | GitHub Actions y `evidencias/` |

### 8.1 Hojas del libro de control

| Hoja | Contenido y uso |
|---|---|
| 00 Instrucciones | Cómo diligenciar, resultados, evidencias |
| 01 Parámetros y equipo | Versiones, fechas, metas y responsables |
| 02 Casos | Catálogo: 85 funcionales, 30 no funcionales y 5 E2E |
| 03 Ejecución C1 | Resultados del ciclo 1 (Sprints 1 y 2) |
| 04 Ejecución C2 | Resultados del ciclo 2 (Sprint 3) y re-pruebas de C1 |
| 05 Ejecución C3 | Regresión, E2E, no funcionales y resultado final |
| 06 Defectos | Un defecto por fila con su ciclo de vida |
| 07 Trazabilidad | Requisito → historia → casos → pruebas → estado |
| 08 Bitácora | Una fila por responsable y día de pruebas |
| 09 Tablero | Indicadores por ciclo, cobertura por módulo y defectos |
| 10 Listas | Valores permitidos, severidad, prioridad y matriz |

## 9. Métricas e indicadores

| Indicador | Fórmula | Uso |
|---|---|---|
| Avance de ejecución | Casos ejecutados / casos planeados | Seguimiento por sprint |
| Tasa de aprobación | Aprobados / (ejecutados − No aplica) | Criterio de salida y de liberación |
| Casos Alta ejecutados | Casos Alta con resultado / casos Alta | Criterio de liberación |
| Cobertura de requisitos | Requisitos verificados / 38 | Trazabilidad |
| Cobertura de código | Líneas cubiertas en domain + application (JaCoCo) | RNF-022, meta ≥ 70 % |
| Densidad de defectos | Defectos confirmados / requisitos del módulo | Módulos de mayor riesgo para la regresión |
| Tasa de reapertura | Defectos reabiertos / corregidos | Calidad de las correcciones |
| Tiempo de corrección | Días entre Confirmado y Cerrado, por severidad | Cumplimiento de la atención esperada |

## 10. Protocolos no funcionales

### 10.1 Accesibilidad

Meta WCAG 2.1 AA y RNF-028 (Lighthouse ≥ 90, texto base ≥ 16 px, botones ≥ 44 × 44 px). Francisco revisa con axe DevTools y Lighthouse las pantallas de cliente, tienda y administrador y diligencia el anexo G.

### 10.2 Móvil y navegadores

| Dispositivo / navegador | Ancho | Qué se verifica |
|---|---|---|
| Android (Chrome) | 360–412 px | Publicar, responder, elegir, sin desplazamiento horizontal |
| iPhone (Safari) | 390 px | Lo mismo e instalación de la PWA |
| Escritorio (Chrome, Edge, Firefox) | 1280 px | Panel de tienda y de administración |

### 10.3 Rendimiento

Guion k6 en `scripts/` con 50 usuarios concurrentes (RNF-001, p95 ≤ 500 ms), 100 usuarios y 50 conexiones SSE (RNF-020, error < 1 %), latencia de difusión (RNF-002) y consumo de memoria con `docker stats` (RNF-004). Se ejecuta en el VPS en C3; una línea base local puede correrse en C2.

### 10.4 Seguridad

Trivy y OWASP Dependency-Check en cada pull request (RNF-011), escaneo de secretos (RNF-030), pruebas de autorización por objeto en la API (RNF-007), sesión JWT y cookie (RNF-008), límites de uso (RNF-009), validación de archivos (RNF-012), auditoría (RNF-013) y SSL Labs en producción (RNF-010).

## 11. Riesgos de la campaña

| Riesgo | Probabilidad | Mitigación |
|---|---|---|
| No hay pruebas unitarias escritas al cerrar el Sprint 1 | Alta | Corte del 08/11 con lo que exista; en el Sprint 2 cada pull request exige pruebas (definición de terminado) |
| El tiempo no alcanza para automatizar E2E y aceptación | Alta | E2E limitado a 3 flujos obligatorios en C3; aceptación solo de historias Must |
| El ambiente del VPS se cae o se despliega a mitad de un ciclo | Media | Ventana de despliegue acordada y notas de versión en cada despliegue |
| Los desarrolladores no etiquetan las pruebas con `@HU`/`@CP` | Media | QA revisa las etiquetas en cada pull request antes de aprobar |
| Cobertura por debajo del 70 % | Media | `jacoco:check` en el pipeline hace fallar el build |
| Solo dos personas en QA para 120 casos | Media | La suite automatizada ejecuta la mayoría; QA registra y revisa, los desarrolladores apoyan las re-pruebas |
| Datos de prueba contaminados | Baja | Restaurar la línea base |

## 12. Procedimiento de liberación 1.0

1. **Congelamiento** (25/11): después del comité solo entran correcciones aprobadas; la versión aprobada se etiqueta `v1.0.0`.
2. **Respaldo de producción**: respaldo completo de la base de datos y de los archivos subidos, verificado.
3. **Despliegue** en producción con Docker Compose en la ventana acordada.
4. **Pruebas de humo** (anexo H) por Yanimar y Francisco; si alguna falla, reversión inmediata a la versión anterior.
5. **Monitoreo** durante la evaluación: disponibilidad (RNF-018), registros y defectos reportados; parches `v1.0.x` con el mismo flujo de defectos.

## 13. Hallazgos de la preparación

Al 07/11/2026 el repositorio tiene la estructura de los 10 módulos y una sola prueba (`QuienLoTieneApplicationTests`). El `pom.xml` aún no tiene JaCoCo ni Failsafe. Se registran como punto de partida del C1 y se resuelven en el acta de inicio.

## Anexos

| Anexo | Plantilla |
|---|---|
| A. Formato de reporte de defecto | `plantillas/REPORTE-DE-DEFECTO.md` |
| B. Acta de inicio del ciclo | `plantillas/ACTA-DE-INICIO.md` |
| C. Informe de corte de sprint | `plantillas/INFORME-DE-CORTE.md` |
| D. Informe de cierre de ciclo | `plantillas/INFORME-DE-CIERRE-DE-CICLO.md` |
| E. Acta de liberación (go / no-go) | `plantillas/ACTA-DE-LIBERACION.md` |
| F. Hoja de sesión exploratoria | `plantillas/SESION-EXPLORATORIA.md` |
| G. Lista de verificación de accesibilidad | `plantillas/CHECKLIST-ACCESIBILIDAD.md` |
| H. Pruebas de humo posteriores al despliegue | `plantillas/PRUEBAS-DE-HUMO.md` |
