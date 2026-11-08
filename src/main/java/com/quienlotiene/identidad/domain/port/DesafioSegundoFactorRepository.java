package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.DesafioSegundoFactor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Desafíos de segundo factor guardados como hash en {@code identidad.desafio_segundo_factor}. */
public interface DesafioSegundoFactorRepository {

    void guardar(DesafioSegundoFactor desafio);

    Optional<DesafioSegundoFactor> buscarPorHash(String hashToken);

    void registrarIntentoFallido(UUID desafioId, Instant ahora);

    /**
     * Marca el desafío como usado solo si nadie lo usó antes. Si dos peticiones llegan a la vez con un código válido,
     * solo una inicia sesión.
     *
     * @return {@code true} si esta llamada lo marcó
     */
    boolean marcarUsadoSiNoLoEsta(UUID desafioId, Instant ahora);
}
