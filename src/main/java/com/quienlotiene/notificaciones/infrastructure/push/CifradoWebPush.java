package com.quienlotiene.notificaciones.infrastructure.push;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cifra el contenido de un push con {@code aes128gcm} (RFC 8188) y las claves del navegador (RFC 8291): ECDH con una
 * clave efímera, HKDF-SHA256 con el secreto {@code auth} y AES-128-GCM en un solo registro. Solo usa la JCA de la JDK.
 */
final class CifradoWebPush {

    static final int TAMANO_REGISTRO = 4096;
    private static final int BYTES_SAL = 16;
    private static final int BYTES_LLAVE = 16;
    private static final int BYTES_NONCE = 12;
    private static final int BITS_ETIQUETA = 128;
    private static final byte DELIMITADOR_ULTIMO_REGISTRO = 0x02;

    private final SecureRandom aleatorio;

    CifradoWebPush(SecureRandom aleatorio) {
        this.aleatorio = aleatorio;
    }

    /** Devuelve el cuerpo listo para enviar: cabecera (sal, tamaño de registro, clave efímera) y texto cifrado. */
    byte[] cifrar(byte[] contenido, byte[] clavePublicaNavegador, byte[] secretoNavegador)
            throws GeneralSecurityException {
        var generador = KeyPairGenerator.getInstance("EC");
        generador.initialize(ClavesP256.curva(), aleatorio);
        var sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        return cifrar(contenido, clavePublicaNavegador, secretoNavegador, generador.generateKeyPair(), sal);
    }

    /** Con la clave efímera y la sal dadas; separado para probarlo con el vector del apéndice A de RFC 8291. */
    static byte[] cifrar(
            byte[] contenido, byte[] clavePublicaNavegador, byte[] secretoNavegador, KeyPair efimera, byte[] sal)
            throws GeneralSecurityException {
        var navegador = ClavesP256.publica(clavePublicaNavegador);
        var publicaServidor = ClavesP256.aBytes((ECPublicKey) efimera.getPublic());

        var acuerdo = KeyAgreement.getInstance("ECDH");
        acuerdo.init(efimera.getPrivate());
        acuerdo.doPhase(navegador, true);
        var secretoCompartido = acuerdo.generateSecret();

        // RFC 8291 sección 3.4
        var prkClave = hmac(secretoNavegador, secretoCompartido);
        var infoClave = concatenar(
                "WebPush: info\0".getBytes(StandardCharsets.US_ASCII), clavePublicaNavegador, publicaServidor);
        var ikm = expandir(prkClave, infoClave, 32);

        var prk = hmac(sal, ikm);
        var llave = expandir(prk, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), BYTES_LLAVE);
        var nonce = expandir(prk, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), BYTES_NONCE);

        var cifrador = Cipher.getInstance("AES/GCM/NoPadding");
        cifrador.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(llave, "AES"), new GCMParameterSpec(BITS_ETIQUETA, nonce));
        var conDelimitador = Arrays.copyOf(contenido, contenido.length + 1);
        conDelimitador[contenido.length] = DELIMITADOR_ULTIMO_REGISTRO;
        var cifrado = cifrador.doFinal(conDelimitador);

        return ByteBuffer.allocate(BYTES_SAL + 4 + 1 + publicaServidor.length + cifrado.length)
                .put(sal)
                .putInt(TAMANO_REGISTRO)
                .put((byte) publicaServidor.length)
                .put(publicaServidor)
                .put(cifrado)
                .array();
    }

    static byte[] hmac(byte[] llave, byte[] datos) throws GeneralSecurityException {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(llave, "HmacSHA256"));
        return mac.doFinal(datos);
    }

    /** HKDF-Expand de un solo bloque (todo lo que pide RFC 8291 cabe en 32 bytes). */
    static byte[] expandir(byte[] prk, byte[] info, int largo) throws GeneralSecurityException {
        return Arrays.copyOf(hmac(prk, concatenar(info, new byte[] {0x01})), largo);
    }

    static byte[] concatenar(byte[]... partes) {
        var total = 0;
        for (var parte : partes) {
            total += parte.length;
        }
        var buffer = ByteBuffer.allocate(total);
        for (var parte : partes) {
            buffer.put(parte);
        }
        return buffer.array();
    }
}
