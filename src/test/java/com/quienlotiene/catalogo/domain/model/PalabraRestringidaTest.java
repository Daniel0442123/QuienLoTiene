package com.quienlotiene.catalogo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-023")
@DisplayName("HU-023 y HU-038 Palabra restringida")
class PalabraRestringidaTest {

    @Test
    @Tag("CP-SOL-023-01")
    @DisplayName("CP-SOL-023-01 «Amoxicilina 500 mg» contiene «amoxicilina» sin importar mayúsculas ni tildes")
    void comparaNormalizado() {
        var palabra = new PalabraRestringida(UUID.randomUUID(), "  AMOXICILÍNA ");

        assertThat(palabra.palabraClave()).isEqualTo("amoxicilina");
        assertThat(palabra.apareceEn("Amoxicilina 500 mg")).isTrue();
        assertThat(palabra.apareceEn("amoxicilína   x 12")).isTrue();
        assertThat(palabra.apareceEn("Paracetamol 500 mg")).isFalse();
        assertThat(palabra.apareceEn(null)).isFalse();
    }

    @Test
    @DisplayName("Rechaza palabras de menos de 2 o más de 80 caracteres (CAT-003)")
    void longitud() {
        assertThatThrownBy(() -> new PalabraRestringida(UUID.randomUUID(), " a "))
                .isInstanceOf(PalabraRestringidaInvalidaException.class)
                .hasFieldOrPropertyWithValue("codigo", "CAT-003");
        assertThatThrownBy(() -> new PalabraRestringida(UUID.randomUUID(), "x".repeat(81)))
                .isInstanceOf(PalabraRestringidaInvalidaException.class);
    }
}
