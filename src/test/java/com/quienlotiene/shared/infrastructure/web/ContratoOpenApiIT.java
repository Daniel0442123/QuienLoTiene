package com.quienlotiene.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * DEF-006 (C-04): el contrato que publica {@code /v3/api-docs} sirve para generar los tipos del frontend. Cada caso
 * corresponde a una fila de la sección A de {@code docs/pruebas/informes/C1/C-04-DIFERENCIAS-TIPOS.md}.
 */
@SpringBootTest(properties = "springdoc.api-docs.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("DEF-006")
@Tag("NF-MAN-024")
@DisplayName("DEF-006 Contrato OpenAPI para generar los tipos del frontend")
class ContratoOpenApiIT {

    private static final String REFERENCIA = "#/components/schemas/";

    @Autowired
    MockMvc mockMvc;

    private JsonNode contrato;

    @BeforeEach
    void leerContrato() throws Exception {
        var cuerpo = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        contrato = JsonMapper.builder().build().readTree(cuerpo);
    }

    @Test
    @DisplayName("A1 Toda operación declara su respuesta exitosa")
    void todaOperacionTieneRespuestaExitosa() {
        // Cuando
        var sinExito = operaciones().stream()
                .filter(operacion ->
                        operacion.respuestas().propertyNames().stream().noneMatch(codigo -> codigo.startsWith("2")))
                .map(Operacion::nombre)
                .toList();

        // Entonces
        assertThat(sinExito).isEmpty();
        var miTienda = contrato.at("/paths/~1api~1v1~1tiendas~1me/get/responses/200/content/application~1json/schema");
        assertThat(miTienda.path("$ref").asString()).isEqualTo(REFERENCIA + "MiTienda");
    }

    @Test
    @DisplayName("A2 Los éxitos salen como application/json y los errores como ProblemDetail")
    void tiposDeContenido() {
        // Cuando
        var conTodos = new ArrayList<String>();
        var erroresMalDescritos = new ArrayList<String>();
        for (var operacion : operaciones()) {
            for (var codigo : operacion.respuestas().propertyNames()) {
                var contenido = operacion.respuestas().path(codigo).path("content");
                if (contenido.has("*/*")) {
                    conTodos.add(operacion.nombre() + " " + codigo);
                }
                var esError = codigo.startsWith("4") || codigo.startsWith("5");
                var referencia =
                        contenido.at("/application~1problem+json/schema/$ref").asString("");
                if (esError && (contenido.size() != 1 || !referencia.equals(REFERENCIA + "ProblemDetail"))) {
                    erroresMalDescritos.add(operacion.nombre() + " " + codigo);
                }
            }
        }

        // Entonces
        assertThat(conTodos).isEmpty();
        assertThat(erroresMalDescritos).isEmpty();
        var problema = esquema("ProblemDetail");
        assertThat(problema.path("properties").propertyNames())
                .contains("type", "title", "status", "detail", "instance", "codigo", "errores", "idCorrelacion");
    }

    @Test
    @DisplayName("A3 y A6 Los estados de la orden y los demás enums del dominio salen con nombre")
    void enumsConNombre() {
        // Entonces
        assertThat(contrato.path("components").path("schemas").propertyNames())
                .contains(
                        "Modalidad",
                        "MedioPago",
                        "TipoRespuesta",
                        "EstadoSolicitud",
                        "EstadoRespuesta",
                        "EstadoOrden",
                        "EstadoTienda",
                        "DiaSemana");
        var orden = esquema("OrdenResponse").path("properties");
        assertThat(orden.at("/estado/$ref").asString()).isEqualTo(REFERENCIA + "EstadoOrden");
        assertThat(orden.at("/medioPago/$ref").asString()).isEqualTo(REFERENCIA + "MedioPago");
        assertThat(esquema("CambioEstadoOrden").at("/properties/estado/$ref").asString())
                .isEqualTo(REFERENCIA + "EstadoOrden");
        assertThat(esquema("RespuestaDeTiendaResponse")
                        .at("/properties/estado/$ref")
                        .asString())
                .isEqualTo(REFERENCIA + "EstadoRespuesta");
        assertThat(esquema("Franja").at("/properties/diaSemana/$ref").asString())
                .isEqualTo(REFERENCIA + "DiaSemana");
        // Un subconjunto no es el enum: la tienda solo avanza a algunos estados
        assertThat(esquema("CambiarEstadoOrdenRequest").at("/properties/estado/enum"))
                .hasSize(4);
    }

    @Test
    @DisplayName("A4 Los eventos del flujo SSE se publican con los nombres del contrato")
    void eventosTiempoReal() {
        // Cuando
        var flujo = contrato.at("/paths/~1api~1v1~1eventos/get/responses/200/content/text~1event-stream/schema/oneOf");

        // Entonces
        assertThat(flujo.valueStream().map(evento -> evento.path("$ref").asString()))
                .containsExactly(
                        REFERENCIA + "EventoSolicitudNueva",
                        REFERENCIA + "EventoRespuestaRecibida",
                        REFERENCIA + "EventoSolicitudCerrada",
                        REFERENCIA + "EventoRespuestaNoElegida",
                        REFERENCIA + "EventoSolicitudVencida",
                        REFERENCIA + "EventoOrdenCreada",
                        REFERENCIA + "EventoEstadoOrdenCambiado",
                        REFERENCIA + "EventoMensajeRecibido");
        assertThat(esquema("EventoSolicitudNueva")
                        .path("required")
                        .valueStream()
                        .map(JsonNode::asString))
                .contains("solicitudId", "producto", "esUrgente");
        assertThat(esquema("EventoEstadoOrdenCambiado")
                        .at("/properties/estado/$ref")
                        .asString())
                .isEqualTo(REFERENCIA + "EstadoOrden");
    }

    @Test
    @DisplayName("A5 Una dirección guardada siempre trae su id")
    void direccionConId() {
        // Cuando
        var direccion = esquema("DatosDireccion");

        // Entonces
        assertThat(direccion.path("required").valueStream().map(JsonNode::asString))
                .contains("id", "fechaCreacion");
        assertThat(direccion.at("/properties/id/type").asString()).isEqualTo("string");
        assertThat(direccion.at("/properties/id").toString()).doesNotContain("null");
    }

    @Test
    @Tag("DEF-007")
    @Tag("CP-TIE-012-01")
    @DisplayName("DEF-007 La respuesta para el cliente trae los medios de pago que acepta la tienda")
    void mediosPagoAceptados() {
        // Cuando
        var tarjeta = esquema("RespuestaParaClienteResponse");

        // Entonces
        assertThat(tarjeta.path("required").valueStream().map(JsonNode::asString))
                .contains("mediosPagoAceptados");
        assertThat(tarjeta.at("/properties/mediosPagoAceptados/items/$ref").asString())
                .isEqualTo(REFERENCIA + "MedioPago");
    }

    @Test
    @Tag("HU-014")
    @DisplayName("Sprint 3 Los medios de pago del detalle público de la tienda salen como lista")
    void mediosPagoDeLaTiendaComoLista() {
        // Cuando
        var mediosPago = esquema("TiendaPublicaResponse").at("/properties/mediosPago");

        // Entonces
        assertThat(mediosPago.path("type").asString()).isEqualTo("array");
        assertThat(mediosPago.at("/items/$ref").asString()).isEqualTo(REFERENCIA + "MedioPago");
    }

    @Test
    @Tag("HU-035")
    @Tag("HU-037")
    @DisplayName("Sprint 3 El motivo del reporte es un enum y la bandeja trae el nombre y el estado de la tienda")
    void reportesTipados() {
        // Cuando
        var motivoPedido = esquema("ReportarRequest").at("/properties/motivo");
        var enPanel = esquema("ReporteParaAdministrador").path("properties");

        // Entonces
        assertThat(motivoPedido.toString()).contains("DEMORA_ENTREGA", "PRODUCTO_DIFERENTE", "COBRO_DIFERENTE", "OTRO");
        assertThat(enPanel.propertyNames()).contains("nombreTienda", "estadoTienda", "reportesDeLaTienda");
    }

    private JsonNode esquema(String nombre) {
        var esquema = contrato.path("components").path("schemas").path(nombre);
        assertThat(esquema.isMissingNode()).as("esquema %s", nombre).isFalse();
        return esquema;
    }

    private List<Operacion> operaciones() {
        var operaciones = new ArrayList<Operacion>();
        var rutas = contrato.path("paths");
        for (var ruta : rutas.propertyNames()) {
            for (var metodo : rutas.path(ruta).propertyNames()) {
                var operacion = rutas.path(ruta).path(metodo);
                if (operacion.has("responses")) {
                    operaciones.add(new Operacion(metodo + " " + ruta, operacion.path("responses")));
                }
            }
        }
        return operaciones;
    }

    private record Operacion(String nombre, JsonNode respuestas) {}
}
