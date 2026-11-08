# Decisiones técnicas y convenciones del frontend · Quién lo tiene

Versión 1.2 (aprobada por Pedro) · 08/11/2026

Este documento hace para el frontend lo mismo que `DECISIONES-BACKEND.md` hace para el backend: deja por escrito cómo se construye la PWA para que Monica (y quien la apoye) escriba todo de la misma forma. Nace de analizar una lista de estándares de usabilidad y accesibilidad contra lo que el proyecto ya tiene documentado y contra el plazo real. Pedro aprobó todas las decisiones el 08/11/2026; lo que queda abierto es del equipo de backend (sección 15.2).

Documentos que mandan sobre este: `Arquitectura-de-software-QuienLoTiene` (DA-06 React + Vite como PWA, DA-07 SSE y push web, Tabla 17 de la pila y Tabla 18 de componentes que se evitan), los RNF del plan de proyecto, `docs/pruebas/ESTRATEGIA-DE-PRUEBAS.md` y `docs/arquitectura/DECISIONES-BACKEND.md` (glosario, convenciones de la API y códigos de error).

---

## 0. Lo que ya está decidido y no se discute aquí

| Fuente | Qué fija para el frontend |
|---|---|
| DA-06 y Tabla 17 | React con Vite, instalable como PWA, React Router y TanStack Query. Licencias MIT o equivalentes |
| Tabla 18 | Mapas con Leaflet directo o MapLibre y teselas de OpenStreetMap; **no** `react-leaflet` (licencia Hippocratic); **no** Google Maps. Mostrar «© OpenStreetMap contributors» |
| DA-07 | Tiempo real con SSE mientras la app está abierta y push web (VAPID) para la tienda. En iPhone el push solo funciona con la PWA instalada (iOS 16.4+) |
| Arquitectura, Rendimiento | División de código por pantalla, archivos comprimidos y cacheados por Caddy, fotos reducidas en el navegador antes de subirlas |
| RNF-005 | Lighthouse móvil de rendimiento ≥ 80 y primera carga ≤ 3 s en 4G simulado |
| RNF-026 | Dos últimas versiones de Chrome, Edge, Firefox y Safari; Android 10+ e iOS 16.4+ |
| RNF-027 | Publicar una solicitud en ≤ 60 s; responder «Sí» en ≤ 3 toques; «No lo tengo» en 1 toque |
| RNF-028 | Lighthouse de accesibilidad ≥ 90, texto base ≥ 16 px, botones ≥ 44 × 44 px, contraste WCAG 2.1 AA |
| RNF-008 | Token de acceso de 15 min; refresco en cookie `HttpOnly`, `Secure`, `SameSite=Strict` |
| Estrategia de pruebas | Unitarias primero; punta a punta con Playwright solo en 3 a 5 flujos críticos en el Cierre; cada ejecución queda registrada en el libro de control |
| Plan de proyecto | Entrega final el 26/11/2026. Hoy (08/11) quedan unas dos semanas y media de construcción para el frontend |

La última fila es la que más pesa en el análisis: todo lo que se adopte tiene que caber en ese plazo con una persona principal en el frontend.

---

## 1. Resumen de decisiones

| # | Tema | Decisión | Estado |
|---|---|---|---|
| F-01 | Lenguaje | TypeScript en modo estricto | Decidida (Pedro, 08/11) |
| F-02 | Andamiaje | Vite 8 + React 19, TypeScript 5.9, Node 24 LTS, npm; React Router 8 en modo librería (sin modo *framework*) | Decidida (Pedro, 08/11) |
| F-03 | Datos del servidor | TanStack Query para todo lo que viene de la API; sin Redux | Decidida (arquitectura) |
| F-04 | Contrato con la API | Tipos generados desde OpenAPI (`openapi-typescript`) y cliente `openapi-fetch` | Decidida (Pedro, 08/11) |
| F-05 | Origen único | En desarrollo, proxy de Vite `/api → localhost:8080`; en producción Caddy sirve la PWA y la API en el mismo dominio | Decidida (Pedro, 08/11) |
| F-06 | Sesión | Token de acceso solo en memoria; renovación silenciosa con `/auth/refresco` | Decidida (Pedro, 08/11) |
| F-07 | Errores | `ProblemDetail` → mensaje por `codigo`; errores de campo al formulario; `idCorrelacion` visible en errores graves | Decidida (Pedro, 08/11) |
| F-08 | Accesibilidad | WCAG 2.2 AA (incluye todo 2.1 AA); APG como especificación de componentes | Decidida (Pedro, 08/11) |
| F-09 | Componentes base | shadcn/ui sobre Radix + Tailwind CSS v4 | Decidida (Pedro, 08/11) |
| F-10 | Tokens de diseño | Variables CSS en un solo archivo; sin Style Dictionary | Decidida (Pedro, 08/11) |
| F-11 | Formularios | React Hook Form + Zod, validación al salir del campo | Decidida (Pedro, 08/11) |
| F-12 | Textos e idioma | Español (`es-CO`), textos centralizados por funcionalidad, formato con `Intl`; sin librería de i18n | Decidida (Pedro, 08/11) |
| F-13 | PWA | `vite-plugin-pwa` (Workbox); el service worker **no** guarda respuestas de la API | Decidida (Pedro, 08/11) |
| F-14 | Tiempo real | SSE con `fetch` (cabecera `Authorization`), una conexión por usuario | Decidida (Pedro, 08/11); depende del backend (15.2) |
| F-15 | Mapas | Leaflet directo, cargado solo en las pantallas que lo usan | Decidida (arquitectura) |
| F-16 | Rendimiento | Core Web Vitals como meta interna, división por ruta, *skeletons* en listas | Decidida (Pedro, 08/11) |
| F-17 | Pruebas | Vitest + Testing Library (por rol) + axe-core; Playwright + `@axe-core/playwright` en los flujos del Cierre | Decidida (Pedro, 08/11) |
| F-18 | Calidad estática | oxlint con las reglas de `jsx-a11y`, Prettier, `tsc -b` en CI | Decidida (Pedro, 08/11) |
| F-19 | Lighthouse CI | Accesibilidad ≥ 90 bloquea; rendimiento ≥ 80 solo avisa | Decidida (Pedro, 08/11) |
| F-20 | Revisión manual | Teclado + NVDA (Windows) + TalkBack (Android) una vez por ciclo; heurísticas de Nielsen en cada pantalla nueva | Decidida (Pedro, 08/11) |
| F-21 | Usabilidad | ISO 9241-11 para definir la prueba con 5 usuarios de RNF-027 (eficacia, eficiencia y satisfacción con SUS) | Decidida (Pedro, 08/11) |
| F-22 | Fuera del MVP | Storybook, TanStack Virtual, modo oscuro, Style Dictionary, librería de i18n, lenguaje de diseño completo (Material, Fluent, Carbon) | Decidida (Pedro, 08/11) |
| F-23 | Git y CI | Mismo flujo que el backend; trabajo de CI propio para `frontend/`; el arquitecto de software (Daniel) aprueba los pull requests del frontend | Decidida (Pedro, 08/11) |

