# SRS: Quién lo tiene

Especificación de Requisitos de Software (SRS) de ¿Quién lo tiene?, versión 1.1 del 24/11/2026 (la 1.0 es del 26/10/2026), elaborada para el equipo Byte Force según ISO/IEC/IEEE 29148:2018 con la estructura de IEEE 830. Cubre el producto mínimo viable que se entrega el 26/11/2026: 12 requerimientos de negocio, 42 funcionales, 30 no funcionales, 9 épicas, 38 historias de usuario y 85 casos de prueba, todos aprobados por el equipo. La versión 1.1 incorpora las solicitudes de cambio aprobadas durante la construcción (SC-001 a SC-023); el detalle está en la sección «Control de cambios de la SRS», al final del documento.

## 1. Introducción

¿Quién lo tiene? es una aplicación web adaptada al celular en la que un cliente publica lo que busca y las tiendas cercanas responden si lo tienen, a qué precio y si hacen domicilio. Este documento especifica qué debe hacer el sistema y con qué calidad, sin fijar cómo se implementa; las decisiones de diseño están en el documento de arquitectura (`docs/arquitectura/Arquitectura-de-software-QuienLoTiene.docx`).

Organización: las secciones 1 a 8 describen el contexto, los usuarios y los objetivos; las secciones 9 a 11 contienen los requerimientos; las secciones 12 a 15 los traducen a épicas, historias, criterios de aceptación y prioridades; las secciones 16 a 21 cubren el ciclo de vida, la trazabilidad, los riesgos, las restricciones, los supuestos y las recomendaciones. El Anexo A registra la validación de calidad y el Anexo B los diagramas UML.

## 2. Propósito

El propósito de este SRS es definir de forma completa, verificable y rastreable los requisitos del producto mínimo viable de ¿Quién lo tiene?, para que sirva de acuerdo entre el equipo y el evaluador y de base para planificar, construir y probar el sistema.

Lectores previstos: el equipo Byte Force (7 personas) como guía de desarrollo y pruebas, el profesor de Ingeniería del Software 3 como evaluador, y futuros interesados (tiendas piloto o inversionistas) como descripción del producto.

## 3. Alcance

Producto: ¿Quién lo tiene?, versión MVP 1.0, con API REST en Java + Spring Boot y frontend React como aplicación web progresiva (PWA). Se demuestra con datos de prueba el 26/11/2026 en un VPS de producción.

Dentro del alcance:

- Registro e inicio de sesión de clientes, tiendas y administrador, con autorización de tratamiento de datos.
- Publicación de solicitudes y difusión a tiendas abiertas de la categoría en un radio de 3 km.
- Respuestas Sí lo tengo, Tengo algo parecido y No lo tengo, con comparación por precio y cercanía.
- Órdenes para recoger o con domicilio hecho por la tienda, con estados, desglose del total y registro del medio de pago.
- Notificaciones en tiempo real, botón de WhatsApp y mini chat de texto.
- Reseñas, reportes de problemas y panel de administración (aprobación de tiendas, suspensiones, categorías y productos restringidos).
- Categorías iniciales: farmacias y ferreterías.

Fuera del alcance: cobro dentro de la app, plan de pago para tiendas, red de domiciliarios propios, inicio de sesión sin contraseña, supresión de cuenta, verificación por SMS, aplicaciones nativas para tiendas de apps y piloto con tiendas reales.

Beneficio esperado: que el cliente encuentre un producto cercano preguntando una sola vez y que las tiendas pequeñas vendan sin inventario digital (objetivos en la sección 8).

## 4. Definiciones, acrónimos y abreviaturas

| Término | Definición |
| --- | --- |
| Solicitud | Publicación del cliente con el producto que busca, la cantidad, la modalidad y su ubicación. Vence a los 30 minutos. |
| Respuesta | Contestación de una tienda a una solicitud: Sí lo tengo, Tengo algo parecido o No lo tengo. |
| Orden | Compromiso entre el cliente y la tienda elegida, para recoger o con domicilio, con su propio ciclo de estados. |
| Tienda elegible | Tienda aprobada, abierta, en horario, de la categoría de la solicitud, no suspendida y dentro del radio. |
| Radio de difusión | Distancia máxima entre cliente y tienda para recibir una solicitud: 3 km, ampliable a 5 km. |
| Reserva | Tiempo durante el cual la tienda aparta el producto para recogida (30 minutos desde que la tienda marca «Listo para recoger», SC-019). |
| Producto restringido | Medicamento de venta con fórmula médica; solo se consulta disponibilidad. |
| MVP | Producto mínimo viable. |
| PWA | Aplicación web progresiva: web instalable en el celular que puede recibir notificaciones. |
| SSE | Server-Sent Events: canal HTTP por el que el servidor envía eventos en tiempo real al navegador. |
| Push web / VAPID | Notificaciones del navegador con la app cerrada; VAPID identifica al servidor ante el servicio de push. |
| JWT | JSON Web Token, credencial de acceso de corta duración. |
| TOTP | Código de un solo uso basado en tiempo, usado como segundo factor. |
| PostGIS | Extensión geoespacial de PostgreSQL usada para las búsquedas por radio. |
| VPS | Servidor privado virtual; en este proyecto, Hostinger KVM 1. |
| CI/CD | Integración y despliegue continuos. |
| DTO | Objeto de transferencia de datos que expone solo los campos permitidos a cada rol. |
| MoSCoW | Priorización en Must, Should, Could y Won't Have. |
| Gherkin | Lenguaje Dado / Cuando / Entonces para criterios de aceptación ejecutables. |
| SRS | Especificación de Requisitos de Software. |
| OB, RN, RF, RNF | Objetivo de negocio, requerimiento del negocio, requerimiento funcional y requerimiento no funcional. |
| EP, HU, CP | Épica, historia de usuario y caso de prueba. |
| COP | Peso colombiano. |

## 5. Referencias

- ISO/IEC/IEEE 29148:2018, Systems and software engineering, Life cycle processes, Requirements engineering.
- IEEE Std 830-1998, Recommended Practice for Software Requirements Specifications (referencia histórica de estructura).
- ISO/IEC 25010, Systems and software Quality Requirements and Evaluation (SQuaRE), modelo de calidad del producto.
- IEEE Computer Society, SWEBOK, Guide to the Software Engineering Body of Knowledge.
- Schwaber y Sutherland, The Scrum Guide (2020).
- Ley Estatutaria 1581 de 2012 de Colombia, protección de datos personales.
- NIST SP 800-63B, Digital Identity Guidelines: Authentication and Lifecycle Management.
- OWASP Application Security Verification Standard (ASVS) y OWASP API Security Top 10.
- W3C, Web Content Accessibility Guidelines (WCAG) 2.2 (SC-004; la versión 1.0 citaba la 2.1).
- IETF RFC 9457, Problem Details for HTTP APIs.
- Equipo Byte Force, "¿Quién lo tiene? Idea de negocio ampliada", Ingeniería del Software 3.
- Equipo Byte Force. (2026). *Arquitectura de software del sistema ¿Quién lo tiene?* Fundación Universitaria de Popayán.

## 6. Descripción general

### 6.1 Perspectiva del producto

Es un producto nuevo e independiente: una PWA en React que consume una API REST hecha como monolito modular en Spring Boot, con PostgreSQL + PostGIS, desplegada con Docker Compose en un solo VPS. Se relaciona con sistemas externos que no controla: el servicio de push de cada navegador, las teselas de OpenStreetMap, WhatsApp (enlaces wa.me) y la app de mapas del teléfono. El diagrama de contexto y el de despliegue están en el Anexo B.

### 6.2 Problema que resuelve

- Para conseguir un producto hay que llamar o visitar varias tiendas, una por una, con pérdida de tiempo y dinero en transporte.
- No existe un lugar donde preguntar una sola vez y comparar precio y distancia.
- Las tiendas pequeñas no tienen inventario digital, así que no aparecen en catálogos en línea.
- Las tiendas pierden ventas porque los clientes no saben que tienen lo que buscan.

### 6.3 Proceso actual y proceso propuesto

Hoy el cliente llama, escribe o se desplaza a varias tiendas hasta encontrar el producto, y las tiendas pequeñas llevan su inventario en cuaderno o de memoria.

Con el sistema, el proceso es:

1. El cliente publica lo que busca y comparte su ubicación.
2. El sistema envía la solicitud a las tiendas elegibles en 3 km.
3. Cada tienda responde Sí lo tengo, Tengo algo parecido o No lo tengo.
4. El cliente compara las respuestas por precio o cercanía y elige una.
5. Si recoge, la tienda aparta el producto; si pide domicilio, confirma dirección y medio de pago.
6. La tienda actualiza el estado hasta la entrega y el pago contra entrega.
7. El cliente confirma la recepción y califica a la tienda.

### 6.4 Funciones del producto

| Épica | Función principal |
| --- | --- |
| EP-001 Identidad y acceso | Registro, inicio de sesión, roles y autorización de datos |
| EP-002 Perfil del cliente | Datos, direcciones e historial |
| EP-003 Gestión de la tienda | Perfil, horario, domicilio, medios de pago y Abierto / Cerrado |
| EP-004 Solicitudes y respuestas | Publicar, difundir por radio, responder y comparar |
| EP-005 Órdenes y entrega | Elegir, reservar, seguir estados y cerrar |
| EP-006 Comunicación y notificaciones | Tiempo real, push web, WhatsApp y mini chat |
| EP-007 Reputación | Reseñas y reportes |
| EP-008 Administración | Aprobación de tiendas, sanciones y catálogo |

### 6.5 Características de los usuarios

| Usuario | Perfil | Implicación para el diseño |
| --- | --- | --- |
| Cliente | Persona con celular y conocimientos básicos de apps | Publicar en menos de 60 segundos, sin pasos innecesarios |
| Tienda | Dueño o vendedor de farmacia o ferretería, a menudo poco técnico | Letras grandes, pocos botones, responder en 3 toques o menos |
| Administrador | Miembro del equipo con conocimientos técnicos | Panel funcional con segundo factor y auditoría |

### 6.6 Entorno operativo

- Servidor: VPS Hostinger KVM 1 con Linux, Docker Engine y Docker Compose; contenedores Caddy (proxy y TLS), API Spring Boot y PostgreSQL + PostGIS.
- Clientes: navegadores Chrome, Edge, Firefox y Safari en sus dos últimas versiones, Android 10 o superior e iOS 16.4 o superior.
- Zona horaria America/Bogota, idioma español de Colombia y moneda COP.

### 6.7 Decisiones acordadas con el equipo

- Ciclo de vida híbrido Scrum con sprints de una semana y prácticas DevOps.
- Gratis en esta entrega; plan de pago para tiendas y cobro dentro de la app son visión futura.
- La tienda configura sus medios de pago y el cliente elige uno; el sistema solo lo registra.
- Inicio de sesión con frase de contraseña de mínimo 15 caracteres; el administrador usa además TOTP.
- Botón de WhatsApp (Must) y mini chat de solo texto (Should).
- Ley 1581: política de tratamiento de datos y casilla de autorización al registrarse.
- Valores operativos: solicitudes que vencen a los 30 minutos, ampliación de 3 a 5 km a los 10 minutos sin respuestas y máximo 10 solicitudes por hora por cliente.

## 7. Stakeholders

| Stakeholder | Tipo | Interés principal |
| --- | --- | --- |
| Cliente | Usuario | Encontrar rápido un producto cercano y comparar precio y distancia |
| Tienda (dueño o vendedor) | Usuario | Recibir clientes con intención de compra sin cargar inventario; interfaz sencilla |
| Administrador | Usuario | Verificar tiendas, atender reportes, suspender cuentas, mantener productos restringidos |
| Equipo Byte Force (7 personas) | Desarrollo | Entregar el MVP completo a tiempo; roles pendientes de confirmar |
| Profesor de Ingeniería del Software 3 | Evaluador | Documentación alineada con normas, diagramas UML, app y pruebas funcionando |
| Titulares de datos personales | Regulatorio | Tratamiento de datos conforme a la Ley 1581 de 2012 |

## 8. Objetivos de negocio

| ID | Objetivo |
| --- | --- |
| OB-01 | Reducir el tiempo y el costo que gasta un cliente para encontrar un producto en tiendas cercanas. |
| OB-02 | Dar a las tiendas pequeñas un canal de ventas sin exigirles inventario digital. |
| OB-03 | Construir confianza entre clientes y tiendas mediante verificación, reseñas y reportes. |
| OB-04 | Proteger los datos personales de los clientes (Ley 1581 de 2012). |
| OB-05 | Validar la idea con un producto mínimo viable funcional, a bajo costo, antes del 26/11/2026. |

Métricas de éxito del piloto futuro (del documento de negocio): tiempo promedio de respuesta de las tiendas, porcentaje de solicitudes con al menos una respuesta, solicitudes resueltas y calificación promedio. Metas aprobadas para el piloto: 70 % o más de solicitudes con al menos una respuesta y tiempo medio de respuesta de 10 minutos o menos.

## 9. Requerimientos del negocio

