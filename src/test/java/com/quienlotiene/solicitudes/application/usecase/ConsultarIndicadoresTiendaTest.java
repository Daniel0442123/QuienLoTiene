package com.quienlotiene.solicitudes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.application.dto.IndicadoresTienda;
import com.quienlotiene.solicitudes.domain.model.ActividadDeTienda;
import com.quienlotiene.solicitudes.domain.port.ActividadDeTiendaRepository;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-015")
@DisplayName("HU-015 Ver resumen de indicadores (caso de uso)")
class ConsultarIndicadoresTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-23T15:00:00Z");
    private static final UUID DUENO = UUID.randomUUID();
    private static final UUID TIENDA = UUID.randomUUID();

    @Mock
    DatosTiendasApi datosTiendasApi;

    @Mock
    ActividadDeTiendaRepository actividadRepository;

    ConsultarIndicadoresTienda consultar;

    @BeforeEach
    void configurar() {
        when(datosTiendasApi.obtenerTiendaDelDueno(DUENO))
                .thenReturn(new DatosTienda(TIENDA, DUENO, "Droguería La 5", "3001234567"));
        consultar = new ConsultarIndicadoresTienda(
                datosTiendasApi, actividadRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    @Tag("CP-TIE-015-01")
    @DisplayName("CP-TIE-015-01 Con 10 recibidas, 8 respondidas y 3 ganadas en 7 días ve exactamente esos conteos")
    void muestraLosConteosDelPeriodo() {
        when(actividadRepository.contarDesde(TIENDA, AHORA.minus(Duration.ofDays(7))))
                .thenReturn(new ActividadDeTienda(10, 8, 8, 3));

        var indicadores = consultar.ejecutar(DUENO, 7);

        assertThat(indicadores).isEqualTo(new IndicadoresTienda(7, 10, 8, 8, 3, 80, 38));
    }

    @Test
    @Tag("CP-TIE-015-02")
    @DisplayName("CP-TIE-015-02 Una tienda nueva ve todo en cero en los últimos 30 días, sin errores")
    void tiendaNuevaEnCero() {
        when(actividadRepository.contarDesde(TIENDA, AHORA.minus(Duration.ofDays(30))))
                .thenReturn(ActividadDeTienda.SIN_ACTIVIDAD);

        var indicadores = consultar.ejecutar(DUENO, 30);

        assertThat(indicadores).isEqualTo(new IndicadoresTienda(30, 0, 0, 0, 0, null, null));
    }
}
