package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("NF-SEG-009")
@DisplayName("NF-SEG-009 Intentos de inicio de sesión por cuenta")
class IntentosInicioSesionTest {

    private static final Instant AHORA = Instant.parse("2026-11-22T15:00:00Z");

    @Test
    @DisplayName("Permite 5 intentos en un minuto y el sexto responde con la espera hasta que se libere uno")
    void limitaACincoPorMinuto() {
        var intentos = new IntentosInicioSesion();
        for (int segundo = 0; segundo < 5; segundo++) {
            intentos.registrar("ana@correo.com", AHORA.plusSeconds(segundo * 10L));
        }

        assertThatThrownBy(() -> intentos.registrar("ana@correo.com", AHORA.plusSeconds(50)))
                .isInstanceOf(DemasiadosIntentosInicioSesionException.class)
                .hasFieldOrPropertyWithValue("codigo", "IDE-015")
                .satisfies(error -> assertThat(((DemasiadosIntentosInicioSesionException) error).tiempoDeEspera())
                        .isEqualTo(Duration.ofSeconds(10)));
    }

    @Test
    @DisplayName("El correo cuenta igual con mayúsculas y espacios; otra cuenta tiene sus propios intentos")
    void cuentaPorCorreoNormalizado() {
        var intentos = new IntentosInicioSesion();
        for (var correo :
                new String[] {"Ana@Correo.com", " ana@correo.com", "ANA@CORREO.COM ", "ana@correo.com", "ana@correo.com"
                }) {
            intentos.registrar(correo, AHORA);
        }

        assertThatThrownBy(() -> intentos.registrar("ana@CORREO.com", AHORA))
                .isInstanceOf(DemasiadosIntentosInicioSesionException.class);
        assertThatCode(() -> intentos.registrar("luis@correo.com", AHORA)).doesNotThrowAnyException();
        assertThatCode(() -> intentos.registrar(null, AHORA)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Pasado un minuto desde el primer intento la cuenta puede volver a intentar")
    void seLiberaAlMinuto() {
        var intentos = new IntentosInicioSesion();
        for (int i = 0; i < 5; i++) {
            intentos.registrar("ana@correo.com", AHORA);
        }

        assertThatCode(() -> intentos.registrar("ana@correo.com", AHORA.plus(IntentosInicioSesion.VENTANA)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Con más de 10.000 cuentas en memoria se borran las que ya no tienen intentos en el último minuto")
    void limpiaLasCuentasVencidas() {
        var intentos = new IntentosInicioSesion();
        for (int i = 0; i <= 10_000; i++) {
            intentos.registrar("cuenta" + i + "@correo.com", AHORA);
        }

        intentos.registrar("nueva@correo.com", AHORA.plus(Duration.ofMinutes(2)));

        assertThat(intentos.cuentasRegistradas()).isEqualTo(1);
    }

    @Test
    @DisplayName("La espera nunca es negativa")
    void esperaNoNegativa() {
        assertThat(new DemasiadosIntentosInicioSesionException(Duration.ofSeconds(-1)).tiempoDeEspera())
                .isZero();
    }
}
