import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import type { DatosPublicarSolicitud, DatosResponder } from './esquemas'
import type { components } from '@/services/api-tipos'

/** Consultas y mutaciones de solicitudes (DECISIONES-FRONTEND sección 4). */

type Esquemas = components['schemas']

export type Solicitud = Esquemas['SolicitudResponse']
export type SolicitudDisponible = Esquemas['SolicitudDisponibleResponse']
export type RespuestaDeTienda = Esquemas['RespuestaDeTiendaResponse']
export type EstadoRespuesta = Esquemas['EstadoRespuesta']

/** Claves de TanStack Query de las solicitudes de la tienda (el cierre de sesión borra toda la caché). */
export const clavesSolicitudesTienda = {
  disponibles: ['solicitudes', 'disponibles'] as const,
  misRespuestas: ['tiendas', 'me', 'respuestas'] as const,
}

/**
 * Mientras no llega el tiempo real (HU-030), la lista se vuelve a pedir cada 30 s. Con SSE, el evento
 * `solicitud-nueva` invalida esta consulta y el intervalo queda solo como respaldo.
 */
const INTERVALO_SIN_TIEMPO_REAL_MS = 30_000

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/** HU-018: solicitudes abiertas que llegaron a la tienda y no ha respondido; urgentes primero (lo ordena la API). */
export function useSolicitudesDisponibles() {
  return useQuery({
    queryKey: clavesSolicitudesTienda.disponibles,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/solicitudes/disponibles')).data),
    refetchInterval: INTERVALO_SIN_TIEMPO_REAL_MS,
  })
}

/** HU-019 y HU-021: respuestas de la tienda de las últimas 24 h con su estado. */
export function useMisRespuestas() {
  return useQuery({
    queryKey: clavesSolicitudesTienda.misRespuestas,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/tiendas/me/respuestas')).data),
    refetchInterval: INTERVALO_SIN_TIEMPO_REAL_MS,
  })
}

export type SolicitudDelCliente = Esquemas['SolicitudResponse']
export type RespuestasDeSolicitud = Esquemas['RespuestasDeSolicitudResponse']
export type RespuestaParaCliente = Esquemas['RespuestaParaClienteResponse']
export type CriterioOrden = 'precio' | 'distancia' | 'total'

/** Claves de las consultas del cliente sobre su solicitud. */
export const clavesSolicitudCliente = {
  solicitud: (solicitudId: string) => ['solicitudes', solicitudId] as const,
  respuestas: (solicitudId: string, orden: CriterioOrden) => ['solicitudes', solicitudId, 'respuestas', orden] as const,
  todasLasRespuestas: (solicitudId: string) => ['solicitudes', solicitudId, 'respuestas'] as const,
}

/** Mientras la solicitud está abierta y no hay tiempo real (HU-030), las respuestas se vuelven a pedir cada 10 s. */
const INTERVALO_RESPUESTAS_MS = 10_000

/** HU-020 y HU-021: mi solicitud y su estado (ABIERTA, RESUELTA o VENCIDA). */
export function useSolicitud(solicitudId: string) {
  return useQuery({
    queryKey: clavesSolicitudCliente.solicitud(solicitudId),
    queryFn: async () =>
      exigirDatos(
        (await clienteApi.GET('/api/v1/solicitudes/{solicitudId}', { params: { path: { solicitudId } } })).data,
      ),
    refetchInterval: (consulta) => (consulta.state.data?.estado === 'ABIERTA' ? INTERVALO_RESPUESTAS_MS : false),
  })
}

/** HU-020: tarjetas SI y PARECIDO ordenadas por la API; al cambiar el orden se ve la lista anterior hasta que llegue. */
export function useRespuestasDeSolicitud(solicitudId: string, orden: CriterioOrden) {
  return useQuery({
    queryKey: clavesSolicitudCliente.respuestas(solicitudId, orden),
    queryFn: async () =>
      exigirDatos(
        (
          await clienteApi.GET('/api/v1/solicitudes/{solicitudId}/respuestas', {
            params: { path: { solicitudId }, query: { orden } },
          })
        ).data,
      ),
    placeholderData: keepPreviousData,
    refetchInterval: (consulta) =>
      consulta.state.data?.estadoSolicitud === 'ABIERTA' ? INTERVALO_RESPUESTAS_MS : false,
  })
}

type Respuesta = { solicitudId: string } & ({ tipo: 'NO' } | DatosResponder)

/**
 * HU-019: responder SI, PARECIDO o NO, una sola vez (SOL-005). Sin actualización optimista para SI y PARECIDO: el
 * backend puede rechazar por vencida o duplicada. «No lo tengo» ya salió de la lista antes de enviarse (deshacer).
 */
export function useResponderSolicitud() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (respuesta: Respuesta) => {
      // C-04 B3: «No lo tengo» va solo con el tipo y ningún campo se manda en null (el backend no los admite)
      const cuerpo =
        respuesta.tipo === 'NO'
          ? { tipo: 'NO' as const }
          : {
              tipo: respuesta.tipo,
              precioEnPesos: respuesta.precio,
              cantidadDisponible: respuesta.cantidad,
              ...(respuesta.tipo === 'PARECIDO' ? { descripcionAlternativa: respuesta.descripcionAlternativa } : {}),
            }
      const { data } = await clienteApi.POST('/api/v1/solicitudes/{solicitudId}/respuestas', {
        params: { path: { solicitudId: respuesta.solicitudId } },
        body: cuerpo,
      })
      return exigirDatos(data)
    },
    // Respondida, duplicada o cerrada: en todos los casos la lista y «Mis respuestas» cambiaron. No se espera la
    // recarga para que la pantalla avance apenas responde la API.
    onSettled: () => {
      void clienteConsultas.invalidateQueries({ queryKey: clavesSolicitudesTienda.disponibles })
      void clienteConsultas.invalidateQueries({ queryKey: clavesSolicitudesTienda.misRespuestas })
    },
  })
}

/** Desde cuántas letras vale la pena preguntar si el producto es restringido. */
const LARGO_MINIMO_RESTRICCION = 3

/**
 * HU-023: si el producto que escribe el cliente está en la lista de restringidos, para avisar antes de publicar.
 * Se pregunta con el texto ya diferido (no en cada tecla) y se mantiene la respuesta anterior mientras llega la nueva.
 */
export function useRestriccionProducto(producto: string) {
  const texto = producto.trim()
  return useQuery({
    queryKey: ['catalogo', 'restriccion', texto.toLowerCase()],
    queryFn: async () =>
      exigirDatos(
        (await clienteApi.GET('/api/v1/catalogo/restriccion', { params: { query: { producto: texto } } })).data,
      ),
    enabled: texto.length >= LARGO_MINIMO_RESTRICCION,
    placeholderData: keepPreviousData,
    staleTime: 5 * 60_000,
  })
}

/**
 * HU-016: publica «¿quién tiene X?» para las tiendas cercanas. Sin actualización optimista: el backend puede rechazar
 * con 429 (SOL-001) y mostrar un éxito falso confundiría (DECISIONES-FRONTEND 2.3).
 */
export function usePublicarSolicitud() {
  return useMutation({
    mutationFn: async ({ ubicacion, ...datos }: DatosPublicarSolicitud) => {
      if (!ubicacion) throw new Error('La solicitud se envió sin ubicación')
      const { data } = await clienteApi.POST('/api/v1/solicitudes', { body: { ...datos, ubicacion } })
      if (!data) throw new Error('La API no devolvió la solicitud publicada')
      return data
    },
  })
}
