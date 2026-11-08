package com.quienlotiene.identidad.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Token de refresco opaco y rotativo (D-15): cada uso lo revoca y entrega uno nuevo. Si llega uno ya revocado es
 * señal de robo y se cierran todas las sesiones del usuario.
 */
public final class TokenRefresco {

    private final UUID id;
    private final UUID usuarioId;
    private final String hashToken;
    private final Instant fechaExpiracion;
    private final boolean estaRevocado;
    private final Instant fechaCreacion;

    private TokenRefresco(
            UUID id,
            UUID usuarioId,
            String hashToken,
            Instant fechaExpiracion,
            boolean estaRevocado,
            Instant fechaCreacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.hashToken = hashToken;
        this.fechaExpiracion = fechaExpiracion;
        this.estaRevocado = estaRevocado;
        this.fechaCreacion = fechaCreacion;
    }

    public static TokenRefresco emitir(
            UUID id, UUID usuarioId, ValorTokenRefresco valor, Duration duracion, Instant ahora) {
        return new TokenRefresco(id, usuarioId, valor.calcularHash(), ahora.plus(duracion), false, ahora);
    }

    /** Vuelve a armar un token ya guardado; solo lo usa el adaptador de persistencia. */
    public static TokenRefresco reconstituir(
            UUID id,
            UUID usuarioId,
            String hashToken,
            Instant fechaExpiracion,
            boolean estaRevocado,
            Instant fechaCreacion) {
        return new TokenRefresco(id, usuarioId, hashToken, fechaExpiracion, estaRevocado, fechaCreacion);
    }

    public boolean estaVencido(Instant ahora) {
        return !ahora.isBefore(fechaExpiracion);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getHashToken() {
        return hashToken;
    }

    public Instant getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean estaRevocado() {
        return estaRevocado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
