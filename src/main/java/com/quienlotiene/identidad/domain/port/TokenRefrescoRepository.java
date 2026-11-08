package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.TokenRefresco;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Tokens de refresco guardados como hash en {@code identidad.token_refresco}. */
public interface TokenRefrescoRepository {

    void guardar(TokenRefresco token);

    Optional<TokenRefresco> buscarPorHash(String hashToken);

    /**
     * Revoca el token solo si todavía no estaba revocado. Si dos peticiones intentan rotar el mismo token a la vez,
     * solo una gana.
     *
     * @return {@code true} si esta llamada lo revocó
     */
    boolean revocarSiSigueActivo(UUID tokenId, Instant ahora);

    /** Cierra todas las sesiones del usuario (reuso de un token revocado o cuenta suspendida). */
    void revocarTodosDe(UUID usuarioId, Instant ahora);
}
