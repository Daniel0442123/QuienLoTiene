import { useRegisterSW } from 'virtual:pwa-register/react'
import { Boton } from '@/components/ui/Boton'

/**
 * Cuando hay una versión nueva de la PWA se avisa en vez de recargar sola, para no interrumpir a alguien en medio de
 * una orden (DECISIONES-FRONTEND sección 7.2).
 */
export function AvisoActualizacion() {
  const {
    needRefresh: [hayVersionNueva, setHayVersionNueva],
    updateServiceWorker,
  } = useRegisterSW()

  if (!hayVersionNueva) return null

  return (
    <div
      role="status"
      className="fixed inset-x-4 bottom-4 z-40 mx-auto flex max-w-md items-center justify-between gap-3 rounded-control border border-borde bg-fondo p-4 shadow-lg"
    >
      <p>Hay una versión nueva de la aplicación.</p>
      <div className="flex gap-2">
        <Boton variante="secundario" onClick={() => setHayVersionNueva(false)}>
          Después
        </Boton>
        <Boton onClick={() => updateServiceWorker(true)}>Actualizar</Boton>
      </div>
    </div>
  )
}
