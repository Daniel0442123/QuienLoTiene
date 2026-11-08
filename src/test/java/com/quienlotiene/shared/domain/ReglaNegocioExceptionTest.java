package com.quienlotiene.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("T-01")
@DisplayName("T-01 Excepción base de reglas de negocio")
class ReglaNegocioExceptionTest {

    @Test
    @DisplayName("Conserva el código, el título, el detalle y el tipo")
    void conservaSusDatos() {
        // Cuando
        var excepcion = new ExcepcionDePrueba("PRU-001", "Título", "Detalle para el usuario.", TipoError.CONFLICTO);

        // Entonces
        assertThat(excepcion.getCodigo()).isEqualTo("PRU-001");
        assertThat(excepcion.getTitulo()).isEqualTo("Título");
        assertThat(excepcion.getMessage()).isEqualTo("Detalle para el usuario.");
        assertThat(excepcion.getTipo()).isEqualTo(TipoError.CONFLICTO);
    }

    @Test
    @DisplayName("No se puede crear sin código, título, detalle o tipo")
    void exigeTodosSusDatos() {
        assertThatThrownBy(() -> new ExcepcionDePrueba(" ", "Título", "Detalle", TipoError.CONFLICTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("codigo");
        assertThatThrownBy(() -> new ExcepcionDePrueba("PRU-001", null, "Detalle", TipoError.CONFLICTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("titulo");
        assertThatThrownBy(() -> new ExcepcionDePrueba("PRU-001", "Título", "", TipoError.CONFLICTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("detalle");
        assertThatThrownBy(() -> new ExcepcionDePrueba("PRU-001", "Título", "Detalle", null))
                .isInstanceOf(NullPointerException.class);
    }

    private static final class ExcepcionDePrueba extends ReglaNegocioException {
        ExcepcionDePrueba(String codigo, String titulo, String detalle, TipoError tipo) {
            super(codigo, titulo, detalle, tipo);
        }
    }
}
