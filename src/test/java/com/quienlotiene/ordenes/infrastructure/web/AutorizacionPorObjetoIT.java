package com.quienlotiene.ordenes.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * NF-SEG-007 (RNF-007): autorización por objeto. Una tienda que no fue elegida y otro cliente intentan cada endpoint
 * que expone datos de una orden o una solicitud ajena. Ninguno recibe 2xx ni ve el nombre o el celular del cliente.
 * El mismo recorrido comprueba que el acceso de la tienda elegida queda en la auditoría (NF-SEG-013). Las
 * direcciones del cliente se prueban en {@code DireccionesIT#otroClienteNoVeLaDireccion} y el detalle público de la
 * tienda en {@code DetalleTiendaIT}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("NF-SEG-007")
@DisplayName("NF-SEG-007 Autorización por objeto en los endpoints con datos personales")
class AutorizacionPorObjetoIT {

    private static final double LATITUD_CLIENTE = 2.4448;
    private static final double LONGITUD_CLIENTE = -76.6147;
    private static final double GRADOS_POR_KILOMETRO = 1 / 111.2;
    private static final String NOMBRE_CLIENTE = "Carla Ruiz";
    private static final String CELULAR_CLIENTE = "3157776655";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JdbcClient jdbcClient;

    private record Usuario(UUID id, String token) {}

    private Usuario cliente(String nombre, String celular) {
        var id = UUID.randomUUID();
        var ahora = Timestamp.from(Instant.now());
        jdbcClient
                .sql("""
                        insert into identidad.usuario
                            (id, nombre, correo, telefono, hash_contrasena, rol, estado, creado_en, actualizado_en)
                        values (:id, :nombre, :correo, :telefono, 'sin-uso', 'CLIENTE', 'ACTIVA', :ahora, :ahora)
                        """)
                .param("id", id)
                .param("nombre", nombre)
                .param("correo", id + "@prueba.co")
                .param("telefono", celular)
                .param("ahora", ahora)
                .update();
        return new Usuario(id, emisorTokenAcceso.emitir(id, List.of("CLIENTE")).valor());
    }

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

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder peticion, String cuerpo) {
        return peticion.contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static String idDe(String cuerpo) {
        return cuerpo.replaceAll("^\\{\"id\":\"([^\"]+)\".*", "$1");
    }

    /** El intento se niega con 403 o 404 y la respuesta no trae el nombre ni el celular del cliente. */
    private void negado(String quien, Usuario usuario, MockHttpServletRequestBuilder peticion) throws Exception {
        var respuesta = mockMvc.perform(como(usuario, peticion)).andReturn().getResponse();
        var cuerpo = respuesta.getContentAsString(StandardCharsets.UTF_8);
        assertThat(respuesta.getStatus())
                .as(
                        "%s en %s %s",
                        quien,
                        peticion.buildRequest(null).getMethod(),
                        peticion.buildRequest(null).getRequestURI())
                .isIn(403, 404);
        assertThat(cuerpo).doesNotContain(NOMBRE_CLIENTE).doesNotContain(CELULAR_CLIENTE);
    }

    @Test
    @Tag("NF-SEG-013")
    @ExtendWith(OutputCaptureExtension.class)
    @DisplayName("La tienda no elegida y otro cliente reciben 403 o 404 en la orden, el chat, WhatsApp, la reseña, "
            + "el reporte y la solicitud ajena, sin datos del cliente")
    void nadieVeDatosAjenos(CapturedOutput salida) throws Exception {
        // Dado: el cliente publica, dos farmacias responden Sí y elige la primera
        var farmacias = UUID.randomUUID();
        var cliente = cliente(NOMBRE_CLIENTE, CELULAR_CLIENTE);
        var otroCliente = cliente("Otro Cliente", "3000000001");
        var tiendaElegida = tiendaA(1, farmacias);
        var tiendaNoElegida = tiendaA(2, farmacias);
        var solicitudId = idDe(mockMvc.perform(json(como(cliente, post("/api/v1/solicitudes")), """
                                {"producto": "Ibuprofeno 400 mg", "cantidad": 1, "categoriaId": "%s",
                                 "modalidad": "RECOGER", "ubicacion": {"latitud": %s, "longitud": %s}}
                                """.formatted(
                                farmacias, LATITUD_CLIENTE, LONGITUD_CLIENTE)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
        var siLoTengo = "{\"tipo\": \"SI\", \"precioEnPesos\": 5000, \"cantidadDisponible\": 1}";
        mockMvc.perform(json(como(tiendaElegida, post("/api/v1/solicitudes/{id}/respuestas", solicitudId)), siLoTengo))
                .andExpect(status().isCreated());
        mockMvc.perform(json(
                        como(tiendaNoElegida, post("/api/v1/solicitudes/{id}/respuestas", solicitudId)), siLoTengo))
                .andExpect(status().isCreated());
        var respuestas = mockMvc.perform(como(cliente, get("/api/v1/solicitudes/{id}/respuestas", solicitudId)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        var respuestaElegida = jdbcClient
                .sql("""
                        select id from solicitudes.respuesta
                        where solicitud_id = :solicitudId and dueno_id = :duenoId
                        """)
                .param("solicitudId", UUID.fromString(solicitudId))
                .param("duenoId", tiendaElegida.id())
                .query(UUID.class)
                .single();
        assertThat(respuestas).contains(respuestaElegida.toString());
        var ordenId =
                idDe(mockMvc.perform(json(como(cliente, post("/api/v1/ordenes")), """
                                {"respuestaId": "%s", "modalidad": "RECOGER", "medioPago": "EFECTIVO"}
                                """.formatted(respuestaElegida)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());

        // La tienda elegida sí ve al cliente (control positivo)
        mockMvc.perform(como(tiendaElegida, get("/api/v1/ordenes/{id}", ordenId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.nombre").value(NOMBRE_CLIENTE));
        mockMvc.perform(como(tiendaElegida, get("/api/v1/ordenes/{id}/whatsapp", ordenId)))
                .andExpect(status().isOk());

        // NF-SEG-013: ese acceso a los datos del cliente queda en la auditoría, con ids y sin el nombre ni el celular
        assertThat(salida.getOut())
                .contains("La tienda del usuario " + tiendaElegida.id() + " vio los datos del cliente de la orden "
                        + ordenId)
                .contains("La tienda del usuario " + tiendaElegida.id() + " pidió el contacto del cliente de la orden "
                        + ordenId);
        var lineasDeAuditoria = salida.getOut()
                .lines()
                .filter(linea -> linea.contains("auditoria"))
                .toList();
        assertThat(lineasDeAuditoria)
                .noneMatch(linea -> linea.contains(NOMBRE_CLIENTE) || linea.contains(CELULAR_CLIENTE));

        // Entonces: cada endpoint con datos de la orden se niega a la tienda no elegida y al otro cliente
        for (var intruso : List.of(tiendaNoElegida, otroCliente)) {
            var quien = intruso == tiendaNoElegida ? "Tienda no elegida" : "Otro cliente";
            negado(quien, intruso, get("/api/v1/ordenes/{id}", ordenId));
            negado(quien, intruso, get("/api/v1/ordenes/{id}/mensajes", ordenId));
            negado(quien, intruso, json(post("/api/v1/ordenes/{id}/mensajes", ordenId), "{\"texto\": \"Hola\"}"));
            negado(quien, intruso, get("/api/v1/ordenes/{id}/whatsapp", ordenId));
            negado(quien, intruso, get("/api/v1/ordenes/{id}/resena", ordenId));
            negado(
                    quien,
                    intruso,
                    json(post("/api/v1/ordenes/{id}/reportes", ordenId), "{\"motivo\": \"DEMORA_ENTREGA\"}"));
        }
        negado(
                "Tienda no elegida",
                tiendaNoElegida,
                json(patch("/api/v1/ordenes/{id}/estado", ordenId), "{\"estado\": \"PREPARANDO\"}"));
        negado(
                "Otro cliente",
                otroCliente,
                json(post("/api/v1/ordenes/{id}/cancelacion", ordenId), "{\"motivo\": \"Ya no lo necesito\"}"));
        negado("Otro cliente", otroCliente, post("/api/v1/ordenes/{id}/recepcion", ordenId));
        negado("Otro cliente", otroCliente, json(post("/api/v1/ordenes/{id}/resena", ordenId), "{\"estrellas\": 1}"));
        negado("Otro cliente", otroCliente, get("/api/v1/solicitudes/{id}", solicitudId));
        negado("Otro cliente", otroCliente, get("/api/v1/solicitudes/{id}/respuestas", solicitudId));
        negado(
                "Otro cliente",
                otroCliente,
                json(post("/api/v1/solicitudes/{id}/repeticion", solicitudId), """
                {"ubicacion": {"latitud": %s, "longitud": %s}}
                """.formatted(
                                LATITUD_CLIENTE, LONGITUD_CLIENTE)));

        // Y la orden sigue intacta para su dueño
        mockMvc.perform(como(cliente, get("/api/v1/ordenes/{id}", ordenId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }
}
