package com.quienlotiene.administracion.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import com.quienlotiene.tiendas.api.TiendaRevisadaEvent;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-036 de punta a punta: bandeja, documento, aprobación y rechazo con auditoría y evento para el dueño. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@RecordApplicationEvents
@Tag("HU-036")
@DisplayName("HU-036 Aprobar o rechazar tiendas por la API")
class AdministracionTiendasIT {

    @TempDir
    static Path directorioArchivos;

    @DynamicPropertySource
    static void almacenTemporal(DynamicPropertyRegistry registro) {
        registro.add("quienlotiene.archivos.directorio", () -> directorioArchivos.toString());
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    ApplicationEvents eventos;

    private final UUID administradorId = UUID.randomUUID();

    private String tokenCon(String rol) {
        var usuario = rol.equals("ADMIN") ? administradorId : UUID.randomUUID();
        return "Bearer " + emisorTokenAcceso.emitir(usuario, List.of(rol)).valor();
    }

    /** Registra una tienda por la API pública y devuelve su id (queda PENDIENTE). */
    private UUID registrarTiendaPendiente() throws Exception {
        var json = """
                {"nombreDueno": "Carlos Muñoz", "correo": "tienda-%s@correo.com", "telefono": "3001234567",
                 "fraseContrasena": "mi tienda vende de todo un poco", "aceptaPoliticaDatos": true,
                 "versionPoliticaDatos": "1.0", "nombreTienda": "Droguería La 5", "categoriaId": "%s",
                 "ubicacion": {"latitud": 2.4448, "longitud": -76.6147}}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
        var respuesta = mockMvc.perform(multipart("/api/v1/tiendas/registro")
                        .file(new MockMultipartFile("datos", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes()))
                        .file(new MockMultipartFile("fotoLocal", "f.jpg", "image/jpeg", ArchivosDePrueba.unJpeg()))
                        .file(new MockMultipartFile("rut", "r.pdf", "application/pdf", ArchivosDePrueba.unPdf())))
                .andExpect(status().isCreated())
                .andReturn();
        var cuerpo = respuesta.getResponse().getContentAsString();
        return UUID.fromString(cuerpo.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
    }

    private ResultActions aprobar(UUID tiendaId, String token) throws Exception {
        return mockMvc.perform(
                post("/api/v1/admin/tiendas/{id}/aprobacion", tiendaId).header(HttpHeaders.AUTHORIZATION, token));
    }

    private long auditoriasDe(UUID tiendaId, String accion) {
        return jdbcClient
                .sql("select count(*) from administracion.auditoria where recurso_id = :tiendaId and accion = :accion"
                        + " and administrador_id = :administradorId")
                .param("tiendaId", tiendaId)
                .param("accion", accion)
                .param("administradorId", administradorId)
                .query(Long.class)
                .single();
    }

    @Test
    @Tag("CP-ADM-036-01")
    @DisplayName("CP-ADM-036-01 La tienda pendiente queda Aprobada, se avisa al dueño y queda auditado")
    void apruebaLaTienda() throws Exception {
        // Dado: una tienda pendiente en la bandeja
        var tiendaId = registrarTiendaPendiente();
        mockMvc.perform(get("/api/v1/admin/tiendas").header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].estado", tiendaId).value("PENDIENTE"))
                .andExpect(jsonPath("$[?(@.id == '%s')].documentos.length()", tiendaId)
                        .value(2));

        // Cuando
        aprobar(tiendaId, tokenCon("ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.fechaRevision").isNotEmpty());

        // Entonces
        var estado = jdbcClient
                .sql("select estado from tiendas.tienda where id = :id")
                .param("id", tiendaId)
                .query(String.class)
                .single();
        assertThat(estado).isEqualTo("APROBADA");
        assertThat(auditoriasDe(tiendaId, "TIENDA_APROBADA")).isEqualTo(1);
        assertThat(eventos.stream(TiendaRevisadaEvent.class)).anySatisfy(evento -> {
            assertThat(evento.tiendaId()).isEqualTo(tiendaId);
            assertThat(evento.estado()).isEqualTo("APROBADA");
        });
        // Aprobar dos veces no se permite
        aprobar(tiendaId, tokenCon("ADMIN"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TIE-004"));
    }

    @Test
    @Tag("CP-ADM-036-02")
    @DisplayName("CP-ADM-036-02 Rechazada con el motivo \"Documento ilegible\"")
    void rechazaLaTiendaConMotivo() throws Exception {
        var tiendaId = registrarTiendaPendiente();

        mockMvc.perform(post("/api/v1/admin/tiendas/{id}/rechazo", tiendaId)
                        .header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo": "Documento ilegible"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"))
                .andExpect(jsonPath("$.motivoRechazo").value("Documento ilegible"));

        assertThat(auditoriasDe(tiendaId, "TIENDA_RECHAZADA")).isEqualTo(1);
        mockMvc.perform(post("/api/v1/admin/tiendas/{id}/rechazo", tiendaId)
                        .header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("motivo"));
    }

    @Test
    @DisplayName("El administrador descarga el documento como adjunto y queda auditado")
    void descargaElDocumento() throws Exception {
        // Dado
        var tiendaId = registrarTiendaPendiente();
        var documentoId = jdbcClient
                .sql("select id from tiendas.documento_verificacion where tienda_id = :id and tipo = 'RUT'")
                .param("id", tiendaId)
                .query(UUID.class)
                .single();

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/admin/tiendas/{id}/documentos/{doc}", tiendaId, documentoId)
                        .header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"rut.pdf\""))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(content().bytes(ArchivosDePrueba.unPdf()));
        assertThat(auditoriasDe(tiendaId, "DOCUMENTO_TIENDA_DESCARGADO")).isEqualTo(1);
        mockMvc.perform(get("/api/v1/admin/tiendas/{id}/documentos/{doc}", tiendaId, UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("TIE-006"));
    }

    @Test
    @DisplayName("Una tienda o un cliente no entran al panel de administración")
    void soloElAdministradorRevisa() throws Exception {
        var tiendaId = registrarTiendaPendiente();

        aprobar(tiendaId, tokenCon("TIENDA")).andExpect(status().isForbidden());
        aprobar(tiendaId, tokenCon("CLIENTE")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/tiendas")).andExpect(status().isUnauthorized());
    }
}
