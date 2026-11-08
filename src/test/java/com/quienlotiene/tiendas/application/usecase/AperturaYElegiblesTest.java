package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.HorarioAtencion;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaCandidata;
import com.quienlotiene.tiendas.domain.model.TiendaNoAprobadaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-013")
@DisplayName("HU-013 Cambiar apertura y buscar tiendas elegibles")
class AperturaYElegiblesTest {

    /** Lunes 9 de noviembre de 2026, 19:00 en Colombia. */
    private static final Instant LUNES_SIETE_DE_LA_NOCHE = Instant.parse("2026-11-09T19:00:00-05:00");

    private static final UUID FARMACIAS = UUID.randomUUID();

    @Mock
    TiendaRepository tiendaRepository;

    private static Tienda tienda(String nombre, EstadoTienda estado, boolean estaAbierta, HorarioAtencion horario) {
        return Tienda.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                FARMACIAS,
                nombre,
                "3001234567",
                horario,
                new Ubicacion(2.4448, -76.6147),
                estado,
                estaAbierta,
                LUNES_SIETE_DE_LA_NOCHE,
                null,
                LUNES_SIETE_DE_LA_NOCHE);
    }

    @Test
    @Tag("CP-TIE-013-02")
    @DisplayName("CP-TIE-013-02 El dueño abre su tienda aprobada y queda guardado")
    void abreLaTiendaDelDueno() {
        var tienda = tienda("La 5", EstadoTienda.APROBADA, false, HorarioAtencion.sinConfigurar());
        when(tiendaRepository.buscarPorDueno(tienda.getDuenoId())).thenReturn(Optional.of(tienda));

        var miTienda = new CambiarAperturaTienda(tiendaRepository).ejecutar(tienda.getDuenoId(), true);

        assertThat(miTienda.estaAbierta()).isTrue();
        verify(tiendaRepository).guardarApertura(tienda);
    }

    @Test
    @DisplayName("Una tienda pendiente no puede abrir y no se guarda nada")
    void noAbreUnaTiendaPendiente() {
        var tienda = tienda("La 5", EstadoTienda.PENDIENTE, false, HorarioAtencion.sinConfigurar());
        when(tiendaRepository.buscarPorDueno(tienda.getDuenoId())).thenReturn(Optional.of(tienda));
        var cambiarApertura = new CambiarAperturaTienda(tiendaRepository);

        assertThatThrownBy(() -> cambiarApertura.ejecutar(tienda.getDuenoId(), true))
                .isInstanceOf(TiendaNoAprobadaException.class);
        verify(tiendaRepository, never()).guardarApertura(any());
    }

    @Test
    @Tag("CP-TIE-009-01")
    @DisplayName("De las candidatas por radio deja fuera la que a esa hora está fuera de su horario")
    void filtraPorHorario() {
        // Dado: dos farmacias abiertas cerca; una atiende de 8:00 a 18:00 los lunes
        var sinHorario = tienda("Siempre abierta", EstadoTienda.APROBADA, true, HorarioAtencion.sinConfigurar());
        var deOchoASeis = tienda(
                "De 8 a 6",
                EstadoTienda.APROBADA,
                true,
                HorarioAtencion.de(List.of(
                        new HorarioAtencion.FranjaHoraria(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0)))));
        when(tiendaRepository.buscarCandidatasCerca(eq(FARMACIAS), any(), eq(3_000)))
                .thenReturn(List.of(new TiendaCandidata(sinHorario, 812.4), new TiendaCandidata(deOchoASeis, 1500)));

        // Cuando: el cliente publica a las 19:00
        var elegibles = new BuscarTiendasElegibles(tiendaRepository)
                .buscarElegibles(FARMACIAS, 2.4448, -76.6147, 3_000, LUNES_SIETE_DE_LA_NOCHE);

        // Entonces
        assertThat(elegibles).singleElement().satisfies(elegible -> {
            assertThat(elegible.nombre()).isEqualTo("Siempre abierta");
            assertThat(elegible.distanciaEnMetros()).isEqualTo(812);
            assertThat(elegible.duenoId()).isEqualTo(sinHorario.getDuenoId());
        });
    }
}
