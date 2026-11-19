package com.quienlotiene.identidad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.EstadoCuenta;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-037")
@DisplayName("HU-037 Suspender la cuenta del dueño")
class GestionarEstadoCuentaTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    TokenRefrescoRepository tokenRefrescoRepository;

    private GestionarEstadoCuenta gestionar() {
        return new GestionarEstadoCuenta(
                usuarioRepository, tokenRefrescoRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private Usuario dueno() {
        var usuario = Usuario.registrarDuenoTienda(
                UUID.randomUUID(), "Luis", new Correo("luis@correo.com"), "3001234567", "$argon2id$hash", AHORA);
        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        return usuario;
    }

    @Test
    @Tag("CP-ADM-037-01")
    @DisplayName("CP-ADM-037-01 Suspender deja la cuenta SUSPENDIDA y revoca sus sesiones")
    void suspende() {
        var usuario = dueno();

        gestionar().suspender(usuario.getId());

        assertThat(usuario.getEstado()).isEqualTo(EstadoCuenta.SUSPENDIDA);
        verify(usuarioRepository).guardarEstado(usuario);
        verify(tokenRefrescoRepository).revocarTodosDe(usuario.getId(), AHORA);
    }

    @Test
    @DisplayName("Reactivar devuelve la cuenta a ACTIVA; un usuario inexistente no hace nada")
    void reactiva() {
        var usuario = dueno();
        usuario.suspender();

        gestionar().reactivar(usuario.getId());
        assertThat(usuario.estaActiva()).isTrue();

        gestionar().suspender(UUID.randomUUID());
        verify(tokenRefrescoRepository, never()).revocarTodosDe(any(), any());
    }
}
