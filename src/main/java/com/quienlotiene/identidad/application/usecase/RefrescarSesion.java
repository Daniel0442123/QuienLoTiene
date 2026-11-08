package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.domain.model.SesionVencidaException;
import com.quienlotiene.identidad.domain.model.ValorTokenRefresco;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-003 Renovar la sesión con el token de refresco de la cookie. El token se rota: el usado queda revocado y se
 * entrega uno nuevo. Si llega un token ya revocado, alguien lo copió: se cierran todas las sesiones del usuario.
 */
@Slf4j
@Service
public class RefrescarSesion {

    private final TokenRefrescoRepository tokenRefrescoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmisorSesion emisorSesion;
    private final Clock reloj;

    RefrescarSesion(
            TokenRefrescoRepository tokenRefrescoRepository,
            UsuarioRepository usuarioRepository,
            EmisorSesion emisorSesion,
            Clock reloj) {
        this.tokenRefrescoRepository = tokenRefrescoRepository;
        this.usuarioRepository = usuarioRepository;
        this.emisorSesion = emisorSesion;
        this.reloj = reloj;
    }

    /**
     * La transacción no se revierte con {@link SesionVencidaException}: si se detecta reuso, la revocación de todas
     * las sesiones debe quedar guardada aunque la respuesta sea un error.
     */
    @Transactional(noRollbackFor = SesionVencidaException.class)
    public SesionIniciada ejecutar(String tokenRefrescoRecibido) {
        if (tokenRefrescoRecibido == null || tokenRefrescoRecibido.isBlank()) {
            throw new SesionVencidaException();
        }
        var ahora = reloj.instant();
        var token = tokenRefrescoRepository
                .buscarPorHash(new ValorTokenRefresco(tokenRefrescoRecibido).calcularHash())
                .orElseThrow(SesionVencidaException::new);

        if (token.estaRevocado() || !tokenRefrescoRepository.revocarSiSigueActivo(token.getId(), ahora)) {
            log.warn(
                    "Reuso de un token de refresco revocado del usuario {}: se cierran sus sesiones",
                    token.getUsuarioId());
            tokenRefrescoRepository.revocarTodosDe(token.getUsuarioId(), ahora);
            throw new SesionVencidaException();
        }
        if (token.estaVencido(ahora)) {
            throw new SesionVencidaException();
        }
        var usuario = usuarioRepository
                .buscarPorId(token.getUsuarioId())
                .filter(usuarioEncontrado -> usuarioEncontrado.estaActiva())
                .orElseThrow(SesionVencidaException::new);

        return emisorSesion.emitirPara(usuario, ahora);
    }
}
