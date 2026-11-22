package com.quienlotiene.identidad.infrastructure.config;

import com.quienlotiene.identidad.domain.model.IntentosInicioSesion;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({PropiedadesIdentidad.class, PropiedadesAdministradorInicial.class})
class IdentidadConfig {

    /** Una sola ventana de intentos para toda la API (NF-SEG-009). */
    @Bean
    IntentosInicioSesion intentosInicioSesion() {
        return new IntentosInicioSesion();
    }
}
