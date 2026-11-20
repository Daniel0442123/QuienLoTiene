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
  /** Otra clienta: publica las solicitudes que ve la tienda de prueba (Sprint 2). */
  vecina: 'vecina@ejemplo.com',
  /** Dueños de las dos tiendas que responden solas a cada solicitud nueva (Sprint 2). */
  tiendaBotica: 'botica@ejemplo.com',
  tiendaPopular: 'popular@ejemplo.com',
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

const [FARMACIA, FERRETERIA] = CATEGORIAS_SIMULADAS

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
  ordenId: string | null
}

export type MedioPagoSimulado = 'EFECTIVO' | 'TRANSFERENCIA' | 'DATAFONO'
export type EstadoOrdenSimulado =
  'CONFIRMADO' | 'PREPARANDO' | 'EN_CAMINO' | 'LISTO_PARA_RECOGER' | 'ENTREGADO' | 'RESUELTA' | 'CANCELADO' | 'VENCIDA'

/**
 * Respuesta de una tienda a una solicitud (HU-019). `fechaRespuesta` puede estar en el futuro: así las tiendas
 * simuladas «responden» unos segundos después de publicar sin usar temporizadores; antes de esa hora no existe.
 */
export interface RespuestaSimulada {
  id: string
  solicitudId: string
  tiendaId: string
  tipo: 'SI' | 'PARECIDO' | 'NO'
  precioEnPesos: number | null
  cantidadDisponible: number | null
  descripcionAlternativa: string | null
  tiempoEstimadoEnMinutos: number | null
  ofreceDomicilio: boolean
  costoDomicilioEnPesos: number | null
  fechaRespuesta: string
}

export interface OrdenSimulada {
  id: string
  solicitudId: string
  respuestaId: string
  clienteId: string
  tiendaId: string
  producto: string
  cantidad: number
  precioUnitarioEnPesos: number
  costoDomicilioEnPesos: number
  modalidad: 'RECOGER' | 'DOMICILIO'
  medioPago: MedioPagoSimulado
  direccionId: string | null
  estado: EstadoOrdenSimulado
  fechaCreacion: string
  historial: Array<{ estado: EstadoOrdenSimulado; fecha: string }>
  /** HU-025: hasta cuándo se guarda una recogida LISTO_PARA_RECOGER (SC-019). */
  reservaVence?: string | null
  /** HU-029: motivo y quién canceló. */
  motivoCancelacion?: string | null
  canceladaPor?: 'CLIENTE' | 'TIENDA' | null
}

/** HU-034: una reseña por orden RESUELTA, del cliente de esa orden. */
export interface ResenaSimulada {
  id: string
  ordenId: string
  tiendaId: string
  clienteId: string
  estrellas: number
  comentario: string | null
  fecha: string
}

export interface BaseSimulada {
  usuarios: UsuarioSimulado[]
  tiendas: TiendaSimulada[]
  direcciones: DireccionSimulada[]
  solicitudes: SolicitudSimulada[]
  respuestas: RespuestaSimulada[]
  ordenes: OrdenSimulada[]
  resenas: ResenaSimulada[]
  /** Token temporal del segundo factor → administrador, vencimiento (ms) e intentos fallidos. */
  segundosFactoresPendientes: Map<string, { usuarioId: string; vence: number; intentosFallidos: number }>
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

  const vecina = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b60',
    nombre: 'Vecina de Prueba',
    correo: CORREOS_DE_PRUEBA.vecina,
    telefono: '3157778899',
    rol: 'CLIENTE',
  })
  const duenoBotica = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b70',
    nombre: 'Dueño Botica',
    correo: CORREOS_DE_PRUEBA.tiendaBotica,
    telefono: '3014445566',
    rol: 'TIENDA',
  })
  const duenoPopular = usuario({
    id: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b80',
    nombre: 'Dueña Popular',
    correo: CORREOS_DE_PRUEBA.tiendaPopular,
    telefono: '3025556677',
    rol: 'TIENDA',
  })
  const ejemplosSprint2 = crearEjemplosSprint2(cliente.id, vecina.id)

  return {
    usuarios: [cliente, duenoAprobada, duenoPendiente, administrador, suspendido, vecina, duenoBotica, duenoPopular],
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
      {
        ...tienda({
          id: 'c1a2b3d4-0002-4e5f-8a9b-0c1d2e3f4a02',
          duenoId: duenoPendiente.id,
          nombre: 'Ferretería El Tornillo',
          estado: 'PENDIENTE',
          estaAbierta: false,
        }),
        categoriaId: FERRETERIA.id,
      },
      {
        ...tienda({
          id: TIENDAS_SPRINT_2.botica,
          duenoId: duenoBotica.id,
          nombre: 'Botica San José',
          estado: 'APROBADA',
          estaAbierta: true,
        }),
        telefono: '3014445566',
        latitud: 2.4501,
        longitud: -76.6001,
        fechaRevision: '2026-11-05T15:00:00Z',
        domicilio: { haceDomicilio: true, radioCoberturaEnMetros: 5000, tarifaFijaEnPesos: 3000 },
        mediosPago: ['EFECTIVO', 'DATAFONO'],
      },
      {
        ...tienda({
          id: TIENDAS_SPRINT_2.popular,
          duenoId: duenoPopular.id,
          nombre: 'Farmacia Popular',
          estado: 'APROBADA',
          estaAbierta: true,
        }),
        telefono: '3025556677',
        latitud: 2.4379,
        longitud: -76.6149,
        fechaRevision: '2026-11-05T15:00:00Z',
        mediosPago: ['EFECTIVO', 'TRANSFERENCIA'],
      },
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
      {
        id: 'd1e2f3a4-0002-4b5c-8d9e-0f1a2b3c4d02',
        clienteId: vecina.id,
        nombre: 'Apartamento',
        latitud: 2.444,
        longitud: -76.608,
        direccionEscrita: 'Carrera 9 # 4-15, apto 302',
        indicaciones: null,
        // Otro número que el de la cuenta: con domicilio la tienda ve el de la dirección (quien recibe)
        telefono: '3168889900',
        fechaCreacion: HACE_UNA_SEMANA,
      },
    ],
    ...ejemplosSprint2,
    resenas: crearResenasDeEjemplo(vecina.id),
    segundosFactoresPendientes: new Map(),
  }
}

