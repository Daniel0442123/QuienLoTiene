package com.quienlotiene.notificaciones.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("HU-031")
@DisplayName("HU-031 Suscripción push")
class SuscripcionPushTest {

    static final String P256DH = Base64.getUrlEncoder().withoutPadding().encodeToString(punto());
    static final String AUTH = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[16]);

    private static byte[] punto() {
        var punto = new byte[65];
        punto[0] = 0x04;
        return punto;
    }

    private static SuscripcionPush suscribir(String endpoint, String p256dh, String auth) {
        return SuscripcionPush.suscribir(UUID.randomUUID(), UUID.randomUUID(), endpoint, p256dh, auth, Instant.now());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://fcm.googleapis.com/fcm/send/abc",
                "https://updates.push.services.mozilla.com/wpush/v2/abc",
                "https://web.push.apple.com/QGx",
                "https://wns2-par02p.notify.windows.com/w/?token=abc"
            })
    @DisplayName("Acepta los servicios push de Chrome, Firefox, Safari y Edge")
    void aceptaServiciosConocidos(String endpoint) {
        assertThat(suscribir(endpoint, P256DH, AUTH).endpoint()).isEqualTo(endpoint);
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "http://fcm.googleapis.com/fcm/send/abc",
                "https://169.254.169.254/latest/meta-data",
                "https://localhost/push",
                "https://fcm.googleapis.com.atacante.com/x",
                "https://atacante.com/fcm.googleapis.com",
                "https://usuario@fcm.googleapis.com/x",
                "https://fcm.googleapis.com:8443/x",
                "no es una url",
                " "
            })
    @DisplayName("Rechaza endpoints que no son de un servicio push (SSRF) con NOT-001")
    void rechazaOtrosEndpoints(String endpoint) {
        assertThatThrownBy(() -> suscribir(endpoint, P256DH, AUTH))
                .isInstanceOf(SuscripcionPushInvalidaException.class)
                .hasFieldOrPropertyWithValue("codigo", "NOT-001");
    }

    @Test
    @DisplayName("Las claves deben tener 65 y 16 bytes en Base64 URL")
    void validaLasClaves() {
        var endpoint = "https://fcm.googleapis.com/fcm/send/abc";
        assertThatThrownBy(() -> suscribir(endpoint, AUTH, AUTH)).isInstanceOf(SuscripcionPushInvalidaException.class);
        assertThatThrownBy(() -> suscribir(endpoint, P256DH, "%%"))
                .isInstanceOf(SuscripcionPushInvalidaException.class);
        assertThatThrownBy(() -> suscribir(endpoint, P256DH, null))
                .isInstanceOf(SuscripcionPushInvalidaException.class);
        assertThat(suscribir(endpoint, P256DH, AUTH).secretoNavegador()).hasSize(16);
    }
}
