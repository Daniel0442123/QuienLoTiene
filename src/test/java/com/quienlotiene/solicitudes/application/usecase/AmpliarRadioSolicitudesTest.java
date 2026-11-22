package com.quienlotiene.solicitudes.application.usecase;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.solicitudes.api.RadioAmpliadoEvent;
import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.ParametrosSolicitudes;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.TiendaElegible;
import com.quienlotiene.tiendas.api.TiendasApi;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@Tag("HU-022")
@DisplayName("HU-022 Ampliar el radio de las solicitudes sin respuestas")
class AmpliarRadioSolicitudesTest {

    private static final Ubicacion POPAYAN = SolicitudesDePrueba.POPAYAN;

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    TiendasApi tiendasApi;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    AmpliarRadioSolicitudes ampliar;

    @BeforeEach
    void configurar() {
        var parametros = new ParametrosSolicitudes() {
            @Override
            public Duration vigencia() {
                return Duration.ofMinutes(30);
            }

            @Override
            public int maximoPorHora() {
                return 10;
            }

            @Override
            public int radioInicialEnMetros() {
                return 3_000;
            }

            @Override
            public int radioAmpliadoEnMetros() {
                return 5_000;
            }

            @Override
            public Duration esperaParaAmpliar() {
                return Duration.ofMinutes(10);
            }
        };
        ampliar = new AmpliarRadioSolicitudes(
                solicitudRepository,
                new DifundirSolicitud(tiendasApi, envioRepository, publicadorEventos),
                parametros,
                publicadorEventos,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    @Tag("CP-SOL-022-01")
    @DisplayName(
            "CP-SOL-022-01 Sin respuestas a los 10 minutos pasa a 5 km, avisa solo a las tiendas nuevas y al cliente")
    void ampliaYAvisaALasTiendasNuevas() {
        // Dado: la solicitud ya le llegó a la tienda de 1 km; con 5 km entra también la de 4 km
        var solicitud = SolicitudesDePrueba.abierta();
        var aUnKilometro = new TiendaElegible(UUID.randomUUID(), UUID.randomUUID(), "La 5", 1_000);
        var aCuatroKilometros = new TiendaElegible(UUID.randomUUID(), UUID.randomUUID(), "La 9", 4_000);
        when(solicitudRepository.tomarParaAmpliar(
                        AHORA.minus(Duration.ofMinutes(10)), AHORA, AmpliarRadioSolicitudes.LOTE))
                .thenReturn(List.of(solicitud));
        when(envioRepository.listarDe(solicitud.getId()))
                .thenReturn(List.of(new EnvioSolicitud(
                        UUID.randomUUID(),
                        solicitud.getId(),
                        aUnKilometro.tiendaId(),
                        aUnKilometro.duenoId(),
                        1_000,
                        AHORA)));
        when(tiendasApi.buscarElegibles(
                        solicitud.getCategoriaId(), POPAYAN.latitud(), POPAYAN.longitud(), 5_000, AHORA))
                .thenReturn(List.of(aUnKilometro, aCuatroKilometros));

        // Cuando
        var ampliadas = ampliar.ejecutar();

        // Entonces
        assertThat(ampliadas).isEqualTo(1);
        assertThat(solicitud.getRadioEnMetros()).isEqualTo(5_000);
        verify(solicitudRepository).guardarRadio(solicitud);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EnvioSolicitud>> envios = ArgumentCaptor.forClass(List.class);
        verify(envioRepository).guardarTodos(envios.capture());
        assertThat(envios.getValue())
                .extracting(EnvioSolicitud::tiendaId)
                .containsExactly(aCuatroKilometros.tiendaId());
        var eventos = ArgumentCaptor.forClass(Object.class);
        verify(publicadorEventos, times(2)).publishEvent(eventos.capture());
        assertThat(eventos.getAllValues().get(0))
                .isInstanceOfSatisfying(
                        SolicitudDifundidaEvent.class,
                        evento -> assertThat(evento.destinatarios())
                                .extracting(SolicitudDifundidaEvent.Destinatario::duenoId)
                                .containsExactly(aCuatroKilometros.duenoId()));
        assertThat(eventos.getAllValues().get(1))
                .isEqualTo(new RadioAmpliadoEvent(solicitud.getId(), solicitud.getClienteId(), 5_000, 1));
    }

    @Test
    @Tag("CP-SOL-022-01")
    @DisplayName("Si no hay tiendas nuevas en 5 km igual se amplía y se avisa al cliente con 0 tiendas nuevas")
    void ampliaAunqueNoHayaTiendasNuevas() {
        var solicitud = SolicitudesDePrueba.abierta();
        when(solicitudRepository.tomarParaAmpliar(any(), any(), anyInt())).thenReturn(List.of(solicitud));
        when(envioRepository.listarDe(solicitud.getId())).thenReturn(List.of());
        when(tiendasApi.buscarElegibles(any(), anyDouble(), anyDouble(), anyInt(), any()))
                .thenReturn(List.of());

        assertThat(ampliar.ejecutar()).isEqualTo(1);

        verify(envioRepository, never()).guardarTodos(any());
        verify(publicadorEventos)
                .publishEvent(new RadioAmpliadoEvent(solicitud.getId(), solicitud.getClienteId(), 5_000, 0));
    }

    @Test
    @Tag("CP-SOL-022-02")
    @DisplayName("CP-SOL-022-02 Una solicitud que ya se amplió no vuelve a cambiar ni a difundirse")
    void noAmpliaDosVeces() {
        var solicitud = SolicitudesDePrueba.abierta();
        solicitud.ampliarRadio(5_000, AHORA);
        when(solicitudRepository.tomarParaAmpliar(any(), any(), anyInt())).thenReturn(List.of(solicitud));

        assertThat(ampliar.ejecutar()).isZero();

        verify(solicitudRepository, never()).guardarRadio(any());
        verify(publicadorEventos, never()).publishEvent(any());
    }
}
