import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { ComponentesProvisionales } from '@/services/api-tipos-provisionales'
import { clienteApi } from '@/services/clienteApi'
import { VERSION_POLITICA_DATOS } from '@/services/politicaDatos'
import type { DatosRegistroTienda } from './esquemas'

/** Consultas y mutaciones de tiendas (DECISIONES-FRONTEND sección 4). */

export type MiTienda = ComponentesProvisionales['MiTienda']
export type Franja = ComponentesProvisionales['Franja']
export type DiaSemana = ComponentesProvisionales['DiaSemana']
export type Domicilio = ComponentesProvisionales['Domicilio']
export type MedioPago = ComponentesProvisionales['MedioPago']

/** Claves de TanStack Query de la tienda del usuario: se invalidan juntas al cerrar sesión. */
export const clavesTienda = {
  miTienda: ['tiendas', 'me'] as const,
  domicilio: ['tiendas', 'me', 'domicilio'] as const,
  mediosPago: ['tiendas', 'me', 'medios-pago'] as const,
}

/** El middleware de clienteApi lanza ErrorApi en cualquier respuesta fallida: aquí solo falta un cuerpo vacío. */
function exigirDatos<T>(data: T | undefined): T {
  if (data === undefined) throw new Error('La API respondió sin datos')
  return data
}

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

/** La tienda del usuario con rol TIENDA: estado, apertura, perfil y horario (CU-16 pasos 1 a 5). */
export function useMiTienda() {
  return useQuery({
    queryKey: clavesTienda.miTienda,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/tiendas/me')).data),
  })
}

/**
 * HU-013: abrir o cerrar la tienda. Es un cambio de bajo riesgo, así que se muestra al instante (actualización
 * optimista, DECISIONES-FRONTEND 2.3); si el backend lo rechaza (TIE-008) se devuelve al estado anterior.
 */
export function useCambiarApertura() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (abierta: boolean) =>
      exigirDatos((await clienteApi.PATCH('/api/v1/tiendas/me/apertura', { body: { abierta } })).data),
    onMutate: async (abierta) => {
      await clienteConsultas.cancelQueries({ queryKey: clavesTienda.miTienda })
      const anterior = clienteConsultas.getQueryData<MiTienda>(clavesTienda.miTienda)
      if (anterior)
        clienteConsultas.setQueryData<MiTienda>(clavesTienda.miTienda, { ...anterior, estaAbierta: abierta })
      return { anterior }
    },
    onError: (_error, _abierta, contexto) => {
      if (contexto?.anterior) clienteConsultas.setQueryData(clavesTienda.miTienda, contexto.anterior)
    },
    onSuccess: (tienda) => clienteConsultas.setQueryData(clavesTienda.miTienda, tienda),
  })
}

/** HU-009: nombre, teléfono y una franja por día en hora de Colombia (CU-16 pasos 6 a 13). */
export function useActualizarPerfilTienda() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (perfil: ComponentesProvisionales['ActualizarPerfilTiendaRequest']) =>
      exigirDatos((await clienteApi.PUT('/api/v1/tiendas/me/perfil', { body: perfil })).data),
    onSuccess: (tienda) => clienteConsultas.setQueryData(clavesTienda.miTienda, tienda),
  })
}

/** HU-010: domicilio con tarifa fija. */
export function useDomicilio() {
  return useQuery({
    queryKey: clavesTienda.domicilio,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/tiendas/me/domicilio')).data),
  })
}

export function useGuardarDomicilio() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (domicilio: ComponentesProvisionales['ConfigurarDomicilioRequest']) =>
      exigirDatos((await clienteApi.PUT('/api/v1/tiendas/me/domicilio', { body: domicilio })).data),
    onSuccess: (domicilio) => clienteConsultas.setQueryData(clavesTienda.domicilio, domicilio),
  })
}

/** CU-17 pasos 7 a 10: lo que pagaría un cliente con ese subtotal y a esa distancia. TIE-010 si queda fuera. */
export function useSimularDomicilio() {
  return useMutation({
    mutationFn: async (consulta: { subtotalEnPesos: number; distanciaEnMetros: number }) =>
      exigirDatos(
        (await clienteApi.GET('/api/v1/tiendas/me/domicilio/simulacion', { params: { query: consulta } })).data,
      ),
  })
}

/** HU-012: medios de pago que acepta la tienda (solo se registran; no hay cobro en la app). */
export function useMediosPago() {
  return useQuery({
    queryKey: clavesTienda.mediosPago,
    queryFn: async () => exigirDatos((await clienteApi.GET('/api/v1/tiendas/me/medios-pago')).data),
  })
}

export function useGuardarMediosPago() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    mutationFn: async (mediosPago: MedioPago[]) =>
      exigirDatos((await clienteApi.PUT('/api/v1/tiendas/me/medios-pago', { body: { mediosPago } })).data),
    onSuccess: (respuesta) => clienteConsultas.setQueryData(clavesTienda.mediosPago, respuesta),
  })
}
