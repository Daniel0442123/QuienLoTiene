/**
 * Datos simulados de la API (solo memoria): usuarios de prueba, tiendas, direcciones y solicitudes.
 * Se reinician al recargar la página (modo simulado) y después de cada prueba (reiniciarDatosSimulados).
 *
 * Los valores siguen el contrato del backend (DTOs de los controladores y CODIGOS-DE-ERROR.md).
 * Nada de esto es un dato real: son valores de prueba para trabajar sin el backend.
 */

export type RolSimulado = 'CLIENTE' | 'TIENDA' | 'ADMIN'
export type EstadoTienda = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'SUSPENDIDA'
export type DiaSemana = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'

/** Frase de contraseña de todos los usuarios de prueba. */
export const FRASE_DE_PRUEBA = 'una frase larga que recuerdo bien'

/** Correos de los usuarios de prueba, uno por caso que hace falta probar en pantalla. */
export const CORREOS_DE_PRUEBA = {
  cliente: 'cliente@ejemplo.com',
  tienda: 'tienda@ejemplo.com',
  tiendaPendiente: 'tienda.nueva@ejemplo.com',
  administrador: 'admin@ejemplo.com',
  suspendido: 'suspendido@ejemplo.com',
} as const

/** Código TOTP que acepta el segundo factor simulado del administrador (HU-004). */
export const CODIGO_TOTP_DE_PRUEBA = '123456'

/** Versión vigente de la política de datos (application.yml del backend: qlt.version-politica-datos). */
export const VERSION_POLITICA_VIGENTE = '1.0'

/**
 * Categorías simuladas. OJO: el backend todavía no publica un endpoint de categorías (módulo catalogo);
 * estos identificadores solo existen en la simulación.
 */
export const CATEGORIAS_SIMULADAS = [
  { id: '3f6b1c2a-0001-4a8e-9b1d-5c2e7a9f0001', nombre: 'Farmacia' },
  { id: '3f6b1c2a-0002-4a8e-9b1d-5c2e7a9f0002', nombre: 'Ferretería' },
] as const

const [FARMACIA] = CATEGORIAS_SIMULADAS

/** Popayán, centro: sirve de ubicación por defecto en los datos simulados. */
const UBICACION_BASE = { latitud: 2.4419, longitud: -76.6063 }

export interface UsuarioSimulado {
  id: string
  nombre: string
  correo: string
  telefono: string
  rol: RolSimulado
  fraseContrasena: string
  estaSuspendido: boolean
  fechaCreacion: string
}

export interface Franja {
  diaSemana: DiaSemana
  abre: string
  cierra: string
}

export interface DocumentoSimulado {
  id: string
  tipo: 'FOTO_LOCAL' | 'RUT'
  tipoContenido: string
  tamanoEnBytes: number
  nombreArchivo: string
  contenido: Blob
}

export interface TiendaSimulada {
  id: string
  duenoId: string
  nombre: string
  telefono: string
  categoriaId: string
  latitud: number
  longitud: number
  estado: EstadoTienda
  estaAbierta: boolean
  motivoRechazo: string | null
  fechaRegistro: string
  fechaRevision: string | null
  horario: Franja[]
  domicilio: {
    haceDomicilio: boolean
    radioCoberturaEnMetros: number | null
    tarifaFijaEnPesos: number | null
  }
  mediosPago: string[]
  documentos: DocumentoSimulado[]
}

export interface DireccionSimulada {
  id: string
  clienteId: string
  nombre: string
  latitud: number
  longitud: number
  direccionEscrita: string
  indicaciones: string | null
  telefono: string
  fechaCreacion: string
}

export interface SolicitudSimulada {
  id: string
  clienteId: string
  categoriaId: string
  producto: string
  cantidad: number
  modalidad: 'RECOGER' | 'DOMICILIO'
  esUrgente: boolean
  ubicacion: { latitud: number; longitud: number }
  radioEnMetros: number
  estado: 'ABIERTA' | 'RESUELTA' | 'VENCIDA'
  fechaPublicacion: string
  fechaVencimiento: string
  tiendasNotificadas: number
}

export interface BaseSimulada {
  usuarios: UsuarioSimulado[]
  tiendas: TiendaSimulada[]
  direcciones: DireccionSimulada[]
  solicitudes: SolicitudSimulada[]
  /** Token temporal del segundo factor → id del administrador. */
  segundosFactoresPendientes: Map<string, string>
}

const HACE_UNA_SEMANA = '2026-11-04T14:00:00Z'

function documentoDePrueba(tipo: DocumentoSimulado['tipo']): DocumentoSimulado {
  const contenido = new Blob([`Documento simulado: ${tipo}`], { type: 'application/pdf' })
  return {
    id: crypto.randomUUID(),
    tipo,
    tipoContenido: 'application/pdf',
    tamanoEnBytes: contenido.size,
    nombreArchivo: `${tipo.toLowerCase()}.pdf`,
    contenido,
  }
}

