import axe from 'axe-core'

/**
 * Revisión automática de accesibilidad con axe-core en las pruebas de componentes (DECISIONES-FRONTEND F-17).
 * Uso: expect(await violacionesDeAccesibilidad(container)).toEqual([])
 * Solo detecta una parte de los problemas: la revisión manual con teclado y lector de pantalla sigue siendo necesaria.
 */
export async function violacionesDeAccesibilidad(elemento: Element): Promise<string[]> {
  const resultado = await axe.run(elemento, {
    runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'] },
    // jsdom no calcula estilos ni colores: el contraste lo mide Lighthouse en CI
    rules: { 'color-contrast': { enabled: false } },
  })
  return resultado.violations.map((violacion) => `${violacion.id}: ${violacion.help}`)
}
