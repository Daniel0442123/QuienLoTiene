import { afterEach, describe, expect, it, vi } from 'vitest'
import { CATEGORIAS_SIMULADAS, CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from './datos'

/** La simulación del Sprint 2 responde como docs/api/contrato-sprint-2.yaml: si se desvía, las pantallas mienten. */

type Cuerpo = Record<string, unknown>

async function pedir(ruta: string, token?: string, metodo = 'GET', cuerpo?: unknown) {
  const respuesta = await fetch(ruta, {
    method: metodo,
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(cuerpo === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
  })
  return { status: respuesta.status, cuerpo: (await respuesta.json()) as unknown }
}

async function entrar(correo: string): Promise<string> {
  const { cuerpo } = await pedir('/api/v1/auth/login', undefined, 'POST', { correo, fraseContrasena: FRASE_DE_PRUEBA })
  return (cuerpo as { tokenAcceso: string }).tokenAcceso
}

async function publicarSolicitud(token: string, modalidad: 'RECOGER' | 'DOMICILIO' = 'RECOGER'): Promise<string> {
  const { cuerpo } = await pedir('/api/v1/solicitudes', token, 'POST', {
    producto: 'Acetaminofén 500 mg',
    cantidad: 1,
    categoriaId: CATEGORIAS_SIMULADAS[0].id,
    modalidad,
    ubicacion: { latitud: 2.44, longitud: -76.6 },
  })
  return (cuerpo as { id: string }).id
}

const ORDEN_DE_LA_VECINA = 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01'
const ORDEN_DE_LA_CLIENTA = 'e1f2a3b4-0002-4c5d-8e9f-0a1b2c3d4e02'
const SOLICITUD_DE_LA_VECINA = 'a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01'
const DIRECCION_DE_LA_CLIENTA = 'd1e2f3a4-0001-4b5c-8d9e-0f1a2b3c4d01'

afterEach(() => {
  vi.useRealTimers()
})

describe('Simulación de la API · Sprint 2 · tienda (HU-018, HU-019, HU-021)', () => {
  it('HU-018: muestra las solicitudes nuevas con la urgente primero, sin datos del cliente y con precio sugerido', async () => {
    // Dado
    const token = await entrar(CORREOS_DE_PRUEBA.tienda)

    // Cuando
    const { status, cuerpo } = await pedir('/api/v1/solicitudes/disponibles', token)

    // Entonces
    expect(status).toBe(200)
    const [primera, segunda] = cuerpo as Cuerpo[]
    expect(primera).toMatchObject({ producto: 'Paracetamol 500 mg', esUrgente: true, precioSugeridoEnPesos: 800 })
    expect(segunda).toMatchObject({ esUrgente: false, precioSugeridoEnPesos: null })
    expect(Number(primera?.distanciaEnMetros) % 100).toBe(0)
    expect(primera).not.toHaveProperty('clienteId')
    expect(primera).not.toHaveProperty('ubicacion')
  })

  it('HU-019: responde una sola vez, pide la descripción de «Tengo algo parecido» y la lista en «Mis respuestas»', async () => {
    // Dado
    const token = await entrar(CORREOS_DE_PRUEBA.tienda)
    const ruta = `/api/v1/solicitudes/${SOLICITUD_DE_LA_VECINA}/respuestas`

    // Cuando
    const incompleta = await pedir(ruta, token, 'POST', { tipo: 'PARECIDO', precioEnPesos: 900, cantidadDisponible: 2 })
    const valida = await pedir(ruta, token, 'POST', { tipo: 'SI', precioEnPesos: 900, cantidadDisponible: 2 })
    const repetida = await pedir(ruta, token, 'POST', { tipo: 'NO' })

    // Entonces
    expect(incompleta).toMatchObject({ status: 400, cuerpo: { codigo: 'SOL-006' } })
    expect(valida).toMatchObject({ status: 201, cuerpo: { estado: 'ESPERANDO', ofreceDomicilio: true } })
    expect(repetida).toMatchObject({ status: 409, cuerpo: { codigo: 'SOL-005' } })
    const disponibles = (await pedir('/api/v1/solicitudes/disponibles', token)).cuerpo as Cuerpo[]
    expect(disponibles.map((s) => s.id)).not.toContain(SOLICITUD_DE_LA_VECINA)
    const mias = (await pedir('/api/v1/tiendas/me/respuestas', token)).cuerpo as Cuerpo[]
    expect(mias[0]).toMatchObject({ solicitudId: SOLICITUD_DE_LA_VECINA, estado: 'ESPERANDO' })
    expect(mias.find((r) => r.producto === 'Loratadina 10 mg')).toMatchObject({ estado: 'ELEGIDA' })
  })

  it('HU-019: no acepta respuestas a una solicitud que llegó a otra categoría (SOL-003)', async () => {
    // Dado
    const cliente = await entrar(CORREOS_DE_PRUEBA.cliente)
    const tienda = await entrar(CORREOS_DE_PRUEBA.tienda)
    const { cuerpo } = await pedir('/api/v1/solicitudes', cliente, 'POST', {
      producto: 'Martillo',
      cantidad: 1,
      categoriaId: CATEGORIAS_SIMULADAS[1].id,
      modalidad: 'RECOGER',
      ubicacion: { latitud: 2.44, longitud: -76.6 },
    })

    // Cuando
    const respuesta = await pedir(`/api/v1/solicitudes/${(cuerpo as { id: string }).id}/respuestas`, tienda, 'POST', {
      tipo: 'NO',
    })

    // Entonces
    expect(respuesta).toMatchObject({ status: 404, cuerpo: { codigo: 'SOL-003' } })
  })
})

describe('Simulación de la API · Sprint 2 · cliente (HU-020, HU-021, HU-024)', () => {
  it('HU-020 y HU-024: las tiendas simuladas responden a los segundos y el cliente elige una con domicilio', async () => {
    // Dado
    vi.useFakeTimers({ toFake: ['Date'] })
    const token = await entrar(CORREOS_DE_PRUEBA.cliente)
    const id = await publicarSolicitud(token, 'DOMICILIO')
    const antes = await pedir(`/api/v1/solicitudes/${id}/respuestas`, token)
    vi.setSystemTime(Date.now() + 20_000)

    // Cuando
    const despues = (await pedir(`/api/v1/solicitudes/${id}/respuestas?orden=total`, token)).cuerpo as {
      respuestasRecibidas: number
      respuestas: Cuerpo[]
    }

    // Entonces
    expect(antes.cuerpo).toMatchObject({ respuestasRecibidas: 0, respuestas: [] })
    expect(despues.respuestasRecibidas).toBe(2)
    expect(despues.respuestas.map((t) => t.esAlternativa)).toEqual([true, false])
    const botica = despues.respuestas.find((t) => t.nombreTienda === 'Botica San José')
    expect(botica).toMatchObject({ subtotalEnPesos: 2500, costoDomicilioEnPesos: 3000, totalEnPesos: 5500 })

    const pedido = { respuestaId: botica?.id, modalidad: 'DOMICILIO', direccionId: DIRECCION_DE_LA_CLIENTA }
    const sinDireccion = await pedir('/api/v1/ordenes', token, 'POST', {
      ...pedido,
      direccionId: null,
      medioPago: 'EFECTIVO',
    })
    const otroMedio = await pedir('/api/v1/ordenes', token, 'POST', { ...pedido, medioPago: 'TRANSFERENCIA' })
    const orden = await pedir('/api/v1/ordenes', token, 'POST', { ...pedido, medioPago: 'DATAFONO' })
    const otraVez = await pedir('/api/v1/ordenes', token, 'POST', { ...pedido, medioPago: 'DATAFONO' })
    expect(sinDireccion).toMatchObject({ status: 400, cuerpo: { codigo: 'ORD-004' } })
    expect(otroMedio).toMatchObject({ status: 422, cuerpo: { codigo: 'ORD-003' } })
    expect(orden).toMatchObject({ status: 201, cuerpo: { estado: 'CONFIRMADO', totalEnPesos: 5500, cliente: null } })
    expect(otraVez).toMatchObject({ status: 409, cuerpo: { codigo: 'SOL-004' } })
    expect((await pedir(`/api/v1/solicitudes/${id}`, token)).cuerpo).toMatchObject({ estado: 'RESUELTA' })
  })

  it('HU-021: la solicitud pasa a VENCIDA cuando se cumple su hora', async () => {
    // Dado
    vi.useFakeTimers({ toFake: ['Date'] })
    const token = await entrar(CORREOS_DE_PRUEBA.cliente)
    const id = await publicarSolicitud(token)
    vi.setSystemTime(Date.now() + 31 * 60_000)

    // Cuando
    const { cuerpo } = await pedir(`/api/v1/solicitudes/${id}`, token)

    // Entonces
    expect(cuerpo).toMatchObject({ estado: 'VENCIDA' })
  })

  it('responde SOL-003 al cliente que pide la solicitud de otro, sin decir que existe', async () => {
    // Dado
    const token = await entrar(CORREOS_DE_PRUEBA.cliente)

    // Cuando
    const respuesta = await pedir(`/api/v1/solicitudes/${SOLICITUD_DE_LA_VECINA}`, token)

    // Entonces
    expect(respuesta).toMatchObject({ status: 404, cuerpo: { codigo: 'SOL-003' } })
  })
})

describe('Simulación de la API · Sprint 2 · órdenes (HU-027, HU-028, HU-032)', () => {
  it('HU-027: la tienda solo avanza al paso siguiente y ve los datos del cliente con la orden activa', async () => {
    // Dado
    const token = await entrar(CORREOS_DE_PRUEBA.tienda)
    const ruta = `/api/v1/ordenes/${ORDEN_DE_LA_VECINA}/estado`

    // Cuando
    const salto = await pedir(ruta, token, 'PATCH', { estado: 'ENTREGADO' })
    const siguiente = await pedir(ruta, token, 'PATCH', { estado: 'PREPARANDO' })

    // Entonces
    expect(salto).toMatchObject({ status: 409, cuerpo: { codigo: 'ORD-002' } })
    expect(siguiente.cuerpo).toMatchObject({
      estado: 'PREPARANDO',
      cliente: { nombre: 'Vecina de Prueba', direccion: { nombre: 'Apartamento' } },
    })
    const lista = (await pedir('/api/v1/ordenes', token)).cuerpo as Cuerpo[]
    expect(lista.map((o) => o.esActiva)).toEqual([true, true])
  })

  it('HU-028 y HU-032: el cliente confirma la recepción y después ya no hay WhatsApp ni datos para la tienda', async () => {
    // Dado
    const cliente = await entrar(CORREOS_DE_PRUEBA.cliente)
    const tienda = await entrar(CORREOS_DE_PRUEBA.tienda)
    const ruta = `/api/v1/ordenes/${ORDEN_DE_LA_CLIENTA}`
    const whatsapp = (await pedir(`${ruta}/whatsapp`, cliente)).cuerpo as Cuerpo

    // Cuando
    const recibida = await pedir(`${ruta}/recepcion`, cliente, 'POST')

    // Entonces
    expect(whatsapp).toMatchObject({ nombre: 'Droguería La 5', telefono: '573001234567' })
    expect(String(whatsapp.enlace)).toMatch(/^https:\/\/wa\.me\/573001234567\?text=/)
    expect(recibida.cuerpo).toMatchObject({ estado: 'RESUELTA', esActiva: false })
    expect((await pedir(`${ruta}/whatsapp`, cliente)).cuerpo).toMatchObject({ codigo: 'MEN-001' })
    expect((await pedir(ruta, tienda)).cuerpo).toMatchObject({ cliente: null })
    expect((await pedir(`/api/v1/ordenes/${ORDEN_DE_LA_VECINA}`, cliente)).cuerpo).toMatchObject({ codigo: 'ORD-001' })
  })
})
