package com.quienlotiene.tiendas.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Formatos permitidos para los documentos (RNF-012). El tipo se reconoce por los primeros bytes del contenido, no por
 * la extensión ni por lo que diga el navegador, que el usuario puede cambiar.
 */
public enum TipoArchivo {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp"),
    PDF("pdf", "application/pdf");

    private static final byte[] FIRMA_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] FIRMA_RIFF = {'R', 'I', 'F', 'F'};
    private static final byte[] FIRMA_WEBP = {'W', 'E', 'B', 'P'};
    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F', '-'};
    private static final int POSICION_FIRMA_WEBP = 8;

    private final String extension;
    private final String tipoContenido;

    TipoArchivo(String extension, String tipoContenido) {
        this.extension = extension;
        this.tipoContenido = tipoContenido;
    }

    public static Optional<TipoArchivo> detectar(byte[] contenido) {
        if (empiezaCon(contenido, 0, FIRMA_JPEG)) {
            return Optional.of(JPEG);
        }
        if (empiezaCon(contenido, 0, FIRMA_PNG)) {
            return Optional.of(PNG);
        }
        if (empiezaCon(contenido, 0, FIRMA_RIFF) && empiezaCon(contenido, POSICION_FIRMA_WEBP, FIRMA_WEBP)) {
            return Optional.of(WEBP);
        }
        if (empiezaCon(contenido, 0, FIRMA_PDF)) {
            return Optional.of(PDF);
        }
        return Optional.empty();
    }

    private static boolean empiezaCon(byte[] contenido, int desde, byte[] firma) {
        return contenido.length >= desde + firma.length
                && Arrays.equals(contenido, desde, desde + firma.length, firma, 0, firma.length);
    }

    public String extension() {
        return extension;
    }

    public String tipoContenido() {
        return tipoContenido;
    }
}
