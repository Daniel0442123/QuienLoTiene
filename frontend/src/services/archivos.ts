/**
 * Documentos que se suben al backend (HU-002): tipos y tamaño que acepta, y reducción de fotos en el navegador
 * antes de enviarlas (arquitectura, rendimiento: menos datos en 4G y menos espera para la tienda).
 */

/** Igual que el backend (TIE-003, GEN-010). */
export const TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024
export const TIPOS_DOCUMENTO_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp', 'application/pdf'] as const

const TIPOS_FOTO_REDUCIBLE = ['image/jpeg', 'image/png', 'image/webp']
/** Lado mayor de la foto reducida: alcanza para leer un RUT o ver la fachada del local. */
export const LADO_MAXIMO_PX = 1600
const CALIDAD_JPEG = 0.82

export type ProblemaArchivo = 'tipo' | 'tamano'

/** Lo que el backend rechazaría por tipo o tamaño, revisado antes de enviar. */
export function problemaDeArchivo(archivo: File): ProblemaArchivo | null {
  if (!(TIPOS_DOCUMENTO_PERMITIDOS as readonly string[]).includes(archivo.type)) return 'tipo'
  if (archivo.size > TAMANO_MAXIMO_BYTES) return 'tamano'
  return null
}

function aBlob(lienzo: HTMLCanvasElement, tipo: string, calidad: number): Promise<Blob | null> {
  return new Promise((resolver) => lienzo.toBlob(resolver, tipo, calidad))
}

/**
 * Reduce una foto a máximo 1600 px de lado y la pasa a JPEG. Los PDF y lo que no se pueda leer quedan igual,
 * y si la versión reducida no pesa menos se conserva la original: nunca empeora el archivo.
 */
export async function reducirFoto(archivo: File): Promise<File> {
  if (!TIPOS_FOTO_REDUCIBLE.includes(archivo.type)) return archivo
  try {
    const imagen = await createImageBitmap(archivo)
    const escala = Math.min(1, LADO_MAXIMO_PX / Math.max(imagen.width, imagen.height))
    const lienzo = document.createElement('canvas')
    lienzo.width = Math.round(imagen.width * escala)
    lienzo.height = Math.round(imagen.height * escala)
    const contexto = lienzo.getContext('2d')
    if (!contexto) return archivo
    // JPEG no tiene transparencia: fondo blanco para que un PNG transparente no quede negro
    contexto.fillStyle = '#ffffff'
    contexto.fillRect(0, 0, lienzo.width, lienzo.height)
    contexto.drawImage(imagen, 0, 0, lienzo.width, lienzo.height)
    imagen.close()

    const reducida = await aBlob(lienzo, 'image/jpeg', CALIDAD_JPEG)
    if (!reducida || reducida.size >= archivo.size) return archivo
    const nombre = archivo.name.replace(/\.[^.]+$/, '') + '.jpg'
    return new File([reducida], nombre, { type: 'image/jpeg', lastModified: archivo.lastModified })
  } catch {
    return archivo
  }
}
