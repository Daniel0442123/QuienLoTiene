package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.api.ContactoDeOrden;
import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link OrdenesApi} para mensajeria (HU-032, diagrama 18 pasos 6 y 7). */
@Service
class ConsultarContactoDeOrden implements OrdenesApi {

    private final OrdenRepository ordenRepository;

    ConsultarContactoDeOrden(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ContactoDeOrden obtenerContactoDeLaOtraParte(UUID ordenId, UUID usuarioId) {
        var orden = ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esParte(usuarioId))
                .orElseThrow(OrdenNoEncontradaException::new);
        if (orden.esDelCliente(usuarioId)) {
            return new ContactoDeOrden(
                    orden.getId(),
                    orden.esActiva(),
                    orden.getNombreTienda(),
                    orden.getTelefonoTienda(),
                    orden.getProducto());
        }
        return new ContactoDeOrden(
                orden.getId(),
                orden.esActiva(),
                orden.getContactoCliente().nombre(),
                orden.getContactoCliente().telefono(),
                orden.getProducto());
    }
}
