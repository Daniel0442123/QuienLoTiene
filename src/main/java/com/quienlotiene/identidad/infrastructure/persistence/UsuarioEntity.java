package com.quienlotiene.identidad.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code identidad.usuario}. El dominio no la conoce: el adaptador la convierte en {@code Usuario}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "identidad", name = "usuario")
public class UsuarioEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 254, unique = true)
    private String correo;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "hash_contrasena", nullable = false)
    private String hashContrasena;

    /** Nombre del enum {@code Rol} del dominio. */
    @Column(nullable = false, length = 10)
    private String rol;

    /** Nombre del enum {@code EstadoCuenta} del dominio. */
    @Column(nullable = false, length = 12)
    private String estado;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public UsuarioEntity(
            UUID id,
            String nombre,
            String correo,
            String telefono,
            String hashContrasena,
            String rol,
            String estado,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.hashContrasena = hashContrasena;
        this.rol = rol;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }
}
