package com.quienlotiene.identidad.infrastructure.config;

import com.quienlotiene.identidad.domain.port.PoliticaDatos;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración del módulo identidad.
 *
 * @param versionPoliticaDatos versión vigente de la política de tratamiento de datos; al publicar una nueva se cambia
 *     aquí y los registros con la versión anterior se rechazan (IDE-007)
 */
@Validated
@ConfigurationProperties("quienlotiene.identidad")
public record PropiedadesIdentidad(@NotBlank String versionPoliticaDatos) implements PoliticaDatos {

    @Override
    public String versionVigente() {
        return versionPoliticaDatos;
    }
}
