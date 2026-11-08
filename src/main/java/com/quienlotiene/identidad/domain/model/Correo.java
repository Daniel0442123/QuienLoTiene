package com.quienlotiene.identidad.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correo con el que el usuario inicia sesión. Se guarda sin espacios y en minúsculas para que "Ana@Correo.com" y
 * "ana@correo.com" sean la misma cuenta.
 */
public record Correo(String valor) {

    public static final int LONGITUD_MAXIMA = 254;

    /** Forma básica usuario@dominio.tld; la validación completa la hace Bean Validation en el Request. */
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Correo {
        if (valor == null) {
            throw new CorreoInvalidoException();
        }
        valor = valor.strip().toLowerCase(Locale.ROOT);
        if (valor.length() > LONGITUD_MAXIMA || !FORMATO.matcher(valor).matches()) {
            throw new CorreoInvalidoException();
        }
    }

    @Override
    public String toString() {
        // Un correo completo es un dato personal (Ley 1581): no debe salir en logs
        return "Correo[***]";
    }
}
