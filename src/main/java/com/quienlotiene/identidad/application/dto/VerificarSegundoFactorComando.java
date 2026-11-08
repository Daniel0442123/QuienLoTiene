package com.quienlotiene.identidad.application.dto;

/** Token temporal del paso anterior y código de 6 dígitos de la aplicación autenticadora (HU-004). */
public record VerificarSegundoFactorComando(String tokenSegundoFactor, String codigo) {

    @Override
    public String toString() {
        return "VerificarSegundoFactorComando[***]";
    }
}
