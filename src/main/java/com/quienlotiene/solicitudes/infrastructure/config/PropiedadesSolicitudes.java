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
 * @param radioAmpliadoEnMetros HU-022: radio de la ampliación automática (5 000 metros)
 * @param esperaParaAmpliar HU-022: minutos sin respuestas antes de ampliar (10 minutos)
 */
@Validated
@ConfigurationProperties("quienlotiene.solicitudes")
public record PropiedadesSolicitudes(
        @NotNull Duration vigencia,
        @Positive int maximoPorHora,
        @Positive int radioInicialEnMetros,
        @Positive int radioAmpliadoEnMetros,
        @NotNull Duration esperaParaAmpliar)
        implements ParametrosSolicitudes {}