| ID | Nombre | Descripción | Justificación | Valor para el negocio | Objetivo |
| --- | --- | --- | --- | --- | --- |
| RN-001 | Consulta única de disponibilidad | El cliente pregunta una sola vez por un producto y la consulta llega a varias tiendas cercanas. | Hoy el cliente llama o visita tiendas una por una. | Ahorra tiempo y transporte al cliente; es la propuesta de valor central. | OB-01 |
| RN-002 | Comparación de ofertas | El cliente compara las respuestas por precio, distancia, domicilio y calificación antes de decidir. | No existe un lugar donde comparar tiendas locales. | Decisiones de compra informadas y competencia sana entre tiendas. | OB-01 |
| RN-003 | Participación sin inventario digital | La tienda participa confirmando disponibilidad en el momento, sin cargar catálogo. | Las tiendas pequeñas no tienen inventario digital. | Barrera de entrada mínima; información siempre al día. | OB-02 |
| RN-004 | Modalidades de entrega | El cliente recoge en la tienda o pide domicilio, que hace la propia tienda. | Ambas modalidades son comunes en el comercio local. | Más ventas cerradas sin que la plataforma opere repartidores. | OB-01, OB-02 |
| RN-005 | Tiendas verificadas | Solo aparecen tiendas cuyos documentos aprobó un administrador. | Evitar fraudes y tiendas falsas. | Confianza del cliente en las respuestas. | OB-03 |
| RN-006 | Reputación basada en pedidos reales | Solo califica quien resolvió una orden con la tienda; la calificación y el tiempo de respuesta son visibles. | Las reseñas falsas destruyen la confianza. | Premia a las tiendas cumplidas con más visibilidad. | OB-03 |
| RN-007 | Gestión de incidentes y sanciones | Clientes y tiendas reportan problemas y el administrador puede suspender cuentas. | La tienda debe respetar precio, disponibilidad y domicilio ofrecidos. | Reduce incumplimientos y protege la reputación de la plataforma. | OB-03 |
| RN-008 | Privacidad del cliente | La tienda no ve nombre, teléfono ni dirección del cliente hasta ser elegida, y deja de ver la dirección tras la entrega. | Ley 1581 de 2012 y principio de minimización. | Cumplimiento legal y confianza del cliente. | OB-04 |
| RN-009 | Restricción de medicamentos con fórmula | Los medicamentos de venta con fórmula médica solo muestran disponibilidad y un aviso; no se venden por la app. | Regulación farmacéutica. | Evita riesgos legales para tiendas y plataforma. | OB-03, OB-04 |
| RN-010 | Pago contra entrega sin intermediación | La app registra el medio de pago elegido y el total, pero no mueve dinero. | Construir pagos exige contratos y permisos financieros. | Producto viable sin costos ni riesgos financieros. | OB-05 |
| RN-011 | Operación de bajo costo | El sistema opera en un solo VPS de bajo costo con herramientas gratuitas. | Presupuesto limitado al VPS (COP 57.900). | Viabilidad económica del MVP y del piloto. | OB-05 |
| RN-012 | Medición de indicadores | El sistema registra tiempo de respuesta, tasa de respuesta, solicitudes resueltas y calificación. | Son las métricas del plan de arranque. | Permite decidir si la idea funciona y si se escala. | OB-05 |

## 10. Requerimientos funcionales

Resumen MoSCoW: 22 Must, 11 Should, 5 Could y 4 Won't. Los actores son Cliente, Tienda, Administrador y Sistema (procesos automáticos).

### 10.1 Identidad y cuentas

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-001 | Registrar cliente | El sistema permite crear una cuenta de cliente con nombre, correo, teléfono y frase de contraseña. | Cliente | No tiene cuenta con ese correo. | Cuenta activa con rol CLIENTE. | Must | Un registro válido crea la cuenta; un correo repetido se rechaza con mensaje claro. |
| RF-002 | Registrar tienda | El sistema permite registrar una tienda con nombre, categoría, dirección, teléfono, horario, ubicación en el mapa, foto del local y documento de existencia (RUT o registro mercantil). | Tienda | No tiene cuenta con ese correo. | Tienda en estado Pendiente de aprobación; no recibe solicitudes. | Must | La tienda queda Pendiente y no aparece en difusiones hasta ser aprobada. |
| RF-003 | Iniciar y cerrar sesión | El sistema autentica con correo y frase de contraseña y permite cerrar la sesión. | Cliente, Tienda, Administrador | Cuenta existente y no suspendida. | Sesión activa con el rol correspondiente, o cerrada. | Must | Credenciales correctas dan acceso; incorrectas o cuenta suspendida lo niegan. |
| RF-004 | Segundo factor del administrador | El sistema exige un código TOTP al administrador después de la contraseña. | Administrador | Administrador con TOTP configurado. | Sesión de administrador activa. | Should | Sin código válido no se concede acceso al panel de administración. |
| RF-005 | Aceptar política de datos | El registro muestra la política de tratamiento de datos y exige marcar la autorización explícita. | Cliente, Tienda | Formulario de registro abierto. | Autorización guardada con fecha y versión de la política. | Must | No es posible registrarse sin marcar la casilla; la autorización queda registrada. |
| RF-006 | Verificar correo | El sistema envía un enlace para confirmar el correo del usuario. | Cliente, Tienda | Cuenta recién creada. | Correo marcado como verificado. | Won't (SC-021) | Sale de esta entrega por SC-021: necesita un servidor de correo que no está configurado. |

### 10.2 Cliente

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-007 | Gestionar perfil y direcciones | El cliente edita sus datos y guarda direcciones con pin en el mapa, dirección escrita, indicaciones y teléfono de contacto. | Cliente | Sesión de cliente activa. | Perfil y direcciones actualizados. | Must | Una dirección guardada aparece disponible al pedir domicilio. |
| RF-008 | Consultar historial y repetir | El cliente ve sus solicitudes y órdenes anteriores y puede repetir una solicitud. | Cliente | Al menos una solicitud previa. | Nueva solicitud con los datos de la anterior. Incluye «Mis preguntas»: las solicitudes abiertas de las últimas 24 horas con cuántas respuestas tiene cada una (SC-018). | Could | Repetir crea una solicitud con el mismo producto y cantidad. |

### 10.3 Tiendas

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-009 | Configurar perfil de tienda | La tienda edita nombre, fotos, horario, teléfono y ubicación. | Tienda | Tienda aprobada. | Perfil actualizado y visible para clientes. | Must | Los cambios de horario se reflejan en la elegibilidad para recibir solicitudes. |
| RF-010 | Configurar domicilio con tarifa fija | La tienda indica si hace domicilio, su radio de cobertura y una tarifa fija. | Tienda | Tienda aprobada. | Configuración de domicilio guardada. | Must | La tarifa configurada aparece en las respuestas y en el desglose del total. |
| RF-011 | Tarifas de domicilio avanzadas | La tienda elige domicilio gratis desde un monto de compra o tarifa por distancia. | Tienda | RF-010 configurado. | Estrategia de tarifa guardada. | Won't (SC-021) | Sale de esta entrega por SC-021; la tarifa fija de RF-010 cubre el domicilio. |
| RF-012 | Configurar medios de pago | La tienda activa los medios que acepta: efectivo, transferencia o datáfono contra entrega. | Tienda | Tienda aprobada. | Medios de pago guardados. | Must | El cliente solo puede elegir medios que la tienda tenga activos. |
| RF-013 | Abrir o cerrar tienda | Un interruptor Abierto / Cerrado controla si la tienda recibe solicitudes nuevas. El panel muestra además la categoría de la tienda y un enlace a su ubicación (SC-017). | Tienda | Tienda aprobada. | Estado de apertura actualizado. | Must | Una tienda cerrada no recibe solicitudes nuevas. |
| RF-014 | Ver detalle de tienda | El cliente ve fotos, horario, ubicación, teléfono, reseñas y medios de pago de una tienda. | Cliente | La tienda respondió una solicitud del cliente. | Ninguna. | Should | La ficha muestra todos los datos públicos y ningún dato privado. |
| RF-015 | Ver resumen de indicadores | La tienda ve solicitudes recibidas, respondidas y ganadas, tiempo medio de respuesta y calificación promedio. | Tienda | Tienda aprobada. | Ninguna. | Could | Los valores coinciden con los datos de prueba cargados. |

### 10.4 Solicitudes y respuestas

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-016 | Publicar solicitud | El cliente publica producto, cantidad, categoría, foto opcional, modalidad (recoger o domicilio), marca de urgencia y su ubicación. | Cliente | Sesión de cliente activa y límite de solicitudes por hora no superado. | Solicitud en estado Abierta que vence a los 30 minutos (valor configurable por el administrador). | Must | Una solicitud válida queda Abierta; sin producto o sin ubicación se rechaza. |
| RF-017 | Difundir a tiendas elegibles | El sistema envía la solicitud a tiendas aprobadas, abiertas, en horario, de la categoría, no suspendidas y dentro de 3 km. | Sistema | Solicitud Abierta. | Cada tienda elegible tiene la solicitud en su panel. | Must | Con datos de prueba, solo las tiendas que cumplen todas las condiciones la reciben. |
| RF-018 | Ver solicitudes nuevas | La tienda ve las solicitudes con producto, cantidad, foto, distancia aproximada, modalidad, urgencia y tiempo restante, sin datos personales del cliente. | Tienda | Tienda aprobada y abierta. | Ninguna. | Must | La respuesta de la API no contiene nombre, teléfono ni dirección del cliente. |
| RF-019 | Responder solicitud | La tienda responde Sí lo tengo (precio, cantidad disponible, que puede ser menor que la pedida, nota, domicilio, costo y tiempo), Tengo algo parecido (con descripción de la alternativa) o No lo tengo (un toque). | Tienda | Solicitud Abierta y no respondida por esa tienda. | Respuesta registrada; con No lo tengo, la solicitud desaparece de su panel. | Must | Cada tipo exige sus campos; una segunda respuesta de la misma tienda se rechaza. |
| RF-020 | Ver y ordenar respuestas | El cliente ve las respuestas en tarjetas a medida que llegan y las ordena por precio o cercanía; las parecidas aparecen marcadas como alternativa y, si la tienda ofrece menos unidades, la tarjeta muestra por ejemplo "2 de 3 disponibles". | Cliente | Solicitud propia Abierta. | Ninguna. | Must | Una respuesta nueva aparece sin recargar la página; el orden cambia según el criterio. |
| RF-021 | Vencer y cerrar solicitudes | El sistema vence las solicitudes sin elección al cumplirse su tiempo y, cuando el cliente elige, cierra la solicitud y avisa a las demás tiendas. | Sistema | Solicitud Abierta. | Solicitud Vencida o Resuelta. | Must | Pasado el tiempo, la solicitud no acepta respuestas; las tiendas no elegidas ven su respuesta como no elegida. |
| RF-022 | Ampliar radio automáticamente | Si nadie responde en 10 minutos, el sistema amplía el radio una sola vez, de 3 km a 5 km, y avisa al cliente. | Sistema | Solicitud Abierta sin respuestas. | Radio ampliado y nuevas tiendas notificadas. | Could | Sin respuestas tras el tiempo, tiendas del radio mayor reciben la solicitud. |
| RF-023 | Advertir productos restringidos | Si el producto coincide con una palabra clave de la lista de restringidos (sin distinguir mayúsculas ni tildes), la solicitud solo consulta disponibilidad y muestra un aviso de consultar con la farmacia. | Sistema | Lista de productos restringidos cargada. | Solicitud marcada como restringida; no se permite pedir domicilio. | Should | Un producto de la lista muestra el aviso y bloquea la opción de domicilio. |

### 10.5 Órdenes

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-024 | Elegir respuesta y crear orden | El cliente elige una respuesta, la modalidad, la dirección si es domicilio y un medio de pago, y ve el desglose producto + domicilio = total. | Cliente | Solicitud propia Abierta con al menos una respuesta Sí o Parecido. | Orden Confirmada; la tienda elegida ve los datos del cliente necesarios. | Must | El total coincide con precio × cantidad ofrecida + domicilio; solo la tienda elegida accede a los datos del cliente. |
| RF-025 | Reservar para recogida | En recogida, la tienda aparta el producto 30 minutos, contados desde que marca la orden «Listo para recoger», y la reserva vence sola (SC-019). | Sistema | Orden de recogida Confirmada. | Reserva activa o Vencida. | Should | Pasado el tiempo sin recoger, la orden pasa a Vencida y se avisa a ambos. |
| RF-026 | Ver cómo llegar | El cliente abre la ubicación de la tienda en la app de mapas del teléfono. | Cliente | Orden de recogida Confirmada. | Ninguna. | Should | El enlace abre la ubicación correcta de la tienda. |
| RF-027 | Actualizar estado de la orden | La tienda cambia el estado solo por transiciones válidas. Domicilio: Confirmado, Preparando, En camino, Entregado. Recogida: Confirmado, Preparando, Listo para recoger, Entregado. Desde Confirmado o Preparando se puede pasar a Cancelado; el sistema pasa a Vencida una reserva no recogida. | Tienda | Orden activa de la tienda. | Nuevo estado registrado y cliente notificado. | Must | Una transición inválida (Confirmado a Entregado) se rechaza. |
| RF-028 | Confirmar recepción | El cliente marca Recibí mi pedido y la orden queda Resuelta; la dirección deja de mostrarse a la tienda. | Cliente | Orden propia en estado Entregado. | Orden Resuelta y reseña habilitada. | Must | Tras confirmar, la API ya no entrega la dirección del cliente a la tienda. |
| RF-029 | Cancelar orden | El cliente cancela la orden si la tienda aún no la despachó, y se avisa a la tienda. | Cliente | Orden en Confirmado o Preparando. | Orden Cancelada sin penalización. | Should | Cancelar en En camino se rechaza; en Preparando se acepta. |

