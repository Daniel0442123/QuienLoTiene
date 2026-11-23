import { expect, test } from '@playwright/test'
import { abrirTienda, aprobarTienda, entrar, entrarComoAdmin, FRASE, nuevaPagina, publicarSolicitud, registrarTienda, registrarYEntrarCliente } from './apoyo/actores'

/**
 * E2E-03 · Aprobar una tienda con segundo factor (HU-002, HU-003, HU-004, HU-036).
 * Una tienda se registra con documentos; el administrador inicia sesión con TOTP y la aprueba; la tienda empieza a
 * recibir solicitudes.
 */
test('E2E-03 · la tienda aprobada por el administrador con TOTP recibe solicitudes', { tag: ['@E2E-03', '@HU-002', '@HU-004', '@HU-036'] }, async ({ browser }) => {
  // HU-002: registro con foto del local y RUT
  const paginaTienda = await nuevaPagina(browser)
  const tienda = await registrarTienda(paginaTienda)
  await entrar(paginaTienda, tienda.correo, FRASE)
  await expect(paginaTienda.getByText('En revisión', { exact: true })).toBeVisible()
  await expect(paginaTienda.getByRole('switch', { name: 'Recibir solicitudes' })).toBeDisabled()

  // HU-003 y HU-004: el administrador entra con frase y código TOTP; HU-036: revisa documentos y aprueba
  const paginaAdmin = await nuevaPagina(browser)
  await entrarComoAdmin(paginaAdmin)
  await paginaAdmin.goto('/admin/tiendas')
  const tarjeta = paginaAdmin.getByRole('article', { name: tienda.nombre })
  await expect(tarjeta.getByRole('button', { name: 'Descargar Foto del local (PNG, 1 KB)' })).toBeVisible()
  await expect(tarjeta.getByRole('button', { name: 'Descargar RUT o documento del negocio (PNG, 1 KB)' })).toBeVisible()
  await aprobarTienda(paginaAdmin, tienda)
  await expect(paginaAdmin.getByText(`Aprobaste ${tienda.nombre}. Ya puede abrir y recibir solicitudes.`).first()).toBeVisible()

  // La tienda queda aprobada, abre y le llega una solicitud cercana
  await paginaTienda.reload()
  await expect(paginaTienda.getByText('Aprobada', { exact: true })).toBeVisible()
  await abrirTienda(paginaTienda)
  const paginaCliente = await nuevaPagina(browser)
  await registrarYEntrarCliente(paginaCliente)
  const producto = `Ibuprofeno 400 mg E2E-03 ${Date.now() % 10000}`
  await publicarSolicitud(paginaCliente, producto)
  await paginaTienda.goto('/tienda/solicitudes')
  await expect(paginaTienda.getByRole('article', { name: producto })).toBeVisible()
})
