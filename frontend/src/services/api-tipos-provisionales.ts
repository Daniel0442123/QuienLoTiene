/**
 * PROVISIONAL. Tipos del contrato escritos a mano, con el mismo formato que genera openapi-typescript, para las rutas
 * que ya usa el frontend mientras `api-tipos.ts` sigue vacío (nadie ha corrido `npm run api:tipos` con el backend).
 *
 * Salen de los DTOs del backend (src/main/java/.../infrastructure/web). Cuando se genere `api-tipos.ts`, se borra este
 * archivo y su uso en `clienteApi.ts`: si algún campo no coincide, el compilador lo señala.
 */

export interface PathsProvisionales {
  '/api/v1/auth/registro': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get?: never
    put?: never
    /** HU-001. Crea una cuenta de cliente activa. */
    post: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['RegistrarClienteRequest'] } }
      responses: {
        201: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['UsuarioRegistradoResponse'] }
        }
      }
    }
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  /**
   * PROPUESTO por el frontend: el backend aún no tiene catálogo (módulo catalogo vacío). Lo usan HU-002 y HU-016.
   * Contrato sugerido a Daniel: lista pública de categorías activas, ordenadas por nombre.
   */
  '/api/v1/categorias': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['CategoriaResponse'][] }
        }
      }
    }
    put?: never
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/registro': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get?: never
    put?: never
    /** HU-002. multipart/form-data: parte "datos" (JSON) y archivos "fotoLocal" y "rut". */
    post: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: {
        content: {
          'multipart/form-data': {
            datos: ComponentesProvisionales['RegistrarTiendaRequest']
            /** Format: binary. openapi-typescript lo genera como string: al regenerar, ajustar el tipo en api.ts */
            fotoLocal: Blob
            /** Format: binary */
            rut: Blob
          }
        }
      }
      responses: {
        201: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['TiendaRegistradaResponse'] }
        }
      }
    }
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/me': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    /** Estado, motivo de rechazo, perfil y horario. */
    get: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['MiTienda'] }
        }
      }
    }
    put?: never
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/me/perfil': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get?: never
    /** HU-009. Los días que no se envían quedan cerrados. */
    put: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['ActualizarPerfilTiendaRequest'] } }
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['MiTienda'] }
        }
      }
    }
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/me/apertura': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get?: never
    put?: never
    post?: never
    delete?: never
    options?: never
    head?: never
    /** HU-013. 409 TIE-008 si la tienda aún no está aprobada. */
    patch: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['CambiarAperturaRequest'] } }
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['MiTienda'] }
        }
      }
    }
    trace?: never
  }
  '/api/v1/tiendas/me/domicilio': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['Domicilio'] }
        }
      }
    }
    /** HU-010. 400 TIE-009 si el domicilio es inválido. */
    put: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['ConfigurarDomicilioRequest'] } }
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['Domicilio'] }
        }
      }
    }
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/me/domicilio/simulacion': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    /** CU-17 pasos 7 a 10. 422 TIE-010 fuera de cobertura. */
    get: {
      parameters: {
        query: { subtotalEnPesos: number; distanciaEnMetros: number }
        header?: never
        path?: never
        cookie?: never
      }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['SimulacionDomicilioResponse'] }
        }
      }
    }
    put?: never
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/tiendas/me/medios-pago': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    get: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['MediosPagoResponse'] }
        }
      }
    }
    /** HU-012. Reemplaza la lista; 400 TIE-011 si no hay ninguno. */
    put: {
      parameters: { query?: never; header?: never; path?: never; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['ConfigurarMediosPagoRequest'] } }
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['MediosPagoResponse'] }
        }
      }
    }
    post?: never
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
}

export interface ComponentesProvisionales {
  DiaSemana: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY'
  /** Hora de Colombia "HH:mm" (LocalTime). */
  Franja: { diaSemana: ComponentesProvisionales['DiaSemana']; abre: string; cierra: string }
  MiTienda: {
    /** Format: uuid */
    id: string
    nombre: string
    telefono: string
    /** Format: uuid */
    categoriaId: string
    latitud: number
    longitud: number
    estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'SUSPENDIDA'
    estaAbierta: boolean
    motivoRechazo: string | null
    /** Format: date-time */
    fechaRegistro: string
    horario: ComponentesProvisionales['Franja'][]
  }
  ActualizarPerfilTiendaRequest: {
    nombre: string
    telefono: string
    horario: ComponentesProvisionales['Franja'][]
  }
  CambiarAperturaRequest: { abierta: boolean }
  Domicilio: {
    haceDomicilio: boolean
    radioCoberturaEnMetros: number | null
    /** "FIJA" (HU-010); null si no hace domicilio. */
    tipoTarifa: string | null
    tarifaFijaEnPesos: number | null
  }
  ConfigurarDomicilioRequest: {
    haceDomicilio: boolean
    radioCoberturaEnMetros: number | null
    tarifaFijaEnPesos: number | null
  }
  SimulacionDomicilioResponse: { costoDomicilioEnPesos: number }
  MedioPago: 'EFECTIVO' | 'TRANSFERENCIA' | 'DATAFONO'
  MediosPagoResponse: { mediosPago: ComponentesProvisionales['MedioPago'][] }
  ConfigurarMediosPagoRequest: { mediosPago: ComponentesProvisionales['MedioPago'][] }
  CategoriaResponse: {
    /** Format: uuid */
    id: string
    nombre: string
  }
  RegistrarTiendaRequest: {
    nombreDueno: string
    correo: string
    telefono: string
    fraseContrasena: string
    aceptaPoliticaDatos: boolean
    versionPoliticaDatos: string
    nombreTienda: string
    /** Format: uuid */
    categoriaId: string
    ubicacion: { latitud: number; longitud: number }
  }
  TiendaRegistradaResponse: {
    /** Format: uuid */
    id: string
    nombre: string
    estado: string
    /** Format: date-time */
    fechaRegistro: string
  }
  RegistrarClienteRequest: {
    nombre: string
    correo: string
    telefono: string
    fraseContrasena: string
    aceptaPoliticaDatos: boolean
    versionPoliticaDatos: string
  }
  UsuarioRegistradoResponse: {
    /** Format: uuid */
    id: string
    nombre: string
    correo: string
    rol: string
    /** Format: date-time */
    fechaCreacion: string
  }
}
