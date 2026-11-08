package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-010")
@DisplayName("HU-010 Domicilio con tarifa fija")
class ConfiguracionDomicilioTest {

    @Test
    @Tag("CP-TIE-010-01")
    @DisplayName(
            "CP-TIE-010-01 Con tarifa fija de 5.000 COP el domicilio cuesta 5.000 sin importar subtotal o distancia")
    void cobraLaTarifaFija() {
        // Dado
        var domicilio = ConfiguracionDomicilio.conDomicilio(3_000, new TarifaDomicilio.TarifaFija(5_000));

        // Cuando / Entonces
        assertThat(domicilio.calcularCostoEnPesos(12_000, 800)).isEqualTo(5_000);
        assertThat(domicilio.calcularCostoEnPesos(250_000, 3_000)).isEqualTo(5_000);
        assertThat(domicilio.haceDomicilio()).isTrue();
        assertThat(domicilio.tarifa()).get().extracting(TarifaDomicilio::tipo).isEqualTo("FIJA");
    }

    @Test
    @Tag("CP-TIE-010-02")
    @DisplayName("CP-TIE-010-02 Rechaza una tarifa negativa")
    void rechazaUnaTarifaNegativa() {
        assertThatThrownBy(() -> new TarifaDomicilio.TarifaFija(-1))
                .isInstanceOf(DomicilioInvalidoException.class)
                .hasMessageContaining("negativa")
                .extracting("codigo")
                .isEqualTo("TIE-009");
        assertThat(new TarifaDomicilio.TarifaFija(0).valorEnPesos()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {499, 10_001, 0})
    @DisplayName("El radio de cobertura va de 500 m a 10 km (valores límite)")
    void validaElRadio(int radioInvalido) {
        assertThatThrownBy(
                        () -> ConfiguracionDomicilio.conDomicilio(radioInvalido, new TarifaDomicilio.TarifaFija(5_000)))
                .isInstanceOf(DomicilioInvalidoException.class)
                .hasMessageContaining("radio");
        assertThat(ConfiguracionDomicilio.conDomicilio(500, new TarifaDomicilio.TarifaFija(1))
                        .radioCoberturaEnMetros())
                .isEqualTo(500);
        assertThatThrownBy(() -> ConfiguracionDomicilio.conDomicilio(3_000, null))
                .isInstanceOf(DomicilioInvalidoException.class);
    }

    @Test
    @DisplayName("Fuera del radio o sin domicilio no hay costo: TIE-010")
    void rechazaFueraDeCobertura() {
        var domicilio = ConfiguracionDomicilio.conDomicilio(3_000, new TarifaDomicilio.TarifaFija(5_000));

        assertThat(domicilio.cubre(3_000)).isTrue();
        assertThat(domicilio.cubre(3_001)).isFalse();
        assertThatThrownBy(() -> domicilio.calcularCostoEnPesos(10_000, 3_001))
                .isInstanceOf(FueraDeCoberturaException.class)
                .extracting("codigo")
                .isEqualTo("TIE-010");
        var sinDomicilio = ConfiguracionDomicilio.sinDomicilio();
        assertThat(sinDomicilio.haceDomicilio()).isFalse();
        assertThat(sinDomicilio.tarifa()).isEmpty();
        assertThatThrownBy(() -> sinDomicilio.calcularCostoEnPesos(10_000, 100))
                .isInstanceOf(FueraDeCoberturaException.class);
    }
}
