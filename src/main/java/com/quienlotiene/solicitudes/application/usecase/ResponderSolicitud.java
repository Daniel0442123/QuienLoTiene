package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.api.RespuestaRecibidaEvent;
import com.quienlotiene.solicitudes.application.dto.ResponderSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.RespuestaDeTienda;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.RespuestaDuplicadaException;
import com.quienlotiene.solicitudes.domain.model.RespuestaIncompletaException;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.TipoRespuesta;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-019 Responder solicitud (diagrama 03 pasos 13 a 17): solo la tienda a la que llegó, una sola vez y mientras esté
 * abierta. Calcula el domicilio con la configuración de la tienda (HU-010) y avisa al cliente con
 * {@link RespuestaRecibidaEvent}.
 */
@Slf4j
@Service
public class ResponderSolicitud {

    private final DatosTiendasApi datosTiendasApi;
    private final SolicitudRepository solicitudRepository;
    private final EnvioSolicitudRepository envioRepository;
    private final RespuestaRepository respuestaRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    ResponderSolicitud(
            DatosTiendasApi datosTiendasApi,
            SolicitudRepository solicitudRepository,
            EnvioSolicitudRepository envioRepository,
            RespuestaRepository respuestaRepository,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.datosTiendasApi = datosTiendasApi;
        this.solicitudRepository = solicitudRepository;
        this.envioRepository = envioRepository;
        this.respuestaRepository = respuestaRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Transactional
    public RespuestaDeTienda ejecutar(ResponderSolicitudComando comando) {
        var ahora = reloj.instant();
        var tienda = datosTiendasApi.obtenerTiendaDelDueno(comando.duenoId());
        var solicitud = solicitudRepository
                .buscarPorId(comando.solicitudId())
                .filter(encontrada -> envioRepository
                        .buscar(encontrada.getId(), tienda.tiendaId())
                        .isPresent())
                .orElseThrow(SolicitudNoEncontradaException::new);
        if (respuestaRepository.existe(solicitud.getId(), tienda.tiendaId())) {
            throw new RespuestaDuplicadaException();
        }

        var respuesta = Respuesta.responder(
                UUID.randomUUID(),
                solicitud,
                tienda.tiendaId(),
                tienda.duenoId(),
                tipoDe(comando.tipo()),
                comando.precioEnPesos(),
                comando.cantidadDisponible(),
                comando.descripcionAlternativa(),
                comando.tiempoEstimadoEnMinutos(),
                null,
                ahora);
        // HU-023: a un producto restringido no se le cotiza domicilio (CP-SOL-023-02)
        if (respuesta.getTipo().ofreceProducto() && !solicitud.esRestringida()) {
            var costoDomicilio = datosTiendasApi
                    .cotizarDomicilio(
                            tienda.tiendaId(),
                            solicitud.getUbicacion().latitud(),
                            solicitud.getUbicacion().longitud(),
                            respuesta.subtotalEnPesos())
                    .orElse(null);
            respuesta = respuesta.conCostoDomicilio(costoDomicilio);
        }
        respuestaRepository.guardarNueva(respuesta);
        publicadorEventos.publishEvent(new RespuestaRecibidaEvent(
                solicitud.getId(),
                respuesta.getId(),
                solicitud.getClienteId(),
                respuesta.getTipo().name(),
                tienda.nombre()));
        log.info("Tienda {} respondió {} a la solicitud {}", tienda.tiendaId(), respuesta.getTipo(), solicitud.getId());

        return RespuestasMapper.aRespuestaDeTienda(respuesta, solicitud);
    }

    private static TipoRespuesta tipoDe(String tipo) {
        if (tipo == null) {
            return null;
        }
        try {
            return TipoRespuesta.valueOf(tipo);
        } catch (IllegalArgumentException tipoDesconocido) {
            throw new RespuestaIncompletaException("el tipo debe ser SI, PARECIDO o NO");
        }
    }
}
