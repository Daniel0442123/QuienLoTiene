package com.quienlotiene.tiendas.domain.model;

/** Documentos que el administrador revisa antes de aprobar la tienda (HU-002, HU-036). */
public enum TipoDocumento {
    /** Foto de la fachada o del interior del local. */
    FOTO_LOCAL("la foto del local"),
    /** RUT o certificado de existencia del negocio. */
    RUT("el RUT o documento de existencia del negocio");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String descripcion() {
        return descripcion;
    }
}
