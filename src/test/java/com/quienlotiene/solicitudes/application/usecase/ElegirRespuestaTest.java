package com.quienlotiene.solicitudes.application.usecase;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.solicitudes.api.RespuestaElegidaEvent;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.EstadoSolicitud;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.RespuestaNoElegibleException;
import com.quienlotiene.solicitudes.domain.model.RespuestaNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba;
import com.quienlotiene.solicitudes.domain.model.TipoRespuesta;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
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
@Tag("HU-024")
@DisplayName("HU-024 Elegir una respuesta")
class ElegirRespuestaTest {

    private static final UUID CLIENTE = UUID.randomUUID();

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    RespuestaRepository respuestaRepository;

    @Mock
    EnvioSolicitudRepository envioRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    ElegirRespuesta elegirRespuesta;

    @BeforeEach
    void configurar() {
        elegirRespuesta = new ElegirRespuesta(
                solicitudRepository,
                respuestaRepository,
                envioRepository,
                publicadorEventos,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-02")
    @DisplayName("CP-SOL-021-02 Elegir resuelve la solicitud y avisa a las otras tiendas que no fueron elegidas")
    void eligeYAvisaALasOtras() {
        // Dado
        var solicitud = SolicitudesDePrueba.abierta(CLIENTE, Modalidad.DOMICILIO);
        var elegida =
                SolicitudesDePrueba.si(solicitud, UUID.randomUUID(), 8_000, 2).conCostoDomicilio(5_000L);
        var otra = SolicitudesDePrueba.si(solicitud, UUID.randomUUID(), 9_000, 2);
        var tercera = SolicitudesDePrueba.si(solicitud, UUID.randomUUID(), 9_500, 1);
        var no = SolicitudesDePrueba.no(solicitud, UUID.randomUUID());
        var dueno = UUID.randomUUID();
        when(respuestaRepository.buscarPorId(elegida.getId())).thenReturn(Optional.of(elegida));
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));
        when(respuestaRepository.listarDe(solicitud.getId())).thenReturn(List.of(elegida, otra, tercera, no));
        when(envioRepository.listarDe(solicitud.getId()))
                .thenReturn(List.of(
                        new EnvioSolicitud(UUID.randomUUID(), solicitud.getId(), UUID.randomUUID(), dueno, 300, AHORA),
                        new EnvioSolicitud(UUID.randomUUID(), solicitud.getId(), UUID.randomUUID(), null, 300, AHORA)));
        var orden = UUID.randomUUID();
        // Cuando
        var resultado = elegirRespuesta.elegirRespuesta(CLIENTE, elegida.getId(), orden);
        // Entonces
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.RESUELTA);
        assertThat(elegida.getOrdenId()).isEqualTo(orden);
        verify(solicitudRepository).guardarEstado(solicitud);
        verify(respuestaRepository).guardarEleccion(elegida);
        assertThat(resultado.producto()).isEqualTo("Paracetamol 500 mg");
        assertThat(resultado.cantidad()).isEqualTo(2);
        assertThat(resultado.precioUnitarioEnPesos()).isEqualTo(8_000);
        assertThat(resultado.costoDomicilioCotizadoEnPesos()).isEqualTo(5_000L);
        var evento = ArgumentCaptor.forClass(RespuestaElegidaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().noElegidas())
                .extracting(RespuestaElegidaEvent.RespuestaNoElegida::respuestaId)
                .containsExactly(otra.getId(), tercera.getId());
        assertThat(evento.getValue().duenosQueLaRecibieron()).containsExactly(dueno);
    }

    @Test
    @Tag("CP-SOL-020-03")
    @DisplayName("Con una alternativa, la orden lleva la descripción de la tienda como producto")
    void alternativaUsaLaDescripcion() {
        var solicitud = SolicitudesDePrueba.abierta(CLIENTE, Modalidad.RECOGER);
        var parecido = Respuesta.responder(
                UUID.randomUUID(),
                solicitud,
                UUID.randomUUID(),
                UUID.randomUUID(),
                TipoRespuesta.PARECIDO,
                7_000L,
                1,
                "Acetaminofén genérico",
                null,
                null,
                AHORA);
        when(respuestaRepository.buscarPorId(parecido.getId())).thenReturn(Optional.of(parecido));
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));

        var resultado = elegirRespuesta.elegirRespuesta(CLIENTE, parecido.getId(), UUID.randomUUID());

        assertThat(resultado.producto()).isEqualTo("Acetaminofén genérico");
    }

    @Test
    @DisplayName("Otro cliente no puede elegir; una respuesta NO no se puede elegir")
    void rechazaAjenaYNo() {
        var solicitud = SolicitudesDePrueba.abierta(CLIENTE, Modalidad.RECOGER);
        var no = SolicitudesDePrueba.no(solicitud, UUID.randomUUID());
        when(respuestaRepository.buscarPorId(no.getId())).thenReturn(Optional.of(no));
        when(solicitudRepository.buscarPorId(solicitud.getId())).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> elegirRespuesta.elegirRespuesta(UUID.randomUUID(), no.getId(), UUID.randomUUID()))
                .isInstanceOf(RespuestaNoEncontradaException.class);
        assertThatThrownBy(() -> elegirRespuesta.elegirRespuesta(CLIENTE, no.getId(), UUID.randomUUID()))
                .isInstanceOf(RespuestaNoElegibleException.class);
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.ABIERTA);
        verify(publicadorEventos, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Una respuesta que no existe responde como no encontrada")
    void noExiste() {
        var id = UUID.randomUUID();
        when(respuestaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> elegirRespuesta.elegirRespuesta(CLIENTE, id, UUID.randomUUID()))
                .isInstanceOf(RespuestaNoEncontradaException.class);
    }
}
