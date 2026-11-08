package com.quienlotiene.solicitudes.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * HU-017 de punta a punta con PostGIS (casos de integración del plan C1): el cliente publica por la API y la solicitud
 * llega solo a las tiendas elegibles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@RecordApplicationEvents
@Tag("HU-017")
@DisplayName("HU-017 Difundir a tiendas elegibles por la API")
class DifusionSolicitudIT {

    private static final double LATITUD_CLIENTE = 2.4448;
    private static final double LONGITUD_CLIENTE = -76.6147;
    private static final double GRADOS_POR_KILOMETRO = 1 / 111.2;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    ApplicationEvents eventos;

    /** Inserta una tienda al norte del cliente, a {@code kilometros} de distancia. */
    private UUID tiendaA(double kilometros, UUID categoriaId, String estado) {
        var tiendaId = UUID.randomUUID();
        jdbcClient
                .sql("""
                        insert into tiendas.tienda
                            (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                             actualizado_en)
                        values (:id, :duenoId, :categoriaId, 'Tienda de prueba', '3001234567',
                                st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                :estado, true, :ahora, :ahora)
                        """)
                .param("id", tiendaId)
                .param("duenoId", UUID.randomUUID())
                .param("categoriaId", categoriaId)
                .param("longitud", LONGITUD_CLIENTE)
                .param("latitud", LATITUD_CLIENTE + kilometros * GRADOS_POR_KILOMETRO)
                .param("estado", estado)
                .param("ahora", Timestamp.from(Instant.now()))
                .update();
        return tiendaId;
    }

    /** Publica "Paracetamol 500 mg" de la categoría como cliente y devuelve el id de la solicitud. */
    private UUID publicarParacetamol(UUID categoriaId, int tiendasEsperadas) throws Exception {
        var token =
                emisorTokenAcceso.emitir(UUID.randomUUID(), List.of("CLIENTE")).valor();
        var respuesta = mockMvc.perform(post("/api/v1/solicitudes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"producto": "Paracetamol 500 mg", "cantidad": 1, "categoriaId": "%s",
                                 "modalidad": "RECOGER", "ubicacion": {"latitud": %s, "longitud": %s}}
                                """.formatted(categoriaId, LATITUD_CLIENTE, LONGITUD_CLIENTE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tiendasNotificadas").value(tiendasEsperadas))
                .andReturn();
        var cuerpo = respuesta.getResponse().getContentAsString();
        return UUID.fromString(cuerpo.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
    }

    private List<UUID> tiendasQueRecibieron(UUID solicitudId) {
        return jdbcClient
                .sql(
                        "select tienda_id from solicitudes.envio_solicitud where solicitud_id = :id order by distancia_metros")
                .param("id", solicitudId)
                .query(UUID.class)
                .list();
    }

    @Test
    @Tag("CP-SOL-017-01")
    @DisplayName("CP-SOL-017-01 Con farmacias a 1, 2 y 5 km solo las de 1 km y 2 km la reciben")
    void difundeSoloDentroDelRadio() throws Exception {
        // Dado
        var farmacias = UUID.randomUUID();
        var aUnKilometro = tiendaA(1, farmacias, "APROBADA");
        var aDosKilometros = tiendaA(2, farmacias, "APROBADA");
        tiendaA(5, farmacias, "APROBADA");

        // Cuando
        var solicitudId = publicarParacetamol(farmacias, 2);

        // Entonces
        assertThat(tiendasQueRecibieron(solicitudId)).containsExactly(aUnKilometro, aDosKilometros);
        assertThat(eventos.stream(SolicitudDifundidaEvent.class))
                .filteredOn(evento -> evento.solicitudId().equals(solicitudId))
                .singleElement()
                .satisfies(evento -> assertThat(evento.destinatarios()).hasSize(2));
    }

    @Test
    @Tag("CP-SOL-017-02")
    @DisplayName("CP-SOL-017-02 Una ferretería abierta a 500 m no recibe una solicitud de Farmacias")
    void noDifundeAOtraCategoria() throws Exception {
        var farmacias = UUID.randomUUID();
        tiendaA(0.5, UUID.randomUUID(), "APROBADA");

        var solicitudId = publicarParacetamol(farmacias, 0);

        assertThat(tiendasQueRecibieron(solicitudId)).isEmpty();
    }

    @Test
    @Tag("CP-SOL-017-03")
    @DisplayName("CP-SOL-017-03 Una farmacia suspendida a 1 km no la recibe")
    void noDifundeAUnaTiendaSuspendida() throws Exception {
        var farmacias = UUID.randomUUID();
        tiendaA(1, farmacias, "SUSPENDIDA");
        var aprobada = tiendaA(1.5, farmacias, "APROBADA");

        var solicitudId = publicarParacetamol(farmacias, 1);

        assertThat(tiendasQueRecibieron(solicitudId)).containsExactly(aprobada);
    }
}
