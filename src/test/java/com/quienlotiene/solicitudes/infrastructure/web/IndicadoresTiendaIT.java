package com.quienlotiene.solicitudes.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-015 por la API con PostgreSQL: los conteos salen de envíos y respuestas reales del periodo. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-015")
@DisplayName("HU-015 Ver resumen de indicadores por la API")
class IndicadoresTiendaIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JdbcClient jdbcClient;

    /** Inserta una tienda aprobada del dueño y devuelve su id. */
    private UUID tiendaDe(UUID duenoId) {
        var tiendaId = UUID.randomUUID();
        jdbcClient
                .sql("""
                        insert into tiendas.tienda
                            (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                             actualizado_en)
                        values (:id, :duenoId, :categoriaId, 'Droguería La 5', '3001234567',
                                st_setsrid(st_makepoint(-76.6147, 2.4448), 4326)::geography,
                                'APROBADA', true, :ahora, :ahora)
                        """)
                .param("id", tiendaId)
                .param("duenoId", duenoId)
                .param("categoriaId", UUID.randomUUID())
                .param("ahora", Timestamp.from(Instant.now()))
                .update();
        return tiendaId;
    }

    /**
     * Una solicitud que le llegó a la tienda hace {@code dias} días, con la respuesta del tipo dado (o sin respuesta si
     * es {@code null}); {@code elegida} le pone una orden.
     */
    private void solicitudRecibida(UUID tiendaId, int dias, String tipo, boolean elegida) {
        var cuando = Timestamp.from(Instant.now().minus(Duration.ofDays(dias)));
        var solicitudId = UUID.randomUUID();
        jdbcClient
                .sql("""
                        insert into solicitudes.solicitud
                            (id, cliente_id, categoria_id, producto, cantidad, modalidad, ubicacion, radio_metros,
                             estado, vence_en, creado_en, actualizado_en)
                        values (:id, :clienteId, :categoriaId, 'Paracetamol 500 mg', 1, 'RECOGER',
                                st_setsrid(st_makepoint(-76.6147, 2.4448), 4326)::geography, 3000,
                                'VENCIDA', :cuando, :cuando, :cuando)
                        """)
                .param("id", solicitudId)
                .param("clienteId", UUID.randomUUID())
                .param("categoriaId", UUID.randomUUID())
                .param("cuando", cuando)
                .update();
        jdbcClient
                .sql("""
                        insert into solicitudes.envio_solicitud
                            (id, solicitud_id, tienda_id, distancia_metros, creado_en, actualizado_en)
                        values (:id, :solicitudId, :tiendaId, 800, :cuando, :cuando)
                        """)
                .param("id", UUID.randomUUID())
                .param("solicitudId", solicitudId)
                .param("tiendaId", tiendaId)
                .param("cuando", cuando)
                .update();
        if (tipo == null) {
            return;
        }
        var conProducto = !"NO".equals(tipo);
        jdbcClient
                .sql("""
                        insert into solicitudes.respuesta
                            (id, solicitud_id, tienda_id, dueno_id, tipo, precio_pesos, cantidad_disponible,
                             descripcion_alternativa, orden_id, creado_en, actualizado_en)
                        values (:id, :solicitudId, :tiendaId, :duenoId, :tipo, :precio, :cantidad, :alternativa,
                                :ordenId, :cuando, :cuando)
                        """)
                .param("id", UUID.randomUUID())
                .param("solicitudId", solicitudId)
                .param("tiendaId", tiendaId)
                .param("duenoId", UUID.randomUUID())
                .param("tipo", tipo)
                .param("precio", conProducto ? 7_500L : null)
                .param("cantidad", conProducto ? 1 : null)
                .param("alternativa", "PARECIDO".equals(tipo) ? "Acetaminofén 500 mg" : null)
                .param("ordenId", elegida ? UUID.randomUUID() : null)
                .param("cuando", cuando)
                .update();
    }

    private MockHttpServletRequestBuilder comoTienda(UUID duenoId, String ruta) {
        var token = emisorTokenAcceso.emitir(duenoId, List.of("TIENDA")).valor();
        return get(ruta).header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    @Test
    @Tag("CP-TIE-015-01")
    @DisplayName("CP-TIE-015-01 Con 10 recibidas, 8 respondidas y 3 ganadas en 7 días ve 10, 8, 3, 80 % y 38 %")
    void muestraLosConteosDelPeriodo() throws Exception {
        // Dado: en la semana, 10 solicitudes, 8 respuestas «Sí lo tengo» o parecidas y 3 elegidas; más una de hace 20
        // días
        var dueno = UUID.randomUUID();
        var tienda = tiendaDe(dueno);
        for (var i = 0; i < 3; i++) {
            solicitudRecibida(tienda, 1, "SI", true);
        }
        for (var i = 0; i < 4; i++) {
            solicitudRecibida(tienda, 2, "SI", false);
        }
        solicitudRecibida(tienda, 3, "PARECIDO", false);
        solicitudRecibida(tienda, 4, null, false);
        solicitudRecibida(tienda, 5, null, false);
        solicitudRecibida(tienda, 20, "SI", true);

        // Cuando / Entonces
        mockMvc.perform(comoTienda(dueno, "/api/v1/tiendas/me/indicadores?periodo=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodoEnDias").value(7))
                .andExpect(jsonPath("$.recibidas").value(10))
                .andExpect(jsonPath("$.respondidas").value(8))
                .andExpect(jsonPath("$.respuestasConProducto").value(8))
                .andExpect(jsonPath("$.ganadas").value(3))
                .andExpect(jsonPath("$.porcentajeRespondiste").value(80))
                .andExpect(jsonPath("$.porcentajeTeEligieron").value(38));
        // Con 30 días entra la de hace 20
        mockMvc.perform(comoTienda(dueno, "/api/v1/tiendas/me/indicadores"))
                .andExpect(jsonPath("$.periodoEnDias").value(30))
                .andExpect(jsonPath("$.recibidas").value(11))
                .andExpect(jsonPath("$.ganadas").value(4));
    }

    @Test
    @Tag("CP-TIE-015-02")
    @DisplayName("CP-TIE-015-02 Una tienda nueva ve todo en cero y sin porcentajes, sin errores")
    void tiendaNuevaEnCero() throws Exception {
        var dueno = UUID.randomUUID();
        tiendaDe(dueno);

        mockMvc.perform(comoTienda(dueno, "/api/v1/tiendas/me/indicadores?periodo=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recibidas").value(0))
                .andExpect(jsonPath("$.respondidas").value(0))
                .andExpect(jsonPath("$.ganadas").value(0))
                .andExpect(jsonPath("$.porcentajeRespondiste").isEmpty())
                .andExpect(jsonPath("$.porcentajeTeEligieron").isEmpty());
    }

    @Test
    @DisplayName("Un periodo distinto de 7 o 30 responde GEN-001")
    void rechazaOtroPeriodo() throws Exception {
        var dueno = UUID.randomUUID();
        tiendaDe(dueno);

        mockMvc.perform(comoTienda(dueno, "/api/v1/tiendas/me/indicadores?periodo=15"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-001"));
    }
}
