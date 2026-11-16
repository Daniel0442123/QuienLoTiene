/**
 * Alerta sonora de una solicitud urgente (CP-NOT-030-02). Se genera con Web Audio (dos tonos cortos), sin archivos de
 * audio. Acompaña al aviso en texto de la región aria-live; nunca es la única señal (WCAG 1.3.3). Si el navegador no
 * deja sonar (todavía no hubo un gesto del usuario) se ignora.
 */
type ConstructorAudio = typeof AudioContext

let contexto: AudioContext | null = null

export function sonarAlertaUrgente(): void {
  try {
    const Audio = (globalThis.AudioContext ??
      (globalThis as unknown as { webkitAudioContext?: ConstructorAudio }).webkitAudioContext) as
      ConstructorAudio | undefined
    if (!Audio) return
    contexto ??= new Audio()
    const ahora = contexto.currentTime
    for (const [inicio, frecuencia] of [
      [0, 880],
      [0.2, 1175],
    ] as const) {
      const oscilador = contexto.createOscillator()
      const volumen = contexto.createGain()
      oscilador.frequency.value = frecuencia
      volumen.gain.setValueAtTime(0.2, ahora + inicio)
      volumen.gain.exponentialRampToValueAtTime(0.001, ahora + inicio + 0.18)
      oscilador.connect(volumen).connect(contexto.destination)
      oscilador.start(ahora + inicio)
      oscilador.stop(ahora + inicio + 0.18)
    }
  } catch {
    // Sin sonido no se pierde nada: el aviso en texto ya se anunció
  }
}
