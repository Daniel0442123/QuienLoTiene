import path from 'node:path'
import { expect, type Browser, type Page } from '@playwright/test'
import { codigoTotpSeguro } from './totp'

const DOCUMENTO = path.join(import.meta.dirname, 'local.png')

/** Correos del dominio de pruebas: «limpiar.sh pruebas» los borra si se corre contra el VPS (SC-016). */
export function correoUnico(rol: string): string {
  return `e2e-${rol}-${Date.now()}-${Math.floor(Math.random() * 1000)}@prueba.quienlotiene.tech`
}

export const FRASE = 'una frase de prueba larga para e2e'

export const ADMIN = {
  correo: process.env.E2E_ADMIN_CORREO ?? 'admin@prueba.quienlotiene.tech',
  frase: process.env.E2E_ADMIN_FRASE ?? '',
  secretoTotp: process.env.E2E_ADMIN_SECRETO_TOTP ?? '',
}

/** Página en un contexto propio: cada actor tiene su sesión, como en dispositivos distintos. */
export async function nuevaPagina(browser: Browser, geolocation?: { latitude: number; longitude: number }): Promise<Page> {
  const contexto = await browser.newContext(geolocation ? { geolocation } : {})
  return contexto.newPage()
}

export async function entrar(pagina: Page, correo: string, frase: string): Promise<void> {
  await pagina.goto('/entrar')
  await pagina.getByLabel('Correo').fill(correo)
  await pagina.getByLabel('Frase de contraseña').fill(frase)
  await pagina.getByRole('button', { name: 'Entrar', exact: true }).click()
}

export async function entrarComoAdmin(pagina: Page): Promise<void> {
  await entrar(pagina, ADMIN.correo, ADMIN.frase)
  await expect(pagina.getByRole('heading', { name: 'Código de verificación' })).toBeVisible()
  await pagina.getByLabel('Código de 6 dígitos').fill(await codigoTotpSeguro(ADMIN.secretoTotp))
  await pagina.getByRole('button', { name: 'Verificar' }).click()
  await expect(pagina.getByRole('navigation', { name: 'Menú principal' }).getByRole('link', { name: 'Reportes' })).toBeVisible()
}

/**
 * Una sola sesión de administrador por corrida: la API admite 5 intentos de inicio de sesión por minuto por cuenta
 * (RNF-009, DEF-013) y varios escenarios aprueban tiendas seguidas.
 */
const sesionesAdmin = new WeakMap<Browser, Page>()
export async function sesionAdmin(browser: Browser): Promise<Page> {
  const existente = sesionesAdmin.get(browser)
  if (existente && !existente.isClosed()) return existente
  const pagina = await nuevaPagina(browser)
  await entrarComoAdmin(pagina)
  sesionesAdmin.set(browser, pagina)
  return pagina
}

export interface Tienda {
  correo: string
  nombre: string
}

/** HU-002: la tienda se registra con sus documentos y queda pendiente de revisión. */
export async function registrarTienda(pagina: Page, tipo: 'Farmacia' | 'Ferretería' = 'Farmacia'): Promise<Tienda> {
  const tienda = { correo: correoUnico('tienda'), nombre: `Droguería E2E ${Date.now() % 100000}` }
  await pagina.goto('/registrar-tienda')
  await pagina.getByLabel('Tu nombre').fill('Dueña E2E')
  await pagina.getByLabel('Correo').fill(tienda.correo)
  await pagina.getByLabel('Celular').fill('3001234567')
  await pagina.getByLabel('Frase de contraseña').fill(FRASE)
  await pagina.getByLabel('Nombre de la tienda').fill(tienda.nombre)
  await pagina.getByRole('radio', { name: tipo }).check()
  await pagina.getByRole('button', { name: 'Usar mi ubicación' }).click()
  await expect(pagina.getByText('Ubicación marcada en el mapa.')).toBeVisible()
  await pagina.locator('#tienda-foto').setInputFiles(DOCUMENTO)
  await pagina.locator('#tienda-rut').setInputFiles(DOCUMENTO)
  await pagina.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
  await pagina.getByRole('button', { name: 'Registrar tienda' }).click()
  return tienda
}

/** HU-036: el administrador revisa la tienda pendiente y la aprueba. */
export async function aprobarTienda(paginaAdmin: Page, tienda: Tienda): Promise<void> {
  await paginaAdmin.goto('/admin/tiendas')
  const tarjeta = paginaAdmin.getByRole('article', { name: tienda.nombre })
  await expect(tarjeta.getByText('Farmacia · Pendiente').or(tarjeta.getByText('Ferretería · Pendiente'))).toBeVisible()
  await tarjeta.getByRole('button', { name: 'Aprobar' }).click()
  await paginaAdmin.getByRole('button', { name: 'Sí, aprobar' }).click()
}

/** HU-010: la tienda abre para recibir solicitudes. */
export async function abrirTienda(paginaTienda: Page): Promise<void> {
  await paginaTienda.goto('/tienda')
  const interruptor = paginaTienda.getByRole('switch', { name: 'Recibir solicitudes' })
  await expect(interruptor).toBeEnabled()
  await interruptor.click()
  await expect(interruptor).toBeChecked()
}

