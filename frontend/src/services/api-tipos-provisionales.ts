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
}

export interface ComponentesProvisionales {
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