### 10.6 Comunicación y notificaciones

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-030 | Notificar en tiempo real | Con la app abierta, el sistema envía por SSE las solicitudes nuevas a tiendas y las respuestas y cambios de estado a clientes. Las urgentes llegan primero, con alerta visual y con sonido si el usuario ya interactuó con el panel. Cada usuario puede tener hasta 5 conexiones abiertas (por ejemplo, celular y computador) y cada aviso llega a todas (SC-014). | Sistema | Usuario con sesión abierta. | Notificación mostrada. | Must | El evento aparece en la otra sesión sin recargar. |
| RF-031 | Notificaciones push web | Con la app cerrada, el sistema envía notificaciones push web (VAPID) si el usuario las autorizó; en iPhone requiere la app instalada en la pantalla de inicio (iOS 16.4 o superior). | Sistema | Suscripción push registrada. | Notificación entregada al navegador. | Should | Con la app cerrada, la tienda recibe la notificación de una solicitud nueva. |
| RF-032 | Contactar por WhatsApp | Tras crear la orden, cliente y tienda ven un botón que abre WhatsApp con el número del otro. | Cliente, Tienda | Orden activa. | Ninguna. | Must | El botón no aparece antes de la elección ni después de resolver la orden. |
| RF-033 | Mini chat | Chat de solo texto entre cliente y tienda, ligado a una orden activa, que se cierra al terminar la orden. | Cliente, Tienda | Orden activa. | Mensajes guardados en la orden. | Should | Un mensaje enviado aparece en la otra sesión sin recargar; con la orden cerrada no se puede escribir. |

### 10.7 Reputación

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-034 | Calificar tienda | El cliente califica de 1 a 5 estrellas con comentario opcional; la calificación promedio se recalcula. | Cliente | Orden propia Resuelta y sin reseña. | Reseña publicada y promedio actualizado. | Must | No es posible calificar sin orden Resuelta ni dos veces la misma orden. |
| RF-035 | Reportar problema | Cliente o tienda reportan un problema de una orden (producto distinto, demora, no llegó). | Cliente, Tienda | Orden propia existente. | Reporte Abierto para el administrador. | Should | El reporte queda visible en el panel del administrador. |

### 10.8 Administración

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-036 | Aprobar o rechazar tiendas | El administrador revisa documentos y fotos y aprueba o rechaza la tienda con un motivo. | Administrador | Tienda Pendiente. | Tienda Aprobada o Rechazada, notificada y auditada. | Must | Solo una tienda Aprobada puede recibir solicitudes. |
| RF-037 | Gestionar reportes y suspender | El administrador revisa reportes, los resuelve y puede suspender o reactivar cuentas. | Administrador | Reporte Abierto o cuenta activa. | Reporte resuelto; cuenta suspendida o reactivada y auditada. | Should | Una cuenta suspendida no puede iniciar sesión ni recibir solicitudes. |
| RF-038 | Gestionar categorías y restringidos | El administrador activa categorías y mantiene la lista de productos restringidos. | Administrador | Sesión de administrador activa. | Catálogo actualizado. | Should | Una categoría desactivada no aparece al publicar solicitudes. |

### 10.9 Fuera de esta entrega (Won't Have)

| ID | Nombre | Descripción | Actor | Precondición | Postcondición | MoSCoW | Criterio de validación |
| --- | --- | --- | --- | --- | --- | --- | --- |
| RF-039 | Pagar dentro de la app | Pago con tarjeta o cuenta en línea mediante pasarela (Wompi o PayU) con dinero retenido hasta la entrega. | Cliente | Contrato con pasarela. | Pago retenido. | Won't | Se valida en una versión futura. |
| RF-040 | Plan de pago para tiendas | Suscripción mensual para mayor visibilidad y estadísticas. | Tienda | Modelo de precios definido. | Plan activo. | Won't | Se valida en una versión futura. |
| RF-041 | Solicitar supresión de cuenta | El titular pide eliminar su cuenta y sus datos personales. | Cliente, Tienda | Cuenta activa. | Datos suprimidos o anonimizados. | Won't | Se valida en una versión futura. |
| RF-042 | Iniciar sesión sin contraseña | Acceso con un código de un solo uso enviado al correo. | Cliente, Tienda | Envío de correo configurado. | Sesión activa. | Won't | Se valida en una versión futura. |

Otras ideas futuras sin requerimiento propio: código de 4 dígitos para confirmar la entrega, verificación de teléfono por SMS y red de domiciliarios independientes.

## 11. Requerimientos no funcionales

Cada requerimiento se clasifica en la categoría pedida y, entre paréntesis, la característica de ISO/IEC 25010 a la que pertenece. Los valores numéricos son propuestas para el MVP en el VPS KVM 1 y se ajustan si la rúbrica del profesor exige otros.

| ID | Categoría | Descripción | Métrica | Criterio de aceptación |
| --- | --- | --- | --- | --- |
| RNF-001 | Rendimiento (Eficiencia de desempeño) | Tiempo de respuesta de la API en operaciones de lectura y escritura. | Percentil 95 de latencia con 50 usuarios concurrentes. | p95 menor o igual a 500 ms en prueba de carga con k6 o JMeter. |
| RNF-002 | Rendimiento (Eficiencia de desempeño) | Tiempo desde que se publica una solicitud hasta que llega al panel de las tiendas elegibles. | Segundos entre publicación y evento SSE recibido. | p95 menor o igual a 3 s. |
| RNF-003 | Rendimiento (Eficiencia de desempeño) | Consulta geoespacial de tiendas elegibles. | Tiempo de la consulta con 1.000 tiendas de prueba. | Menor o igual a 200 ms usando índice GiST. |
| RNF-004 | Rendimiento (Eficiencia de desempeño) | Consumo de memoria en el servidor. | RAM usada por los contenedores en carga normal. | API como máximo 640 MB y todo el stack como máximo 2 GB de los 4 GB del VPS. |
| RNF-005 | Rendimiento (Eficiencia de desempeño) | Carga del frontend en el celular. | Puntaje Lighthouse de rendimiento en modo móvil. | Mayor o igual a 80 y primera carga de 3 s o menos en 4G simulado. |
| RNF-006 | Seguridad (Autenticidad) | Almacenamiento y política de contraseñas según NIST SP 800-63B. | Longitud mínima, algoritmo de hash y comparación con contraseñas filtradas. | Mínimo 15 caracteres, hash Argon2id o bcrypt, rechazo de contraseñas de la lista de filtradas. |
| RNF-007 | Seguridad (Confidencialidad) | Autorización por objeto: ninguna tienda accede a datos personales de un cliente que no la eligió. | Accesos indebidos en las pruebas automatizadas de autorización. | 0 accesos indebidos; cada endpoint con datos personales tiene prueba negativa. |
| RNF-008 | Seguridad (Autenticidad) | Manejo de sesiones. | Duración del token de acceso y protección del token de refresco. | JWT de 15 minutos; refresco rotativo en cookie HttpOnly, Secure y SameSite=Strict. |
| RNF-009 | Seguridad (Resistencia) | Límites de uso contra abuso y spam. | Intentos de inicio de sesión y solicitudes permitidos por periodo. | Máximo 5 intentos de inicio de sesión por minuto por cuenta y 10 solicitudes por hora por cliente; el exceso recibe HTTP 429. |
| RNF-010 | Seguridad (Confidencialidad) | Cifrado del transporte. | Versión de TLS y calificación de SSL Labs. | Solo TLS 1.2 y 1.3, HSTS activo y calificación A o superior. |
| RNF-011 | Seguridad (Integridad) | Seguridad de dependencias e imágenes. | Vulnerabilidades críticas y altas reportadas por Trivy y OWASP Dependency-Check. | 0 críticas y 0 altas sin justificar en el build de entrega. |
| RNF-012 | Seguridad (Integridad) | Validación de archivos subidos. | Tipos, tamaño y metadatos. | Solo JPEG, PNG, WebP y PDF de hasta 5 MB, tipo verificado por contenido y metadatos EXIF eliminados. |
| RNF-013 | Seguridad (Responsabilidad) | Auditoría de acciones sensibles. | Porcentaje de aprobaciones, suspensiones, inicios de sesión y accesos a datos de clientes registrados. | 100 % registrados con usuario, fecha y acción, sin datos personales en los logs. |
| RNF-014 | Seguridad (Confidencialidad) | Cumplimiento de la Ley 1581: autorización de tratamiento. | Usuarios registrados con autorización guardada. | 100 % con fecha y versión de la política aceptada. |
| RNF-015 | Fiabilidad (Tolerancia a fallos) | No se pierden eventos de dominio ante un reinicio. | Eventos pendientes reprocesados después de reiniciar la API. | 0 eventos perdidos en la prueba de reinicio. |
| RNF-016 | Fiabilidad (Madurez) | Las máquinas de estado rechazan transiciones inválidas. | Transiciones inválidas aceptadas en las pruebas. | 0 aceptadas; todas las combinaciones probadas. |
| RNF-017 | Fiabilidad (Recuperabilidad) | Copias de seguridad y restauración. | Punto de recuperación (RPO) y tiempo de recuperación (RTO). | RPO de 24 h, RTO de 4 h y una restauración probada antes de la entrega. |
| RNF-018 | Disponibilidad (Fiabilidad) | Disponibilidad del servicio en el periodo de evaluación. | Porcentaje de tiempo en línea medido por un monitor externo. | Mayor o igual a 99 % entre el 16/11/2026 y el 26/11/2026. |
| RNF-019 | Disponibilidad (Fiabilidad) | Despliegues sin interrumpir la demo. | Tiempo fuera de servicio por despliegue. | Como máximo 60 s por despliegue. |
| RNF-020 | Escalabilidad (Flexibilidad) | Capacidad en el servidor actual. | Usuarios concurrentes y conexiones SSE abiertas. | 100 usuarios concurrentes y 50 conexiones SSE con tasa de error menor a 1 %. |
| RNF-021 | Escalabilidad (Flexibilidad) | Fronteras de módulos que permitan extraer servicios en el futuro. | Resultado de ApplicationModules.verify() de Spring Modulith. | 0 violaciones de dependencias entre módulos. |
| RNF-022 | Mantenibilidad (Capacidad de prueba) | Cobertura de pruebas automatizadas. | Cobertura de líneas medida con JaCoCo en domain y application. | Mayor o igual a 70 %. |
| RNF-023 | Mantenibilidad (Modularidad) | Respeto de la arquitectura limpia. | Violaciones detectadas por ArchUnit. | 0 violaciones en el pipeline. |
| RNF-024 | Mantenibilidad (Analizabilidad) | Documentación de la API. | Endpoints documentados en OpenAPI y errores en formato RFC 9457. | 100 % de endpoints documentados. |
| RNF-025 | Compatibilidad (Interoperabilidad) | API REST versionada. | Formato y versionado. | JSON sobre HTTPS con prefijo /api/v1. |
| RNF-026 | Compatibilidad (Coexistencia) | Navegadores y dispositivos soportados. | Pruebas manuales en la matriz de navegadores. | Funciona en las dos últimas versiones de Chrome, Edge, Firefox y Safari, en Android 10+ e iOS 16.4+. |
| RNF-027 | Usabilidad (Operabilidad) | Rapidez para publicar y responder. | Tiempo y toques medidos en prueba con 5 usuarios. Estado (SC-022 y SC-023): los toques y el manejo de errores se verificaron en la prueba de usabilidad de caja negra del 23/11/2026; el tiempo con usuarios reales se mide en las sesiones con personas. | Publicar una solicitud en 60 s o menos; responder Sí en 3 toques o menos; No lo tengo en 1 toque. |
| RNF-028 | Usabilidad (Accesibilidad) | Accesibilidad para usuarios poco técnicos. | Puntaje Lighthouse de accesibilidad y tamaños mínimos. | Mayor o igual a 90, texto base de 16 px o más, botones de 44 × 44 px o más y contraste WCAG 2.2 AA (SC-004). |
| RNF-029 | Portabilidad (Instalabilidad) | Despliegue reproducible. | Tiempo para desplegar en un servidor Linux limpio con Docker Compose. | 15 minutos o menos siguiendo el manual de despliegue. Estado: no se verifica en esta entrega por SC-022. |
| RNF-030 | Portabilidad (Adaptabilidad) | Configuración por entorno sin secretos en el código. | Secretos detectados por el escaneo del repositorio. | 0 secretos en el repositorio; toda la configuración por variables de entorno. |

## 12. Épicas

Los 42 requerimientos funcionales se agrupan en 9 épicas. Las épicas 4 y 5 son el núcleo del producto; la 9 queda completa fuera de esta entrega.

