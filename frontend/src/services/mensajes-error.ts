import { CODIGO_SIN_CONEXION, type ErrorApi } from './errores'

/**
 * Mensaje para el usuario por cada código de docs/api/CODIGOS-DE-ERROR.md (DECISIONES-FRONTEND sección 6.4).
 * Cada mensaje dice qué pasó y cómo seguir. Cada pull request del backend que agregue un código agrega aquí su mensaje.
 */
const MENSAJES_POR_CODIGO: Record<string, string> = {
  [CODIGO_SIN_CONEXION]: 'No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo.',
  'GEN-001': 'Hay datos por corregir. Revisa los campos marcados.',
  'GEN-003': 'Tu sesión terminó. Vuelve a entrar.',
  'GEN-004': 'Tu cuenta no tiene permiso para hacer esto.',
  'GEN-005': 'No encontramos lo que buscas. Puede que ya no exista.',
  'GEN-008': 'Algo falló de nuestro lado. Inténtalo de nuevo en unos minutos.',
  'GEN-009': 'La ubicación no es válida. Márcala de nuevo en el mapa.',
  'GEN-010': 'Un archivo pesa más de 5 MB. Usa uno más liviano o una foto.',
  'TIE-001': 'Faltan datos de la tienda: nombre, categoría, celular o ubicación.',
  'TIE-002': 'Falta un documento. Adjunta la foto del local y el RUT.',
  'TIE-003': 'Un documento no sirve. Usa una foto JPG, PNG o WebP, o un PDF de máximo 5 MB.',
  'IDE-001': 'Ya existe una cuenta con ese correo. Inicia sesión o usa otro correo.',
  'IDE-002': 'La frase de contraseña debe tener al menos 15 caracteres. Puedes usar espacios.',
  'IDE-003': 'Esa frase es muy común y fácil de adivinar. Escribe otra, por ejemplo una oración que recuerdes.',
  'IDE-004': 'La frase de contraseña puede tener máximo 128 caracteres.',
  'IDE-005': 'Escribe un correo válido, por ejemplo nombre@correo.com.',
  'IDE-006': 'Para crear la cuenta debes aceptar la política de tratamiento de datos.',
  'IDE-007': 'La política de datos cambió. Léela y acéptala de nuevo para continuar.',
  'IDE-008': 'El correo o la frase de contraseña no coinciden. Revísalos e inténtalo de nuevo.',
  'IDE-009': 'Tu cuenta está suspendida. Escríbenos si crees que es un error.',
  'IDE-010': 'Tu sesión terminó. Vuelve a entrar.',
}

const MENSAJE_GENERAL = 'No pudimos completar la acción. Inténtalo de nuevo.'

export function mensajeParaUsuario(error: ErrorApi): string {
  if (error.status === 429 && error.segundosParaReintentar) {
    const minutos = Math.ceil(error.segundosParaReintentar / 60)
    return `Hiciste muchos intentos seguidos. Vuelve a intentarlo en ${minutos} ${minutos === 1 ? 'minuto' : 'minutos'}.`
  }
  return MENSAJES_POR_CODIGO[error.codigo] ?? error.detalle ?? MENSAJE_GENERAL
}

/** En errores inesperados se muestra el id de correlación para que el equipo lo busque en los logs. */
export function debeMostrarIdCorrelacion(error: ErrorApi): boolean {
  return error.status >= 500 && Boolean(error.idCorrelacion)
}
