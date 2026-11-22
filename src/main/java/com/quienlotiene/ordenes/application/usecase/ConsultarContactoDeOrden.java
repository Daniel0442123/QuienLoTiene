package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.ordenes.api.ContactoDeOrden;
import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.ordenes.api.ResumenDeOrden;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link OrdenesApi} para mensajeria y reputacion (HU-032 a HU-035, diagrama 18 pasos 6 y 7). */
@Service
class ConsultarContactoDeOrden implements OrdenesApi {

    private static final Logger AUDITORIA = LoggerFactory.getLogger("auditoria");

    private final OrdenRepository ordenRepository;

    ConsultarContactoDeOrden(OrdenRepository ordenRepository) {
        this.ordenRepository = ordenRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenDeOrden obtenerResumen(UUID ordenId, UUID usuarioId) {
        var orden = deLaParte(ordenId, usuarioId);
        return new ResumenDeOrden(
                orden.getId(),
                orden.getClienteId(),
                orden.getTiendaId(),
                orden.getDuenoTiendaId(),
                orden.getNombreTienda(),
                orden.getProducto(),
                orden.getEstado().name(),
                orden.esActiva());
    }

    private Orden deLaParte(UUID ordenId, UUID usuarioId) {
        return ordenRepository
                .buscarPorId(ordenId)
                .filter(encontrada -> encontrada.esParte(usuarioId))
                .orElseThrow(OrdenNoEncontradaException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactoDeOrden obtenerContactoDeLaOtraParte(UUID ordenId, UUID usuarioId) {
        var orden = deLaParte(ordenId, usuarioId);
        if (orden.esDelCliente(usuarioId)) {
            return new ContactoDeOrden(
                    orden.getId(),
                    orden.esActiva(),
                    orden.getNombreTienda(),
                    orden.getTelefonoTienda(),
                    orden.getProducto());
        }
        // NF-SEG-013: la tienda recibe el celular del cliente (WhatsApp, HU-032)
        AUDITORIA.info(
                "La tienda del usuario {} pidió el contacto del cliente de la orden {}", usuarioId, orden.getId());
        return new ContactoDeOrden(
                orden.getId(),
                orden.esActiva(),
                orden.getContactoCliente().nombre(),
                orden.getContactoCliente().telefono(),
                orden.getProducto());
    }
}
