package com.quienlotiene.identidad.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import org.apache.commons.codec.binary.Base32;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

/**
 * HU-004 de punta a punta: el administrador inicial se crea al arrancar con las variables QLT_ADMIN_*, inicia sesión
 * con su frase, recibe el token temporal y entra con un código TOTP real calculado con su secreto.
 */
@SpringBootTest(
        properties = {
            // Valores solo de prueba; la llave y el secreto TOTP se arman abajo, no se escriben en el código
            "quienlotiene.identidad.administrador-inicial.correo=admin@quienlotiene.test",
            "quienlotiene.identidad.administrador-inicial.frase-contrasena=frase de prueba del administrador",
            "quienlotiene.identidad.administrador-inicial.telefono=3000000000"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-004")
@DisplayName("HU-004 Segundo factor del administrador por la API")
class SegundoFactorIT {

    /** Secreto del ejemplo de RFC 6238 ("12345678901234567890" en Base32), armado en tiempo de ejecución. */
    private static final String SECRETO =
            new Base32().encodeToString("12345678901234567890".getBytes(StandardCharsets.US_ASCII));

    @DynamicPropertySource
    static void secretosDePrueba(DynamicPropertyRegistry registro) {
        registro.add(
                "quienlotiene.identidad.llave-cifrado-totp",
                () -> Base64.getEncoder().encodeToString(new byte[32]));
        registro.add("quienlotiene.identidad.administrador-inicial.secreto-totp", () -> SECRETO);
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    private final JsonMapper lectorJson = JsonMapper.builder().build();

    @Test
    @Tag("CP-IDE-004-01")
    @DisplayName(
            "CP-IDE-004-01 Con frase y código TOTP vigente entra con rol ADMIN; el secreto está cifrado en la base")
    void entraConElSegundoFactor() throws Exception {
        // Dado: el administrador inicial ya se creó al arrancar
        var secretoGuardado = jdbcClient
                .sql("select secreto_totp from identidad.usuario where correo = 'admin@quienlotiene.test'")
                .query(String.class)
                .single();
        assertThat(secretoGuardado).isNotBlank().isNotEqualTo(SECRETO);

        // Cuando: inicia sesión con la frase
        var tokenTemporal = tokenTemporalTrasLaFrase();

        // Entonces: con el código vigente entra al panel de administración
        var respuesta = verificar(tokenTemporal, codigoVigente())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.segundoFactorRequerido").value(false))
                .andExpect(cookie().httpOnly(CookieTokenRefresco.NOMBRE, true))
                .andReturn();
        var tokenAcceso = lectorJson
                .readTree(respuesta.getResponse().getContentAsString())
                .get("tokenAcceso")
                .asString();
        mockMvc.perform(get("/api/v1/admin/tiendas").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAcceso))
                .andExpect(status().isNotFound());
        // El token temporal es de un solo uso
        verificar(tokenTemporal, codigoVigente())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("IDE-013"));
    }

    @Test
    @Tag("CP-IDE-004-02")
    @DisplayName("CP-IDE-004-02 Con un código incorrecto niega el acceso; al quinto intento el token deja de servir")
    void niegaElAccesoConCodigoIncorrecto() throws Exception {
        var tokenTemporal = tokenTemporalTrasLaFrase();
        var codigoIncorrecto = codigoVigente().equals("000000") ? "111111" : "000000";

        for (int intento = 1; intento <= 5; intento++) {
            verificar(tokenTemporal, codigoIncorrecto)
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.codigo").value("IDE-012"))
                    .andExpect(cookie().doesNotExist(CookieTokenRefresco.NOMBRE));
        }
        verificar(tokenTemporal, codigoVigente())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("IDE-013"));
    }

    @Test
    @DisplayName("El token temporal no sirve como token de acceso")
    void elTokenTemporalNoAbreLaApi() throws Exception {
        var tokenTemporal = tokenTemporalTrasLaFrase();

        mockMvc.perform(get("/api/v1/admin/tiendas").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTemporal))
                .andExpect(status().isUnauthorized());
    }

    private String tokenTemporalTrasLaFrase() throws Exception {
        var respuesta = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo": "admin@quienlotiene.test",
                                 "fraseContrasena": "frase de prueba del administrador"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.segundoFactorRequerido").value(true))
                .andExpect(jsonPath("$.tokenAcceso").doesNotExist())
                .andExpect(cookie().doesNotExist(CookieTokenRefresco.NOMBRE))
                .andReturn();
        return lectorJson
                .readTree(respuesta.getResponse().getContentAsString())
                .get("tokenSegundoFactor")
                .asString();
    }

    private ResultActions verificar(String tokenTemporal, String codigo) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/totp")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"tokenSegundoFactor": "%s", "codigo": "%s"}
                        """.formatted(tokenTemporal, codigo)));
    }

    /** Lo que mostraría la aplicación autenticadora del administrador en este momento. */
    private static String codigoVigente() throws Exception {
        var pasoActual = Instant.now().getEpochSecond() / 30;
        return new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6).generate(SECRETO, pasoActual);
    }
}
