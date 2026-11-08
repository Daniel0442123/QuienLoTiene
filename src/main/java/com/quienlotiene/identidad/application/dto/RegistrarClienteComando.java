package com.quienlotiene.identidad.application.dto;

/** Datos para registrar un cliente (HU-001 y HU-005), ya con la forma validada en el Request. */
public record RegistrarClienteComando(
        String nombre,
        String correo,
        String telefono,
        String fraseContrasena,
        boolean aceptaPoliticaDatos,
        String versionPoliticaDatos) {

    @Override
    public String toString() {
        // Ni la frase ni el correo deben terminar en un log
        return "RegistrarClienteComando[***]";
    }
}
