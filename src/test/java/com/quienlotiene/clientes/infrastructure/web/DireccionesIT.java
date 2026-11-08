package com.quienlotiene.clientes.infrastructure.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

/** HU-007 de punta a punta: cliente registrado, sus direcciones con PostGIS y su perfil. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ContenedoresDePrueba.class)
@Testcontainers(disabledWithoutDocker = true)
@Tag("HU-007")
@DisplayName("HU-007 Perfil y direcciones del cliente por la API")
class DireccionesIT {

    private static final String FRASE = "mi perro come mango los domingos";
    private static final String CASA = """
            {"nombre": "Casa", "ubicacion": {"latitud": 2.4448, "longitud": -76.6147},
             "direccionEscrita": "Calle 5 # 10-20", "indicaciones": "Portón verde", "telefono": "3001234567"}
            """;

    @Autowired
    MockMvc mockMvc;

    private final JsonMapper lectorJson = JsonMapper.builder().build();

    /** Registra un cliente, inicia sesión y devuelve "Bearer ...". */
    private String clienteConSesion() throws Exception {
        var correo = "cliente-" + UUID.randomUUID() + "@correo.com";
        mockMvc.perform(post("/api/v1/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana Pérez", "correo": "%s", "telefono": "3001234567",
                                 "fraseContrasena": "%s", "aceptaPoliticaDatos": true, "versionPoliticaDatos": "1.0"}
                                """.formatted(correo, FRASE)))
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

    private String crearCasa(String token) throws Exception {
        var respuesta = mockMvc.perform(post("/api/v1/clientes/me/direcciones")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CASA))
                .andExpect(status().isCreated())
                .andReturn();
        return lectorJson
                .readTree(respuesta.getResponse().getContentAsString())
                .get("id")
                .asString();
    }

    @Test
    @Tag("CP-CLI-007-01")
    @DisplayName("CP-CLI-007-01 Guarda \"Casa\" y aparece en sus direcciones; la edita y la borra")
    void gestionaSusDirecciones() throws Exception {
        // Dado
        var token = clienteConSesion();

        // Cuando
        var direccionId = crearCasa(token);

        // Entonces
        mockMvc.perform(get("/api/v1/clientes/me/direcciones").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Casa"))
                .andExpect(jsonPath("$[0].latitud").value(2.4448))
                .andExpect(jsonPath("$[0].indicaciones").value("Portón verde"));
        mockMvc.perform(put("/api/v1/clientes/me/direcciones/{id}", direccionId)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CASA.replace("Casa", "Oficina")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Oficina"));
        mockMvc.perform(delete("/api/v1/clientes/me/direcciones/{id}", direccionId)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/clientes/me/direcciones").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Tag("CP-CLI-007-02")
    @DisplayName("CP-CLI-007-02 Sin ubicación en el mapa responde 400 con el campo")
    void rechazaLaDireccionSinUbicacion() throws Exception {
        var token = clienteConSesion();

        mockMvc.perform(post("/api/v1/clientes/me/direcciones")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Casa", "direccionEscrita": "Calle 5", "telefono": "3001234567"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("ubicacion"));
    }

    @Test
    @DisplayName("Otro cliente no puede editar ni borrar la dirección (404 CLI-003)")
    void otroClienteNoVeLaDireccion() throws Exception {
        var direccionId = crearCasa(clienteConSesion());
        var otroCliente = clienteConSesion();

        mockMvc.perform(delete("/api/v1/clientes/me/direcciones/{id}", direccionId)
                        .header(HttpHeaders.AUTHORIZATION, otroCliente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLI-003"));
        mockMvc.perform(get("/api/v1/clientes/me/direcciones").header(HttpHeaders.AUTHORIZATION, otroCliente))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("El cliente ve y cambia el nombre y el teléfono de su perfil")
    void editaSuPerfil() throws Exception {
        var token = clienteConSesion();

        mockMvc.perform(patch("/api/v1/usuarios/me")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana María Pérez", "telefono": "3109876543"}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/usuarios/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana María Pérez"))
                .andExpect(jsonPath("$.telefono").value("3109876543"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }
}
