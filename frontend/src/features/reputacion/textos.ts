/** Textos de calificaciones y reseñas (HU-034, HU-014; DECISIONES-FRONTEND F-12). */
export const textosReputacion = {
  // Estrellas
  estrellas: (cantidad: number) => (cantidad === 1 ? '1 estrella' : `${cantidad} estrellas`),
  estrellasDeCinco: (cantidad: number) => `${cantidad} de 5 estrellas`,
  promedio: (promedio: string, total: number) =>
    total === 1 ? `${promedio} de 5 · 1 reseña` : `${promedio} de 5 · ${total} reseñas`,
  sinResenas: 'Sin reseñas todavía',

  // Calificar (HU-034)
  tituloCalificar: (tienda: string) => `¿Cómo te fue con ${tienda}?`,
  tituloDocumento: 'Calificar la tienda',
  volverAlPedido: 'Pedido',
  ayudaCalificar: 'Tu calificación ayuda a otros vecinos a elegir.',
  etiquetaCalificacion: 'Calificación',
  calificacionElegida: (estrellas: number) => `Calificación: ${estrellas} de 5 estrellas`,
  calificacionSinElegir: 'Calificación: elige de 1 a 5 estrellas',
  etiquetaComentario: 'Comentario (opcional)',
  pistaComentario: 'Lo verán otros clientes, sin tu nombre.',
  contadorComentario: (largo: number, maximo: number) => `${largo} de ${maximo} caracteres`,
  botonPublicar: 'Publicar calificación',
  publicando: 'Publicando…',
  tituloResumenErrores: 'Revisa esto antes de publicar:',
  estrellasObligatorias: 'Elige de 1 a 5 estrellas.',
  comentarioLargo: (maximo: number) => `El comentario puede tener máximo ${maximo} caracteres.`,
  errorCalificar: 'No pudimos publicar tu calificación. Inténtalo de nuevo.',
  codigoSoporte: (idCorrelacion?: string) => `Código para soporte: ${idCorrelacion ?? ''}`,
  publicada: 'Gracias, tu calificación quedó publicada.',
  cargando: 'Cargando el pedido…',
  noSePuedeCalificar: 'Puedes calificar cuando confirmes que recibiste el pedido.',
  yaCalificaste: 'Ya calificaste este pedido',

  // La reseña en el pedido
  tuCalificacion: 'Tu calificación',
  calificacionDelCliente: 'Calificación del cliente',
  calificarA: (tienda: string) => `Calificar a ${tienda}`,
  invitacionCalificar: '¿Cómo te fue? Tu calificación ayuda a otros vecinos a elegir.',
  clienteSinCalificar: 'El cliente todavía no ha calificado este pedido.',
  fechaResena: (fecha: string) => `el ${fecha}`,
} as const
