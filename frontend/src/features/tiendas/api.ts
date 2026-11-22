import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { clienteApi } from '@/services/clienteApi'
import { VERSION_POLITICA_DATOS } from '@/services/politicaDatos'
import type { DatosRegistroTienda } from './esquemas'
import type { components, paths } from '@/services/api-tipos'

type Esquemas = components['schemas']
type CuerpoRegistroTienda = NonNullable<
  paths['/api/v1/tiendas/registro']['post']['requestBody']
>['content']['multipart/form-data']

/** Consultas y mutaciones de tiendas (DECISIONES-FRONTEND sección 4). */

export type MiTienda = Esquemas['MiTienda']
export type Franja = Esquemas['Franja']
export type DiaSemana = Esquemas['DiaSemana']
export type Domicilio = Esquemas['Domicilio']
export type MedioPago = Esquemas['MedioPago']
export type IndicadoresTienda = Esquemas['IndicadoresTienda']
export type PeriodoIndicadores = '7' | '30'

/** Claves de TanStack Query de la tienda del usuario: se invalidan juntas al cerrar sesión. */
export const clavesTienda = {
  miTienda: ['tiendas', 'me'] as const,
  domicilio: ['tiendas', 'me', 'domicilio'] as const,
  mediosPago: ['tiendas', 'me', 'medios-pago'] as const,
  indicadores: (periodo: PeriodoIndicadores) => ['tiendas', 'me', 'indicadores', periodo] as const,
  publica: (tiendaId: string) => ['tiendas', 'publica', tiendaId] as const,
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
      const parteDatos: Esquemas['RegistrarTiendaRequest'] = {
        ...resto,
        ubicacion,
        versionPoliticaDatos: VERSION_POLITICA_DATOS,
      }
      const formulario = new FormData()
      formulario.append('datos', new Blob([JSON.stringify(parteDatos)], { type: 'application/json' }))
      formulario.append('fotoLocal', fotoLocal, fotoLocal instanceof File ? fotoLocal.name : 'foto-local')
      formulario.append('rut', rut, rut instanceof File ? rut.name : 'rut')
      // C-04 B6: openapi-typescript tipa las partes binarias del multipart como string (limitación conocida), así que
      // el cuerpo tipado no puede llevar los archivos. Se arma el FormData aquí y se pasa con una conversión explícita;
      // el serializador lo entrega tal cual.
      const { data } = await clienteApi.POST('/api/v1/tiendas/registro', {
        body: formulario as unknown as CuerpoRegistroTienda,
        bodySerializer: () => formulario,
      })
      if (!data) throw new Error('El registro no devolvió la tienda creada')
      return data
    },
  })
}

export type TiendaPublica = Esquemas['TiendaPublicaResponse']

/**
 * HU-014 y HU-026: detalle público de una tienda APROBADA (404 TIE-006 si no). La orden no trae la ubicación de la
 * tienda (solo id y nombre), así que «Cómo llegar» la toma de aquí.
 */
export function useTiendaPublica(tiendaId: string, habilitado = true) {
  return useQuery({
    queryKey: clavesTienda.publica(tiendaId),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/tiendas/{tiendaId}', { params: { path: { tiendaId } } })).data),
    enabled: habilitado,
    staleTime: 5 * 60_000,
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
    mutationFn: async (perfil: Esquemas['ActualizarPerfilTiendaRequest']) =>
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

/** Lo que entrega el formulario: sin domicilio, el radio y la tarifa quedan en null. */
type DatosGuardarDomicilio = {
  haceDomicilio: boolean
  radioCoberturaEnMetros: number | null
  tarifaFijaEnPesos: number | null
}

export function useGuardarDomicilio() {
  const clienteConsultas = useQueryClient()
  return useMutation({
    // C-04 B8: ConfigurarDomicilioRequest no admite null; sin domicilio solo se envía haceDomicilio
    mutationFn: async ({ haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos }: DatosGuardarDomicilio) =>
      exigirDatos(
        (
          await clienteApi.PUT('/api/v1/tiendas/me/domicilio', {
            body:
              haceDomicilio && radioCoberturaEnMetros !== null && tarifaFijaEnPesos !== null
                ? { haceDomicilio, radioCoberturaEnMetros, tarifaFijaEnPesos }
                : { haceDomicilio: false },
          })
        ).data,
      ),
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

/** HU-015: «Tu resumen» de los últimos 7 o 30 días (SC-011 punto 6). */
export function useIndicadores(periodo: PeriodoIndicadores) {
  return useQuery({
    queryKey: clavesTienda.indicadores(periodo),
    queryFn: async () =>
      exigirDatos((await clienteApi.GET('/api/v1/tiendas/me/indicadores', { params: { query: { periodo } } })).data),
  })
}
