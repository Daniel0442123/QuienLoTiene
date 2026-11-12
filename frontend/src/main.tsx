import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Aplicacion } from '@/app/Aplicacion'
import '@/styles/index.css'

const raiz = document.getElementById('raiz')
if (!raiz) {
  throw new Error('No se encontró el elemento #raiz en index.html')
}

/**
 * Con `npm run dev:simulado` la API la responde MSW en el navegador (src/mocks/), sin backend.
 * La condición se resuelve al compilar: en producción el import desaparece y MSW no llega al paquete.
 */
async function prepararApi(): Promise<void> {
  if (import.meta.env.DEV && import.meta.env.MODE === 'simulado') {
    const { iniciarSimulacionApi } = await import('@/mocks/navegador')
    // Si el navegador no permite service workers, la app arranca igual (sin API) en vez de quedar en blanco
    await iniciarSimulacionApi().catch((error: unknown) => {
      console.error('No se pudo iniciar la simulación de la API. Usa Chrome o Edge en http://localhost:5173.', error)
    })
  }
}

void prepararApi().then(() => {
  createRoot(raiz).render(
    <StrictMode>
      <Aplicacion />
    </StrictMode>,
  )
})