---

## 2. Análisis de la propuesta de estándares

Cada punto de la lista se compara con lo ya documentado. **Adoptar** = tal cual. **Adaptar** = sí, pero recortado o cambiado para este proyecto. **Fuera** = no entra en el MVP, con la razón.

### 2.1 Estándares de referencia

| Decidida (Pedro, 08/11) | Recomendación | Por qué |
|---|---|---|
| WCAG 2.2 AA | **Adoptar** y actualizar los documentos (aprobado) | El proyecto ya pide WCAG **2.1** AA (RNF-028, Plan Maestro 10.1, anexo G). La 2.2 contiene toda la 2.1 y agrega pocos criterios, y varios nos tocan de lleno: **2.4.11** foco no tapado (las barras fijas abajo en el celular suelen tapar el campo enfocado), **2.5.7** alternativa a arrastrar (el pin del mapa debe poder ubicarse sin arrastrar: «Usar mi ubicación» o elegir una dirección guardada), **2.5.8** tamaño de objetivo (ya lo cumplimos con 44 px), **3.3.7** no pedir dos veces el mismo dato (direcciones guardadas, repetir solicitud), **3.3.8** autenticación accesible (permitir pegar la frase de contraseña y usar el gestor de contraseñas; nada de captchas cognitivos). Es barato: son cinco filas más en el anexo G. Nota: la European Accessibility Act no aplica en Colombia; si se quiere un respaldo local, la Resolución 1519 de 2020 de MinTIC exige WCAG 2.1 AA a las entidades públicas (no a nosotros, pero sirve de referencia) |
| WAI-ARIA APG | **Adoptar como especificación**, sin trabajo extra | Si la librería base ya implementa los patrones (F-09), Monica solo abre el APG cuando tenga que construir algo que la librería no trae. Se cita en la revisión del pull request: «este combobox se comporta como dice el APG» |
| ISO 9241-110 / 9241-11 | **Adaptar** | El proyecto clasifica la calidad con ISO/IEC 25010, y ahí la usabilidad ya es una característica. La 9241-11 sirve para algo concreto: definir cómo se mide la prueba con 5 usuarios de RNF-027 (eficacia = tareas completadas, eficiencia = tiempo y toques, satisfacción = cuestionario SUS). La 9241-110 se cita como justificación en la documentación, no se audita punto por punto |
| Heurísticas de Nielsen | **Adoptar** | Cuestan casi nada y son la mejor revisión rápida de una pantalla. Se usan como lista en la revisión de diseño de cada pantalla nueva (sección 10.3). Varias ya están en los requisitos: visibilidad del estado (la solicitud muestra cuánto falta para vencer y si se amplió el radio), prevención de errores (confirmar antes de cancelar una orden), control del usuario (cancelar, deshacer) |

### 2.2 Sistema de diseño

| Decidida (Pedro, 08/11) | Recomendación | Por qué |
|---|---|---|
| Design tokens con Style Dictionary o variables CSS | **Adaptar: solo variables CSS** | Style Dictionary sirve cuando los tokens salen de una herramienta de diseño hacia varias plataformas (web, iOS, Android). Aquí hay una sola plataforma y una sola persona que diseña y programa. Un archivo `tokens.css` con variables (`--color-primario`, `--espacio-3`, `--texto-base: 1rem`) da el mismo beneficio. Tailwind v4 lee esas variables directamente con `@theme` |
| Componentes accesibles de base (React Aria, Radix, Ariakit, shadcn/ui) | **Adoptar uno: shadcn/ui sobre Radix** (aprobado) | Construir desde cero un modal, un menú o un selector accesibles es justo lo que no cabe en el plazo. shadcn/ui copia el código de cada componente al repositorio (Monica lo puede leer y ajustar), trae los colores como variables CSS (encaja con el punto anterior), tiene la mayor cantidad de ejemplos y es MIT. React Aria es técnicamente más fuerte en móvil y ya trae textos en español, pero su curva de aprendizaje es mayor. Ariakit tiene menos ejemplos. Se elige **una** y no se mezclan |
| Storybook con addon de accesibilidad | **Fuera del MVP** | Configurarlo y mantener las historias cuesta días que no hay, y con una sola persona en el frontend el catálogo tiene pocos lectores. Lo que sí se adopta de la idea: **cada componente con estados documenta y prueba sus cuatro estados** (cargando, vacío, error, deshabilitado) en su prueba de Vitest, y la revisión de accesibilidad automática se hace con axe-core en Vitest (F-17). Se puede agregar después del 26/11 sin rehacer nada |
| Lenguaje de diseño de referencia (Material 3, Fluent 2, Carbon) | **Adaptar: Material 3 solo como guía de patrones móviles** | Instalar una de esas librerías (MUI, Fluent UI, Carbon) pesa y choca con RNF-005 y con la elección de F-09. Lo útil de Material 3 son sus patrones para celular: barra de navegación inferior, botón principal flotante, hojas inferiores, tamaños táctiles. Se consultan, no se instalan |

