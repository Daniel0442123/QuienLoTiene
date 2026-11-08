package com.quienlotiene.shared.infrastructure.security;

import java.util.UUID;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Da el id del usuario que hizo la petición, sacado del token. Los controladores lo pasan al caso de uso para
 * comprobar que el recurso le pertenece (autorización por objeto, riesgo R-03).
 */
@Component
public class UsuarioActual {

    public UUID id() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion instanceof JwtAuthenticationToken autenticacionJwt) {
            return UUID.fromString(autenticacionJwt.getName());
        }
        throw new AuthenticationCredentialsNotFoundException("La petición no tiene una sesión válida");
    }
}
