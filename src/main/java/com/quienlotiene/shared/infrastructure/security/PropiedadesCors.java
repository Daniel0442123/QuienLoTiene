package com.quienlotiene.shared.infrastructure.security;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Orígenes del frontend que pueden llamar a la API desde el navegador (DECISIONES-BACKEND, sección 11).
 *
 * @param origenesPermitidos por ejemplo {@code http://localhost:5173} en desarrollo
 */
@Validated
@ConfigurationProperties("quienlotiene.seguridad.cors")
public record PropiedadesCors(@NotEmpty List<String> origenesPermitidos) {}
