import { manejadoresAdministracion } from './manejadores-administracion'
import { manejadoresCatalogo } from './manejadores-catalogo'
import { manejadoresClientes } from './manejadores-clientes'
import { manejadoresIdentidad } from './manejadores-identidad'
import { manejadoresMensajes } from './manejadores-mensajes'
import { manejadoresNotificaciones } from './manejadores-notificaciones'
import { manejadoresEventos } from './eventos'
import { manejadoresOrdenes } from './manejadores-ordenes'
import { manejadoresReputacion } from './manejadores-reputacion'
import { manejadoresRespuestas } from './manejadores-respuestas'
import { manejadoresHistorial, manejadoresSolicitudes } from './manejadores-solicitudes'
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
  ...manejadoresEventos,
  ...manejadoresCatalogo,
  ...manejadoresClientes,
  // /tiendas/:tiendaId/resenas antes que /tiendas/:tiendaId (MSW no las confunde, pero así se lee mejor)
  ...manejadoresReputacion,
  ...manejadoresTiendas,
  // Rutas de solicitudes del Sprint 2 antes que las del Sprint 1 (/disponibles antes de /:solicitudId), y /mias
  // (HU-008) antes que todas
  ...manejadoresHistorial,
  ...manejadoresRespuestas,
  ...manejadoresSolicitudes,
  ...manejadoresOrdenes,
  ...manejadoresMensajes,
  ...manejadoresNotificaciones,
  ...manejadoresAdministracion,
]
