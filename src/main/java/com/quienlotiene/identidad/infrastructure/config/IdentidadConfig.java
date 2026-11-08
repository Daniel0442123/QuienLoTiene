package com.quienlotiene.identidad.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({PropiedadesIdentidad.class, PropiedadesAdministradorInicial.class})
class IdentidadConfig {}
