package com.quienlotiene.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

@Tag("T-02")
@DisplayName("T-02 Llaves RSA y codificación de frases de contraseña")
class CargadorLlavesRsaTest {

    @Test
    @DisplayName("Lee las llaves en PEM de una sola línea, con \\n escritos como texto")
    void leeLasLlavesEnPem() throws Exception {
        // Dado
        var generador = KeyPairGenerator.getInstance("RSA");
        generador.initialize(2048);
        var parOriginal = generador.generateKeyPair();
        var pemPrivada = aPem("PRIVATE KEY", parOriginal.getPrivate().getEncoded());
        var pemPublica = aPem("PUBLIC KEY", parOriginal.getPublic().getEncoded());

        // Cuando
        var parLeido = CargadorLlavesRsa.cargar(propiedades(pemPrivada, pemPublica, false));

        // Entonces
        assertThat(parLeido.getPrivate().getEncoded())
                .isEqualTo(parOriginal.getPrivate().getEncoded());
        assertThat(parLeido.getPublic().getEncoded())
                .isEqualTo(parOriginal.getPublic().getEncoded());
    }

    @Test
    @DisplayName("En producción no arranca sin llaves")
    void exigeLasLlavesEnProduccion() {
        assertThatThrownBy(() -> CargadorLlavesRsa.cargar(propiedades("", null, false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QLT_JWT_LLAVE_PRIVADA");
    }

    @Test
    @DisplayName("No arranca si solo hay una de las dos llaves")
    void exigeLasDosLlaves() {
        assertThatThrownBy(() -> CargadorLlavesRsa.cargar(propiedades("abc", null, true)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("solo hay una");
    }

    @Test
    @DisplayName("Rechaza una llave que no es PEM válido")
    void rechazaUnaLlaveInvalida() {
        assertThatThrownBy(() -> CargadorLlavesRsa.cargar(propiedades("no-es-una-llave", "tampoco", false)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X.509");
        assertThatThrownBy(() -> CargadorLlavesRsa.leerLlavePrivada("bm8tZXMtdW5hLWxsYXZl"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PKCS#8");
    }

    @Test
    @DisplayName("En dev genera llaves temporales si faltan")
    void generaLlavesTemporalesEnDesarrollo() {
        var parTemporal = CargadorLlavesRsa.cargar(propiedades(null, null, true));

        assertThat(parTemporal.getPublic().getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    @DisplayName("NF-SEG-006 Las frases de contraseña se guardan con Argon2id")
    @Tag("NF-SEG-006")
    void codificaLasFrasesConArgon2id() {
        // Dado
        var codificador = (Argon2PasswordEncoder) new SeguridadConfig().codificadorFrasesContrasena();
        var frase = "mi perro come mango los domingos";

        // Cuando
        var hash = codificador.encode(frase);

        // Entonces
        assertThat(hash).startsWith("$argon2id$").contains("m=19456,t=2,p=1");
        assertThat(codificador.matches(frase, hash)).isTrue();
        assertThat(codificador.matches("otra frase cualquiera", hash)).isFalse();
    }

    private static PropiedadesJwt propiedades(String llavePrivada, String llavePublica, boolean permitirTemporales) {
        return new PropiedadesJwt(
                "quienlotiene", Duration.ofMinutes(15), llavePrivada, llavePublica, "llave-1", permitirTemporales);
    }

    private static String aPem(String tipo, byte[] contenido) {
        // Simula el .env: los saltos de línea del PEM quedan escritos como el texto \n
        var base64ConSaltos = Base64.getMimeEncoder(64, new byte[] {'\n'})
                .encodeToString(contenido)
                .replace("\n", "\\n");
        return "-----BEGIN " + tipo + "-----\\n" + base64ConSaltos + "\\n-----END " + tipo + "-----";
    }
}
