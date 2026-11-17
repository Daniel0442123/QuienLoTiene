/** Textos de los avisos en tiempo real (HU-030, DECISIONES-FRONTEND F-12). */
export const textosNotificaciones = {
  solicitudNueva: (producto: string) => `Solicitud nueva: «${producto}».`,
  solicitudUrgente: (producto: string) => `Solicitud urgente: «${producto}». Está primero en tu lista.`,
  pedidoNuevo: (producto: string) => `Te eligieron: pedido nuevo de «${producto}».`,
} as const
