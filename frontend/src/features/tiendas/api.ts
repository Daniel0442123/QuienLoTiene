import { useMutation } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import { VERSION_POLITICA_DATOS } from '@/services/politicaDatos'
import type { DatosRegistroTienda } from './esquemas'

/** Consultas y mutaciones de tiendas (DECISIONES-FRONTEND sección 4). */

/**
 * HU-002: registra la tienda y la cuenta del dueño (queda PENDIENTE). multipart/form-data con la parte "datos" en JSON
 * (el backend exige Content-Type application/json en esa parte) y los archivos "fotoLocal" y "rut".
 */
export function useRegistrarTienda() {
  return useMutation({
    mutationFn: async (datos: DatosRegistroTienda) => {
      const { fotoLocal, rut, ubicacion, ...resto } = datos
      if (!fotoLocal || !rut || !ubicacion) throw new Error('El formulario se envió sin documentos o sin ubicación')
      const { data } = await clienteApi.POST('/api/v1/tiendas/registro', {
        body: {
          datos: { ...resto, ubicacion, versionPoliticaDatos: VERSION_POLITICA_DATOS },
          fotoLocal,
          rut,
        },
        bodySerializer: ({ datos: parteDatos, fotoLocal: foto, rut: documento }) => {
          const formulario = new FormData()
          formulario.append('datos', new Blob([JSON.stringify(parteDatos)], { type: 'application/json' }))
          formulario.append('fotoLocal', foto, foto instanceof File ? foto.name : 'foto-local')
          formulario.append('rut', documento, documento instanceof File ? documento.name : 'rut')
          return formulario
        },
      })
      if (!data) throw new Error('El registro no devolvió la tienda creada')
      return data
    },
  })
}
