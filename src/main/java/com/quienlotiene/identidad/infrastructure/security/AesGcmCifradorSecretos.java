package com.quienlotiene.identidad.infrastructure.security;

import com.quienlotiene.identidad.domain.port.CifradorSecretos;
import com.quienlotiene.identidad.infrastructure.config.PropiedadesIdentidad;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * Cifra los secretos TOTP con AES-256-GCM. La llave sale de {@code QLT_TOTP_LLAVE_CIFRADO} (32 bytes en Base64). El
 * resultado es Base64 de: vector de inicialización (12 bytes) + texto cifrado con su etiqueta de autenticación.
 */
@Component
class AesGcmCifradorSecretos implements CifradorSecretos {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int BYTES_LLAVE = 32;
    private static final int BYTES_VECTOR = 12;
    private static final int BITS_ETIQUETA = 128;
    private static final SecureRandom GENERADOR = new SecureRandom();

    private final String llaveEnBase64;

    AesGcmCifradorSecretos(PropiedadesIdentidad propiedades) {
        this.llaveEnBase64 = propiedades.llaveCifradoTotp();
    }

    @Override
    public String cifrar(String textoPlano) {
        try {
            var vector = new byte[BYTES_VECTOR];
            GENERADOR.nextBytes(vector);
            var cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.ENCRYPT_MODE, llave(), new GCMParameterSpec(BITS_ETIQUETA, vector));
            var cifrado = cifrador.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            var resultado = ByteBuffer.allocate(vector.length + cifrado.length)
                    .put(vector)
                    .put(cifrado)
                    .array();
            return Base64.getEncoder().encodeToString(resultado);
        } catch (GeneralSecurityException causa) {
            throw new IllegalStateException("No se pudo cifrar el secreto", causa);
        }
    }

    @Override
    public String descifrar(String textoCifrado) {
        try {
            var contenido = Base64.getDecoder().decode(textoCifrado);
            var cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(
                    Cipher.DECRYPT_MODE, llave(), new GCMParameterSpec(BITS_ETIQUETA, contenido, 0, BYTES_VECTOR));
            var plano = cifrador.doFinal(contenido, BYTES_VECTOR, contenido.length - BYTES_VECTOR);
            return new String(plano, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException causa) {
            throw new IllegalStateException("No se pudo descifrar el secreto: revisa QLT_TOTP_LLAVE_CIFRADO", causa);
        }
    }

    private SecretKeySpec llave() {
        if (llaveEnBase64 == null || llaveEnBase64.isBlank()) {
            throw new IllegalStateException(
                    "Falta QLT_TOTP_LLAVE_CIFRADO: genérala con 'openssl rand -base64 32' (ver .env.ejemplo)");
        }
        var bytes = Base64.getDecoder().decode(llaveEnBase64.strip());
        if (bytes.length != BYTES_LLAVE) {
            throw new IllegalStateException("QLT_TOTP_LLAVE_CIFRADO debe tener 32 bytes en Base64");
        }
        return new SecretKeySpec(bytes, "AES");
    }
}
