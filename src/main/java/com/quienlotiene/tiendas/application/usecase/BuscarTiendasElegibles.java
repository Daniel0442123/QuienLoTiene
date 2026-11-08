package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.api.TiendaElegible;
import com.quienlotiene.tiendas.api.TiendasApi;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tiendas elegibles para una solicitud (diagrama 03 pasos 5 y 6): la base filtra por estado, apertura, categoría y
 * radio con PostGIS; el dominio revisa el horario de cada una ({@code Tienda.puedeRecibirSolicitudes}).
 */
@Service
class BuscarTiendasElegibles implements TiendasApi {

    private final TiendaRepository tiendaRepository;

    BuscarTiendasElegibles(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TiendaElegible> buscarElegibles(
            UUID categoriaId, double latitud, double longitud, int radioEnMetros, Instant momento) {
        return tiendaRepository
                .buscarCandidatasCerca(categoriaId, new Ubicacion(latitud, longitud), radioEnMetros)
                .stream()
                .filter(candidata -> candidata.tienda().puedeRecibirSolicitudes(momento))
                .map(candidata -> new TiendaElegible(
                        candidata.tienda().getId(),
                        candidata.tienda().getDuenoId(),
                        candidata.tienda().getNombre(),
                        (int) Math.round(candidata.distanciaEnMetros())))
                .toList();
    }
}