| ID | Nombre | Objetivo | Valor de negocio | Usuarios involucrados | Requerimientos relacionados | Requerimientos de negocio |
| --- | --- | --- | --- | --- | --- | --- |
| EP-001 | Identidad y acceso | Permitir que cada actor se registre, autentique y autorice el tratamiento de sus datos de forma segura. | Base de confianza y cumplimiento de la Ley 1581; sin ella no hay roles ni privacidad. | Cliente, Tienda, Administrador | RF-001 a RF-006, RF-041, RF-042 | RN-005, RN-008 |
| EP-002 | Perfil del cliente | Que el cliente mantenga sus datos y direcciones y consulte su historial. | Pedidos a domicilio sin fricción y recompra rápida. | Cliente | RF-007, RF-008 | RN-004, RN-008 |
| EP-003 | Gestión de la tienda | Que la tienda configure su perfil, horario, domicilio, medios de pago y disponibilidad. | Habilita la participación de tiendas sin inventario digital. | Tienda, Cliente | RF-009 a RF-015 | RN-003, RN-004, RN-010, RN-012 |
| EP-004 | Solicitudes y respuestas | Que el cliente pregunte una vez y reciba respuestas comparables de tiendas cercanas. | Es la propuesta de valor central del producto. | Cliente, Tienda, Sistema | RF-016 a RF-023 | RN-001, RN-002, RN-003, RN-008, RN-009 |
| EP-005 | Órdenes y entrega | Convertir la respuesta elegida en un compromiso con estados, reserva o domicilio y cierre. | Cierra la venta y genera la experiencia que se califica. | Cliente, Tienda, Sistema | RF-024 a RF-029 | RN-004, RN-008, RN-010 |
| EP-006 | Comunicación y notificaciones | Avisar en tiempo real y permitir el contacto entre cliente y tienda elegida. | Respuestas rápidas y coordinación de la entrega. | Cliente, Tienda, Sistema | RF-030 a RF-033 | RN-001, RN-004, RN-008 |
| EP-007 | Reputación | Calificar tiendas y reportar problemas solo sobre órdenes reales. | Premia el cumplimiento y genera confianza. | Cliente, Tienda | RF-034, RF-035 | RN-006, RN-007, RN-012 |
| EP-008 | Administración | Verificar tiendas, atender reportes, sancionar y mantener el catálogo de categorías y restringidos. | Control de calidad y cumplimiento de la plataforma. | Administrador | RF-036 a RF-038 | RN-005, RN-007, RN-009 |
| EP-009 | Pagos y monetización (futuro) | Cobrar dentro de la app y ofrecer planes de pago a tiendas. | Fuente de ingresos cuando la idea se valide. | Cliente, Tienda | RF-039, RF-040 | RN-010, RN-011 |

## 13. Historias de usuario

Hay 38 historias, una por cada requerimiento funcional dentro de la entrega, con el mismo número (HU-016 implementa RF-016). En la versión 1.0 sumaban 154 puntos: 94 Must, 39 Should y 21 Could. Con SC-021, HU-006 y HU-011 (8 puntos Could) salen de esta entrega, así que se construyen 36 historias con 146 puntos: 94 Must, 39 Should y 13 Could. La complejidad está en puntos de historia con escala Fibonacci (1, 2, 3, 5, 8) y es una estimación inicial que el equipo debe recalibrar en la primera planificación.

| ID | Épica | Historia | Descripción detallada | Dependencias | MoSCoW | Complejidad | Valor de negocio |
| --- | --- | --- | --- | --- | --- | --- | --- |
| HU-001 | EP-001 | Como cliente quiero registrarme con mi correo y una frase de contraseña para publicar solicitudes. | Formulario con nombre, correo, teléfono y frase de 15 o más caracteres, validada contra contraseñas filtradas. | HU-005 | Must | 3 | Alto |
| HU-002 | EP-001 | Como dueño de tienda quiero registrar mi negocio con sus documentos para recibir solicitudes de clientes cercanos. | Datos del negocio, categoría, ubicación en el mapa, foto del local y documento; queda Pendiente. | HU-005 | Must | 5 | Alto |
| HU-003 | EP-001 | Como usuario quiero iniciar y cerrar sesión para usar la app de forma segura. | JWT de 15 minutos y refresco rotativo; cuentas suspendidas no ingresan. | HU-001, HU-002 | Must | 5 | Alto |
| HU-004 | EP-001 | Como administrador quiero un segundo factor de autenticación para proteger el panel de administración. | Código TOTP desde una app autenticadora. | HU-003 | Should | 3 | Medio |
| HU-005 | EP-001 | Como usuario quiero leer y aceptar la política de tratamiento de datos para saber cómo se usan mis datos. | Casilla obligatoria; se guarda fecha y versión aceptada. | Ninguna | Must | 2 | Alto |
| HU-006 | EP-001 | Como usuario quiero verificar mi correo para demostrar que mi cuenta es real. | Enlace de un solo uso con vencimiento. | HU-001 | Won't (SC-021) | 3 | Bajo |
| HU-007 | EP-002 | Como cliente quiero guardar mis direcciones para no escribirlas en cada pedido a domicilio. | Pin en el mapa, dirección, indicaciones y teléfono; varias direcciones con nombre. | HU-003 | Must | 5 | Alto |
| HU-008 | EP-002 | Como cliente quiero ver mi historial y repetir una solicitud para volver a pedir lo mismo con un toque. | Lista paginada de solicitudes y órdenes, y «Mis preguntas» con las solicitudes abiertas de las últimas 24 horas (SC-018). | HU-016 | Could | 3 | Bajo |
| HU-009 | EP-003 | Como tienda quiero editar mi perfil y horario para que los clientes me encuentren con datos correctos. | Fotos, horario por día, teléfono y ubicación. | HU-036 | Must | 3 | Alto |
| HU-010 | EP-003 | Como tienda quiero configurar si hago domicilio y su tarifa fija para ofrecerlo en mis respuestas. | Radio de cobertura y tarifa fija en COP. | HU-009 | Must | 3 | Alto |
| HU-011 | EP-003 | Como tienda quiero ofrecer domicilio gratis desde un monto o por distancia para competir mejor. | Estrategias adicionales de tarifa. | HU-010 | Won't (SC-021) | 5 | Medio |
| HU-012 | EP-003 | Como tienda quiero indicar qué medios de pago acepto para que el cliente elija uno válido. | Efectivo, transferencia y datáfono contra entrega. | HU-009 | Must | 2 | Alto |
| HU-013 | EP-003 | Como tienda quiero un interruptor Abierto / Cerrado para no recibir solicitudes cuando no puedo atender. | Botón grande en la parte superior del panel. | HU-009 | Must | 2 | Alto |
| HU-014 | EP-003 | Como cliente quiero ver el detalle de una tienda para decidir si confiar en ella. | Fotos, horario, mapa, teléfono, reseñas y medios de pago. | HU-009, HU-034 | Should | 3 | Medio |
| HU-015 | EP-003 | Como tienda quiero ver un resumen de mis indicadores para saber cómo me va. | Recibidas, respondidas, ganadas, tiempo medio de respuesta y calificación. | HU-019, HU-034 | Could | 5 | Medio |
| HU-016 | EP-004 | Como cliente quiero publicar lo que busco para que las tiendas cercanas me digan si lo tienen. | Producto, cantidad, categoría, foto opcional, modalidad, urgencia y ubicación. | HU-003 | Must | 5 | Alto |
| HU-017 | EP-004 | Como sistema quiero enviar la solicitud solo a las tiendas elegibles para que respondan las que pueden atender. | Aprobada, abierta, en horario, de la categoría, no suspendida y a 3 km o menos (PostGIS). | HU-016, HU-013 | Must | 8 | Alto |
| HU-018 | EP-004 | Como tienda quiero ver las solicitudes nuevas sin datos personales del cliente para responder rápido respetando su privacidad. | Tarjeta con producto, cantidad, foto, distancia redondeada, modalidad, urgencia y tiempo restante. | HU-017 | Must | 3 | Alto |
| HU-019 | EP-004 | Como tienda quiero responder «Sí lo tengo», «Tengo algo parecido» o «No lo tengo» para informar mi disponibilidad en pocos toques. | Precio, cantidad, nota, domicilio, costo y tiempo; No lo tengo en un toque y con opción de deshacer (SC-011). | HU-018 | Must | 5 | Alto |
| HU-020 | EP-004 | Como cliente quiero ver las respuestas a medida que llegan y ordenarlas para elegir la mejor oferta. | Tarjetas por tienda; orden por precio o cercanía; alternativas marcadas. | HU-019, HU-030 | Must | 5 | Alto |
| HU-021 | EP-004 | Como sistema quiero vencer y cerrar solicitudes para que las tiendas no respondan pedidos ya resueltos. | Vencimiento por tiempo y cierre al elegir; aviso a tiendas no elegidas. | HU-016 | Must | 5 | Alto |
| HU-022 | EP-004 | Como cliente quiero que se amplíe el radio si nadie responde para no quedarme sin opciones. | 10 minutos sin respuestas; radio de 3 km a 5 km, una sola vez. | HU-017 | Could | 5 | Medio |
| HU-023 | EP-004 | Como cliente quiero ver un aviso si busco un medicamento con fórmula para saber que debo consultar en la farmacia. | Coincidencia con la lista de restringidos; sin domicilio. | HU-016, HU-038 | Should | 3 | Medio |
| HU-024 | EP-005 | Como cliente quiero elegir una respuesta y confirmar el pedido para comprar el producto. | Modalidad, dirección, medio de pago y desglose producto + domicilio = total. | HU-020, HU-007, HU-012 | Must | 8 | Alto |
| HU-025 | EP-005 | Como cliente quiero que la tienda me aparte el producto para ir a recogerlo con tranquilidad. | Reserva de 30 minutos desde «Listo para recoger» que vence sola (SC-019). | HU-024 | Should | 5 | Medio |
| HU-026 | EP-005 | Como cliente quiero abrir la ubicación de la tienda en mi app de mapas para llegar fácilmente. | Enlace a la app de mapas del teléfono. | HU-024 | Should | 1 | Medio |
| HU-027 | EP-005 | Como tienda quiero actualizar el estado del pedido para que el cliente sepa en qué va. | Domicilio: Confirmado, Preparando, En camino, Entregado. Recogida: Confirmado, Preparando, Listo para recoger, Entregado. Cancelado y Vencida como estados de cierre. | HU-024 | Must | 5 | Alto |
| HU-028 | EP-005 | Como cliente quiero confirmar que recibí mi pedido para cerrarlo y poder calificar. | Orden Resuelta; la dirección deja de verse en la tienda. | HU-027 | Must | 3 | Alto |
| HU-029 | EP-005 | Como cliente quiero cancelar mi pedido antes del despacho para no comprar algo que ya no necesito. | Permitido en Confirmado y Preparando. | HU-027 | Should | 3 | Medio |
| HU-030 | EP-006 | Como usuario quiero recibir avisos en tiempo real para no tener que recargar la app. | SSE para solicitudes, respuestas y estados; las urgentes con sonido. | HU-003 | Must | 8 | Alto |
| HU-031 | EP-006 | Como tienda quiero recibir notificaciones aunque la app esté cerrada para no perder solicitudes. | Push web con VAPID previa autorización del navegador. | HU-030 | Should | 5 | Alto |
| HU-032 | EP-006 | Como cliente o tienda quiero un botón de WhatsApp para coordinar la entrega rápidamente. | Visible solo con la orden activa. | HU-024 | Must | 1 | Alto |
| HU-033 | EP-006 | Como cliente o tienda quiero un mini chat dentro de la app para resolver dudas sin salir de ella. | Solo texto, ligado a la orden activa. | HU-030, HU-024 | Should | 5 | Medio |
| HU-034 | EP-007 | Como cliente quiero calificar a la tienda después de mi pedido para ayudar a otros a elegir. | 1 a 5 estrellas y comentario; una vez por orden resuelta. | HU-028 | Must | 3 | Alto |
| HU-035 | EP-007 | Como cliente o tienda quiero reportar un problema para que el administrador lo revise. | Motivo y descripción ligados a la orden. | HU-024 | Should | 3 | Medio |
| HU-036 | EP-008 | Como administrador quiero aprobar o rechazar tiendas para que solo participen negocios reales. | Revisión de documentos y fotos; motivo de rechazo; auditoría. | HU-002 | Must | 5 | Alto |
| HU-037 | EP-008 | Como administrador quiero gestionar reportes y suspender cuentas para sancionar incumplimientos. | Resolver reportes, suspender y reactivar con auditoría. | HU-035 | Should | 5 | Medio |
| HU-038 | EP-008 | Como administrador quiero gestionar categorías y productos restringidos para controlar qué se puede pedir. | Activar categorías y mantener la lista de restringidos. | Ninguna | Should | 3 | Medio |

Las categorías iniciales (farmacias y ferreterías) se cargan con datos semilla, así HU-016 no depende de HU-038 para la demo.

## 14. Criterios de aceptación

Hay 85 escenarios en Gherkin en español, entre 1 y 4 por historia. Cada escenario lleva la etiqueta de su historia y un identificador de caso de prueba (CP-001 a CP-085), que se usa en la matriz de trazabilidad. Están escritos con la sintaxis de Cucumber (`# language: es`), así que pueden automatizarse tal cual.

### EP-001 Identidad y acceso

