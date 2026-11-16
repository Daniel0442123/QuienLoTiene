const ENFOCABLES = 'input:not([disabled]), select, textarea, button:not([disabled]), [tabindex]'

/**
 * Lleva el foco al campo con ese id. Si el id es de un grupo (un fieldset de opciones), enfoca la opción marcada o, si
 * no hay, su primer control.
 * Devuelve false si no encontró nada que enfocar.
 */
export function enfocarCampo(idCampo: string): boolean {
  const destino = document.getElementById(idCampo)
  // En un grupo de opciones, la marcada (así se entra con el teclado a un grupo de radios); si no hay, la primera
  const enfocable = destino?.matches(ENFOCABLES)
    ? destino
    : (destino?.querySelector<HTMLElement>('input:checked:not([disabled])') ??
      destino?.querySelector<HTMLElement>(ENFOCABLES))
  enfocable?.focus()
  return Boolean(enfocable)
}
