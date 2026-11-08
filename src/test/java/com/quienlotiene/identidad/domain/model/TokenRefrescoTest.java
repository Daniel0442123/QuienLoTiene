package com.quienlotiene.identidad.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("HU-003")
@DisplayName("HU-003 Token de refresco")
class TokenRefrescoTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final Duration SIETE_DIAS = Duration.ofDays(7);

    @Test
    @DisplayName("Se emite activo, con vencimiento a 7 días y guardando solo el hash")
    void seEmiteActivoYConHash() {
        // Dado
        var valor = ValorTokenRefresco.generar();

        // Cuando
        var token = TokenRefresco.emitir(UUID.randomUUID(), UUID.randomUUID(), valor, SIETE_DIAS, AHORA);

        // Entonces
        assertThat(token.estaRevocado()).isFalse();
        assertThat(token.getFechaExpiracion()).isEqualTo(AHORA.plus(SIETE_DIAS));
        assertThat(token.getFechaCreacion()).isEqualTo(AHORA);
        assertThat(token.getHashToken())
                .hasSize(64)
                .isEqualTo(valor.calcularHash())
                .isNotEqualTo(valor.valor());
    }

    @Test
    @DisplayName("Vence justo en su fecha de expiración")
    void venceEnSuFechaDeExpiracion() {
        var token = TokenRefresco.emitir(
                UUID.randomUUID(), UUID.randomUUID(), ValorTokenRefresco.generar(), SIETE_DIAS, AHORA);

        assertThat(token.estaVencido(AHORA.plus(SIETE_DIAS).minusSeconds(1))).isFalse();
        assertThat(token.estaVencido(AHORA.plus(SIETE_DIAS))).isTrue();
    }

    @Test
    @DisplayName("Un token reconstituido conserva si está revocado")
    void conservaLaRevocacion() {
        var token = TokenRefresco.reconstituir(
                UUID.randomUUID(), UUID.randomUUID(), "hash", AHORA.plus(SIETE_DIAS), true, AHORA);

        assertThat(token.estaRevocado()).isTrue();
        assertThat(token.getUsuarioId()).isNotNull();
        assertThat(token.getId()).isNotNull();
    }

    @Test
    @DisplayName("Cada valor generado es distinto, de 43 caracteres URL-safe, y su hash es estable")
    void generaValoresAleatoriosConHashEstable() {
        var valores = new HashSet<String>();
        for (int intento = 0; intento < 100; intento++) {
            valores.add(ValorTokenRefresco.generar().valor());
        }

        assertThat(valores).hasSize(100).allMatch(valor -> valor.matches("[A-Za-z0-9_-]{43}"));
        var valor = new ValorTokenRefresco("valor-fijo");
        assertThat(valor.calcularHash()).isEqualTo(new ValorTokenRefresco("valor-fijo").calcularHash());
        assertThat(valor.toString()).doesNotContain("valor-fijo");
    }
}
