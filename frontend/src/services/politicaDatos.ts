/**
 * Política de tratamiento de datos personales (Ley 1581 de 2012, HU-005).
 *
 * BORRADOR: el texto definitivo lo aprueba el equipo (Wilson, Product Owner). La versión debe coincidir con
 * `qlt.version-politica-datos` del backend; si no coincide, el registro responde IDE-007.
 */
export const VERSION_POLITICA_DATOS = '1.0'

export const politicaDatos = {
  resumen: 'Leer la política de tratamiento de datos',
  titulo: `Política de tratamiento de datos · versión ${VERSION_POLITICA_DATOS}`,
  secciones: [
    {
      titulo: 'Qué datos pedimos',
      texto:
        'Tu nombre, correo, celular, las direcciones que guardes y la ubicación aproximada cuando publicas una solicitud.',
    },
    {
      titulo: 'Para qué los usamos',
      texto:
        'Para crear tu cuenta, avisar a las tiendas cercanas de lo que buscas y que la tienda que elijas pueda coordinar la entrega contigo. No vendemos tus datos.',
    },
    {
      titulo: 'Con quién los compartimos',
      texto:
        'Las tiendas solo ven tu solicitud y tu zona. Tu nombre, celular y dirección se comparten únicamente con la tienda a la que le haces un pedido.',
    },
    {
      titulo: 'Tus derechos',
      texto:
        'Puedes conocer, actualizar y rectificar tus datos, pedir prueba de esta autorización y revocarla escribiéndonos desde la sección de ayuda.',
    },
  ],
} as const
