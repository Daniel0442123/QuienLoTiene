package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.api.CuentasApi;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link CuentasApi} (HU-037): suspender una cuenta cierra todas sus sesiones. El token de acceso que ya
 * tenga vence solo (15 minutos, RNF-008); los refrescos se rechazan desde ya.
 */
@Slf4j
@Service
class GestionarEstadoCuenta implements CuentasApi {

    private final UsuarioRepository usuarioRepository;
    private final TokenRefrescoRepository tokenRefrescoRepository;
    private final Clock reloj;

    GestionarEstadoCuenta(
            UsuarioRepository usuarioRepository, TokenRefrescoRepository tokenRefrescoRepository, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRefrescoRepository = tokenRefrescoRepository;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void suspender(UUID usuarioId) {
        usuarioRepository.buscarPorId(usuarioId).ifPresent(usuario -> {
            usuario.suspender();
            usuarioRepository.guardarEstado(usuario);
            tokenRefrescoRepository.revocarTodosDe(usuarioId, reloj.instant());
            log.info("Cuenta {} suspendida", usuarioId);
        });
    }

    @Override
    @Transactional
    public void reactivar(UUID usuarioId) {
        usuarioRepository.buscarPorId(usuarioId).ifPresent(usuario -> {
            usuario.reactivar();
            usuarioRepository.guardarEstado(usuario);
            log.info("Cuenta {} reactivada", usuarioId);
        });
    }
}
