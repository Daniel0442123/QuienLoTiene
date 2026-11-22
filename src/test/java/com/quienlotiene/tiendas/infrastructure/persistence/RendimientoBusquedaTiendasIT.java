package com.quienlotiene.tiendas.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.tiendas.api.TiendasApi;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * NF-REN-003 (RNF-003): la búsqueda de tiendas elegibles por radio responde en 200 ms o menos con 1.000 tiendas y usa
 * el índice GiST {@code ix_tienda_ubicacion}. Las tiendas quedan repartidas en un cuadrado de unos 20 km alrededor del
 * cliente; el tiempo se mide sobre la consulta nativa de la búsqueda, después de una vuelta de calentamiento.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("NF-REN-003")
@DisplayName("NF-REN-003 Búsqueda geoespacial de tiendas con 1.000 tiendas")
class RendimientoBusquedaTiendasIT {

    private static final double LATITUD_CLIENTE = 2.4448;
    private static final double LONGITUD_CLIENTE = -76.6147;
    private static final double GRADOS_POR_KILOMETRO = 1 / 111.2;
    private static final int TIENDAS = 1_000;
    private static final int TRES_KILOMETROS = 3_000;
    private static final Duration LIMITE = Duration.ofMillis(200);
    private static final Instant LUNES_DIEZ_DE_LA_MANANA = Instant.parse("2026-11-09T10:00:00-05:00");

    @Autowired
    TiendasApi tiendasApi;

    @Autowired
    JdbcClient jdbcClient;

    /** Inserta las tiendas en una cuadrícula de 32 x 32 con 600 m entre una y otra (unos 19 km de lado). */
    private UUID sembrarTiendas() {
        var farmacias = UUID.randomUUID();
        var ahora = Timestamp.from(LUNES_DIEZ_DE_LA_MANANA);
        for (int i = 0; i < TIENDAS; i++) {
            var fila = i / 32 - 16;
            var columna = i % 32 - 16;
            jdbcClient
                    .sql("""
                            insert into tiendas.tienda
                                (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                                 actualizado_en)
                            values (:id, :duenoId, :categoriaId, :nombre, '3001234567',
                                    st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                    'APROBADA', true, :ahora, :ahora)
                            """)
                    .param("id", UUID.randomUUID())
                    .param("duenoId", UUID.randomUUID())
                    .param("categoriaId", farmacias)
                    .param("nombre", "Tienda " + i)
                    .param("longitud", LONGITUD_CLIENTE + columna * 0.6 * GRADOS_POR_KILOMETRO)
                    .param("latitud", LATITUD_CLIENTE + fila * 0.6 * GRADOS_POR_KILOMETRO)
                    .param("ahora", ahora)
                    .update();
        }
        jdbcClient.sql("analyze tiendas.tienda").update();
        return farmacias;
    }

    /** La consulta de {@code JpaTiendaRepository#buscarCandidatasCerca}, con la distancia y el orden. */
    private static final String CONSULTA = """
            select t.id,
                   st_distance(t.ubicacion, st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography)
                       as distancia_metros
            from tiendas.tienda t
            where t.estado = 'APROBADA'
              and t.abierta = true
              and t.categoria_id = :categoriaId
              and st_dwithin(t.ubicacion,
                             st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                             :radio)
            order by distancia_metros
            """;

    private int consultar(UUID categoriaId) {
        return jdbcClient
                .sql(CONSULTA)
                .param("categoriaId", categoriaId)
                .param("longitud", LONGITUD_CLIENTE)
                .param("latitud", LATITUD_CLIENTE)
                .param("radio", TRES_KILOMETROS)
                .query((fila, numero) -> fila.getObject("id", UUID.class))
                .list()
                .size();
    }

    @Test
    @DisplayName("Con 1.000 tiendas la consulta geoespacial en 3 km tarda 200 ms o menos")
    void respondeATiempo() {
        // Dado
        var farmacias = sembrarTiendas();
        consultar(farmacias);

        // Cuando: cinco consultas, se toma la más lenta
        var tiempos = new ArrayList<Duration>();
        var encontradas = 0;
        for (int vuelta = 0; vuelta < 5; vuelta++) {
            var inicio = System.nanoTime();
            encontradas = consultar(farmacias);
            tiempos.add(Duration.ofNanos(System.nanoTime() - inicio));
        }
        var masLenta = tiempos.stream().max(Duration::compareTo).orElseThrow();

        // Entonces: las tiendas a 3 km o menos (unas 75 de la cuadrícula) y la más lenta en 200 ms o menos
        assertThat(encontradas).isBetween(60, 100);
        assertThat(masLenta).isLessThanOrEqualTo(LIMITE);

        // Referencia para el informe: la búsqueda completa también carga cada tienda (horario, medios de pago)
        var inicio = System.nanoTime();
        var elegibles = tiendasApi.buscarElegibles(
                farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE, TRES_KILOMETROS, LUNES_DIEZ_DE_LA_MANANA);
        System.out.printf(
                "NF-REN-003: consulta %d ms (la más lenta de 5); búsqueda completa con %d tiendas %d ms%n",
                masLenta.toMillis(),
                elegibles.size(),
                Duration.ofNanos(System.nanoTime() - inicio).toMillis());
    }

    @Test
    @DisplayName("El plan de la consulta usa el índice GiST de la ubicación")
    void usaElIndiceGist() {
        // Dado
        var farmacias = sembrarTiendas();

        // Cuando: el mismo filtro espacial que JpaTiendaRepository#buscarCandidatasCerca
        var plan = String.join(
                "\n",
                jdbcClient
                        .sql("""
                        explain select t.id
                        from tiendas.tienda t
                        where t.estado = 'APROBADA'
                          and t.abierta = true
                          and t.categoria_id = :categoriaId
                          and st_dwithin(t.ubicacion,
                                         st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                         :radio)
                        """)
                        .param("categoriaId", farmacias)
                        .param("longitud", LONGITUD_CLIENTE)
                        .param("latitud", LATITUD_CLIENTE)
                        .param("radio", TRES_KILOMETROS)
                        .query(String.class)
                        .list());

        // Entonces
        assertThat(plan).contains("ix_tienda_ubicacion");
    }
}
