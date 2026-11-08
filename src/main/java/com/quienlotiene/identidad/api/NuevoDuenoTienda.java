package com.quienlotiene.identidad.api;

/** Datos del dueño de una tienda nueva (HU-002). */
public record NuevoDuenoTienda(
        String nombre,
        String correo,
        String telefono,
        String fraseContrasena,
        boolean aceptaPoliticaDatos,
        String versionPoliticaDatos) {

    @Override
    public String toString() {
        return "NuevoDuenoTienda[***]";
    }
}
