package com.quienlotiene.tiendas.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import com.quienlotiene.tiendas.fixtures.ArchivosDePrueba;
import java.nio.file.Path;
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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

/** HU-009 de punta a punta: el dueño registra su tienda, inicia sesión, ve su tienda y cambia perfil y horario. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-009")
@DisplayName("HU-009 Perfil y horario de la tienda por la API")
class MiTiendaIT {

    private static final String FRASE = "mi tienda vende de todo un poco";

    @TempDir
    static Path directorioArchivos;

    @DynamicPropertySource
    static void almacenTemporal(DynamicPropertyRegistry registro) {
        registro.add("quienlotiene.archivos.directorio", () -> directorioArchivos.toString());
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    private final JsonMapper lectorJson = JsonMapper.builder().build();

    /** Registra una tienda y devuelve el "Bearer ..." de su dueño. */
    private String registrarTiendaEIniciarSesion() throws Exception {
        var correo = "tienda-" + UUID.randomUUID() + "@correo.com";
        var json = """
                {"nombreDueno": "Carlos Muñoz", "correo": "%s", "telefono": "3001234567",
                 "fraseContrasena": "%s", "aceptaPoliticaDatos": true, "versionPoliticaDatos": "1.0",
                 "nombreTienda": "La 5", "categoriaId": "%s", "ubicacion": {"latitud": 2.4448, "longitud": -76.6147}}
                """.formatted(correo, FRASE, UUID.randomUUID());
        mockMvc.perform(multipart("/api/v1/tiendas/registro")
                        .file(new MockMultipartFile("datos", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes()))
                        .file(new MockMultipartFile("fotoLocal", "f.jpg", "image/jpeg", ArchivosDePrueba.unJpeg()))
                        .file(new MockMultipartFile("rut", "r.pdf", "application/pdf", ArchivosDePrueba.unPdf())))
                .andExpect(status().isCreated());
        var sesion = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "%s", "fraseContrasena": "%s"}
                                """.formatted(correo, FRASE)))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer "
                + lectorJson
                        .readTree(sesion.getResponse().getContentAsString())
                        .get("tokenAcceso")
                        .asString();
    }

    @Test
    @Tag("CP-TIE-009-02")
    @DisplayName("CP-TIE-009-02 El dueño cambia teléfono y horario y los ve al consultar su tienda")
    void actualizaPerfilYHorario() throws Exception {
        // Dado
        var token = registrarTiendaEIniciarSesion();
        mockMvc.perform(get("/api/v1/tiendas/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.horario.length()").value(0));

        // Cuando
        mockMvc.perform(put("/api/v1/tiendas/me/perfil")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Droguería La 5", "telefono": "3109876543",
                                 "horario": [{"diaSemana": "MONDAY", "abre": "08:00", "cierra": "18:00"},
                                             {"diaSemana": "SATURDAY", "abre": "09:00", "cierra": "13:00"}]}
                                """))
                .andExpect(status().isOk());

        // Entonces
        mockMvc.perform(get("/api/v1/tiendas/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Droguería La 5"))
                .andExpect(jsonPath("$.telefono").value("3109876543"))
                .andExpect(jsonPath("$.horario[0].diaSemana").value("MONDAY"))
                .andExpect(jsonPath("$.horario[0].abre").value("08:00:00"))
                .andExpect(jsonPath("$.horario[1].diaSemana").value("SATURDAY"));
        // La base guarda la hora local de Colombia tal cual, sin correrla por la zona horaria de la JVM
        var horasGuardadas = jdbcClient
                .sql("select cast(abre as text) from tiendas.horario where dia_semana = 1 and cierra = '18:00'")
                .query(String.class)
                .list();
        assertThat(horasGuardadas).contains("08:00:00");
    }

    @Test
    @DisplayName("Un horario que abre después de cerrar responde 400 TIE-007")
    void rechazaUnHorarioInvalido() throws Exception {
        var token = registrarTiendaEIniciarSesion();

        mockMvc.perform(put("/api/v1/tiendas/me/perfil")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "La 5", "telefono": "3001234567",
                                 "horario": [{"diaSemana": "MONDAY", "abre": "18:00", "cierra": "08:00"}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TIE-007"));
    }

    @Test
    @Tag("HU-013")
    @Tag("CP-TIE-013-02")
    @DisplayName("HU-013 Una tienda pendiente no abre (409 TIE-008); aprobada abre y cierra")
    void abreYCierraLaTienda() throws Exception {
        // Dado: la tienda recién registrada sigue pendiente
        var token = registrarTiendaEIniciarSesion();
        mockMvc.perform(patch("/api/v1/tiendas/me/apertura")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"abierta\": true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TIE-008"));

        // Cuando: el administrador la aprueba
        jdbcClient
                .sql("update tiendas.tienda set estado = 'APROBADA' where nombre = 'La 5' and estado = 'PENDIENTE'")
                .update();

        // Entonces: puede abrir y cerrar
        mockMvc.perform(patch("/api/v1/tiendas/me/apertura")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"abierta\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estaAbierta").value(true));
        mockMvc.perform(patch("/api/v1/tiendas/me/apertura")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"abierta\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estaAbierta").value(false));
    }

    @Test
    @DisplayName("Solo el rol TIENDA usa /tiendas/me")
    void soloLasTiendasVenSuPerfil() throws Exception {
        mockMvc.perform(get("/api/v1/tiendas/me")).andExpect(status().isUnauthorized());
    }
}
