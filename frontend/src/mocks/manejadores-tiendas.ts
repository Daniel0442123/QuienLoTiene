import { http, HttpResponse } from 'msw'
import { base, type DiaSemana, type DocumentoSimulado, type Franja, type TiendaSimulada } from './datos'
import { problemaDeCuenta, problemaDeFrase } from './manejadores-identidad'
import {
  autorizar,
  CORREO_VALIDO,
  problema,
  TELEFONO_VALIDO,
  ubicacionInvalida,
  vacio,
  validar,
  leerJson,
} from './utilidades'

/** Tiendas: registro con documentos, perfil y horario, apertura, domicilio y medios de pago (HU-002, 009 a 013). */

const RUTA = '/api/v1/tiendas'
const CINCO_MB = 5 * 1024 * 1024
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf']
const DIAS: DiaSemana[] = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
const MEDIOS_PAGO = ['EFECTIVO', 'TRANSFERENCIA', 'DATAFONO']
const HORA = /^([01]\d|2[0-3]):[0-5]\d(:[0-5]\d)?$/

/** MiTienda del backend. */
export function aMiTienda(tienda: TiendaSimulada) {
  return {
    id: tienda.id,
    nombre: tienda.nombre,
    telefono: tienda.telefono,
    categoriaId: tienda.categoriaId,
    latitud: tienda.latitud,
    longitud: tienda.longitud,
    estado: tienda.estado,
    estaAbierta: tienda.estaAbierta,
    motivoRechazo: tienda.motivoRechazo,
    fechaRegistro: tienda.fechaRegistro,
    horario: tienda.horario,
  }
}

function aDomicilio(tienda: TiendaSimulada) {
  const { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos } = tienda.domicilio
  return { haceDomicilio, radioCoberturaEnMetros, tipoTarifa: haceDomicilio ? 'FIJA' : null, tarifaFijaEnPesos }
}

/** La tienda del usuario con rol TIENDA, o el error que daría el backend. */
function miTienda(request: Request): TiendaSimulada | Response {
  const dueno = autorizar(request, ['TIENDA'])
  if (dueno instanceof Response) return dueno
  return base.tiendas.find((t) => t.duenoId === dueno.id) ?? problema(404, 'TIE-006', 'Tienda no encontrada')
}

function documentoDesde(parte: FormDataEntryValue | null, tipo: DocumentoSimulado['tipo']) {
  if (!(parte instanceof File) || parte.size === 0) return null
  return {
    id: crypto.randomUUID(),
    tipo,
    tipoContenido: parte.type,
    tamanoEnBytes: parte.size,
    nombreArchivo: parte.name,
    contenido: parte,
  } satisfies DocumentoSimulado
}

interface RegistrarTiendaRequest {
  nombreDueno: string
  correo: string
  telefono: string
  fraseContrasena: string
  aceptaPoliticaDatos: boolean
  versionPoliticaDatos: string
  nombreTienda: string
  categoriaId: string
  ubicacion: { latitud: number; longitud: number }
}

async function leerParteDatos(parte: FormDataEntryValue | null): Promise<Partial<RegistrarTiendaRequest>> {
  try {
    const texto = typeof parte === 'string' ? parte : parte ? await parte.text() : '{}'
    return JSON.parse(texto) as Partial<RegistrarTiendaRequest>
  } catch {
    return {}
  }
}

