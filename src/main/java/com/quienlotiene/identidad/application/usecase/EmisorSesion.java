package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.domain.model.TokenRefresco;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.model.ValorTokenOpaco;
import com.quienlotiene.identidad.domain.port.ConfiguracionSesion;
import com.quienlotiene.identidad.domain.port.GeneradorTokenAcceso;
import com.quienlotiene.identidad.domain.port.TokenRefrescoRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Arma una sesión nueva: token de acceso firmado y token de refresco guardado como hash. */
@Component
class EmisorSesion {

    private final GeneradorTokenAcceso generadorTokenAcceso;
    private final TokenRefrescoRepository tokenRefrescoRepository;
    private final ConfiguracionSesion configuracionSesion;

    EmisorSesion(
            GeneradorTokenAcceso generadorTokenAcceso,
            TokenRefrescoRepository tokenRefrescoRepository,
            ConfiguracionSesion configuracionSesion) {
        this.generadorTokenAcceso = generadorTokenAcceso;
        this.tokenRefrescoRepository = tokenRefrescoRepository;
        this.configuracionSesion = configuracionSesion;
    }

    SesionIniciada emitirPara(Usuario usuario, Instant ahora) {
        var tokenAcceso = generadorTokenAcceso.generar(usuario.getId(), usuario.getRol());
        var valorRefresco = ValorTokenOpaco.generar();
        var tokenRefresco = TokenRefresco.emitir(
                UUID.randomUUID(), usuario.getId(), valorRefresco, configuracionSesion.duracionTokenRefresco(), ahora);
        tokenRefrescoRepository.guardar(tokenRefresco);
        return new SesionIniciada(
                usuario.getId(),
                usuario.getRol().name(),
                tokenAcceso.valor(),
                tokenAcceso.fechaExpiracion(),
                valorRefresco.valor(),
                tokenRefresco.getFechaExpiracion());
    }
}
