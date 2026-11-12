import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterAll, afterEach, beforeAll } from 'vitest'
import { reiniciarDatosSimulados } from '@/mocks/datos'
import { servidorSimulado } from '@/mocks/servidor'

// Envíos multipart (registro de tienda): jsdom trae sus propios FormData, Blob y File, pero fetch es el de Node, y el
// puente de Vitest entre los dos corta los archivos (RequestContentLengthMismatchError). En el navegador todo es de una
// pieza, así que en las pruebas se usan también los de Node. Se redefinen solo en el global (defineProperty): con `=`
// también cambiarían los de la ventana de jsdom y Vitest intentaría convertirlos de nuevo.
// process.getBuiltinModule evita importar node:buffer, cuyos tipos no están en el tsconfig de la app.
const moduloBuffer = (
  globalThis as unknown as { process: { getBuiltinModule: (nombre: string) => { Blob: unknown; File: unknown } } }
).process.getBuiltinModule('node:buffer')
const FormDataDeNode = (await new Response(new URLSearchParams('a=1')).formData()).constructor
for (const [nombre, valor] of Object.entries({
  FormData: FormDataDeNode,
  Blob: moduloBuffer.Blob,
  File: moduloBuffer.File,
})) {
  Object.defineProperty(globalThis, nombre, { value: valor, writable: true, configurable: true })
}

// MSW simula la API en las pruebas: ninguna prueba sale a la red
beforeAll(() => servidorSimulado.listen({ onUnhandledFrame: 'error' }))
afterEach(() => {
  servidorSimulado.resetHandlers()
  reiniciarDatosSimulados()
  cleanup()
})
afterAll(() => servidorSimulado.close())