```gherkin
# language: es
Característica: Identidad y acceso

  @HU-001 @CP-001
  Escenario: Registro exitoso de cliente
    Dado que no existe una cuenta con el correo "ana@correo.com"
    Y la visitante aceptó la política de tratamiento de datos
    Cuando se registra con una frase de contraseña de 20 caracteres
    Entonces el sistema crea la cuenta con rol CLIENTE

  @HU-001 @CP-002
  Escenario: Correo ya registrado
    Dado que existe una cuenta con el correo "ana@correo.com"
    Cuando alguien intenta registrarse con ese correo
    Entonces el sistema rechaza el registro e indica que el correo ya está en uso

  @HU-001 @CP-003
  Escenario: Frase de contraseña débil
    Cuando un visitante se registra con una frase de 10 caracteres o con una contraseña filtrada conocida
    Entonces el sistema rechaza el registro y explica el requisito de longitud

  @HU-002 @CP-004
  Escenario: Registro de tienda queda pendiente
    Cuando un dueño registra su tienda con todos los datos, la foto del local y el RUT
    Entonces la tienda queda en estado Pendiente
    Y no aparece en la difusión de solicitudes

  @HU-002 @CP-005
  Escenario: Registro de tienda sin documento
    Cuando un dueño intenta registrar su tienda sin adjuntar el documento de existencia
    Entonces el sistema rechaza el registro e indica el campo faltante

  @HU-003 @CP-006
  Escenario: Inicio de sesión correcto
    Dado un cliente con cuenta activa
    Cuando inicia sesión con su correo y su frase de contraseña
    Entonces recibe un token de acceso de 15 minutos y una cookie de refresco HttpOnly

  @HU-003 @CP-007
  Escenario: Credenciales incorrectas
    Cuando un usuario inicia sesión con una frase de contraseña incorrecta
    Entonces el sistema niega el acceso con un mensaje genérico

  @HU-003 @CP-008
  Escenario: Cuenta suspendida
    Dado una tienda suspendida por el administrador
    Cuando intenta iniciar sesión con credenciales correctas
    Entonces el sistema niega el acceso e informa que la cuenta está suspendida

  @HU-004 @CP-009
  Escenario: Administrador con segundo factor válido
    Dado un administrador con TOTP configurado que ingresó su contraseña
    Cuando ingresa un código TOTP vigente
    Entonces accede al panel de administración

  @HU-004 @CP-010
  Escenario: Segundo factor inválido
    Cuando el administrador ingresa un código TOTP vencido o incorrecto
    Entonces el sistema niega el acceso al panel

  @HU-005 @CP-011
  Escenario: Registro sin autorización de datos
    Cuando un visitante envía el registro sin marcar la casilla de autorización
    Entonces el sistema no crea la cuenta y resalta la casilla

  @HU-005 @CP-012
  Escenario: Autorización registrada
    Cuando un visitante se registra aceptando la política versión 1.0
    Entonces el sistema guarda la autorización con fecha, hora y versión 1.0

  @HU-006 @CP-013
  Escenario: Verificación de correo exitosa
    Dado un cliente que recibió el enlace de verificación hace menos de 24 horas
    Cuando abre el enlace
    Entonces su correo queda verificado

  @HU-006 @CP-014
  Escenario: Enlace vencido
    Cuando el cliente abre un enlace de verificación de hace más de 24 horas
    Entonces el sistema lo rechaza y ofrece reenviarlo
```

### EP-002 Perfil del cliente

```gherkin
# language: es
Característica: Perfil del cliente

  @HU-007 @CP-015
  Escenario: Guardar una dirección
    Dado que el cliente está autenticado
    Cuando guarda la dirección "Casa" con pin en el mapa, dirección escrita, indicaciones y teléfono
    Entonces la dirección aparece disponible al pedir domicilio

  @HU-007 @CP-016
  Escenario: Dirección incompleta
    Cuando el cliente intenta guardar una dirección sin ubicación en el mapa
    Entonces el sistema la rechaza e indica el dato faltante

  @HU-008 @CP-017
  Escenario: Repetir una solicitud
    Dado que el cliente tiene una solicitud anterior de "Paracetamol 500 mg, 1 caja"
    Cuando pulsa Repetir en su historial
    Entonces se crea una solicitud nueva con el mismo producto y cantidad

  @HU-008 @CP-018
  Escenario: Historial vacío
    Dado un cliente sin solicitudes
    Cuando abre su historial
    Entonces ve un mensaje que lo invita a publicar su primera solicitud
```

### EP-003 Gestión de la tienda

```gherkin
# language: es
Característica: Gestión de la tienda

  @HU-009 @CP-019
  Escenario: Horario fuera de atención
    Dado una tienda aprobada y abierta con horario de 8:00 a 18:00
    Cuando un cliente publica una solicitud a las 19:00
    Entonces la tienda no la recibe

  @HU-009 @CP-020
  Escenario: Cambios de perfil visibles
    Cuando la tienda actualiza su teléfono y sus fotos
    Entonces el cliente ve los datos nuevos en el detalle de la tienda

  @HU-010 @CP-021
  Escenario: Tarifa fija en la respuesta
    Dado una tienda con domicilio y tarifa fija de 5.000 COP
    Cuando responde Sí lo tengo a una solicitud con domicilio
    Entonces el cliente ve un costo de domicilio de 5.000 COP en la tarjeta

  @HU-010 @CP-022
  Escenario: Tarifa inválida
    Cuando la tienda intenta guardar una tarifa negativa
    Entonces el sistema rechaza el valor

  @HU-011 @CP-023
  Escenario: Domicilio gratis desde un monto
    Dado una tienda con domicilio gratis desde 50.000 COP
    Cuando el cliente confirma una orden de 60.000 COP
    Entonces el costo de domicilio es 0 COP

  @HU-011 @CP-024
  Escenario: Tarifa por distancia
    Dado una tienda con tarifa de 1.000 COP por kilómetro
    Cuando el cliente está a 2,5 km
    Entonces el costo de domicilio es 2.500 COP

  @HU-012 @CP-025
  Escenario: Solo medios activos
    Dado una tienda que acepta solo efectivo y transferencia
    Cuando el cliente confirma una orden con esa tienda
    Entonces solo puede elegir efectivo o transferencia

  @HU-012 @CP-026
  Escenario: Desactivar un medio de pago
    Cuando la tienda desactiva el datáfono
    Entonces el datáfono deja de aparecer en las órdenes nuevas

  @HU-013 @CP-027
  Escenario: Tienda cerrada
    Dado una tienda con el interruptor en Cerrado
    Cuando un cliente cercano publica una solicitud de su categoría
    Entonces la tienda no la recibe

  @HU-013 @CP-028
  Escenario: Tienda abierta
    Dado una tienda con el interruptor en Abierto y dentro de su horario
    Cuando un cliente a 1 km publica una solicitud de su categoría
    Entonces la tienda la recibe en su panel

  @HU-014 @CP-029
  Escenario: Detalle público de la tienda
    Cuando el cliente abre el detalle de una tienda que le respondió
    Entonces ve fotos, horario, mapa, teléfono, reseñas y medios de pago

  @HU-014 @CP-030
  Escenario: Documentos privados ocultos
    Cuando el cliente abre el detalle de una tienda
    Entonces la respuesta no incluye el RUT ni los documentos de verificación

  @HU-015 @CP-031
  Escenario: Indicadores correctos
    Dado una tienda que recibió 10 solicitudes, respondió 8 y ganó 3
    Cuando abre su resumen
    Entonces ve 10 recibidas, 8 respondidas y 3 ganadas

  @HU-015 @CP-032
  Escenario: Tienda sin actividad
    Dado una tienda recién aprobada
    Cuando abre su resumen
    Entonces ve todos los indicadores en cero sin errores
```

### EP-004 Solicitudes y respuestas

```gherkin
# language: es
Característica: Solicitudes y respuestas

  @HU-016 @CP-033
  Escenario: Publicar una solicitud
    Dado que el cliente está autenticado y compartió su ubicación
    Cuando publica "Paracetamol 500 mg, 1 caja" en Farmacias para recoger
    Entonces la solicitud queda Abierta con su tiempo de vencimiento

  @HU-016 @CP-034
  Escenario: Solicitud incompleta
    Cuando el cliente publica una solicitud sin producto o sin ubicación
    Entonces el sistema la rechaza e indica el dato faltante

  @HU-016 @CP-035
  Escenario: Límite de solicitudes
    Dado un cliente que publicó 10 solicitudes en la última hora
    Cuando intenta publicar otra
    Entonces el sistema responde HTTP 429 y le indica cuándo puede volver a intentarlo

  @HU-017 @CP-036
  Escenario: Difusión solo a tiendas elegibles
    Dado tres farmacias aprobadas y abiertas a 1 km, 2 km y 5 km del cliente
    Cuando el cliente publica una solicitud de Farmacias
    Entonces solo las farmacias a 1 km y 2 km la reciben

  @HU-017 @CP-037
  Escenario: Categoría distinta
    Dado una ferretería abierta a 500 m del cliente
    Cuando el cliente publica una solicitud de Farmacias
    Entonces la ferretería no la recibe

  @HU-017 @CP-038
  Escenario: Tienda suspendida
    Dado una farmacia suspendida a 1 km del cliente
    Cuando el cliente publica una solicitud de Farmacias
    Entonces la farmacia suspendida no la recibe

  @HU-018 @CP-039
  Escenario: Privacidad en la tarjeta de solicitud
    Cuando la tienda consulta sus solicitudes nuevas
    Entonces la respuesta de la API no incluye nombre, teléfono, dirección ni coordenadas exactas del cliente

  @HU-018 @CP-040
  Escenario: Distancia aproximada
    Dado un cliente a 1.234 m de la tienda
    Cuando la tienda ve la solicitud
    Entonces ve la distancia como "a 1,2 km"

  @HU-019 @CP-041
  Escenario: Responder Sí lo tengo
    Dado una solicitud Abierta en el panel de la tienda
    Cuando la tienda responde Sí lo tengo con precio 8.000 COP y cantidad 2
    Entonces la respuesta queda registrada y el cliente es notificado

  @HU-019 @CP-042
  Escenario: Alternativa sin descripción
    Cuando la tienda responde Tengo algo parecido sin describir la alternativa
    Entonces el sistema rechaza la respuesta e indica el campo faltante

  @HU-019 @CP-043
  Escenario: No lo tengo en un toque
    Cuando la tienda pulsa No lo tengo
    Entonces la solicitud desaparece de su panel de solicitudes nuevas

  @HU-019 @CP-044
  Escenario: Respuesta duplicada
    Dado que la tienda ya respondió la solicitud
    Cuando intenta responderla de nuevo
    Entonces el sistema rechaza la segunda respuesta

  @HU-020 @CP-045
  Escenario: Respuestas en tiempo real
    Dado que el cliente tiene abierta la pantalla de respuestas
    Cuando una tienda responde Sí lo tengo
    Entonces la tarjeta de la tienda aparece sin recargar la página

  @HU-020 @CP-046
  Escenario: Ordenar por precio
    Dado respuestas con precios 9.000, 7.500 y 8.200 COP
    Cuando el cliente ordena por precio
    Entonces ve las tarjetas en el orden 7.500, 8.200 y 9.000 COP

  @HU-020 @CP-047
  Escenario: Alternativa marcada
    Cuando una tienda responde Tengo algo parecido
    Entonces su tarjeta aparece marcada como alternativa

  @HU-021 @CP-048
  Escenario: Vencimiento de la solicitud
    Dado una solicitud Abierta sin elección
    Cuando se cumple su tiempo de vencimiento
    Entonces la solicitud pasa a Vencida y no acepta más respuestas

  @HU-021 @CP-049
  Escenario: Aviso a tiendas no elegidas
    Dado una solicitud con respuestas de tres tiendas
    Cuando el cliente elige una de ellas
    Entonces las otras dos ven su respuesta como no elegida

  @HU-022 @CP-050
  Escenario: Ampliación del radio
    Dado una solicitud sin respuestas después del tiempo configurado
    Cuando el sistema revisa las solicitudes sin respuesta
    Entonces amplía el radio, notifica a las tiendas nuevas y avisa al cliente

  @HU-022 @CP-051
  Escenario: Sin ampliación si hay respuestas
    Dado una solicitud con al menos una respuesta
    Cuando pasa el tiempo configurado
    Entonces el radio no cambia

  @HU-023 @CP-052
  Escenario: Producto restringido
    Dado que "Amoxicilina" está en la lista de productos restringidos
    Cuando el cliente publica una solicitud de "Amoxicilina 500 mg"
    Entonces ve el aviso de consultar con la farmacia

  @HU-023 @CP-053
  Escenario: Restringido sin domicilio
    Dado una solicitud marcada como restringida
    Cuando el cliente intenta elegir domicilio
    Entonces la opción de domicilio no está disponible
```

### EP-005 Órdenes y entrega

