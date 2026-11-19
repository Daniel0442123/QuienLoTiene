package com.quienlotiene.tiendas.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.shared.infrastructure.security.EmisorTokenAcceso;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-014 de punta a punta: el detalle público de la tienda no expone el RUT ni los documentos. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-014")
@DisplayName("HU-014 Ver detalle de tienda por la API")
class DetalleTiendaIT {

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

    private String tokenCon(String rol) {
        return "Bearer "
                + emisorTokenAcceso.emitir(UUID.randomUUID(), List.of(rol)).valor();
    }

    /** Registra una tienda con foto y RUT por la API pública (queda PENDIENTE). */
    private UUID registrarTienda() throws Exception {
        var json = """
                {"nombreDueno": "Carlos Muñoz", "correo": "detalle-%s@correo.com", "telefono": "3001234567",
                 "fraseContrasena": "mi tienda vende de todo un poco", "aceptaPoliticaDatos": true,
                 "versionPoliticaDatos": "1.0", "nombreTienda": "Droguería La 5", "categoriaId": "%s",
                 "ubicacion": {"latitud": 2.4448, "longitud": -76.6147}}
                """.formatted(UUID.randomUUID(), UUID.randomUUID());
        var cuerpo = mockMvc.perform(multipart("/api/v1/tiendas/registro")
                        .file(new MockMultipartFile("datos", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes()))
                        .file(new MockMultipartFile("fotoLocal", "local.jpg", "image/jpeg", ArchivosDePrueba.unJpeg()))
                        .file(new MockMultipartFile(
                                "rut", "rut-tienda.pdf", "application/pdf", ArchivosDePrueba.unPdf())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(cuerpo.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1"));
    }

    @Test
    @Tag("CP-TIE-014-01")
    @Tag("CP-TIE-014-02")
    @DisplayName(
            "CP-TIE-014-02 El cliente ve nombre, teléfono, ubicación y horario de la tienda, sin RUT ni documentos")
    void detalleSinDatosSensibles() throws Exception {
        // Dado: una tienda registrada con su RUT, pendiente de revisión
        var tiendaId = registrarTienda();
        var cliente = tokenCon("CLIENTE");

        // Mientras no esté aprobada, no existe para el público
        mockMvc.perform(get("/api/v1/tiendas/{id}", tiendaId).header(HttpHeaders.AUTHORIZATION, cliente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("TIE-006"));

        // Cuando: el administrador la aprueba y el cliente abre su detalle
        mockMvc.perform(post("/api/v1/admin/tiendas/{id}/aprobacion", tiendaId)
                        .header(HttpHeaders.AUTHORIZATION, tokenCon("ADMIN")))
                .andExpect(status().isOk());
        var cuerpo = mockMvc.perform(get("/api/v1/tiendas/{id}", tiendaId).header(HttpHeaders.AUTHORIZATION, cliente))
                // Entonces: los datos públicos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Droguería La 5"))
                .andExpect(jsonPath("$.telefono").value("3001234567"))
                .andExpect(jsonPath("$.latitud").value(2.4448))
                .andExpect(jsonPath("$.horario").isArray())
                .andExpect(jsonPath("$.mediosPago").isArray())
                // y nada de lo que sirvió para verificarla ni del dueño
                .andExpect(jsonPath("$.rut").doesNotExist())
                .andExpect(jsonPath("$.documentos").doesNotExist())
                .andExpect(jsonPath("$.duenoId").doesNotExist())
                .andExpect(jsonPath("$.estado").doesNotExist())
                .andExpect(jsonPath("$.motivoRechazo").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();
        var minusculas = cuerpo.toLowerCase(Locale.ROOT);
        assertThat(minusculas)
                .doesNotContain("rut")
                .doesNotContain("documento")
                .doesNotContain("carlos")
                .doesNotContain("@correo.com")
                .doesNotContain(directorioArchivos.toString().toLowerCase(Locale.ROOT));

        // Las reseñas salen aparte y una tienda nueva arranca sin ninguna
        mockMvc.perform(get("/api/v1/tiendas/{id}/resenas", tiendaId).header(HttpHeaders.AUTHORIZATION, cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedio").value(0.0))
                .andExpect(jsonPath("$.totalResenas").value(0))
                .andExpect(jsonPath("$.recientes").isEmpty());
    }

    @Test
    @DisplayName("Sin sesión no se ve el detalle de la tienda")
    void sinSesion() throws Exception {
        mockMvc.perform(get("/api/v1/tiendas/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }
}
