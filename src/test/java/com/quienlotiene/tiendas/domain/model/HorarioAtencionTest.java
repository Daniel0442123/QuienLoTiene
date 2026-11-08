package com.quienlotiene.tiendas.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-009")
@DisplayName("HU-009 Horario de atención y perfil de la tienda")
class HorarioAtencionTest {

    /** Lunes 9 de noviembre de 2026; en Colombia es UTC-5. */
    private static Instant lunesALas(String horaColombia) {
        return Instant.parse("2026-11-09T" + horaColombia + ":00-05:00");
    }

    private static final HorarioAtencion DE_OCHO_A_SEIS_ENTRE_SEMANA = HorarioAtencion.de(List.of(
            new HorarioAtencion.FranjaHoraria(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)),
            new HorarioAtencion.FranjaHoraria(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(18, 0))));

    @Test
    @Tag("CP-TIE-009-01")
    @DisplayName("CP-TIE-009-01 Con horario de 8:00 a 18:00, a las 19:00 la tienda no atiende (valores límite)")
    void respetaElHorarioEnHoraDeColombia() {
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("19:00"))).isFalse();
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("18:00"))).isFalse();
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("17:59"))).isTrue();
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("08:00"))).isTrue();
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("07:59"))).isFalse();
        // Martes no está en el horario: cerrado todo el día
        assertThat(DE_OCHO_A_SEIS_ENTRE_SEMANA.atiendeEn(lunesALas("10:00").plusSeconds(24 * 3600)))
                .isFalse();
    }

    @Test
    @Tag("CP-TIE-009-01")
    @DisplayName("CP-TIE-009-01 Una tienda aprobada y abierta no recibe solicitudes fuera de su horario")
    void laTiendaFueraDeHorarioNoRecibeSolicitudes() {
        var tienda = Tienda.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Droguería La 5",
                "3001234567",
                DE_OCHO_A_SEIS_ENTRE_SEMANA,
                new Ubicacion(2.4448, -76.6147),
                EstadoTienda.APROBADA,
                true,
                lunesALas("07:00"),
                null,
                lunesALas("07:00"));

        assertThat(tienda.puedeRecibirSolicitudes(lunesALas("19:00"))).isFalse();
        assertThat(tienda.puedeRecibirSolicitudes(lunesALas("10:00"))).isTrue();
    }

    @Test
    @DisplayName("Sin horario configurado no hay restricción por hora")
    void sinHorarioNoRestringe() {
        var horario = HorarioAtencion.sinConfigurar();

        assertThat(horario.estaConfigurado()).isFalse();
        assertThat(horario.atiendeEn(lunesALas("03:00"))).isTrue();
        assertThat(horario.franjas()).isEmpty();
    }

    @Test
    @DisplayName("Rechaza una franja que abre después de cerrar, sin horas o un día repetido")
    void rechazaHorariosInvalidos() {
        assertThatThrownBy(() ->
                        new HorarioAtencion.FranjaHoraria(DayOfWeek.MONDAY, LocalTime.of(18, 0), LocalTime.of(8, 0)))
                .isInstanceOf(HorarioInvalidoException.class)
                .hasMessageContaining("lunes")
                .extracting("codigo")
                .isEqualTo("TIE-007");
        assertThatThrownBy(() -> new HorarioAtencion.FranjaHoraria(DayOfWeek.SUNDAY, null, LocalTime.of(8, 0)))
                .isInstanceOf(HorarioInvalidoException.class);
        var lunes = new HorarioAtencion.FranjaHoraria(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
        assertThatThrownBy(() -> HorarioAtencion.de(List.of(lunes, lunes)))
                .isInstanceOf(HorarioInvalidoException.class)
                .hasMessageContaining("dos veces");
    }

    @Test
    @Tag("CP-TIE-009-02")
    @DisplayName("CP-TIE-009-02 Actualiza el nombre, el teléfono y el horario del perfil")
    void actualizaElPerfil() {
        var tienda = Tienda.registrar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "La 5",
                "3001234567",
                new Ubicacion(2.4, -76.6),
                lunesALas("07:00"));

        tienda.actualizarPerfil(" Droguería La Quinta ", " 3109876543 ", DE_OCHO_A_SEIS_ENTRE_SEMANA);

        assertThat(tienda.getNombre()).isEqualTo("Droguería La Quinta");
        assertThat(tienda.getTelefono()).isEqualTo("3109876543");
        assertThat(tienda.getHorario().franjas())
                .extracting(HorarioAtencion.FranjaHoraria::dia)
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        assertThatThrownBy(() -> tienda.actualizarPerfil(" ", "3001234567", null))
                .isInstanceOf(TiendaIncompletaException.class);
        tienda.actualizarPerfil("La 5", "3001234567", null);
        assertThat(tienda.getHorario().estaConfigurado()).isFalse();
    }
}
