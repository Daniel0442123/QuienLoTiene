package com.quienlotiene.identidad.domain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Valor opaco y aleatorio de un token (refresco de sesión o desafío de segundo factor). En la base solo se guarda su
 * hash SHA-256: si alguien copia la tabla no puede usar los tokens. Al ser 256 bits aleatorios no hace falta un hash
 * lento como Argon2id.
 */
public record ValorTokenOpaco(String valor) {

    private static final int BYTES_ALEATORIOS = 32;
    private static final SecureRandom GENERADOR = new SecureRandom();

    public static ValorTokenOpaco generar() {
        var bytes = new byte[BYTES_ALEATORIOS];
        GENERADOR.nextBytes(bytes);
        return new ValorTokenOpaco(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    public String calcularHash() {
        try {
            var resumen = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumen);
        } catch (NoSuchAlgorithmException causa) {
            throw new IllegalStateException("La JVM no tiene SHA-256", causa);
        }
    }

    @Override
    public String toString() {
        return "ValorTokenOpaco[***]";
    }
}
