import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { base, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from '@/mocks/datos'
import { CLAVE_PUBLICA_SIMULADA } from '@/mocks/manejadores-notificaciones'
import { servidorSimulado } from '@/mocks/servidor'
import { violacionesDeAccesibilidad } from '@/pruebas/accesibilidad'
import { renderizarConProveedores } from '@/pruebas/renderizar'
import { claveAplicacion } from '@/services/push'
import { iniciarSesion, limpiarSesion } from '@/services/sesion'
import { TarjetaAvisosPush } from './TarjetaAvisosPush'

/** Claves del tamaño que exige el backend: p256dh de 65 bytes y auth de 16, en base64url. */
const aBase64Url = (bytes: number) =>
  btoa(String.fromCharCode(...Array.from({ length: bytes }, (_, i) => i + 1)))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '')

function suscripcionFalsa(endpoint = 'https://fcm.googleapis.com/fcm/send/esta-tienda') {
  return {
    endpoint,
    unsubscribe: vi.fn().mockResolvedValue(true),
    toJSON: () => ({ endpoint, keys: { p256dh: aBase64Url(65), auth: aBase64Url(16) } }),
  }
}

/** Un navegador con Push API, un service worker registrado y el permiso en `permiso`. */
function prepararNavegador({
  permiso = 'default',
  respuestaPermiso = 'granted',
  suscripcion = null as ReturnType<typeof suscripcionFalsa> | null,
  nueva = suscripcionFalsa(),
} = {}) {
  const pushManager = {
    getSubscription: vi.fn().mockResolvedValue(suscripcion),
    subscribe: vi.fn().mockImplementation(async () => {
      pushManager.getSubscription.mockResolvedValue(nueva)
      return nueva
    }),
  }
  const notificacion = {
    permission: permiso,
    requestPermission: vi.fn().mockImplementation(async () => {
      notificacion.permission = respuestaPermiso
      return respuestaPermiso
    }),
  }
  vi.stubGlobal('PushManager', function PushManager() {})
  vi.stubGlobal('Notification', notificacion)
  Object.defineProperty(navigator, 'serviceWorker', {
    configurable: true,
    value: { getRegistration: vi.fn().mockResolvedValue({ pushManager }) },
  })
  return { pushManager, notificacion, nueva }
}

