/** Número corto de la orden, como lo muestra el backend en WhatsApp: los primeros 8 caracteres en mayúsculas. */
export function numeroCorto(ordenId: string): string {
  return ordenId.slice(0, 8).toUpperCase()
}
