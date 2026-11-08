package com.quienlotiene.clientes.infrastructure.persistence;

import com.quienlotiene.clientes.domain.model.Direccion;
import com.quienlotiene.clientes.domain.model.DireccionNoEncontradaException;
import com.quienlotiene.clientes.domain.port.DireccionRepository;
import com.quienlotiene.shared.domain.Ubicacion;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link DireccionRepository}. */
@Component
class JpaDireccionRepository implements DireccionRepository {

    private final DireccionJpaRepository direccionJpaRepository;
    private final EntityManager entityManager;
    private final Clock reloj;

    JpaDireccionRepository(DireccionJpaRepository direccionJpaRepository, EntityManager entityManager, Clock reloj) {
        this.direccionJpaRepository = direccionJpaRepository;
        this.entityManager = entityManager;
        this.reloj = reloj;
    }

    @Override
    public List<Direccion> listarDe(UUID clienteId) {
        return direccionJpaRepository.findByClienteIdOrderByCreadoEnAsc(clienteId).stream()
                .map(JpaDireccionRepository::aDominio)
                .toList();
    }

    @Override
    public long contarDe(UUID clienteId) {
        return direccionJpaRepository.countByClienteId(clienteId);
    }

    @Override
    public Optional<Direccion> buscarPorId(UUID direccionId) {
        return direccionJpaRepository.findById(direccionId).map(JpaDireccionRepository::aDominio);
    }

    @Override
    public void guardarNueva(Direccion direccion) {
        var fila = new DireccionEntity(direccion.getId(), direccion.getClienteId(), direccion.getFechaCreacion());
        copiar(direccion, fila);
        entityManager.persist(fila);
    }

    @Override
    public void guardarCambios(Direccion direccion) {
        var fila = direccionJpaRepository.findById(direccion.getId()).orElseThrow(DireccionNoEncontradaException::new);
        copiar(direccion, fila);
    }

    @Override
    public void borrar(UUID direccionId) {
        direccionJpaRepository.deleteById(direccionId);
    }

    private void copiar(Direccion direccion, DireccionEntity fila) {
        fila.copiarDatos(
                direccion.getNombre(),
                direccion.getUbicacion().latitud(),
                direccion.getUbicacion().longitud(),
                direccion.getDireccionEscrita(),
                direccion.getIndicaciones(),
                direccion.getTelefono(),
                reloj.instant());
    }

    private static Direccion aDominio(DireccionEntity fila) {
        return Direccion.reconstituir(
                fila.getId(),
                fila.getClienteId(),
                fila.getNombre(),
                new Ubicacion(fila.latitud(), fila.longitud()),
                fila.getDireccionEscrita(),
                fila.getIndicaciones(),
                fila.getTelefono(),
                fila.getCreadoEn());
    }
}