### 2.3 Rendimiento percibido

| Decidida (Pedro, 08/11) | Recomendación | Por qué |
|---|---|---|
| Core Web Vitals (LCP < 2,5 s, INP < 200 ms, CLS < 0,1) | **Adoptar como meta interna** | El requisito formal es RNF-005 (Lighthouse ≥ 80 y ≤ 3 s en 4G). Los Core Web Vitals son lo que ese puntaje mide por dentro, así que sirven para saber qué arreglar. Ojo: INP solo se mide con usuarios reales; en Lighthouse se usa su equivalente de laboratorio (TBT, tiempo de bloqueo). No se agrega un requisito nuevo |
| Code splitting por ruta | **Adoptar** | Ya está en la arquitectura. Con React Router se hace con `lazy` en cada ruta. Las tres zonas (cliente, tienda, administrador) quedan en paquetes separados: un cliente nunca descarga el panel del administrador. Leaflet se carga solo en las pantallas con mapa |
| Skeletons en lugar de spinners | **Adoptar en listas, no en todo** | En las listas de respuestas, solicitudes y tiendas pendientes, el *skeleton* evita saltos de diseño (CLS). En un botón que envía un formulario, un indicador dentro del botón es lo correcto |
| Actualizaciones optimistas | **Adaptar: solo en cambios de bajo riesgo** | Sirve para «Abrir/Cerrar tienda» o editar el perfil. **No** se usa en elegir respuesta, responder solicitud ni cambiar el estado de una orden: ahí el servidor puede rechazar con `409` o `422` (por ejemplo, la solicitud ya venció), y mostrar un éxito falso confunde. En esos casos se muestra el estado «enviando» y se espera la respuesta |
| Virtualización con TanStack Virtual | **Fuera** | Las listas son cortas: las respuestas a una solicitud vienen de tiendas en 3 a 5 km, y las listas largas (historial, tiendas pendientes) ya vienen paginadas desde la API. Virtualizar además complica la accesibilidad (el lector de pantalla no ve lo que no está en el DOM). Se agrega solo si una medición lo pide |

### 2.4 Patrones de una SPA en React

| Decidida (Pedro, 08/11) | Recomendación | Por qué |
|---|---|---|
| Gestión del foco al cambiar de ruta | **Adoptar** | Barato y necesario: React Router no lo hace solo. Un componente `AnunciadorDeRuta` mueve el foco al `h1` de cada pantalla y actualiza `document.title` |
| Regiones `aria-live` para notificaciones | **Adoptar, con prioridad** | Es el corazón de la app: las respuestas y las solicitudes nuevas llegan por SSE sin que el usuario haga nada. Ya está en el anexo G (punto 8, criterio 4.1.3). Una región `polite` para avisos («Llegó una respuesta de Droguería La 5») y una `assertive` solo para errores que bloquean |
| React Hook Form + Zod, validar al salir del campo, `aria-describedby` | **Adoptar** | Encaja con el backend: el `ProblemDetail` de validación trae `errores[{campo, mensaje}]` y se pasa directo a `setError`. Zod valida la forma en el navegador; las reglas de negocio las sigue decidiendo el backend (igual que D-10) |
| Error boundaries con reintentar | **Adoptar** | Con el `errorElement` de cada ruta de React Router y el reinicio de TanStack Query. El mensaje dice qué pasó y qué hacer, y en errores `GEN-008` muestra el `idCorrelacion` para que el equipo lo busque en los logs |
| i18n desde el inicio | **Adaptar: textos centralizados, sin librería** | El argumento es cierto en productos que van a varios países. Aquí el producto es para Colombia, la demo es en español y no hay un segundo idioma en ningún requisito. Lo que sí se hace, porque cuesta lo mismo: los textos de cada funcionalidad viven en un archivo `textos.ts` (no regados por los componentes), `<html lang="es-CO">`, y dinero y fechas siempre con `Intl` (`es-CO`, `COP` sin decimales, zona `America/Bogota`). Si algún día hay otro idioma, pasar esos archivos a `react-i18next` es mecánico |
| `prefers-reduced-motion` | **Adoptar** | Una regla CSS global y transiciones cortas |
| `prefers-color-scheme` (modo oscuro) | **Fuera del MVP** | Duplica la verificación de contraste de cada pantalla (RNF-028) y no está en ningún requisito. Se declara `color-scheme: light` para que el navegador no invierta colores por su cuenta. Con los colores en variables CSS, agregarlo después es cambiar un archivo |

### 2.5 Control de calidad automatizado

