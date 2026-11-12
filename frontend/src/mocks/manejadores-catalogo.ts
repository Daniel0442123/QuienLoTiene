import { http, HttpResponse } from 'msw'
import { CATEGORIAS_SIMULADAS } from './datos'

/**
 * Catálogo. PROPUESTA del frontend: el backend todavía no publica categorías (módulo catalogo vacío).
 * GET /api/v1/categorias → [{ id, nombre }], público, ordenado por nombre.
 */
export const manejadoresCatalogo = [
  http.get('/api/v1/categorias', () =>
    HttpResponse.json([...CATEGORIAS_SIMULADAS].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))),
  ),
]
