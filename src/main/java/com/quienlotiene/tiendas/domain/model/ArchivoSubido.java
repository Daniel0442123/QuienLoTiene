package com.quienlotiene.tiendas.domain.model;

/**
 * Documento que llegó en el registro, ya validado: no está vacío, pesa máximo 5 MB y es JPEG, PNG, WebP o PDF de
 * verdad (RNF-012).
 */
public record ArchivoSubido(TipoDocumento tipoDocumento, TipoArchivo tipoArchivo, byte[] contenido) {

    public static final int TAMANO_MAXIMO_EN_BYTES = 5 * 1024 * 1024;

    /**
     * @throws DocumentoFaltanteException si no llegó o llegó vacío (CP-TIE-002-02)
     * @throws ArchivoNoPermitidoException si pesa más de 5 MB o no es un formato permitido
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
        return new ArchivoSubido(tipoDocumento, tipoArchivo, contenido);
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
