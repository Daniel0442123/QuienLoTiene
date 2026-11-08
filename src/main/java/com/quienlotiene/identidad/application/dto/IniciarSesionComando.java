package com.quienlotiene.identidad.application.dto;

/** Correo y frase con que el usuario intenta iniciar sesión (HU-003). */
public record IniciarSesionComando(String correo, String fraseContrasena) {

    @Override
    public String toString() {
        return "IniciarSesionComando[***]";
    }
}
