Crea una presentación de diapositivas en español para sustentar ante el profesor el plan del proyecto de software **¿Quién lo tiene?**. La exponen 7 personas, así que cada diapositiva debe mostrar de forma visible el nombre y el rol de quien la expone. El profesor, Andres Felipe Arboleda, va a preguntar mucho por qué elegimos cada tecnología y por qué no otra, así que las diapositivas de justificación son las más importantes: cada una debe decir qué elegimos, por qué, y qué alternativas descartamos y por qué.

## Datos generales

- Proyecto: ¿Quién lo tiene?, una aplicación web adaptada al celular (PWA) donde el cliente publica el producto que busca y las tiendas cercanas (farmacias y ferreterías) responden si lo tienen, a qué precio y si hacen domicilio.
- Equipo: Byte Force.
- Universidad: Fundación Universitaria de Popayán, Facultad de Ingeniería de Sistemas.
- Asignatura: Ingeniería de Software III. Docente: Andres Felipe Arboleda.
- Entrega final del MVP: 26 de noviembre de 2026.
- Logo: adjunto el escudo de la FUP. Úsalo en la portada y pequeño en una esquina de las demás diapositivas.

## Estilo

- Formato 16:9, sobrio y académico, con el azul del escudo de la FUP como color principal, fondo claro y un color de acento.
- Poco texto por diapositiva: frases cortas, viñetas de máximo 10 palabras, tablas simples y diagramas cuando ayuden. El detalle va en las notas del orador.
- En todas las diapositivas, en la parte inferior, una franja o etiqueta con el texto "Expone: [Nombre] · [Rol]".
- Al inicio de cada bloque de un nuevo expositor, una diapositiva de transición corta con su nombre y el tema que presenta (opcional si la presentación queda muy larga).
- Cada diapositiva debe tener notas del orador con lo que la persona debe decir (3 a 6 frases) y una posible pregunta del profesor con su respuesta.
- Las diapositivas de "¿por qué X?" deben tener la misma estructura: a la izquierda "Por qué lo elegimos" (3 a 5 razones), a la derecha "Alternativas que descartamos" (tabla de 2 columnas: alternativa y motivo), y abajo una frase de cierre en negrita.

## Expositores y roles

| Integrante | Rol |
|---|---|
| Daniel Montenegro Almendra | Arquitecto de Software / Developer (backend) |
| Brayan Grajales Escarraga | Líder de Proyecto · Scrum Master |
| Monica Anyi Cajas Mellizo | UX Designer · Developer (frontend) |
| Yanimar Burbano Piamba | QA (pruebas funcionales) |
| Wilson Javier Galindez Certuche | Documentador · Product Owner |
| Francisco José Castro Muñoz | QA (pruebas no funcionales y seguridad) |
| Deiner Stiven Ramírez Vente | Developer (backend) |

## Diapositivas

### 1. Portada · Expone: Brayan Grajales Escarraga (Scrum Master)
Logo de la FUP, título "¿Quién lo tiene? Plan de proyecto de software", subtítulo "Ciclo de vida, roles, requerimientos, metodología y tecnologías", equipo Byte Force, los 7 integrantes, asignatura, docente y fecha.

### 2. Agenda y equipo · Expone: Brayan Grajales Escarraga (Scrum Master)
Agenda en 6 bloques (problema, requerimientos, metodología, arquitectura y tecnologías, calidad, plan). Al lado, el equipo con foto o ícono, nombre y rol. Mencionar que Wilson es el Product Owner y Brayan el Scrum Master, con roles separados como recomienda la guía de Scrum.

### 3. El problema · Expone: Wilson Javier Galindez Certuche (Product Owner)
- Para conseguir un producto hay que llamar o visitar varias tiendas, una por una.
- No existe un lugar donde preguntar una sola vez y comparar precio y distancia.
- Las tiendas pequeñas no tienen inventario digital y pierden ventas.

