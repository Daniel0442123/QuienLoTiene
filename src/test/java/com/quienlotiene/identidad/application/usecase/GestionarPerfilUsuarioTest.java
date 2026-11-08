package com.quienlotiene.identidad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.SesionVencidaException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-007")
@DisplayName("HU-007 Perfil del usuario")
class GestionarPerfilUsuarioTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Cambia el nombre y el teléfono, sin tocar el correo")
    void actualizaElPerfil() {
        // Dado
        var usuario = Usuario.registrarCliente(
                UUID.randomUUID(), "Ana", new Correo("ana@correo.com"), "3001234567", "$argon2id$hash", Instant.now());
        when(usuarioRepository.buscarPorId(usuario.getId())).thenReturn(Optional.of(usuario));
        var gestionarPerfil = new GestionarPerfilUsuario(usuarioRepository);

        // Cuando
        var perfil = gestionarPerfil.actualizar(usuario.getId(), " Ana María ", " 3109876543 ");

        // Entonces
        assertThat(perfil.nombre()).isEqualTo("Ana María");
        assertThat(perfil.telefono()).isEqualTo("3109876543");
        assertThat(perfil.correo()).isEqualTo("ana@correo.com");
        verify(usuarioRepository).guardarPerfil(usuario);
        assertThat(gestionarPerfil.consultar(usuario.getId()).rol()).isEqualTo("CLIENTE");
        assertThat(perfil.toString()).doesNotContain("ana@correo.com");
    }

    @Test
    @DisplayName("Un token de un usuario que ya no existe se trata como sesión vencida")
    void usuarioInexistente() {
        var idInexistente = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new GestionarPerfilUsuario(usuarioRepository).consultar(idInexistente))
                .isInstanceOf(SesionVencidaException.class);
    }
}
