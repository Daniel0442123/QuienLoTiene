package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-005")
@DisplayName("HU-005 Autorización de tratamiento de datos")
class AutorizacionDatosTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final String VERSION_VIGENTE = "1.0";

    @Test
    @Tag("CP-IDE-005-01")
    @DisplayName("CP-IDE-005-01 Sin marcar la casilla no hay autorización")
    void exigeQueElVisitanteAcepteLaPolitica() {
        assertThatThrownBy(() -> AutorizacionDatos.otorgar(
                        UUID.randomUUID(), UUID.randomUUID(), false, VERSION_VIGENTE, VERSION_VIGENTE, AHORA))
                .isInstanceOf(AutorizacionDatosRequeridaException.class)
                .hasMessageContaining("aceptar la política")
                .extracting("codigo")
                .isEqualTo("IDE-006");
    }

    @Test
    @Tag("CP-IDE-005-02")
    @Tag("NF-SEG-014")
    @DisplayName("CP-IDE-005-02 Guarda la versión 1.0 con la fecha y hora de aceptación")
    void guardaVersionYFechaDeAceptacion() {
        // Dado
        var usuarioId = UUID.randomUUID();

        // Cuando
        var autorizacion =
                AutorizacionDatos.otorgar(UUID.randomUUID(), usuarioId, true, " 1.0 ", VERSION_VIGENTE, AHORA);

        // Entonces
        assertThat(autorizacion.getId()).isNotNull();
        assertThat(autorizacion.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(autorizacion.getVersionPolitica()).isEqualTo("1.0");
        assertThat(autorizacion.getFechaAceptacion()).isEqualTo(AHORA);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0.9", "2.0", ""})
    @DisplayName("Rechaza la aceptación de una versión que no es la vigente")
    void rechazaUnaVersionDistintaALaVigente(String versionAceptada) {
        assertThatThrownBy(() -> AutorizacionDatos.otorgar(
                        UUID.randomUUID(), UUID.randomUUID(), true, versionAceptada, VERSION_VIGENTE, AHORA))
                .isInstanceOf(VersionPoliticaDesactualizadaException.class)
                .hasMessageContaining("versión 1.0")
                .extracting("codigo")
                .isEqualTo("IDE-007");
    }
}
