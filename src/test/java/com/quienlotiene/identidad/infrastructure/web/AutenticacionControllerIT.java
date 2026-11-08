package com.quienlotiene.identidad.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

/** HU-001 de punta a punta: petición HTTP real, Spring Security, Flyway y PostgreSQL con Testcontainers. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-001")
@DisplayName("HU-001 Registrar cliente por la API")
class AutenticacionControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    @Test
    @Tag("CP-IDE-001-01")
    @DisplayName("CP-IDE-001-01 Registra al cliente sin token y guarda la frase con Argon2id")
    void registraUnClienteYGuardaElHash() throws Exception {
        // Dado
        var correo = correoUnico();

        // Cuando / Entonces
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correo.toUpperCase(), "mi perro come mango los domingos")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/usuarios/me"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.correo").value(correo))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.hashContrasena").doesNotExist())
                .andExpect(jsonPath("$.fraseContrasena").doesNotExist());

        var hashGuardado = jdbcClient
                .sql("select hash_contrasena from identidad.usuario where correo = :correo")
                .param("correo", correo)
                .query(String.class)
                .single();
        assertThat(hashGuardado).startsWith("$argon2id$").doesNotContain("perro");
    }

    @Test
    @Tag("CP-IDE-001-02")
    @DisplayName("CP-IDE-001-02 Un segundo registro con el mismo correo responde 409 IDE-001")
    void rechazaElCorreoRepetido() throws Exception {
        // Dado
        var correo = correoUnico();
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correo, "mi perro come mango los domingos")))
                .andExpect(status().isCreated());

        // Cuando / Entonces
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correo, "otra frase distinta y larga")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("IDE-001"))
                .andExpect(jsonPath("$.title").value("Correo ya registrado"));
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Una frase de 10 caracteres responde 400 IDE-002 y no crea la cuenta")
    void rechazaLaFraseCorta() throws Exception {
        // Dado
        var correo = correoUnico();

        // Cuando / Entonces
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correo, "corta12345")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("IDE-002"));
        assertThat(contarUsuariosCon(correo)).isZero();
    }

    @Test
    @Tag("CP-IDE-001-03")
    @DisplayName("CP-IDE-001-03 Una contraseña filtrada conocida responde 400 IDE-003")
    void rechazaLaFraseFiltrada() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correoUnico(), "qwertyuiopasdfgh")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("IDE-003"));
    }

    @Test
    @DisplayName("Los campos con forma inválida responden 400 GEN-001 con la lista de errores")
    void rechazaLosCamposInvalidos() throws Exception {
        var cuerpoInvalido = """
                {"nombre": "", "correo": "no-es-correo", "telefono": "abc", "fraseContrasena": "mi perro come mango",
                 "aceptaPoliticaDatos": true, "versionPoliticaDatos": "1.0"}
                """;

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-001"))
                .andExpect(jsonPath("$.errores.length()").value(3));
    }

    @Test
    @Tag("HU-005")
    @Tag("CP-IDE-005-01")
    @DisplayName(
            "CP-IDE-005-01 Sin marcar la casilla responde 400 con el campo aceptaPoliticaDatos y no crea la cuenta")
    void rechazaElRegistroSinAutorizacion() throws Exception {
        // Dado
        var correo = correoUnico();
        var cuerpoSinAutorizacion = """
                {"nombre": "Ana Pérez", "correo": "%s", "telefono": "3001234567",
                 "fraseContrasena": "mi perro come mango los domingos",
                 "aceptaPoliticaDatos": false, "versionPoliticaDatos": "1.0"}
                """.formatted(correo);

        // Cuando / Entonces
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoSinAutorizacion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-001"))
                .andExpect(jsonPath("$.errores[0].campo").value("aceptaPoliticaDatos"));
        assertThat(contarUsuariosCon(correo)).isZero();
    }

    @Test
    @Tag("HU-005")
    @Tag("CP-IDE-005-02")
    @Tag("NF-SEG-014")
    @DisplayName("CP-IDE-005-02 Guarda la autorización con la versión 1.0 y la fecha de aceptación")
    void guardaLaAutorizacionDeDatos() throws Exception {
        // Dado
        var correo = correoUnico();

        // Cuando
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRegistro(correo, "mi perro come mango los domingos")))
                .andExpect(status().isCreated());

        // Entonces
        var versionGuardada =
                jdbcClient.sql("""
                        select a.version_politica from identidad.autorizacion_datos a
                        join identidad.usuario u on u.id = a.usuario_id
                        where u.correo = :correo and a.aceptada_en is not null
                        """).param("correo", correo).query(String.class).single();
        assertThat(versionGuardada).isEqualTo("1.0");
    }

    @Test
    @Tag("HU-005")
    @DisplayName("Una versión vieja de la política responde 409 IDE-007")
    void rechazaUnaVersionViejaDeLaPolitica() throws Exception {
        var cuerpoVersionVieja = cuerpoRegistro(correoUnico(), "mi perro come mango los domingos")
                .replace("\"versionPoliticaDatos\": \"1.0\"", "\"versionPoliticaDatos\": \"0.9\"");

        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoVersionVieja))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("IDE-007"));
    }

    private long contarUsuariosCon(String correo) {
        return jdbcClient
                .sql("select count(*) from identidad.usuario where correo = :correo")
                .param("correo", correo)
                .query(Long.class)
                .single();
    }

    private static String correoUnico() {
        return "cliente-" + UUID.randomUUID() + "@correo.com";
    }

    private static String cuerpoRegistro(String correo, String frase) {
        return """
                {"nombre": "Ana Pérez", "correo": "%s", "telefono": "3001234567", "fraseContrasena": "%s",
                 "aceptaPoliticaDatos": true, "versionPoliticaDatos": "1.0"}
                """.formatted(correo, frase);
    }
}
