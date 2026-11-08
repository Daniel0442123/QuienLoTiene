package com.quienlotiene.solicitudes.infrastructure.config;

import com.quienlotiene.solicitudes.domain.port.ParametrosSolicitudes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@code quienlotiene.solicitudes.*} en {@code application.yml}.
 *
 * @param vigencia cuánto tiempo recibe respuestas una solicitud (30 minutos)
 * @param maximoPorHora solicitudes por cliente en una hora (10)
 * @param radioInicialEnMetros radio de difusión al publicar (3 000 metros)
 */
@Validated
@ConfigurationProperties("quienlotiene.solicitudes")
public record PropiedadesSolicitudes(
        @NotNull Duration vigencia,
        @Positive int maximoPorHora,
        @Positive int radioInicialEnMetros) implements ParametrosSolicitudes {}
