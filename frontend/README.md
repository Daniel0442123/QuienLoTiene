# Frontend · ¿Quién lo tiene?

PWA en React + TypeScript. Las reglas están en [`docs/arquitectura/DECISIONES-FRONTEND.md`](../docs/arquitectura/DECISIONES-FRONTEND.md); léelas antes de la primera pantalla.

## Arrancar

Requisitos: Node 24 LTS y el backend corriendo en `http://localhost:8080` con el perfil `dev`.

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173 ; /api va al backend por el proxy de Vite
```

Usa Chrome o Edge en desarrollo: tratan `localhost` como seguro y aceptan la cookie `Secure` del token de refresco.

### Sin backend (API simulada)

```bash
npm run dev:simulado # http://localhost:5173 ; /api lo responde MSW en el navegador
```

- Las respuestas salen de `src/mocks/` (`manejadores-*.ts`, uno por módulo), con la forma de los DTOs del backend y los códigos de `docs/api/CODIGOS-DE-ERROR.md`. Las mismas respuestas usan las pruebas.
- Los datos viven en memoria (`src/mocks/datos.ts`) y vuelven al estado inicial al recargar. Tampoco hay cookie de refresco: **al recargar la página hay que volver a entrar**.
- Usuarios de prueba (todos con `FRASE_DE_PRUEBA` de `src/mocks/datos.ts`):

    | Correo                     | Para probar                                              |
    | -------------------------- | -------------------------------------------------------- |
    | `cliente@ejemplo.com`      | Cliente con una dirección guardada                       |
    | `tienda@ejemplo.com`       | Tienda aprobada y abierta, con horario y domicilio       |
    | `tienda.nueva@ejemplo.com` | Tienda pendiente de revisión (no puede abrir: `TIE-008`) |
    | `admin@ejemplo.com`        | Administrador; pide el código `CODIGO_TOTP_DE_PRUEBA`    |
    | `suspendido@ejemplo.com`   | Cuenta suspendida (`IDE-009`)                            |

- Las categorías (`CATEGORIAS_SIMULADAS`) solo existen en la simulación: el backend aún no publica un endpoint de categorías.
- El service worker de MSW se sirve solo en este modo, desde `node_modules` (plugin en `vite.config.ts`); `main.tsx` carga la simulación con un `import` dinámico que desaparece del build de producción.
- Necesita un navegador que permita service workers (Chrome o Edge). El navegador integrado del escritorio de Claude no los permite: ahí la app abre, pero sin API.

## Comandos

| Para qué                                | Comando                                              |
| --------------------------------------- | ---------------------------------------------------- |
| Servidor de desarrollo                  | `npm run dev`                                        |
| Pruebas (una vez)                       | `npm test`                                           |
| Pruebas mientras programas              | `npm run test:vigilar`                               |
| Pruebas de una historia                 | `npx vitest run -t "HU-003"`                         |
| Cobertura (meta 70 % en la lógica)      | `npm run test:cobertura` → `coverage/index.html`     |
| Lint (incluye accesibilidad `jsx-a11y`) | `npm run lint`                                       |
| Tipos                                   | `npm run tipos`                                      |
| Formato                                 | `npm run formato`                                    |
| Build de producción                     | `npm run build` y `npm run preview`                  |
| Lighthouse como en CI                   | `npm run build && npm run lighthouse`                |
| Regenerar tipos de la API               | `npm run api:tipos` (con el backend en perfil `dev`) |

Antes de abrir un pull request: `npm run formato && npm run lint && npm run tipos && npm test`.

## Dónde va cada cosa

| Carpeta                  | Qué                                                                                                               |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------- |
| `src/app/`               | Arranque: rutas (`rutas.tsx`), plantilla, `AnunciadorDeRuta`, `RutaProtegida`, error de ruta                      |
| `src/components/`        | Componentes compartidos: `CampoTexto`, estados (`EsqueletoLista`, `EstadoVacio`, `EstadoError`), `RegionAnuncios` |
| `src/components/ui/`     | Componentes base al estilo de shadcn/ui (`Boton`)                                                                 |
| `src/features/<modulo>/` | Una carpeta por módulo del backend: `api.ts`, `esquemas.ts`, `textos.ts`, componentes y sus pruebas               |
| `src/pages/`             | Una pantalla por ruta (`...Pagina.tsx`)                                                                           |
| `src/services/`          | Cliente HTTP (`clienteApi.ts`), sesión, errores y mensajes, formato de pesos y fechas, tipos de OpenAPI           |
| `src/styles/tokens.css`  | Colores, tipografía y tamaños. **Único lugar con colores** (paleta «Verde botica» y Atkinson Hyperlegible)        |
| `src/mocks/`             | Respuestas simuladas de la API (MSW) para pruebas y para avanzar sin el backend                                   |
| `src/pruebas/`           | Configuración y ayudas de las pruebas (axe, renderizar con proveedores)                                           |

`features/identidad/FormularioInicioSesion.tsx` y su prueba son el **ejemplo de referencia**: así se arma un formulario (Zod, errores accesibles, mensajes del backend) y así se prueba (por rol, con axe y con MSW).

## Pendiente de tu lado

- Agregar componentes de shadcn/ui a medida que se necesiten: `npx shadcn@latest add dialog` (revisa que queden en `src/components/ui/` y con los tokens).
- La primera vez que el backend esté arriba: `npm run api:tipos` y subir `src/services/api-tipos.ts`.

## Identidad visual

- **Paleta «B · Verde botica»** en `src/styles/tokens.css`, con el contraste de cada color anotado (texto ≥ 4,5:1, bordes y foco ≥ 3:1). Si se cambia un color, recalcular el contraste antes de subirlo. El manifest (`vite.config.ts`) y el `<meta name="theme-color">` de `index.html` repiten el primario `#0b6e63` porque no pueden leer variables CSS.
- **Atkinson Hyperlegible** (OFL 1.1) desde `@fontsource`, solo latino en regular y negrita, con `font-display: swap`. Como no tiene pesos intermedios, `font-medium` y `font-semibold` usan la negrita (`src/styles/index.css`).
- **Íconos** en `public/`: `icono.svg` (pestaña), `icono-192.png`, `icono-512.png` e `icono-maskable-512.png` (el pin queda dentro de la zona segura del 80 %). El «?» es un trazo, no texto, así que no depende de ninguna fuente.
