package com.quienlotiene.notificaciones.infrastructure.sse;

import static org.assertj.core.api.Assertions.assertThat;

import com.quienlotiene.notificaciones.domain.model.Notificacion;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-030")
@DisplayName("HU-030 Conexiones SSE")
class EmisoresSseTest {

    private static final Instant AHORA = Instant.parse("2026-11-17T15:00:00Z");

    @Test
    @DisplayName("Una segunda conexión del mismo usuario reemplaza a la primera")
    void unaConexionPorUsuario() {
        // Dado
        var emisores = new EmisoresSse(Clock.fixed(AHORA, ZoneOffset.UTC));
        var usuario = UUID.randomUUID();
        // Cuando
        var primera = emisores.conectar(usuario, null);
        var segunda = emisores.conectar(usuario, null);
        // Entonces
        assertThat(segunda).isNotSameAs(primera);
        assertThat(emisores.estaConectado(usuario)).isTrue();
    }

    @Test
    @DisplayName("Un aviso para alguien sin conexión se guarda y el latido limpia lo viejo sin fallar")
    void avisoSinConexion() {
        var emisores = new EmisoresSse(Clock.fixed(AHORA, ZoneOffset.UTC));
        var usuario = UUID.randomUUID();

        emisores.enviar(new Notificacion("1", usuario, "solicitud-nueva", "datos", AHORA));
        emisores.latir();

        assertThat(emisores.estaConectado(usuario)).isFalse();
    }
}
