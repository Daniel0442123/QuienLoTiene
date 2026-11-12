import { afterEach, describe, expect, it, vi } from 'vitest'
import { LADO_MAXIMO_PX, problemaDeArchivo, reducirFoto, TAMANO_MAXIMO_BYTES } from './archivos'

function archivo(nombre: string, tipo: string, bytes: number): File {
  return new File([new Uint8Array(bytes)], nombre, { type: tipo })
}

/** jsdom no dibuja: se simulan createImageBitmap y el lienzo con el tamaño de salida que se quiera. */
function simularLienzo({ ancho, alto, bytesSalida }: { ancho: number; alto: number; bytesSalida: number | null }) {
  const dibujar = vi.fn()
  vi.stubGlobal(
    'createImageBitmap',
    vi.fn(async () => ({ width: ancho, height: alto, close: vi.fn() })),
  )
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue({
    fillRect: vi.fn(),
    drawImage: dibujar,
    fillStyle: '',
  } as unknown as CanvasRenderingContext2D)
  vi.spyOn(HTMLCanvasElement.prototype, 'toBlob').mockImplementation(function (alListo) {
    alListo(bytesSalida === null ? null : new Blob([new Uint8Array(bytesSalida)], { type: 'image/jpeg' }))
  })
  return { dibujar }
}

describe('HU-002 · Documentos de la tienda', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('acepta JPEG, PNG, WebP y PDF de hasta 5 MB', () => {
    expect(problemaDeArchivo(archivo('foto.jpg', 'image/jpeg', 1000))).toBeNull()
    expect(problemaDeArchivo(archivo('rut.pdf', 'application/pdf', TAMANO_MAXIMO_BYTES))).toBeNull()
  })

  it('rechaza otros tipos (TIE-003) y lo que pase de 5 MB (GEN-010)', () => {
    expect(problemaDeArchivo(archivo('foto.heic', 'image/heic', 1000))).toBe('tipo')
    expect(problemaDeArchivo(archivo('rut.pdf', 'application/pdf', TAMANO_MAXIMO_BYTES + 1))).toBe('tamano')
  })

  it('deja el PDF igual', async () => {
    const pdf = archivo('rut.pdf', 'application/pdf', 2000)
    expect(await reducirFoto(pdf)).toBe(pdf)
  })

  it('reduce una foto grande a 1600 px de lado y la pasa a JPEG', async () => {
    // Dado
    const { dibujar } = simularLienzo({ ancho: 4000, alto: 3000, bytesSalida: 300_000 })
    const foto = archivo('fachada.png', 'image/png', 4_000_000)

    // Cuando
    const reducida = await reducirFoto(foto)

    // Entonces
    expect(reducida.type).toBe('image/jpeg')
    expect(reducida.name).toBe('fachada.jpg')
    expect(reducida.size).toBe(300_000)
    expect(dibujar).toHaveBeenCalledWith(expect.anything(), 0, 0, LADO_MAXIMO_PX, 1200)
  })

  it('conserva la original si la reducida no pesa menos', async () => {
    simularLienzo({ ancho: 800, alto: 600, bytesSalida: 90_000 })
    const foto = archivo('pequena.jpg', 'image/jpeg', 50_000)
    expect(await reducirFoto(foto)).toBe(foto)
  })

  it('conserva la original si el navegador no puede leerla', async () => {
    vi.stubGlobal(
      'createImageBitmap',
      vi.fn(async () => Promise.reject(new Error('formato no soportado'))),
    )
    const foto = archivo('rara.webp', 'image/webp', 50_000)
    expect(await reducirFoto(foto)).toBe(foto)
  })

  it('conserva la original si el lienzo no entrega imagen', async () => {
    simularLienzo({ ancho: 4000, alto: 3000, bytesSalida: null })
    const foto = archivo('fachada.jpg', 'image/jpeg', 4_000_000)
    expect(await reducirFoto(foto)).toBe(foto)
  })
})
