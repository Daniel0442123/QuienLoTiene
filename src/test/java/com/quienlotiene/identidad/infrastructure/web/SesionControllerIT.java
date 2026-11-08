package com.quienlotiene.identidad.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.ContenedoresDePrueba;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

/** HU-003 de punta a punta: inicio de sesión, uso del token, rotación del refresco y cierre de sesión. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-003")
@DisplayName("HU-003 Iniciar y cerrar sesión por la API")
class SesionControllerIT {

    private static final String FRASE = "mi perro come mango los domingos";
    private static final int SIETE_DIAS_EN_SEGUNDOS = 7 * 24 * 60 * 60;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    private final JsonMapper lectorJson = JsonMapper.builder().build();

    @Test
    @Tag("CP-IDE-003-01")
    @DisplayName("CP-IDE-003-01 Recibe un token de 15 minutos que abre las rutas protegidas y una cookie HttpOnly")
    void iniciaSesionYUsaElToken() throws Exception {
        // Dado: un cliente con cuenta activa
        var correo = registrarCliente();

        // Cuando
        var respuesta = iniciarSesion(correo, FRASE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.tokenRefresco").doesNotExist())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(cookie().httpOnly(CookieTokenRefresco.NOMBRE, true))
                .andExpect(cookie().secure(CookieTokenRefresco.NOMBRE, true))
                .andExpect(cookie().sameSite(CookieTokenRefresco.NOMBRE, "Strict"))
                .andExpect(cookie().path(CookieTokenRefresco.NOMBRE, CookieTokenRefresco.RUTA))
                // Siete días; puede faltar un segundo por el tiempo que tarda la petición
                .andExpect(resultado -> assertThat(resultado
                                .getResponse()
                                .getCookie(CookieTokenRefresco.NOMBRE)
                                .getMaxAge())
                        .isBetween(SIETE_DIAS_EN_SEGUNDOS - 5, SIETE_DIAS_EN_SEGUNDOS))
                .andReturn();

        // Entonces: el token abre una ruta protegida (responde 404 porque aún no existe, no 401)
        var tokenAcceso = leer(respuesta, "tokenAcceso");
        mockMvc.perform(get("/api/v1/usuarios/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAcceso))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/usuarios/me")).andExpect(status().isUnauthorized());
        var minutosDeVigencia = Duration.between(Instant.now(), Instant.parse(leer(respuesta, "fechaExpiracion")))
                .toMinutes();
        assertThat(minutosDeVigencia).isBetween(14L, 15L);
    }

    @Test
    @Tag("CP-IDE-003-02")
    @DisplayName("CP-IDE-003-02 Frase incorrecta y correo inexistente dan el mismo 401 IDE-008")
    void niegaElAccesoConMensajeGenerico() throws Exception {
        var correo = registrarCliente();

        iniciarSesion(correo, "frase equivocada cualquiera")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("IDE-008"))
                .andExpect(jsonPath("$.detail").value("El correo o la frase de contraseña no son correctos."))
                .andExpect(cookie().doesNotExist(CookieTokenRefresco.NOMBRE));
        iniciarSesion("nadie-" + UUID.randomUUID() + "@correo.com", FRASE)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("El correo o la frase de contraseña no son correctos."));
    }

    @Test
    @Tag("CP-IDE-003-03")
    @DisplayName("CP-IDE-003-03 Una cuenta suspendida con credenciales correctas responde 403 IDE-009")
    void niegaElAccesoALaCuentaSuspendida() throws Exception {
        // Dado
        var correo = registrarCliente();
        jdbcClient
                .sql("update identidad.usuario set estado = 'SUSPENDIDA' where correo = :correo")
                .param("correo", correo)
                .update();

        // Cuando / Entonces
        iniciarSesion(correo, FRASE)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("IDE-009"))
                .andExpect(jsonPath("$.title").value("Cuenta suspendida"));
    }

    @Test
    @DisplayName("El refresco rota la cookie y reusar la vieja cierra todas las sesiones")
    void rotaElRefrescoYDetectaElReuso() throws Exception {
        // Dado
        var correo = registrarCliente();
        var cookieInicial =
                iniciarSesion(correo, FRASE).andReturn().getResponse().getCookie(CookieTokenRefresco.NOMBRE);

        // Cuando: se renueva la sesión
        var cookieRotada = mockMvc.perform(post("/api/v1/auth/refresco").cookie(cookieInicial))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenAcceso").isNotEmpty())
                .andReturn()
                .getResponse()
                .getCookie(CookieTokenRefresco.NOMBRE);

        // Entonces: la cookie cambió, la vieja ya no sirve y su reuso invalida también la nueva
        assertThat(cookieRotada.getValue()).isNotEqualTo(cookieInicial.getValue());
        mockMvc.perform(post("/api/v1/auth/refresco").cookie(cookieInicial))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("IDE-010"));
        mockMvc.perform(post("/api/v1/auth/refresco").cookie(cookieRotada)).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cerrar sesión revoca el refresco y borra la cookie")
    void cierraLaSesion() throws Exception {
        // Dado
        var correo = registrarCliente();
        var cookie = iniciarSesion(correo, FRASE).andReturn().getResponse().getCookie(CookieTokenRefresco.NOMBRE);

        // Cuando
        mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(CookieTokenRefresco.NOMBRE, 0));

        // Entonces
        mockMvc.perform(post("/api/v1/auth/refresco").cookie(new Cookie(CookieTokenRefresco.NOMBRE, cookie.getValue())))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/auth/logout")).andExpect(status().isNoContent());
    }

    private String registrarCliente() throws Exception {
        var correo = "cliente-" + UUID.randomUUID() + "@correo.com";
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana Pérez", "correo": "%s", "telefono": "3001234567",
                                 "fraseContrasena": "%s", "aceptaPoliticaDatos": true, "versionPoliticaDatos": "1.0"}
                                """.formatted(correo, FRASE)))
                .andExpect(status().isCreated());
        return correo;
    }

    private ResultActions iniciarSesion(String correo, String frase) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"correo": "%s", "fraseContrasena": "%s"}
                        """.formatted(correo, frase)));
    }

    private String leer(MvcResult respuesta, String campo) throws Exception {
        return lectorJson
                .readTree(respuesta.getResponse().getContentAsString())
                .get(campo)
                .asString();
    }
}
