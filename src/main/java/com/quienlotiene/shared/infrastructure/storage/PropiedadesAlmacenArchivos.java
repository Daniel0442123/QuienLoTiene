package com.quienlotiene.shared.infrastructure.storage;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @param directorio carpeta del disco donde se guardan los archivos; en el VPS va fuera de la carpeta de la aplicación
 *     y con permisos solo para el usuario del servicio ({@code QLT_ARCHIVOS_DIRECTORIO})
 */
@Validated
@ConfigurationProperties("quienlotiene.archivos")
public record PropiedadesAlmacenArchivos(@NotBlank String directorio) {}