### 4. La solución · Expone: Wilson Javier Galindez Certuche (Product Owner)
Diagrama del flujo principal en 6 pasos: el cliente publica lo que busca → el sistema avisa a tiendas abiertas en 3 km → cada tienda responde "Sí lo tengo", "Tengo algo parecido" o "No lo tengo" → el cliente compara por precio o cercanía y elige → recoge o pide domicilio → confirma y califica. Indicar qué queda fuera del MVP: pagos dentro de la app, plan de pago para tiendas, supresión de cuenta e inicio de sesión sin contraseña.

### 5. Requerimientos según estándares · Expone: Wilson Javier Galindez Certuche (Product Owner)
- Cifras grandes: 12 requerimientos de negocio, 42 funcionales, 30 no funcionales, 9 épicas, 38 historias de usuario, 85 casos de prueba.
- Estándares: ISO/IEC/IEEE 29148:2018 (ingeniería de requisitos), IEEE 830 (estructura del SRS) e ISO/IEC 25010 (calidad del producto).
- Priorización MoSCoW de los 42 funcionales: 22 Must, 11 Should, 5 Could, 4 Won't (gráfico de barras o dona).
- Cada requisito se validó con 8 criterios: correcto, completo, consistente, no ambiguo, verificable, priorizado, rastreable y factible.
- Ejemplo de historia de usuario: "Como cliente quiero publicar lo que busco para que las tiendas cercanas me digan si lo tienen".

### 6. ¿Por qué Scrum + DevOps? · Expone: Brayan Grajales Escarraga (Scrum Master)
Matriz comparativa (tabla o gráfico de barras horizontales) con la puntuación sobre 40, evaluando escalabilidad, tiempo de entrega, complejidad, riesgos, tamaño del equipo, presupuesto, cambios esperados y necesidades del negocio: Híbrido Scrum + DevOps 38, Scrum 35, Scrumban 34, Kanban 32, Incremental 31, DevOps solo 29, Evolutivo 25, Espiral 23, RUP 21, Cascada 18.

Por qué lo elegimos:
- Plazo fijo de 4,5 semanas: sprints de una semana dan 4 entregas funcionando y 4 puntos de control.
- El mayor riesgo es no terminar: cada semana se puede recortar lo Should y Could a tiempo.
- 7 personas es un tamaño ideal para un equipo Scrum (la guía recomienda 10 o menos).
- El alcance todavía cambia (rúbrica, roles): Scrum absorbe cambios en el backlog sin rehacer el plan.
- DevOps: el pipeline de CI/CD y el despliegue en el servidor desde la primera semana evitan sorpresas el día de la entrega.

Alternativas descartadas:
- Cascada (18/40): exige todo el análisis antes de construir; un cambio tardío obliga a rehacer fases y no hay software funcionando hasta el final.
- RUP y Espiral: demasiada documentación y ciclos de análisis de riesgo para 4,5 semanas.
- Kanban o Scrumban solos: sin iteraciones con fecha fija se pierde el ritmo de entregas frente a una fecha límite. Sí usamos un tablero Kanban dentro de cada sprint.

Frase de cierre: "Elegimos el modelo que nos da software funcionando cada semana, no el que más documentos produce".

### 7. Plan de sprints y ceremonias · Expone: Brayan Grajales Escarraga (Scrum Master)
Línea de tiempo:
- Sprint 0 (26/10 al 01/11): SRS, UML, roles, repositorio, CI/CD, servidor y estructura inicial del backend y del frontend.
- Sprint 1 (02/11 al 08/11): identidad, registro y aprobación de tiendas, publicación de solicitudes y difusión por radio.
- Sprint 2 (09/11 al 15/11): respuestas, elección, órdenes con estados y notificaciones en tiempo real.
- Sprint 3 (16/11 al 22/11): reseñas, reportes, panel de administración, WhatsApp y mini chat.
- Cierre (23/11 al 26/11): pruebas finales, documentación y ensayo de la demo.
Ceremonias: planning el lunes (1 h), daily de 15 min, review y retrospectiva el domingo. Tablero en GitHub Projects. Definición de terminado: código revisado por pull request, pruebas pasando, criterios de aceptación verificados y desplegado.