describe('HU-031 · Avisos push en el panel de la tienda · CP-NOT-031-01, CP-NOT-031-02', () => {
  beforeEach(async () => {
    limpiarSesion()
    await iniciarSesion(CORREOS_DE_PRUEBA.tienda, FRASE_DE_PRUEBA)
  })
  afterEach(() => {
    vi.unstubAllGlobals()
    Reflect.deleteProperty(navigator, 'serviceWorker')
  })

  it('no pide el permiso al abrir: solo al tocar «Activar avisos»', async () => {
    // Dado
    const { notificacion } = prepararNavegador()

    // Cuando
    const { container } = renderizarConProveedores(<TarjetaAvisosPush />)

    // Entonces
    expect(await screen.findByRole('button', { name: 'Activar avisos' })).toBeInTheDocument()
    expect(screen.getByText('Los avisos están desactivados en este dispositivo.')).toBeInTheDocument()
    expect(notificacion.requestPermission).not.toHaveBeenCalled()
    expect(await violacionesDeAccesibilidad(container)).toEqual([])
  })

  it('CP-NOT-031-01 · al activar, suscribe este navegador con la clave VAPID y lo registra en el servidor', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    const { notificacion, pushManager, nueva } = prepararNavegador()
    renderizarConProveedores(<TarjetaAvisosPush />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Activar avisos' }))

    // Entonces
    expect(await screen.findByText('Los avisos están activados en este dispositivo.')).toBeInTheDocument()
    expect(await screen.findByText('Listo: te avisaremos en este dispositivo.')).toBeInTheDocument()
    expect(notificacion.requestPermission).toHaveBeenCalledTimes(1)
    expect(pushManager.subscribe).toHaveBeenCalledWith({
      userVisibleOnly: true,
      applicationServerKey: claveAplicacion(CLAVE_PUBLICA_SIMULADA),
    })
    const tienda = base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.tienda)!
    expect(base.suscripcionesPush).toEqual([{ usuarioId: tienda.id, endpoint: nueva.endpoint }])
    expect(screen.getByRole('button', { name: 'Desactivar avisos' })).toBeInTheDocument()
  })

  it('CP-NOT-031-02 · si la tienda no da el permiso, no se suscribe y se explica cómo activarlos', async () => {
    // Dado
    const usuario = userEvent.setup({ delay: null })
    const { pushManager } = prepararNavegador({ respuestaPermiso: 'denied' })
    renderizarConProveedores(<TarjetaAvisosPush />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Activar avisos' }))

    // Entonces
    expect(await screen.findByText(/^Bloqueaste los avisos de ¿Quién lo tiene\? en este navegador/)).toBeInTheDocument()
    expect(pushManager.subscribe).not.toHaveBeenCalled()
    expect(base.suscripcionesPush).toHaveLength(0)
    expect(screen.queryByRole('button', { name: 'Activar avisos' })).not.toBeInTheDocument()
  })

  it('si el servidor rechaza el endpoint (NOT-001), lo dice y deshace la suscripción del navegador', async () => {
    // Dado: un endpoint que no es de un servicio push conocido
    const usuario = userEvent.setup({ delay: null })
    const { nueva } = prepararNavegador({ nueva: suscripcionFalsa('https://atacante.example/push') })
    renderizarConProveedores(<TarjetaAvisosPush />)

    // Cuando
    await usuario.click(await screen.findByRole('button', { name: 'Activar avisos' }))

    // Entonces
    expect(
      await screen.findByText(
        'Este navegador no se pudo registrar para recibir avisos. Inténtalo de nuevo o usa otro navegador.',
      ),
    ).toBeInTheDocument()
    expect(nueva.unsubscribe).toHaveBeenCalled()
    expect(base.suscripcionesPush).toHaveLength(0)
  })

  it('desactivar borra la suscripción en el servidor y en el navegador', async () => {
    // Dado: ya está suscrita
    const usuario = userEvent.setup({ delay: null })
    const actual = suscripcionFalsa()
    const tienda = base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.tienda)!
    base.suscripcionesPush.push({ usuarioId: tienda.id, endpoint: actual.endpoint })
    const { pushManager } = prepararNavegador({ permiso: 'granted', suscripcion: actual })
    renderizarConProveedores(<TarjetaAvisosPush />)

    // Cuando: después de darse de baja, el navegador ya no tiene suscripción
    const desactivar = await screen.findByRole('button', { name: 'Desactivar avisos' })
    pushManager.getSubscription.mockResolvedValue(null)
    await usuario.click(desactivar)

    // Entonces
    expect(await screen.findByText('Desactivaste los avisos en este dispositivo.')).toBeInTheDocument()
    expect(actual.unsubscribe).toHaveBeenCalled()
    expect(base.suscripcionesPush).toHaveLength(0)
  })

  it('con el push apagado en el servidor no se muestra ni se pide nada', async () => {
    // Dado
    servidorSimulado.use(
      http.get('/api/v1/notificaciones/configuracion-push', () =>
        HttpResponse.json({ habilitado: false, clavePublica: null }),
      ),
    )
    const { notificacion } = prepararNavegador()

    // Cuando
    renderizarConProveedores(<TarjetaAvisosPush />)

    // Entonces
    await new Promise((listo) => setTimeout(listo, 50))
    expect(screen.queryByRole('heading', { name: 'Avisos con la app cerrada' })).not.toBeInTheDocument()
    expect(notificacion.requestPermission).not.toHaveBeenCalled()
  })

  it('en un navegador sin Push API lo dice y no ofrece activar', async () => {
    renderizarConProveedores(<TarjetaAvisosPush />)
    expect(await screen.findByText(/^Este navegador no puede mostrar avisos/)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Activar avisos' })).not.toBeInTheDocument()
  })
})
