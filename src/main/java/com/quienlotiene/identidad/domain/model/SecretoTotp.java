package com.quienlotiene.identidad.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Secreto compartido con la aplicación autenticadora del administrador (Google Authenticator, Authy...), en Base32.
 * Solo vive en memoria mientras se cifra o se verifica un código; nunca se registra.
 */
public record SecretoTotp(String valorBase32) {

    /** 26 caracteres Base32 son 130 bits, por encima del mínimo de 128 de RFC 4226. */
    public static final int LONGITUD_MINIMA = 26;

    private static final Pattern BASE32 = Pattern.compile("[A-Z2-7]+=*");

    public SecretoTotp {
        if (valorBase32 == null) {
            throw new SecretoTotpInvalidoException();
        }
        valorBase32 = valorBase32.replace(" ", "").toUpperCase(Locale.ROOT);
        if (valorBase32.length() < LONGITUD_MINIMA
                || !BASE32.matcher(valorBase32).matches()) {
            throw new SecretoTotpInvalidoException();
        }
    }

    @Override
    public String toString() {
        return "SecretoTotp[***]";
    }
}
