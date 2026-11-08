package com.quienlotiene.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.quienlotiene.shared.domain.ConTiempoDeEspera;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag("T-01")
@DisplayName("T-01 Manejador global de errores con ProblemDetail")
class ManejadorGlobalErroresTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControladorDePrueba())
                .setControllerAdvice(new ManejadorGlobalErrores())
                .addFilters(new FiltroIdCorrelacion())
                .build();
    }

    @Test
    @DisplayName("Una regla de negocio responde con su código, título y detalle")
    void respondeLaReglaDeNegocioConSuCodigo() throws Exception {
        // Dado: el controlador lanza LimiteDePruebaSuperadoException

        // Cuando / Entonces
        mockMvc.perform(post("/prueba/regla").header(FiltroIdCorrelacion.CABECERA, "correlacion-de-prueba-01"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(FiltroIdCorrelacion.CABECERA, "correlacion-de-prueba-01"))
                .andExpect(jsonPath("$.type").value("https://quienlotiene.app/errores/limite-de-prueba-superado"))
                .andExpect(jsonPath("$.title").value("Límite de prueba superado"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("Puedes intentarlo de nuevo en 12 minutos."))
                .andExpect(jsonPath("$.instance").value("/prueba/regla"))
                .andExpect(jsonPath("$.codigo").value("PRU-001"))
                .andExpect(jsonPath("$.errores", hasSize(0)))
                .andExpect(jsonPath("$.idCorrelacion").value("correlacion-de-prueba-01"));
    }

    @ParameterizedTest(name = "{0} responde {1}")
    @CsvSource({
        "DATOS_INVALIDOS, 400",
        "NO_AUTENTICADO, 401",
        "SIN_PERMISO, 403",
        "NO_ENCONTRADO, 404",
        "CONFLICTO, 409",
        "REGLA_NEGOCIO, 422",
        "DEMASIADOS_INTENTOS, 429"
    })
    @DisplayName("Cada tipo de error del dominio tiene su estado HTTP")
    void traduceCadaTipoDeErrorAUnEstadoHttp(TipoError tipo, int estadoEsperado) throws Exception {
        // Dado: una excepción del tipo indicado

        // Cuando / Entonces
        mockMvc.perform(get("/prueba/tipo/{tipo}", tipo.name()))
                .andExpect(status().is(estadoEsperado))
                .andExpect(jsonPath("$.codigo").value("PRU-002"));
    }

    @Test
    @DisplayName("Los datos inválidos responden 400 con la lista de campos")
    void respondeLosDatosInvalidosConLaListaDeCampos() throws Exception {
        // Dado
        var cuerpoConNombreVacio = """
                {"nombre": "", "descripcion": "corta"}
                """;

        // Cuando / Entonces
        mockMvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoConNombreVacio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-001"))
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.errores", hasSize(2)))
                .andExpect(jsonPath("$.errores[?(@.campo == 'nombre')].mensaje").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.errores[?(@.campo == 'descripcion')].mensaje")
                        .value("La descripción debe tener al menos 10 caracteres"));
    }

    @Test
    @DisplayName("Un cuerpo que no es JSON válido responde 400 GEN-002 en español")
    void respondeElCuerpoIlegibleComoPeticionMalFormada() throws Exception {
        // Dado
        var cuerpoRoto = "{\"nombre\": ";

        // Cuando / Entonces
        mockMvc.perform(post("/prueba/validacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoRoto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("GEN-002"))
                .andExpect(jsonPath("$.title").value("Petición mal formada"))
                .andExpect(jsonPath("$.instance").value("/prueba/validacion"));
    }

    @Test
    @DisplayName("Un método no soportado responde 405 GEN-006")
    void respondeElMetodoNoPermitido() throws Exception {
        mockMvc.perform(delete("/prueba/regla"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.codigo").value("GEN-006"));
    }

    @Test
    @DisplayName("Un error inesperado responde 500 sin revelar el mensaje técnico")
    void ocultaElMensajeTecnicoDeUnErrorInesperado() throws Exception {
        mockMvc.perform(get("/prueba/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("GEN-008"))
                .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado. Intenta de nuevo más tarde."))
                .andExpect(content().string(not(containsString("tabla_secreta"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))));
    }

    @Test
    @DisplayName("Un acceso denegado dentro del controlador responde 403 GEN-004")
    void respondeElAccesoDenegado() throws Exception {
        mockMvc.perform(get("/prueba/denegado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("GEN-004"));
    }

    @Test
    @DisplayName("Una autenticación fallida dentro del controlador responde 401 GEN-003")
    void respondeLaSesionRequerida() throws Exception {
        mockMvc.perform(get("/prueba/sin-sesion"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("GEN-003"));
    }

    @Test
    @DisplayName("El tipo del error se arma sin tildes ni espacios")
    void armaElSlugSinTildes() {
        assertThat(ProblemasHttp.aSlug("Límite de solicitudes superado")).isEqualTo("limite-de-solicitudes-superado");
        assertThat(ProblemasHttp.aSlug("  ¿Qué pasó, Ñandú?  ")).isEqualTo("que-paso-nandu");
    }

    @Test
    @DisplayName("Un límite de uso envía Retry-After en segundos enteros hacia arriba")
    void enviaRetryAfterEnLosLimitesDeUso() throws Exception {
        mockMvc.perform(get("/prueba/limite"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "721"));
    }

    static final class LimiteConEsperaException extends ReglaNegocioException implements ConTiempoDeEspera {
        LimiteConEsperaException() {
            super("PRU-003", "Límite con espera", "Intenta en 13 minutos.", TipoError.DEMASIADOS_INTENTOS);
        }

        @Override
        public Duration tiempoDeEspera() {
            return Duration.ofSeconds(720).plusMillis(1);
        }
    }

    record PeticionDePrueba(
            @NotBlank(message = "El nombre es obligatorio") String nombre,

            @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
            String descripcion) {}

    static final class LimiteDePruebaSuperadoException extends ReglaNegocioException {
        LimiteDePruebaSuperadoException() {
            super(
                    "PRU-001",
                    "Límite de prueba superado",
                    "Puedes intentarlo de nuevo en 12 minutos.",
                    TipoError.REGLA_NEGOCIO);
        }
    }

    static final class ErrorDeTipoException extends ReglaNegocioException {
        ErrorDeTipoException(TipoError tipo) {
            super("PRU-002", "Error de tipo", "Detalle del error de tipo.", tipo);
        }
    }

    @RestController
    static class ControladorDePrueba {

        @PostMapping("/prueba/regla")
        void lanzarReglaDeNegocio() {
            throw new LimiteDePruebaSuperadoException();
        }

        @GetMapping("/prueba/tipo/{tipo}")
        void lanzarSegunTipo(@PathVariable TipoError tipo) {
            throw new ErrorDeTipoException(tipo);
        }

        @PostMapping("/prueba/validacion")
        void validar(@Valid @RequestBody PeticionDePrueba peticion) {}

        @GetMapping("/prueba/limite")
        void lanzarLimite() {
            throw new LimiteConEsperaException();
        }

        @GetMapping("/prueba/inesperado")
        void fallarInesperadamente() {
            throw new IllegalStateException("Falló la consulta a tabla_secreta");
        }

        @GetMapping("/prueba/denegado")
        void denegar() {
            throw new AccessDeniedException("sin rol");
        }

        @GetMapping("/prueba/sin-sesion")
        void exigirSesion() {
            throw new BadCredentialsException("token vencido");
        }
    }
}
