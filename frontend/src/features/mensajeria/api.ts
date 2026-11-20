import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import type { components } from '@/services/api-tipos'

/** Consultas y mutaciones del chat del pedido (HU-033). */

type Esquemas = components['schemas']

export type ChatDeOrden = Esquemas['ChatDeOrden']
export type MensajeDelChat = Esquemas['MensajeVista']

export const clavesMensajes = {
  // Bajo ['ordenes', id]: cuando la orden cambia de estado (SSE) también se refresca si el chat quedó de solo lectura
  chat: (ordenId: string) => ['ordenes', ordenId, 'mensajes'] as const,
}

/** Por si la conexión en tiempo real se cae: con el chat abierto se vuelve a pedir cada 15 s. */
const INTERVALO_SIN_TIEMPO_REAL_MS = 15_000

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/** Mensajes del más antiguo al más reciente y si el chat es de solo lectura (orden en un estado final). */
export function useChat(ordenId: string) {
  return useQuery({
    queryKey: clavesMensajes.chat(ordenId),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/ordenes/{ordenId}/mensajes', { params: { path: { ordenId } } })).data),
    refetchInterval: (consulta) => (consulta.state.data?.soloLectura === false ? INTERVALO_SIN_TIEMPO_REAL_MS : false),
  })
}

/**
 * Enviar un mensaje (1 a 1000 caracteres). Sin actualización optimista: con la orden cerrada responde MEN-002 y se
 * vuelve a pedir el chat, que llega en solo lectura. El mensaje enviado se agrega al final sin esperar la recarga.
 */
export function useEnviarMensaje(ordenId: string) {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (texto: string) =>
      exigirDatos(
        (
          await clienteApi.POST('/api/v1/ordenes/{ordenId}/mensajes', {
            params: { path: { ordenId } },
            body: { texto },
          })
        ).data,
      ),
    onSuccess: (mensaje) =>
      clienteConsultas.setQueryData<ChatDeOrden>(clavesMensajes.chat(ordenId), (chat) =>
        chat && !chat.mensajes.some((m) => m.id === mensaje.id)
          ? { ...chat, mensajes: [...chat.mensajes, mensaje] }
          : chat,
      ),
    onError: () => void clienteConsultas.invalidateQueries({ queryKey: clavesMensajes.chat(ordenId) }),
  })
}
