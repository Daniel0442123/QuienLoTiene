import { setupWorker } from 'msw/browser'
import { manejadores } from './manejadores'

/**
 * Simulación de la API en el navegador. Solo se carga con `npm run dev:simulado` (modo de Vite "simulado"):
 * main.tsx la importa de forma dinámica, así que nunca entra en el build de producción.
 */
export async function iniciarSimulacionApi(): Promise<void> {
  const trabajador = setupWorker(...manejadores)
  await trabajador.start({
    // Solo se simula /api; lo demás (archivos de Vite, teselas del mapa) pasa sin aviso
    onUnhandledFrame({ frame, defaults }) {
      const { request } = frame.data as { request?: Request }
      if (request && new URL(request.url).pathname.startsWith('/api/')) defaults.warn()
    },
  })
}
