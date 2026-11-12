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
  '/api/v1/admin/tiendas': {
    parameters: { query?: never; header?: never; path?: never; cookie?: never }
    /** HU-036. Por defecto, las PENDIENTE. */
    get: {
      parameters: {
        query?: { estado?: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'SUSPENDIDA' }
        header?: never
        path?: never
        cookie?: never
      }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['TiendaParaRevision'][] }
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
  '/api/v1/admin/tiendas/{tiendaId}/documentos/{documentoId}': {
    parameters: { query?: never; header?: never; path: { tiendaId: string; documentoId: string }; cookie?: never }
    /** HU-036. Archivo con Content-Disposition attachment; 404 TIE-006. */
    get: {
      parameters: { query?: never; header?: never; path: { tiendaId: string; documentoId: string }; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/octet-stream': Blob }
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
  '/api/v1/admin/tiendas/{tiendaId}/aprobacion': {
    parameters: { query?: never; header?: never; path: { tiendaId: string }; cookie?: never }
    get?: never
    put?: never
    /** HU-036. 409 TIE-004 si no está PENDIENTE. */
    post: {
      parameters: { query?: never; header?: never; path: { tiendaId: string }; cookie?: never }
      requestBody?: never
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['TiendaParaRevision'] }
        }
      }
    }
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
  '/api/v1/admin/tiendas/{tiendaId}/rechazo': {
    parameters: { query?: never; header?: never; path: { tiendaId: string }; cookie?: never }
    get?: never
    put?: never
    /** HU-036. Motivo de 5 a 300 caracteres (TIE-005); 409 TIE-004. */
    post: {
      parameters: { query?: never; header?: never; path: { tiendaId: string }; cookie?: never }
      requestBody: { content: { 'application/json': ComponentesProvisionales['RechazarTiendaRequest'] } }
      responses: {
        200: {
          headers: { [nombre: string]: unknown }
          content: { 'application/json': ComponentesProvisionales['TiendaParaRevision'] }
        }
      }
    }
    delete?: never
    options?: never
    head?: never
    patch?: never
    trace?: never
  }
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
}

export interface ComponentesProvisionales {
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
  TiendaParaRevision: {
    /** Format: uuid */
    id: string
    nombre: string
    /** Format: uuid */
    categoriaId: string
    telefono: string
    latitud: number
    longitud: number
    estado: 'PENDIENTE' | 'APROBADA' | 'RECHAZADA' | 'SUSPENDIDA'
    /** Format: date-time */
    fechaRegistro: string
    motivoRechazo: string | null
    /** Format: date-time */
    fechaRevision: string | null
    documentos: ComponentesProvisionales['DocumentoResumen'][]
  }
  DocumentoResumen: {
    /** Format: uuid */
    id: string
    tipo: 'FOTO_LOCAL' | 'RUT'
    tipoContenido: string
    tamanoEnBytes: number
  }
  RechazarTiendaRequest: { motivo: string }
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
