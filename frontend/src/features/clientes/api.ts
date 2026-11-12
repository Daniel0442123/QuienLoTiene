import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CLAVE_DIRECCIONES } from '@/hooks/useDirecciones'
import type { ComponentesProvisionales } from '@/services/api-tipos-provisionales'
import { clienteApi } from '@/services/clienteApi'
import type { DatosDireccionFormulario, DatosPerfil } from './esquemas'

/** Consultas y mutaciones de clientes (DECISIONES-FRONTEND sección 4). La lista de direcciones es useDirecciones. */

export type Direccion = ComponentesProvisionales['DatosDireccion']
export type Perfil = ComponentesProvisionales['PerfilUsuario']

export const MAXIMO_DIRECCIONES = 10
const CLAVE_PERFIL = ['usuarios', 'me'] as const

function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

/** DireccionRequest: la ubicación va en un objeto aparte; el formulario ya la trae así. */
function aPeticion({ ubicacion, ...resto }: DatosDireccionFormulario) {
  if (!ubicacion) throw new Error('La dirección se envió sin ubicación')
  return { ...resto, ubicacion }
}

export function usePerfil() {
  return useQuery({
    queryKey: CLAVE_PERFIL,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/usuarios/me')).data),
  })
}

/** CU-03 pasos 14 a 16: nombre y celular. El correo no se cambia aquí. */
export function useActualizarPerfil() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (perfil: DatosPerfil) =>
      exigirDatos((await clienteApi.PATCH('/api/v1/usuarios/me', { body: perfil })).data),
    onSuccess: (perfil) => clienteConsultas.setQueryData(CLAVE_PERFIL, perfil),
  })
}

/**
 * Guardar, editar y borrar direcciones (CU-03 pasos 8 a 13). Sin actualización optimista: el backend puede responder
 * CLI-002 (ya tiene 10) o CLI-003 (ya no existe). Al terminar se recarga la lista, que también usa publicar solicitud.
 */
function useCambiarDirecciones<Variables, Resultado>(enviar: (variables: Variables) => Promise<Resultado>) {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: enviar,
    onSettled: () => clienteConsultas.invalidateQueries({ queryKey: CLAVE_DIRECCIONES }),
  })
}

export function useCrearDireccion() {
  return useCambiarDirecciones(async (direccion: DatosDireccionFormulario) =>
    exigirDatos((await clienteApi.POST('/api/v1/clientes/me/direcciones', { body: aPeticion(direccion) })).data),
  )
}

export function useEditarDireccion() {
  return useCambiarDirecciones(
    async ({ direccionId, direccion }: { direccionId: string; direccion: DatosDireccionFormulario }) =>
      exigirDatos(
        (
          await clienteApi.PUT('/api/v1/clientes/me/direcciones/{direccionId}', {
            params: { path: { direccionId } },
            body: aPeticion(direccion),
          })
        ).data,
      ),
  )
}

export function useBorrarDireccion() {
  return useCambiarDirecciones(async (direccionId: string) => {
    await clienteApi.DELETE('/api/v1/clientes/me/direcciones/{direccionId}', { params: { path: { direccionId } } })
  })
}
