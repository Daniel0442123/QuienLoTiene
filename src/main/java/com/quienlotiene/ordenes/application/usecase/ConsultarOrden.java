package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-024 y HU-027: ver una orden. Solo su cliente o su tienda; cualquier otro recibe 404 (CP-ORD-024-03). */
@Service
public class ConsultarOrden {

    private final OrdenRepository ordenRepository;

    ConsultarOrden(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Transactional(readOnly = true)
    public OrdenVista ejecutar(UUID usuarioId, UUID ordenId) {
        var orden = ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esParte(usuarioId))
                .orElseThrow(OrdenNoEncontradaException::new);
        return OrdenVistaMapper.aVista(orden, usuarioId);
    }
}
