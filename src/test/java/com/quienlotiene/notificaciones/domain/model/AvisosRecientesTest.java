package com.quienlotiene.notificaciones.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-030")
@DisplayName("HU-030 Avisos recientes para reenviar tras una reconexión")
class AvisosRecientesTest {

    private static final Instant AHORA = Instant.parse("2026-11-17T15:00:00Z");
    private static final UUID USUARIO = UUID.randomUUID();

    private static Notificacion aviso(String id, Instant fecha) {
        return new Notificacion(id, USUARIO, "solicitud-nueva", id, fecha);
    }

    @Test
    @Tag("CP-NOT-030-01")
    @DisplayName("CP-NOT-030-01 Al reconectar con Last-Event-ID se reenvían solo los avisos posteriores")
    void reenviaLosPosteriores() {
        // Dado
        var avisos = new AvisosRecientes();
        avisos.agregar(aviso("1", AHORA));
        avisos.agregar(aviso("2", AHORA));
        avisos.agregar(aviso("3", AHORA));
        // Cuando
        var perdidos = avisos.posterioresA("1", AHORA.plusSeconds(10));
        // Entonces
        assertThat(perdidos).extracting(Notificacion::id).containsExactly("2", "3");
        assertThat(avisos.posterioresA("3", AHORA)).isEmpty();
        assertThat(avisos.posterioresA(null, AHORA)).isEmpty();
        assertThat(avisos.posterioresA(" ", AHORA)).isEmpty();
        assertThat(avisos.posterioresA("desconocido", AHORA))
                .as("si el id ya no está se reenvía todo lo guardado")
                .hasSize(3);
    }

    @Test
    @DisplayName("Los avisos de más de 5 minutos se descartan y se guardan como máximo 100")
    void descartaViejosYExcedentes() {
        var avisos = new AvisosRecientes();
        avisos.agregar(aviso("viejo", AHORA.minus(Duration.ofMinutes(6))));
        for (var numero = 0; numero < AvisosRecientes.MAXIMO_AVISOS + 5; numero++) {
            avisos.agregar(aviso("n" + numero, AHORA));
        }

        assertThat(avisos.posterioresA("otro", AHORA)).hasSize(AvisosRecientes.MAXIMO_AVISOS);
        assertThat(avisos.estaVacio(AHORA)).isFalse();
        assertThat(avisos.estaVacio(AHORA.plus(Duration.ofMinutes(6)))).isTrue();
    }
}
