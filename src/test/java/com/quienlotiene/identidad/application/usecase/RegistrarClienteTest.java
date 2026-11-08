package com.quienlotiene.identidad.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException;
import com.quienlotiene.identidad.domain.model.FraseContrasena;
import com.quienlotiene.identidad.domain.model.FraseContrasenaComunException;
import com.quienlotiene.identidad.domain.model.FraseContrasenaMuyCortaException;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.CodificadorFrasesContrasena;
import com.quienlotiene.identidad.domain.port.ListaFrasesComunes;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-001")
@DisplayName("HU-001 Registrar cliente")
class RegistrarClienteTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final String FRASE_VALIDA = "mi perro come mango los domingos";

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    CodificadorFrasesContrasena codificadorFrases;

    @Mock
    ListaFrasesComunes listaFrasesComunes;

    RegistrarCliente registrarCliente;

    @BeforeEach
    void configurar() {
        registrarCliente = new RegistrarCliente(
                usuarioRepository, codificadorFrases, listaFrasesComunes, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    @Tag("CP-IDE-001-01")
    @DisplayName("CP-IDE-001-01 Registro exitoso: crea la cuenta con rol CLIENTE y guarda solo el hash")
    void registraAlClienteConRolCliente() {
        // Dado: no existe una cuenta con "ana@correo.com" y la frase tiene 32 caracteres
        when(usuarioRepository.existePorCorreo(new Correo("ana@correo.com"))).thenReturn(false);
        when(codificadorFrases.calcularHash(new FraseContrasena(FRASE_VALIDA))).thenReturn("$argon2id$hash");

        // Cuando
        var resultado = registrarCliente.ejecutar(comando("Ana@Correo.com", FRASE_VALIDA));

        // Entonces
        var usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardarNuevo(usuarioGuardado.capture());
        assertThat(usuarioGuardado.getValue().getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(usuarioGuardado.getValue().estaActiva()).isTrue();
        assertThat(usuarioGuardado.getValue().getHashContrasena())
                .isEqualTo("$argon2id$hash")
                .doesNotContain(FRASE_VALIDA);
        assertThat(resultado.id()).isEqualTo(usuarioGuardado.getValue().getId());
        assertThat(resultado.correo()).isEqualTo("ana@correo.com");
        assertThat(resultado.rol()).isEqualTo("CLIENTE");
        assertThat(resultado.fechaCreacion()).isEqualTo(AHORA);
    }

    @Test
    @Tag("CP-IDE-001-02")
    @DisplayName("CP-IDE-001-02 Correo ya registrado: rechaza el registro con IDE-001")
    void rechazaUnCorreoYaRegistrado() {
        // Dado: existe una cuenta con "ana@correo.com"
        when(usuarioRepository.existePorCorreo(new Correo("ana@correo.com"))).thenReturn(true);

        // Cuando / Entonces
        assertThatThrownBy(() -> registrarCliente.ejecutar(comando("ana@correo.com", FRASE_VALIDA)))
                .isInstanceOf(CorreoYaRegistradoException.class)
                .hasMessageContaining("ya tiene una cuenta")
                .extracting("codigo")
                .isEqualTo("IDE-001");
        verify(usuarioRepository, never()).guardarNuevo(any());
        verify(codificadorFrases, never()).calcularHash(any());
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Frase de 10 caracteres: rechaza el registro y explica la longitud")
    void rechazaUnaFraseCorta() {
        assertThatThrownBy(() -> registrarCliente.ejecutar(comando("ana@correo.com", "corta12345")))
                .isInstanceOf(FraseContrasenaMuyCortaException.class)
                .hasMessageContaining("al menos 15 caracteres");
        verify(usuarioRepository, never()).guardarNuevo(any());
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Contraseña filtrada conocida: rechaza el registro con IDE-003")
    void rechazaUnaFraseComun() {
        // Dado
        when(listaFrasesComunes.contiene(new FraseContrasena("1q2w3e4r5t6y7u8i")))
                .thenReturn(true);

        // Cuando / Entonces
        assertThatThrownBy(() -> registrarCliente.ejecutar(comando("ana@correo.com", "1q2w3e4r5t6y7u8i")))
                .isInstanceOf(FraseContrasenaComunException.class)
                .extracting("codigo")
                .isEqualTo("IDE-003");
        verify(usuarioRepository, never()).guardarNuevo(any());
    }

    private static RegistrarClienteComando comando(String correo, String frase) {
        return new RegistrarClienteComando("Ana Pérez", correo, "3001234567", frase);
    }
}
