package com.quienlotiene.mensajeria.domain.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-033")
@DisplayName("HU-033 Mensaje")
class MensajeTest {

    @Test
    @DisplayName("Un mensaje vacío o de más de 1000 caracteres se rechaza (MEN-003)")
    void validaElTexto() {
        for (var texto : new String[] {null, "   ", "x".repeat(1001)}) {
            assertThatThrownBy(() -> Mensaje.escribir(
                            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), true, texto, Instant.now()))
                    .isInstanceOf(MensajeInvalidoException.class)
                    .hasFieldOrPropertyWithValue("codigo", "MEN-003");
        }
    }
}
