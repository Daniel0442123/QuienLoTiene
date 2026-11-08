package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Tag("HU-013")
@DisplayName("HU-013 Abrir o cerrar la tienda")
class AperturaTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");

    private static Tienda tiendaEn(EstadoTienda estado, boolean estaAbierta) {
        return Tienda.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Droguería La 5",
                "3001234567",
                HorarioAtencion.sinConfigurar(),
                new Ubicacion(2.4448, -76.6147),
                estado,
                estaAbierta,
                AHORA,
                null,
                AHORA);
    }

    @Test
    @Tag("CP-TIE-013-01")
    @DisplayName("CP-TIE-013-01 Con el interruptor en Cerrado la tienda no recibe solicitudes")
    void cerradaNoRecibeSolicitudes() {
        // Dado
        var tienda = tiendaEn(EstadoTienda.APROBADA, true);

        // Cuando
        tienda.cambiarApertura(false);

        // Entonces
        assertThat(tienda.estaAbierta()).isFalse();
        assertThat(tienda.puedeRecibirSolicitudes(AHORA)).isFalse();
    }

    @Test
    @Tag("CP-TIE-013-02")
    @DisplayName("CP-TIE-013-02 Con el interruptor en Abierto y dentro de su horario la recibe")
    void abiertaRecibeSolicitudes() {
        var tienda = tiendaEn(EstadoTienda.APROBADA, false);

        tienda.cambiarApertura(true);

        assertThat(tienda.estaAbierta()).isTrue();
        assertThat(tienda.puedeRecibirSolicitudes(AHORA)).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTienda.class, names = "APROBADA", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Una tienda que no está aprobada no puede abrir ni cerrar (CU-19 paso 5)")
    void soloUnaTiendaAprobadaAbre(EstadoTienda estado) {
        var tienda = tiendaEn(estado, false);

        assertThatThrownBy(() -> tienda.cambiarApertura(true))
                .isInstanceOf(TiendaNoAprobadaException.class)
                .hasMessageContaining("aún no está aprobada")
                .extracting("codigo")
                .isEqualTo("TIE-008");
        assertThat(tienda.estaAbierta()).isFalse();
    }
}