| Decidida (Pedro, 08/11) | Recomendación | Por qué |
|---|---|---|
| `eslint-plugin-jsx-a11y` | **Adoptar sus reglas, con oxlint** | Gratis una vez configurado; atrapa imágenes sin `alt`, botones sin nombre, `onClick` en un `div`. Al crear el andamiaje se vio que `eslint-plugin-jsx-a11y` todavía no soporta ESLint 10; oxlint (el linter que trae la plantilla actual de Vite) implementa las mismas reglas de `jsx-a11y` y es mucho más rápido |
| Testing Library con `getByRole` | **Adoptar**, con Vitest | Es la prueba unitaria del frontend que pide la estrategia ("Monica en frontend"). Vitest es el corredor natural con Vite. Buscar por rol obliga a usar HTML semántico, que es medio camino de la accesibilidad |
| axe-core en Playwright | **Adaptar: en los flujos que ya están planeados** | La estrategia ya tiene Playwright con 3 a 5 flujos críticos en el Cierre (Yanimar). Agregar `@axe-core/playwright` a esos mismos flujos es una línea por pantalla. No se crea una batería E2E aparte. Antes del Cierre, axe-core revisa los componentes en cada pull request |
| Lighthouse CI con umbrales que bloquean el merge | **Adaptar** | Se corre sobre el build de producción (`vite preview`). La accesibilidad ≥ 90 **bloquea** (es estable y es RNF-028). El rendimiento ≥ 80 **solo avisa**, porque en las máquinas de GitHub Actions varía de una ejecución a otra y bloquearía pull requests sin motivo; la medición oficial de RNF-005 la hace Francisco en el VPS en el C3 |
| Pruebas manuales con teclado y lector de pantalla | **Adaptar a una vez por ciclo** | En C2 y C3, Francisco y Monica recorren el flujo principal con teclado, con **NVDA** (el equipo trabaja en Windows) y con **TalkBack** (la mayoría de usuarios en Colombia usa Android). VoiceOver solo si alguien tiene iPhone. El resultado va al anexo G del libro de control, como ya pide la estrategia |

---

## 3. Pila y andamiaje (F-01, F-02)

| Pieza | Elección | Licencia | Nota |
|---|---|---|---|
| Lenguaje | TypeScript 5.9 estricto | Apache 2.0 | TypeScript 7 (la versión nueva en Go) todavía no es compatible con `openapi-typescript` ni con las herramientas de lint; se sube cuando lo sean. Los tipos del contrato OpenAPI y de Zod son la razón principal |
| Entorno | Node 24 LTS, npm | MIT | npm para no agregar otra herramienta en Windows |
| Empaquetador | Vite (versión estable actual) con `@vitejs/plugin-react` | MIT | Objetivo de compilación por defecto de Vite, compatible con Safari 16.4+ (RNF-026) |
| UI | React 19 | MIT | |
| Rutas | React Router (modo librería, `createBrowserRouter`) | MIT | Sin modo *framework* ni renderizado en servidor: Caddy sirve archivos estáticos |
| Datos | TanStack Query v5 | MIT | |
| Formularios | React Hook Form + Zod + `@hookform/resolvers` | MIT | |
| Estilos | Tailwind CSS v4 | MIT | Tailwind v4 pide Safari 16.4+, Chrome 111+ y Firefox 128+: coincide con RNF-026 |
| Componentes | shadcn/ui (Radix Primitives) | MIT | |
| Íconos | `lucide-react` | ISC | Solo los que se usan (se importan uno por uno) |
| Contrato | `openapi-typescript` + `openapi-fetch` | MIT | |
| SSE | `@microsoft/fetch-event-source` | MIT | Ver F-14 |
| PWA | `vite-plugin-pwa` (Workbox) | MIT | |
| Mapas | `leaflet` | BSD-2 | Sin `react-leaflet` (Tabla 18) |
| Fotos | Reducción con `canvas` propio o `browser-image-compression` | MIT | Antes de subir documentos de la tienda (máx. 5 MB, JPEG, PNG, WebP, PDF) |
| Pruebas | Vitest, Testing Library, `@testing-library/user-event`, axe-core (ayuda propia en `src/pruebas/accesibilidad.ts`), MSW | MIT, MPL 2.0 (axe-core) | MSW simula la API en las pruebas y mientras el backend no tiene la historia lista |
| Calidad | oxlint (con los plugins `jsx-a11y`, `react`, `typescript`, `vitest`), Prettier | MIT | |

Las versiones exactas se fijan al crear el andamiaje y se revisan las licencias de nuevo, como pide la arquitectura.

---

## 4. Estructura de carpetas

Se respeta la estructura que ya existe en `frontend/src/` y se le da un significado fijo:

```
frontend/
├── index.html                 ← lang="es-CO"
├── vite.config.ts             ← proxy /api, PWA, alias @/
├── public/                    ← íconos de la PWA, manifest
└── src/
    ├── app/                   ← arranque: rutas, proveedores (Query, sesión), AnunciadorDeRuta, layout por rol
    ├── assets/                ← imágenes y fuentes
    ├── components/            ← componentes compartidos sin lógica de negocio
    │   └── ui/                ← componentes de shadcn/ui (Boton, Dialogo, Campo...)
    ├── features/              ← una carpeta por módulo del backend
    │   ├── identidad/         ← registro, inicio de sesión, segundo factor
    │   ├── clientes/          ← perfil y direcciones
    │   ├── tiendas/           ← registro, perfil, horario, domicilio, medios de pago, apertura
    │   ├── solicitudes/       ← publicar, ver respuestas, responder
    │   ├── ordenes/
    │   ├── reputacion/
    │   ├── notificaciones/    ← conexión SSE y push
    │   └── administracion/
    ├── hooks/                 ← hooks compartidos (useMediaQuery, useAnunciar)
    ├── pages/                 ← una pantalla por ruta; arma componentes de features
    ├── services/              ← cliente HTTP, sesión, tipos generados de OpenAPI, mapa de errores
    └── styles/                ← tokens.css y estilos globales
```

