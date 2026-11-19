package com.quienlotiene.notificaciones.infrastructure.push;

import com.quienlotiene.notificaciones.domain.model.ResultadoPush;
import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import com.quienlotiene.notificaciones.domain.port.ServicioPush;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Adaptador Web Push (HU-031) con el cliente HTTP de la JDK, sin librerías nuevas. Cifra con {@link CifradoWebPush},
 * firma con {@link FirmaVapid} y traduce la respuesta: 2xx entregado, 404 o 410 suscripción vencida, lo demás falla
 * (se registra y se sigue; el aviso igual queda en el flujo SSE para cuando la tienda abra la app).
 */
@Slf4j
@Component
@EnableConfigurationProperties(PropiedadesPush.class)
final class ServicioWebPush implements ServicioPush {

    /** Segundos que el servicio push guarda el aviso si el navegador está apagado: lo que dura una solicitud. */
    static final int TTL_SEGUNDOS = 30 * 60;

    private static final Duration ESPERA = Duration.ofSeconds(5);

    private final HttpClient cliente;
    private final Clock reloj;
    private final CifradoWebPush cifrado = new CifradoWebPush(new SecureRandom());
    private final FirmaVapid firma;
    private final String clavePublica;

    @Autowired
    ServicioWebPush(PropiedadesPush propiedades, Clock reloj) throws GeneralSecurityException {
        this(
                propiedades,
                reloj,
                HttpClient.newBuilder()
                        .connectTimeout(ESPERA)
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build());
    }

    ServicioWebPush(PropiedadesPush propiedades, Clock reloj, HttpClient cliente) throws GeneralSecurityException {
        this.cliente = cliente;
        this.reloj = reloj;
        if (propiedades.tieneClaves()) {
            var decodificador = Base64.getUrlDecoder();
            this.clavePublica = propiedades.clavePublica().strip();
            // Valida al arrancar que la pública sea un punto P-256 y la privada un escalar de 32 bytes
            ClavesP256.publica(decodificador.decode(clavePublica));
            var privada = ClavesP256.privada(
                    decodificador.decode(propiedades.clavePrivada().strip()));
            var sujeto = propiedades.sujeto() == null || propiedades.sujeto().isBlank()
                    ? "mailto:soporte@quienlotiene.tech"
                    : propiedades.sujeto().strip();
            this.firma = new FirmaVapid(privada, clavePublica, sujeto);
        } else {
            this.clavePublica = null;
            this.firma = null;
            log.info("Web Push apagado: faltan las claves VAPID (QLT_VAPID_CLAVE_PUBLICA y QLT_VAPID_CLAVE_PRIVADA)");
        }
    }

    @Override
    public boolean estaHabilitado() {
        return firma != null;
    }

    @Override
    public String clavePublica() {
        return clavePublica;
    }

    @Override
    public ResultadoPush enviar(SuscripcionPush suscripcion, String contenidoJson) {
        if (!estaHabilitado()) {
            return ResultadoPush.FALLO;
        }
        try {
            var endpoint = URI.create(suscripcion.endpoint());
            var cuerpo = cifrado.cifrar(
                    contenidoJson.getBytes(StandardCharsets.UTF_8),
                    suscripcion.clavePublicaNavegador(),
                    suscripcion.secretoNavegador());
            var peticion = HttpRequest.newBuilder(endpoint)
                    .timeout(ESPERA)
                    .header("Authorization", firma.cabecera(endpoint, reloj.instant()))
                    .header("Content-Encoding", "aes128gcm")
                    .header("Content-Type", "application/octet-stream")
                    .header("TTL", Integer.toString(TTL_SEGUNDOS))
                    .header("Urgency", "high")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(cuerpo))
                    .build();
            var respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.discarding());
            return traducir(respuesta.statusCode(), suscripcion);
        } catch (InterruptedException interrumpido) {
            Thread.currentThread().interrupt();
            return ResultadoPush.FALLO;
        } catch (IOException | GeneralSecurityException | IllegalArgumentException falla) {
            log.warn("No se pudo enviar el push de la suscripción {}: {}", suscripcion.id(), falla.getMessage());
            return ResultadoPush.FALLO;
        }
    }

    static ResultadoPush traducir(int estado, SuscripcionPush suscripcion) {
        if (estado >= 200 && estado < 300) {
            return ResultadoPush.ENTREGADO;
        }
        if (estado == 404 || estado == 410) {
            return ResultadoPush.VENCIDA;
        }
        log.warn("El servicio push respondió {} para la suscripción {}", estado, suscripcion.id());
        return ResultadoPush.FALLO;
    }
}
