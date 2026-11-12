import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { reiniciarDatosSimulados } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'

// MSW simula la API en las pruebas: ninguna prueba sale a la red
beforeAll(() => servidorSimulado.listen({ onUnhandledFrame: 'error' }))
afterEach(() => {
  servidorSimulado.resetHandlers()
  reiniciarDatosSimulados()
  cleanup()
})
afterAll(() => servidorSimulado.close())
