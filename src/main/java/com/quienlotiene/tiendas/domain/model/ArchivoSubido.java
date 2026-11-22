package com.quienlotiene.tiendas.domain.model;

/**
 * Documento que llegó en el registro, ya validado: no está vacío, pesa máximo 5 MB, es JPEG, PNG, WebP o PDF de
 * verdad y, si es imagen, ya no trae metadatos EXIF (RNF-012).
 */
public record ArchivoSubido(TipoDocumento tipoDocumento, TipoArchivo tipoArchivo, byte[] contenido) {

    public static final int TAMANO_MAXIMO_EN_BYTES = 5 * 1024 * 1024;

    /**
     * @throws DocumentoFaltanteException si no llegó o llegó vacío (CP-TIE-002-02)
     * @throws ArchivoNoPermitidoException si pesa más de 5 MB, no es un formato permitido o su estructura está dañada
     */
    public static ArchivoSubido validar(TipoDocumento tipoDocumento, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new DocumentoFaltanteException(tipoDocumento);
        }
        if (contenido.length > TAMANO_MAXIMO_EN_BYTES) {
            throw new ArchivoNoPermitidoException(tipoDocumento);
        }
        var tipoArchivo =
                TipoArchivo.detectar(contenido).orElseThrow(() -> new ArchivoNoPermitidoException(tipoDocumento));
        try {
            return new ArchivoSubido(tipoDocumento, tipoArchivo, LimpiadorMetadatos.limpiar(tipoArchivo, contenido));
        } catch (IllegalArgumentException estructuraDanada) {
            throw new ArchivoNoPermitidoException(tipoDocumento);
        }
    }

    /** Copia del contenido: el archivo no comparte su arreglo con nadie. */
    public ArchivoSubido {
        contenido = contenido.clone();
    }

    @Override
    public byte[] contenido() {
        return contenido.clone();
    }

    public int tamanoEnBytes() {
        return contenido.length;
    }

    @Override
    public String toString() {
        return "ArchivoSubido[" + tipoDocumento + ", " + tipoArchivo + ", " + contenido.length + " bytes]";
    }
}
