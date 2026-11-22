package com.quienlotiene.solicitudes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.solicitudes.application.dto.PublicarSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.SolicitudPublicada;
import com.quienlotiene.solicitudes.domain.model.EstadoSolicitud;
import com.quienlotiene.solicitudes.domain.model.Modalidad;
import com.quienlotiene.solicitudes.domain.model.Solicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudIncompletaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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

@ExtendWith(MockitoExtension.class)
@Tag("HU-008")
@DisplayName("HU-008 Consultar historial y repetir")
class ConsultarHistorialTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID FARMACIAS = UUID.randomUUID();

    @Mock
    SolicitudRepository solicitudRepository;

    @Mock
    RespuestaRepository respuestaRepository;

    @Mock
    PublicarSolicitud publicarSolicitud;

    ConsultarHistorial historial;

    @BeforeEach
    void configurar() {
        historial = new ConsultarHistorial(
                solicitudRepository, respuestaRepository, publicarSolicitud, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static Solicitud solicitud(EstadoSolicitud estado, Instant publicada) {
        return Solicitud.reconstituir(
                UUID.randomUUID(),
                CLIENTE,
                FARMACIAS,
                "Paracetamol 500 mg",
                1,
                Modalidad.RECOGER,
                true,
                new Ubicacion(2.4448, -76.6147),
                3_000,
                false,
                false,
                estado,
                publicada,
                publicada.plus(Duration.ofMinutes(30)));
    }

    @Test
    @Tag("CP-CLI-008-01")
    @DisplayName("CP-CLI-008-01 Repetir «Paracetamol 500 mg, 1 caja» publica otra igual en la ubicación de ahora")
    void repite() {
        // Dado
        var anterior = solicitud(EstadoSolicitud.RESUELTA, AHORA.minus(Duration.ofDays(3)));
        when(solicitudRepository.buscarPorId(anterior.getId())).thenReturn(Optional.of(anterior));
        var nueva = new SolicitudPublicada(
                UUID.randomUUID(),
                FARMACIAS,
                "Paracetamol 500 mg",
                1,
                "RECOGER",
                true,
                2.45,
                -76.61,
                3_000,
                "ABIERTA",
                AHORA,
                AHORA.plusSeconds(1800),
                4,
                false);
        when(publicarSolicitud.ejecutar(any())).thenReturn(nueva);
        // Cuando
        var resultado = historial.repetir(CLIENTE, anterior.getId(), 2.45, -76.61);
        // Entonces
        assertThat(resultado).isEqualTo(nueva);
        var comando = ArgumentCaptor.forClass(PublicarSolicitudComando.class);
        verify(publicarSolicitud).ejecutar(comando.capture());
        assertThat(comando.getValue())
                .isEqualTo(new PublicarSolicitudComando(
                        CLIENTE, FARMACIAS, "Paracetamol 500 mg", 1, "RECOGER", true, 2.45, -76.61));
    }

    @Test
    @DisplayName("Repetir una solicitud de otro cliente responde SOL-003 y no publica nada")
    void noRepiteAjena() {
        var ajena = solicitud(EstadoSolicitud.VENCIDA, AHORA);
        when(solicitudRepository.buscarPorId(ajena.getId())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> historial.repetir(UUID.randomUUID(), ajena.getId(), 2.4, -76.6))
                .isInstanceOf(SolicitudNoEncontradaException.class);
        verify(publicarSolicitud, never()).ejecutar(any());
    }

    @Test
    @Tag("CP-CLI-008-02")
    @DisplayName("CP-CLI-008-02 Un cliente sin solicitudes recibe el historial vacío")
    void historialVacio() {
        when(solicitudRepository.listarDelCliente(CLIENTE, 0, 21)).thenReturn(List.of());
        when(respuestaRepository.contarPorSolicitud(List.of())).thenReturn(Map.of());
        when(respuestaRepository.ordenesPorSolicitud(List.of())).thenReturn(Map.of());

        var resultado = historial.listar(CLIENTE, 0, 20);

        assertThat(resultado.solicitudes()).isEmpty();
        assertThat(resultado.hayMas()).isFalse();
    }

    @Test
    @Tag("SC-018")
    @Tag("CP-CLI-008-03")
    @DisplayName(
            "CP-CLI-008-03 «Mis preguntas»: marca como abierta solo la que no ha vencido, con sus respuestas y la orden de la resuelta")
    void misPreguntas() {
        var abierta = solicitud(EstadoSolicitud.ABIERTA, AHORA.minus(Duration.ofMinutes(10)));
        var sinVencerAun = solicitud(EstadoSolicitud.ABIERTA, AHORA.minus(Duration.ofHours(2)));
        var resuelta = solicitud(EstadoSolicitud.RESUELTA, AHORA.minus(Duration.ofDays(1)));
        var orden = UUID.randomUUID();
        when(solicitudRepository.listarDelCliente(CLIENTE, 0, 3)).thenReturn(List.of(abierta, sinVencerAun, resuelta));
        when(respuestaRepository.contarPorSolicitud(List.of(abierta.getId(), sinVencerAun.getId())))
                .thenReturn(Map.of(abierta.getId(), 3));
        when(respuestaRepository.ordenesPorSolicitud(List.of(abierta.getId(), sinVencerAun.getId())))
                .thenReturn(Map.of());

        var pagina = historial.listar(CLIENTE, 0, 2);

        assertThat(pagina.hayMas()).isTrue();
        assertThat(pagina.solicitudes()).hasSize(2);
        assertThat(pagina.solicitudes().get(0).estaAbierta()).isTrue();
        assertThat(pagina.solicitudes().get(0).respuestasRecibidas()).isEqualTo(3);
        // Sigue ABIERTA en la base porque la tarea de vencimiento no ha pasado, pero ya venció
        assertThat(pagina.solicitudes().get(1).estaAbierta()).isFalse();
        assertThat(pagina.solicitudes().get(1).respuestasRecibidas()).isZero();

        when(solicitudRepository.listarDelCliente(CLIENTE, 1, 3)).thenReturn(List.of(resuelta));
        when(respuestaRepository.contarPorSolicitud(List.of(resuelta.getId()))).thenReturn(Map.of());
        when(respuestaRepository.ordenesPorSolicitud(List.of(resuelta.getId())))
                .thenReturn(Map.of(resuelta.getId(), orden));
        assertThat(historial.listar(CLIENTE, 1, 2).solicitudes().get(0).ordenId())
                .isEqualTo(orden);
    }

    @Test
    @DisplayName("Una página negativa o un tamaño fuera de 1 a 50 responde SOL-002")
    void validaLaPagina() {
        assertThatThrownBy(() -> historial.listar(CLIENTE, -1, 20)).isInstanceOf(SolicitudIncompletaException.class);
        assertThatThrownBy(() -> historial.listar(CLIENTE, 0, 51)).isInstanceOf(SolicitudIncompletaException.class);
        assertThatThrownBy(() -> historial.listar(CLIENTE, 0, 0)).isInstanceOf(SolicitudIncompletaException.class);
    }
}