export const manejadoresTiendas = [
  http.post(`${RUTA}/registro`, async ({ request }) => {
    let formulario: FormData
    try {
      formulario = await request.formData()
    } catch {
      return problema(415, 'GEN-007', 'Formato no soportado', { detail: 'Envía multipart/form-data.' })
    }
    const datos = await leerParteDatos(formulario.get('datos'))
    const invalidos = validar([
      ['nombreDueno', vacio(datos.nombreDueno), 'Escribe tu nombre'],
      ['correo', vacio(datos.correo) || !CORREO_VALIDO.test(datos.correo ?? ''), 'El correo no es válido'],
      [
        'telefono',
        !TELEFONO_VALIDO.test(datos.telefono ?? ''),
        'El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio',
      ],
      ['fraseContrasena', vacio(datos.fraseContrasena), 'La frase de contraseña es obligatoria'],
      ['aceptaPoliticaDatos', datos.aceptaPoliticaDatos !== true, 'Debes aceptar la política de tratamiento de datos'],
      ['versionPoliticaDatos', vacio(datos.versionPoliticaDatos), 'La versión de la política aceptada es obligatoria'],
      ['nombreTienda', vacio(datos.nombreTienda), 'Escribe el nombre de la tienda'],
      ['categoriaId', vacio(datos.categoriaId), 'Elige la categoría de la tienda'],
      ['ubicacion', ubicacionInvalida(datos.ubicacion), 'Marca la ubicación del local en el mapa'],
    ])
    if (invalidos) return invalidos

    const archivos = [formulario.get('fotoLocal'), formulario.get('rut')].filter((a) => a instanceof File)
    if (archivos.some((a) => a.size > CINCO_MB)) return problema(413, 'GEN-010', 'Archivo muy grande')
    if (archivos.some((a) => !TIPOS_PERMITIDOS.includes(a.type))) {
      return problema(400, 'TIE-003', 'Archivo no permitido', { detail: 'Usa JPEG, PNG, WebP o PDF de máximo 5 MB.' })
    }
    const fotoLocal = documentoDesde(formulario.get('fotoLocal'), 'FOTO_LOCAL')
    const rut = documentoDesde(formulario.get('rut'), 'RUT')
    if (!fotoLocal || !rut) {
      return problema(400, 'TIE-002', 'Documento faltante', {
        detail: `Falta ${!fotoLocal ? 'la foto del local' : 'el RUT'}.`,
      })
    }

    const completos = datos as RegistrarTiendaRequest
    const rechazo =
      problemaDeFrase(completos.fraseContrasena) ?? problemaDeCuenta(completos.correo, completos.versionPoliticaDatos)
    if (rechazo) return rechazo

    const ahora = new Date().toISOString()
    const duenoId = crypto.randomUUID()
    base.usuarios.push({
      id: duenoId,
      nombre: completos.nombreDueno,
      correo: completos.correo,
      telefono: completos.telefono,
      rol: 'TIENDA',
      fraseContrasena: completos.fraseContrasena,
      estaSuspendido: false,
      fechaCreacion: ahora,
    })
    const tienda: TiendaSimulada = {
      id: crypto.randomUUID(),
      duenoId,
      nombre: completos.nombreTienda,
      telefono: completos.telefono,
      categoriaId: completos.categoriaId,
      latitud: completos.ubicacion.latitud,
      longitud: completos.ubicacion.longitud,
      estado: 'PENDIENTE',
      estaAbierta: false,
      motivoRechazo: null,
      fechaRegistro: ahora,
      fechaRevision: null,
      horario: [],
      domicilio: { haceDomicilio: false, radioCoberturaEnMetros: null, tarifaFijaEnPesos: null },
      mediosPago: ['EFECTIVO'],
      documentos: [fotoLocal, rut],
    }
    base.tiendas.push(tienda)
    return HttpResponse.json(
      { id: tienda.id, nombre: tienda.nombre, estado: tienda.estado, fechaRegistro: tienda.fechaRegistro },
      { status: 201, headers: { Location: `${RUTA}/me` } },
    )
  }),

  http.get(`${RUTA}/me`, ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    return HttpResponse.json(aMiTienda(tienda))
  }),

  http.put(`${RUTA}/me/perfil`, async ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    const datos = await leerJson<{ nombre: string; telefono: string; horario: Franja[] }>(request)
    const invalidos = validar([
      ['nombre', vacio(datos.nombre) || (datos.nombre ?? '').length > 120, 'Escribe el nombre de la tienda'],
      [
        'telefono',
        !TELEFONO_VALIDO.test(datos.telefono ?? ''),
        'El teléfono debe tener entre 7 y 15 dígitos, con + opcional al inicio',
      ],
      ['horario', !Array.isArray(datos.horario), 'Envía el horario; una lista vacía quita el horario'],
      [
        'horario',
        Array.isArray(datos.horario) && datos.horario.length > 7,
        'El horario tiene máximo una franja por día',
      ],
      ...(datos.horario ?? []).flatMap((franja, i): Array<[string, boolean, string]> => [
        [`horario[${i}].diaSemana`, !DIAS.includes(franja.diaSemana), 'El día no es válido'],
        [`horario[${i}].abre`, !HORA.test(franja.abre ?? ''), 'Falta la hora de apertura'],
        [`horario[${i}].cierra`, !HORA.test(franja.cierra ?? ''), 'Falta la hora de cierre'],
      ]),
    ])
    if (invalidos) return invalidos

    const horario = datos.horario as Franja[]
    const diasRepetidos = new Set(horario.map((f) => f.diaSemana)).size !== horario.length
    const abreDespues = horario.some((f) => f.abre.slice(0, 5) >= f.cierra.slice(0, 5))
    if (diasRepetidos || abreDespues) {
      return problema(400, 'TIE-007', 'Horario inválido', {
        detail: diasRepetidos ? 'Un día aparece dos veces.' : 'Una franja abre después de cerrar.',
      })
    }
    tienda.nombre = datos.nombre as string
    tienda.telefono = datos.telefono as string
    tienda.horario = horario.map((f) => ({
      diaSemana: f.diaSemana,
      abre: f.abre.slice(0, 5),
      cierra: f.cierra.slice(0, 5),
    }))
    return HttpResponse.json(aMiTienda(tienda))
  }),

  http.patch(`${RUTA}/me/apertura`, async ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    const { abierta } = await leerJson<{ abierta: boolean }>(request)
    const invalidos = validar([
      ['abierta', typeof abierta !== 'boolean', 'Indica si la tienda queda abierta o cerrada'],
    ])
    if (invalidos) return invalidos
    if (tienda.estado !== 'APROBADA') return problema(409, 'TIE-008', 'Tienda no aprobada')
    tienda.estaAbierta = abierta as boolean
    return HttpResponse.json(aMiTienda(tienda))
  }),

  http.get(`${RUTA}/me/domicilio`, ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    return HttpResponse.json(aDomicilio(tienda))
  }),

  http.put(`${RUTA}/me/domicilio`, async ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    const { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos } = await leerJson<{
      haceDomicilio: boolean
      radioCoberturaEnMetros: number | null
      tarifaFijaEnPesos: number | null
    }>(request)
    const invalidos = validar([
      ['haceDomicilio', typeof haceDomicilio !== 'boolean', 'Indica si la tienda hace domicilios'],
    ])
    if (invalidos) return invalidos

    const radio = radioCoberturaEnMetros ?? null
    const tarifa = tarifaFijaEnPesos ?? null
    const radioFueraDeRango = radio !== null && (radio < 500 || radio > 10_000)
    const tarifaNegativa = tarifa !== null && tarifa < 0
    const faltaDato = haceDomicilio === true && (radio === null || tarifa === null)
    if (radioFueraDeRango || tarifaNegativa || faltaDato) {
      return problema(400, 'TIE-009', 'Domicilio inválido', {
        detail:
          'El radio va de 500 a 10.000 m, la tarifa no puede ser negativa y con domicilio activo ambos son obligatorios.',
      })
    }
    tienda.domicilio = haceDomicilio
      ? { haceDomicilio: true, radioCoberturaEnMetros: radio, tarifaFijaEnPesos: tarifa }
      : { haceDomicilio: false, radioCoberturaEnMetros: null, tarifaFijaEnPesos: null }
    return HttpResponse.json(aDomicilio(tienda))
  }),

  http.get(`${RUTA}/me/domicilio/simulacion`, ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    const parametros = new URL(request.url).searchParams
    const subtotal = Number(parametros.get('subtotalEnPesos'))
    const distancia = Number(parametros.get('distanciaEnMetros'))
    if (
      !parametros.has('subtotalEnPesos') ||
      !parametros.has('distanciaEnMetros') ||
      Number.isNaN(subtotal) ||
      Number.isNaN(distancia)
    ) {
      return problema(400, 'GEN-002', 'Petición mal formada', { detail: 'Faltan subtotalEnPesos o distanciaEnMetros.' })
    }
    const invalidos = validar([
      ['subtotalEnPesos', subtotal < 0, 'El subtotal no puede ser negativo'],
      ['distanciaEnMetros', distancia < 0, 'La distancia no puede ser negativa'],
    ])
    if (invalidos) return invalidos
    const { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos } = tienda.domicilio
    if (!haceDomicilio || distancia > (radioCoberturaEnMetros ?? 0)) {
      return problema(422, 'TIE-010', 'Fuera de cobertura')
    }
    return HttpResponse.json({ costoDomicilioEnPesos: tarifaFijaEnPesos ?? 0 })
  }),

  http.get(`${RUTA}/me/medios-pago`, ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    return HttpResponse.json({ mediosPago: tienda.mediosPago })
  }),

  http.put(`${RUTA}/me/medios-pago`, async ({ request }) => {
    const tienda = miTienda(request)
    if (tienda instanceof Response) return tienda
    const { mediosPago } = await leerJson<{ mediosPago: string[] }>(request)
    const invalidos = validar([
      ['mediosPago', !Array.isArray(mediosPago) || mediosPago.length === 0, 'Elige al menos un medio de pago'],
      ...(mediosPago ?? []).map((medio, i): [string, boolean, string] => [
        `mediosPago[${i}]`,
        !MEDIOS_PAGO.includes(medio),
        'El medio de pago debe ser EFECTIVO, TRANSFERENCIA o DATAFONO',
      ]),
    ])
    if (invalidos) return invalidos
    tienda.mediosPago = [...new Set(mediosPago as string[])]
    return HttpResponse.json({ mediosPago: tienda.mediosPago })
  }),
]
