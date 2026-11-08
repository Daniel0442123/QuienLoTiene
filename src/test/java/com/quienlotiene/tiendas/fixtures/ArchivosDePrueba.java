package com.quienlotiene.tiendas.fixtures;

import java.util.Arrays;

/** Contenidos mínimos con la firma real de cada formato, para probar la detección por contenido. */
public final class ArchivosDePrueba {

    private ArchivosDePrueba() {}

    public static byte[] unJpeg() {
        return conRelleno(new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0});
    }

    public static byte[] unPng() {
        return conRelleno(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
    }

    public static byte[] unWebp() {
        return conRelleno(new byte[] {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'});
    }

    public static byte[] unPdf() {
        return conRelleno("%PDF-1.7".getBytes());
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
}
