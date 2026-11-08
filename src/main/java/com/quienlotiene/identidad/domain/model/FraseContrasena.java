package com.quienlotiene.identidad.domain.model;

/**
 * Frase de contraseña en texto plano, solo mientras se valida y se calcula su hash (D-15, NIST 800-63B): mínimo 15
 * caracteres, sin reglas de composición, y no puede ser un mismo carácter repetido. Nunca se guarda ni se registra.
 */
public record FraseContrasena(String valor) {

    public static final int LONGITUD_MINIMA = 15;
    public static final int LONGITUD_MAXIMA = 128;

    public FraseContrasena {
        if (valor == null || cantidadCaracteres(valor) < LONGITUD_MINIMA) {
            throw new FraseContrasenaMuyCortaException();
        }
        if (cantidadCaracteres(valor) > LONGITUD_MAXIMA) {
            throw new FraseContrasenaMuyLargaException();
        }
        if (esUnSoloCaracterRepetido(valor)) {
            throw new FraseContrasenaComunException();
        }
    }

    /** Cuenta caracteres reales, no unidades UTF-16: una tilde o un emoji cuentan como uno. */
    private static int cantidadCaracteres(String texto) {
        return texto.codePointCount(0, texto.length());
    }

    private static boolean esUnSoloCaracterRepetido(String texto) {
        return texto.codePoints().distinct().count() == 1;
    }

    @Override
    public String toString() {
        return "FraseContrasena[***]";
    }
}
