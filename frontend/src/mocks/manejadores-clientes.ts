import { http, HttpResponse } from 'msw'
import { base, type DireccionSimulada } from './datos'
import { autorizar, leerJson, problema, TELEFONO_VALIDO, ubicacionInvalida, vacio, validar } from './utilidades'

/** Clientes: direcciones de entrega (HU-007). Solo para el rol CLIENTE. */

const RUTA = '/api/v1/clientes/me/direcciones'
const MAXIMO_DIRECCIONES = 10

interface DireccionRequest {
  nombre: string
  ubicacion: { latitud: number; longitud: number }
  direccionEscrita: string
  indicaciones?: string | null
  telefono: string
}

function aRespuesta(direccion: DireccionSimulada) {
  const { clienteId: _clienteId, ...datos } = direccion
  return datos
}

function validarDireccion(datos: Partial<DireccionRequest>) {
  return validar([
    [
      'nombre',
      vacio(datos.nombre) || (datos.nombre ?? '').length > 40,
      'Ponle un nombre a la dirección, por ejemplo Casa',
    ],
    ['ubicacion', ubicacionInvalida(datos.ubicacion), 'Marca la ubicación en el mapa'],
    [
      'direccionEscrita',
      vacio(datos.direccionEscrita) || (datos.direccionEscrita ?? '').length > 200,
      'Escribe la dirección (máximo 200 caracteres)',
    ],
    ['indicaciones', (datos.indicaciones ?? '').length > 200, 'Las indicaciones pueden tener máximo 200 caracteres'],
    [
      'telefono',
      !TELEFONO_VALIDO.test(datos.telefono ?? ''),
      'El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio',
    ],
  ])
}

function buscarPropia(clienteId: string, direccionId: unknown) {
  return base.direcciones.find((d) => d.id === direccionId && d.clienteId === clienteId)
}

export const manejadoresClientes = [
  http.get(RUTA, ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    return HttpResponse.json(base.direcciones.filter((d) => d.clienteId === cliente.id).map(aRespuesta))
  }),

  http.post(RUTA, async ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const datos = await leerJson<DireccionRequest>(request)
    const invalidos = validarDireccion(datos)
    if (invalidos) return invalidos
    if (base.direcciones.filter((d) => d.clienteId === cliente.id).length >= MAXIMO_DIRECCIONES) {
      return problema(422, 'CLI-002', 'Límite de direcciones')
    }
    const completos = datos as DireccionRequest
    const nueva: DireccionSimulada = {
      id: crypto.randomUUID(),
      clienteId: cliente.id,
      nombre: completos.nombre,
      latitud: completos.ubicacion.latitud,
      longitud: completos.ubicacion.longitud,
      direccionEscrita: completos.direccionEscrita,
      indicaciones: completos.indicaciones ?? null,
      telefono: completos.telefono,
      fechaCreacion: new Date().toISOString(),
    }
    base.direcciones.push(nueva)
    return HttpResponse.json(aRespuesta(nueva), { status: 201, headers: { Location: `${RUTA}/${nueva.id}` } })
  }),

  http.put(`${RUTA}/:direccionId`, async ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const direccion = buscarPropia(cliente.id, params.direccionId)
    if (!direccion) return problema(404, 'CLI-003', 'Dirección no encontrada')
    const datos = await leerJson<DireccionRequest>(request)
    const invalidos = validarDireccion(datos)
    if (invalidos) return invalidos
    const completos = datos as DireccionRequest
    Object.assign(direccion, {
      nombre: completos.nombre,
      latitud: completos.ubicacion.latitud,
      longitud: completos.ubicacion.longitud,
      direccionEscrita: completos.direccionEscrita,
      indicaciones: completos.indicaciones ?? null,
      telefono: completos.telefono,
    })
    return HttpResponse.json(aRespuesta(direccion))
  }),

  http.delete(`${RUTA}/:direccionId`, ({ request, params }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const direccion = buscarPropia(cliente.id, params.direccionId)
    if (!direccion) return problema(404, 'CLI-003', 'Dirección no encontrada')
    base.direcciones = base.direcciones.filter((d) => d !== direccion)
    return new HttpResponse(null, { status: 204 })
  }),
]
