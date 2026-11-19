package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.TiendaPublica;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-014 Ver detalle de tienda (CU-06, diagrama 14): datos públicos de una tienda APROBADA (CP-TIE-014-01). Una tienda
 * pendiente, rechazada o suspendida no se muestra. El promedio y las reseñas los da reputacion en otra ruta, para que
 * tiendas no dependa de reputacion (reputacion ya depende de ordenes, que depende de tiendas).
 */
@Service
public class VerDetalleTienda {

    private final TiendaRepository tiendaRepository;

    VerDetalleTienda(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    /** @throws TiendaNoEncontradaException TIE-006 si no existe o no está APROBADA */
    @Transactional(readOnly = true)
    public TiendaPublica ejecutar(UUID tiendaId) {
        var tienda = tiendaRepository
                .buscarPorId(tiendaId)
                .filter(encontrada -> encontrada.getEstado() == EstadoTienda.APROBADA)
                .orElseThrow(TiendaNoEncontradaException::new);
        var miTienda = MiTiendaMapper.aMiTienda(tienda);
        return new TiendaPublica(
                tienda.getId(),
                tienda.getNombre(),
                tienda.getTelefono(),
                tienda.getCategoriaId(),
                tienda.getUbicacion().latitud(),
                tienda.getUbicacion().longitud(),
                tienda.estaAbierta(),
                miTienda.horario(),
                ConfigurarDomicilio.aDomicilio(tiendaRepository.buscarDomicilio(tiendaId)),
                ConfigurarMediosPago.nombres(tiendaRepository.buscarMediosPago(tiendaId)));
    }
}
