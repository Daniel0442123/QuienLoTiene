package com.quienlotiene.ordenes.infrastructure.persistence;

import com.quienlotiene.ordenes.domain.model.CambioEstado;
import com.quienlotiene.ordenes.domain.model.ContactoCliente;
import com.quienlotiene.ordenes.domain.model.DireccionDeEntrega;
import com.quienlotiene.ordenes.domain.model.EstadoOrden;
import com.quienlotiene.ordenes.domain.model.MedioPagoOrden;
import com.quienlotiene.ordenes.domain.model.ModalidadEntrega;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link OrdenRepository}. */
@Component
class JpaOrdenRepository implements OrdenRepository {

    private final OrdenJpaRepository ordenJpaRepository;
    private final HistorialEstadoJpaRepository historialJpaRepository;
    private final EntityManager entityManager;
    private final Clock reloj;

    JpaOrdenRepository(
            OrdenJpaRepository ordenJpaRepository,
            HistorialEstadoJpaRepository historialJpaRepository,
            EntityManager entityManager,
            Clock reloj) {
        this.ordenJpaRepository = ordenJpaRepository;
        this.historialJpaRepository = historialJpaRepository;
        this.entityManager = entityManager;
        this.reloj = reloj;
    }

    @Override
    public void guardarNueva(Orden orden) {
        var direccion = orden.getDireccionEntrega();
        entityManager.persist(new OrdenEntity(
                new OrdenEntity.Datos(
                        orden.getId(),
                        orden.getSolicitudId(),
                        orden.getRespuestaId(),
                        orden.getClienteId(),
                        orden.getTiendaId(),
                        orden.getDuenoTiendaId(),
                        orden.getProducto(),
                        orden.getCantidad(),
                        orden.getPrecioUnitarioEnPesos(),
                        orden.getCostoDomicilioEnPesos(),
                        orden.getModalidad().name(),
                        orden.getMedioPago().name(),
                        orden.getContactoCliente().nombre(),
                        orden.getContactoCliente().telefono(),
                        orden.getNombreTienda(),
                        orden.getTelefonoTienda(),
                        direccion == null ? null : direccion.nombre(),
                        direccion == null ? null : direccion.direccionEscrita(),
                        direccion == null ? null : direccion.indicaciones(),
                        direccion == null ? null : direccion.latitud(),
                        direccion == null ? null : direccion.longitud()),
                orden.getEstado().name(),
                orden.getFechaCreacion()));
        guardarHistorialNuevo(orden, 0);
    }

    @Override
    public void guardarCambioEstado(Orden orden) {
        var fila = ordenJpaRepository.findById(orden.getId()).orElseThrow();
        fila.cambiarEstado(orden.getEstado().name(), reloj.instant());
        guardarHistorialNuevo(orden, historialJpaRepository.countByOrdenId(orden.getId()));
    }

    @Override
    public Optional<Orden> buscarPorId(UUID ordenId) {
        return ordenJpaRepository
                .findById(ordenId)
                .map(fila -> aDominio(fila, historialJpaRepository.findByOrdenIdOrderByCambiadoEnAsc(ordenId)));
    }

    @Override
    public List<Orden> listarDeUsuario(UUID usuarioId, int maximo) {
        var filas = ordenJpaRepository.listarDeUsuario(usuarioId, Limit.of(maximo));
        if (filas.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<HistorialEstadoEntity>> historialPorOrden =
                historialJpaRepository
                        .findByOrdenIdInOrderByCambiadoEnAsc(
                                filas.stream().map(OrdenEntity::getId).toList())
                        .stream()
                        .collect(Collectors.groupingBy(HistorialEstadoEntity::getOrdenId));
        return filas.stream()
                .map(fila -> aDominio(fila, historialPorOrden.getOrDefault(fila.getId(), List.of())))
                .toList();
    }

    private void guardarHistorialNuevo(Orden orden, long yaGuardados) {
        orden.getHistorial().stream()
                .skip(yaGuardados)
                .forEach(cambio -> entityManager.persist(new HistorialEstadoEntity(
                        UUID.randomUUID(), orden.getId(), cambio.estado().name(), cambio.fecha())));
    }

    private static Orden aDominio(OrdenEntity fila, List<HistorialEstadoEntity> historial) {
        var direccion = fila.getDireccionEscrita() == null
                ? null
                : new DireccionDeEntrega(
                        fila.getDireccionNombre(),
                        fila.getDireccionEscrita(),
                        fila.getDireccionIndicaciones(),
                        fila.getDireccionLatitud(),
                        fila.getDireccionLongitud());
        return Orden.reconstituir(
                fila.getId(),
                fila.getSolicitudId(),
                fila.getRespuestaId(),
                fila.getClienteId(),
                new Orden.TiendaDeOrden(
                        fila.getTiendaId(), fila.getDuenoTiendaId(), fila.getTiendaNombre(), fila.getTiendaTelefono()),
                fila.getProducto(),
                fila.getCantidad(),
                fila.getPrecioUnitarioPesos(),
                fila.getCostoDomicilioPesos(),
                ModalidadEntrega.valueOf(fila.getModalidad()),
                MedioPagoOrden.valueOf(fila.getMedioPago()),
                new ContactoCliente(fila.getClienteNombre(), fila.getClienteTelefono()),
                direccion,
                EstadoOrden.valueOf(fila.getEstado()),
                fila.getCreadoEn(),
                historial.stream()
                        .map(cambio ->
                                new CambioEstado(EstadoOrden.valueOf(cambio.getEstado()), cambio.getCambiadoEn()))
                        .toList());
    }
}