Dentro de cada `features/<modulo>/`: `api.ts` (consultas y mutaciones de TanStack Query), `esquemas.ts` (Zod), `textos.ts`, los componentes y sus pruebas al lado (`TarjetaRespuesta.test.tsx`).

Regla equivalente a las fronteras del backend: una `feature` no importa archivos internos de otra; si necesita algo compartido, se mueve a `components/`, `hooks/` o `services/`.

---

## 5. Idioma y nombres (mismas reglas que D-03 y D-04)

- **El negocio en español**, con el glosario de `DECISIONES-BACKEND` sección 3.4 (solicitud, respuesta, orden, tienda, domicilio, resena...). Sin tildes ni eñes en identificadores.
- **Lo técnico de React en inglés** solo donde es un prefijo o sufijo de la herramienta: `use` en los hooks, `Provider`, `.test.tsx`.
- Componentes en `PascalCase`, sustantivo del negocio: `TarjetaRespuesta`, `FormularioPublicarSolicitud`, `InterruptorAperturaTienda`, `ListaSolicitudesNuevas`.
- Hooks: `use` + lo que hacen: `useSesion`, `usePublicarSolicitud`, `useRespuestasDeSolicitud`.
- Páginas con sufijo `Pagina`: `PublicarSolicitudPagina`, `PanelTiendaPagina`.
- Rutas de la PWA en español y `kebab-case`: `/solicitudes/nueva`, `/tienda/medios-pago`, `/admin/tiendas-pendientes`.
- Los nombres de campos siguen exactamente el JSON de la API (`tarifaEnPesos`, `radioEnMetros`), que llega tipado desde OpenAPI.
- Booleanos como pregunta (`estaAbierta`, `puedeCalificar`), igual que en el backend.

---

## 6. Comunicación con la API (F-04 a F-07)

### 6.1 Origen único (F-05)

La cookie de refresco es `SameSite=Strict` y su ruta es `/api/v1/auth`. Lo más simple y seguro es que el navegador vea la PWA y la API en **el mismo origen**:

- **Desarrollo:** el proxy de Vite manda `/api` a `http://localhost:8080`. El frontend siempre llama rutas relativas (`/api/v1/...`), no hay CORS de por medio y la cookie viaja sin configuración extra. Se prueba en Chrome o Edge, que tratan `localhost` como contexto seguro para la cookie `Secure`.
- **Producción:** Caddy sirve el build de `frontend/dist` en `/` (con *fallback* a `index.html` para las rutas de la SPA) y pasa `/api/*` a la API, todo en el mismo dominio.
- El CORS que ya tiene el backend (`QLT_CORS_ORIGENES`, por defecto `http://localhost:5173`) queda como respaldo, por si alguien corre el frontend sin el proxy.

### 6.2 Tipos desde OpenAPI (F-04)

- Con el backend en perfil `dev`, `npm run api:tipos` descarga `/v3/api-docs` y genera `src/services/api-tipos.ts` con `openapi-typescript`. El archivo generado se sube al repositorio, para que el frontend compile aunque el backend no esté corriendo.
- `openapi-fetch` usa esos tipos: si una ruta o un campo cambia en el backend, el frontend deja de compilar en vez de fallar en la demo.
- Mientras una historia no esté en el backend, Monica trabaja con MSW simulando la respuesta descrita en el contrato (como ya dice el backlog del Sprint 1).

### 6.3 Sesión (F-06)

- El token de acceso (15 min) vive **solo en memoria** (un módulo de `services/sesion.ts`), nunca en `localStorage` ni `sessionStorage`: si un script malicioso entrara, no tendría dónde leerlo. El token de refresco no lo ve el JavaScript (cookie `HttpOnly`).
- Al abrir la app, se llama `POST /api/v1/auth/refresco`: si la cookie es válida, la sesión se recupera sin pedir la frase otra vez.
- Si una petición responde `401` con `GEN-003`, el cliente HTTP renueva **una sola vez** (si hay varias peticiones a la vez, todas esperan la misma renovación) y repite la petición. Si la renovación responde `IDE-010`, se limpia la sesión y se lleva al inicio de sesión con el mensaje «Tu sesión terminó. Vuelve a entrar.».
- El administrador: si `/auth/login` devuelve `segundoFactorRequerido=true`, se pasa a la pantalla del código TOTP (`/auth/totp`).
- Las rutas se protegen por rol (`CLIENTE`, `TIENDA`, `ADMIN`) con un componente `RutaProtegida`. Es solo comodidad: la autorización real la hace el backend.

### 6.4 Errores (F-07)

- Un único `services/errores.ts` convierte cada `ProblemDetail` en un error tipado con `codigo`, `status`, `detail`, `errores` e `idCorrelacion`.
- `src/services/mensajes-error.ts` tiene un mensaje para el usuario por cada código de `docs/api/CODIGOS-DE-ERROR.md` que diga **qué pasó y cómo seguir**. Ejemplo: `IDE-001` → «Ya existe una cuenta con ese correo. ¿Quieres iniciar sesión?» con el enlace. Si llega un código que no está en la tabla, se usa el `detail` del backend.
- Errores de validación (`GEN-001` con `errores[]`): cada `campo` va al campo del formulario con `setError`, y el foco pasa al primer campo con error.
- `429` (límite de solicitudes o de intentos): se muestra cuánto falta usando la cabecera `Retry-After`.
- `GEN-008` y fallas de red: mensaje general, botón «Reintentar» y el `idCorrelacion` en letra pequeña para soporte.
- Cada pull request que agregue un código en el backend agrega su mensaje aquí; Wilson lo revisa con la tabla de códigos.

