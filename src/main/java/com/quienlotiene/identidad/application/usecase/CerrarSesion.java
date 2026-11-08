package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import java.time.Clock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-003 Cerrar sesión (CU-02, pasos 12 a 15): revoca el token de refresco. Es idempotente: cerrar una sesión que ya
 * no existe no es un error. El token de acceso sigue siendo válido hasta que venza (máximo 15 minutos).
 */
@Slf4j
@Service
public class CerrarSesion {

    private final TokenRefrescoRepository tokenRefrescoRepository;
    private final Clock reloj;

    CerrarSesion(TokenRefrescoRepository tokenRefrescoRepository, Clock reloj) {
        this.tokenRefrescoRepository = tokenRefrescoRepository;
        this.reloj = reloj;
    }

    @Transactional
    public void ejecutar(String tokenRefrescoRecibido) {
        if (tokenRefrescoRecibido == null || tokenRefrescoRecibido.isBlank()) {
            return;
        }
        tokenRefrescoRepository
                .buscarPorHash(new ValorTokenOpaco(tokenRefrescoRecibido).calcularHash())
                .ifPresent(token -> {
                    tokenRefrescoRepository.revocarSiSigueActivo(token.getId(), reloj.instant());
                    log.info("Usuario {} cerró sesión", token.getUsuarioId());
                });
    }
}
