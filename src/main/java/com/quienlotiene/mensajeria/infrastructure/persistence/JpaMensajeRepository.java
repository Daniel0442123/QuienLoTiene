package com.quienlotiene.mensajeria.infrastructure.persistence;

import com.quienlotiene.mensajeria.domain.model.Mensaje;
import com.quienlotiene.mensajeria.domain.port.MensajeRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link MensajeRepository}. */
@Component
class JpaMensajeRepository implements MensajeRepository {

    private final MensajeJpaRepository mensajeJpaRepository;

    JpaMensajeRepository(MensajeJpaRepository mensajeJpaRepository) {
        this.mensajeJpaRepository = mensajeJpaRepository;
    }

    @Override
    public void guardar(Mensaje mensaje) {
        mensajeJpaRepository.save(new MensajeEntity(
                mensaje.id(),
                mensaje.ordenId(),
                mensaje.autorId(),
                mensaje.autorEsCliente(),
                mensaje.texto(),
                mensaje.fecha()));
    }

    @Override
    public List<Mensaje> listarPorOrden(UUID ordenId) {
        return mensajeJpaRepository.findByOrdenIdOrderByCreadoEnAscIdAsc(ordenId).stream()
                .map(fila -> new Mensaje(
                        fila.getId(),
                        fila.getOrdenId(),
                        fila.getAutorId(),
                        fila.isAutorEsCliente(),
                        fila.getTexto(),
                        fila.getCreadoEn()))
                .toList();
    }
}
