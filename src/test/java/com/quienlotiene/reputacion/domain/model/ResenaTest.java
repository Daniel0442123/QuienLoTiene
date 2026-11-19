package com.quienlotiene.reputacion.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-034")
@DisplayName("HU-034 Reseña")
class ResenaTest {

    private static Resena con(int estrellas, String comentario) {
        return new Resena(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                estrellas,
                comentario,
                Instant.parse("2026-11-19T15:00:00Z"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    @DisplayName("Acepta de 1 a 5 estrellas y limpia el comentario")
    void valida(int estrellas) {
        var resena = con(estrellas, "  Muy amables  ");

        assertThat(resena.estrellas()).isEqualTo(estrellas);
        assertThat(resena.comentario()).isEqualTo("Muy amables");
        assertThat(con(estrellas, "   ").comentario()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("Rechaza estrellas fuera de 1 a 5 (REP-004)")
    void estrellasFuera(int estrellas) {
        assertThatThrownBy(() -> con(estrellas, null))
                .isInstanceOf(CalificacionInvalidaException.class)
                .hasFieldOrPropertyWithValue("codigo", "REP-004");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Rechaza un comentario de más de 500 caracteres")
    void comentarioLargo() {
        assertThatThrownBy(() -> con(4, "a".repeat(501))).isInstanceOf(CalificacionInvalidaException.class);
    }
}
