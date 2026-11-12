import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ComponentesProvisionales } from '@/services/api-tipos-provisionales'
import { clienteApi } from '@/services/clienteApi'
import { esErrorApi, type ErrorApi } from '@/services/errores'
import { mensajeParaUsuario } from '@/services/mensajes-error'
import { textosAdministracion as textos } from './textos'

/** Consultas y mutaciones de administración (DECISIONES-FRONTEND sección 4). */

export type TiendaParaRevision = ComponentesProvisionales['TiendaParaRevision']
export type EstadoTienda = TiendaParaRevision['estado']
export type DocumentoResumen = ComponentesProvisionales['DocumentoResumen']

export const ESTADOS_TIENDA: EstadoTienda[] = ['PENDIENTE', 'APROBADA', 'RECHAZADA', 'SUSPENDIDA']

const claveTiendas = (estado: EstadoTienda) => ['admin', 'tiendas', estado] as const

/** Mensaje para el administrador: los códigos de esta pantalla tienen texto propio; los demás, el general. */
export function mensajeAdministracion(error: unknown): string {
  if (!esErrorApi(error)) return textos.errorGeneral
  return textos.errores[error.codigo] ?? mensajeParaUsuario(error)
}

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/** HU-036 (CU-26 pasos 1 a 5): tiendas en un estado; por defecto, las pendientes. */
export function useTiendasPorEstado(estado: EstadoTienda) {
  return useQuery({
    queryKey: claveTiendas(estado),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/admin/tiendas', { params: { query: { estado } } })).data),
  })
}

/**
 * Aprobar o rechazar. Sin actualización optimista: el backend responde 409 (TIE-004) si otra persona ya la revisó.
 * Al terminar (bien o con TIE-004) se recargan todas las listas para que cada tienda quede en su estado real.
 */
function useRevisarTienda<Variables>(enviar: (variables: Variables) => Promise<TiendaParaRevision>) {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: enviar,
    onSettled: (_tienda, error) => {
      if (!error || (esErrorApi(error) && (error as ErrorApi).codigo === 'TIE-004')) {
        void clienteConsultas.invalidateQueries({ queryKey: ['admin', 'tiendas'] })
      }
    },
  })
}

export function useAprobarTienda() {
  return useRevisarTienda(async (tiendaId: string) =>
    exigirDatos(
      (await clienteApi.POST('/api/v1/admin/tiendas/{tiendaId}/aprobacion', { params: { path: { tiendaId } } })).data,
    ),
  )
}

export function useRechazarTienda() {
  return useRevisarTienda(async ({ tiendaId, motivo }: { tiendaId: string; motivo: string }) =>
    exigirDatos(
      (
        await clienteApi.POST('/api/v1/admin/tiendas/{tiendaId}/rechazo', {
          params: { path: { tiendaId } },
          body: { motivo },
        })
      ).data,
    ),
  )
}

/** Nombre del archivo que manda el backend en Content-Disposition, o uno armado con el tipo. */
function nombreDeArchivo(cabecera: string | null, respaldo: string): string {
  const nombre = cabecera?.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i)?.[1]
  return nombre ? decodeURIComponent(nombre) : respaldo
}

/**
 * CU-26 pasos 6 a 11: descarga un documento con el token (no se puede abrir con un enlace común, que no lleva la
 * cabecera Authorization) y lo entrega al navegador como descarga. El backend responde Cache-Control: no-store.
 */
export function useDescargarDocumento() {
  return useMutation({
    mutationFn: async ({ tiendaId, documento }: { tiendaId: string; documento: DocumentoResumen }) => {
      const { data, response } = await clienteApi.GET('/api/v1/admin/tiendas/{tiendaId}/documentos/{documentoId}', {
        params: { path: { tiendaId, documentoId: documento.id } },
        parseAs: 'blob',
      })
      if (!data) throw new Error('El documento llegó vacío')
      const nombre = nombreDeArchivo(
        response.headers.get('Content-Disposition'),
        `${documento.tipo.toLowerCase()}.${documento.tipoContenido.split('/')[1] ?? 'bin'}`,
      )
      const enlace = document.createElement('a')
      enlace.href = URL.createObjectURL(data)
      enlace.download = nombre
      document.body.append(enlace)
      enlace.click()
      enlace.remove()
      // Se libera después: algunos navegadores leen el archivo un momento después del clic
      setTimeout(() => URL.revokeObjectURL(enlace.href), 10_000)
      return nombre
    },
  })
}
