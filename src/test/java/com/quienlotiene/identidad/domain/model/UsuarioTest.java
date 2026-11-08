package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-001")
@DisplayName("HU-001 Usuario")
class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");

    @Test
    @Tag("CP-IDE-001-01")
    @DisplayName("CP-IDE-001-01 Un cliente registrado nace con rol CLIENTE y cuenta activa")
    void registraUnClienteActivo() {
        // Dado
        var id = UUID.randomUUID();

        // Cuando
        var cliente = Usuario.registrarCliente(
                id, "  Ana Pérez ", new Correo("ana@correo.com"), " 3001234567 ", "$argon2id$hash", AHORA);

        // Entonces
        assertThat(cliente.getId()).isEqualTo(id);
        assertThat(cliente.getRol()).isEqualTo(Rol.CLIENTE);
        assertThat(cliente.getEstado()).isEqualTo(EstadoCuenta.ACTIVA);
        assertThat(cliente.estaActiva()).isTrue();
        assertThat(cliente.getNombre()).isEqualTo("Ana Pérez");
        assertThat(cliente.getTelefono()).isEqualTo("3001234567");
        assertThat(cliente.getCorreo().valor()).isEqualTo("ana@correo.com");
        assertThat(cliente.getHashContrasena()).isEqualTo("$argon2id$hash");
        assertThat(cliente.getFechaCreacion()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("Un usuario reconstituido conserva su estado suspendido")
    void reconstituyeUnUsuarioSuspendido() {
        var usuario = Usuario.reconstituir(
                UUID.randomUUID(),
                "Tienda La 5",
                new Correo("tienda@correo.com"),
                "3001234567",
                "$argon2id$hash",
                Rol.TIENDA,
                EstadoCuenta.SUSPENDIDA,
                AHORA);

        assertThat(usuario.estaActiva()).isFalse();
    }

    @Test
    @DisplayName("No se puede crear sin nombre, teléfono o hash")
    void exigeLosDatosObligatorios() {
        var correo = new Correo("ana@correo.com");
        var id = UUID.randomUUID();

        assertThatThrownBy(() -> Usuario.reconstituir(
                        id, " ", correo, "3001234567", "hash", Rol.CLIENTE, EstadoCuenta.ACTIVA, AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() ->
                        Usuario.reconstituir(id, "Ana", correo, null, "hash", Rol.CLIENTE, EstadoCuenta.ACTIVA, AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("telefono");
        assertThatThrownBy(() -> Usuario.reconstituir(
                        id, "Ana", correo, "3001234567", "", Rol.CLIENTE, EstadoCuenta.ACTIVA, AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hashContrasena");
    }
}
