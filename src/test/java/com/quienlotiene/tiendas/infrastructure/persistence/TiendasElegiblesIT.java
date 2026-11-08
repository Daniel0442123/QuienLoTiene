package com.quienlotiene.tiendas.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.tiendas.api.TiendaElegible;
import com.quienlotiene.tiendas.api.TiendasApi;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.sql.Timestamp;
import java.time.Instant;
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
 * Búsqueda de tiendas elegibles con PostGIS real (D-23: toda consulta nativa lleva su prueba de integración). Los
 * casos son los que usará la difusión de HU-017: radio de 3 km, categoría, estado, apertura y horario.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-013")
@DisplayName("HU-013 TiendasApi: tiendas elegibles por radio con PostGIS")
class TiendasElegiblesIT {

    /** Centro de Popayán; un grado de latitud son unos 111,2 km. */
    private static final double LATITUD_CLIENTE = 2.4448;

    private static final double LONGITUD_CLIENTE = -76.6147;
    private static final double GRADOS_POR_KILOMETRO = 1 / 111.2;
    private static final int TRES_KILOMETROS = 3_000;
    private static final Instant LUNES_DIEZ_DE_LA_MANANA = Instant.parse("2026-11-09T10:00:00-05:00");

    @Autowired
    TiendasApi tiendasApi;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    TiendaRepository tiendaRepository;

    /** Inserta una tienda al norte del cliente, a {@code kilometros} de distancia. */
    private UUID tiendaA(double kilometros, UUID categoriaId, String estado, boolean abierta) {
        var tiendaId = UUID.randomUUID();
        jdbcClient
                .sql("""
                        insert into tiendas.tienda
                            (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                             actualizado_en)
                        values (:id, :duenoId, :categoriaId, :nombre, '3001234567',
                                st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                :estado, :abierta, :ahora, :ahora)
                        """)
                .param("id", tiendaId)
                .param("duenoId", UUID.randomUUID())
                .param("categoriaId", categoriaId)
                .param("nombre", "Tienda a " + kilometros + " km")
                .param("longitud", LONGITUD_CLIENTE)
                .param("latitud", LATITUD_CLIENTE + kilometros * GRADOS_POR_KILOMETRO)
                .param("estado", estado)
                .param("abierta", abierta)
                .param("ahora", Timestamp.from(LUNES_DIEZ_DE_LA_MANANA))
                .update();
        return tiendaId;
    }

    private void horarioDeLunes(UUID tiendaId, String abre, String cierra) {
        jdbcClient
                .sql("""
                        insert into tiendas.horario (id, tienda_id, dia_semana, abre, cierra, creado_en, actualizado_en)
                        values (:id, :tiendaId, 1, cast(:abre as time), cast(:cierra as time), now(), now())
                        """)
                .param("id", UUID.randomUUID())
                .param("tiendaId", tiendaId)
                .param("abre", abre)
                .param("cierra", cierra)
                .update();
    }

    @Test
    @DisplayName("Solo las farmacias aprobadas y abiertas a 1 km y 2 km reciben; la de 5 km no (radio de 3 km)")
    void filtraPorRadio() {
        // Dado
        var farmacias = UUID.randomUUID();
        var aUnKilometro = tiendaA(1, farmacias, "APROBADA", true);
        var aDosKilometros = tiendaA(2, farmacias, "APROBADA", true);
        tiendaA(5, farmacias, "APROBADA", true);

        // Cuando
        var elegibles = tiendasApi.buscarElegibles(
                farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE, TRES_KILOMETROS, LUNES_DIEZ_DE_LA_MANANA);

        // Entonces: de la más cercana a la más lejana, con la distancia en metros
        assertThat(elegibles).extracting(TiendaElegible::tiendaId).containsExactly(aUnKilometro, aDosKilometros);
        assertThat(elegibles.get(0).distanciaEnMetros()).isBetween(975, 1025);
        assertThat(elegibles.get(1).distanciaEnMetros()).isBetween(1975, 2025);
    }

    @Test
    @DisplayName("Deja fuera otra categoría, una tienda suspendida, una pendiente, una cerrada y una fuera de horario")
    void filtraPorCategoriaEstadoAperturaYHorario() {
        // Dado
        var farmacias = UUID.randomUUID();
        var elegible = tiendaA(0.5, farmacias, "APROBADA", true);
        tiendaA(0.5, UUID.randomUUID(), "APROBADA", true);
        tiendaA(1, farmacias, "SUSPENDIDA", true);
        tiendaA(1, farmacias, "PENDIENTE", true);
        tiendaA(1, farmacias, "APROBADA", false);
        var fueraDeHorario = tiendaA(1, farmacias, "APROBADA", true);
        horarioDeLunes(fueraDeHorario, "14:00", "20:00");

        // Cuando
        var elegibles = tiendasApi.buscarElegibles(
                farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE, TRES_KILOMETROS, LUNES_DIEZ_DE_LA_MANANA);

        // Entonces
        // El horario se lee en hora de Colombia, sin desfase por la zona horaria de la JVM
        var tiendaFueraDeHorario = tiendaRepository.buscarPorId(fueraDeHorario).orElseThrow();
        assertThat(tiendaFueraDeHorario.getHorario().franjas().get(0).abre()).hasToString("14:00");
        assertThat(elegibles).extracting(TiendaElegible::tiendaId).containsExactly(elegible);
    }

    @Test
    @DisplayName("Con radio de 5 km (radio ampliado) entra la tienda a 4,5 km")
    void elRadioAmpliadoIncluyeTiendasMasLejanas() {
        var farmacias = UUID.randomUUID();
        var aCuatroYMedio = tiendaA(4.5, farmacias, "APROBADA", true);

        assertThat(tiendasApi.buscarElegibles(
                        farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE, TRES_KILOMETROS, LUNES_DIEZ_DE_LA_MANANA))
                .isEmpty();
        assertThat(tiendasApi.buscarElegibles(
                        farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE, 5_000, LUNES_DIEZ_DE_LA_MANANA))
                .extracting(TiendaElegible::tiendaId)
                .containsExactly(aCuatroYMedio);
    }
}
