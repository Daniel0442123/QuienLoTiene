package com.quienlotiene.notificaciones.infrastructure.push;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

/** Conversión entre las claves P-256 en bytes de Web Push (RFC 8291 y 8292) y las clases de Java. */
final class ClavesP256 {

    static final int BYTES_COORDENADA = 32;
    static final int BYTES_PUNTO = 1 + 2 * BYTES_COORDENADA;
    private static final ECParameterSpec CURVA = curva();

    private ClavesP256() {}

    static ECPublicKey publica(byte[] puntoSinComprimir) throws GeneralSecurityException {
        if (puntoSinComprimir.length != BYTES_PUNTO || puntoSinComprimir[0] != 0x04) {
            throw new GeneralSecurityException("La clave pública no es un punto P-256 sin comprimir");
        }
        var x = new BigInteger(1, Arrays.copyOfRange(puntoSinComprimir, 1, 1 + BYTES_COORDENADA));
        var y = new BigInteger(1, Arrays.copyOfRange(puntoSinComprimir, 1 + BYTES_COORDENADA, BYTES_PUNTO));
        return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(new ECPoint(x, y), CURVA));
    }

    static ECPrivateKey privada(byte[] escalar) throws GeneralSecurityException {
        if (escalar.length != BYTES_COORDENADA) {
            throw new GeneralSecurityException("La clave privada no tiene 32 bytes");
        }
        return (ECPrivateKey)
                KeyFactory.getInstance("EC").generatePrivate(new ECPrivateKeySpec(new BigInteger(1, escalar), CURVA));
    }

    static byte[] aBytes(ECPublicKey clave) {
        var punto = new byte[BYTES_PUNTO];
        punto[0] = 0x04;
        copiarCoordenada(clave.getW().getAffineX(), punto, 1);
        copiarCoordenada(clave.getW().getAffineY(), punto, 1 + BYTES_COORDENADA);
        return punto;
    }

    static ECParameterSpec curva() {
        try {
            var parametros = AlgorithmParameters.getInstance("EC");
            parametros.init(new ECGenParameterSpec("secp256r1"));
            return parametros.getParameterSpec(ECParameterSpec.class);
        } catch (GeneralSecurityException sinCurva) {
            throw new IllegalStateException("La JVM no tiene la curva P-256", sinCurva);
        }
    }

    private static void copiarCoordenada(BigInteger valor, byte[] destino, int desde) {
        var bytes = valor.toByteArray();
        var inicio = Math.max(0, bytes.length - BYTES_COORDENADA);
        var largo = bytes.length - inicio;
        System.arraycopy(bytes, inicio, destino, desde + BYTES_COORDENADA - largo, largo);
    }
}