function usuario(datos: Omit<UsuarioSimulado, 'fraseContrasena' | 'estaSuspendido' | 'fechaCreacion'>) {
  return { ...datos, fraseContrasena: FRASE_DE_PRUEBA, estaSuspendido: false, fechaCreacion: HACE_UNA_SEMANA }
}

function tienda(datos: Pick<TiendaSimulada, 'id' | 'duenoId' | 'nombre' | 'estado' | 'estaAbierta'>): TiendaSimulada {
  return {
    telefono: '3001234567',
    categoriaId: FARMACIA.id,
    ...UBICACION_BASE,
    motivoRechazo: null,
    fechaRegistro: HACE_UNA_SEMANA,
    fechaRevision: null,
    horario: [],
    domicilio: { haceDomicilio: false, radioCoberturaEnMetros: null, tarifaFijaEnPesos: null },
    mediosPago: ['EFECTIVO'],
    documentos: [documentoDePrueba('FOTO_LOCAL'), documentoDePrueba('RUT')],
    ...datos,
  }
}

function crearBaseInicial(): BaseSimulada {
  const cliente = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b10',
    nombre: 'Clienta de Prueba',
    correo: CORREOS_DE_PRUEBA.cliente,
    telefono: '3101112233',
    rol: 'CLIENTE',
  })
  const duenoAprobada = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b20',
    nombre: 'Dueño de Prueba',
    correo: CORREOS_DE_PRUEBA.tienda,
    telefono: '3001234567',
    rol: 'TIENDA',
  })
  const duenoPendiente = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b30',
    nombre: 'Dueña Nueva',
    correo: CORREOS_DE_PRUEBA.tiendaPendiente,
    telefono: '3009876543',
    rol: 'TIENDA',
  })
  const administrador = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b40',
    nombre: 'Administración',
    correo: CORREOS_DE_PRUEBA.administrador,
    telefono: '3000000000',
    rol: 'ADMIN',
  })
  const suspendido = {
    ...usuario({
      id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b50',
      nombre: 'Cuenta Suspendida',
      correo: CORREOS_DE_PRUEBA.suspendido,
      telefono: '3112223344',
      rol: 'CLIENTE',
    }),
    estaSuspendido: true,
  }

  return {
    usuarios: [cliente, duenoAprobada, duenoPendiente, administrador, suspendido],
    tiendas: [
      {
        ...tienda({
          id: 'c1a2b3d4-0001-4e5f-8a9b-0c1d2e3f4a01',
          duenoId: duenoAprobada.id,
          nombre: 'Droguería La 5',
          estado: 'APROBADA',
          estaAbierta: true,
        }),
        fechaRevision: '2026-11-05T15:00:00Z',
        horario: [
          { diaSemana: 'MONDAY', abre: '08:00', cierra: '18:00' },
          { diaSemana: 'TUESDAY', abre: '08:00', cierra: '18:00' },
          { diaSemana: 'WEDNESDAY', abre: '08:00', cierra: '18:00' },
          { diaSemana: 'THURSDAY', abre: '08:00', cierra: '18:00' },
          { diaSemana: 'FRIDAY', abre: '08:00', cierra: '18:00' },
          { diaSemana: 'SATURDAY', abre: '09:00', cierra: '13:00' },
        ],
        domicilio: { haceDomicilio: true, radioCoberturaEnMetros: 3000, tarifaFijaEnPesos: 4000 },
        mediosPago: ['EFECTIVO', 'TRANSFERENCIA'],
      },
      tienda({
        id: 'c1a2b3d4-0002-4e5f-8a9b-0c1d2e3f4a02',
        duenoId: duenoPendiente.id,
        nombre: 'Ferretería El Tornillo',
        estado: 'PENDIENTE',
        estaAbierta: false,
      }),
    ],
    direcciones: [
      {
        id: 'd1e2f3a4-0001-4b5c-8d9e-0f1a2b3c4d01',
        clienteId: cliente.id,
        nombre: 'Casa',
        ...UBICACION_BASE,
        direccionEscrita: 'Calle 5 # 10-20, Centro',
        indicaciones: 'Puerta verde',
        telefono: cliente.telefono,
        fechaCreacion: HACE_UNA_SEMANA,
      },
    ],
    solicitudes: [],
    segundosFactoresPendientes: new Map(),
  }
}

export let base: BaseSimulada = crearBaseInicial()

/** Vuelve a los datos iniciales. Las pruebas lo llaman después de cada caso. */
export function reiniciarDatosSimulados(): void {
  base = crearBaseInicial()
}
