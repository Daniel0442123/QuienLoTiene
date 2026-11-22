package com.quienlotiene.notificaciones.application.usecase;

import com.quienlotiene.mensajeria.api.MensajeEnviadoEvent;
import com.quienlotiene.notificaciones.application.dto.EventosTiempoReal;
import com.quienlotiene.notificaciones.domain.model.Notificacion;
import com.quienlotiene.notificaciones.domain.port.CanalTiempoReal;
import com.quienlotiene.ordenes.api.EstadoOrdenCambiadoEvent;
import com.quienlotiene.ordenes.api.OrdenCreadaEvent;
import com.quienlotiene.solicitudes.api.RadioAmpliadoEvent;
import com.quienlotiene.solicitudes.api.RespuestaElegidaEvent;
import com.quienlotiene.solicitudes.api.RespuestaRecibidaEvent;
import com.quienlotiene.solicitudes.api.SolicitudDifundidaEvent;
import com.quienlotiene.solicitudes.api.SolicitudVencidaEvent;
import java.time.Clock;
import java.util.UUID;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

/**
 * HU-030 Notificar en tiempo real (CU-14, diagrama 20, patrón Observer): escucha los eventos de solicitudes, ordenes y mensajeria
 * después de que su transacción se confirma, decide a quién le toca cada aviso y lo entrega por el canal SSE. Los
 * eventos ya traen lo necesario, así que no consulta otros módulos (DECISIONES-BACKEND 4.3). A la tienda sin la app
 * abierta le llegan además por push la solicitud nueva, la orden creada y los cambios de estado (HU-031, diagrama 28).
 */
@Service
public class NotificarEnTiempoReal {

    private static final String RESUELTA = "RESUELTA";
    private static final String VENCIDA = "VENCIDA";
    private static final String NO_LO_TENGO = "NO";

    private final CanalTiempoReal canal;
    private final AvisarPorPush avisarPorPush;
    private final Clock reloj;

    NotificarEnTiempoReal(CanalTiempoReal canal, AvisarPorPush avisarPorPush, Clock reloj) {
        this.canal = canal;
        this.avisarPorPush = avisarPorPush;
        this.reloj = reloj;
    }

    @ApplicationModuleListener
    public void alDifundirSolicitud(SolicitudDifundidaEvent evento) {
        for (var destinatario : evento.destinatarios()) {
            avisar(
                    destinatario.duenoId(),
                    EventosTiempoReal.SolicitudNueva.TIPO,
                    new EventosTiempoReal.SolicitudNueva(
                            evento.solicitudId(),
                            evento.producto(),
                            evento.cantidad(),
                            evento.modalidad(),
                            evento.esUrgente(),
                            destinatario.distanciaAproximadaEnMetros(),
                            evento.fechaVencimiento()));
            avisarPorPush.siNoEstaConectado(
                    destinatario.duenoId(), EventosTiempoReal.SolicitudNueva.TIPO, evento.solicitudId());
        }
    }

    @ApplicationModuleListener
    public void alRecibirRespuesta(RespuestaRecibidaEvent evento) {
        if (NO_LO_TENGO.equals(evento.tipo())) {
            return;
        }
        avisar(
                evento.clienteId(),
                EventosTiempoReal.RespuestaRecibida.TIPO,
                new EventosTiempoReal.RespuestaRecibida(
                        evento.solicitudId(), evento.respuestaId(), evento.tipo(), evento.nombreTienda()));
    }

    @ApplicationModuleListener
    public void alElegirRespuesta(RespuestaElegidaEvent evento) {
        for (var dueno : evento.duenosQueLaRecibieron()) {
            avisar(
                    dueno,
                    EventosTiempoReal.SolicitudCerrada.TIPO,
                    new EventosTiempoReal.SolicitudCerrada(evento.solicitudId(), RESUELTA));
        }
        for (var noElegida : evento.noElegidas()) {
            avisar(
                    noElegida.duenoId(),
                    EventosTiempoReal.RespuestaNoElegida.TIPO,
                    new EventosTiempoReal.RespuestaNoElegida(evento.solicitudId(), noElegida.respuestaId()));
        }
    }

    @ApplicationModuleListener
    public void alVencerSolicitud(SolicitudVencidaEvent evento) {
        avisar(
                evento.clienteId(),
                EventosTiempoReal.SolicitudVencida.TIPO,
                new EventosTiempoReal.SolicitudVencida(evento.solicitudId(), evento.respuestasRecibidas()));
        for (var dueno : evento.duenosQueLaRecibieron()) {
            avisar(
                    dueno,
                    EventosTiempoReal.SolicitudCerrada.TIPO,
                    new EventosTiempoReal.SolicitudCerrada(evento.solicitudId(), VENCIDA));
        }
    }

    /** HU-022: a las tiendas nuevas les llega la solicitud por {@link #alDifundirSolicitud}; aquí solo el cliente. */
    @ApplicationModuleListener
    public void alAmpliarRadio(RadioAmpliadoEvent evento) {
        avisar(
                evento.clienteId(),
                EventosTiempoReal.RadioAmpliado.TIPO,
                new EventosTiempoReal.RadioAmpliado(
                        evento.solicitudId(), evento.radioEnMetros(), evento.tiendasNuevas()));
    }

    @ApplicationModuleListener
    public void alCrearOrden(OrdenCreadaEvent evento) {
        avisar(
                evento.duenoTiendaId(),
                EventosTiempoReal.OrdenCreada.TIPO,
                new EventosTiempoReal.OrdenCreada(
                        evento.ordenId(),
                        evento.solicitudId(),
                        evento.producto(),
                        evento.totalEnPesos(),
                        evento.modalidad()));
        avisarPorPush.siNoEstaConectado(evento.duenoTiendaId(), EventosTiempoReal.OrdenCreada.TIPO, evento.ordenId());
    }

    @ApplicationModuleListener
    public void alCambiarEstadoOrden(EstadoOrdenCambiadoEvent evento) {
        var datos = new EventosTiempoReal.EstadoOrdenCambiado(evento.ordenId(), evento.estado(), evento.motivo());
        avisar(evento.clienteId(), EventosTiempoReal.EstadoOrdenCambiado.TIPO, datos);
        avisar(evento.duenoTiendaId(), EventosTiempoReal.EstadoOrdenCambiado.TIPO, datos);
        avisarPorPush.siNoEstaConectado(
                evento.duenoTiendaId(), EventosTiempoReal.EstadoOrdenCambiado.TIPO, evento.ordenId());
    }

    @ApplicationModuleListener
    public void alEnviarMensaje(MensajeEnviadoEvent evento) {
        avisar(
                evento.destinatarioId(),
                EventosTiempoReal.MensajeRecibido.TIPO,
                new EventosTiempoReal.MensajeRecibido(evento.ordenId(), evento.mensajeId()));
    }

    private void avisar(UUID usuarioId, String tipo, Object datos) {
        if (usuarioId == null) {
            return;
        }
        canal.enviar(new Notificacion(UUID.randomUUID().toString(), usuarioId, tipo, datos, reloj.instant()));
    }
}
