package com.quienlotiene.shared.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServlet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@Tag("T-01")
@DisplayName("T-01 Filtro del id de correlación")
class FiltroIdCorrelacionTest {

    private final FiltroIdCorrelacion filtro = new FiltroIdCorrelacion();

    @Test
    @DisplayName("Reutiliza el id que envía el cliente y lo deja en el MDC durante la petición")
    void reutilizaElIdRecibido() throws Exception {
        // Dado
        var peticion = new MockHttpServletRequest("GET", "/api/v1/solicitudes");
        peticion.addHeader(FiltroIdCorrelacion.CABECERA, "abc-123-def-456");
        var respuesta = new MockHttpServletResponse();
        var idDuranteLaPeticion = new AtomicReference<String>();

        // Cuando
        filtro.doFilter(
                peticion,
                respuesta,
                new MockFilterChain(
                        new HttpServlet() {},
                        (peticionInterna, respuestaInterna, cadena) ->
                                idDuranteLaPeticion.set(MDC.get(FiltroIdCorrelacion.CLAVE_MDC))));

        // Entonces
        assertThat(respuesta.getHeader(FiltroIdCorrelacion.CABECERA)).isEqualTo("abc-123-def-456");
        assertThat(idDuranteLaPeticion.get()).isEqualTo("abc-123-def-456");
        assertThat(MDC.get(FiltroIdCorrelacion.CLAVE_MDC)).isNull();
    }

    @Test
    @DisplayName("Genera un UUID cuando la petición no trae id")
    void generaUnIdCuandoNoLlega() throws Exception {
        // Dado
        var respuesta = new MockHttpServletResponse();

        // Cuando
        filtro.doFilter(new MockHttpServletRequest("GET", "/"), respuesta, new MockFilterChain());

        // Entonces
        var idGenerado = respuesta.getHeader(FiltroIdCorrelacion.CABECERA);
        assertThat(UUID.fromString(idGenerado)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"corto", "id con espacios", "linea\nfalsa INFO usuario=admin", "<script>alert(1)</script>"})
    @DisplayName("Descarta un id con formato peligroso y genera uno nuevo")
    void descartaUnIdConFormatoPeligroso(String idRecibido) throws Exception {
        // Dado
        var peticion = new MockHttpServletRequest("GET", "/");
        peticion.addHeader(FiltroIdCorrelacion.CABECERA, idRecibido);
        var respuesta = new MockHttpServletResponse();

        // Cuando
        filtro.doFilter(peticion, respuesta, new MockFilterChain());

        // Entonces
        var idUsado = respuesta.getHeader(FiltroIdCorrelacion.CABECERA);
        assertThat(idUsado).isNotEqualTo(idRecibido);
        assertThat(UUID.fromString(idUsado)).isNotNull();
    }
}
