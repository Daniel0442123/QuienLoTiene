package com.quienlotiene.solicitudes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.api.SolicitudVencidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.EstadoSolicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@Tag("HU-021")
@DisplayName("HU-021 Vencer solicitudes sin elección")
class VencerSolicitudesTest {

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    RespuestaRepository respuestaRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    @Test
    @Tag("CP-SOL-021-01")
    @DisplayName("CP-SOL-021-01 Las solicitudes cumplidas pasan a VENCIDA y se avisa al cliente y a las tiendas")
    void venceLasCumplidas() {
        // Dado
        var vencida = SolicitudesDePrueba.abierta();
        var aunAbierta = SolicitudesDePrueba.abierta();
        var ahora = vencida.getFechaVencimiento().plus(Duration.ofSeconds(5));
        aunAbierta.resolver(SolicitudesDePrueba.AHORA);
        var dueno = UUID.randomUUID();
        when(solicitudRepository.tomarVencidas(ahora, VencerSolicitudes.LOTE)).thenReturn(List.of(vencida, aunAbierta));
        when(envioRepository.listarDe(vencida.getId()))
                .thenReturn(List.of(
                        new EnvioSolicitud(UUID.randomUUID(), vencida.getId(), UUID.randomUUID(), dueno, 500, ahora)));
        when(respuestaRepository.listarDe(vencida.getId()))
                .thenReturn(List.of(SolicitudesDePrueba.no(vencida, UUID.randomUUID())));
        var vencer = new VencerSolicitudes(
                solicitudRepository,
                envioRepository,
                respuestaRepository,
                publicadorEventos,
                Clock.fixed(ahora, ZoneOffset.UTC));
        // Cuando
        var cuantas = vencer.ejecutar();
        // Entonces
        assertThat(cuantas).isEqualTo(1);
        assertThat(vencida.getEstado()).isEqualTo(EstadoSolicitud.VENCIDA);
        verify(solicitudRepository).guardarEstado(vencida);
        verify(solicitudRepository, never()).guardarEstado(aunAbierta);
        var evento = ArgumentCaptor.forClass(SolicitudVencidaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().clienteId()).isEqualTo(vencida.getClienteId());
        assertThat(evento.getValue().respuestasRecibidas()).isEqualTo(1);
        assertThat(evento.getValue().duenosQueLaRecibieron()).containsExactly(dueno);
    }

    @Test
    @DisplayName("Sin solicitudes cumplidas no hace nada")
    void sinVencidas() {
        when(solicitudRepository.tomarVencidas(any(), anyInt())).thenReturn(List.of());
        var vencer = new VencerSolicitudes(
                solicitudRepository, envioRepository, respuestaRepository, publicadorEventos, Clock.systemUTC());

        assertThat(vencer.ejecutar()).isZero();
        verify(publicadorEventos, never()).publishEvent(any());
    }
}
