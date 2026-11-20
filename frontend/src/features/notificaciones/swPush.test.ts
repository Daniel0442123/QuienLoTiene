import { beforeEach, describe, expect, it, vi } from 'vitest'
import codigoSw from '../../../public/sw-push.js?raw'

/** Un service worker de mentira: guarda los manejadores que registra sw-push.js para dispararlos en la prueba. */
function cargarServiceWorker(ventanas: Array<{ url: string; focus: () => Promise<unknown> }> = []) {
  const manejadores = new Map<string, (evento: unknown) => void>()
  const showNotification = vi.fn().mockResolvedValue(undefined)
  const openWindow = vi.fn().mockResolvedValue(null)
  const self = {
    location: { origin: 'https://quienlotiene.tech' },
    registration: { showNotification },
    clients: { matchAll: vi.fn().mockResolvedValue(ventanas), openWindow },
    addEventListener: (tipo: string, manejador: (evento: unknown) => void) => manejadores.set(tipo, manejador),
  }
  new Function('self', codigoSw)(self)
  const disparar = async (tipo: string, evento: Record<string, unknown>) => {
    const esperas: Promise<unknown>[] = []
    manejadores.get(tipo)?.({ ...evento, waitUntil: (promesa: Promise<unknown>) => esperas.push(promesa) })
    await Promise.all(esperas)
  }
  return { disparar, showNotification, openWindow }
}

const ID = 'a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01'
const conDatos = (datos: unknown) => ({ data: { json: () => datos } })

describe('HU-031 · Service worker de avisos push · CP-NOT-031-01', () => {
  let sw: ReturnType<typeof cargarServiceWorker>
  beforeEach(() => {
    sw = cargarServiceWorker()
  })

  it('CP-NOT-031-01 · una solicitud nueva muestra un aviso sin datos personales que abre la solicitud', async () => {
    await sw.disparar('push', conDatos({ tipo: 'solicitud-nueva', id: ID }))
    expect(sw.showNotification).toHaveBeenCalledWith(
      'Solicitud nueva',
      expect.objectContaining({
        body: 'Un vecino pregunta si tienes un producto. Toca para responder.',
        lang: 'es-CO',
        tag: `solicitud-nueva-${ID}`,
        data: { url: `/tienda/solicitudes/${ID}` },
      }),
    )
  })

  it('un pedido nuevo o un cambio de estado abre el pedido de la tienda', async () => {
    await sw.disparar('push', conDatos({ tipo: 'orden-creada', id: ID }))
    await sw.disparar('push', conDatos({ tipo: 'estado-orden-cambiado', id: ID }))
    expect(sw.showNotification.mock.calls.map(([titulo, opciones]) => [titulo, opciones.data.url])).toEqual([
      ['Te eligieron: pedido nuevo', `/tienda/pedidos/${ID}`],
      ['Un pedido cambió', `/tienda/pedidos/${ID}`],
    ])
  })

  it('un id que no es UUID no entra en la ruta, y un tipo desconocido o sin datos no muestra nada', async () => {
    await sw.disparar('push', conDatos({ tipo: 'orden-creada', id: '../../admin' }))
    expect(sw.showNotification.mock.calls[0]?.[1].data).toEqual({ url: '/' })
    sw.showNotification.mockClear()
    await sw.disparar('push', conDatos({ tipo: 'otra-cosa', id: ID }))
    await sw.disparar('push', { data: null })
    await sw.disparar('push', { data: { json: () => JSON.parse('no es json') } })
    expect(sw.showNotification).not.toHaveBeenCalled()
  })

  it('al tocarlo abre la pantalla en una ventana nueva si la app está cerrada', async () => {
    const close = vi.fn()
    await sw.disparar('notificationclick', { notification: { close, data: { url: `/tienda/pedidos/${ID}` } } })
    expect(close).toHaveBeenCalled()
    expect(sw.openWindow).toHaveBeenCalledWith(`https://quienlotiene.tech/tienda/pedidos/${ID}`)
  })

  it('si la app ya está abierta, la enfoca y la lleva a la pantalla', async () => {
    const navigate = vi.fn().mockResolvedValue(undefined)
    const ventana = { url: 'https://quienlotiene.tech/tienda', focus: vi.fn().mockResolvedValue({ navigate }) }
    const conVentana = cargarServiceWorker([ventana])
    await conVentana.disparar('notificationclick', {
      notification: { close: vi.fn(), data: { url: `/tienda/solicitudes/${ID}` } },
    })
    expect(ventana.focus).toHaveBeenCalled()
    expect(navigate).toHaveBeenCalledWith(`https://quienlotiene.tech/tienda/solicitudes/${ID}`)
    expect(conVentana.openWindow).not.toHaveBeenCalled()
  })
})
