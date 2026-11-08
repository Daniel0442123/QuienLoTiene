package com.quienlotiene.shared.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj único de la aplicación, en UTC (D-14). Los casos de uso y las tareas programadas lo reciben inyectado en vez
 * de llamar a {@code Instant.now()}, para que las pruebas de vencimiento usen un reloj fijo (D-19).
 */
@Configuration(proxyBeanMethods = false)
public class RelojConfig {

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
