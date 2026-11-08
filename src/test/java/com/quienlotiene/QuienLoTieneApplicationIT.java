package com.quienlotiene;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Arranca la aplicación completa contra PostgreSQL + PostGIS real: Flyway aplica las migraciones y Hibernate
 * valida el esquema (ddl-auto=validate). Se salta solo si el equipo no tiene Docker.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class QuienLoTieneApplicationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer baseDeDatos = new PostgreSQLContainer(
            DockerImageName.parse("postgis/postgis:17-3.5-alpine").asCompatibleSubstituteFor("postgres"));

    @Autowired
    JdbcClient jdbcClient;

    @Test
    @DisplayName("La aplicación arranca y la primera migración crea los esquemas y PostGIS")
    void arrancaConLosEsquemasDeLosModulosYPostgis() {
        // Dado: la aplicación arrancó y Flyway aplicó las migraciones

        // Cuando
        var cantidadEsquemasDeModulos = jdbcClient.sql("""
                        select count(*) from information_schema.schemata
                        where schema_name in ('identidad', 'clientes', 'tiendas', 'catalogo', 'solicitudes',
                                              'ordenes', 'mensajeria', 'reputacion', 'notificaciones', 'administracion')
                        """).query(Long.class).single();
        var versionPostgis =
                jdbcClient.sql("select postgis_version()").query(String.class).single();

        // Entonces
        assertThat(cantidadEsquemasDeModulos).isEqualTo(10);
        assertThat(versionPostgis).startsWith("3.5");
    }
}
