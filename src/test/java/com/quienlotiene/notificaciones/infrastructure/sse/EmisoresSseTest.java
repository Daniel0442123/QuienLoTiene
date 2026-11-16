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
    @DisplayName("SC-014: una segunda conexión del mismo usuario (otro celular u otra pestaña) no cierra la primera")
    void variasConexionesPorUsuario() {
        // Dado
        var emisores = new EmisoresSse(Clock.fixed(AHORA, ZoneOffset.UTC));
        var usuario = UUID.randomUUID();
        // Cuando
        var primera = emisores.conectar(usuario, null);
        var segunda = emisores.conectar(usuario, null);
        // Entonces
        assertThat(segunda).isNotSameAs(primera);
        assertThat(emisores.conexionesDe(usuario)).isEqualTo(2);
    }

    @Test
    @DisplayName("SC-014: con el tope de conexiones lleno, la nueva reemplaza a la más antigua")
    void topeDeConexiones() {
        // Dado
        var emisores = new EmisoresSse(Clock.fixed(AHORA, ZoneOffset.UTC));
        var usuario = UUID.randomUUID();
        for (var i = 0; i < EmisoresSse.MAXIMO_CONEXIONES_POR_USUARIO; i++) {
            emisores.conectar(usuario, null);
        }
        // Cuando
        emisores.conectar(usuario, null);
        // Entonces
        assertThat(emisores.conexionesDe(usuario)).isEqualTo(EmisoresSse.MAXIMO_CONEXIONES_POR_USUARIO);
    }

    @Test
    @DisplayName("Las conexiones de usuarios distintos no se cuentan juntas")
    void conexionesPorUsuarioSeparadas() {
        var emisores = new EmisoresSse(Clock.fixed(AHORA, ZoneOffset.UTC));
        var tienda = UUID.randomUUID();
        var cliente = UUID.randomUUID();

        emisores.conectar(tienda, null);
        emisores.conectar(tienda, null);
        emisores.conectar(cliente, null);
        emisores.enviar(new Notificacion("1", tienda, "solicitud-nueva", "datos", AHORA));

        assertThat(emisores.conexionesDe(tienda)).isEqualTo(2);
        assertThat(emisores.conexionesDe(cliente)).isEqualTo(1);
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
