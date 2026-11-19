package com.quienlotiene.notificaciones.infrastructure.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Claves VAPID de Web Push (HU-031). Sin claves, el push queda apagado y el resto de la API funciona igual.
 *
 * @param clavePublica punto P-256 sin comprimir (65 bytes) en Base64 URL
 * @param clavePrivada escalar P-256 (32 bytes) en Base64 URL
 * @param sujeto contacto del servidor para el servicio push, por ejemplo {@code mailto:soporte@dominio}
 */
@ConfigurationProperties("quienlotiene.notificaciones.push")
public record PropiedadesPush(String clavePublica, String clavePrivada, String sujeto) {

    public boolean tieneClaves() {
        return clavePublica != null && !clavePublica.isBlank() && clavePrivada != null && !clavePrivada.isBlank();
    }
}
