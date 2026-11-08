package com.quienlotiene.shared.domain;

/**
 * Guarda archivos fuera de la base (DECISIONES-BACKEND, sección 12): documentos de las tiendas y, más adelante, fotos.
 * Los nombres los genera el almacén; nunca se usa el nombre que mandó el usuario. Los archivos no se sirven por una
 * ruta pública.
 */
public interface AlmacenArchivos {

    /**
     * @param carpeta carpeta lógica, por ejemplo {@code tiendas/<id>}; solo letras minúsculas, dígitos, guiones y "/"
     * @param extension extensión sin punto, por ejemplo {@code pdf}
     * @return ruta relativa con la que se vuelve a leer o borrar el archivo
     */
    String guardar(String carpeta, String extension, byte[] contenido);

    byte[] leer(String ruta);

    /** Borra el archivo si existe; no falla si ya no está. */
    void borrar(String ruta);
}
