package com.quienlotiene.reputacion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("HU-035 y HU-037 Reporte")
class ReporteTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");

    private static Reporte reporte(MotivoReporte motivo, String descripcion) {
        return Reporte.crear(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                motivo,
                descripcion,
                AHORA);
    }

    @Test
    @Tag("HU-035")
    @Tag("CP-REP-035-01")
    @DisplayName("CP-REP-035-01 «Demora en la entrega» nace ABIERTO sin descripción obligatoria")
    void naceAbierto() {
        var reporte = reporte(MotivoReporte.DEMORA_ENTREGA, "  ");

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.ABIERTO);
        assertThat(reporte.getDescripcion()).isNull();
        assertThat(reporte.getNotaResolucion()).isNull();
    }

    @Test
    @Tag("HU-035")
    @DisplayName("OTRO sin descripción, sin motivo o con más de 1000 caracteres se rechaza (REP-004)")
    void validaDatos() {
        assertThatThrownBy(() -> reporte(MotivoReporte.OTRO, null))
                .isInstanceOf(CalificacionInvalidaException.class)
                .hasFieldOrPropertyWithValue("codigo", "REP-004");
        assertThatThrownBy(() -> reporte(null, "x")).isInstanceOf(CalificacionInvalidaException.class);
        assertThatThrownBy(() -> reporte(MotivoReporte.COBRO_DIFERENTE, "x".repeat(1001)))
                .isInstanceOf(CalificacionInvalidaException.class);
        assertThat(reporte(MotivoReporte.OTRO, " Llegó abierto ").getDescripcion())
                .isEqualTo("Llegó abierto");
    }

    @Test
    @Tag("HU-037")
    @Tag("CP-ADM-037-02")
    @DisplayName("CP-ADM-037-02 Resolver con nota lo deja RESUELTO; resolverlo otra vez responde REP-005")
    void resolver() {
        var reporte = reporte(MotivoReporte.DEMORA_ENTREGA, null);

        reporte.resolver("  Hablé con la tienda  ", AHORA.plusSeconds(60));

        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.RESUELTO);
        assertThat(reporte.getNotaResolucion()).isEqualTo("Hablé con la tienda");
        assertThat(reporte.getFechaResolucion()).isEqualTo(AHORA.plusSeconds(60));
        assertThatThrownBy(() -> reporte.resolver("otra", AHORA))
                .isInstanceOf(ReporteYaResueltoException.class)
                .hasFieldOrPropertyWithValue("codigo", "REP-005");
    }

    @Test
    @Tag("HU-037")
    @DisplayName("Una nota de menos de 3 caracteres no resuelve el reporte")
    void notaCorta() {
        var reporte = reporte(MotivoReporte.DEMORA_ENTREGA, null);

        assertThatThrownBy(() -> reporte.resolver("ok", AHORA)).isInstanceOf(CalificacionInvalidaException.class);
        assertThat(reporte.getEstado()).isEqualTo(EstadoReporte.ABIERTO);
    }
}
