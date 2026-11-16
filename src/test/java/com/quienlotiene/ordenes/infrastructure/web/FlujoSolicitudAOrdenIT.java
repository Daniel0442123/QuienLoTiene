package com.quienlotiene.ordenes.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.ordenes.api.OrdenCreadaEvent;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import com.quienlotiene.solicitudes.api.RespuestaElegidaEvent;
import com.quienlotiene.solicitudes.application.usecase.VencerSolicitudes;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Sprint 2 de punta a punta con PostGIS: el cliente publica, dos tiendas responden, el cliente elige, la tienda avanza
 * la orden, se contactan por WhatsApp y el cliente confirma la recepción. Cubre los casos de integración de HU-018,
 * HU-019, HU-020, HU-021, HU-024, HU-027, HU-028 y HU-032.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@RecordApplicationEvents
@DisplayName("Sprint 2 De la solicitud a la orden resuelta por la API")
class FlujoSolicitudAOrdenIT {

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
    VencerSolicitudes vencerSolicitudes;

    private record Usuario(UUID id, String token) {}

    private Usuario cliente() {
        var id = UUID.randomUUID();
        var ahora = Timestamp.from(Instant.now());
        jdbcClient
                .sql("""
                        insert into identidad.usuario
                            (id, nombre, correo, telefono, hash_contrasena, rol, estado, creado_en, actualizado_en)
                        values (:id, 'Ana Pérez', :correo, '3109998877', 'sin-uso', 'CLIENTE', 'ACTIVA', :ahora, :ahora)
                        """)
                .param("id", id)
                .param("correo", id + "@prueba.co")
                .param("ahora", ahora)
                .update();
        return new Usuario(id, emisorTokenAcceso.emitir(id, List.of("CLIENTE")).valor());
    }

    /** Tienda aprobada y abierta al norte del cliente que acepta efectivo; devuelve el token de su dueño. */
    private Usuario tiendaA(double kilometros, UUID categoriaId) {
        var tiendaId = UUID.randomUUID();
        var duenoId = UUID.randomUUID();
        var ahora = Timestamp.from(Instant.now());
        jdbcClient
                .sql("""
                        insert into tiendas.tienda
                            (id, dueno_id, categoria_id, nombre, telefono, ubicacion, estado, abierta, creado_en,
                             actualizado_en)
                        values (:id, :duenoId, :categoriaId, 'Droguería de prueba', '3001234567',
                                st_setsrid(st_makepoint(:longitud, :latitud), 4326)::geography,
                                'APROBADA', true, :ahora, :ahora)
                        """)
                .param("id", tiendaId)
                .param("duenoId", duenoId)
                .param("categoriaId", categoriaId)
                .param("longitud", LONGITUD_CLIENTE)
                .param("latitud", LATITUD_CLIENTE + kilometros * GRADOS_POR_KILOMETRO)
                .param("ahora", ahora)
                .update();
        jdbcClient.sql("""
                        insert into tiendas.medio_pago_tienda (tienda_id, medio_pago, creado_en, actualizado_en)
                        values (:id, 'EFECTIVO', :ahora, :ahora)
                        """).param("id", tiendaId).param("ahora", ahora).update();
        return new Usuario(
                duenoId, emisorTokenAcceso.emitir(duenoId, List.of("TIENDA")).valor());
    }

    private static MockHttpServletRequestBuilder como(Usuario usuario, MockHttpServletRequestBuilder peticion) {
        return peticion.header(HttpHeaders.AUTHORIZATION, "Bearer " + usuario.token());
    }

    private static String idDe(String cuerpo) {
        return cuerpo.replaceAll("^\\{\"id\":\"([^\"]+)\".*", "$1");
    }

