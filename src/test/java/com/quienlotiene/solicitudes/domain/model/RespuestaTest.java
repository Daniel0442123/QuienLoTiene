package com.quienlotiene.solicitudes.domain.model;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.abierta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-019")
@DisplayName("HU-019 Responder una solicitud")
class RespuestaTest {

    private static final UUID TIENDA = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    private static Respuesta responder(
            Solicitud solicitud, TipoRespuesta tipo, Long precio, Integer unidades, String alternativa) {
        return Respuesta.responder(
                UUID.randomUUID(), solicitud, TIENDA, DUENO, tipo, precio, unidades, alternativa, 15, 3_000L, AHORA);
    }

    @Test
    @Tag("CP-SOL-019-01")
    @DisplayName("CP-SOL-019-01 Sí lo tengo con precio 8.000 y 2 unidades queda guardada con subtotal 16.000")
    void respondeSiLoTengo() {
        // Dado
        var solicitud = abierta();
        // Cuando
        var respuesta = responder(solicitud, TipoRespuesta.SI, 8_000L, 2, "se ignora");
        // Entonces
        assertThat(respuesta.getTipo()).isEqualTo(TipoRespuesta.SI);
        assertThat(respuesta.getSolicitudId()).isEqualTo(solicitud.getId());
        assertThat(respuesta.getTiendaId()).isEqualTo(TIENDA);
        assertThat(respuesta.getDuenoId()).isEqualTo(DUENO);
        assertThat(respuesta.subtotalEnPesos()).isEqualTo(16_000);
        assertThat(respuesta.getDescripcionAlternativa()).isNull();
        assertThat(respuesta.ofreceDomicilio()).isTrue();
        assertThat(respuesta.fueElegida()).isFalse();
        assertThat(respuesta.getFechaRespuesta()).isEqualTo(AHORA);
    }