### 8. Arquitectura: monolito modular · Expone: Daniel Montenegro Almendra (Arquitecto de Software)
Diagrama: un solo proceso Spring Boot con 10 módulos de negocio (identidad, clientes, tiendas, catálogo, solicitudes, órdenes, mensajería, reputación, notificaciones, administración), cada uno con Clean Architecture por dentro (api, domain, application, infrastructure), una base de datos PostgreSQL con un esquema por módulo, y Caddy al frente.

Por qué lo elegimos:
- El costo y la simplicidad de un monolito: un solo despliegue, una base de datos, un servidor de bajo costo.
- Fronteras estrictas entre módulos: se comunican por interfaces públicas y eventos, nunca leyendo tablas ajenas; Spring Modulith lo verifica en las pruebas.
- Si un módulo necesita escalar, se puede extraer como microservicio sin reescribir el resto.

Alternativas descartadas:
- Microservicios: para un piloto de 10 a 15 tiendas agregan red, despliegues múltiples, memoria y complejidad sin resolver ningún problema real.
- Monolito tradicional por capas: con el tiempo todo depende de todo y no se puede separar.
- Kafka, RabbitMQ, Redis o Kubernetes: más memoria, operación y costo que el servidor no necesita en esta etapa.

### 9. ¿Por qué Java? · Expone: Daniel Montenegro Almendra (Arquitecto de Software)
Por qué lo elegimos:
- Tipado estático: muchos errores se detectan al compilar, no frente al usuario; muy útil en un equipo de 7 personas que toca el mismo código.
- Versiones LTS (21 y 25) con años de soporte, y Eclipse Temurin como JDK gratuito para uso comercial.
- Hilos virtuales (Java 21): permiten mantener muchas conexiones en tiempo real (SSE) abiertas con poca memoria, clave para un servidor pequeño.
- Ecosistema muy maduro para lo que necesitamos: seguridad, ORM, datos geoespaciales, pruebas con contenedores, documentación de API.
- Es uno de los lenguajes más usados en backend empresarial y lo trabajamos en la carrera.

Alternativas descartadas:
- Python (Django o FastAPI): tipado dinámico, más errores en tiempo de ejecución y menos estructura para una arquitectura modular estricta.
- JavaScript/TypeScript (Node.js o NestJS): viable, pero el ecosistema cambia muy rápido y el manejo de transacciones y del ORM es menos maduro.
- C# (.NET): técnicamente comparable, pero el equipo tiene más experiencia en Java y su ecosistema en Linux es menor.
- PHP (Laravel): pensado para peticiones cortas; las conexiones en tiempo real de larga duración son más difíciles de manejar.
- Go: muy eficiente, pero con menos herramientas para dominio rico, ORM y seguridad, y sin experiencia previa en el equipo.

Frase de cierre: "Java nos da seguridad en el código, rendimiento con poca memoria y un ecosistema probado".

### 10. ¿Por qué Spring Boot? · Expone: Deiner Stiven Ramírez Vente (Developer backend)
Por qué lo elegimos:
- Autoconfiguración: arrancamos la API en minutos, sin configurar a mano servidor, base de datos ni seguridad.
- Spring Security: autenticación con JWT, roles, límites de intentos y segundo factor para el administrador.
- Spring Data JPA + Hibernate Spatial: repositorios y consultas geoespaciales sin escribir SQL repetitivo.
- Spring Modulith: verifica automáticamente que los módulos no se salten sus fronteras y maneja los eventos entre módulos sin perderlos si la app se reinicia.
- Todo lo que necesitamos ya existe y está documentado: tiempo real (SSE), tareas programadas (vencimiento de solicitudes), migraciones con Flyway, documentación OpenAPI y pruebas con Testcontainers. Licencia Apache 2.0, gratuita.

