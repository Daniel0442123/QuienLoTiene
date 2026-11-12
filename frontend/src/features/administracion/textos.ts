/** Textos de la funcionalidad de administración (DECISIONES-FRONTEND F-12). */
export const textosAdministracion = {
  // Revisar tiendas (HU-036, CU-26)
  tituloRevision: 'Revisar tiendas',
  introRevision:
    'Revisa los documentos de cada tienda antes de aprobarla. Una tienda aprobada empieza a recibir solicitudes cuando abre.',
  filtroEstado: 'Mostrar tiendas',
  estados: { PENDIENTE: 'Pendientes', APROBADA: 'Aprobadas', RECHAZADA: 'Rechazadas', SUSPENDIDA: 'Suspendidas' },
  estadoSingular: { PENDIENTE: 'Pendiente', APROBADA: 'Aprobada', RECHAZADA: 'Rechazada', SUSPENDIDA: 'Suspendida' },
  cargando: 'Cargando tiendas…',
  vacio: {
    PENDIENTE: 'No hay tiendas pendientes de revisión.',
    APROBADA: 'Todavía no hay tiendas aprobadas.',
    RECHAZADA: 'No hay tiendas rechazadas.',
    SUSPENDIDA: 'No hay tiendas suspendidas.',
  },
  vacioAyuda: 'Cuando una tienda se registre, aparecerá en Pendientes.',
  registrada: (fecha: string) => `Registrada el ${fecha}`,
  revisada: (fecha: string) => `Revisada el ${fecha}`,
  categoriaDesconocida: 'Categoría sin nombre',
  telefono: (numero: string) => `Celular: ${numero}`,
  verEnMapa: 'Ver la ubicación en OpenStreetMap',
  abreEnOtraPestana: '(se abre en otra pestaña)',
  documentos: 'Documentos',
  sinDocumentos: 'Esta tienda no tiene documentos.',
  tipoDocumento: { FOTO_LOCAL: 'Foto del local', RUT: 'RUT o documento del negocio' },
  descargar: (documento: string, detalle: string) => `Descargar ${documento} (${detalle})`,
  descargando: 'Descargando…',
  motivoRechazo: (motivo: string) => `Motivo del rechazo: ${motivo}`,

  // Aprobar (CP-ADM-036-01)
  botonAprobar: 'Aprobar',
  confirmarAprobar: (nombre: string) => `¿Aprobar ${nombre}? Podrá abrir y recibir solicitudes.`,
  botonConfirmarAprobar: 'Sí, aprobar',
  botonAprobando: 'Aprobando…',
  aprobada: (nombre: string) => `Aprobaste ${nombre}. Ya puede abrir y recibir solicitudes.`,

  // Rechazar (CP-ADM-036-02)
  botonRechazar: 'Rechazar',
  etiquetaMotivo: 'Motivo del rechazo',
  pistaMotivo: 'La tienda lo verá en su panel. Di qué debe corregir, por ejemplo «Documento ilegible».',
  contadorMotivo: (largo: number) => `${largo} de 300 caracteres.`,
  botonConfirmarRechazo: 'Rechazar tienda',
  botonRechazando: 'Rechazando…',
  botonCancelar: 'Cancelar',
  rechazada: (nombre: string) => `Rechazaste ${nombre}. La tienda verá el motivo en su panel.`,
  motivoCorto: (largo: number) => `Escribe el motivo con al menos 5 caracteres. Llevas ${largo}.`,
  motivoLargo: (largo: number) => `El motivo puede tener máximo 300 caracteres. Llevas ${largo}.`,

  // Errores propios de esta pantalla (CODIGOS-DE-ERROR.md)
  errores: {
    'TIE-004': 'Otra persona ya revisó esta tienda. Actualizamos la lista con su estado actual.',
    'TIE-005': 'Escribe el motivo del rechazo, entre 5 y 300 caracteres.',
    'TIE-006': 'No encontramos esa tienda o ese documento. Puede que ya no exista.',
  } as Record<string, string>,
  errorGeneral: 'No pudimos completar la acción. Inténtalo de nuevo.',
  errorDescarga: 'No pudimos descargar el documento. Inténtalo de nuevo.',
} as const
