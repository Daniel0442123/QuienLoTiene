package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-013 Abrir o cerrar tienda (CU-19): el interruptor que decide si la tienda recibe solicitudes nuevas. */
@Slf4j
@Service
public class CambiarAperturaTienda {

    private final TiendaRepository tiendaRepository;

    CambiarAperturaTienda(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Transactional
    public MiTienda ejecutar(UUID duenoId, boolean abrir) {
        var tienda = tiendaRepository.buscarPorDueno(duenoId).orElseThrow(TiendaNoEncontradaException::new);
        tienda.cambiarApertura(abrir);
        tiendaRepository.guardarApertura(tienda);
        log.info("Tienda {} quedó {}", tienda.getId(), abrir ? "abierta" : "cerrada");
        return MiTiendaMapper.aMiTienda(tienda);
    }
}
