package com.quienlotiene.reputacion.domain.port;

import com.quienlotiene.reputacion.domain.model.Resena;
import com.quienlotiene.reputacion.domain.model.ResumenReputacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Reseñas del esquema {@code reputacion}. */
public interface ResenaRepository {

    void guardar(Resena resena);

    boolean existeParaOrden(UUID ordenId);

    Optional<Resena> buscarPorOrden(UUID ordenId);

    ResumenReputacion resumenDe(UUID tiendaId);

    /** De la más reciente a la más antigua. */
    List<Resena> recientesDe(UUID tiendaId, int maximo);
}
