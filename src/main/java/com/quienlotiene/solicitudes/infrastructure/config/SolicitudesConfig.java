package com.quienlotiene.solicitudes.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(PropiedadesSolicitudes.class)
class SolicitudesConfig {}
