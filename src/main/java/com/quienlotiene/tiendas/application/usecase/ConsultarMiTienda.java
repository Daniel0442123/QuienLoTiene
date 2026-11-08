package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** CU-16 pasos 2 a 5: la tienda del usuario que inició sesión, con su estado y su horario. */
@Service
public class ConsultarMiTienda {

    private final TiendaRepository tiendaRepository;

    ConsultarMiTienda(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Transactional(readOnly = true)
    public MiTienda ejecutar(UUID duenoId) {
        return tiendaRepository
                .buscarPorDueno(duenoId)
                .map(MiTiendaMapper::aMiTienda)
                .orElseThrow(TiendaNoEncontradaException::new);
    }
}
