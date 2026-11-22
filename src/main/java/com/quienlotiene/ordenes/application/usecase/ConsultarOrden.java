package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-024 y HU-027: ver una orden. Solo su cliente o su tienda; cualquier otro recibe 404 (CP-ORD-024-03). Cuando la
 * tienda recibe el nombre y el celular del cliente queda en la auditoría (NF-SEG-013), con los ids y sin los datos.
 */
@Service
public class ConsultarOrden {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

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
        var vista = OrdenVistaMapper.aVista(orden, usuarioId);
        if (vista.cliente() != null && !orden.esDelCliente(usuarioId)) {
            AUDITORIA.info("La tienda del usuario {} vio los datos del cliente de la orden {}", usuarioId, ordenId);
        }
        return vista;
    }
}
