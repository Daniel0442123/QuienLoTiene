package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-001")
@DisplayName("HU-001 Frase de contraseña")
class FraseContrasenaTest {

    @Test
    @Tag("CP-IDE-001-03")
    @Tag("NF-SEG-006")
    @DisplayName("CP-IDE-001-03 Rechaza una frase de 14 caracteres (valor límite inferior - 1)")
    void rechazaUnaFraseDeCatorceCaracteres() {
        assertThatThrownBy(() -> new FraseContrasena("abcdefghijklmn"))
                .isInstanceOf(FraseContrasenaMuyCortaException.class)
                .hasMessageContaining("al menos 15 caracteres")
                .extracting("codigo")
                .isEqualTo("IDE-002");
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Rechaza una frase de 10 caracteres")
    void rechazaUnaFraseDeDiezCaracteres() {
        assertThatThrownBy(() -> new FraseContrasena("corta12345"))
                .isInstanceOf(FraseContrasenaMuyCortaException.class);
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Acepta una frase de exactamente 15 caracteres (valor límite)")
    void aceptaUnaFraseDeQuinceCaracteres() {
        var frase = new FraseContrasena("mango con queso");

        assertThat(frase.valor()).hasSize(15);
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("Cuenta las tildes y los emojis como un solo carácter")
    void cuentaCaracteresRealesNoUnidadesUtf16() {
        // 14 caracteres reales; el emoji ocupa dos unidades UTF-16 y no debe completar los 15
        assertThatThrownBy(() -> new FraseContrasena("mi perro feliz".substring(0, 13) + "🐶"))
                .isInstanceOf(FraseContrasenaMuyCortaException.class);
        assertThat(new FraseContrasena("canción del año").valor()).isNotBlank();
    }

    @Test
    @DisplayName("Rechaza una frase nula")
    void rechazaUnaFraseNula() {
        assertThatThrownBy(() -> new FraseContrasena(null)).isInstanceOf(FraseContrasenaMuyCortaException.class);
    }

    @Test
    @DisplayName("Acepta 128 caracteres y rechaza 129")
    void limitaLaLongitudMaxima() {
        assertThat(new FraseContrasena("ab".repeat(64)).valor()).hasSize(128);
        assertThatThrownBy(() -> new FraseContrasena("ab".repeat(64) + "c"))
                .isInstanceOf(FraseContrasenaMuyLargaException.class)
                .extracting("codigo")
                .isEqualTo("IDE-004");
    }

    @ParameterizedTest
    @Tag("CP-IDE-001-03")
    @ValueSource(strings = {"aaaaaaaaaaaaaaa", "111111111111111111", "               "})
    @DisplayName("CP-IDE-001-03 Rechaza un mismo carácter repetido")
    void rechazaUnMismoCaracterRepetido(String fraseRepetida) {
        assertThatThrownBy(() -> new FraseContrasena(fraseRepetida))
                .isInstanceOf(FraseContrasenaComunException.class)
                .extracting("codigo")
                .isEqualTo("IDE-003");
    }

    @Test
    @DisplayName("No muestra la frase al imprimirla")
    void ocultaLaFraseAlImprimirla() {
        assertThat(new FraseContrasena("mi perro come mango").toString()).doesNotContain("perro");
    }
}
