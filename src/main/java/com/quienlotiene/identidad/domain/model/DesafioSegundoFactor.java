package com.quienlotiene.identidad.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Paso intermedio del inicio de sesión del administrador (CU-25): la frase ya fue correcta y falta el código TOTP. El
 * token temporal que se entrega solo sirve para {@code POST /api/v1/auth/totp}, vence a los 5 minutos, se usa una
 * sola vez y admite 5 intentos fallidos.
 */
public final class DesafioSegundoFactor {

    public static final Duration VIGENCIA = Duration.ofMinutes(5);
    public static final int MAXIMO_INTENTOS_FALLIDOS = 5;

    private final UUID id;
    private final UUID usuarioId;
    private final String hashToken;
    private final Instant fechaExpiracion;
    private final int intentosFallidos;
    private final boolean fueUsado;

    private DesafioSegundoFactor(
            UUID id,
            UUID usuarioId,
            String hashToken,
            Instant fechaExpiracion,
            int intentosFallidos,
            boolean fueUsado) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.hashToken = hashToken;
        this.fechaExpiracion = fechaExpiracion;
        this.intentosFallidos = intentosFallidos;
        this.fueUsado = fueUsado;
    }

    public static DesafioSegundoFactor emitir(UUID id, UUID usuarioId, ValorTokenOpaco valor, Instant ahora) {
        return new DesafioSegundoFactor(id, usuarioId, valor.calcularHash(), ahora.plus(VIGENCIA), 0, false);
    }

    /** Vuelve a armar un desafío guardado; solo lo usa el adaptador de persistencia. */
    public static DesafioSegundoFactor reconstituir(
            UUID id,
            UUID usuarioId,
            String hashToken,
            Instant fechaExpiracion,
            int intentosFallidos,
            boolean fueUsado) {
        return new DesafioSegundoFactor(id, usuarioId, hashToken, fechaExpiracion, intentosFallidos, fueUsado);
    }

    /** Sigue sirviendo si no se usó, no venció y no agotó los intentos. */
    public boolean admiteIntentos(Instant ahora) {
        return !fueUsado && ahora.isBefore(fechaExpiracion) && intentosFallidos < MAXIMO_INTENTOS_FALLIDOS;
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

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public boolean fueUsado() {
        return fueUsado;
    }
}
