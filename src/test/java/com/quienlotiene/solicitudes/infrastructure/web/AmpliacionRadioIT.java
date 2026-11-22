package com.quienlotiene.solicitudes.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import com.quienlotiene.solicitudes.api.RadioAmpliadoEvent;
import com.quienlotiene.solicitudes.application.usecase.AmpliarRadioSolicitudes;
import java.sql.Timestamp;
import java.time.Duration;
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
 * HU-022 con PostGIS (CU-31, diagrama 35): la consulta {@code FOR UPDATE SKIP LOCKED} toma solo las solicitudes sin
 * respuestas que cumplieron la espera, y {@code ST_DWithin} con 5 km encuentra las tiendas nuevas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@RecordApplicationEvents
@Tag("HU-022")
@DisplayName("HU-022 Ampliar el radio de búsqueda con PostGIS")
class AmpliacionRadioIT {

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

    @Autowired
    AmpliarRadioSolicitudes ampliarRadioSolicitudes;

    /** Inserta una tienda aprobada y abierta al norte del cliente, a {@code kilometros} de distancia. */
    private UUID tiendaA(double kilometros, UUID categoriaId) {
        var tiendaId = UUID.randomUUID();
        jdbcClient
                .sql("""
                        insert into tiendas.tienda
                            (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                             actualizado_en)
                        values (:id, :duenoId, :categoriaId, 'Tienda de prueba', '3001234567',
                                st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                'APROBADA', true, :ahora, :ahora)
                        """)
                .param("id", tiendaId)
                .param("duenoId", UUID.randomUUID())
                .param("categoriaId", categoriaId)
                .param("longitud", LONGITUD_CLIENTE)
                .param("latitud", LATITUD_CLIENTE + kilometros * GRADOS_POR_KILOMETRO)
                .param("ahora", Timestamp.from(Instant.now()))
                .update();
        return tiendaId;
    }

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

    /** Mueve la publicación hacia atrás, como si hubieran pasado esos minutos. */
    private void publicadaHace(UUID solicitudId, int minutos) {
        jdbcClient
                .sql("update solicitudes.solicitud set creado_en = :antes where id = :id")
                .param("antes", Timestamp.from(Instant.now().minus(Duration.ofMinutes(minutos))))
                .param("id", solicitudId)
                .update();
    }

    private int radioDe(UUID solicitudId) {
        return jdbcClient
                .sql("select radio_metros from solicitudes.solicitud where id = :id")
                .param("id", solicitudId)
                .query(Integer.class)
                .single();
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
    @Tag("CP-SOL-022-01")
    @DisplayName("CP-SOL-022-01 Sin respuestas a los 11 minutos pasa a 5 km, llega a la tienda de 4 km y solo una vez")
    void ampliaUnaSolaVezYLlegaALaTiendaNueva() throws Exception {
        // Dado: farmacias a 1 km y 4 km; al publicar con 3 km solo llega a la primera
        var farmacias = UUID.randomUUID();
        var aUnKilometro = tiendaA(1, farmacias);
        var aCuatroKilometros = tiendaA(4, farmacias);
        var solicitudId = publicarParacetamol(farmacias, 1);
        publicadaHace(solicitudId, 11);

        // Cuando
        ampliarRadioSolicitudes.ejecutar();
        ampliarRadioSolicitudes.ejecutar();

        // Entonces
        assertThat(radioDe(solicitudId)).isEqualTo(5_000);
        assertThat(tiendasQueRecibieron(solicitudId)).containsExactly(aUnKilometro, aCuatroKilometros);
        assertThat(eventos.stream(RadioAmpliadoEvent.class))
                .filteredOn(evento -> evento.solicitudId().equals(solicitudId))
                .singleElement()
                .satisfies(evento -> {
                    assertThat(evento.radioEnMetros()).isEqualTo(5_000);
                    assertThat(evento.tiendasNuevas()).isEqualTo(1);
                });
    }

    @Test
    @Tag("CP-SOL-022-02")
    @DisplayName("CP-SOL-022-02 Con una respuesta, o antes de los 10 minutos, el radio no cambia")
    void noAmpliaConRespuestasNiAntesDeTiempo() throws Exception {
        // Dado
        var farmacias = UUID.randomUUID();
        var tienda = tiendaA(1, farmacias);
        tiendaA(4, farmacias);
        var conRespuesta = publicarParacetamol(farmacias, 1);
        publicadaHace(conRespuesta, 11);
        jdbcClient
                .sql("""
                        insert into solicitudes.respuesta
                            (id, solicitud_id, tienda_id, dueno_id, tipo, creado_en, actualizado_en)
                        values (:id, :solicitudId, :tiendaId, :duenoId, 'NO', :ahora, :ahora)
                        """)
                .param("id", UUID.randomUUID())
                .param("solicitudId", conRespuesta)
                .param("tiendaId", tienda)
                .param("duenoId", UUID.randomUUID())
                .param("ahora", Timestamp.from(Instant.now()))
                .update();
        var reciente = publicarParacetamol(farmacias, 1);
        publicadaHace(reciente, 9);

        // Cuando
        ampliarRadioSolicitudes.ejecutar();

        // Entonces
        assertThat(radioDe(conRespuesta)).isEqualTo(3_000);
        assertThat(radioDe(reciente)).isEqualTo(3_000);
        assertThat(tiendasQueRecibieron(conRespuesta)).containsExactly(tienda);
        assertThat(eventos.stream(RadioAmpliadoEvent.class))
                .noneMatch(evento -> evento.solicitudId().equals(conRespuesta)
                        || evento.solicitudId().equals(reciente));
    }
}