---

## 7. Tiempo real y PWA (F-13, F-14)

### 7.1 SSE

- `EventSource` del navegador **no permite enviar la cabecera `Authorization`**, y la API se autentica con el JWT en esa cabecera. Por eso se usa `@microsoft/fetch-event-source`, que abre el flujo con `fetch` y sí la envía, y además reintenta con espera creciente y renueva el token si recibe `401`.
- Una sola conexión por usuario, abierta desde `features/notificaciones` mientras la app está visible. Cada evento invalida la consulta correspondiente de TanStack Query (por ejemplo, `RespuestaRecibida` refresca las respuestas de esa solicitud) y anuncia el cambio en la región `aria-live`.
- Esto hay que acordarlo con Daniel antes de que construya `/api/v1/eventos` en el Sprint 2 (sección 15.2).

### 7.2 PWA

- `vite-plugin-pwa` genera el `manifest` (nombre, íconos, `lang: es-CO`, `display: standalone`) y el service worker.
- El service worker guarda **solo los archivos estáticos** de la aplicación. **No** guarda respuestas de `/api`: tienen datos personales (Ley 1581, RN-008) y deben estar siempre al día. Sin conexión, la app muestra una pantalla que lo dice claramente.
- Actualización: cuando hay una versión nueva, se muestra un aviso «Hay una versión nueva. Actualizar» en lugar de recargar sola en medio de una orden.
- Push web para la tienda: se pide el permiso **después** de un gesto del usuario y explicando para qué sirve («Activa los avisos para enterarte de las solicitudes aunque tengas la app cerrada»), nunca al abrir la app. En iPhone se muestran primero las instrucciones para instalar la PWA (RA-05).

---

## 8. Accesibilidad y usabilidad en el código (F-08)

Reglas que se revisan en cada pull request del frontend:

1. **HTML semántico primero**: `button` para acciones, `a` para navegar, `label` en cada campo, un solo `h1` por pantalla, `main`, `nav` y `header` como regiones.
2. **Texto base de 16 px** (`1rem`) y tamaños en `rem` para que el zoom al 200 % funcione (RNF-028, 1.4.4).
3. **Objetivos táctiles de 44 × 44 px o más** con al menos 8 px entre ellos (RNF-028). Los botones «Sí lo tengo», «Parecido» y «No lo tengo» son grandes y están al alcance del pulgar.
4. **Foco visible** siempre (anillo de 2 px con contraste ≥ 3:1) y **nunca tapado** por barras fijas (2.4.11): `scroll-padding` del alto de esas barras.
5. **Contraste** de texto ≥ 4,5:1 y de bordes de controles ≥ 3:1, verificado al definir los tokens, no pantalla por pantalla.
6. **No solo el color** comunica estado: «Abierta/Cerrada», el estado de la orden y los errores llevan texto o ícono además del color.
7. **Avisos en tiempo real** por la región `aria-live` (sección 7.1). Los *toasts* no desaparecen antes de 5 s y se pueden cerrar.
8. **Formularios** según la sección 9.
9. **Mapa**: todo lo que se hace en el mapa se puede hacer sin él (lista de tiendas con distancia, dirección escrita, botón «Usar mi ubicación»). El mapa no atrapa el foco del teclado. Sin arrastrar obligatorio (2.5.7).
10. **Sin desplazamiento horizontal a 320 px** (1.4.10): se diseña primero para celular.
11. **Movimiento reducido**: animaciones cortas y apagadas con `prefers-reduced-motion`.
12. **Ayuda consistente** (3.2.6): el enlace de ayuda o de contacto siempre en el mismo lugar.

Para cumplir RNF-027 (rapidez) se diseñan así los dos flujos medidos:

- **Publicar solicitud (≤ 60 s):** un solo campo de texto con foco al entrar, la ubicación ya elegida (última dirección usada o la actual) y el botón «Preguntar» visible sin desplazarse. Cantidad y detalles son opcionales.
- **Responder (Sí ≤ 3 toques, No en 1):** la tarjeta de la solicitud nueva tiene «No lo tengo» directo (1 toque, con opción de deshacer durante unos segundos en vez de un diálogo de confirmación) y «Sí lo tengo» abre precio y unidades ya precargados (toque 2 para ajustar, toque 3 para enviar).

---

## 9. Formularios (F-11)

- React Hook Form con `mode: 'onTouched'`: valida al salir del campo y, una vez hay error, revalida mientras se escribe para que el error desaparezca apenas se corrige.
- Esquemas Zod en `features/<modulo>/esquemas.ts`, con los mismos límites del backend (frase de 15 a 128 caracteres, correo de máximo 254). Los mensajes dicen cómo corregir: «La frase debe tener al menos 15 caracteres. Llevas 9.» y no «Campo inválido».
- Cada error se asocia al campo con `aria-describedby` y el campo lleva `aria-invalid`. Las pistas («Mínimo 15 caracteres; puedes usar espacios») se muestran **antes** del error, no solo cuando falla.
- Al enviar con errores, el foco va al primer campo con error y arriba aparece un resumen con enlaces a cada campo.
- Frase de contraseña: botón «Mostrar», contador de caracteres, se puede pegar, `autocomplete="new-password"` en el registro y `current-password` en el inicio de sesión (3.3.8). El código TOTP con `autocomplete="one-time-code"` e `inputmode="numeric"`.
- Campos con el teclado correcto en el celular: `type="email"`, `inputmode="numeric"` para precios y teléfonos, `type="tel"`.
- Precios en pesos enteros, sin decimales, mostrados con `Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })`.
- El botón de enviar se deshabilita **solo mientras se envía** (con el indicador dentro), no mientras el formulario tiene errores: un botón gris sin explicación es peor que mostrar los errores.

