package com.quienlotiene.solicitudes.application.usecase;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.api.RespuestaRecibidaEvent;
import com.quienlotiene.solicitudes.application.dto.ResponderSolicitudComando;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.RespuestaDuplicadaException;
import com.quienlotiene.solicitudes.domain.model.RespuestaIncompletaException;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
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
@Tag("HU-019")
@DisplayName("HU-019 Responder solicitud (caso de uso)")
class ResponderSolicitudTest {

    private static final UUID DUENO = UUID.randomUUID();
    private static final DatosTienda TIENDA =
            new DatosTienda(UUID.randomUUID(), DUENO, "Droguería Central", "3001112233");

    @Mock
    DatosTiendasApi datosTiendasApi;

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    RespuestaRepository respuestaRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    ResponderSolicitud responderSolicitud;
    Solicitud solicitud;

    @BeforeEach
    void configurar() {
        responderSolicitud = new ResponderSolicitud(
                datosTiendasApi,
                solicitudRepository,
                envioRepository,
                respuestaRepository,
                publicadorEventos,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        solicitud = SolicitudesDePrueba.abierta();
        when(datosTiendasApi.obtenerTiendaDelDueno(DUENO)).thenReturn(TIENDA);
    }

    private void llegoALaTienda() {
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(envioRepository.buscar(solicitud.getId(), TIENDA.tiendaId()))
                .thenReturn(Optional.of(new EnvioSolicitud(
                        UUID.randomUUID(), solicitud.getId(), TIENDA.tiendaId(), DUENO, 800, AHORA)));
    }

    private ResponderSolicitudComando comando(String tipo, Long precio, Integer unidades) {
        return new ResponderSolicitudComando(DUENO, solicitud.getId(), tipo, precio, unidades, null, 20);
    }

    @Test
    @Tag("CP-SOL-019-01")
    @DisplayName("CP-SOL-019-01 Sí lo tengo guarda la respuesta con el domicilio cotizado y avisa al cliente")
    void respondeSi() {
        // Dado
        llegoALaTienda();
        when(datosTiendasApi.cotizarDomicilio(eq(TIENDA.tiendaId()), anyDouble(), anyDouble(), eq(16_000L)))
                .thenReturn(Optional.of(4_000L));
        // Cuando
        var resultado = responderSolicitud.ejecutar(comando("SI", 8_000L, 2));
        // Entonces
        assertThat(resultado.tipo()).isEqualTo("SI");
        assertThat(resultado.estado()).isEqualTo("ESPERANDO");
        assertThat(resultado.ofreceDomicilio()).isTrue();
        assertThat(resultado.costoDomicilioEnPesos()).isEqualTo(4_000L);
        assertThat(resultado.producto()).isEqualTo("Paracetamol 500 mg");
        var guardada = ArgumentCaptor.forClass(Respuesta.class);
        verify(respuestaRepository).guardarNueva(guardada.capture());
        assertThat(guardada.getValue().getCostoDomicilioEnPesos()).isEqualTo(4_000L);
        var evento = ArgumentCaptor.forClass(RespuestaRecibidaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().clienteId()).isEqualTo(solicitud.getClienteId());
        assertThat(evento.getValue().nombreTienda()).isEqualTo("Droguería Central");
    }

    @Test
    @Tag("CP-SOL-019-03")
    @DisplayName("CP-SOL-019-03 No lo tengo se guarda sin cotizar domicilio")
    void respondeNo() {
        llegoALaTienda();

        var resultado = responderSolicitud.ejecutar(comando("NO", null, null));

        assertThat(resultado.tipo()).isEqualTo("NO");
        assertThat(resultado.ofreceDomicilio()).isFalse();
        verify(datosTiendasApi, never()).cotizarDomicilio(any(), anyDouble(), anyDouble(), anyLong());
        verify(respuestaRepository).guardarNueva(any());
    }

    @Test
    @Tag("CP-SOL-019-04")
    @DisplayName("CP-SOL-019-04 La segunda respuesta de la misma tienda se rechaza")
    void rechazaDuplicada() {
        llegoALaTienda();
        when(respuestaRepository.existe(solicitud.getId(), TIENDA.tiendaId())).thenReturn(true);

        assertThatThrownBy(() -> responderSolicitud.ejecutar(comando("NO", null, null)))
                .isInstanceOf(RespuestaDuplicadaException.class);
        verify(respuestaRepository, never()).guardarNueva(any());
    }

    @Test
    @DisplayName("Una solicitud que no llegó a la tienda responde como no encontrada")
    void noLlegoALaTienda() {
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(envioRepository.buscar(solicitud.getId(), TIENDA.tiendaId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> responderSolicitud.ejecutar(comando("NO", null, null)))
                .isInstanceOf(SolicitudNoEncontradaException.class);
    }

    @Test
    @DisplayName("Un tipo desconocido se rechaza como respuesta incompleta")
    void tipoDesconocido() {
        llegoALaTienda();

        assertThatThrownBy(() -> responderSolicitud.ejecutar(comando("QUIZAS", 1L, 1)))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responderSolicitud.ejecutar(comando(null, 1L, 1)))
                .isInstanceOf(RespuestaIncompletaException.class);
    }
}
