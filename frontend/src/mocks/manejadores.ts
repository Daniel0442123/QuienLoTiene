import { http, HttpResponse } from 'msw'

/**
 * Respuestas simuladas de la API, con la forma del contrato OpenAPI y de CODIGOS-DE-ERROR.md.
 * Sirven en las pruebas y para avanzar en una pantalla mientras su historia no esté lista en el backend.
 */
export const CORREO_REGISTRADO = 'cliente@ejemplo.com'
export const FRASE_CORRECTA = 'una frase larga que recuerdo bien'

export const manejadores = [
  http.post('/api/v1/auth/login', async ({ request }) => {
    const { correo, fraseContrasena } = (await request.json()) as { correo: string; fraseContrasena: string }
    if (correo === CORREO_REGISTRADO && fraseContrasena === FRASE_CORRECTA) {
      return HttpResponse.json({
        usuarioId: '7b0c9a52-3f0e-4b8e-9d1a-2f6c1e2a4b10',
        rol: 'CLIENTE',
        tokenAcceso: 'token-de-prueba',
        tipoToken: 'Bearer',
        fechaExpiracion: '2026-11-08T15:30:00Z',
        segundoFactorRequerido: false,
      })
    }
    return HttpResponse.json(
      {
        type: 'https://quienlotiene.app/errores/credenciales-incorrectas',
        title: 'Credenciales incorrectas',
        status: 401,
        codigo: 'IDE-008',
        idCorrelacion: 'prueba-123',
        errores: [],
      },
      { status: 401 },
    )
  }),
  http.post('/api/v1/auth/refresco', () =>
    HttpResponse.json({ title: 'Sesión vencida', status: 401, codigo: 'IDE-010', errores: [] }, { status: 401 }),
  ),
]
