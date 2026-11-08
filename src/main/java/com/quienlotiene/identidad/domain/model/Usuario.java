package com.quienlotiene.identidad.domain.model;

import java.time.Instant;
import java.util.Objects;
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
    private final Instant fechaCreacion;

    private Usuario(
            UUID id,
            String nombre,
            Correo correo,
            String telefono,
            String hashContrasena,
            Rol rol,
            EstadoCuenta estado,
            Instant fechaCreacion) {
        this.id = Objects.requireNonNull(id, "id");
        this.nombre = exigirTexto(nombre, "nombre");
        this.correo = Objects.requireNonNull(correo, "correo");
        this.telefono = exigirTexto(telefono, "telefono");
        this.hashContrasena = exigirTexto(hashContrasena, "hashContrasena");
        this.rol = Objects.requireNonNull(rol, "rol");
        this.estado = Objects.requireNonNull(estado, "estado");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "fechaCreacion");
    }

    /** Un cliente nuevo nace activo (HU-001): puede iniciar sesión de inmediato. */
    public static Usuario registrarCliente(
            UUID id, String nombre, Correo correo, String telefono, String hashContrasena, Instant ahora) {
        return new Usuario(
                id, nombre.strip(), correo, telefono.strip(), hashContrasena, Rol.CLIENTE, EstadoCuenta.ACTIVA, ahora);
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
        return new Usuario(id, nombre, correo, telefono, hashContrasena, rol, estado, fechaCreacion);
    }

    public boolean estaActiva() {
        return estado == EstadoCuenta.ACTIVA;
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
