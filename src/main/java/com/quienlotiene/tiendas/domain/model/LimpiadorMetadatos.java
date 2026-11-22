package com.quienlotiene.tiendas.domain.model;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * RNF-012 (NF-SEG-012): quita los metadatos de las imágenes subidas antes de guardarlas, porque una foto del celular
 * puede traer en EXIF la ubicación GPS, el modelo del equipo y la fecha. Se quitan:
 *
 * <ul>
 *   <li>JPEG: los segmentos APP1 (EXIF y XMP) y APP13 (IPTC). APP0 (JFIF) y APP2 (perfil de color) se conservan.
 *   <li>PNG: los bloques {@code eXIf}, {@code tEXt}, {@code zTXt} e {@code iTXt}.
 *   <li>WebP: los bloques {@code EXIF} y {@code XMP } del contenedor RIFF, y sus marcas en {@code VP8X}.
 * </ul>
 *
 * Los PDF no llevan EXIF y se guardan como llegan. Si la estructura no se puede leer, el archivo se rechaza: un
 * archivo que no se puede limpiar no se guarda.
 */
final class LimpiadorMetadatos {

    private static final int APP1 = 0xE1;
    private static final int APP13 = 0xED;
    private static final int SOS = 0xDA;
    private static final int EOI = 0xD9;
    private static final int TAMANO_FIRMA_PNG = 8;
    private static final Set<String> BLOQUES_PNG_CON_METADATOS = Set.of("eXIf", "tEXt", "zTXt", "iTXt");
    private static final Set<String> BLOQUES_WEBP_CON_METADATOS = Set.of("EXIF", "XMP ");
    private static final int MARCA_VP8X_EXIF = 0x08;
    private static final int MARCA_VP8X_XMP = 0x04;
    private static final int INICIO_BLOQUES_WEBP = 12;

    private LimpiadorMetadatos() {}

    /**
     * @return el contenido sin metadatos
     * @throws IllegalArgumentException si la estructura del archivo está dañada
     */
    static byte[] limpiar(TipoArchivo tipo, byte[] contenido) {
        return switch (tipo) {
            case JPEG -> limpiarJpeg(contenido);
            case PNG -> limpiarPng(contenido);
            case WEBP -> limpiarWebp(contenido);
            case PDF -> contenido;
        };
    }

    private static byte[] limpiarJpeg(byte[] jpeg) {
        var salida = new ByteArrayOutputStream(jpeg.length);
        salida.write(jpeg, 0, 2); // SOI
        var posicion = 2;
        while (posicion < jpeg.length) {
            exigir(posicion + 1 < jpeg.length && (jpeg[posicion] & 0xFF) == 0xFF);
            var marcador = jpeg[posicion + 1] & 0xFF;
            if (marcador == 0xFF) { // byte de relleno
                posicion++;
                continue;
            }
            if (marcador == SOS || marcador == EOI) {
                // Desde aquí van los datos de la imagen: se copian tal cual
                salida.write(jpeg, posicion, jpeg.length - posicion);
                return salida.toByteArray();
            }
            if (marcador == 0x01 || (marcador >= 0xD0 && marcador <= 0xD7)) {
                salida.write(jpeg, posicion, 2);
                posicion += 2;
                continue;
            }
            exigir(posicion + 3 < jpeg.length);
            var longitud = ((jpeg[posicion + 2] & 0xFF) << 8) | (jpeg[posicion + 3] & 0xFF);
            exigir(longitud >= 2 && posicion + 2 + longitud <= jpeg.length);
            if (marcador != APP1 && marcador != APP13) {
                salida.write(jpeg, posicion, 2 + longitud);
            }
            posicion += 2 + longitud;
        }
        return salida.toByteArray();
    }

    private static byte[] limpiarPng(byte[] png) {
        var salida = new ByteArrayOutputStream(png.length);
        salida.write(png, 0, TAMANO_FIRMA_PNG);
        var posicion = TAMANO_FIRMA_PNG;
        while (posicion < png.length) {
            exigir(posicion + 8 <= png.length);
            var longitud = enteroBigEndian(png, posicion);
            var tipo = new String(png, posicion + 4, 4, StandardCharsets.ISO_8859_1);
            var total = 12L + longitud; // longitud + tipo + datos + CRC
            exigir(longitud >= 0 && posicion + total <= png.length);
            if (!BLOQUES_PNG_CON_METADATOS.contains(tipo)) {
                salida.write(png, posicion, (int) total);
            }
            posicion += (int) total;
        }
        return salida.toByteArray();
    }

    private static byte[] limpiarWebp(byte[] webp) {
        var bloques = new ByteArrayOutputStream(webp.length);
        var posicion = INICIO_BLOQUES_WEBP;
        var posicionVp8x = -1;
        while (posicion < webp.length) {
            exigir(posicion + 8 <= webp.length);
            var tipo = new String(webp, posicion, 4, StandardCharsets.ISO_8859_1);
            var longitud = enteroLittleEndian(webp, posicion + 4);
            var total = 8L + longitud + (longitud % 2); // los bloques se rellenan a número par
            exigir(longitud >= 0 && posicion + 8 + longitud <= webp.length);
            total = Math.min(total, webp.length - posicion);
            if (!BLOQUES_WEBP_CON_METADATOS.contains(tipo)) {
                if ("VP8X".equals(tipo)) {
                    posicionVp8x = bloques.size();
                }
                bloques.write(webp, posicion, (int) total);
            }
            posicion += (int) total;
        }
        var cuerpo = bloques.toByteArray();
        if (posicionVp8x >= 0 && cuerpo.length > posicionVp8x + 8) {
            cuerpo[posicionVp8x + 8] &= (byte) ~(MARCA_VP8X_EXIF | MARCA_VP8X_XMP);
        }
        var salida = new ByteArrayOutputStream(INICIO_BLOQUES_WEBP + cuerpo.length);
        salida.write(webp, 0, 4); // RIFF
        var tamanoRiff = 4 + cuerpo.length; // "WEBP" + bloques
        salida.write(tamanoRiff & 0xFF);
        salida.write((tamanoRiff >> 8) & 0xFF);
        salida.write((tamanoRiff >> 16) & 0xFF);
        salida.write((tamanoRiff >> 24) & 0xFF);
        salida.write(webp, 8, 4); // WEBP
        salida.write(cuerpo, 0, cuerpo.length);
        return salida.toByteArray();
    }

    private static int enteroBigEndian(byte[] datos, int desde) {
        return ((datos[desde] & 0xFF) << 24)
                | ((datos[desde + 1] & 0xFF) << 16)
                | ((datos[desde + 2] & 0xFF) << 8)
                | (datos[desde + 3] & 0xFF);
    }

    private static int enteroLittleEndian(byte[] datos, int desde) {
        return (datos[desde] & 0xFF)
                | ((datos[desde + 1] & 0xFF) << 8)
                | ((datos[desde + 2] & 0xFF) << 16)
                | ((datos[desde + 3] & 0xFF) << 24);
    }

    private static void exigir(boolean condicion) {
        if (!condicion) {
            throw new IllegalArgumentException("Estructura de archivo dañada");
        }
    }
}
