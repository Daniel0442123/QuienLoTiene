package com.quienlotiene.solicitudes.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-016")
@Tag("CP-SOL-016-03")
@DisplayName("HU-016 Límite de 10 solicitudes por hora")
class LimitePublicacionTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final int MAXIMO_POR_HORA = 10;

    /** Una solicitud cada 5 minutos, la primera hace {@code minutosDeLaPrimera} minutos. */
    private static List<Instant> publicaciones(int cantidad, int minutosDeLaPrimera) {
        var fechas = new ArrayList<Instant>();
        for (int numero = 0; numero < cantidad; numero++) {
            fechas.add(AHORA.minus(Duration.ofMinutes(minutosDeLaPrimera - 5L * numero)));
        }
        return fechas;
    }

    @Test
    @DisplayName("CP-SOL-016-03 Con 9 en la última hora deja publicar la décima (valor límite)")
    void dejaPublicarLaDecima() {
        assertThatCode(() -> LimitePublicacion.verificar(publicaciones(9, 48), MAXIMO_POR_HORA, AHORA))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CP-SOL-016-03 Con 10 en la última hora rechaza la undécima e indica cuándo reintentar")
    void rechazaLaUndecimaYDiceCuandoReintentar() {
        // Dado: 10 solicitudes, la más vieja hace 48 minutos
        var fechas = publicaciones(10, 48);

        // Cuando / Entonces: el cupo se libera cuando la de hace 48 minutos cumpla la hora, en 12 minutos
        assertThatThrownBy(() -> LimitePublicacion.verificar(fechas, MAXIMO_POR_HORA, AHORA))
                .isInstanceOf(LimiteSolicitudesSuperadoException.class)
                .hasMessage("Puedes publicar hasta 10 solicitudes por hora. Intenta de nuevo en 12 minutos.")
                .satisfies(excepcion -> {
                    var limite = (LimiteSolicitudesSuperadoException) excepcion;
                    assertThat(limite.tiempoDeEspera()).isEqualTo(Duration.ofMinutes(12));
                    assertThat(limite.getCodigo()).isEqualTo("SOL-001");
                });
    }

    @Test
    @DisplayName("Si hay más de 10 (por ejemplo tras bajar el máximo), espera a que queden 9")
    void esperaAQueQuedenNueve() {
        // Dado: 11 solicitudes; la que deja 9 al salir es la segunda más vieja (hace 50 - 5 = 45 minutos)
        var fechas = publicaciones(11, 50);

        assertThatThrownBy(() -> LimitePublicacion.verificar(fechas, MAXIMO_POR_HORA, AHORA))
                .isInstanceOf(LimiteSolicitudesSuperadoException.class)
                .satisfies(excepcion -> assertThat(((LimiteSolicitudesSuperadoException) excepcion).tiempoDeEspera())
                        .isEqualTo(Duration.ofMinutes(15)));
    }

    @Test
    @DisplayName("Una espera de segundos se muestra como 1 minuto")
    void redondeaLaEsperaHaciaArriba() {
        var excepcion = new LimiteSolicitudesSuperadoException(10, Duration.ofSeconds(20));

        assertThat(excepcion.getMessage()).endsWith("Intenta de nuevo en 1 minuto.");
    }

    @Test
    @DisplayName("Cuenta desde una hora antes de ahora")
    void cuentaDesdeUnaHoraAntes() {
        assertThat(LimitePublicacion.inicioDeVentana(AHORA)).isEqualTo(AHORA.minus(Duration.ofHours(1)));
    }
}
