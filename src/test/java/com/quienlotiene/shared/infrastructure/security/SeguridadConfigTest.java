package com.quienlotiene.shared.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.shared.infrastructure.web.FiltroIdCorrelacion;
import com.quienlotiene.shared.infrastructure.web.ManejadorGlobalErrores;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = SeguridadConfigTest.ControladorDePrueba.class)
@Import({
    SeguridadConfig.class,
    ManejadorFallasSeguridad.class,
    EmisorTokenAcceso.class,
    UsuarioActual.class,
    ManejadorGlobalErrores.class,
    FiltroIdCorrelacion.class,
    SeguridadConfigTest.ConfiguracionDePrueba.class,
    SeguridadConfigTest.ControladorDePrueba.class
})
@ActiveProfiles("test")
@Tag("T-02")
@DisplayName("T-02 Seguridad con JWT RS256 y roles")
class SeguridadConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmisorTokenAcceso emisorTokenAcceso;

    @Autowired
    JwtEncoder codificadorJwt;

    @Autowired
    PropiedadesJwt propiedadesJwt;

    @Test
    @DisplayName("Sin token una ruta protegida responde 401 GEN-003 con ProblemDetail")
    void rechazaLaPeticionSinToken() throws Exception {
        mockMvc.perform(get("/api/v1/prueba/protegida"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(header().exists(FiltroIdCorrelacion.CABECERA))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("GEN-003"))
                .andExpect(jsonPath("$.instance").value("/api/v1/prueba/protegida"));
    }

    @Test
    @DisplayName("Con un token válido la petición pasa y se conoce el id del usuario")
    void aceptaUnTokenValidoYEntregaElUsuario() throws Exception {
        // Dado
        var usuarioId = UUID.randomUUID();
        var token = emisorTokenAcceso.emitir(usuarioId, List.of("CLIENTE"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/prueba/protegida").header(HttpHeaders.AUTHORIZATION, "Bearer " + token.valor()))
                .andExpect(status().isOk())
                .andExpect(content().string(usuarioId.toString()));
    }

    @Test
    @DisplayName("Un token firmado con otra llave responde 401")
    void rechazaUnTokenConOtraFirma() throws Exception {
        // Dado: otro par de llaves, como si alguien fabricara su propio token
        var propiedadesAjenas =
                new PropiedadesJwt(propiedadesJwt.emisor(), Duration.ofMinutes(15), null, null, "llave-ajena", true);
        var configuracionAjena = new SeguridadConfig();
        var codificadorAjeno =
                configuracionAjena.codificadorJwt(CargadorLlavesRsa.cargar(propiedadesAjenas), propiedadesAjenas);
        var tokenAjeno = new EmisorTokenAcceso(codificadorAjeno, propiedadesAjenas, Clock.systemUTC())
                .emitir(UUID.randomUUID(), List.of("ADMIN"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/admin/prueba").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAjeno.valor()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("GEN-003"));
    }

    @Test
    @DisplayName("Un token vencido responde 401")
    void rechazaUnTokenVencido() throws Exception {
        // Dado: un token emitido hace una hora, con vigencia de 15 minutos
        var haceUnaHora = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        var tokenVencido = new EmisorTokenAcceso(codificadorJwt, propiedadesJwt, haceUnaHora)
                .emitir(UUID.randomUUID(), List.of("CLIENTE"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/prueba/protegida")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenVencido.valor()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un token de otro emisor responde 401")
    void rechazaUnTokenDeOtroEmisor() throws Exception {
        // Dado
        var propiedadesOtroEmisor =
                new PropiedadesJwt("otro-emisor", Duration.ofMinutes(15), null, null, propiedadesJwt.kid(), true);
        var token = new EmisorTokenAcceso(codificadorJwt, propiedadesOtroEmisor, Clock.systemUTC())
                .emitir(UUID.randomUUID(), List.of("CLIENTE"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/prueba/protegida").header(HttpHeaders.AUTHORIZATION, "Bearer " + token.valor()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Un cliente que entra a administración responde 403 GEN-004")
    void rechazaAUnClienteEnAdministracion() throws Exception {
        // Dado
        var token = emisorTokenAcceso.emitir(UUID.randomUUID(), List.of("CLIENTE"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/admin/prueba").header(HttpHeaders.AUTHORIZATION, "Bearer " + token.valor()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-004"));
    }

    @Test
    @DisplayName("Un administrador entra a administración")
    void permiteAlAdministradorEnAdministracion() throws Exception {
        // Dado
        var token = emisorTokenAcceso.emitir(UUID.randomUUID(), List.of("ADMIN"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/admin/prueba").header(HttpHeaders.AUTHORIZATION, "Bearer " + token.valor()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("@PreAuthorize con el rol equivocado responde 403 GEN-004")
    void respetaElRolDePreAuthorize() throws Exception {
        // Dado
        var tokenCliente = emisorTokenAcceso.emitir(UUID.randomUUID(), List.of("CLIENTE"));
        var tokenTienda = emisorTokenAcceso.emitir(UUID.randomUUID(), List.of("TIENDA"));

        // Cuando / Entonces
        mockMvc.perform(get("/api/v1/prueba/solo-tienda")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente.valor()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-004"));
        mockMvc.perform(get("/api/v1/prueba/solo-tienda")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTienda.valor()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Las rutas de autenticación no piden token")
    void dejaPasarLasRutasPublicas() throws Exception {
        mockMvc.perform(get("/api/v1/auth/prueba")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS solo acepta el origen del frontend")
    void aceptaSoloElOrigenDelFrontend() throws Exception {
        mockMvc.perform(options("/api/v1/prueba/protegida")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
        mockMvc.perform(options("/api/v1/prueba/protegida")
                        .header(HttpHeaders.ORIGIN, "https://sitio-malicioso.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracionDePrueba {
        @Bean
        Clock reloj() {
            return Clock.systemUTC();
        }
    }

    @RestController
    static class ControladorDePrueba {

        private final UsuarioActual usuarioActual;

        ControladorDePrueba(UsuarioActual usuarioActual) {
            this.usuarioActual = usuarioActual;
        }

        @GetMapping("/api/v1/prueba/protegida")
        String protegida() {
            return usuarioActual.id().toString();
        }

        @GetMapping("/api/v1/prueba/solo-tienda")
        @PreAuthorize("hasRole('TIENDA')")
        String soloTienda() {
            return "ok";
        }

        @GetMapping("/api/v1/admin/prueba")
        String administracion() {
            return "ok";
        }

        @GetMapping("/api/v1/auth/prueba")
        String publica() {
            return "ok";
        }
    }
}
