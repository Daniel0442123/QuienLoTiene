package com.quienlotiene.tiendas.application.usecase;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import com.quienlotiene.tiendas.domain.model.MedioPago;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link DatosTiendasApi}: los demás módulos consultan tiendas sin conocer su dominio. */
@Service
class ConsultarDatosTiendas implements DatosTiendasApi {

    private final TiendaRepository tiendaRepository;

    ConsultarDatosTiendas(TiendaRepository tiendaRepository) {
        this.tiendaRepository = tiendaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DatosTienda obtenerTiendaDelDueno(UUID duenoId) {
        return tiendaRepository
                .buscarPorDueno(duenoId)
                .map(ConsultarDatosTiendas::aDatos)
                .orElseThrow(TiendaNoEncontradaException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatosTienda> buscarPorIds(Collection<UUID> tiendaIds) {
        return new LinkedHashSet<>(tiendaIds)
                .stream()
                        .map(tiendaRepository::buscarPorId)
                        .flatMap(Optional::stream)
                        .map(ConsultarDatosTiendas::aDatos)
                        .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> cotizarDomicilio(UUID tiendaId, double latitud, double longitud, long subtotalEnPesos) {
        var distanciaEnMetros = distanciaA(tiendaId, latitud, longitud);
        var domicilio = tiendaRepository.buscarDomicilio(tiendaId);
        if (!domicilio.cubre(distanciaEnMetros)) {
            return Optional.empty();
        }
        return Optional.of(domicilio.calcularCostoEnPesos(subtotalEnPesos, distanciaEnMetros));
    }

    @Override
    @Transactional(readOnly = true)
    public long calcularCostoDomicilio(UUID tiendaId, double latitud, double longitud, long subtotalEnPesos) {
        var distanciaEnMetros = distanciaA(tiendaId, latitud, longitud);
        return tiendaRepository.buscarDomicilio(tiendaId).calcularCostoEnPesos(subtotalEnPesos, distanciaEnMetros);
    }

    private int distanciaA(UUID tiendaId, double latitud, double longitud) {
        var tienda = tiendaRepository.buscarPorId(tiendaId).orElseThrow(TiendaNoEncontradaException::new);
        return (int) Math.round(tienda.getUbicacion().distanciaEnMetrosA(new Ubicacion(latitud, longitud)));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean aceptaMedioPago(UUID tiendaId, String medioPago) {
        var medio = Arrays.stream(MedioPago.values())
                .filter(valor -> valor.name().equals(medioPago))
                .findFirst();
        return medio.isPresent() && tiendaRepository.buscarMediosPago(tiendaId).acepta(medio.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<String>> mediosPagoDe(Collection<UUID> tiendaIds) {
        return new LinkedHashSet<>(tiendaIds)
                .stream().collect(Collectors.toMap(Function.identity(), this::mediosPagoDe));
    }

    private List<String> mediosPagoDe(UUID tiendaId) {
        return tiendaRepository.buscarMediosPago(tiendaId).medios().stream()
                .map(MedioPago::name)
                .toList();
    }

    private static DatosTienda aDatos(Tienda tienda) {
        return new DatosTienda(tienda.getId(), tienda.getDuenoId(), tienda.getNombre(), tienda.getTelefono());
    }
}
