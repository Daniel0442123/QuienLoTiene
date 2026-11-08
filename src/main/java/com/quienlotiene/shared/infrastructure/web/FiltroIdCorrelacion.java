package com.quienlotiene.shared.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Asigna un id de correlación a cada petición (DECISIONES-BACKEND, sección 14): lo toma de la cabecera
 * {@value #CABECERA} si viene con un formato seguro o genera uno nuevo, lo deja en el MDC para que salga en cada
 * línea de log y lo devuelve en la respuesta. Corre antes que Spring Security para que los 401 y 403 también lo
 * lleven.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroIdCorrelacion extends OncePerRequestFilter {

    public static final String CABECERA = "X-Correlacion-Id";
    public static final String CLAVE_MDC = "idCorrelacion";

    /** Solo letras, dígitos y guiones: evita que un valor enviado por el cliente inyecte líneas falsas en los logs. */
    private static final Pattern FORMATO_PERMITIDO = Pattern.compile("[A-Za-z0-9-]{8,64}");

    @Override
    protected void doFilterInternal(
            HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadenaFiltros)
            throws ServletException, IOException {
        var idCorrelacion = idRecibidoOGenerado(peticion.getHeader(CABECERA));
        MDC.put(CLAVE_MDC, idCorrelacion);
        respuesta.setHeader(CABECERA, idCorrelacion);
        try {
            cadenaFiltros.doFilter(peticion, respuesta);
        } finally {
            MDC.remove(CLAVE_MDC);
        }
    }

    private static String idRecibidoOGenerado(String idRecibido) {
        if (idRecibido != null && FORMATO_PERMITIDO.matcher(idRecibido).matches()) {
            return idRecibido;
        }
        return UUID.randomUUID().toString();
    }
}
