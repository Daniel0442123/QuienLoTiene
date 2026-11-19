package com.quienlotiene.notificaciones.infrastructure.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.notificaciones.domain.model.ResultadoPush;
import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@Tag("HU-031")
@DisplayName("HU-031 Web Push: cifrado RFC 8291 y firma VAPID RFC 8292")
class WebPushTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private static KeyPair parP256() throws Exception {
        var generador = KeyPairGenerator.getInstance("EC");
        generador.initialize(ClavesP256.curva());
        return generador.generateKeyPair();
    }

    /** Lo que hace el navegador al recibir el push (RFC 8291 sección 3, lado del agente de usuario). */
    private static byte[] descifrarComoNavegador(byte[] cuerpo, KeyPair navegador, byte[] secreto) throws Exception {
        var buffer = ByteBuffer.wrap(cuerpo);
        var sal = new byte[16];
        buffer.get(sal);
        assertThat(buffer.getInt()).isEqualTo(CifradoWebPush.TAMANO_REGISTRO);
        var largoClave = buffer.get();
        var publicaServidor = new byte[largoClave];
        buffer.get(publicaServidor);
        var cifrado = new byte[buffer.remaining()];
        buffer.get(cifrado);

        var acuerdo = KeyAgreement.getInstance("ECDH");
        acuerdo.init(navegador.getPrivate());
        acuerdo.doPhase(ClavesP256.publica(publicaServidor), true);
        var publicaNavegador = ClavesP256.aBytes((ECPublicKey) navegador.getPublic());
        var ikm = CifradoWebPush.expandir(
                CifradoWebPush.hmac(secreto, acuerdo.generateSecret()),
                CifradoWebPush.concatenar(
                        "WebPush: info\0".getBytes(StandardCharsets.US_ASCII), publicaNavegador, publicaServidor),
                32);
        var prk = CifradoWebPush.hmac(sal, ikm);
        var llave =
                CifradoWebPush.expandir(prk, "Content-Encoding: aes128gcm\0".getBytes(StandardCharsets.US_ASCII), 16);
        var nonce = CifradoWebPush.expandir(prk, "Content-Encoding: nonce\0".getBytes(StandardCharsets.US_ASCII), 12);
        var descifrador = Cipher.getInstance("AES/GCM/NoPadding");
        descifrador.init(Cipher.DECRYPT_MODE, new SecretKeySpec(llave, "AES"), new GCMParameterSpec(128, nonce));
        var claro = descifrador.doFinal(cifrado);
        assertThat(claro[claro.length - 1]).isEqualTo((byte) 0x02);
        return Arrays.copyOf(claro, claro.length - 1);
    }

    @Test
    @DisplayName("El navegador descifra el aviso con su clave privada y su secreto auth")
    void cifradoIdaYVuelta() throws Exception {
        var navegador = parP256();
        var secreto = new byte[16];
        new SecureRandom().nextBytes(secreto);
        var contenido = "{\"tipo\":\"solicitud-nueva\",\"id\":\"x\"}".getBytes(StandardCharsets.UTF_8);

        var cuerpo = new CifradoWebPush(new SecureRandom())
                .cifrar(contenido, ClavesP256.aBytes((ECPublicKey) navegador.getPublic()), secreto);

        assertThat(descifrarComoNavegador(cuerpo, navegador, secreto)).isEqualTo(contenido);
    }

    @Test
    @DisplayName("Cifra igual que el vector de prueba del apéndice A de RFC 8291")
    void vectorDeLaRfc8291() throws Exception {
        var d = Base64.getUrlDecoder();
        var publicaServidor =
                d.decode("BP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6TlzAC8wEqKK6PBru3jl7A8");
        var efimera = new KeyPair(
                ClavesP256.publica(publicaServidor),
                ClavesP256.privada(d.decode("yfWPiYE-n46HLnH0KqZOF1fJJU3MYrct3AELtAQ-oRw")));

        var cuerpo = CifradoWebPush.cifrar(
                "When I grow up, I want to be a watermelon".getBytes(StandardCharsets.UTF_8),
                d.decode("BCVxsr7N_eNgVRqvHtD0zTZsEc6-VV-JvLexhqUzORcxaOzi6-AYWXvTBHm4bjyPjs7Vd8pZGH6SRpkNtoIAiw4"),
                d.decode("BTBZMqHH6r4Tts7J_aSIgg"),
                efimera,
                d.decode("DGv6ra1nlYgDCS1FRnbzlw"));

        assertThat(B64.encodeToString(cuerpo))
                .isEqualTo(
                        "DGv6ra1nlYgDCS1FRnbzlwAAEABBBP4z9KsN6nGRTbVYI_c7VJSPQTBtkgcy27mlmlMoZIIgDll6e3vCYLocInmYWAmS6Tl"
                                + "zAC8wEqKK6PBru3jl7A_yl95bQpu6cVPTpK4Mqgkf1CXztLVBSt2Ks3oZwbuwXPXLWyouBWLVWGNWQexSgSxsj_Qulcy4a-fN");
    }

    @Test
    @DisplayName("La cabecera VAPID es un JWT ES256 válido con aud del servicio push, exp a 12 h y la clave pública")
    void firmaVapid() throws Exception {
        var servidor = parP256();
        var publica = B64.encodeToString(ClavesP256.aBytes((ECPublicKey) servidor.getPublic()));

        var cabecera = new FirmaVapid((ECPrivateKey) servidor.getPrivate(), publica, "mailto:soporte@ejemplo.com")
                .cabecera(URI.create("https://fcm.googleapis.com/fcm/send/abc"), AHORA);

        assertThat(cabecera).startsWith("vapid t=").endsWith(", k=" + publica);
        var jwt = cabecera.substring("vapid t=".length(), cabecera.indexOf(", k="));
        var partes = jwt.split("\\.");
        var reclamos = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        assertThat(reclamos)
                .contains("\"aud\":\"https://fcm.googleapis.com\"")
                .contains("\"exp\":" + AHORA.plus(FirmaVapid.VIGENCIA).getEpochSecond())
                .contains("\"sub\":\"mailto:soporte@ejemplo.com\"");
        var verificador = Signature.getInstance("SHA256withECDSAinP1363Format");
        verificador.initVerify(servidor.getPublic());
        verificador.update((partes[0] + "." + partes[1]).getBytes(StandardCharsets.US_ASCII));
        assertThat(verificador.verify(Base64.getUrlDecoder().decode(partes[2]))).isTrue();
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("Envía con aes128gcm, TTL y VAPID; 201 es entregado, 410 vencida y 500 falla")
    void enviaYTraduce() throws Exception {
        var servidor = parP256();
        var escalar = ((ECPrivateKey) servidor.getPrivate()).getS().toByteArray();
        var privada = Arrays.copyOfRange(escalar, Math.max(0, escalar.length - 32), escalar.length);
        if (privada.length < 32) {
            var relleno = new byte[32];
            System.arraycopy(privada, 0, relleno, 32 - privada.length, privada.length);
            privada = relleno;
        }
        var propiedades = new PropiedadesPush(
                B64.encodeToString(ClavesP256.aBytes((ECPublicKey) servidor.getPublic())),
                B64.encodeToString(privada),
                "");
        var cliente = mock(HttpClient.class);
        var respuesta = mock(HttpResponse.class);
        when(cliente.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(respuesta);
        when(respuesta.statusCode()).thenReturn(201, 410, 500);
        var servicio = new ServicioWebPush(propiedades, Clock.fixed(AHORA, ZoneOffset.UTC), cliente);
        var navegador = parP256();
        var suscripcion = new SuscripcionPush(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "https://fcm.googleapis.com/fcm/send/abc",
                B64.encodeToString(ClavesP256.aBytes((ECPublicKey) navegador.getPublic())),
                B64.encodeToString(new byte[16]),
                AHORA);

        assertThat(servicio.estaHabilitado()).isTrue();
        assertThat(servicio.enviar(suscripcion, "{}")).isEqualTo(ResultadoPush.ENTREGADO);
        assertThat(servicio.enviar(suscripcion, "{}")).isEqualTo(ResultadoPush.VENCIDA);
        assertThat(servicio.enviar(suscripcion, "{}")).isEqualTo(ResultadoPush.FALLO);

        var peticion = ArgumentCaptor.forClass(HttpRequest.class);
        verify(cliente, org.mockito.Mockito.times(3)).send(peticion.capture(), any(HttpResponse.BodyHandler.class));
        var cabeceras = peticion.getValue().headers();
        assertThat(cabeceras.firstValue("Content-Encoding")).hasValue("aes128gcm");
        assertThat(cabeceras.firstValue("TTL")).hasValue("1800");
        assertThat(cabeceras.firstValue("Authorization"))
                .hasValueSatisfying(valor -> assertThat(valor).startsWith("vapid t="));
    }

    @Test
    @DisplayName("Sin claves VAPID el push queda apagado y no envía nada")
    void apagadoSinClaves() throws Exception {
        var cliente = mock(HttpClient.class);
        var servicio = new ServicioWebPush(new PropiedadesPush("", "", null), Clock.systemUTC(), cliente);

        assertThat(servicio.estaHabilitado()).isFalse();
        assertThat(servicio.clavePublica()).isNull();
        assertThat(servicio.enviar(
                        new SuscripcionPush(UUID.randomUUID(), UUID.randomUUID(), "https://x", "p", "a", AHORA), "{}"))
                .isEqualTo(ResultadoPush.FALLO);
        org.mockito.Mockito.verifyNoInteractions(cliente);
    }
}