/** Tienda de prueba (tienda@ejemplo.com) y las dos que responden solas. */
export const TIENDAS_SPRINT_2 = {
  la5: 'c1a2b3d4-0001-4e5f-8a9b-0c1d2e3f4a01',
  botica: 'c1a2b3d4-0003-4e5f-8a9b-0c1d2e3f4a03',
  popular: 'c1a2b3d4-0004-4e5f-8a9b-0c1d2e3f4a04',
} as const

/**
 * Ejemplos del Sprint 2, con fechas relativas a cuando se crean los datos:
 * - La tienda de prueba ve dos solicitudes nuevas de la vecina (una urgente) y tiene un precio anterior de
 *   «Paracetamol 500 mg» para precargar «Sí lo tengo» (SC-011).
 * - La tienda tiene una orden CONFIRMADO con domicilio para avanzar (HU-027).
 * - La clienta de prueba tiene una orden ENTREGADO para confirmar la recepción (HU-028). Su solicitud es de hace
 *   3 horas para no contar en el límite de 10 por hora (HU-016).
 */
function crearEjemplosSprint2(clienteId: string, vecinaId: string) {
  const ahora = Date.now()
  const hace = (minutos: number) => new Date(ahora - minutos * 60_000).toISOString()
  const en = (minutos: number) => new Date(ahora + minutos * 60_000).toISOString()
  const solicitud = (datos: Partial<SolicitudSimulada> & Pick<SolicitudSimulada, 'id' | 'producto'>) => ({
    clienteId: vecinaId,
    categoriaId: FARMACIA.id,
    cantidad: 1,
    modalidad: 'RECOGER' as const,
    esUrgente: false,
    ubicacion: { latitud: 2.444, longitud: -76.608 },
    radioEnMetros: 3000,
    estado: 'ABIERTA' as const,
    fechaPublicacion: hace(5),
    fechaVencimiento: en(25),
    tiendasNotificadas: 3,
    ordenId: null,
    ...datos,
  })
  const respuesta = (datos: Partial<RespuestaSimulada> & Pick<RespuestaSimulada, 'id' | 'solicitudId'>) => ({
    tiendaId: TIENDAS_SPRINT_2.la5,
    tipo: 'SI' as const,
    precioEnPesos: null,
    cantidadDisponible: null,
    descripcionAlternativa: null,
    tiempoEstimadoEnMinutos: null,
    ofreceDomicilio: true,
    costoDomicilioEnPesos: 4000,
    fechaRespuesta: hace(60),
    ...datos,
  })

  const solicitudes: SolicitudSimulada[] = [
    solicitud({
      id: 'a5b6c7d8-0001-4e2f-9a3b-1c2d3e4f5a01',
      producto: 'Paracetamol 500 mg',
      cantidad: 2,
      modalidad: 'DOMICILIO',
      esUrgente: true,
      fechaPublicacion: hace(3),
      fechaVencimiento: en(27),
    }),
    solicitud({ id: 'a5b6c7d8-0002-4e2f-9a3b-1c2d3e4f5a02', producto: 'Suero oral sabor a naranja' }),
    solicitud({
      id: 'a5b6c7d8-0003-4e2f-9a3b-1c2d3e4f5a03',
      producto: 'paracetamol 500 MG',
      cantidad: 2,
      estado: 'VENCIDA',
      fechaPublicacion: hace(2 * 24 * 60),
      fechaVencimiento: hace(2 * 24 * 60 - 30),
    }),
    solicitud({
      id: 'a5b6c7d8-0004-4e2f-9a3b-1c2d3e4f5a04',
      producto: 'Loratadina 10 mg',
      modalidad: 'DOMICILIO',
      estado: 'RESUELTA',
      fechaPublicacion: hace(45),
      fechaVencimiento: hace(15),
      ordenId: 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01',
    }),
    solicitud({
      id: 'a5b6c7d8-0005-4e2f-9a3b-1c2d3e4f5a05',
      clienteId,
      producto: 'Ibuprofeno 400 mg',
      cantidad: 2,
      estado: 'RESUELTA',
      fechaPublicacion: hace(180),
      fechaVencimiento: hace(150),
      ordenId: 'e1f2a3b4-0002-4c5d-8e9f-0a1b2c3d4e02',
    }),
  ]

  const respuestas: RespuestaSimulada[] = [
    respuesta({
      id: 'b6c7d8e9-0001-4f3a-8b4c-2d3e4f5a6b01',
      solicitudId: 'a5b6c7d8-0003-4e2f-9a3b-1c2d3e4f5a03',
      precioEnPesos: 800,
      cantidadDisponible: 2,
      fechaRespuesta: hace(2 * 24 * 60 - 5),
    }),
    respuesta({
      id: 'b6c7d8e9-0002-4f3a-8b4c-2d3e4f5a6b02',
      solicitudId: 'a5b6c7d8-0004-4e2f-9a3b-1c2d3e4f5a04',
      precioEnPesos: 1500,
      cantidadDisponible: 1,
      tiempoEstimadoEnMinutos: 30,
      fechaRespuesta: hace(40),
    }),
    respuesta({
      id: 'b6c7d8e9-0003-4f3a-8b4c-2d3e4f5a6b03',
      solicitudId: 'a5b6c7d8-0005-4e2f-9a3b-1c2d3e4f5a05',
      precioEnPesos: 600,
      cantidadDisponible: 2,
      fechaRespuesta: hace(175),
    }),
  ]

  const ordenes: OrdenSimulada[] = [
    {
      id: 'e1f2a3b4-0001-4c5d-8e9f-0a1b2c3d4e01',
      solicitudId: 'a5b6c7d8-0004-4e2f-9a3b-1c2d3e4f5a04',
      respuestaId: 'b6c7d8e9-0002-4f3a-8b4c-2d3e4f5a6b02',
      clienteId: vecinaId,
      tiendaId: TIENDAS_SPRINT_2.la5,
      producto: 'Loratadina 10 mg',
      cantidad: 1,
      precioUnitarioEnPesos: 1500,
      costoDomicilioEnPesos: 4000,
      modalidad: 'DOMICILIO',
      medioPago: 'EFECTIVO',
      direccionId: 'd1e2f3a4-0002-4b5c-8d9e-0f1a2b3c4d02',
      estado: 'CONFIRMADO',
      fechaCreacion: hace(30),
      historial: [{ estado: 'CONFIRMADO', fecha: hace(30) }],
    },
    {
      id: 'e1f2a3b4-0002-4c5d-8e9f-0a1b2c3d4e02',
      solicitudId: 'a5b6c7d8-0005-4e2f-9a3b-1c2d3e4f5a05',
      respuestaId: 'b6c7d8e9-0003-4f3a-8b4c-2d3e4f5a6b03',
      clienteId,
      tiendaId: TIENDAS_SPRINT_2.la5,
      producto: 'Ibuprofeno 400 mg',
      cantidad: 2,
      precioUnitarioEnPesos: 600,
      costoDomicilioEnPesos: 0,
      modalidad: 'RECOGER',
      medioPago: 'EFECTIVO',
      direccionId: null,
      estado: 'ENTREGADO',
      fechaCreacion: hace(170),
      historial: [
        { estado: 'CONFIRMADO', fecha: hace(170) },
        { estado: 'PREPARANDO', fecha: hace(165) },
        { estado: 'LISTO_PARA_RECOGER', fecha: hace(150) },
        { estado: 'ENTREGADO', fecha: hace(120) },
      ],
    },
  ]

  return { solicitudes, respuestas, ordenes }
}

