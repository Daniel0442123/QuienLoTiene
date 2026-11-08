package com.quienlotiene.identidad.infrastructure.config;

import com.quienlotiene.identidad.domain.port.ConfiguracionSesion;
import com.quienlotiene.identidad.domain.port.PoliticaDatos;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración del módulo identidad.
 *
 * @param versionPoliticaDatos versión vigente de la política de tratamiento de datos; al publicar una nueva se cambia
 *     aquí y los registros con la versión anterior se rechazan (IDE-007)
 * @param duracionTokenRefresco cuánto dura una sesión sin volver a escribir la frase (7 días)
 */
@Validated
@ConfigurationProperties("quienlotiene.identidad")
public record PropiedadesIdentidad(
        @NotBlank String versionPoliticaDatos, @NotNull Duration duracionTokenRefresco)
        implements PoliticaDatos, ConfiguracionSesion {

    @Override
    public String versionVigente() {
        return versionPoliticaDatos;
    }
}
