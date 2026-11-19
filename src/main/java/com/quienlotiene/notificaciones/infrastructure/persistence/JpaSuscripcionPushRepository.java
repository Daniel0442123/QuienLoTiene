package com.quienlotiene.notificaciones.infrastructure.persistence;

import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import com.quienlotiene.notificaciones.domain.port.SuscripcionPushRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Adaptador JPA del puerto {@link SuscripcionPushRepository}. */
@Component
class JpaSuscripcionPushRepository implements SuscripcionPushRepository {

    private final SuscripcionPushJpaRepository suscripcionPushJpaRepository;

    JpaSuscripcionPushRepository(SuscripcionPushJpaRepository suscripcionPushJpaRepository) {
        this.suscripcionPushJpaRepository = suscripcionPushJpaRepository;
    }

    @Override
    @Transactional
    public void guardar(SuscripcionPush suscripcion) {
        // Un navegador tiene un solo endpoint: si otra cuenta inició sesión en él, la suscripción pasa a la nueva
        suscripcionPushJpaRepository.borrarPorEndpoint(suscripcion.endpoint());
        suscripcionPushJpaRepository.save(new SuscripcionPushEntity(
                suscripcion.id(),
                suscripcion.usuarioId(),
                suscripcion.endpoint(),
                suscripcion.p256dh(),
                suscripcion.auth(),
                suscripcion.fecha()));
    }

    @Override
    @Transactional
    public void eliminar(UUID usuarioId, String endpoint) {
        suscripcionPushJpaRepository.borrarDelUsuario(usuarioId, endpoint);
    }

    @Override
    @Transactional
    public void eliminar(UUID suscripcionId) {
        suscripcionPushJpaRepository.deleteById(suscripcionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuscripcionPush> listarPorUsuario(UUID usuarioId) {
        return suscripcionPushJpaRepository.findByUsuarioId(usuarioId).stream()
                .map(fila -> new SuscripcionPush(
                        fila.getId(),
                        fila.getUsuarioId(),
                        fila.getEndpoint(),
                        fila.getP256dh(),
                        fila.getAuth(),
                        fila.getCreadoEn()))
                .toList();
    }
}
