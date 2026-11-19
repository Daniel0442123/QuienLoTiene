package com.quienlotiene.notificaciones.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.notificaciones.application.dto.ConfiguracionPush;
import com.quienlotiene.notificaciones.domain.model.ResultadoPush;
import com.quienlotiene.notificaciones.domain.model.SuscripcionPush;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import com.quienlotiene.notificaciones.domain.port.ServicioPush;
import com.quienlotiene.notificaciones.domain.port.SuscripcionPushRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-031")
@DisplayName("HU-031 Notificaciones push web")
class AvisarPorPushTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID DUENO = UUID.randomUUID();
    private static final UUID SOLICITUD = UUID.randomUUID();

    @Mock
    CanalTiempoReal canal;

    @Mock
    ServicioPush servicioPush;

    @Mock
    SuscripcionPushRepository suscripcionPushRepository;

    private AvisarPorPush avisar() {
        return new AvisarPorPush(canal, servicioPush, suscripcionPushRepository);
    }

    private static SuscripcionPush suscripcion() {
        return new SuscripcionPush(UUID.randomUUID(), DUENO, "https://fcm.googleapis.com/fcm/send/a", "p", "a", AHORA);
    }

    @Test
    @Tag("CP-NOT-031-01")
    @DisplayName("CP-NOT-031-01 Con la app cerrada, la tienda recibe por push la solicitud nueva con solo tipo e id")
    void pushConLaAppCerrada() {
        // Dado
        var navegador = suscripcion();
        when(servicioPush.estaHabilitado()).thenReturn(true);
        when(canal.estaConectado(DUENO)).thenReturn(false);
        when(suscripcionPushRepository.listarPorUsuario(DUENO)).thenReturn(List.of(navegador));
        when(servicioPush.enviar(any(), any())).thenReturn(ResultadoPush.ENTREGADO);
        // Cuando
        avisar().siNoEstaConectado(DUENO, "solicitud-nueva", SOLICITUD);
        // Entonces
        var contenido = ArgumentCaptor.forClass(String.class);
        verify(servicioPush).enviar(org.mockito.ArgumentMatchers.eq(navegador), contenido.capture());
        assertThat(contenido.getValue()).isEqualTo("{\"tipo\":\"solicitud-nueva\",\"id\":\"" + SOLICITUD + "\"}");
        verify(suscripcionPushRepository, never()).eliminar(any(UUID.class));
    }

    @Test
    @Tag("CP-NOT-031-02")
    @DisplayName("CP-NOT-031-02 Una tienda que no autorizó notificaciones no tiene suscripción y no se envía push")
    void sinAutorizacion() {
        when(servicioPush.estaHabilitado()).thenReturn(true);
        when(suscripcionPushRepository.listarPorUsuario(DUENO)).thenReturn(List.of());

        avisar().siNoEstaConectado(DUENO, "solicitud-nueva", SOLICITUD);

        verify(servicioPush, never()).enviar(any(), any());
    }

    @Test
    @DisplayName("Con la app abierta basta el SSE: no se envía push")
    void conLaAppAbierta() {
        when(servicioPush.estaHabilitado()).thenReturn(true);
        when(canal.estaConectado(DUENO)).thenReturn(true);

        avisar().siNoEstaConectado(DUENO, "solicitud-nueva", SOLICITUD);

        verifyNoInteractions(suscripcionPushRepository);
    }

    @Test
    @DisplayName("Una suscripción que el servicio da por vencida (410) se borra; sin claves VAPID no se hace nada")
    void borraLaVencida() {
        var navegador = suscripcion();
        when(servicioPush.estaHabilitado()).thenReturn(true, false);
        when(suscripcionPushRepository.listarPorUsuario(DUENO)).thenReturn(List.of(navegador));
        when(servicioPush.enviar(any(), any())).thenReturn(ResultadoPush.VENCIDA);

        avisar().siNoEstaConectado(DUENO, "orden-creada", SOLICITUD);
        verify(suscripcionPushRepository).eliminar(navegador.id());

        avisar().siNoEstaConectado(DUENO, "orden-creada", SOLICITUD);
        verify(suscripcionPushRepository).listarPorUsuario(DUENO);
    }

    @Test
    @DisplayName("Suscribir guarda la suscripción, darse de baja la borra y la configuración trae la clave pública")
    void gestionaSuscripciones() {
        var gestionar = new GestionarSuscripcionesPush(
                suscripcionPushRepository, servicioPush, Clock.fixed(AHORA, ZoneOffset.UTC));
        var p256dh = com.quienlotiene.notificaciones.domain.model.SuscripcionPushTestDatos.P256DH;
        var auth = com.quienlotiene.notificaciones.domain.model.SuscripcionPushTestDatos.AUTH;

        gestionar.suscribir(DUENO, "https://fcm.googleapis.com/fcm/send/a", p256dh, auth);
        var guardada = ArgumentCaptor.forClass(SuscripcionPush.class);
        verify(suscripcionPushRepository).guardar(guardada.capture());
        assertThat(guardada.getValue().usuarioId()).isEqualTo(DUENO);

        gestionar.cancelar(DUENO, "https://fcm.googleapis.com/fcm/send/a");
        verify(suscripcionPushRepository).eliminar(DUENO, "https://fcm.googleapis.com/fcm/send/a");

        when(servicioPush.estaHabilitado()).thenReturn(true, false);
        when(servicioPush.clavePublica()).thenReturn("BPub");
        assertThat(gestionar.configuracion()).isEqualTo(new ConfiguracionPush(true, "BPub"));
        assertThat(gestionar.configuracion()).isEqualTo(new ConfiguracionPush(false, null));
    }
}
