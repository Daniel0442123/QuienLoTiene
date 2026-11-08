/**
 * Errores de la API en formato ProblemDetail (RFC 9457), como los describe docs/api/CODIGOS-DE-ERROR.md.
 * DECISIONES-FRONTEND F-07.
 */

export interface ErrorDeCampo {
  campo: string
  mensaje: string
}

export interface ProblemDetail {
  type?: string
  title?: string
  status: number
  detail?: string
  instance?: string
  codigo?: string
  idCorrelacion?: string
  errores?: ErrorDeCampo[]
}

/** Código propio para cuando no hubo respuesta del servidor (sin conexión, servidor caído). */
export const CODIGO_SIN_CONEXION = 'FRONT-RED'

export class ErrorApi extends Error {
  readonly status: number
  readonly codigo: string
  readonly detalle?: string
  readonly errores: ErrorDeCampo[]
  readonly idCorrelacion?: string
  /** Segundos que pide esperar el servidor en un 429 (cabecera Retry-After). */
  readonly segundosParaReintentar?: number

  constructor(problema: ProblemDetail, segundosParaReintentar?: number) {
    super(problema.title ?? 'Error de la API')
    this.name = 'ErrorApi'
    this.status = problema.status
    this.codigo = problema.codigo ?? `HTTP-${problema.status}`
    this.detalle = problema.detail
    this.errores = problema.errores ?? []
    this.idCorrelacion = problema.idCorrelacion
    this.segundosParaReintentar = segundosParaReintentar
  }

  get esErrorDeValidacion(): boolean {
    return this.errores.length > 0
  }
}

export function esErrorApi(error: unknown): error is ErrorApi {
  return error instanceof ErrorApi
}

function esProblemDetail(cuerpo: unknown): cuerpo is ProblemDetail {
  return typeof cuerpo === 'object' && cuerpo !== null && 'status' in cuerpo
}

function leerSegundosParaReintentar(respuesta: Response): number | undefined {
  const valor = respuesta.headers.get('Retry-After')
  if (!valor) return undefined
  const segundos = Number(valor)
  return Number.isFinite(segundos) ? segundos : undefined
}

/** Convierte una respuesta HTTP fallida en un ErrorApi, venga o no con ProblemDetail. */
export async function errorDesdeRespuesta(respuesta: Response): Promise<ErrorApi> {
  let cuerpo: unknown
  try {
    cuerpo = await respuesta.clone().json()
  } catch {
    cuerpo = undefined
  }
  const problema: ProblemDetail = esProblemDetail(cuerpo)
    ? cuerpo
    : {
        status: respuesta.status,
        title: respuesta.statusText,
        idCorrelacion: respuesta.headers.get('X-Correlacion-Id') ?? undefined,
      }
  return new ErrorApi(problema, leerSegundosParaReintentar(respuesta))
}

export function errorSinConexion(): ErrorApi {
  return new ErrorApi({ status: 0, codigo: CODIGO_SIN_CONEXION, title: 'Sin conexión' })
}