    private String publicar(Usuario cliente, UUID categoriaId, int tiendasEsperadas) throws Exception {
        var cuerpo = mockMvc.perform(como(cliente, post("/api/v1/solicitudes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"producto": "Paracetamol 500 mg", "cantidad": 2, "categoriaId": "%s",
                                 "modalidad": "RECOGER", "ubicacion": {"latitud": %s, "longitud": %s}}
                                """.formatted(categoriaId, LATITUD_CLIENTE, LONGITUD_CLIENTE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tiendasNotificadas").value(tiendasEsperadas))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return idDe(cuerpo);
    }

    private org.springframework.test.web.servlet.ResultActions responder(
            Usuario tienda, String solicitudId, String json) throws Exception {
        return mockMvc.perform(como(tienda, post("/api/v1/solicitudes/{id}/respuestas", solicitudId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private void cambiarEstado(Usuario tienda, String ordenId, String estado, int esperado) throws Exception {
        mockMvc.perform(como(tienda, patch("/api/v1/ordenes/{id}/estado", ordenId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"%s\"}".formatted(estado)))
                .andExpect(status().is(esperado));
    }

    @Test
    @Tag("HU-019")
    @Tag("HU-024")
    @Tag("HU-027")
    @Tag("HU-028")
    @Tag("HU-032")
    @Tag("CP-SOL-018-01")
    @Tag("CP-SOL-019-04")
    @Tag("CP-ORD-024-02")
    @Tag("CP-ORD-024-03")
    @Tag("CP-ORD-028-01")
    @Tag("CP-MEN-032-01")
    @DisplayName("Una tienda responde Sí, otra No; el cliente elige, la orden avanza y queda resuelta")
    void flujoCompleto() throws Exception {
        // Dado: un cliente y dos farmacias a 1,2 km y 2 km
        var farmacias = UUID.randomUUID();
        var cliente = cliente();
        var tiendaElegida = tiendaA(1.234, farmacias);
        var otraTienda = tiendaA(2, farmacias);
        var solicitudId = publicar(cliente, farmacias, 2);

        // HU-018: la tienda la ve con la distancia redondeada y sin datos del cliente (CP-SOL-018-01)
        mockMvc.perform(como(tiendaElegida, get("/api/v1/solicitudes/disponibles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].distanciaEnMetros".formatted(solicitudId))
                        .value(hasItem(1_200)))
                .andExpect(jsonPath("$[0].clienteId").doesNotExist())
                .andExpect(jsonPath("$[0].latitud").doesNotExist());

        // HU-019: una responde Sí, la otra No; la segunda respuesta de la misma tienda se rechaza
        responder(tiendaElegida, solicitudId, "{\"tipo\": \"SI\", \"precioEnPesos\": 8000, \"cantidadDisponible\": 2}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ESPERANDO"));
        responder(tiendaElegida, solicitudId, "{\"tipo\": \"NO\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SOL-005"));
        responder(otraTienda, solicitudId, "{\"tipo\": \"NO\"}").andExpect(status().isCreated());
        mockMvc.perform(como(otraTienda, get("/api/v1/solicitudes/disponibles")))
                .andExpect(jsonPath("$[*].id").value(not(hasItem(solicitudId))));

        // HU-020: el cliente ve una tarjeta y dos respuestas recibidas
        var cuerpoRespuestas = mockMvc.perform(como(cliente, get("/api/v1/solicitudes/{id}/respuestas", solicitudId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuestasRecibidas").value(2))
                .andExpect(jsonPath("$.respuestas.length()").value(1))
                .andExpect(jsonPath("$.respuestas[0].totalEnPesos").value(16_000))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var respuestaId = cuerpoRespuestas.replaceAll(".*\"respuestas\":\\[\\{\"id\":\"([^\"]+)\".*", "$1");

        // HU-024: el cliente elige y nace la orden CONFIRMADO
        var cuerpoOrden = mockMvc.perform(como(cliente, post("/api/v1/ordenes"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"respuestaId": "%s", "modalidad": "RECOGER", "medioPago": "EFECTIVO"}
                                """.formatted(respuestaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.totalEnPesos").value(16_000))
                .andExpect(jsonPath("$.cliente").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var ordenId = idDe(cuerpoOrden);
        assertThat(eventos.stream(OrdenCreadaEvent.class))
                .anyMatch(e -> e.ordenId().toString().equals(ordenId));
        assertThat(eventos.stream(RespuestaElegidaEvent.class))
                .anyMatch(e -> e.solicitudId().toString().equals(solicitudId));
        mockMvc.perform(como(cliente, get("/api/v1/solicitudes/{id}", solicitudId)))
                .andExpect(jsonPath("$.estado").value("RESUELTA"))
                .andExpect(jsonPath("$.ordenId").value(ordenId));

        // CP-ORD-024-02 y CP-ORD-024-03: solo la tienda elegida ve los datos del cliente
        mockMvc.perform(como(tiendaElegida, get("/api/v1/ordenes/{id}", ordenId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nombre").value("Ana Pérez"));
        mockMvc.perform(como(otraTienda, get("/api/v1/ordenes/{id}", ordenId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ORD-001"));

        // HU-027: no se salta pasos (CP-ORD-027-02) y avanza paso a paso (CP-ORD-027-01)
        cambiarEstado(tiendaElegida, ordenId, "ENTREGADO", 409);
        cambiarEstado(tiendaElegida, ordenId, "PREPARANDO", 200);
        cambiarEstado(tiendaElegida, ordenId, "LISTO_PARA_RECOGER", 200);
        cambiarEstado(tiendaElegida, ordenId, "ENTREGADO", 200);

        // HU-032: el cliente escribe a la tienda por WhatsApp
        mockMvc.perform(como(cliente, get("/api/v1/ordenes/{id}/whatsapp", ordenId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enlace").value(startsWith("https://wa.me/573001234567?text=")));

        // HU-028: el cliente confirma; la tienda deja de ver sus datos y ya no hay WhatsApp
        mockMvc.perform(como(cliente, post("/api/v1/ordenes/{id}/recepcion", ordenId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTA"));
        mockMvc.perform(como(tiendaElegida, get("/api/v1/ordenes/{id}", ordenId)))
                .andExpect(jsonPath("$.cliente").doesNotExist());
        mockMvc.perform(como(tiendaElegida, get("/api/v1/ordenes/{id}/whatsapp", ordenId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("MEN-001"));
        mockMvc.perform(como(tiendaElegida, get("/api/v1/tiendas/me/respuestas")))
                .andExpect(jsonPath("$[?(@.solicitudId == '%s')].estado".formatted(solicitudId))
                        .value(hasItem("ELEGIDA")));
    }

    @Test
    @Tag("HU-021")
    @Tag("CP-SOL-021-01")
    @DisplayName("CP-SOL-021-01 Una solicitud cumplida pasa a VENCIDA y ya no acepta respuestas")
    void venceYRechazaRespuestas() throws Exception {
        // Dado
        var farmacias = UUID.randomUUID();
        var cliente = cliente();
        var tienda = tiendaA(1, farmacias);
        var solicitudId = publicar(cliente, farmacias, 1);
        jdbcClient
                .sql("update solicitudes.solicitud set vence_en = :antes where id = :id")
                .param("antes", Timestamp.from(Instant.now().minus(Duration.ofMinutes(1))))
                .param("id", UUID.fromString(solicitudId))
                .update();

        // Cuando
        vencerSolicitudes.ejecutar();

        // Entonces
        mockMvc.perform(como(cliente, get("/api/v1/solicitudes/{id}", solicitudId)))
                .andExpect(jsonPath("$.estado").value("VENCIDA"));
        responder(tienda, solicitudId, "{\"tipo\": \"NO\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("SOL-004"));
    }
}
