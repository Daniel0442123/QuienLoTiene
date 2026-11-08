package com.quienlotiene.identidad.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.identidad.domain.model.SecretoTotp;
import com.quienlotiene.identidad.infrastructure.config.PropiedadesIdentidad;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-004")
@DisplayName("HU-004 Verificación TOTP y cifrado del secreto")
class SegundoFactorAdaptadoresTest {

    /** Vector de prueba de RFC 6238 (SHA-1): secreto "12345678901234567890" en Base32. */
    private static final SecretoTotp SECRETO_RFC = new SecretoTotp("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");

    private static SamstevensVerificadorCodigoTotp verificadorEn(long segundosDesdeEpoch) {
        return new SamstevensVerificadorCodigoTotp(
                Clock.fixed(Instant.ofEpochSecond(segundosDesdeEpoch), ZoneOffset.UTC));
    }

    @Test
    @Tag("CP-IDE-004-01")
    @DisplayName("CP-IDE-004-01 Acepta el código del vector de RFC 6238 en su paso de 30 segundos")
    void aceptaElCodigoDeRfc6238() {
        // En t=59 s el código de 8 dígitos es 94287082; los 6 últimos son 287082
        assertThat(verificadorEn(59).esValido(SECRETO_RFC, "287082")).isTrue();
        assertThat(verificadorEn(1111111109).esValido(SECRETO_RFC, "081804")).isTrue();
    }

    @Test
    @Tag("CP-IDE-004-02")
    @DisplayName("CP-IDE-004-02 Rechaza un código vencido (dos pasos después) o incorrecto")
    void rechazaUnCodigoVencidoOIncorrecto() {
        // Un paso después (hasta 30 s de desfase) se tolera; dos pasos después ya venció
        assertThat(verificadorEn(59 + 30).esValido(SECRETO_RFC, "287082")).isTrue();
        assertThat(verificadorEn(59 + 61).esValido(SECRETO_RFC, "287082")).isFalse();
        assertThat(verificadorEn(59).esValido(SECRETO_RFC, "287083")).isFalse();
        assertThat(verificadorEn(59).esValido(SECRETO_RFC, "28708")).isFalse();
        assertThat(verificadorEn(59).esValido(SECRETO_RFC, null)).isFalse();
    }

    @Test
    @DisplayName("Cifra y descifra el secreto con AES-GCM; cada cifrado es distinto")
    void cifraYDescifraElSecreto() {
        // Dado
        var llave = Base64.getEncoder().encodeToString(new byte[32]);
        var cifrador = new AesGcmCifradorSecretos(propiedades(llave));

        // Cuando
        var primerCifrado = cifrador.cifrar("JBSWY3DPEHPK3PXP");
        var segundoCifrado = cifrador.cifrar("JBSWY3DPEHPK3PXP");

        // Entonces
        assertThat(primerCifrado).isNotEqualTo(segundoCifrado).doesNotContain("JBSWY3DPEHPK3PXP");
        assertThat(cifrador.descifrar(primerCifrado)).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    @DisplayName("Detecta un secreto alterado o descifrado con otra llave")
    void detectaUnSecretoAlterado() {
        var cifrador =
                new AesGcmCifradorSecretos(propiedades(Base64.getEncoder().encodeToString(new byte[32])));
        var otraLlave = new byte[32];
        otraLlave[0] = 1;
        var otroCifrador =
                new AesGcmCifradorSecretos(propiedades(Base64.getEncoder().encodeToString(otraLlave)));
        var cifrado = cifrador.cifrar("JBSWY3DPEHPK3PXP");

        assertThatThrownBy(() -> otroCifrador.descifrar(cifrado)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cifrador.descifrar("no-es-base64!")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sin llave o con una llave de otro tamaño explica qué configurar")
    void exigeUnaLlaveDe32Bytes() {
        assertThatThrownBy(() -> new AesGcmCifradorSecretos(propiedades("")).cifrar("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QLT_TOTP_LLAVE_CIFRADO");
        assertThatThrownBy(() -> new AesGcmCifradorSecretos(
                                propiedades(Base64.getEncoder().encodeToString(new byte[16])))
                        .cifrar("x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    private static PropiedadesIdentidad propiedades(String llave) {
        return new PropiedadesIdentidad("1.0", Duration.ofDays(7), llave);
    }
}
