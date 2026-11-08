package com.quienlotiene.identidad.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Cuenta que inicia sesión: cliente, tienda o administrador (glosario, DECISIONES-BACKEND 3.4). */
public final class Usuario {

    private final UUID id;
    private final String nombre;
    private final Correo correo;
    private final String telefono;
    private final String hashContrasena;
    private final Rol rol;
    private final EstadoCuenta estado;
    /** Secreto TOTP del administrador, cifrado; {@code null} para clientes y tiendas. */
    private final String secretoTotpCifrado;

    private final Instant fechaCreacion;

    private Usuario(
            UUID id,
            String nombre,
            Correo correo,
            String telefono,
            String hashContrasena,
            Rol rol,
            EstadoCuenta estado,
            String secretoTotpCifrado,
            Instant fechaCreacion) {
        this.id = Objects.requireNonNull(id, "id");
        this.nombre = exigirTexto(nombre, "nombre");
        this.correo = Objects.requireNonNull(correo, "correo");
        this.telefono = exigirTexto(telefono, "telefono");
        this.hashContrasena = exigirTexto(hashContrasena, "hashContrasena");
        this.rol = Objects.requireNonNull(rol, "rol");
        this.estado = Objects.requireNonNull(estado, "estado");
        this.secretoTotpCifrado = secretoTotpCifrado;
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion");
    }

    /** Un cliente nuevo nace activo (HU-001): puede iniciar sesión de inmediato. */
    public static Usuario registrarCliente(
            UUID id, String nombre, Correo correo, String telefono, String hashContrasena, Instant ahora) {
        return new Usuario(
                id,
                nombre.strip(),
                correo,
                telefono.strip(),
                hashContrasena,
                Rol.CLIENTE,
                EstadoCuenta.ACTIVA,
                null,
                ahora);
    }

    /**
     * El administrador se crea con su secreto TOTP ya cifrado (HU-004): sin él no podría completar el segundo factor.
     */
    public static Usuario registrarAdministrador(
            UUID id,
            String nombre,
            Correo correo,
            String telefono,
            String hashContrasena,
            String secretoTotpCifrado,
            Instant ahora) {
        return new Usuario(
                id,
                nombre.strip(),
                correo,
                telefono.strip(),
                hashContrasena,
                Rol.ADMIN,
                EstadoCuenta.ACTIVA,
                exigirTexto(secretoTotpCifrado, "secretoTotpCifrado"),
                ahora);
    }

    /** Vuelve a armar un usuario ya guardado; solo lo usa el adaptador de persistencia. */
    public static Usuario reconstituir(
            UUID id,
            String nombre,
            Correo correo,
            String telefono,
            String hashContrasena,
            Rol rol,
            EstadoCuenta estado,
            Instant fechaCreacion) {
        return reconstituir(id, nombre, correo, telefono, hashContrasena, rol, estado, null, fechaCreacion);
    }

    /** Igual que el anterior, con el secreto TOTP cifrado (solo administradores). */
    public static Usuario reconstituir(
            UUID id,
            String nombre,
            Correo correo,
            String telefono,
            String hashContrasena,
            Rol rol,
            EstadoCuenta estado,
            String secretoTotpCifrado,
            Instant fechaCreacion) {
        return new Usuario(
                id, nombre, correo, telefono, hashContrasena, rol, estado, secretoTotpCifrado, fechaCreacion);
    }

    public boolean estaActiva() {
        return estado == EstadoCuenta.ACTIVA;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMIN;
    }

    public boolean tieneSegundoFactor() {
        return secretoTotpCifrado != null && !secretoTotpCifrado.isBlank();
    }

    public Optional<String> getSecretoTotpCifrado() {
        return Optional.ofNullable(secretoTotpCifrado);
    }

    private static String exigirTexto(String valor, String nombreCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El campo " + nombreCampo + " del usuario es obligatorio");
        }
        return valor;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Correo getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