Alternativas descartadas:
- Quarkus o Micronaut: arrancan más rápido y usan menos memoria, pero tienen menos comunidad, documentación y ejemplos; para el plazo nos conviene lo más conocido.
- Jakarta EE sin Spring: más configuración manual y menos productividad.
- Javalin o Spark: demasiado minimalistas; tendríamos que armar seguridad, datos y pruebas por nuestra cuenta.

### 11. ¿Por qué PostgreSQL + PostGIS? · Expone: Deiner Stiven Ramírez Vente (Developer backend)
Por qué lo elegimos:
- Nuestros datos son relacionales (clientes, tiendas, solicitudes, respuestas, órdenes y reseñas) y las órdenes necesitan transacciones ACID para no quedar en estados inconsistentes.
- PostGIS: la función central del producto es buscar tiendas en un radio de 3 km. PostGIS lo resuelve con una consulta y un índice espacial (GiST), con meta de 200 ms o menos con 1.000 tiendas.
- Un esquema por módulo, lo que encaja con el monolito modular.
- Funciones incluidas que nos ahorran otras herramientas: búsqueda sin tildes (unaccent) para los productos restringidos, búsqueda de texto y JSONB.
- Licencia PostgreSQL, libre y sin costo.

Alternativas descartadas:
- MySQL o MariaDB: su soporte geoespacial es más limitado que PostGIS, y MySQL pertenece a Oracle con licencia dual.
- MongoDB: nuestros datos son relacionales, y su licencia (SSPL) no es de código abierto estándar.
- SQL Server u Oracle: licencias pagas que no caben en el presupuesto.
- Firebase o Firestore: nos ata a un proveedor, cobra por uso y no tiene consultas geoespaciales por radio de forma nativa.

Frase de cierre: "Sin PostGIS tendríamos que calcular distancias a mano; con PostgreSQL lo hace la base de datos con un índice".

### 12. ¿Por qué React como PWA? · Expone: Monica Anyi Cajas Mellizo (UX Designer · Frontend)
Por qué lo elegimos:
- Componentes reutilizables: la tarjeta de respuesta, el botón Abierto/Cerrado y los formularios se diseñan una vez y se usan en todas las pantallas.
- El ecosistema más grande del frontend: librerías, documentación y soluciones para casi cualquier problema.
- PWA: se instala en el celular desde el navegador y recibe notificaciones push sin publicar en Play Store ni App Store; una sola aplicación para Android, iPhone y escritorio.
- Vite para desarrollo rápido y TanStack Query para manejar los datos del servidor y las actualizaciones en tiempo real.
- Licencia MIT, gratuita.

Alternativas descartadas:
- Angular: más pesado y con una curva de aprendizaje mayor para el plazo.
- Vue: muy bueno, pero con un ecosistema más pequeño.
- Flutter o apps nativas (Kotlin o Swift): exigen publicar en las tiendas de apps, revisión de cada versión y, en nativo, dos aplicaciones distintas.
- Next.js: el renderizado en servidor no es necesario para esta app y agregaría un servidor Node más en el VPS.
- Create React App: está abandonado; Vite es su reemplazo.

### 13. Experiencia de usuario y accesibilidad · Expone: Monica Anyi Cajas Mellizo (UX Designer · Frontend)
- Usuarios poco técnicos (dueños de tiendas): letras grandes, pocos botones, responder "Sí lo tengo" en 3 toques o menos y "No lo tengo" en 1.
- Publicar una solicitud en 60 segundos o menos.
- WCAG 2.1 AA: texto de 16 px o más, botones de 44 × 44 px o más y buen contraste; Lighthouse de accesibilidad de 90 o más.
- Espacio para 2 o 3 wireframes de las pantallas principales (publicar, ver respuestas, panel de la tienda).

