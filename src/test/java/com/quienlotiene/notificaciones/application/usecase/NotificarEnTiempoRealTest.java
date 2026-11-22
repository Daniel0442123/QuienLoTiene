package com.quienlotiene.notificaciones.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.quienlotiene.notificaciones.application.dto.EventosTiempoReal;
import com.quienlotiene.notificaciones.domain.model.Notificacion;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import com.quienlotiene.ordenes.api.EstadoOrdenCambiadoEvent;
import com.quienlotiene.ordenes.api.OrdenCreadaEvent;
import com.quienlotiene.solicitudes.api.RadioAmpliadoEvent;
import com.quienlotiene.solicitudes.api.RespuestaElegidaEvent;
import com.quienlotiene.solicitudes.api.RespuestaRecibidaEvent;
import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.api.SolicitudVencidaEvent;
import java.time.Clock;
import java.time.Instant;
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

@ExtendWith(MockitoExtension.class)
@Tag("HU-030")
@DisplayName("HU-030 Notificar en tiempo real")
class NotificarEnTiempoRealTest {

    private static final Instant AHORA = Instant.parse("2026-11-17T15:00:00Z");
    private static final UUID SOLICITUD = UUID.randomUUID();
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO_A = UUID.randomUUID();
    private static final UUID DUENO_B = UUID.randomUUID();

    @Mock
    CanalTiempoReal canal;

    @Mock
    AvisarPorPush avisarPorPush;

    NotificarEnTiempoReal notificar;

