import { useMutation } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import type { DatosRegistroCliente } from './esquemas'
import { VERSION_POLITICA_DATOS } from '@/services/politicaDatos'

/** Consultas y mutaciones de identidad (DECISIONES-FRONTEND sección 4). El inicio de sesión vive en services/sesion.ts. */

/** HU-001 y HU-005: crea la cuenta de cliente con la versión de la política que la persona leyó. */
export function useRegistrarCliente() {
  return useMutation({
    mutationFn: async (datos: DatosRegistroCliente) => {
      const { data } = await clienteApi.POST('/api/v1/auth/registro', {
        body: { ...datos, versionPoliticaDatos: VERSION_POLITICA_DATOS },
      })
      // El middleware lanza ErrorApi en cualquier respuesta fallida: aquí solo falta un cuerpo inesperado
      if (!data) throw new Error('El registro no devolvió la cuenta creada')
      return data
    },
  })
}
