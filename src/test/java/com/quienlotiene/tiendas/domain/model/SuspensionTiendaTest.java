package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-037")
@DisplayName("HU-037 Suspender y reactivar una tienda")
class SuspensionTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");

    private static Tienda tiendaEn(EstadoTienda estado) {
        return Tienda.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Droguería La 5",
                "3001234567",
                HorarioAtencion.sinConfigurar(),
                new Ubicacion(2.4448, -76.6147),
                estado,
                estado == EstadoTienda.APROBADA,
                AHORA,
                null,
                AHORA);
    }

    @Test
    @Tag("CP-ADM-037-01")
    @DisplayName("CP-ADM-037-01 Una tienda suspendida queda cerrada y no recibe solicitudes")
    void suspendida() {
        var tienda = tiendaEn(EstadoTienda.APROBADA);

        tienda.suspender(AHORA);

        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.SUSPENDIDA);
        assertThat(tienda.estaAbierta()).isFalse();
        assertThat(tienda.puedeRecibirSolicitudes(AHORA)).isFalse();

        tienda.reactivar(AHORA);
        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.APROBADA);
    }

    @Test
    @DisplayName("Solo se suspende una APROBADA y solo se reactiva una SUSPENDIDA (TIE-004)")
    void transicionesInvalidas() {
        assertThatThrownBy(() -> tiendaEn(EstadoTienda.PENDIENTE).suspender(AHORA))
                .hasFieldOrPropertyWithValue("codigo", "TIE-004");
        assertThatThrownBy(() -> tiendaEn(EstadoTienda.APROBADA).reactivar(AHORA))
                .hasFieldOrPropertyWithValue("codigo", "TIE-004");
    }
}