---

## 10. Componentes, tokens y estados (F-09, F-10)

### 10.1 Tokens

Un solo archivo `src/styles/tokens.css` con variables CSS: colores por función (`--color-primario`, `--color-exito`, `--color-peligro`, `--color-texto`, `--color-fondo`, `--color-borde`), tipografía (`--texto-base: 1rem`, escala de tamaños), espacios (múltiplos de 4 px), radios y sombras. Tailwind v4 los toma con `@theme`, y shadcn/ui ya usa variables con ese mismo esquema. Ningún componente escribe un color en hexadecimal suelto. Monica decide la paleta y la identidad visual junto con los prototipos; los valores iniciales del andamiaje son provisionales y solo cumplen el contraste.

### 10.2 Estados obligatorios

Todo componente que muestra datos del servidor maneja y prueba estos estados:

| Estado | Qué ve el usuario |
|---|---|
| Cargando | *Skeleton* con la forma del contenido (listas) o indicador en el botón (acciones) |
| Vacío | Mensaje que dice qué pasa y qué hacer: «Todavía no hay respuestas. Te avisamos apenas una tienda conteste.» |
| Error | Qué pasó, botón «Reintentar» y, si aplica, el `idCorrelacion` |
| Deshabilitado | Con la razón visible: «Tu tienda está cerrada. Ábrela para recibir solicitudes.» |

### 10.3 Revisión de cada pantalla nueva

Antes de abrir el pull request de una pantalla, Monica revisa: las 10 heurísticas de Nielsen (una línea por heurística en la descripción del pull request, solo las que aplican), recorrido con teclado y prueba a 320 px de ancho.

---

## 11. Rendimiento (F-16)

- Metas: las de RNF-005 en el VPS; internamente LCP < 2,5 s, CLS < 0,1 y TBT bajo en Lighthouse móvil.
- División por ruta con `lazy`; las zonas de cliente, tienda y administrador en paquetes distintos; Leaflet solo en las pantallas con mapa.
- Presupuesto: el JavaScript inicial que descarga un cliente (comprimido) se revisa en cada pull request con el reporte de `vite build`; si crece mucho, se explica en el pull request.
- Imágenes en WebP con `width` y `height` declarados (evita CLS). Fuentes del sistema o una sola fuente propia con `font-display: swap`.
- TanStack Query con `staleTime` razonable (por ejemplo 30 s para listas que también se actualizan por SSE) para no repetir peticiones.
- Las fotos de los documentos de la tienda se reducen en el navegador antes de subirlas (arquitectura).

---

## 12. Pruebas y calidad (F-17 a F-21)

Encaja con la estrategia de pruebas: unitarias primero, punta a punta solo en el Cierre.

| Nivel | Qué | Herramienta | Cuándo | Quién |
|---|---|---|---|---|
| Unitarias | Esquemas Zod, mapa de errores, formato de dinero y fechas, renovación de la sesión, hooks | Vitest | Cada pull request | Monica |
| Componentes | Formularios, `TarjetaRespuesta`, interruptor de apertura, sus cuatro estados; accesibilidad automática | Vitest + Testing Library (`getByRole`) + axe-core + MSW | Cada pull request | Monica |
| Estático | Tipos, reglas de React y de accesibilidad, formato | `tsc -b`, oxlint (`jsx-a11y`), Prettier | Cada pull request | CI |
| Lighthouse | Accesibilidad ≥ 90 (bloquea), rendimiento ≥ 80 (avisa) | Lighthouse CI sobre `vite preview` | Cada pull request a `main` desde que haya pantallas | CI; Francisco revisa |
| Punta a punta | Los 3 a 5 flujos de la estrategia, con revisión axe en cada pantalla | Playwright + `@axe-core/playwright` | C3 (Cierre) | Yanimar |
| Manual | Teclado, NVDA, TalkBack, matriz de navegadores (RNF-026) | Anexo G | C2 y C3 | Francisco con Monica |
| Usabilidad | Prueba con 5 usuarios de RNF-027 | ISO 9241-11 + SUS | C3 | Monica la conduce, Francisco registra |

Convenciones (las mismas del backend, adaptadas):

- Archivo `<Componente>.test.tsx` al lado del componente.
- Nombre de la prueba que describe el comportamiento, en español: `it('muestra el error del correo cuando ya tiene cuenta')`.
- Etiquetas de trazabilidad en el título del `describe`: `describe('HU-001 · CP-IDE-001-02 · Registro de cliente', ...)`, para poder filtrar con `vitest -t "HU-001"` y llevarlo al libro de control.
- Cuerpo en tres bloques: `// Dado`, `// Cuando`, `// Entonces`.
- Cobertura: se reporta con `@vitest/coverage-v8` en cada ejecución. La meta del 70 % de la definición de terminado se aplica a `services/` y a los esquemas y hooks de `features/` (la lógica); no se exige sobre componentes visuales.

---

## 13. Git, CI y despliegue (F-23)

