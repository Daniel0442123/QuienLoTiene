const ENFOCABLES = 'input:not([disabled]), select, textarea, button:not([disabled]), [tabindex]'

/**
 * Lleva el foco al campo con ese id. Si el id es de un grupo (un fieldset de opciones), enfoca su primer control.
 * Devuelve false si no encontró nada que enfocar.
 */
export function enfocarCampo(idCampo: string): boolean {
  const destino = document.getElementById(idCampo)
  const enfocable = destino?.matches(ENFOCABLES) ? destino : destino?.querySelector<HTMLElement>(ENFOCABLES)
  enfocable?.focus()
  return Boolean(enfocable)
}