/** HU-001 y HU-005: el cliente crea su cuenta aceptando la política de datos y entra. */
export async function registrarYEntrarCliente(pagina: Page): Promise<string> {
  const correo = correoUnico('cliente')
  await pagina.goto('/crear-cuenta')
  await pagina.getByLabel('Nombre').fill('Cliente E2E')
  await pagina.getByLabel('Correo').fill(correo)
  await pagina.getByLabel('Celular').fill('3109876543')
  await pagina.getByLabel('Frase de contraseña').fill(FRASE)
  await pagina.getByRole('checkbox', { name: /Autorizo el tratamiento/ }).check()
  await pagina.getByRole('button', { name: 'Crear cuenta' }).click()
  await expect(pagina.getByRole('main').getByText(/^Cuenta creada./)).toBeVisible()
  await entrar(pagina, correo, FRASE)
  await expect(pagina.getByRole('button', { name: 'Cerrar sesión' })).toBeVisible()
  return correo
}

/** Tienda registrada, aprobada por el administrador y abierta, con su sesión iniciada en `paginaTienda`. */
export async function prepararTiendaAbierta(browser: Browser, paginaTienda: Page, tipo: 'Farmacia' | 'Ferretería' = 'Farmacia'): Promise<Tienda> {
  const tienda = await registrarTienda(paginaTienda, tipo)
  await expect(paginaTienda.getByRole('main').getByText(/Revisamos/).first()).toBeVisible()
  const paginaAdmin = await sesionAdmin(browser)
  await aprobarTienda(paginaAdmin, tienda)
  await expect(paginaAdmin.getByText(`Aprobaste ${tienda.nombre}.`, { exact: false }).first()).toBeVisible()
  await entrar(paginaTienda, tienda.correo, FRASE)
  await paginaTienda.waitForURL(/\/tienda/)
  await abrirTienda(paginaTienda)
  return tienda
}

/** HU-016 y HU-017: el cliente publica una solicitud cerca de su ubicación. */
export async function publicarSolicitud(pagina: Page, producto: string, opciones: { tipo?: 'Farmacia' | 'Ferretería'; domicilio?: boolean; direccionGuardada?: boolean } = {}): Promise<void> {
  await pagina.goto('/solicitudes/nueva')
  await pagina.getByRole('textbox', { name: '¿Qué estás buscando?' }).fill(producto)
  await pagina.getByRole('radio', { name: opciones.tipo ?? 'Farmacia' }).check()
  if (opciones.direccionGuardada) {
    await pagina.getByRole('radio', { name: /^Casa/ }).check()
  } else {
    await pagina.getByRole('button', { name: 'Usar mi ubicación' }).click()
    await expect(pagina.getByText('Ubicación marcada en el mapa.')).toBeVisible()
  }
  if (opciones.domicilio) await pagina.getByRole('radio', { name: 'Domicilio' }).check()
  await pagina.getByRole('button', { name: 'Preguntar' }).click()
  await pagina.waitForURL(/solicitud=[0-9a-f-]{36}/)
}

/** HU-019: la tienda responde «Sí lo tengo» con precio desde su bandeja. */
export async function responderSiLoTengo(paginaTienda: Page, producto: string, precio: number): Promise<void> {
  await paginaTienda.goto('/tienda/solicitudes')
  await paginaTienda.getByRole('link', { name: `Sí lo tengo: ${producto}` }).click()
  await paginaTienda.getByRole('textbox', { name: 'Precio por unidad' }).fill(String(precio))
  await paginaTienda.getByRole('button', { name: 'Enviar respuesta' }).click()
}

/** HU-012 y HU-013: la tienda activa el domicilio (3 km, $ 3.000) y acepta efectivo. */
export async function configurarDomicilioYPago(paginaTienda: Page): Promise<void> {
  await paginaTienda.goto('/tienda/domicilio')
  await paginaTienda.getByRole('checkbox', { name: 'Hago domicilios' }).check()
  await paginaTienda.getByRole('combobox', { name: 'Hasta qué distancia llevas pedidos' }).selectOption({ label: '3 km' })
  await paginaTienda.getByRole('textbox', { name: 'Valor del domicilio (pesos)' }).fill('3000')
  await paginaTienda.getByRole('button', { name: 'Guardar cambios' }).click()
  await expect(paginaTienda.getByRole('main').getByText(/guardad|actualizad/i).first()).toBeVisible()
  await paginaTienda.goto('/tienda/medios-pago')
  await paginaTienda.getByRole('checkbox', { name: 'Efectivo' }).check()
  await paginaTienda.getByRole('button', { name: 'Guardar cambios' }).click()
  await expect(paginaTienda.getByRole('main').getByText('Medios de pago actualizados.')).toBeVisible()
}

/** HU-007: el cliente guarda la dirección de su casa (se usa para el domicilio). */
export async function guardarDireccion(paginaCliente: Page): Promise<void> {
  await paginaCliente.goto('/cuenta')
  await paginaCliente.getByRole('button', { name: 'Agregar dirección' }).click()
  await paginaCliente.getByLabel('Nombre de la dirección').fill('Casa')
  await paginaCliente.getByRole('button', { name: 'Usar mi ubicación' }).click()
  await paginaCliente.getByRole('textbox', { name: 'Dirección', exact: true }).fill('Calle 5 # 10-20, Centro')
  await paginaCliente.getByRole('textbox', { name: 'Celular de contacto' }).fill('3109876543')
  await paginaCliente.getByRole('button', { name: 'Guardar dirección' }).click()
  await expect(paginaCliente.getByText('Guardaste «Casa».').first()).toBeVisible()
}
