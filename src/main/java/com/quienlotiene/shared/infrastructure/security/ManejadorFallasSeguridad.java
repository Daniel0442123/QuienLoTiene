package com.quienlotiene.shared.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Las fallas de seguridad ocurren en los filtros, antes de llegar a un controlador. Este manejador las entrega al
 * {@code ManejadorGlobalErrores} para que el 401 y el 403 tengan el mismo ProblemDetail que cualquier otro error
 * (GEN-003 y GEN-004).
 */
@Component
public class ManejadorFallasSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver resolutorExcepciones;

    public ManejadorFallasSeguridad(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolutorExcepciones) {
        this.resolutorExcepciones = resolutorExcepciones;
    }

    @Override
    public void commence(
            HttpServletRequest peticion, HttpServletResponse respuesta, AuthenticationException excepcion) {
        respuesta.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        resolutorExcepciones.resolveException(peticion, respuesta, null, excepcion);
    }

    @Override
    public void handle(HttpServletRequest peticion, HttpServletResponse respuesta, AccessDeniedException excepcion) {
        resolutorExcepciones.resolveException(peticion, respuesta, null, excepcion);
    }
}
