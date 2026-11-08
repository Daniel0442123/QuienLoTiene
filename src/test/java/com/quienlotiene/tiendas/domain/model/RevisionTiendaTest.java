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

@Tag("HU-036")
@DisplayName("HU-036 Revisión de la tienda")
class RevisionTiendaTest {

    private static final Instant REGISTRO = Instant.parse("2026-11-09T15:30:00Z");
    private static final Instant REVISION = Instant.parse("2026-11-10T09:00:00Z");

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
                false,
                REGISTRO,
                null,
                null);
    }

    @Test
    @Tag("CP-ADM-036-01")
    @DisplayName("CP-ADM-036-01 Una tienda Pendiente queda Aprobada con la fecha de revisión")
    void apruebaUnaTiendaPendiente() {
        // Dado
        var tienda = tiendaEn(EstadoTienda.PENDIENTE);

        // Cuando
        tienda.aprobar(REVISION);

        // Entonces
        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.APROBADA);
        assertThat(tienda.getFechaRevision()).contains(REVISION);
        assertThat(tienda.getMotivoRechazo()).isEmpty();
        // Aprobada pero cerrada: todavía no recibe solicitudes hasta que la abra (HU-013)
        assertThat(tienda.puedeRecibirSolicitudes(REVISION)).isFalse();
    }

    @Test
    @Tag("CP-ADM-036-02")
    @DisplayName("CP-ADM-036-02 Rechazada con el motivo \"Documento ilegible\", que la tienda puede ver")
    void rechazaConMotivo() {
        // Dado
        var tienda = tiendaEn(EstadoTienda.PENDIENTE);

        // Cuando
        tienda.rechazar("  Documento ilegible ", REVISION);

        // Entonces
        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.RECHAZADA);
        assertThat(tienda.getMotivoRechazo()).contains("Documento ilegible");
        assertThat(tienda.getFechaRevision()).contains(REVISION);
    }

    @Test
    @Tag("CP-ADM-036-02")
    @DisplayName("No se puede rechazar sin motivo o con un motivo de más de 300 caracteres")
    void exigeUnMotivoValido() {
        var tienda = tiendaEn(EstadoTienda.PENDIENTE);

        assertThatThrownBy(() -> tienda.rechazar(null, REVISION))
                .isInstanceOf(MotivoRechazoInvalidoException.class)
                .extracting("codigo")
                .isEqualTo("TIE-005");
        assertThatThrownBy(() -> tienda.rechazar("  no ", REVISION)).isInstanceOf(MotivoRechazoInvalidoException.class);
        assertThatThrownBy(() -> tienda.rechazar("x".repeat(301), REVISION))
                .isInstanceOf(MotivoRechazoInvalidoException.class);
        assertThat(tienda.getEstado()).isEqualTo(EstadoTienda.PENDIENTE);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTienda.class, names = "PENDIENTE", mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Solo una tienda Pendiente se aprueba o se rechaza (RNF-016)")
    void rechazaTransicionesInvalidas(EstadoTienda estado) {
        var tienda = tiendaEn(estado);

        assertThatThrownBy(() -> tienda.aprobar(REVISION))
                .isInstanceOf(TransicionTiendaInvalidaException.class)
                .hasMessageContaining(estado.name())
                .extracting("codigo")
                .isEqualTo("TIE-004");
        assertThatThrownBy(() -> tienda.rechazar("Documento ilegible", REVISION))
                .isInstanceOf(TransicionTiendaInvalidaException.class);
        assertThat(tienda.getEstado()).isEqualTo(estado);
    }

    @Test
    @DisplayName("Una tienda aprobada y abierta recibe solicitudes")
    void unaTiendaAprobadaYAbiertaRecibeSolicitudes() {
        var tienda = Tienda.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "La 5",
                "3001234567",
                HorarioAtencion.sinConfigurar(),
                new Ubicacion(2.4, -76.6),
                EstadoTienda.APROBADA,
                true,
                REGISTRO,
                null,
                REVISION);

        assertThat(tienda.puedeRecibirSolicitudes(REVISION)).isTrue();
        assertThat(tienda.estaAbierta()).isTrue();
    }
}
