package com.quienlotiene.clientes.application.usecase;

import com.quienlotiene.clientes.api.DireccionEntrega;
import com.quienlotiene.clientes.api.DireccionesApi;
import com.quienlotiene.clientes.domain.model.DireccionNoEncontradaException;
import com.quienlotiene.clientes.domain.port.DireccionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link DireccionesApi}. Si la dirección es de otro cliente responde igual que si no existiera (R-03). */
@Service
class ConsultarDireccionEntrega implements DireccionesApi {

    private final DireccionRepository direccionRepository;

    ConsultarDireccionEntrega(DireccionRepository direccionRepository) {
        this.direccionRepository = direccionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DireccionEntrega obtenerDireccionDe(UUID clienteId, UUID direccionId) {
        var direccion = direccionRepository
                .buscarPorId(direccionId)
                .filter(encontrada -> encontrada.esDe(clienteId))
                .orElseThrow(DireccionNoEncontradaException::new);
        return new DireccionEntrega(
                direccion.getId(),
                direccion.getNombre(),
                direccion.getDireccionEscrita(),
                direccion.getIndicaciones(),
                direccion.getTelefono(),
                direccion.getUbicacion().latitud(),
                direccion.getUbicacion().longitud());
    }
}
