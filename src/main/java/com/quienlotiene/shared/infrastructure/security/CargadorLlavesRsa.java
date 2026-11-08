package com.quienlotiene.shared.infrastructure.security;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;

/**
 * Lee el par de llaves RSA de la configuración. En producción las dos llaves son obligatorias; en dev, test y demo, si
 * no están, genera un par temporal en memoria para que el equipo pueda arrancar sin configurar nada.
 */
@Slf4j
final class CargadorLlavesRsa {

    private static final int BITS_LLAVE_TEMPORAL = 2048;

    private CargadorLlavesRsa() {}

    static KeyPair cargar(PropiedadesJwt propiedades) {
        var hayLlavePrivada = tieneTexto(propiedades.llavePrivada());
        var hayLlavePublica = tieneTexto(propiedades.llavePublica());
        if (hayLlavePrivada && hayLlavePublica) {
            return new KeyPair(
                    leerLlavePublica(propiedades.llavePublica()), leerLlavePrivada(propiedades.llavePrivada()));
        }
        if (hayLlavePrivada || hayLlavePublica) {
            throw new IllegalStateException(
                    "Configura las dos llaves JWT: QLT_JWT_LLAVE_PRIVADA y QLT_JWT_LLAVE_PUBLICA (solo hay una)");
        }
        if (!propiedades.permitirLlavesTemporales()) {
            throw new IllegalStateException(
                    "Faltan las llaves JWT: define QLT_JWT_LLAVE_PRIVADA y QLT_JWT_LLAVE_PUBLICA (ver .env.ejemplo)");
        }
        log.warn("No hay llaves JWT configuradas: se generan llaves temporales y las sesiones se pierden al reiniciar");
        return generarParTemporal();
    }

    static RSAPrivateKey leerLlavePrivada(String pem) {
        try {
            var especificacion = new PKCS8EncodedKeySpec(decodificarPem(pem));
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(especificacion);
        } catch (GeneralSecurityException | IllegalArgumentException | ClassCastException causa) {
            throw new IllegalStateException("QLT_JWT_LLAVE_PRIVADA no es una llave privada RSA en PEM PKCS#8", causa);
        }
    }

    static RSAPublicKey leerLlavePublica(String pem) {
        try {
            var especificacion = new X509EncodedKeySpec(decodificarPem(pem));
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(especificacion);
        } catch (GeneralSecurityException | IllegalArgumentException | ClassCastException causa) {
            throw new IllegalStateException("QLT_JWT_LLAVE_PUBLICA no es una llave pública RSA en PEM X.509", causa);
        }
    }

    /** Acepta el PEM con o sin encabezados y con saltos de línea reales o escritos como {@code \n} en el .env. */
    private static byte[] decodificarPem(String pem) {
        var contenidoBase64 = pem.replace("\\n", "")
                .replaceAll("-----(BEGIN|END) [A-Z ]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(contenidoBase64);
    }

    private static KeyPair generarParTemporal() {
        try {
            var generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(BITS_LLAVE_TEMPORAL);
            return generador.generateKeyPair();
        } catch (GeneralSecurityException causa) {
            throw new IllegalStateException("No se pudo generar el par de llaves RSA temporal", causa);
        }
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
