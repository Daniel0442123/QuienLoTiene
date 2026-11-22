package com.quienlotiene.tiendas.fixtures;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Contenidos mínimos con la firma y la estructura real de cada formato, para probar la detección por contenido y la
 * limpieza de metadatos (RNF-012).
 */
public final class ArchivosDePrueba {

    /** Texto que va dentro del EXIF, el XMP o el texto de las imágenes «con metadatos». */
    public static final String METADATO = "GPS 2.4448,-76.6147 Samsung SM-A546";

    private ArchivosDePrueba() {}

    /** JPEG con APP0 (JFIF), una tabla de cuantización ficticia, SOS y EOI. */
    public static byte[] unJpeg() {
        return jpeg(false);
    }

    /** El mismo JPEG con un segmento APP1 de EXIF y otro APP13 (IPTC) con {@link #METADATO}. */
    public static byte[] unJpegConExif() {
        return jpeg(true);
    }

    private static byte[] jpeg(boolean conMetadatos) {
        var salida = new ByteArrayOutputStream();
        escribir(salida, 0xFF, 0xD8);
        escribir(salida, 0xFF, 0xE0, 0x00, 0x10);
        escribir(salida, "JFIF".getBytes(StandardCharsets.US_ASCII));
        escribir(salida, 0x00, 0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00);
        if (conMetadatos) {
            segmentoJpeg(salida, 0xE1, ("Exif\0\0" + METADATO).getBytes(StandardCharsets.ISO_8859_1));
            segmentoJpeg(salida, 0xED, ("Photoshop 3.0\0" + METADATO).getBytes(StandardCharsets.ISO_8859_1));
        }
        segmentoJpeg(salida, 0xDB, new byte[65]);
        escribir(salida, 0xFF, 0xDA, 0x00, 0x08, 0x01, 0x01, 0x00, 0x00, 0x3F, 0x00);
        escribir(salida, 0x12, 0x34, 0x56, 0x78);
        escribir(salida, 0xFF, 0xD9);
        return salida.toByteArray();
    }

    /** PNG de 1 x 1 con IHDR, IDAT e IEND. */
    public static byte[] unPng() {
        return png(false);
    }

    /** El mismo PNG con bloques eXIf y tEXt con {@link #METADATO}. */
    public static byte[] unPngConExif() {
        return png(true);
    }

    private static byte[] png(boolean conMetadatos) {
        var salida = new ByteArrayOutputStream();
        escribir(salida, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
        bloquePng(salida, "IHDR", new byte[] {0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0});
        if (conMetadatos) {
            bloquePng(salida, "eXIf", METADATO.getBytes(StandardCharsets.ISO_8859_1));
            bloquePng(salida, "tEXt", ("Comment\0" + METADATO).getBytes(StandardCharsets.ISO_8859_1));
        }
        bloquePng(salida, "IDAT", new byte[] {0x78, (byte) 0x9C, 0x63, 0x60, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01});
        bloquePng(salida, "IEND", new byte[0]);
        return salida.toByteArray();
    }

    /** WebP extendido (VP8X) con un bloque VP8. */
    public static byte[] unWebp() {
        return webp(false);
    }

    /** El mismo WebP con bloques EXIF y XMP con {@link #METADATO} y sus marcas en VP8X. */
    public static byte[] unWebpConExif() {
        return webp(true);
    }

    private static byte[] webp(boolean conMetadatos) {
        var bloques = new ByteArrayOutputStream();
        var marcas = conMetadatos ? 0x08 | 0x04 : 0;
        bloqueWebp(bloques, "VP8X", new byte[] {(byte) marcas, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        bloqueWebp(bloques, "VP8 ", new byte[] {0x10, 0x02, 0x00, (byte) 0x9D, 0x01, 0x2A, 0x01, 0x00, 0x01});
        if (conMetadatos) {
            bloqueWebp(bloques, "EXIF", METADATO.getBytes(StandardCharsets.ISO_8859_1));
            bloqueWebp(bloques, "XMP ", METADATO.getBytes(StandardCharsets.ISO_8859_1));
        }
        var cuerpo = bloques.toByteArray();
        var salida = new ByteArrayOutputStream();
        escribir(salida, "RIFF".getBytes(StandardCharsets.US_ASCII));
        enteroLittleEndian(salida, 4 + cuerpo.length);
        escribir(salida, "WEBP".getBytes(StandardCharsets.US_ASCII));
        escribir(salida, cuerpo);
        return salida.toByteArray();
    }

    public static byte[] unPdf() {
        return conRelleno("%PDF-1.7".getBytes(StandardCharsets.US_ASCII));
    }

    /** Un ejecutable de Windows que alguien renombró a .pdf. */
    public static byte[] unEjecutableDisfrazado() {
        return conRelleno(new byte[] {'M', 'Z', (byte) 0x90, 0});
    }

    private static byte[] conRelleno(byte[] firma) {
        var contenido = Arrays.copyOf(firma, 64);
        Arrays.fill(contenido, firma.length, contenido.length, (byte) 'x');
        return contenido;
    }

    private static void segmentoJpeg(ByteArrayOutputStream salida, int marcador, byte[] datos) {
        var longitud = datos.length + 2;
        escribir(salida, 0xFF, marcador, longitud >> 8, longitud & 0xFF);
        escribir(salida, datos);
    }

    private static void bloquePng(ByteArrayOutputStream salida, String tipo, byte[] datos) {
        escribir(
                salida,
                datos.length >> 24,
                (datos.length >> 16) & 0xFF,
                (datos.length >> 8) & 0xFF,
                datos.length & 0xFF);
        escribir(salida, tipo.getBytes(StandardCharsets.US_ASCII));
        escribir(salida, datos);
        escribir(salida, 0, 0, 0, 0); // CRC: no se verifica al limpiar
    }

    private static void bloqueWebp(ByteArrayOutputStream salida, String tipo, byte[] datos) {
        escribir(salida, tipo.getBytes(StandardCharsets.US_ASCII));
        enteroLittleEndian(salida, datos.length);
        escribir(salida, datos);
        if (datos.length % 2 == 1) {
            salida.write(0);
        }
    }

    private static void enteroLittleEndian(ByteArrayOutputStream salida, int valor) {
        escribir(salida, valor & 0xFF, (valor >> 8) & 0xFF, (valor >> 16) & 0xFF, (valor >> 24) & 0xFF);
    }

    private static void escribir(ByteArrayOutputStream salida, int... bytes) {
        for (var unByte : bytes) {
            salida.write(unByte);
        }
    }

    private static void escribir(ByteArrayOutputStream salida, byte[] bytes) {
        salida.write(bytes, 0, bytes.length);
    }
}
