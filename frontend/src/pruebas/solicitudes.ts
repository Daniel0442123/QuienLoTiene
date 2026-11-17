import { base, CATEGORIAS_SIMULADAS, CORREOS_DE_PRUEBA, type SolicitudSimulada } from '@/mocks/datos'
import { respuestasAutomaticas } from '@/mocks/reglas-sprint-2'

/** Ayudas de las pruebas del Sprint 2: solicitudes de la clienta de prueba ya publicadas en la simulación. */

export const MINUTO = 60_000
/** Junto a Botica San José: Botica queda más cerca y Farmacia Popular más barata. */
export const JUNTO_A_BOTICA = { latitud: 2.4501, longitud: -76.6001 }

/** Una solicitud de la clienta de prueba; con `respondida`, las dos tiendas simuladas ya contestaron. */
export function crearSolicitud(datos: Partial<SolicitudSimulada> = {}, respondida = true): string {
  const cliente = base.usuarios.find((u) => u.correo === CORREOS_DE_PRUEBA.cliente)!
  const ahora = Date.now()
  const solicitud: SolicitudSimulada = {
    id: crypto.randomUUID(),
    clienteId: cliente.id,
    categoriaId: CATEGORIAS_SIMULADAS[0].id,
    producto: 'Acetaminofén 500 mg',
    cantidad: 1,
    modalidad: 'DOMICILIO',
    esUrgente: false,
    ubicacion: JUNTO_A_BOTICA,
    radioEnMetros: 3000,
    estado: 'ABIERTA',
    fechaPublicacion: new Date(ahora - 5 * MINUTO).toISOString(),
    fechaVencimiento: new Date(ahora + 25 * MINUTO).toISOString(),
    tiendasNotificadas: 3,
    ordenId: null,
    ...datos,
  }
  base.solicitudes.push(solicitud)
  if (respondida) base.respuestas.push(...respuestasAutomaticas(solicitud, ahora - 2 * MINUTO))
  return solicitud.id
}
