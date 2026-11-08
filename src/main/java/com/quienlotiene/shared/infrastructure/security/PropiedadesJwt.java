package com.quienlotiene.shared.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración de los tokens de acceso (D-15, DECISIONES-BACKEND sección 11). Las llaves salen de las variables de
 * entorno {@code QLT_JWT_LLAVE_PRIVADA} y {@code QLT_JWT_LLAVE_PUBLICA}.
 *
 * @param emisor valor del claim {@code iss}; los tokens con otro emisor se rechazan
 * @param vigenciaTokenAcceso cuánto dura un token de acceso (15 minutos)
 * @param llavePrivada llave privada RSA en PEM PKCS#8, en una sola línea
 * @param llavePublica llave pública RSA en PEM X.509, en una sola línea
 * @param kid identificador de la llave, para poder rotarla sin invalidar todo de golpe
 * @param permitirLlavesTemporales si faltan las llaves, genera unas en memoria (solo dev, test y demo)
 */
@Validated
@ConfigurationProperties("quienlotiene.seguridad.jwt")
public record PropiedadesJwt(
        @NotBlank String emisor,
        @NotNull Duration vigenciaTokenAcceso,
        String llavePrivada,
        String llavePublica,
        @NotBlank String kid,
        boolean permitirLlavesTemporales) {}
