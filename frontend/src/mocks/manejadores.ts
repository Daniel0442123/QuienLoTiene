import { manejadoresAdministracion } from './manejadores-administracion'
import { manejadoresClientes } from './manejadores-clientes'
import { manejadoresIdentidad } from './manejadores-identidad'
import { manejadoresSolicitudes } from './manejadores-solicitudes'
import { manejadoresTiendas } from './manejadores-tiendas'
import { CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA } from './datos'

/**
 * Respuestas simuladas de la API (MSW), con la forma del contrato del backend y de CODIGOS-DE-ERROR.md.
 * Sirven en las pruebas (servidor.ts) y en el navegador con `npm run dev:simulado` (navegador.ts),
 * para avanzar en una pantalla mientras su historia no está lista o el backend no está disponible.
 *
 * Usuarios de prueba: ver datos.ts (CORREOS_DE_PRUEBA, FRASE_DE_PRUEBA, CODIGO_TOTP_DE_PRUEBA).
 */
export const CORREO_REGISTRADO = CORREOS_DE_PRUEBA.cliente
export const FRASE_CORRECTA = FRASE_DE_PRUEBA

export const manejadores = [
  ...manejadoresIdentidad,
  ...manejadoresClientes,
  ...manejadoresTiendas,
  ...manejadoresSolicitudes,
  ...manejadoresAdministracion,
]
