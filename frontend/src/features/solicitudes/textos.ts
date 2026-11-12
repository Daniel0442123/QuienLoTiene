/** Textos de la funcionalidad de solicitudes (DECISIONES-FRONTEND F-12). */
export const textosSolicitudes = {
  enlacePreguntar: 'Preguntar quién lo tiene',

  // Publicar solicitud (HU-016)
  tituloPublicar: '¿Qué estás buscando?',
  pistaProducto: 'Por ejemplo: Acetaminofén 500 mg, o tornillos de 2 pulgadas.',
  etiquetaCategoria: '¿Dónde buscamos?',
  cargandoCategorias: 'Cargando categorías…',
  sinCategorias: 'Todavía no hay categorías disponibles.',
  sinCategoriasAyuda: 'Inténtalo más tarde.',
  etiquetaUbicacion: '¿Cerca de dónde?',
  otraUbicacion: 'Otra ubicación',
  cargandoDirecciones: 'Cargando tus direcciones…',
  errorDirecciones: 'No pudimos cargar tus direcciones guardadas. Puedes usar tu ubicación o el mapa.',
  sinDirecciones: 'Usa tu ubicación o toca el mapa. Si guardas tus direcciones, la próxima vez irás más rápido.',
  etiquetaMapa: 'Ubicación de la búsqueda',
  pistaMapa: 'Buscamos tiendas a unos 3 km de este punto.',
  etiquetaModalidad: '¿Cómo lo quieres?',
  modalidades: { RECOGER: 'Lo recojo en la tienda', DOMICILIO: 'Domicilio' },
  masDetalles: 'Más detalles (opcional)',
  etiquetaCantidad: 'Cantidad',
  pistaCantidad: 'De 1 a 999 unidades.',
  etiquetaUrgente: 'Lo necesito con urgencia',
  botonPreguntar: 'Preguntar',
  botonPreguntando: 'Preguntando…',
  tituloResumenErrores: 'Revisa estos datos antes de preguntar:',
  errorGeneral: 'No pudimos publicar tu pregunta. Inténtalo de nuevo.',
  codigoSoporte: (idCorrelacion?: string) => `Código para soporte: ${idCorrelacion ?? ''}`,

  // Validación
  productoObligatorio: 'Escribe qué estás buscando.',
  productoMuyLargo: (largo: number) => `Escribe máximo 150 caracteres. Llevas ${largo}.`,
  categoriaObligatoria: 'Elige dónde buscamos: farmacia o ferretería.',
  ubicacionObligatoria: 'Elige cerca de dónde buscamos: una dirección, tu ubicación o un punto del mapa.',
  cantidadInvalida: 'Escribe una cantidad entre 1 y 999.',

  // Confirmación (CU paso 9: «Esperando respuestas»)
  tituloEsperando: 'Esperando respuestas',
  preguntamosA: (tiendas: number, producto: string) =>
    tiendas === 1
      ? `Le preguntamos a 1 tienda cercana si tiene «${producto}».`
      : `Le preguntamos a ${tiendas} tiendas cercanas si tienen «${producto}».`,
  sinTiendas: (producto: string) =>
    `Por ahora ninguna tienda abierta de esa categoría está cerca para preguntarle por «${producto}».`,
  sugerenciaSinTiendas: 'Prueba más tarde o cambia la ubicación de la búsqueda.',
  vence: (hora: string, minutos: number) => `Las tiendas pueden responder hasta las ${hora} (en ${minutos} min).`,
  radio: (distancia: string) => `Buscamos a ${distancia} a la redonda.`,
  hacerOtraPregunta: 'Hacer otra pregunta',
  solicitudPublicada: (tiendas: number) =>
    tiendas === 0 ? 'Pregunta publicada. Por ahora no hay tiendas cerca.' : 'Pregunta publicada. Esperando respuestas.',
} as const
