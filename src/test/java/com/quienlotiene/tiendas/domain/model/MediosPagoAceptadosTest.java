package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-012")
@DisplayName("HU-012 Medios de pago de la tienda")
class MediosPagoAceptadosTest {

    @Test
    @Tag("CP-TIE-012-01")
    @DisplayName("CP-TIE-012-01 Una tienda que acepta efectivo y transferencia no acepta datáfono")
    void soloAceptaLosMediosActivos() {
        var medios = MediosPagoAceptados.de(List.of(MedioPago.TRANSFERENCIA, MedioPago.EFECTIVO));

        assertThat(medios.acepta(MedioPago.EFECTIVO)).isTrue();
        assertThat(medios.acepta(MedioPago.TRANSFERENCIA)).isTrue();
        assertThat(medios.acepta(MedioPago.DATAFONO)).isFalse();
        assertThat(medios.medios()).containsExactly(MedioPago.EFECTIVO, MedioPago.TRANSFERENCIA);
    }

    @Test
    @Tag("CP-TIE-012-02")
    @DisplayName("CP-TIE-012-02 Al desactivar el datáfono deja de aceptarse")
    void desactivaUnMedio() {
        var antes = MediosPagoAceptados.de(List.of(MedioPago.EFECTIVO, MedioPago.DATAFONO));
        var despues = MediosPagoAceptados.de(List.of(MedioPago.EFECTIVO));

        assertThat(antes.acepta(MedioPago.DATAFONO)).isTrue();
        assertThat(despues.acepta(MedioPago.DATAFONO)).isFalse();
    }

    @Test
    @DisplayName("Exige al menos un medio de pago (CU-18 paso 4)")
    void exigeAlMenosUno() {
        assertThatThrownBy(() -> MediosPagoAceptados.de(List.of()))
                .isInstanceOf(MediosPagoInvalidosException.class)
                .hasMessageContaining("al menos un medio")
                .extracting("codigo")
                .isEqualTo("TIE-011");
        assertThatThrownBy(() -> MediosPagoAceptados.de(null)).isInstanceOf(MediosPagoInvalidosException.class);
        assertThat(MediosPagoAceptados.sinConfigurar().medios()).isEmpty();
    }
}