```gherkin
# language: es
Característica: Órdenes y entrega

  @HU-024 @CP-054
  Escenario: Crear orden con total correcto
    Dado una respuesta de 8.000 COP por unidad, cantidad 2 y domicilio de 5.000 COP
    Cuando el cliente elige la respuesta, su dirección "Casa" y pago en efectivo
    Entonces la orden queda Confirmada con total de 21.000 COP desglosado

  @HU-024 @CP-055
  Escenario: La tienda elegida ve los datos del cliente
    Dado una orden Confirmada con domicilio
    Cuando la tienda elegida consulta la orden
    Entonces ve nombre, teléfono y dirección completa del cliente

  @HU-024 @CP-056
  Escenario: Una tienda no elegida no ve los datos
    Dado una orden Confirmada con la tienda A
    Cuando la tienda B consulta esa orden por su identificador
    Entonces el sistema responde HTTP 403 o 404 sin datos del cliente

  @HU-024 @CP-057
  Escenario: Medio de pago no aceptado
    Cuando el cliente intenta confirmar con un medio de pago que la tienda no tiene activo
    Entonces el sistema rechaza la orden

  @HU-025 @CP-058
  Escenario: Reserva vencida
    Dado una orden de recogida con reserva de 30 minutos
    Cuando pasan 30 minutos sin que el cliente recoja
    Entonces la orden pasa a Vencida y ambos son notificados

  @HU-025 @CP-059
  Escenario: Recogida a tiempo
    Dado una orden de recogida con reserva vigente
    Cuando el cliente confirma que recogió el producto
    Entonces la orden queda Resuelta

  @HU-026 @CP-060
  Escenario: Cómo llegar
    Dado una orden de recogida Confirmada
    Cuando el cliente pulsa Cómo llegar
    Entonces se abre la app de mapas con la ubicación de la tienda

  @HU-027 @CP-061
  Escenario: Transición válida
    Dado una orden Confirmada
    Cuando la tienda la cambia a Preparando
    Entonces el estado se actualiza y el cliente recibe la notificación

  @HU-027 @CP-062
  Escenario: Transición inválida
    Dado una orden Confirmada
    Cuando la tienda intenta cambiarla a Entregado
    Entonces el sistema rechaza el cambio

  @HU-028 @CP-063
  Escenario: Confirmar recepción
    Dado una orden con domicilio en estado Entregado
    Cuando el cliente pulsa Recibí mi pedido
    Entonces la orden queda Resuelta y la tienda deja de ver la dirección del cliente

  @HU-028 @CP-064
  Escenario: Confirmar una orden ajena
    Cuando un cliente intenta confirmar la recepción de una orden de otro cliente
    Entonces el sistema rechaza la acción

  @HU-029 @CP-065
  Escenario: Cancelar antes del despacho
    Dado una orden en estado Preparando
    Cuando el cliente la cancela
    Entonces la orden queda Cancelada y la tienda es notificada

  @HU-029 @CP-066
  Escenario: Cancelar después del despacho
    Dado una orden En camino
    Cuando el cliente intenta cancelarla
    Entonces el sistema rechaza la cancelación y sugiere reportar un problema
```

### EP-006 Comunicación y notificaciones

```gherkin
# language: es
Característica: Comunicación y notificaciones

  @HU-030 @CP-067
  Escenario: Evento en tiempo real
    Dado que la tienda tiene el panel abierto
    Cuando un cliente cercano publica una solicitud
    Entonces la solicitud aparece en el panel en 3 segundos o menos sin recargar

  @HU-030 @CP-068
  Escenario: Solicitud urgente
    Dado una solicitud normal y una urgente publicadas al mismo tiempo
    Cuando llegan al panel de la tienda
    Entonces la urgente aparece primero y suena una alerta

  @HU-031 @CP-069
  Escenario: Push con la app cerrada
    Dado una tienda que autorizó notificaciones y cerró la app
    Cuando llega una solicitud nueva
    Entonces el navegador muestra una notificación push

  @HU-031 @CP-070
  Escenario: Sin autorización de notificaciones
    Dado una tienda que rechazó las notificaciones del navegador
    Cuando llega una solicitud nueva
    Entonces no se envía push y la solicitud queda visible al abrir el panel

  @HU-032 @CP-071
  Escenario: Botón de WhatsApp con orden activa
    Dado una orden Confirmada
    Cuando el cliente abre la orden
    Entonces ve el botón Escribir por WhatsApp con el número de la tienda

  @HU-032 @CP-072
  Escenario: Sin botón antes de elegir
    Dado una solicitud Abierta con respuestas
    Cuando el cliente ve las tarjetas
    Entonces no aparece el botón de WhatsApp de ninguna tienda

  @HU-033 @CP-073
  Escenario: Enviar un mensaje
    Dado una orden activa con el chat abierto en ambas sesiones
    Cuando el cliente envía "¿Tienen cambio de 50.000?"
    Entonces la tienda ve el mensaje sin recargar

  @HU-033 @CP-074
  Escenario: Chat cerrado
    Dado una orden Resuelta
    Cuando el cliente intenta enviar un mensaje
    Entonces el sistema no lo permite y muestra el chat en solo lectura
```

### EP-007 Reputación

```gherkin
# language: es
Característica: Reputación

  @HU-034 @CP-075
  Escenario: Calificar una orden resuelta
    Dado una orden Resuelta sin reseña
    Cuando el cliente califica con 4 estrellas
    Entonces la reseña se publica y el promedio de la tienda se recalcula

  @HU-034 @CP-076
  Escenario: Calificar sin orden resuelta
    Cuando un cliente intenta calificar una tienda con la que no tiene orden Resuelta
    Entonces el sistema rechaza la calificación

  @HU-034 @CP-077
  Escenario: Calificar dos veces
    Dado una orden que ya tiene reseña
    Cuando el cliente intenta calificarla otra vez
    Entonces el sistema rechaza la segunda reseña

  @HU-035 @CP-078
  Escenario: Crear un reporte
    Dado una orden En camino con demora
    Cuando el cliente reporta "Demora en la entrega"
    Entonces el reporte queda Abierto en el panel del administrador

  @HU-035 @CP-079
  Escenario: Reportar una orden ajena
    Cuando un usuario intenta reportar una orden en la que no participa
    Entonces el sistema rechaza el reporte
```

### EP-008 Administración

```gherkin
# language: es
Característica: Administración

  @HU-036 @CP-080
  Escenario: Aprobar una tienda
    Dado una tienda Pendiente con documentos válidos
    Cuando el administrador la aprueba
    Entonces la tienda queda Aprobada, es notificada y la acción queda auditada

  @HU-036 @CP-081
  Escenario: Rechazar una tienda
    Dado una tienda Pendiente con documento ilegible
    Cuando el administrador la rechaza con el motivo "Documento ilegible"
    Entonces la tienda queda Rechazada y ve el motivo

  @HU-037 @CP-082
  Escenario: Suspender una cuenta
    Dado una tienda con tres reportes confirmados
    Cuando el administrador la suspende
    Entonces la tienda no puede iniciar sesión ni recibir solicitudes

  @HU-037 @CP-083
  Escenario: Resolver un reporte
    Dado un reporte Abierto
    Cuando el administrador lo marca como resuelto con una nota
    Entonces el reporte queda Resuelto y la acción queda auditada

  @HU-038 @CP-084
  Escenario: Desactivar una categoría
    Cuando el administrador desactiva la categoría Ferreterías
    Entonces Ferreterías deja de aparecer al publicar solicitudes

  @HU-038 @CP-085
  Escenario: Agregar un producto restringido
    Cuando el administrador agrega "Amoxicilina" a la lista de restringidos
    Entonces las solicitudes nuevas de ese producto muestran el aviso
```

## 15. Priorización MoSCoW

La regla aplicada: es Must lo que hace falta para recorrer el flujo completo de la demo (registrarse, publicar, responder, elegir, entregar y calificar) o para cumplir la ley y la seguridad básica; es Should lo que mejora mucho la experiencia pero tiene un sustituto manual; es Could lo que agrega valor sin afectar el flujo; es Won't lo que el equipo decidió dejar fuera. Como cada historia tiene el mismo número que su requerimiento, la prioridad de RF-xxx es la de HU-xxx.

| Prioridad | RN | RF / HU | RNF | Puntos de historia |
| --- | --- | --- | --- | --- |
| Must Have | 11 | 22 | 21 | 94 |
| Should Have | 1 | 11 | 9 | 39 |
| Could Have | 0 | 5 | 0 | 21 |
| Won't Have | 0 | 4 | 0 | 0 |

### Requerimientos del negocio

| ID | MoSCoW | Justificación |
| --- | --- | --- |
| RN-001 a RN-004 | Must | Son la propuesta de valor: sin consulta, comparación, participación de tiendas y entrega no hay producto. |
| RN-005 a RN-007 | Must | La confianza es la principal objeción de clientes y tiendas en el documento de negocio. |
| RN-008, RN-009 | Must | Obligaciones legales (Ley 1581 y regulación de medicamentos). |
| RN-010, RN-011 | Must | Restricciones de alcance y presupuesto acordadas en la Fase 1. |
| RN-012 | Should | Las métricas importan para el piloto; en la demo con datos de prueba su valor es limitado. |

### Requerimientos funcionales e historias de usuario

| ID | MoSCoW | Justificación |
| --- | --- | --- |
| RF/HU-001 | Must | Sin cuenta de cliente no se puede publicar. |
| RF/HU-002 | Must | Sin tiendas registradas nadie responde. |
| RF/HU-003 | Must | La autenticación habilita roles y privacidad. |
| RF/HU-004 | Should | Protege al administrador; el sustituto temporal es una frase de contraseña fuerte. |
| RF/HU-005 | Must | Exigido por la Ley 1581 y acordado con el profesor. |
| RF/HU-006 | Won't (SC-021) | Salió de esta entrega: necesita un servidor de correo. Queda como trabajo futuro. |
| RF/HU-007 | Must | El domicilio necesita una dirección del cliente. |
| RF/HU-008 | Could | Comodidad para clientes recurrentes; no afecta el flujo principal. |
| RF/HU-009 | Must | El horario y la ubicación definen la elegibilidad de la tienda. |
| RF/HU-010 | Must | La tarifa fija basta para mostrar el desglose del total. |
| RF/HU-011 | Won't (SC-021) | Salió de esta entrega; la tarifa fija de HU-010 cubre el domicilio. Queda como trabajo futuro. |
| RF/HU-012 | Must | El cliente debe elegir un medio de pago al confirmar. |
| RF/HU-013 | Must | Regla central del negocio: tienda cerrada no recibe solicitudes. |
| RF/HU-014 | Should | Ayuda a decidir; la tarjeta de respuesta ya muestra lo esencial. |
| RF/HU-015 | Could | Útil para la tienda pero no para recorrer el flujo. |
| RF/HU-016 | Must | Inicio del flujo principal. |
| RF/HU-017 | Must | Difusión por cercanía, el diferenciador del producto. |
| RF/HU-018 | Must | La tienda debe ver qué se busca, cuidando la privacidad. |
| RF/HU-019 | Must | Sin respuestas no hay comparación. |
| RF/HU-020 | Must | Comparar ofertas es la propuesta de valor. |
| RF/HU-021 | Must | Evita respuestas sobre solicitudes cerradas y estados inconsistentes. |
| RF/HU-022 | Could | Con datos de prueba siempre habrá tiendas en el radio. |
| RF/HU-023 | Should | Requisito regulatorio; el sustituto temporal es no cargar medicamentos con fórmula en la demo. |
| RF/HU-024 | Must | Convierte la respuesta en compra. |
| RF/HU-025 | Should | Mejora la recogida; sin ella la recogida igual funciona. |
| RF/HU-026 | Should | Cuesta muy poco, pero el cliente puede buscar la dirección a mano. |
| RF/HU-027 | Must | El seguimiento del pedido es parte del flujo. |
| RF/HU-028 | Must | Cierra la orden y habilita la calificación. |
| RF/HU-029 | Should | Caso alterno importante, pero no bloquea el flujo principal. |
| RF/HU-030 | Must | Las respuestas y estados deben llegar en vivo. |
| RF/HU-031 | Should | Con la app abierta, SSE ya cubre la demo. |
| RF/HU-032 | Must | Canal de contacto acordado con Pedro; costo de un día. |
| RF/HU-033 | Should | Acordado como lo primero que se suelta si falta tiempo. |
| RF/HU-034 | Must | La reputación es parte de la confianza (RN-006). |
| RF/HU-035 | Should | El administrador puede atender quejas por fuera en la demo. |
| RF/HU-036 | Must | Sin aprobación ninguna tienda puede operar. |
| RF/HU-037 | Should | Las sanciones importan en producción, no en la demo. |
| RF/HU-038 | Should | Las categorías iniciales se cargan como datos semilla. |
| RF-039 a RF-042 | Won't | Decisiones de la Fase 1: pagos, plan de pago, supresión de cuenta e inicio sin contraseña quedan para versiones futuras. |

### Requerimientos no funcionales

| ID | MoSCoW | Justificación |
| --- | --- | --- |
| RNF-001, RNF-002, RNF-004 | Must | Sin ellos la experiencia en vivo falla o el servidor se queda sin memoria. |
| RNF-003 | Should | Con pocos datos de prueba cualquier consulta es rápida; importa al crecer. |
| RNF-005 | Should | Mejora la percepción, no bloquea la demo. |
| RNF-006 a RNF-016 | Must | Seguridad, privacidad y fiabilidad básicas: son las que evalúa una auditoría. |
| RNF-017 a RNF-020 | Should | Operación y capacidad: importantes, pero la demo tolera un margen. |
| RNF-021 a RNF-025 | Must | Calidad del código y pruebas que el profesor exige. |
| RNF-026 a RNF-028 | Should | Compatibilidad y usabilidad medidas con pruebas manuales al final. |
| RNF-029, RNF-030 | Must | El despliegue reproducible y sin secretos es condición de la entrega en el VPS. |

