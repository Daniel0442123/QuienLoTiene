package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-001")
@DisplayName("HU-001 Correo")
class CorreoTest {

    @Test
    @Tag("CP-IDE-001-02")
    @DisplayName("Normaliza el correo a minúsculas y sin espacios para que no haya cuentas repetidas")
    void normalizaElCorreo() {
        assertThat(new Correo("  Ana@Correo.COM ")).isEqualTo(new Correo("ana@correo.com"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "ana", "ana@", "@correo.com", "ana@correo", "ana correo@correo.com"})
    @DisplayName("Rechaza un correo sin la forma nombre@dominio.tld")
    void rechazaUnCorreoMalFormado(String correoInvalido) {
        assertThatThrownBy(() -> new Correo(correoInvalido))
                .isInstanceOf(CorreoInvalidoException.class)
                .extracting("codigo")
                .isEqualTo("IDE-005");
    }

    @Test
    @DisplayName("Rechaza un correo de más de 254 caracteres")
    void rechazaUnCorreoMuyLargo() {
        var correoLargo = "a".repeat(250) + "@co.co";

        assertThatThrownBy(() -> new Correo(correoLargo)).isInstanceOf(CorreoInvalidoException.class);
    }

    @Test
    @DisplayName("No muestra el correo al imprimirlo (Ley 1581)")
    void ocultaElCorreoAlImprimirlo() {
        assertThat(new Correo("ana@correo.com").toString()).doesNotContain("ana");
    }
}
