import { describe, expect, it } from 'vitest'
import { errorDesdeRespuesta } from './errores'
import { debeMostrarIdCorrelacion, mensajeParaUsuario } from './mensajes-error'

function respuestaProblema(cuerpo: object, estado: number, cabeceras: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(cuerpo), {
    status: estado,
    headers: { 'Content-Type': 'application/problem+json', ...cabeceras },
  })
}

describe('ProblemDetail → mensaje para el usuario', () => {
  it('usa el mensaje propio del código', async () => {
    const error = await errorDesdeRespuesta(respuestaProblema({ status: 409, codigo: 'IDE-001' }, 409))
    expect(mensajeParaUsuario(error)).toMatch(/Ya existe una cuenta/)
  })

  it('usa el detail del backend si el código no tiene mensaje propio', async () => {
    const error = await errorDesdeRespuesta(
      respuestaProblema({ status: 422, codigo: 'XYZ-999', detail: 'Explicación del backend' }, 422),
    )
    expect(mensajeParaUsuario(error)).toBe('Explicación del backend')
  })

  it('CP-SOL-016-03 · en un 429 dice cuántos minutos esperar según Retry-After', async () => {
    const error = await errorDesdeRespuesta(
      respuestaProblema({ status: 429, codigo: 'SOL-001' }, 429, { 'Retry-After': '700' }),
    )
    expect(mensajeParaUsuario(error)).toMatch(/12 minutos/)
  })

  it('conserva los errores de campo de una validación', async () => {
    const error = await errorDesdeRespuesta(
      respuestaProblema({ status: 400, codigo: 'GEN-001', errores: [{ campo: 'correo', mensaje: 'Inválido' }] }, 400),
    )
    expect(error.esErrorDeValidacion).toBe(true)
    expect(error.errores[0]?.campo).toBe('correo')
  })

  it('muestra el id de correlación solo en errores del servidor', async () => {
    const error500 = await errorDesdeRespuesta(
      respuestaProblema({ status: 500, codigo: 'GEN-008', idCorrelacion: 'abc' }, 500),
    )
    const error404 = await errorDesdeRespuesta(
      respuestaProblema({ status: 404, codigo: 'GEN-005', idCorrelacion: 'abc' }, 404),
    )
    expect(debeMostrarIdCorrelacion(error500)).toBe(true)
    expect(debeMostrarIdCorrelacion(error404)).toBe(false)
  })

  it('arma el error aunque la respuesta no traiga JSON', async () => {
    const error = await errorDesdeRespuesta(new Response('Bad Gateway', { status: 502, statusText: 'Bad Gateway' }))
    expect(error.status).toBe(502)
    expect(error.codigo).toBe('HTTP-502')
  })
})
