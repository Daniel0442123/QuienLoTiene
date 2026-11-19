package com.quienlotiene.notificaciones.domain.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * HU-031 (CU-24, diagrama 28): la suscripción Web Push de un navegador. El {@code endpoint} es la dirección del
 * servicio push del navegador; {@code p256dh} y {@code auth} son las claves con que se cifra cada aviso (RFC 8291).
 *
 * <p>El servidor hace un POST al {@code endpoint}, así que solo se aceptan HTTPS y los servicios push conocidos de
 * los navegadores: un endpoint cualquiera abriría la puerta a peticiones hacia la red interna (SSRF, OWASP A10).
 */
public record SuscripcionPush(UUID id, UUID usuarioId, String endpoint, String p256dh, String auth, Instant fecha) {

    static final int LARGO_MAXIMO_ENDPOINT = 1000;
    private static final int BYTES_CLAVE_PUBLICA = 65;
    private static final int BYTES_SECRETO = 16;

    /** Servicios push de Chrome, Edge, Firefox y Safari. Un subdominio de estos también vale. */
    static final Set<String> SERVICIOS_PERMITIDOS = Set.of(
            "fcm.googleapis.com",
            "updates.push.services.mozilla.com",
            "push.services.mozilla.com",
            "notify.windows.com",
            "push.apple.com");

    public SuscripcionPush {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(usuarioId, "usuarioId");
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(p256dh, "p256dh");
        Objects.requireNonNull(auth, "auth");
        Objects.requireNonNull(fecha, "fecha");
    }

    /**
     * @throws SuscripcionPushInvalidaException si el endpoint no es de un servicio push conocido o las claves no tienen
     *     el tamaño de la especificación
     */
    public static SuscripcionPush suscribir(
            UUID id, UUID usuarioId, String endpoint, String p256dh, String auth, Instant ahora) {
        exigirEndpointPermitido(endpoint);
        exigirBytes(p256dh, BYTES_CLAVE_PUBLICA, "p256dh");
        exigirBytes(auth, BYTES_SECRETO, "auth");
        return new SuscripcionPush(id, usuarioId, endpoint.strip(), p256dh.strip(), auth.strip(), ahora);
    }

    public byte[] clavePublicaNavegador() {
        return Base64.getUrlDecoder().decode(p256dh);
    }

    public byte[] secretoNavegador() {
        return Base64.getUrlDecoder().decode(auth);
    }

    private static void exigirEndpointPermitido(String endpoint) {
        if (endpoint == null || endpoint.isBlank() || endpoint.length() > LARGO_MAXIMO_ENDPOINT) {
            throw new SuscripcionPushInvalidaException("falta el endpoint o es demasiado largo");
        }
        URI uri;
        try {
            uri = new URI(endpoint.strip());
        } catch (URISyntaxException malFormado) {
            throw new SuscripcionPushInvalidaException("el endpoint no es una URL válida");
        }
        var host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        var esConocido = SERVICIOS_PERMITIDOS.stream()
                .anyMatch(servicio -> host.equals(servicio) || host.endsWith("." + servicio));
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || uri.getUserInfo() != null
                || uri.getPort() != -1
                || !esConocido) {
            throw new SuscripcionPushInvalidaException("el endpoint no es de un servicio push de navegador conocido");
        }
    }

    private static void exigirBytes(String valor, int bytes, String campo) {
        try {
            if (valor != null && Base64.getUrlDecoder().decode(valor.strip()).length == bytes) {
                return;
            }
        } catch (IllegalArgumentException noEsBase64) {
            // cae al rechazo de abajo
        }
        throw new SuscripcionPushInvalidaException("la clave " + campo + " no es válida");
    }
}
