/** Textos de órdenes y pedidos (DECISIONES-FRONTEND F-12). */
export const textosOrdenes = {
  // Elegir una respuesta (HU-024)
  botonElegir: 'Elegir',
  botonElegirDe: (tienda: string) => `Elegir ${tienda}`,
  tituloConfirmar: 'Confirmar pedido',
  volverARespuestas: 'Respuestas',
  resumenEleccion: (tienda: string, producto: string, unidades: string) => `${tienda} · ${producto} · ${unidades}`,
  cargando: 'Cargando…',
  respuestaNoDisponible: 'Esta respuesta ya no se puede elegir.',
  respuestaNoDisponibleAyuda: 'Puede que la solicitud haya vencido o que ya hayas elegido otra tienda.',
  etiquetaModalidad: '¿Cómo lo recibes?',
  modalidadDomicilio: (costo: string) => `Domicilio · ${costo}`,
  modalidadRecoger: 'Recoger en la tienda',
  sinDomicilio: 'Esta tienda no hace domicilio hasta tu ubicación.',
  etiquetaDireccion: '¿A dónde lo llevamos?',
  cargandoDirecciones: 'Cargando tus direcciones…',
  sinDirecciones: 'No tienes direcciones guardadas.',
  enlaceAgregarDireccion: 'Agregar una dirección',
  etiquetaMedioPago: '¿Cómo pagas?',
  pistaMedioPago: 'Le pagas directamente a la tienda al recibir.',
  mediosPago: { EFECTIVO: 'Efectivo', TRANSFERENCIA: 'Transferencia', DATAFONO: 'Datáfono' },
  producto: (precio: string, unidades: string) => `Producto (${precio} × ${unidades})`,
  domicilio: 'Domicilio',
  domicilioNoAplica: 'No aplica',
  total: 'Total',
  avisoDatos: 'Al confirmar, la tienda verá tu nombre, tu teléfono y, si es domicilio, tu dirección.',
  botonConfirmar: (total: string) => `Confirmar pedido · ${total}`,
  botonConfirmando: 'Confirmando…',
  tituloResumenErrores: 'Revisa esto antes de confirmar:',
  errorGeneral: 'No pudimos crear tu pedido. Inténtalo de nuevo.',
  codigoSoporte: (idCorrelacion?: string) => `Código para soporte: ${idCorrelacion ?? ''}`,
  pedidoConfirmado: (tienda: string) => `Pedido confirmado. ${tienda} ya tiene tus datos.`,

  // Validación
  direccionObligatoria: 'Elige a qué dirección llevamos tu pedido.',
  medioPagoObligatorio: 'Elige cómo vas a pagar.',

  // Pedido (HU-024, HU-027)
  tituloPedido: 'Tu pedido',
  numeroPedido: (numero: string) => `Pedido ${numero}`,
  cargandoPedido: 'Cargando el pedido…',
  estados: {
    CONFIRMADO: 'Confirmado',
    PREPARANDO: 'Preparando',
    EN_CAMINO: 'En camino',
    LISTO_PARA_RECOGER: 'Listo para recoger',
    ENTREGADO: 'Entregado',
    RESUELTA: 'Recibido',
    CANCELADO: 'Cancelado',
    VENCIDA: 'Vencido',
  },
  desglose: 'Detalle del pago',
  pagoCon: (medio: string) => `Pagas con ${medio.toLowerCase()} a la tienda.`,
  pagoDelCliente: (medio: string) => `El cliente paga con ${medio.toLowerCase()}.`,
  entregaDomicilio: 'Domicilio',
  entregaRecoger: 'Recoger en la tienda',

  // Seguimiento (HU-027)
  enlaceMisPedidos: 'Mis pedidos',
  tituloMisPedidos: 'Mis pedidos',
  sinPedidos: 'Todavía no tienes pedidos.',
  sinPedidosAyuda: 'Cuando elijas la respuesta de una tienda, tu pedido aparecerá aquí.',
  hacerUnaPregunta: 'Preguntar quién lo tiene',
  volverAMisPedidos: 'Mis pedidos',
  resumenPedido: (producto: string, unidades: string, total: string) => `${producto} · ${unidades} · ${total}`,
  pasos: 'Cómo va tu pedido',
  pasosTienda: 'Cómo va el pedido',
  estadoActual: '(estado actual)',
  paso: (estado: string, hora: string) => `${estado} · ${hora}`,
  estadoCambio: (estado: string) => `Tu pedido ahora está: ${estado}.`,
  activos: 'En curso',
  cerrados: 'Anteriores',

  // Recibí mi pedido (HU-028)
  // DEF-010: la hora va al principio para que su «p. m.» no quede pegado a otro punto
  entregadoPregunta: (tienda: string, hora: string) =>
    hora
      ? `A las ${hora}, ${tienda} marcó tu pedido como entregado. ¿Ya lo tienes?`
      : `${tienda} marcó tu pedido como entregado. ¿Ya lo tienes?`,
  botonRecibi: 'Recibí mi pedido',
  botonRecibiendo: 'Confirmando…',
  pedidoRecibido: 'Listo, tu pedido quedó recibido. ¡Gracias por usar ¿Quién lo tiene?!',
  errorRecibir: 'No pudimos confirmar que recibiste el pedido. Inténtalo de nuevo.',

  // WhatsApp (HU-032)
  botonWhatsapp: (nombre: string) => `Escribir por WhatsApp a ${nombre}`,
  seAbreEnWhatsapp: '(se abre WhatsApp)',
  cargandoWhatsapp: 'Preparando el contacto…',
  errorWhatsapp: 'No pudimos preparar el contacto por WhatsApp.',

  // Pedidos de la tienda (HU-027)
  seccionPedidos: 'Pedidos',
  descripcionSeccionPedidos: 'Los pedidos de los clientes que te eligieron.',
  tituloPedidosTienda: 'Pedidos',
  descripcionPedidosTienda: 'Avanza cada pedido cuando lo prepares, lo envíes y lo entregues.',
  sinPedidosTienda: 'Todavía no tienes pedidos.',
  sinPedidosTiendaAyuda: 'Cuando un cliente elija tu respuesta, el pedido aparecerá aquí.',
  volverAPedidos: 'Pedidos',
  cliente: 'Cliente',
  entregarA: 'Entregar a',
  datosClienteOcultos: 'Los datos del cliente ya no se muestran porque el pedido se cerró.',
  clienteVeCambios: 'El cliente ve cada cambio.',
  siguientePaso: {
    PREPARANDO: 'Empezar a preparar',
    EN_CAMINO: 'Salió a domicilio',
    LISTO_PARA_RECOGER: 'Listo para recoger',
    ENTREGADO: 'Marcar como entregado',
  },
  avanzando: 'Guardando…',
  estadoGuardado: (estado: string) => `Pedido ahora en «${estado}». El cliente ya lo ve.`,
  esperandoCliente: 'Esperando a que el cliente confirme que lo recibió.',
  codigoSoporteOrden: (idCorrelacion?: string) => `Código para soporte: ${idCorrelacion ?? ''}`,

  // Cancelar (HU-029). Las frases no terminan en la hora: su «p. m.» ya trae punto (DEF-010)
  botonCancelar: 'Cancelar pedido',
  etiquetaMotivo: '¿Por qué cancelas el pedido?',
  pistaMotivoCliente: 'La tienda verá lo que escribas.',
  pistaMotivoTienda: 'El cliente verá lo que escribas.',
  contadorMotivo: (largo: number, maximo: number) => `${largo} de ${maximo} caracteres`,
  avisoCancelarCliente: 'La tienda deja de preparar tu pedido. No se puede deshacer.',
  avisoCancelarTienda: 'El cliente recibe el aviso con tu motivo. No se puede deshacer.',
  botonConfirmarCancelacion: 'Sí, cancelar el pedido',
  botonNoCancelar: 'No, volver',
  cancelando: 'Cancelando…',
  errorCancelar: 'No pudimos cancelar el pedido. Inténtalo de nuevo.',
  pedidoCanceladoCliente: 'Cancelaste el pedido. La tienda ya recibió el aviso.',
  pedidoCanceladoTienda: 'Cancelaste el pedido. El cliente ya recibió el aviso.',
  motivoObligatorio: 'Escribe por qué cancelas.',
  motivoCorto: 'Escribe al menos 3 caracteres.',
  motivoLargo: (maximo: number) => `El motivo puede tener máximo ${maximo} caracteres.`,
  yaSalio: 'Cuando el pedido ya salió, escríbele a la tienda o reporta un problema.',
  // Quién canceló, según quién lee (vista del cliente o de la tienda)
  canceladoPor: {
    CLIENTE: {
      cliente: (hora: string) => `A las ${hora}, cancelaste el pedido.`,
      tienda: (hora: string) => `A las ${hora}, el cliente canceló el pedido.`,
    },
    TIENDA: {
      cliente: (hora: string, tienda: string) => `A las ${hora}, ${tienda} canceló el pedido.`,
      tienda: (hora: string) => `A las ${hora}, cancelaste el pedido.`,
    },
  },
  motivoCancelacion: (motivo: string) => `Motivo: «${motivo}»`,
  noLoPrepares: 'No lo prepares ni lo envíes.',
  pasoCancelado: { CLIENTE: 'Cancelado por el cliente', TIENDA: 'Cancelado por la tienda' },

  // Reserva para recoger (HU-025, SC-019)
  reservaHasta: (hora: string) => `Te lo guardan hasta las ${hora}`,
  reservaHastaTienda: (hora: string) => `El cliente tiene hasta las ${hora} para recogerlo`,
  minutosRestantes: (minutos: number) => (minutos <= 1 ? 'Menos de 1 min' : `${minutos} min`),
  quedanMinutos: (minutos: number) => `Quedan ${minutos} minutos`,
  reservaExplicaCliente: 'Si no llegas, la reserva vence y la tienda puede venderlo.',
  reservaExplicaTienda: 'Si no llega a tiempo, el pedido vence solo.',
  avisoCincoMinutos: 'Quedan 5 minutos de tu reserva para recoger el pedido.',
  reservaVencidaCliente: (hora: string) =>
    `A las ${hora} venció la reserva. La tienda ya puede vender el producto; si todavía lo necesitas, pregunta de nuevo.`,
  reservaVencidaTienda: (hora: string) =>
    `A las ${hora} venció la reserva: el cliente no llegó. Ya puedes vender el producto.`,
  tituloReservaVencida: 'La reserva venció',

  // Cómo llegar (HU-026)
  botonComoLlegar: 'Cómo llegar',
  seAbreMapas: '(se abre la app de mapas)',
} as const