## 16. Ciclo de vida seleccionado

Recomendación: un modelo híbrido de Scrum con sprints de una semana y prácticas DevOps (integración y despliegue continuos). Obtiene 38 de 40 puntos en la matriz, frente a 35 de Scrum puro y 18 de Cascada.

### Matriz comparativa

Escala de 1 a 5, donde 5 es el mejor ajuste a este proyecto. "Complejidad" mide qué tan bien el modelo maneja la complejidad del producto sin agregar burocracia.

| Modelo | Escalabilidad | Tiempo de entrega | Complejidad | Riesgos | Tamaño del equipo | Presupuesto | Cambios esperados | Necesidades del negocio | Total (de 40) |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| Híbrido Scrum + DevOps | 5 | 5 | 4 | 5 | 5 | 4 | 5 | 5 | 38 |
| Scrum | 4 | 4 | 4 | 4 | 5 | 4 | 5 | 5 | 35 |
| Scrumban | 4 | 4 | 4 | 4 | 4 | 5 | 5 | 4 | 34 |
| Kanban | 4 | 4 | 4 | 3 | 4 | 5 | 5 | 3 | 32 |
| Incremental | 4 | 4 | 4 | 4 | 4 | 4 | 3 | 4 | 31 |
| DevOps (solo) | 5 | 4 | 3 | 3 | 3 | 4 | 4 | 3 | 29 |
| Evolutivo | 3 | 3 | 3 | 3 | 3 | 3 | 4 | 3 | 25 |
| Espiral | 4 | 1 | 2 | 5 | 2 | 2 | 4 | 3 | 23 |
| RUP | 4 | 1 | 2 | 3 | 3 | 2 | 3 | 3 | 21 |
| Cascada | 2 | 2 | 3 | 1 | 3 | 4 | 1 | 2 | 18 |

### Justificación

- Tiempo de entrega: con 4,5 semanas, sprints de una semana dan cuatro puntos de control y una versión funcionando cada viernes. Cascada, RUP y Espiral gastan demasiado tiempo en fases o ciclos de análisis antes de tener software.
- Riesgos: el mayor riesgo es no terminar. La revisión semanal permite recortar los Should y Could a tiempo, y el despliegue continuo elimina el riesgo de "funciona en mi máquina" el día de la entrega.
- Tamaño del equipo: 7 personas es el tamaño ideal de un equipo Scrum (entre 3 y 9 desarrolladores).
- Cambios esperados: el alcance todavía se ajusta (rúbrica del profesor, roles, mini chat). Scrum absorbe cambios en el backlog sin rehacer el plan; Cascada no.
- Escalabilidad: las prácticas DevOps (pipeline en GitHub Actions, imágenes Docker, despliegue por SSH) son las mismas que servirán cuando el producto crezca.
- Presupuesto: todas las herramientas son gratuitas (GitHub, GitHub Actions, GitHub Projects como tablero) y el único costo es el VPS.
- Necesidades del negocio y académicas: Scrum produce directamente los artefactos que pide el proceso de requisitos (épicas, historias, criterios de aceptación) y deja evidencia de cada sprint para la evaluación.
- Por qué no Kanban o Scrumban: sin iteraciones con fecha fija se pierde el ritmo de entregas parciales frente a una fecha límite inamovible. Sí se usa un tablero Kanban dentro de cada sprint.

### Plan de sprints propuesto

| Sprint | Fechas | Objetivo |
| --- | --- | --- |
| Sprint 0 | 26/10 al 01/11 | SRS, diagramas UML, roles, repositorio, pipeline de CI/CD, esqueleto Docker desplegado en el VPS y prueba técnica de PostGIS |
| Sprint 1 | 02/11 al 08/11 | Identidad y roles, registro y aprobación de tiendas, categorías, publicar solicitud y difusión por radio |
| Sprint 2 | 09/11 al 15/11 | Respuestas Sí / Parecido / No, elección, órdenes y estados, notificaciones en tiempo real |
| Sprint 3 | 16/11 al 22/11 | Reseñas, reportes, panel de administrador, WhatsApp y mini chat; desde el 16/11 el VPS queda estable para la evaluación |
| Cierre | 23/11 al 26/11 | Pruebas finales, correcciones, documentación final y ensayo de la demo |

### Ceremonias y prácticas

- Planificación de sprint (1 hora) el lunes; revisión y retrospectiva (1 hora en total) el domingo.
- Daily de 15 minutos, que puede ser asíncrona por chat.
- Tablero en GitHub Projects con columnas Por hacer, En curso, En revisión y Hecho.
- Cada cambio entra por pull request con revisión de otra persona y el pipeline en verde.
- Definición de Hecho: código revisado, pruebas unitarias y de integración pasando, criterios de aceptación verificados, documentación de la API actualizada y desplegado en el ambiente de pruebas.

## 17. Matriz de trazabilidad

La trazabilidad es completa: los 5 objetivos llegan a requerimientos de negocio, los 12 RN a épicas, las 9 épicas a historias, las 38 historias a sus RF y los 38 RF de la entrega a 85 casos de prueba. Los 4 RF Won't no tienen caso de prueba porque no se construyen.

### Objetivo de negocio → Requerimiento de negocio

| Objetivo | Requerimientos de negocio |
| --- | --- |
| OB-01 Reducir tiempo y costo de búsqueda | RN-001, RN-002, RN-004 |
| OB-02 Canal de ventas sin inventario digital | RN-003, RN-004 |
| OB-03 Confianza entre clientes y tiendas | RN-005, RN-006, RN-007, RN-009 |
| OB-04 Protección de datos personales | RN-008, RN-009 |
| OB-05 MVP funcional y de bajo costo | RN-010, RN-011, RN-012 |

### Requerimiento de negocio → Épica

| Requerimiento de negocio | Épicas |
| --- | --- |
| RN-001 Consulta única | EP-004, EP-006 |
| RN-002 Comparación de ofertas | EP-004 |
| RN-003 Participación sin inventario | EP-003, EP-004 |
| RN-004 Modalidades de entrega | EP-002, EP-003, EP-005, EP-006 |
| RN-005 Tiendas verificadas | EP-001, EP-008 |
| RN-006 Reputación real | EP-007 |
| RN-007 Incidentes y sanciones | EP-007, EP-008 |
| RN-008 Privacidad del cliente | EP-001, EP-002, EP-004, EP-005, EP-006 |
| RN-009 Medicamentos con fórmula | EP-004, EP-008 |
| RN-010 Pago contra entrega | EP-003, EP-005, EP-009 |
| RN-011 Operación de bajo costo | EP-009, y RNF-004, RNF-020, RNF-029 |
| RN-012 Medición de indicadores | EP-003, EP-007 |

### Épica → Historia de usuario