    @Test
    @Tag("CP-SOL-019-02")
    @DisplayName("CP-SOL-019-02 Tengo algo parecido sin describir la alternativa se rechaza")
    void rechazaAlternativaSinDescripcion() {
        var solicitud = abierta();
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.PARECIDO, 8_000L, 1, "   "))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.PARECIDO, 8_000L, 1, null))
                .isInstanceOf(RespuestaIncompletaException.class);
    }

    @Test
    @Tag("CP-SOL-020-03")
    @DisplayName(
            "CP-SOL-020-03 Tengo algo parecido guarda la descripción sin espacios y la tarjeta sale como alternativa")
    void parecidoGuardaDescripcion() {
        var respuesta = responder(abierta(), TipoRespuesta.PARECIDO, 7_000L, 1, "  Acetaminofén 500 mg genérico ");

        assertThat(respuesta.getDescripcionAlternativa()).isEqualTo("Acetaminofén 500 mg genérico");
        assertThat(respuesta.aTarjeta(Modalidad.RECOGER, 400).esAlternativa()).isTrue();
    }

    @Test
    @Tag("CP-SOL-019-03")
    @DisplayName("CP-SOL-019-03 No lo tengo descarta precio, unidades y domicilio")
    void noLoTengoDescartaDatos() {
        var respuesta = responder(abierta(), TipoRespuesta.NO, 8_000L, 2, "x");

        assertThat(respuesta.getPrecioEnPesos()).isNull();
        assertThat(respuesta.getCantidadDisponible()).isNull();
        assertThat(respuesta.getCostoDomicilioEnPesos()).isNull();
        assertThat(respuesta.getTiempoEstimadoEnMinutos()).isNull();
        assertThat(respuesta.subtotalEnPesos()).isZero();
        assertThat(respuesta.conCostoDomicilio(5_000L)).isSameAs(respuesta);
        assertThatThrownBy(() -> respuesta.aTarjeta(Modalidad.DOMICILIO, 100))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sin tipo, sin precio, sin unidades o con más unidades de las pedidas se rechaza")
    void rechazaDatosIncompletos() {
        var solicitud = abierta();
        assertThatThrownBy(() -> responder(solicitud, null, 8_000L, 1, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, null, 1, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, 0L, 1, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, Respuesta.PRECIO_MAXIMO_PESOS + 1, 1, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, 8_000L, null, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, 8_000L, 0, null))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.SI, 8_000L, 3, null))
                .isInstanceOf(RespuestaIncompletaException.class)
                .hasMessageContaining("2");
    }

    @Test
    @DisplayName("El tiempo estimado debe estar entre 1 y 240 minutos y la alternativa tiene máximo 300 caracteres")
    void validaTiempoYLongitud() {
        var solicitud = abierta();
        assertThatThrownBy(() -> Respuesta.responder(
                        UUID.randomUUID(), solicitud, TIENDA, DUENO, TipoRespuesta.SI, 1L, 1, null, 0, null, AHORA))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> Respuesta.responder(
                        UUID.randomUUID(), solicitud, TIENDA, DUENO, TipoRespuesta.SI, 1L, 1, null, 241, null, AHORA))
                .isInstanceOf(RespuestaIncompletaException.class);
        assertThatThrownBy(() -> responder(solicitud, TipoRespuesta.PARECIDO, 1L, 1, "a".repeat(301)))
                .isInstanceOf(RespuestaIncompletaException.class);
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-01")
    @DisplayName("CP-SOL-021-01 Una solicitud vencida no acepta respuestas")
    void rechazaSolicitudVencida() {
        var solicitud = abierta();
        var despues = solicitud.getFechaVencimiento();

        assertThatThrownBy(() -> Respuesta.responder(
                        UUID.randomUUID(),
                        solicitud,
                        TIENDA,
                        DUENO,
                        TipoRespuesta.NO,
                        null,
                        null,
                        null,
                        null,
                        null,
                        despues))
                .isInstanceOf(SolicitudCerradaException.class);
    }

    @Test
    @Tag("HU-024")
    @DisplayName("Elegir guarda la orden; una respuesta NO no se puede elegir")
    void elegir() {
        var solicitud = abierta();
        var si = responder(solicitud, TipoRespuesta.SI, 8_000L, 2, null);
        var no = responder(solicitud, TipoRespuesta.NO, null, null, null);
        var orden = UUID.randomUUID();

        si.elegir(orden);

        assertThat(si.fueElegida()).isTrue();
        assertThat(si.getOrdenId()).isEqualTo(orden);
        assertThatThrownBy(() -> no.elegir(orden)).isInstanceOf(RespuestaNoElegibleException.class);
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-02")
    @DisplayName("CP-SOL-021-02 La tienda ve su respuesta como esperando, elegida, no elegida o vencida")
    void estadoSegunLaSolicitud() {
        var solicitud = abierta();
        var elegida = responder(solicitud, TipoRespuesta.SI, 8_000L, 2, null);
        var otra = responder(solicitud, TipoRespuesta.SI, 9_000L, 2, null);
        elegida.elegir(UUID.randomUUID());

        assertThat(otra.estadoSegun(EstadoSolicitud.ABIERTA)).isEqualTo(EstadoRespuesta.ESPERANDO);
        assertThat(elegida.estadoSegun(EstadoSolicitud.RESUELTA)).isEqualTo(EstadoRespuesta.ELEGIDA);
        assertThat(otra.estadoSegun(EstadoSolicitud.RESUELTA)).isEqualTo(EstadoRespuesta.NO_ELEGIDA);
        assertThat(otra.estadoSegun(EstadoSolicitud.VENCIDA)).isEqualTo(EstadoRespuesta.VENCIDA);
    }

    @Test
    @Tag("HU-020")
    @DisplayName("La tarjeta suma el domicilio solo si la solicitud es a domicilio")
    void tarjetaConDomicilio() {
        var solicitud = abierta();
        var respuesta = responder(solicitud, TipoRespuesta.SI, 8_000L, 2, null).conCostoDomicilio(5_000L);

        var aDomicilio = respuesta.aTarjeta(Modalidad.DOMICILIO, 1_200);
        var paraRecoger = respuesta.aTarjeta(Modalidad.RECOGER, 1_200);

        assertThat(aDomicilio.subtotalEnPesos()).isEqualTo(16_000);
        assertThat(aDomicilio.totalEnPesos()).isEqualTo(21_000);
        assertThat(aDomicilio.ofreceDomicilio()).isTrue();
        assertThat(aDomicilio.distanciaEnMetros()).isEqualTo(1_200);
        assertThat(paraRecoger.totalEnPesos()).isEqualTo(16_000);
        assertThat(respuesta
                        .conCostoDomicilio(null)
                        .aTarjeta(Modalidad.DOMICILIO, 1)
                        .totalEnPesos())
                .isEqualTo(16_000);
    }

    @Test
    @DisplayName("Reconstituir conserva todos los datos guardados")
    void reconstituir() {
        var orden = UUID.randomUUID();
        var respuesta = Respuesta.reconstituir(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TIENDA,
                DUENO,
                TipoRespuesta.PARECIDO,
                7_000L,
                1,
                "Genérico",
                30,
                2_000L,
                AHORA.minus(Duration.ofMinutes(1)),
                orden);

        assertThat(respuesta.getOrdenId()).isEqualTo(orden);
        assertThat(respuesta.getDescripcionAlternativa()).isEqualTo("Genérico");
        assertThat(respuesta.getTiempoEstimadoEnMinutos()).isEqualTo(30);
        assertThat(respuesta.getCostoDomicilioEnPesos()).isEqualTo(2_000L);
        assertThat(respuesta.getPrecioEnPesos()).isEqualTo(7_000L);
        assertThat(respuesta.getCantidadDisponible()).isEqualTo(1);
    }
}
