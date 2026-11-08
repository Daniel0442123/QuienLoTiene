package com.quienlotiene.solicitudes.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-016 de punta a punta: token de cliente, validación, PostGIS y límite por hora contra la base real. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-016")
@DisplayName("HU-016 Publicar solicitud por la API")
class SolicitudControllerIT {

    private static final String CUERPO_PARACETAMOL = """
            {"producto": "Paracetamol 500 mg", "cantidad": 1, "categoriaId": "%s", "modalidad": "RECOGER",
             "ubicacion": {"latitud": 2.4448, "longitud": -76.6147}}
            """.formatted(UUID.randomUUID());

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JdbcClient jdbcClient;

    private ResultActions publicarComo(UUID clienteId, String rol, String cuerpo) throws Exception {
        var token = emisorTokenAcceso.emitir(clienteId, List.of(rol)).valor();
        return mockMvc.perform(post("/api/v1/solicitudes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
    }

    @Test
    @Tag("CP-SOL-016-01")
    @DisplayName("CP-SOL-016-01 Queda ABIERTA con vencimiento a 30 minutos y la ubicación se guarda en PostGIS")
    void publicaLaSolicitud() throws Exception {
        // Dado
        var clienteId = UUID.randomUUID();

        // Cuando
        var respuesta = publicarComo(clienteId, "CLIENTE", CUERPO_PARACETAMOL)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/solicitudes/")))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.radioEnMetros").value(3000))
                .andExpect(jsonPath("$.ubicacion.latitud").value(2.4448))
                .andExpect(jsonPath("$.esUrgente").value(false))
                .andReturn();

        // Entonces
        var fila = jdbcClient.sql("""
                        select st_y(ubicacion::geometry) as latitud, st_x(ubicacion::geometry) as longitud,
                               extract(epoch from (vence_en - creado_en)) as segundos_vigencia
                        from solicitudes.solicitud where cliente_id = :clienteId
                        """).param("clienteId", clienteId).query().singleRow();
        assertThat(((Number) fila.get("latitud")).doubleValue()).isEqualTo(2.4448);
        assertThat(((Number) fila.get("longitud")).doubleValue()).isEqualTo(-76.6147);
        assertThat(((Number) fila.get("segundos_vigencia")).longValue()).isEqualTo(30 * 60);
        assertThat(respuesta.getResponse().getContentAsString()).doesNotContain(clienteId.toString());
    }

    @Test
    @Tag("CP-SOL-016-02")
    @DisplayName("CP-SOL-016-02 Sin producto ni ubicación responde 400 e indica los datos faltantes")
    void rechazaLaSolicitudIncompleta() throws Exception {
        var cuerpoIncompleto = """
                {"producto": "", "cantidad": 1, "categoriaId": "%s", "modalidad": "RECOGER"}
                """.formatted(UUID.randomUUID());

        publicarComo(UUID.randomUUID(), "CLIENTE", cuerpoIncompleto)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-001"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'producto')]").exists())
                .andExpect(jsonPath("$.errores[?(@.campo == 'ubicacion')]").exists());
    }

    @Test
    @Tag("CP-SOL-016-03")
    @DisplayName("CP-SOL-016-03 Con 10 en la última hora responde 429 con Retry-After")
    void aplicaElLimitePorHora() throws Exception {
        // Dado: un cliente que publicó 10 solicitudes; la más vieja hace 48 minutos
        var clienteId = UUID.randomUUID();
        for (int numero = 0; numero < 10; numero++) {
            publicarComo(clienteId, "CLIENTE", CUERPO_PARACETAMOL).andExpect(status().isCreated());
        }
        var haceCuarentaYOchoMinutos = Instant.now().minus(Duration.ofMinutes(48));
        jdbcClient
                .sql("""
                        update solicitudes.solicitud set creado_en = :fecha
                        where id = (select id from solicitudes.solicitud where cliente_id = :clienteId
                                    order by creado_en limit 1)
                        """)
                .param("fecha", Timestamp.from(haceCuarentaYOchoMinutos))
                .param("clienteId", clienteId)
                .update();

        // Cuando / Entonces: la undécima se rechaza y otro cliente sí puede publicar
        var respuesta = publicarComo(clienteId, "CLIENTE", CUERPO_PARACETAMOL)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.codigo").value("SOL-001"))
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andReturn();
        var segundosDeEspera = Long.parseLong(respuesta.getResponse().getHeader(HttpHeaders.RETRY_AFTER));
        assertThat(segundosDeEspera).isBetween(1L, 60L * 60);
        publicarComo(UUID.randomUUID(), "CLIENTE", CUERPO_PARACETAMOL).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Una tienda no puede publicar solicitudes y sin token responde 401")
    void soloLosClientesPublican() throws Exception {
        publicarComo(UUID.randomUUID(), "TIENDA", CUERPO_PARACETAMOL)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-004"));
        mockMvc.perform(post("/api/v1/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_PARACETAMOL))
                .andExpect(status().isUnauthorized());
    }
}
