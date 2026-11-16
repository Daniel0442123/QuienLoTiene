package com.quienlotiene.shared.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Tareas del reloj del sistema (D-19: CU-29, CU-30, CU-31). Las pruebas de integración las apagan con
 * {@code quienlotiene.tareas-programadas.habilitadas=false} y llaman el caso de uso con un reloj fijo.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnProperty(
        name = "quienlotiene.tareas-programadas.habilitadas",
        havingValue = "true",
        matchIfMissing = true)
public class TareasProgramadasConfig {}
