/** Textos de los avisos en tiempo real (HU-030, DECISIONES-FRONTEND F-12). */
export const textosNotificaciones = {
  solicitudNueva: (producto: string) => `Solicitud nueva: «${producto}».`,
  solicitudUrgente: (producto: string) => `Solicitud urgente: «${producto}». Está primero en tu lista.`,
  pedidoNuevo: (producto: string) => `Te eligieron: pedido nuevo de «${producto}».`,
  radioAmpliado: (distancia: string) => `Nadie ha respondido aún. Ampliamos la búsqueda a ${distancia}.`,

  // Avisos push (HU-031)
  tituloAvisos: 'Avisos con la app cerrada',
  explicacionAvisos:
    'Te avisamos en este dispositivo cuando llegue una solicitud nueva o un pedido, aunque no tengas la app abierta. El aviso no lleva datos de los clientes.',
  botonActivar: 'Activar avisos',
  activando: 'Activando…',
  botonDesactivar: 'Desactivar avisos',
  desactivando: 'Desactivando…',
  estadoActivo: 'Los avisos están activados en este dispositivo.',
  estadoInactivo: 'Los avisos están desactivados en este dispositivo.',
  avisosActivados: 'Listo: te avisaremos en este dispositivo.',
  avisosDesactivados: 'Desactivaste los avisos en este dispositivo.',
  bloqueado:
    'Bloqueaste los avisos de ¿Quién lo tiene? en este navegador. Para activarlos, permítelos en la configuración del sitio (el candado junto a la dirección) y vuelve aquí.',
  noSoportado:
    'Este navegador no puede mostrar avisos. Las solicitudes y los pedidos te esperan en el panel cuando abras la app.',
  sinServiceWorker:
    'Los avisos se activan en la app publicada o instalada. En este modo de prueba no están disponibles.',
  errorActivar: 'No pudimos activar los avisos. Inténtalo de nuevo.',
  errorDesactivar: 'No pudimos desactivar los avisos. Inténtalo de nuevo.',
} as const
