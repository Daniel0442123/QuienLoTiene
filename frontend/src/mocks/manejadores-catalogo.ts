import { http, HttpResponse } from 'msw'
import { base } from './datos'
import { normalizarTexto } from './reglas-sprint-2'
import { autorizar, leerJson, problema, validar, vacio } from './utilidades'

/**
 * Catálogo (HU-038 y HU-023), como GestionarCatalogo y RestriccionController del backend: categorías activas para
 * publicar, administración de categorías y de palabras restringidas, y la consulta del aviso mientras se escribe.
 */

const AVISO_RESTRINGIDO = 'Este medicamento necesita fórmula médica'
const LARGO_MINIMO_PALABRA = 2
const LARGO_MAXIMO_PALABRA = 80

const porNombre = <T extends { nombre: string }>(a: T, b: T) => a.nombre.localeCompare(b.nombre, 'es')

export const manejadoresCatalogo = [
  // HU-016 y HU-002: solo las activas (decisión 6 del contrato del Sprint 3)
  http.get('/api/v1/categorias', () =>
    HttpResponse.json(
      base.categorias
        .filter((categoria) => categoria.activa)
        .sort(porNombre)
        .map(({ id, nombre }) => ({ id, nombre })),
    ),
  ),

  // HU-023: ¿el producto que escribe el cliente está en la lista?
  http.get('/api/v1/catalogo/restriccion', ({ request }) => {
    const cliente = autorizar(request, ['CLIENTE'])
    if (cliente instanceof Response) return cliente
    const producto = new URL(request.url).searchParams.get('producto') ?? ''
    const invalidos = validar([['producto', vacio(producto), 'Escribe el producto']])
    if (invalidos) return invalidos
    const normalizado = normalizarTexto(producto)
    const restringido = base.restringidos.some((r) => normalizado.includes(r.palabraClave))
    return HttpResponse.json({ restringido, aviso: restringido ? AVISO_RESTRINGIDO : null })
  }),

  // HU-038: todas las categorías, activas e inactivas
  http.get('/api/v1/admin/categorias', ({ request }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    return HttpResponse.json([...base.categorias].sort(porNombre))
  }),

  http.patch('/api/v1/admin/categorias/:categoriaId', async ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const { activa } = await leerJson<{ activa: boolean }>(request)
    const invalidos = validar([['activa', typeof activa !== 'boolean', 'Indica si la categoría queda activa']])
    if (invalidos) return invalidos
    const categoria = base.categorias.find((c) => c.id === params.categoriaId)
    if (!categoria) return problema(404, 'CAT-001', 'No encontrado', { detail: 'La categoría no existe.' })
    categoria.activa = activa as boolean
    return HttpResponse.json(categoria)
  }),

  http.get('/api/v1/admin/productos-restringidos', ({ request }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    return HttpResponse.json([...base.restringidos].sort((a, b) => a.palabraClave.localeCompare(b.palabraClave, 'es')))
  }),

  // Se guarda normalizada; CAT-003 por el largo y CAT-002 si ya está (comparando normalizado)
  http.post('/api/v1/admin/productos-restringidos', async ({ request }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const { palabraClave } = await leerJson<{ palabraClave: string }>(request)
    const invalidos = validar([['palabraClave', vacio(palabraClave), 'Escribe la palabra clave']])
    if (invalidos) return invalidos
    const normalizada = normalizarTexto(palabraClave as string)
    if (normalizada.length < LARGO_MINIMO_PALABRA || normalizada.length > LARGO_MAXIMO_PALABRA) {
      return problema(400, 'CAT-003', 'Palabra restringida inválida', {
        detail: 'La palabra debe tener entre 2 y 80 caracteres.',
      })
    }
    if (base.restringidos.some((r) => r.palabraClave === normalizada)) {
      return problema(409, 'CAT-002', 'Palabra restringida duplicada', { detail: 'Esa palabra ya está en la lista.' })
    }
    const nueva = { id: crypto.randomUUID(), palabraClave: normalizada }
    base.restringidos.push(nueva)
    return HttpResponse.json(nueva, {
      status: 201,
      headers: { Location: `/api/v1/admin/productos-restringidos/${nueva.id}` },
    })
  }),

  http.delete('/api/v1/admin/productos-restringidos/:productoRestringidoId', ({ request, params }) => {
    const admin = autorizar(request, ['ADMIN'])
    if (admin instanceof Response) return admin
    const indice = base.restringidos.findIndex((r) => r.id === params.productoRestringidoId)
    if (indice < 0) return problema(404, 'CAT-001', 'No encontrado', { detail: 'La palabra restringida no existe.' })
    base.restringidos.splice(indice, 1)
    return new HttpResponse(null, { status: 204 })
  }),
]
