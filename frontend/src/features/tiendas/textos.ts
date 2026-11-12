/** Textos de la funcionalidad de tiendas (DECISIONES-FRONTEND F-12). */
export const textosTiendas = {
  invitacionInicio: '¿Tienes una farmacia o ferretería?',
  enlaceRegistrarTienda: 'Registra tu tienda',

  // Registro de tienda (HU-002, CU-15)
  tituloRegistro: 'Registrar mi tienda',
  introRegistro:
    'Registra tu farmacia o ferretería para recibir las preguntas de los clientes cercanos. Revisamos los documentos antes de activarla.',
  seccionDueno: 'Tus datos',
  seccionTienda: 'Tu tienda',
  seccionDocumentos: 'Documentos',
  introDocumentos: 'JPG, PNG, WebP o PDF de máximo 5 MB. Las fotos las reducimos antes de enviarlas.',

  etiquetaNombreDueno: 'Tu nombre',
  etiquetaCorreo: 'Correo',
  etiquetaTelefono: 'Celular',
  pistaTelefono: 'Solo números, por ejemplo 3001234567. Lo usamos para avisarte de la revisión.',
  etiquetaFrase: 'Frase de contraseña',
  pistaFrase: (largo: number) =>
    `Mínimo 15 caracteres; puedes usar espacios, por ejemplo una oración que recuerdes. Llevas ${largo}.`,
  mostrarFrase: 'Mostrar frase',
  ocultarFrase: 'Ocultar frase',

  etiquetaNombreTienda: 'Nombre de la tienda',
  pistaNombreTienda: 'Como aparece en el aviso del local, por ejemplo Droguería La 5.',
  etiquetaCategoria: 'Qué vende tu tienda',
  cargandoCategorias: 'Cargando categorías…',
  sinCategorias: 'Todavía no hay categorías disponibles.',
  sinCategoriasAyuda: 'Inténtalo más tarde o escríbenos desde la sección de ayuda.',
  etiquetaUbicacion: 'Ubicación del local',
  pistaUbicacion:
    'Si estás en el local, usa tu ubicación. Si no, toca el mapa donde queda; los clientes cercanos te encuentran por aquí.',

  etiquetaFotoLocal: 'Foto del local',
  pistaFotoLocal: 'Una foto de la fachada o del interior donde se vea el aviso.',
  etiquetaRut: 'RUT o documento del negocio',
  pistaRut: 'RUT o certificado de existencia. Puede ser foto o PDF.',
  reduciendoFoto: 'Preparando la foto…',

  etiquetaPolitica: 'Autorizo el tratamiento de mis datos personales según la política de datos.',
  botonRegistrar: 'Registrar tienda',
  botonRegistrando: 'Registrando tienda…',
  tituloResumenErrores: 'Revisa estos datos antes de continuar:',
  yaTienesCuenta: '¿Ya registraste tu tienda?',
  enlaceEntrar: 'Entrar',
  enlaceEntrarConCorreo: 'Entrar con ese correo',
  correoYaRegistrado: 'Este correo ya tiene una cuenta.',
  errorGeneralRegistro: 'No pudimos registrar la tienda. Inténtalo de nuevo.',
  codigoSoporte: (idCorrelacion?: string) => `Código para soporte: ${idCorrelacion ?? ''}`,

  // Confirmación (CU-15 paso 12)
  tituloEnRevision: 'Tu tienda está en revisión',
  textoEnRevision: (nombre: string) =>
    `Recibimos los datos y documentos de ${nombre}. El equipo los revisa y te avisamos al correo cuando la tienda quede aprobada.`,
  textoMientrasTanto: 'Mientras tanto puedes entrar para completar el horario, el domicilio y los medios de pago.',
  tiendaRegistrada: 'Tienda registrada. Queda en revisión.',

  // Validación
  nombreObligatorio: 'Escribe tu nombre.',
  nombreMuyLargo: 'El nombre puede tener máximo 120 caracteres.',
  correoObligatorio: 'Escribe tu correo.',
  correoInvalido: 'Escribe un correo válido, por ejemplo nombre@correo.com.',
  correoMuyLargo: 'El correo puede tener máximo 254 caracteres.',
  telefonoInvalido: 'Escribe tu celular con 7 a 15 dígitos, por ejemplo 3001234567.',
  fraseObligatoria: 'Escribe tu frase de contraseña.',
  fraseCorta: (largo: number) => `La frase debe tener al menos 15 caracteres. Llevas ${largo}.`,
  fraseMuyLarga: 'La frase de contraseña puede tener máximo 128 caracteres.',
  nombreTiendaObligatorio: 'Escribe el nombre de la tienda.',
  nombreTiendaMuyLargo: 'El nombre de la tienda puede tener máximo 120 caracteres.',
  categoriaObligatoria: 'Elige qué vende tu tienda.',
  ubicacionObligatoria: 'Marca la ubicación del local con tu ubicación o tocando el mapa.',
  fotoObligatoria: 'Adjunta una foto del local.',
  rutObligatorio: 'Adjunta el RUT o el certificado de existencia del negocio.',
  archivoTipo: 'Ese archivo no sirve. Usa una foto JPG, PNG o WebP, o un PDF.',
  archivoTamano: 'El archivo pesa más de 5 MB. Usa uno más liviano o una foto.',
  politicaObligatoria: 'Para registrar la tienda debes marcar la autorización de datos.',
} as const