    @BeforeEach
    void configurar() {
        notificar = new NotificarEnTiempoReal(canal, avisarPorPush, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private List<Notificacion> enviadas() {
        var captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(canal, atLeastOnce()).enviar(captor.capture());
        return captor.getAllValues();
    }

    @Test
    @Tag("CP-NOT-030-01")
    @Tag("CP-NOT-030-02")
    @DisplayName("CP-NOT-030-01 Cada tienda destinataria recibe solicitud-nueva con su distancia y la marca de urgente")
    void solicitudNueva() {
        // Dado
        var evento = new SolicitudDifundidaEvent(
                SOLICITUD,
                "Paracetamol",
                2,
                "DOMICILIO",
                true,
                AHORA.plusSeconds(1_800),
                3_000,
                List.of(
                        new SolicitudDifundidaEvent.Destinatario(UUID.randomUUID(), DUENO_A, 400),
                        new SolicitudDifundidaEvent.Destinatario(UUID.randomUUID(), null, 900),
                        new SolicitudDifundidaEvent.Destinatario(UUID.randomUUID(), DUENO_B, 1_200)));
        // Cuando
        notificar.alDifundirSolicitud(evento);
        // Entonces
        var enviadas = enviadas();
        assertThat(enviadas).extracting(Notificacion::usuarioId).containsExactly(DUENO_A, DUENO_B);
        assertThat(enviadas).allSatisfy(aviso -> {
            assertThat(aviso.tipo()).isEqualTo(EventosTiempoReal.SolicitudNueva.TIPO);
            assertThat(aviso.fecha()).isEqualTo(AHORA);
            assertThat(aviso.id()).isNotBlank();
        });
        var datos = (EventosTiempoReal.SolicitudNueva) enviadas.get(1).datos();
        assertThat(datos.esUrgente()).isTrue();
        assertThat(datos.distanciaEnMetros()).isEqualTo(1_200);
    }

    @Test
    @Tag("CP-SOL-020-01")
    @DisplayName("CP-SOL-020-01 El cliente recibe respuesta-recibida por SI y PARECIDO, no por NO")
    void respuestaRecibida() {
        notificar.alRecibirRespuesta(new RespuestaRecibidaEvent(SOLICITUD, UUID.randomUUID(), CLIENTE, "NO", "T"));
        verifyNoInteractions(canal);

        notificar.alRecibirRespuesta(new RespuestaRecibidaEvent(SOLICITUD, UUID.randomUUID(), CLIENTE, "SI", "T"));

        assertThat(enviadas()).singleElement().satisfies(aviso -> {
            assertThat(aviso.usuarioId()).isEqualTo(CLIENTE);
            assertThat(aviso.tipo()).isEqualTo("respuesta-recibida");
        });
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-02")
    @DisplayName("CP-SOL-021-02 Al elegir se cierra la solicitud para todas y las no elegidas reciben su aviso")
    void respuestaElegida() {
        var noElegida = UUID.randomUUID();
        notificar.alElegirRespuesta(new RespuestaElegidaEvent(
                SOLICITUD,
                UUID.randomUUID(),
                UUID.randomUUID(),
                CLIENTE,
                List.of(DUENO_A, DUENO_B),
                List.of(new RespuestaElegidaEvent.RespuestaNoElegida(noElegida, DUENO_B))));

        assertThat(enviadas())
                .extracting(Notificacion::usuarioId, Notificacion::tipo)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(DUENO_A, "solicitud-cerrada"),
                        org.assertj.core.groups.Tuple.tuple(DUENO_B, "solicitud-cerrada"),
                        org.assertj.core.groups.Tuple.tuple(DUENO_B, "respuesta-no-elegida"));
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-01")
    @DisplayName("CP-SOL-021-01 Al vencer se avisa al cliente y se cierra la solicitud para las tiendas")
    void solicitudVencida() {
        notificar.alVencerSolicitud(new SolicitudVencidaEvent(SOLICITUD, CLIENTE, 0, List.of(DUENO_A)));

        var enviadas = enviadas();
        assertThat(enviadas).extracting(Notificacion::tipo).containsExactly("solicitud-vencida", "solicitud-cerrada");
        assertThat(((EventosTiempoReal.SolicitudCerrada) enviadas.get(1).datos()).estado())
                .isEqualTo("VENCIDA");
    }

    @Test
    @Tag("HU-024")
    @Tag("HU-027")
    @DisplayName("La tienda elegida recibe orden-creada y ambas partes reciben estado-orden-cambiado")
    void ordenes() {
        var orden = UUID.randomUUID();
        notificar.alCrearOrden(
                new OrdenCreadaEvent(orden, SOLICITUD, CLIENTE, DUENO_A, "Paracetamol", 21_000, "DOMICILIO"));
        notificar.alCambiarEstadoOrden(new EstadoOrdenCambiadoEvent(orden, CLIENTE, DUENO_A, "PREPARANDO"));

        assertThat(enviadas())
                .extracting(Notificacion::usuarioId, Notificacion::tipo)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(DUENO_A, "orden-creada"),
                        org.assertj.core.groups.Tuple.tuple(CLIENTE, "estado-orden-cambiado"),
                        org.assertj.core.groups.Tuple.tuple(DUENO_A, "estado-orden-cambiado"));
    }

    @Test
    @Tag("HU-033")
    @Tag("CP-MEN-033-01")
    @DisplayName("CP-MEN-033-01 Solo la otra parte recibe mensaje-recibido, sin el texto")
    void mensajeRecibido() {
        var orden = UUID.randomUUID();
        var mensaje = UUID.randomUUID();
        notificar.alEnviarMensaje(new com.quienlotiene.mensajeria.api.MensajeEnviadoEvent(orden, mensaje, DUENO_A));

        assertThat(enviadas()).singleElement().satisfies(aviso -> {
            assertThat(aviso.usuarioId()).isEqualTo(DUENO_A);
            assertThat(aviso.tipo()).isEqualTo("mensaje-recibido");
            assertThat(aviso.datos()).isEqualTo(new EventosTiempoReal.MensajeRecibido(orden, mensaje));
        });
    }

    @Test
    @Tag("HU-031")
    @Tag("CP-NOT-031-01")
    @DisplayName(
            "CP-NOT-031-01 La solicitud nueva, la orden creada y el cambio de estado también van por push a la tienda")
    void pidePushParaLaTienda() {
        var orden = UUID.randomUUID();
        notificar.alCrearOrden(
                new OrdenCreadaEvent(orden, SOLICITUD, CLIENTE, DUENO_A, "Paracetamol", 21_000, "DOMICILIO"));
        notificar.alCambiarEstadoOrden(new EstadoOrdenCambiadoEvent(orden, CLIENTE, DUENO_A, "PREPARANDO"));

        verify(avisarPorPush).siNoEstaConectado(DUENO_A, "orden-creada", orden);
        verify(avisarPorPush).siNoEstaConectado(DUENO_A, "estado-orden-cambiado", orden);
    }

    @Test
    @Tag("HU-022")
    @Tag("CP-SOL-022-01")
    @DisplayName("CP-SOL-022-01 Al ampliar el radio solo el cliente recibe radio-ampliado, sin push")
    void avisaAlClienteQueSeAmplioLaBusqueda() {
        // Cuando
        notificar.alAmpliarRadio(new RadioAmpliadoEvent(SOLICITUD, CLIENTE, 5_000, 2));

        // Entonces
        assertThat(enviadas()).singleElement().satisfies(aviso -> {
            assertThat(aviso.usuarioId()).isEqualTo(CLIENTE);
            assertThat(aviso.tipo()).isEqualTo(EventosTiempoReal.RadioAmpliado.TIPO);
            assertThat(aviso.datos()).isEqualTo(new EventosTiempoReal.RadioAmpliado(SOLICITUD, 5_000, 2));
        });
        verifyNoInteractions(avisarPorPush);
    }
}
