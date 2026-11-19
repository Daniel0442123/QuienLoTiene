package com.quienlotiene.reputacion.application.usecase;

import com.quienlotiene.reputacion.application.dto.ReputacionTienda;
import com.quienlotiene.reputacion.domain.port.ResenaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-014 y HU-034: promedio y reseñas recientes de una tienda, para su detalle público (CP-TIE-014-01). */
@Service
public class ConsultarReputacion {

    /** Reseñas que se muestran en el detalle. */
    static final int RECIENTES = 10;

    private final ResenaRepository resenaRepository;

    ConsultarReputacion(ResenaRepository resenaRepository) {
        this.resenaRepository = resenaRepository;
    }

    @Transactional(readOnly = true)
    public ReputacionTienda ejecutar(UUID tiendaId) {
        var resumen = resenaRepository.resumenDe(tiendaId);
        return new ReputacionTienda(
                tiendaId,
                resumen.promedio(),
                resumen.totalResenas(),
                resenaRepository.recientesDe(tiendaId, RECIENTES).stream()
                        .map(resena -> new ReputacionTienda.ResenaPublica(
                                resena.estrellas(), resena.comentario(), resena.fecha()))
                        .toList());
    }
}
