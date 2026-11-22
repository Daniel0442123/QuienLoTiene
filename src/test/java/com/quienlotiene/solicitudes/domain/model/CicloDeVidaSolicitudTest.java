package com.quienlotiene.solicitudes.domain.model;

import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.AHORA;
import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.POPAYAN;
import static com.quienlotiene.solicitudes.domain.model.SolicitudesDePrueba.abierta;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-021")
@DisplayName("HU-021 Ciclo de vida de la solicitud")
class CicloDeVidaSolicitudTest {

    @Test
    @Tag("CP-SOL-021-01")
    @DisplayName("CP-SOL-021-01 Al cumplirse el tiempo la solicitud pasa a VENCIDA y no recibe más respuestas")
    void venceAlCumplirseElTiempo() {
        // Dado
        var solicitud = abierta();
        // Cuando
        var antes = solicitud.vencer(AHORA);
        var alVencer = solicitud.vencer(solicitud.getFechaVencimiento());
        var otraVez = solicitud.vencer(solicitud.getFechaVencimiento().plusSeconds(60));
        // Entonces
        assertThat(antes).isFalse();
        assertThat(alVencer).isTrue();
        assertThat(otraVez).isFalse();
        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.VENCIDA);
        assertThat(solicitud.recibeRespuestas(AHORA)).isFalse();
    }

    @Test
    @Tag("HU-024")
    @DisplayName("Resolver deja la solicitud RESUELTA; una resuelta no vence ni se vuelve a resolver")
    void resolver() {
        var solicitud = abierta();

        solicitud.resolver(AHORA);

        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.RESUELTA);
        assertThat(solicitud.vencer(solicitud.getFechaVencimiento())).isFalse();
        assertThatThrownBy(() -> solicitud.resolver(AHORA)).isInstanceOf(SolicitudCerradaException.class);
    }

    @Test
    @DisplayName("No se puede resolver una solicitud cuyo tiempo ya se cumplió aunque siga ABIERTA")
    void noResuelveVencida() {
        var solicitud = abierta();
        assertThatThrownBy(() -> solicitud.resolver(solicitud.getFechaVencimiento()))
                .isInstanceOf(SolicitudCerradaException.class);
    }

    @Test
    @DisplayName("Reconstituir conserva el estado y esDe reconoce al cliente")
    void reconstituirYEsDe() {
        var cliente = UUID.randomUUID();
        var solicitud = Solicitud.reconstituir(
                UUID.randomUUID(),
                cliente,
                UUID.randomUUID(),
                "Ibuprofeno",
                1,
                Modalidad.RECOGER,
                true,
                POPAYAN,
                5_000,
                false,
                false,
                EstadoSolicitud.RESUELTA,
                AHORA,
                AHORA.plus(Duration.ofMinutes(30)));

        assertThat(solicitud.getEstado()).isEqualTo(EstadoSolicitud.RESUELTA);
        assertThat(solicitud.esDe(cliente)).isTrue();
        assertThat(solicitud.esDe(UUID.randomUUID())).isFalse();
        assertThat(solicitud.esUrgente()).isTrue();
        assertThat(solicitud.getRadioEnMetros()).isEqualTo(5_000);
    }

    @Test
    @Tag("HU-018")
    @Tag("CP-SOL-018-02")
    @DisplayName("CP-SOL-018-02 Un cliente a 1.234 m se ve a 1.200 m; nunca menos de 100 m")
    void distanciaAproximada() {
        assertThat(DistanciaAproximada.redondear(1_234)).isEqualTo(1_200);
        assertThat(DistanciaAproximada.redondear(1_250)).isEqualTo(1_300);
        assertThat(DistanciaAproximada.redondear(0)).isEqualTo(100);
        assertThat(DistanciaAproximada.redondear(30)).isEqualTo(100);
        assertThat(new SolicitudParaTienda(abierta(), 2_960).distanciaAproximadaEnMetros())
                .isEqualTo(3_000);
    }

    @Test
    @Tag("HU-020")
    @Tag("CP-SOL-020-02")
    @DisplayName("CP-SOL-020-02 Ordenar por precio muestra 7.500, 8.200 y 9.000")
    void ordenaPorPrecio() {
        var tarjetas = new ArrayList<>(List.of(
                tarjeta(9_000, 300, 18_000, AHORA),
                tarjeta(7_500, 900, 20_000, AHORA),
                tarjeta(8_200, 100, 16_400, AHORA)));

        tarjetas.sort(CriterioOrden.desde("precio").comparador());

        assertThat(tarjetas).extracting(TarjetaRespuesta::precioEnPesos).containsExactly(7_500L, 8_200L, 9_000L);
    }

    @Test
    @Tag("HU-020")
    @DisplayName("Ordenar por distancia o total; con empate gana la que respondió primero")
    void ordenaPorDistanciaYTotal() {
        var primera = tarjeta(9_000, 300, 18_000, AHORA);
        var cercana = tarjeta(7_500, 100, 20_000, AHORA);
        var tardia = tarjeta(9_000, 300, 18_000, AHORA.plusSeconds(30));
        var tarjetas = new ArrayList<>(List.of(tardia, primera, cercana));

        tarjetas.sort(CriterioOrden.desde(" Distancia ").comparador());
        assertThat(tarjetas).containsExactly(cercana, primera, tardia);

        tarjetas.sort(CriterioOrden.desde("total").comparador());
        assertThat(tarjetas).containsExactly(primera, tardia, cercana);

        assertThat(CriterioOrden.desde(null)).isEqualTo(CriterioOrden.PRECIO);
        assertThat(CriterioOrden.desde("otro")).isEqualTo(CriterioOrden.PRECIO);
    }

    private static TarjetaRespuesta tarjeta(long precio, int distancia, long total, Instant fecha) {
        return new TarjetaRespuesta(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TipoRespuesta.SI,
                null,
                precio,
                2,
                precio * 2,
                null,
                total,
                distancia,
                null,
                fecha,
                false);
    }
}