/**
 * HU-014 y HU-034: tres reseñas de Droguería La 5 de pedidos anteriores de la vecina (promedio 4,7), para que el
 * detalle de la tienda tenga promedio y comentarios. Las órdenes de estas reseñas no están en la simulación.
 */
function crearResenasDeEjemplo(vecinaId: string): ResenaSimulada[] {
  const haceDias = (dias: number) => new Date(Date.now() - dias * 24 * 60 * 60_000).toISOString()
  const resena = (numero: number, estrellas: number, comentario: string | null, dias: number): ResenaSimulada => ({
    id: `f1a2b3c4-000${numero}-4d5e-8f9a-0b1c2d3e4f5${numero}`,
    ordenId: `e1f2a3b4-009${numero}-4c5d-8e9f-0a1b2c3d4e9${numero}`,
    tiendaId: TIENDAS_SPRINT_2.la5,
    clienteId: vecinaId,
    estrellas,
    comentario,
    fecha: haceDias(dias),
  })
  return [
    resena(1, 5, 'Tenían el medicamento y llegó rápido.', 2),
    resena(2, 4, null, 6),
    resena(3, 5, 'Muy amables, me explicaron cómo tomarlo.', 12),
  ]
}

export let base: BaseSimulada = crearBaseInicial()

/** Vuelve a los datos iniciales. Las pruebas lo llaman después de cada caso. */
export function reiniciarDatosSimulados(): void {
  base = crearBaseInicial()
}
