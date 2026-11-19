package com.quienlotiene.reputacion.infrastructure.persistence;

import com.quienlotiene.reputacion.domain.model.Resena;
import com.quienlotiene.reputacion.domain.model.ResumenReputacion;
import com.quienlotiene.reputacion.domain.port.ResenaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link ResenaRepository}. */
@Component
class JpaResenaRepository implements ResenaRepository {

    private final ResenaJpaRepository resenaJpaRepository;

    JpaResenaRepository(ResenaJpaRepository resenaJpaRepository) {
        this.resenaJpaRepository = resenaJpaRepository;
    }

    @Override
    public void guardar(Resena resena) {
        resenaJpaRepository.save(new ResenaEntity(
                resena.id(),
                resena.ordenId(),
                resena.tiendaId(),
                resena.clienteId(),
                (short) resena.estrellas(),
                resena.comentario(),
                resena.fecha()));
    }

    @Override
    public boolean existeParaOrden(UUID ordenId) {
        return resenaJpaRepository.existsByOrdenId(ordenId);
    }

    @Override
    public Optional<Resena> buscarPorOrden(UUID ordenId) {
        return resenaJpaRepository.findByOrdenId(ordenId).map(JpaResenaRepository::aDominio);
    }

    @Override
    public ResumenReputacion resumenDe(UUID tiendaId) {
        var fila = resenaJpaRepository.resumenDe(tiendaId).getFirst();
        var total = ((Number) fila[1]).longValue();
        if (total == 0 || fila[0] == null) {
            return ResumenReputacion.SIN_RESENAS;
        }
        var promedio = Math.round(((Number) fila[0]).doubleValue() * 10) / 10.0;
        return new ResumenReputacion(promedio, total);
    }

    @Override
    public List<Resena> recientesDe(UUID tiendaId, int maximo) {
        return resenaJpaRepository.findByTiendaIdOrderByCreadoEnDesc(tiendaId, Limit.of(maximo)).stream()
                .map(JpaResenaRepository::aDominio)
                .toList();
    }

    private static Resena aDominio(ResenaEntity fila) {
        return new Resena(
                fila.getId(),
                fila.getOrdenId(),
                fila.getTiendaId(),
                fila.getClienteId(),
                fila.getEstrellas(),
                fila.getComentario(),
                fila.getCreadoEn());
    }
}
