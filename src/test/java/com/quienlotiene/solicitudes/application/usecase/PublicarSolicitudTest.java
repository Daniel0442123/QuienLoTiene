package com.quienlotiene.solicitudes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.api.SolicitudPublicadaEvent;
import com.quienlotiene.solicitudes.application.dto.PublicarSolicitudComando;
import com.quienlotiene.solicitudes.domain.model.LimiteSolicitudesSuperadoException;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudIncompletaException;
import com.quienlotiene.solicitudes.domain.port.ParametrosSolicitudes;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
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
@Tag("HU-016")
@DisplayName("HU-016 Publicar solicitud")
class PublicarSolicitudTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID FARMACIAS = UUID.randomUUID();

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    @Mock
    DifundirSolicitud difundirSolicitud;

    PublicarSolicitud publicarSolicitud;

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
        };
        publicarSolicitud = new PublicarSolicitud(
                solicitudRepository,
                parametros,
                publicadorEventos,
                difundirSolicitud,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static PublicarSolicitudComando paracetamol(String modalidad) {
        return new PublicarSolicitudComando(
                CLIENTE, FARMACIAS, "Paracetamol 500 mg", 1, modalidad, false, 2.4448, -76.6147);
    }

    @Test
    @Tag("CP-SOL-016-01")
    @DisplayName("CP-SOL-016-01 Publica \"Paracetamol 500 mg, 1 caja\" en Farmacias para recoger y avisa a la difusión")
    void publicaLaSolicitudYEmiteElEvento() {
        // Dado: el cliente no ha publicado nada en la última hora
        when(solicitudRepository.listarFechasPublicacionDesde(CLIENTE, AHORA.minus(Duration.ofHours(1))))
                .thenReturn(List.of());

        // Cuando
        var resultado = publicarSolicitud.ejecutar(paracetamol("RECOGER"));

        // Entonces
        assertThat(resultado.estado()).isEqualTo("ABIERTA");
        assertThat(resultado.fechaVencimiento()).isEqualTo(AHORA.plus(Duration.ofMinutes(30)));
        assertThat(resultado.radioEnMetros()).isEqualTo(3_000);
        assertThat(resultado.modalidad()).isEqualTo("RECOGER");
        var solicitudGuardada = ArgumentCaptor.forClass(Solicitud.class);
        verify(solicitudRepository).guardarNueva(solicitudGuardada.capture());
        assertThat(solicitudGuardada.getValue().getClienteId()).isEqualTo(CLIENTE);
        var evento = ArgumentCaptor.forClass(SolicitudPublicadaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().solicitudId()).isEqualTo(resultado.id());
        assertThat(evento.getValue().categoriaId()).isEqualTo(FARMACIAS);
        assertThat(evento.getValue().radioEnMetros()).isEqualTo(3_000);
        assertThat(evento.getValue().latitud()).isEqualTo(2.4448);
    }

    @Test
    @Tag("CP-SOL-016-03")
    @DisplayName("CP-SOL-016-03 Con 10 publicadas en la última hora rechaza la siguiente sin guardarla")
    void rechazaLaUndecimaSolicitud() {
        // Dado
        when(solicitudRepository.listarFechasPublicacionDesde(any(), any()))
                .thenReturn(Collections.nCopies(10, AHORA.minus(Duration.ofMinutes(48))));

        // Cuando / Entonces
        assertThatThrownBy(() -> publicarSolicitud.ejecutar(paracetamol("RECOGER")))
                .isInstanceOf(LimiteSolicitudesSuperadoException.class)
                .hasMessageContaining("12 minutos");
        verify(solicitudRepository, never()).guardarNueva(any());
        verify(publicadorEventos, never()).publishEvent(any());
    }

    @Test
    @Tag("CP-SOL-016-02")
    @DisplayName("Una modalidad desconocida o ausente se rechaza")
    void rechazaUnaModalidadDesconocida() {
        when(solicitudRepository.listarFechasPublicacionDesde(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> publicarSolicitud.ejecutar(paracetamol("VOLANDO")))
                .isInstanceOf(SolicitudIncompletaException.class)
                .hasMessageContaining("RECOGER o DOMICILIO");
        assertThatThrownBy(() -> publicarSolicitud.ejecutar(paracetamol(null)))
                .isInstanceOf(SolicitudIncompletaException.class);
        verify(solicitudRepository, never()).guardarNueva(any());
    }
}
