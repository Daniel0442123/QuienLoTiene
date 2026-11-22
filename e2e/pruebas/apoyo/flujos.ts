import { expect, type Browser, type Page } from '@playwright/test'
import {
  configurarDomicilioYPago,
  guardarDireccion,
  nuevaPagina,
  prepararTiendaAbierta,
  publicarSolicitud,
  registrarYEntrarCliente,
  responderSiLoTengo,
  type Tienda,
} from './actores'

export interface PedidoEntregado {
  paginaCliente: Page
  paginaTienda: Page
  tienda: Tienda
  pedidoUrl: string
}

/** Flujo de E2E-02, que E2E-04 reutiliza: del «Elegir» al «Recibí mi pedido». */
export async function entregarPedidoADomicilio(browser: Browser, escenario: string): Promise<PedidoEntregado> {
  const paginaTienda = await nuevaPagina(browser)
  const paginaCliente = await nuevaPagina(browser)
  const tienda = await prepararTiendaAbierta(browser, paginaTienda)
  await configurarDomicilioYPago(paginaTienda)

  await registrarYEntrarCliente(paginaCliente)
  await guardarDireccion(paginaCliente)
  const producto = `Acetaminofén 500 mg ${escenario} ${Date.now() % 10000}`
  await publicarSolicitud(paginaCliente, producto, { domicilio: true, direccionGuardada: true })
  await responderSiLoTengo(paginaTienda, producto, 8000)

  // HU-020 y HU-024: elegir con domicilio, dirección y medio de pago
  await paginaCliente.getByRole('link', { name: `Elegir ${tienda.nombre}` }).click()
  await expect(paginaCliente.getByRole('heading', { name: 'Confirmar pedido', level: 1 })).toBeVisible()
  await paginaCliente.getByRole('radio', { name: 'Domicilio · $ 3.000' }).check()
  await expect(paginaCliente.getByRole('radio', { name: /^Casa Calle 5 # 10-20/ })).toBeChecked()
  await paginaCliente.getByRole('radio', { name: 'Efectivo' }).check()
  await paginaCliente.getByRole('button', { name: 'Confirmar pedido · $ 11.000' }).click()
  await paginaCliente.waitForURL(/\/pedidos\/[0-9a-f-]{36}$/)
  const pedidoUrl = paginaCliente.url()
  const pasos = paginaCliente.getByRole('region', { name: 'Cómo va tu pedido' })
  await expect(pasos.getByText(/^Confirmado · .*\(estado actual\)$/)).toBeVisible()
  await expect(paginaCliente.getByText('Pagas con efectivo a la tienda.')).toBeVisible()

  // HU-027: la tienda avanza el pedido y el cliente ve cada cambio sin recargar
  await paginaTienda.goto('/tienda/pedidos')
  await paginaTienda.getByRole('link', { name: new RegExp(producto) }).click()
  for (const [boton, estado] of [
    ['Empezar a preparar', 'Preparando'],
    ['Salió a domicilio', 'En camino'],
    ['Marcar como entregado', 'Entregado'],
  ] as const) {
    await paginaTienda.getByRole('button', { name: boton }).click()
    await expect(paginaTienda.getByText(`Pedido ahora en «${estado}». El cliente ya lo ve.`).first()).toBeAttached()
    await expect(pasos.getByText(new RegExp(`^${estado} · .*\\(estado actual\\)$`))).toBeVisible()
  }

  // HU-028: el cliente confirma que lo recibió
  await paginaCliente.getByRole('button', { name: 'Recibí mi pedido' }).click()
  await expect(pasos.getByText(/^Recibido · .*\(estado actual\)$/)).toBeVisible()
  return { paginaCliente, paginaTienda, tienda, pedidoUrl }
}
