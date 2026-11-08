package com.quienlotiene.identidad.application.dto;

/** Datos del administrador inicial que se crea al desplegar (HU-004). */
public record RegistrarAdministradorComando(
        String nombre, String correo, String telefono, String fraseContrasena, String secretoTotpBase32) {

    @Override
    public String toString() {
        return "RegistrarAdministradorComando[***]";
    }
}
