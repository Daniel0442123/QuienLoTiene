package com.quienlotiene.solicitudes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-015")
@DisplayName("HU-015 Porcentajes del resumen de la tienda (SC-011 punto 6)")
class ActividadDeTiendaTest {

    @Test
    @Tag("CP-TIE-015-01")
    @DisplayName("CP-TIE-015-01 Con 10 recibidas, 8 respondidas con producto y 3 ganadas: 80 % y 38 %")
    void calculaLosPorcentajesConLaFormulaAprobada() {
        var actividad = new ActividadDeTienda(10, 8, 8, 3);

        assertThat(actividad.porcentajeRespondiste()).isEqualTo(80);
        assertThat(actividad.porcentajeTeEligieron()).isEqualTo(38);
    }

    @Test
    @Tag("CP-TIE-015-01")
    @DisplayName("«Te eligieron» cuenta solo las respuestas con producto, no los «No lo tengo»")
    void teEligieronSinLosNoLoTengo() {
        var actividad = new ActividadDeTienda(10, 8, 4, 3);

        assertThat(actividad.porcentajeRespondiste()).isEqualTo(80);
        assertThat(actividad.porcentajeTeEligieron()).isEqualTo(75);
    }

    @Test
    @Tag("CP-TIE-015-02")
    @DisplayName("CP-TIE-015-02 Una tienda nueva queda en cero y sin porcentajes («—»)")
    void tiendaNuevaSinPorcentajes() {
        var actividad = ActividadDeTienda.SIN_ACTIVIDAD;

        assertThat(actividad.porcentajeRespondiste()).isNull();
        assertThat(actividad.porcentajeTeEligieron()).isNull();
    }
}