### 14. Estrategia de pruebas · Expone: Yanimar Burbano Piamba (QA)
Pirámide de pruebas:
- Unitarias con JUnit y Mockito.
- Integración con Testcontainers usando la misma imagen de PostGIS (se prueban las consultas por radio contra la base real).
- Aceptación: los 85 escenarios Gherkin en español automatizados con Cucumber.
- De punta a punta: 3 a 5 flujos críticos con Playwright.
- Meta: cobertura de 70 % o más en dominio y aplicación (JaCoCo).
- Trazabilidad: cada prueba lleva la etiqueta de su historia (@HU) y de su caso de prueba (@CP).

### 15. Seguridad y requisitos no funcionales · Expone: Francisco José Castro Muñoz (QA)
- Seguridad según OWASP ASVS, OWASP API Security Top 10 y NIST SP 800-63B.
- Frase de contraseña de 15 caracteres o más, JWT de 15 minutos y segundo factor para el administrador.
- El mayor riesgo es que una tienda vea datos de un cliente que no la eligió: pruebas de autorización por objeto con meta de 0 accesos indebidos.
- Ley 1581 de 2012: política de datos y autorización explícita al registrarse.
- Rendimiento: API con p95 de 500 ms o menos con 50 usuarios, medido con k6.
- Escaneos en el pipeline: Trivy y OWASP Dependency-Check con 0 vulnerabilidades críticas.

### 16. Despliegue y DevOps · Expone: Daniel Montenegro Almendra (Arquitecto de Software)
Diagrama: GitHub → GitHub Actions (compila, prueba, verifica arquitectura y seguridad, construye imágenes Docker) → VPS Hostinger KVM 1 con Docker Compose (contenedores Caddy, API y base de datos).
- ¿Por qué Docker? El mismo entorno en todos los computadores y en el servidor; se despliega en 15 minutos o menos en un servidor limpio.
- ¿Por qué Caddy? Certificados HTTPS automáticos y gratuitos con muy poca configuración.
- ¿Por qué un VPS de bajo costo? Un monolito modular bien optimizado cabe en un servidor pequeño; el único costo del proyecto es el VPS.

### 17. Roles y responsabilidades · Expone: Brayan Grajales Escarraga (Scrum Master)
Tabla resumen de la matriz RACI: quién es responsable de backlog (Wilson), ceremonias (Brayan), arquitectura y CI/CD (Daniel), backend (Daniel y Deiner), frontend (Monica, sobre la estructura inicial de Daniel), pruebas funcionales (Yanimar), pruebas no funcionales (Francisco) y documentación (Wilson).

### 18. Conclusiones y preguntas · Expone: Wilson Javier Galindez Certuche (Product Owner)
- Cada decisión técnica responde a un requisito del negocio: tiempo real, búsqueda por radio, privacidad y bajo costo.
- Scrum + DevOps nos da software funcionando cada semana hasta el 26 de noviembre.
- Todo el stack es gratuito y de código abierto.
- Cierre: "¿Preguntas?" con el logo de la FUP y los nombres del equipo.

## Diapositivas de respaldo (anexo, para responder preguntas del profesor)

Agrega al final 4 diapositivas de respaldo con título "Respaldo" y el nombre de quien respondería, para usarlas solo si el profesor pregunta:

1. **Comparativa completa de tecnologías** (Daniel): una tabla con filas Java, Spring Boot, PostgreSQL + PostGIS, React y columnas "Elegida", "Alternativa principal", "Por qué no la alternativa".
2. **Matriz completa de ciclos de vida** (Brayan): la tabla de los 10 modelos con los 8 criterios y el total.
3. **Licencias del stack** (Deiner): todo es gratuito para uso comercial (Apache 2.0, MIT, BSD, PostgreSQL License). Tecnologías evitadas por licencia o costo: Oracle JDK (posible costo), Redis 7.4+ (cambió de licencia), MinIO (AGPLv3), Liquibase 5 (licencia no abierta), Google Maps API (cobro por uso); usamos OpenStreetMap.
4. **Matriz de trazabilidad** (Wilson): objetivos de negocio → requerimientos de negocio → épicas → historias → casos de prueba.
