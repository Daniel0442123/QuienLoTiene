package com.quienlotiene.notificaciones.infrastructure.push;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Cabecera {@code Authorization: vapid t=..., k=...} (RFC 8292): un JWT ES256 con el origen del servicio push como
 * {@code aud}, vencimiento de 12 horas y el contacto del servidor como {@code sub}.
 */
final class FirmaVapid {

    static final Duration VIGENCIA = Duration.ofHours(12);
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final String CABECERA_JWT =
            BASE64_URL.encodeToString("{\"typ\":\"JWT\",\"alg\":\"ES256\"}".getBytes(StandardCharsets.UTF_8));

    private final ECPrivateKey clavePrivada;
    private final String clavePublica;
    private final String sujeto;

    FirmaVapid(ECPrivateKey clavePrivada, String clavePublica, String sujeto) {
        this.clavePrivada = clavePrivada;
        this.clavePublica = clavePublica;
        this.sujeto = sujeto;
    }

    String cabecera(URI endpoint, Instant ahora) throws GeneralSecurityException {
        var audiencia = endpoint.getScheme() + "://" + endpoint.getHost();
        var reclamos = "{\"aud\":\"" + audiencia + "\",\"exp\":"
                + ahora.plus(VIGENCIA).getEpochSecond() + ",\"sub\":\"" + sujeto + "\"}";
        var sinFirma = CABECERA_JWT + "." + BASE64_URL.encodeToString(reclamos.getBytes(StandardCharsets.UTF_8));
        // P1363: la firma va como R||S de 64 bytes, que es lo que pide JWS (no DER)
        var firmador = Signature.getInstance("SHA256withECDSAinP1363Format");
        firmador.initSign(clavePrivada);
        firmador.update(sinFirma.getBytes(StandardCharsets.US_ASCII));
        var jwt = sinFirma + "." + BASE64_URL.encodeToString(firmador.sign());
        return "vapid t=" + jwt + ", k=" + clavePublica;
    }
}
