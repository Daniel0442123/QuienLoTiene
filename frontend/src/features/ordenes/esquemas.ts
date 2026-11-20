import { z } from 'zod'
import { textosOrdenes as textos } from './textos'

/**
 * Confirmar el pedido (HU-024), con los campos de CrearOrdenRequest. Con domicilio la dirección es obligatoria
 * (ORD-004); el medio de pago lo revisa además el backend contra los que acepta la tienda (ORD-003).
 */

export const MEDIOS_PAGO = ['EFECTIVO', 'TRANSFERENCIA', 'DATAFONO'] as const

export const esquemaConfirmarPedido = z
  .object({
    modalidad: z.enum(['RECOGER', 'DOMICILIO']),
    direccionId: z.string().nullable(),
    // Sin valor inicial: el cliente elige a propósito cómo paga
    medioPago: z.enum(MEDIOS_PAGO, { message: textos.medioPagoObligatorio }).nullable(),
  })
  .superRefine(({ modalidad, direccionId, medioPago }, contexto) => {
    if (modalidad === 'DOMICILIO' && !direccionId) {
      contexto.addIssue({ code: 'custom', path: ['direccionId'], message: textos.direccionObligatoria })
    }
    if (medioPago === null) {
      contexto.addIssue({ code: 'custom', path: ['medioPago'], message: textos.medioPagoObligatorio })
    }
  })
  .transform(({ modalidad, direccionId, medioPago }) => ({
    modalidad,
    direccionId: modalidad === 'DOMICILIO' ? direccionId : null,
    medioPago: medioPago as (typeof MEDIOS_PAGO)[number],
  }))

/** HU-029: motivo de la cancelación, de 3 a 200 caracteres (CancelarOrdenRequest). */
export const LARGO_MAXIMO_MOTIVO = 200

export const esquemaCancelar = z.object({
  motivo: z
    .string()
    .trim()
    .min(1, textos.motivoObligatorio)
    .pipe(z.string().min(3, textos.motivoCorto).max(LARGO_MAXIMO_MOTIVO, textos.motivoLargo(LARGO_MAXIMO_MOTIVO))),
})

export type DatosCancelar = z.output<typeof esquemaCancelar>

export type EntradaConfirmarPedido = z.input<typeof esquemaConfirmarPedido>
export type DatosConfirmarPedido = z.output<typeof esquemaConfirmarPedido>