| Épica | Historias |
| --- | --- |
| EP-001 Identidad y acceso | HU-001 a HU-006 |
| EP-002 Perfil del cliente | HU-007, HU-008 |
| EP-003 Gestión de la tienda | HU-009 a HU-015 |
| EP-004 Solicitudes y respuestas | HU-016 a HU-023 |
| EP-005 Órdenes y entrega | HU-024 a HU-029 |
| EP-006 Comunicación y notificaciones | HU-030 a HU-033 |
| EP-007 Reputación | HU-034, HU-035 |
| EP-008 Administración | HU-036 a HU-038 |
| EP-009 Pagos y monetización | Sin historias en esta entrega (RF-039, RF-040 son Won't) |

### Historia de usuario → Requerimiento funcional → Caso de prueba

| Historia | Requerimiento funcional | Casos de prueba |
| --- | --- | --- |
| HU-001 | RF-001 | CP-001, CP-002, CP-003 |
| HU-002 | RF-002 | CP-004, CP-005 |
| HU-003 | RF-003 | CP-006, CP-007, CP-008 |
| HU-004 | RF-004 | CP-009, CP-010 |
| HU-005 | RF-005 | CP-011, CP-012 |
| HU-006 | RF-006 | CP-013, CP-014 |
| HU-007 | RF-007 | CP-015, CP-016 |
| HU-008 | RF-008 | CP-017, CP-018 |
| HU-009 | RF-009 | CP-019, CP-020 |
| HU-010 | RF-010 | CP-021, CP-022 |
| HU-011 | RF-011 | CP-023, CP-024 |
| HU-012 | RF-012 | CP-025, CP-026 |
| HU-013 | RF-013 | CP-027, CP-028 |
| HU-014 | RF-014 | CP-029, CP-030 |
| HU-015 | RF-015 | CP-031, CP-032 |
| HU-016 | RF-016 | CP-033, CP-034, CP-035 |
| HU-017 | RF-017 | CP-036, CP-037, CP-038 |
| HU-018 | RF-018 | CP-039, CP-040 |
| HU-019 | RF-019 | CP-041, CP-042, CP-043, CP-044 |
| HU-020 | RF-020 | CP-045, CP-046, CP-047 |
| HU-021 | RF-021 | CP-048, CP-049 |
| HU-022 | RF-022 | CP-050, CP-051 |
| HU-023 | RF-023 | CP-052, CP-053 |
| HU-024 | RF-024 | CP-054, CP-055, CP-056, CP-057 |
| HU-025 | RF-025 | CP-058, CP-059 |
| HU-026 | RF-026 | CP-060 |
| HU-027 | RF-027 | CP-061, CP-062 |
| HU-028 | RF-028 | CP-063, CP-064 |
| HU-029 | RF-029 | CP-065, CP-066 |
| HU-030 | RF-030 | CP-067, CP-068 |
| HU-031 | RF-031 | CP-069, CP-070 |
| HU-032 | RF-032 | CP-071, CP-072 |
| HU-033 | RF-033 | CP-073, CP-074 |
| HU-034 | RF-034 | CP-075, CP-076, CP-077 |
| HU-035 | RF-035 | CP-078, CP-079 |
| HU-036 | RF-036 | CP-080, CP-081 |
| HU-037 | RF-037 | CP-082, CP-083 |
| HU-038 | RF-038 | CP-084, CP-085 |
| Sin historia | RF-039 a RF-042 (Won't) | Sin casos en esta entrega |

Los requerimientos no funcionales se verifican con su propio criterio de aceptación (Fase 3): pruebas de carga para RNF-001 a RNF-005 y RNF-020, pruebas de seguridad y escaneos del pipeline para RNF-006 a RNF-014 y RNF-030, y métricas del pipeline para RNF-021 a RNF-024. Varios casos funcionales también cubren RNF: CP-039 y CP-056 verifican RNF-007, CP-035 verifica RNF-009 y CP-062 verifica RNF-016.

## 18. Riesgos

| ID | Riesgo | Impacto | Mitigación |
| --- | --- | --- | --- |
| R-01 | El alcance no cabe en 4,5 semanas | Alto | MVP con MoSCoW estricto, sprints de una semana, recorte de Should y Could primero |
| R-02 | Roles del equipo sin definir | Medio | Asignar roles en el Sprint 0 |
| R-03 | Curva de aprendizaje de PostGIS, SSE y push web | Medio | Prototipo técnico en el Sprint 0 |
| R-04 | Fuga de datos del cliente hacia tiendas no elegidas | Alto | Autorización por objeto y DTOs por rol, con pruebas específicas |
| R-05 | Servidor de bajos recursos | Bajo | Presupuesto de memoria de la arquitectura; 4 GB dan margen |
| R-06 | Requisitos del profesor no conocidos del todo | Medio | Pedir la rúbrica o el enunciado completo |

## 19. Restricciones

| Tipo | Restricción |
| --- | --- |
| Tiempo | Entrega final el 26/11/2026, unas 4,5 semanas desde el inicio de este documento. |
| Técnica | Java + Spring Boot, monolito modular, PostgreSQL + PostGIS, React como PWA, Docker Compose, solo librerías con licencia gratuita. |
| Infraestructura | Un VPS Hostinger KVM 1 (1 vCPU y 4 GB de RAM según la oferta conocida; confirmar). |
| Presupuesto | Costo del VPS: COP 57.900 (confirmar si es mensual y la permanencia exigida). Sin otros costos fijos. |
| Legal | Ley 1581 de 2012: en esta entrega se documenta la política de tratamiento de datos y se pide autorización explícita al registrarse. Medicamentos con fórmula médica no se ofrecen para venta. |
| Operativa | Demo con datos de prueba ante el profesor; sin tiendas reales. El domicilio lo hace la propia tienda. |
| Académica | Diagramas UML exigidos: casos de uso, clases, secuencia, despliegue y otros por confirmar. Elaborados en draw.io. |

## 20. Supuestos

| ID | Supuesto | Si no se cumple |
| --- | --- | --- |
| S-01 | Las 7 personas del equipo están disponibles hasta el 26/11/2026 y sus roles se asignan en el Sprint 0. | Se recorta primero el Should y el Could (sección 15). |
| S-02 | La demo usa datos de prueba; no hay tiendas ni clientes reales. | Habría que operar el proceso de verificación real y ampliar el cumplimiento de la Ley 1581. |
| S-03 | El plan Hostinger KVM 1 ofrece 1 vCPU y 4 GB de RAM por COP 57.900. | Con 2 GB el stack aún cabe según el presupuesto de memoria de la arquitectura. |
| S-04 | Para esta entrega basta documentar la política de datos y pedir la autorización al registrarse. | Se adelanta RF-041 (supresión de cuenta). |
| S-05 | Para verificar una tienda basta el RUT o el registro mercantil; el permiso de funcionamiento de farmacias está por confirmar. | Se agrega un documento obligatorio por categoría en RF-002. |
| S-06 | Los servicios gratuitos usados (push de navegadores, teselas de OpenStreetMap, GitHub Actions) siguen sin costo a la escala de la demo. | Se cambia de proveedor detrás del puerto correspondiente. |
| S-07 | Los usuarios tienen celular con navegador moderno e internet móvil. | Se mantiene la versión web de escritorio como alternativa. |
| S-08 | El domicilio lo hace la propia tienda con sus medios. | Se requeriría una red de domiciliarios (fuera de alcance). |
| S-09 | Los diagramas del Anexo B cubren lo que exige el profesor. | Se agregan los diagramas faltantes cuando llegue la rúbrica. |

Pendientes de confirmar: los roles de las 7 personas y la rúbrica completa del profesor.

## 21. Recomendaciones técnicas

1. Seguir la arquitectura definida: monolito modular en Spring Boot con Clean Architecture por módulo, PostgreSQL + PostGIS, React PWA y Docker Compose.
2. Montar CI/CD en el Sprint 0 y desplegar en el VPS desde la primera semana, para que el 26/11 no haya sorpresas de entorno.
3. Construir primero el camino feliz completo (publicar, responder, elegir, entregar, calificar) en los sprints 1 y 2, y después pulir.
4. Automatizar los escenarios Gherkin de la sección 14 con Cucumber-JVM y REST Assured contra la API, y cubrir 3 a 5 flujos críticos de punta a punta con Playwright.
5. Usar Testcontainers con la imagen de PostGIS en las pruebas de integración, para probar las consultas por radio contra la base real.
6. Mantener una suite propia de pruebas de autorización por objeto (RNF-007); es el riesgo de seguridad más alto del producto.
7. Medir RNF-001, RNF-002 y RNF-020 con k6 antes del cierre, y monitorear la disponibilidad (RNF-018) con un servicio externo gratuito.
8. Preparar un script de datos semilla para la demo: categorías, 10 a 15 tiendas en una zona, clientes y productos restringidos.
9. Dejar configurables los valores operativos (30 minutos, 10 minutos, 3 y 5 km, 10 solicitudes por hora) para ajustarlos sin desplegar.
10. Conservar la trazabilidad en el trabajo diario: un issue de GitHub por historia (HU-xxx), ramas y pull requests que la nombren, y las etiquetas @HU y @CP en las pruebas.

## Anexo A. Validación de calidad de los requisitos

De 84 requisitos (12 RN, 42 RF y 30 RNF), 66 cumplen los 8 criterios de ISO/IEC/IEEE 29148 y 18 tienen observaciones. Dos inconsistencias ya se corrigieron en este documento (V-05 y V-12); las demás tienen una mejora que el equipo aprobó el 26/10/2026 y ya está incorporada en los requisitos.

### Hallazgos y mejoras

| ID | Requisitos | Criterio afectado | Problema | Mejora propuesta | Estado |
| --- | --- | --- | --- | --- | --- |
| V-01 | RF-016, RF-021 | No ambiguo | No se fija el tiempo de vencimiento de una solicitud. | 30 minutos por defecto, configurable por el administrador. | Aprobado |
| V-02 | RF-019, RF-024 | Completo | No se define qué pasa si la tienda tiene menos unidades que las pedidas. | La tienda puede ofrecer menos; la tarjeta muestra "2 de 3 disponibles" y el total usa la cantidad ofrecida. | Aprobado |
| V-03 | RF-022 | No ambiguo | "Tiempo configurable" y "radio mayor" sin valores. | Ampliar una sola vez, de 3 km a 5 km, si no hay respuestas en 10 minutos. | Aprobado |
| V-04 | RF-023 | No ambiguo | No se dice cómo se decide que un producto coincide con la lista de restringidos. | Coincidencia por palabra clave sin distinguir mayúsculas ni tildes (unaccent de PostgreSQL). | Aprobado |
| V-05 | RF-027, RF-028, HU-027 | Consistente | RF-028 usaba un estado "lista para recoger" que no existía y Vencida no aparecía en la máquina de estados. | Se separaron los flujos de domicilio y recogida, se agregó Listo para recoger y se incluyó Vencida. | Corregido |
| V-06 | RF-030 | Factible | Los navegadores bloquean el sonido si el usuario no ha interactuado con la página. | El sonido de urgencia suena después de la primera interacción con el panel; siempre hay alerta visual. | Aprobado |
| V-07 | RF-031 | Factible | En iPhone el push web solo funciona con la app instalada en la pantalla de inicio (iOS 16.4 o superior). | Documentarlo como restricción y mostrar una guía de instalación en iPhone. | Aprobado |
| V-08 | RF-002 | Completo | Los documentos exigidos a farmacías están por confirmar (documento de negocio). | Supuesto: RUT o registro mercantil para todas; el permiso de funcionamiento de farmacia queda pendiente. | Supuesto |
| V-09 | RF-039 a RF-042 | Verificable | Sin criterio de validación real. | Aceptable por ser Won't; se detallan cuando entren a un sprint. | Aceptado |
| V-10 | RN-012 | Verificable | Las métricas de éxito no tienen valor meta. | Para el piloto: tasa de respuesta de 70 % o más y tiempo medio de respuesta de 10 minutos o menos. | Aprobado |
| V-11 | RNF-009 | Correcto | El límite de 10 solicitudes por hora es un supuesto, no viene del negocio. | Valor aprobado por el equipo; queda configurable. | Aprobado |
| V-12 | RNF-018 y plan de sprints | Consistente | La disponibilidad se medía desde el 16/11, pero el despliegue estaba previsto para el Sprint 3 (16 al 22/11). | El primer despliegue en el VPS pasa al Sprint 0 con CI/CD, como corresponde a DevOps. | Corregido |

### Evaluación de requerimientos funcionales

✓ cumple; ⚠ tiene un hallazgo de la tabla anterior.

| RF | Correcto | Completo | Consistente | No ambiguo | Verificable | Priorizado | Rastreable | Factible | Hallazgo |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RF-001 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-002 | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | V-08 |
| RF-003 a RF-015 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-016 | ✓ | ✓ | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | V-01 |
| RF-017, RF-018 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-019 | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | V-02 |
| RF-020 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-021 | ✓ | ✓ | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | V-01 |
| RF-022 | ✓ | ✓ | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | V-03 |
| RF-023 | ✓ | ✓ | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | V-04 |
| RF-024 | ✓ | ⚠ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | V-02 |
| RF-025, RF-026 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-027, RF-028 | ✓ | ✓ | ✓ (corregido) | ✓ | ✓ | ✓ | ✓ | ✓ | V-05 |
| RF-029 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-030 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ⚠ | V-06 |
| RF-031 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ⚠ | V-07 |
| RF-032 a RF-038 | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |  |
| RF-039 a RF-042 | ✓ | ✓ | ✓ | ✓ | ⚠ | ✓ | ✓ | ✓ | V-09 |

### Evaluación de requerimientos de negocio y no funcionales

| Requisitos | Resultado | Hallazgo |
| --- | --- | --- |
| RN-001 a RN-011 | Cumplen los 8 criterios |  |
| RN-012 | No verificable sin valores meta | V-10 |
| RNF-001 a RNF-008, RNF-010 a RNF-017, RNF-019 a RNF-030 | Cumplen los 8 criterios: cada uno tiene métrica y criterio numérico |  |
| RNF-009 | Valor basado en un supuesto | V-11 |
| RNF-018 | Inconsistente con el plan de sprints | V-12, corregido |

## Anexo B. Diagramas UML

Los diagramas están en archivos .drawio editables (carpeta `docs/diagramas` del repositorio) y se abren directamente en draw.io. Cada uno nombra las historias o requerimientos que representa.

| Archivo | Diagrama | Páginas | Qué muestra | Secciones relacionadas |
| --- | --- | --- | --- | --- |
| 00-contexto.drawio | Contexto | 1 | El sistema, sus tres actores y los sistemas externos (push, OpenStreetMap, WhatsApp, mapas, GitHub Actions) | 6.1 |
| 01-casos-de-uso.drawio | Casos de uso | 3 | Uno por actor: Cliente, Tienda, y Administrador con el reloj del sistema; incluye «include» y «extend» | 10, 13 |
| 02-clases-dominio.drawio | Clases | 1 | Agregados, atributos, operaciones, enumeraciones y el patrón Strategy de tarifas | 10, 12 |
| 03-secuencia-publicar-responder.drawio | Secuencia | 1 | Publicar una solicitud, difundirla y recibir una respuesta | HU-016 a HU-020, HU-030 |
| 04-secuencia-elegir-orden.drawio | Secuencia | 1 | Elegir una respuesta, crear la orden, cambiar estados y confirmar recepción | HU-024, HU-027, HU-028 |
| 05-estados.drawio | Estados | 2 | Máquinas de estado de la Solicitud y de la Orden | RF-021, RF-027, RNF-016 |
| 06-actividades-flujo-principal.drawio | Actividades | 1 | Flujo completo con carriles Cliente, Sistema y Tienda | 6.3 |
| 07-componentes.drawio | Componentes | 1 | Módulos del monolito, dependencias por api, eventos y puertos | 21 |
| 08-despliegue.drawio | Despliegue | 1 | VPS Hostinger KVM 1 con los contenedores caddy, api y db, volúmenes y nodos externos | 6.6, 19 |
| 09-entidad-relacion.drawio | Entidad-relación | 1 | Tablas por esquema, llaves físicas dentro del módulo y referencias lógicas entre módulos | 10, 11 |

## Control de cambios de la SRS

La versión 1.0 se aprobó el 26/10/2026. Durante los sprints, el equipo aprobó solicitudes de cambio (SC) en `docs/gestion/CONTROL-DE-CAMBIOS.md`. Las que cambian lo que dice esta especificación quedaron aplicadas así en la versión 1.1:

| SC | Fecha | Cambio aprobado | Dónde se aplicó |
| --- | --- | --- | --- |
| SC-001 | 07/11/2026 | El límite de solicitudes responde HTTP 429 con el tiempo de espera. | Criterios de HU-016 (ya decían 429). |
| SC-004 | 08/11/2026 | Accesibilidad WCAG 2.2 AA en lugar de 2.1 AA. | Referencias y RNF-028. |
| SC-007 | 12/11/2026 | La lista de categorías se adelanta al Sprint 1 porque HU-002 y HU-016 la necesitan. | Sin cambio de requisito: la nota de la sección 13 ya preveía los datos semilla. |
| SC-011 | 12/11/2026 | Nueve decisiones de interfaz (nombre «Tengo algo parecido», deshacer «No lo tengo», motivos de reporte, confirmaciones). | HU-019. |
| SC-014 | 16/11/2026 | Hasta 5 conexiones de tiempo real por usuario. | RF-030. |
| SC-017 | 18/11/2026 | «Mi tienda» muestra la categoría y la ubicación. | RF-013. |
| SC-018 | 18/11/2026 | HU-008 incluye «Mis preguntas» (solicitudes abiertas de las últimas 24 horas). | RF-008 y HU-008. |
| SC-019 | 19/11/2026 | La reserva dura 30 minutos desde «Listo para recoger». | Definiciones, RF-025 y HU-025. |
| SC-020 | 21/11/2026 | Menú por rol y página de inicio con «Cómo funciona». | Cambio de interfaz; no crea requisitos nuevos. |
| SC-021 | 22/11/2026 | HU-006 y HU-011 salen de la entrega; se hacen HU-015 y HU-022. | RF-006, RF-011, HU-006, HU-011, sección 13 y priorización. |
| SC-022 | 22/11/2026 | NF-USA-027 y NF-POR-029 no se ejecutan en el C3. | RNF-027 y RNF-029. |
| SC-023 | 23/11/2026 | Vuelve la prueba de usabilidad pedida por el docente: caja negra con ruta feliz y excepciones. | RNF-027. |

Las demás SC (SC-002, SC-003, SC-005, SC-006, SC-008 a SC-010, SC-012, SC-013, SC-015 y SC-016) son de diseño, proceso o cronograma y no cambian ningún requisito de esta especificación.

## Historial del documento

| Versión | Fecha | Autor | Cambio |
| --- | --- | --- | --- |
| 1.0 | 26/10/2026 | Equipo Byte Force (elaborada con apoyo de Claude) | Versión inicial aprobada por el equipo. |
| 1.1 | 24/11/2026 | Equipo Byte Force (actualizada con apoyo de Claude Code) | Se aplican SC-004, SC-011, SC-014, SC-017 a SC-019 y SC-021 a SC-023 y se agrega el control de cambios. |
