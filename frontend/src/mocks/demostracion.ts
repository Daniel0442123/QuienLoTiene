import { base, CATEGORIAS_SIMULADAS, CORREOS_DE_PRUEBA, type SolicitudSimulada } from './datos'
import { avisarSolicitudNueva } from './manejadores-solicitudes'

/**
 * Solo en `npm run dev:simulado`: la vecina publica una solicitud cada 90 s (máximo 5, una de cada dos urgente) para
 * ver llegar solicitudes en tiempo real al panel de la tienda (HU-030) sin cambiar de usuario. Las pruebas no lo usan.
 */
const CADA_MS = 90_000
const MAXIMO = 5
const PRODUCTOS = [
  'Loratadina 10 mg',
  'Suero oral sabor a coco',
  'Acetaminofén jarabe infantil',
  'Curitas',
  'Alcohol antiséptico',
]

export function iniciarSolicitudesDeDemostracion(): void {
  let publicadas = 0
  const temporizador = window.setInterval(() => {
    const vecina = base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.vecina)
    if (!vecina || publicadas >= MAXIMO) {
      window.clearInterval(temporizador)
      return
    }
    const ahora = Date.now()
    const solicitud: SolicitudSimulada = {
      id: crypto.randomUUID(),
      clienteId: vecina.id,
      categoriaId: CATEGORIAS_SIMULADAS[0].id,
      producto: PRODUCTOS[publicadas % PRODUCTOS.length] ?? 'Curitas',
      cantidad: 1 + (publicadas % 3),
      modalidad: publicadas % 2 === 0 ? 'DOMICILIO' : 'RECOGER',
      esUrgente: publicadas % 2 === 0,
      ubicacion: { latitud: 2.444, longitud: -76.608 },
      radioEnMetros: 3000,
      estado: 'ABIERTA',
      fechaPublicacion: new Date(ahora).toISOString(),
      fechaVencimiento: new Date(ahora + 30 * 60_000).toISOString(),
      tiendasNotificadas: 3,
      ordenId: null,
    }
    base.solicitudes.push(solicitud)
    avisarSolicitudNueva(solicitud)
    publicadas++
  }, CADA_MS)
}
