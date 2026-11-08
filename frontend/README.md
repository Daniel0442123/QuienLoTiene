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
| `src/styles/tokens.css`  | Colores, tipografía y tamaños. **Único lugar con colores**: aquí va tu paleta                                     |
| `src/mocks/`             | Respuestas simuladas de la API (MSW) para pruebas y para avanzar sin el backend                                   |
| `src/pruebas/`           | Configuración y ayudas de las pruebas (axe, renderizar con proveedores)                                           |

`features/identidad/FormularioInicioSesion.tsx` y su prueba son el **ejemplo de referencia**: así se arma un formulario (Zod, errores accesibles, mensajes del backend) y así se prueba (por rol, con axe y con MSW).

## Pendiente de tu lado

- Paleta, tipografía e íconos definitivos en `src/styles/tokens.css` y `public/` (los actuales son provisionales y cumplen contraste).
- Agregar componentes de shadcn/ui a medida que se necesiten: `npx shadcn@latest add dialog` (revisa que queden en `src/components/ui/` y con los tokens).
- La primera vez que el backend esté arriba: `npm run api:tipos` y subir `src/services/api-tipos.ts`.
