/** Textos de la navegación principal y de la página de inicio (DECISIONES-FRONTEND F-12). */
export const textosInicio = {
  // Navegación principal por rol (SC-020)
  navegacionPrincipal: 'Menú principal',
  botonAbrirMenu: 'Menú',
  enlaceInicio: 'Inicio',
  enlacePreguntar: 'Preguntar',
  enlaceMisPreguntas: 'Mis preguntas',

  // Inicio sin sesión
  titulo: '¿Quién lo tiene?',
  lema: 'Pregunta una vez y las farmacias y ferreterías cercanas te responden si tienen lo que buscas.',
  explicacion:
    'Deja de llamar o de ir de tienda en tienda. Escribe qué necesitas y las tiendas de tu zona te dicen si lo tienen y a qué precio.',
  tituloComoFunciona: 'Cómo funciona',
  pasos: [
    {
      titulo: 'Pregunta una sola vez',
      descripcion: 'Escribe qué buscas y cuántas unidades: un medicamento, un tornillo, una pieza.',
    },
    {
      titulo: 'Las tiendas te responden',
      descripcion: 'Las farmacias y ferreterías cercanas te dicen si lo tienen, a qué precio y cuántas unidades.',
    },
    {
      titulo: 'Eliges y lo recibes',
      descripcion: 'Comparas las respuestas, eliges la que te sirve y lo recoges en la tienda o pides domicilio.',
    },
  ],
  descripcionTiendas: 'Recibe las preguntas de los clientes de tu zona y véndeles lo que ya tienes en tus estantes.',

  // Inicio con sesión
  tituloCliente: '¿Qué necesitas hoy?',
  introCliente: 'Pregunta y las tiendas cercanas te responden. Desde aquí también sigues tus preguntas y pedidos.',
  descripcionMisPreguntas: 'Mira las respuestas de las tiendas o repite una pregunta.',
  descripcionMisPedidos: 'Sigue tus pedidos, escribe a la tienda o califícala.',
  descripcionMiCuenta: 'Tus datos y tus direcciones de entrega.',
  tituloTienda: 'Te damos la bienvenida',
  introTienda: 'Responde lo que buscan los clientes cerca y atiende los pedidos de quienes te eligieron.',
  descripcionPanel: 'Estado de tu tienda, abrir o cerrar y avisos.',
  tituloAdmin: 'Administración',
  introAdmin: 'Revisa las tiendas nuevas, atiende los reportes y mantén el catálogo al día.',
  descripcionTiendasAdmin: 'Aprueba o rechaza las tiendas que se registran.',
  descripcionReportesAdmin: 'Problemas reportados en los pedidos; suspende una tienda si hace falta.',
  descripcionCatalogoAdmin: 'Categorías y productos restringidos.',
  tituloAccesos: 'Accesos rápidos',
} as const
