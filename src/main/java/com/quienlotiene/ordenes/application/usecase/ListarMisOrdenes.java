package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-027: órdenes del cliente o de la tienda, primero las activas y cada grupo de la más reciente a la más antigua. */
@Service
public class ListarMisOrdenes {

    static final int MAXIMO_ORDENES = 50;

    private final OrdenRepository ordenRepository;

    ListarMisOrdenes(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Transactional(readOnly = true)
    public List<OrdenVista> ejecutar(UUID usuarioId) {
        return ordenRepository.listarDeUsuario(usuarioId, MAXIMO_ORDENES).stream()
                .sorted(Comparator.comparing((Orden orden) -> !orden.esActiva())
                        .thenComparing(Orden::getFechaCreacion, Comparator.reverseOrder()))
                .map(orden -> OrdenVistaMapper.aVista(orden, usuarioId))
                .toList();
    }
}
