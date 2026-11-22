package com.quienlotiene.solicitudes.domain.model;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-022")
@DisplayName("HU-022 Ampliar el radio de una solicitud")
class AmpliacionRadioTest {

    @Test
    @Tag("CP-SOL-022-01")
    @DisplayName("CP-SOL-022-01 Una solicitud abierta pasa de 3 km a 5 km una sola vez")
    void ampliaElRadioUnaSolaVez() {
        // Dado
        var solicitud = SolicitudesDePrueba.abierta();

        // Cuando
        var primera = solicitud.ampliarRadio(5_000, AHORA);
        var segunda = solicitud.ampliarRadio(8_000, AHORA);

        // Entonces
        assertThat(primera).isTrue();
        assertThat(segunda).isFalse();
        assertThat(solicitud.getRadioEnMetros()).isEqualTo(5_000);
        assertThat(solicitud.fueAmpliado()).isTrue();
    }

    @Test
    @Tag("CP-SOL-022-02")
    @DisplayName("CP-SOL-022-02 Una solicitud resuelta o vencida no cambia de radio")
    void noAmpliaUnaSolicitudCerrada() {
        var resuelta = SolicitudesDePrueba.abierta();
        resuelta.resolver(AHORA);
        var vencida = SolicitudesDePrueba.abierta();

        assertThat(resuelta.ampliarRadio(5_000, AHORA)).isFalse();
        assertThat(vencida.ampliarRadio(5_000, vencida.getFechaVencimiento().plus(Duration.ofSeconds(1))))
                .isFalse();
        assertThat(resuelta.getRadioEnMetros()).isEqualTo(3_000);
        assertThat(vencida.getRadioEnMetros()).isEqualTo(3_000);
    }

    @Test
    @DisplayName("Un radio igual o menor al actual no es una ampliación")
    void noReduceElRadio() {
        var solicitud = SolicitudesDePrueba.abierta();

        assertThat(solicitud.ampliarRadio(3_000, AHORA)).isFalse();
        assertThat(solicitud.fueAmpliado()).isFalse();
    }
}
