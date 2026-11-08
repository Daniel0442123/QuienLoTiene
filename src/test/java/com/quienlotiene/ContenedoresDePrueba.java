package com.quienlotiene;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * PostgreSQL + PostGIS real para las pruebas de integración ({@code *IT}). Se importa con
 * {@code @Import(ContenedoresDePrueba.class)}; como todas las pruebas usan la misma configuración, Spring reutiliza el
 * contexto y el contenedor arranca una sola vez por ejecución.
 */
@TestConfiguration(proxyBeanMethods = false)
public class ContenedoresDePrueba {

    @Bean
    @ServiceConnection
    PostgreSQLContainer baseDeDatos() {
        return new PostgreSQLContainer(
                DockerImageName.parse("postgis/postgis:17-3.5-alpine").asCompatibleSubstituteFor("postgres"));
    }
}