- Mismo flujo que el backend (D-22): ramas cortas desde `main` (`feature/HU-016-publicar-solicitud-pwa`), Conventional Commits en español con el alcance `front` (`feat(front): pantalla de registro de cliente`), pull request pequeño con *squash*.
- Aprobador de los pull requests del frontend: el arquitecto de software (Daniel), igual que en el backend.
- Un flujo propio, `.github/workflows/frontend.yml`, que solo corre si cambian archivos de `frontend/`: `npm ci`, formato, `npm run lint`, `npm run tipos`, `npm run test:cobertura`, `npm audit`, `npm run build` y Lighthouse CI. Los escaneos de seguridad que ya existen (Trivy, Gitleaks) cubren también `frontend/`; se agrega `npm audit --audit-level=high --omit=dev` para RNF-011.
- `frontend/.gitignore` con `node_modules/` y `dist/`. **Ojo:** el `.gitignore` de la raíz ignora `.env.*`; los archivos de Vite `.env.development` no deben llevar secretos (el frontend no tiene secretos: todo lo que va al navegador es público), así que se usan solo para valores como `VITE_NOMBRE_APP` o la llave pública VAPID.
- Despliegue: el `Dockerfile` de Caddy compila el frontend en una etapa con Node y copia `dist/` a la imagen de Caddy. Así RNF-029 sigue siendo un `docker compose up`.

---

### 13.1 Dependencias vulnerables (RNF-011)

- Cuando una alerta de Dependabot o de `npm audit` cae en una dependencia transitiva, primero se actualiza el paquete padre. Si el padre todavía no la actualizó, se fuerza la versión corregida con `overrides` en `frontend/package.json`, se comprueba que el build y las pruebas siguen pasando y se anota aquí.
- Las alertas sin versión corregida se descartan en GitHub con su justificación, nunca en silencio, y se anotan aquí.

| Fecha | Alerta | Qué se hizo |
|---|---|---|
| 08/11/2026 | `minimatch` (GHSA-g3hv-75wg-m249, moderada) y `glob` (GHSA-3g3m-2fjr-6pw4), transitivas de `vite-plugin-pwa` → `workbox-build` | `overrides: { "glob": "^14.0.4" }`, que trae `minimatch` 11.0.5 corregido. El service worker generado sigue precargando los mismos 14 archivos |
| 08/11/2026 | `deepmerge` (GHSA-gcqx-w48m-w2h4, baja), transitiva de `workbox-build` → `@rollup/plugin-node-resolve` | No hay versión corregida (todas hasta la 4.3.1 están afectadas y el paquete no se publica desde 2023). Solo se usa al compilar, una vez, para copiar las opciones internas del plugin (`deepMerge({}, defaults)`), sin datos externos, y no llega al navegador. Se descarta en GitHub como «riesgo tolerable» y se revisa cuando `workbox-build` cambie de dependencia |

## 14. Fuera del MVP (F-22)

| Qué | Por qué no ahora | Cuándo tendría sentido |
|---|---|---|
| Storybook | Días de configuración y mantenimiento para un catálogo con un solo lector | Si el equipo de frontend crece |
| TanStack Virtual | Listas cortas y paginadas | Si una lista pasa de unos cientos de elementos |
| Modo oscuro | Duplica la verificación de contraste; no es requisito | Después del 26/11; las variables CSS lo dejan preparado |
| Style Dictionary | Una sola plataforma | Si hay app nativa además de la web |
| `react-i18next` o FormatJS | Un solo idioma y un solo país | Si se abre otro país; los textos ya están centralizados |
| Material 3, Fluent o Carbon como librería | Peso y choque con shadcn/ui | No se planea; Material 3 se consulta como guía |

---

## 15. Decisiones pendientes

### 15.1 Respuestas de Pedro (08/11/2026)

1. TypeScript estricto (F-01).
2. WCAG 2.2 AA pasa a ser el requisito: se actualizan el Plan Maestro (10.1), el anexo G y el caso NF-USA-028. RNF-028 en la SRS y en el plan de proyecto (Word y PDF) debe decir también 2.2 en su próxima versión.
3. shadcn/ui sobre Radix con Tailwind v4 (F-09).
4. Monica hace los prototipos y decide la paleta. El arquitecto de software (Daniel) aprueba los pull requests del frontend.

### 15.2 Para el equipo de backend (Daniel)

1. **Autenticación del SSE:** confirmar que `/api/v1/eventos` acepta el JWT en la cabecera `Authorization` (el frontend usará `fetch-event-source`). La alternativa sería un token corto de un solo uso en la URL.
2. **Caddy:** servir `frontend/dist` y la API en el mismo dominio (sección 6.1), con *fallback* a `index.html` y caché larga para los archivos con huella (`/assets/*`).
3. **Llave pública VAPID:** exponerla en un endpoint público (por ejemplo `GET /api/v1/notificaciones/llave-publica`) para no tenerla duplicada en el frontend.

---

## Historial

| Versión | Fecha | Cambio |
|---|---|---|
| 0.1 | 08/11/2026 | Primera propuesta: análisis de la lista de estándares contra lo documentado y decisiones F-01 a F-23 |
| 1.0 | 08/11/2026 | Pedro aprueba todas las decisiones: TypeScript, WCAG 2.2 AA, shadcn/ui con Tailwind; Monica define prototipos y paleta; Daniel aprueba los pull requests del frontend |
| 1.1 | 08/11/2026 | Ajustes al crear el andamiaje: oxlint en lugar de ESLint (mismas reglas `jsx-a11y`), TypeScript 5.9 en lugar de 7, axe-core directo en lugar de `vitest-axe`, flujo de CI propio `frontend.yml` |
| 1.2 | 08/11/2026 | Sección 13.1: manejo de dependencias vulnerables; `overrides` de `glob` para la alerta de `minimatch` y justificación de `deepmerge` |
