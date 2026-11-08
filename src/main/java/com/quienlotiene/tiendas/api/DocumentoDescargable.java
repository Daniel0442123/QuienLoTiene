package com.quienlotiene.tiendas.api;

/** Archivo de un documento de verificación, listo para enviarlo al administrador. */
public record DocumentoDescargable(String nombreArchivo, String tipoContenido, byte[] contenido) {

    public DocumentoDescargable {
        contenido = contenido.clone();
    }

    @Override
    public byte[] contenido() {
        return contenido.clone();
    }

    @Override
    public String toString() {
        return "DocumentoDescargable[" + nombreArchivo + ", " + contenido.length + " bytes]";
    }
}
