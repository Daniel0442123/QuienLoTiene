package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.RespuestasDeSolicitud;
import com.quienlotiene.solicitudes.domain.model.CriterioOrden;
import com.quienlotiene.solicitudes.domain.model.EnvioSolicitud;
import com.quienlotiene.solicitudes.domain.model.SolicitudNoEncontradaException;
import com.quienlotiene.solicitudes.domain.model.TarjetaRespuesta;
import com.quienlotiene.solicitudes.domain.port.EnvioSolicitudRepository;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-020 Ver y ordenar respuestas (CU-05, diagrama 13): solo el cliente dueño; tarjetas SI y PARECIDO con precio,
 * domicilio, total y distancia, ordenadas por precio, distancia o total. Las NO solo cuentan.
 */
@Service
public class ListarRespuestasDeSolicitud {

    private final SolicitudRepository solicitudRepository;
    private final RespuestaRepository respuestaRepository;
    private final EnvioSolicitudRepository envioRepository;
    private final DatosTiendasApi datosTiendasApi;

    ListarRespuestasDeSolicitud(
            SolicitudRepository solicitudRepository,
            RespuestaRepository respuestaRepository,
            EnvioSolicitudRepository envioRepository,
            DatosTiendasApi datosTiendasApi) {
        this.solicitudRepository = solicitudRepository;
        this.respuestaRepository = respuestaRepository;
        this.envioRepository = envioRepository;
        this.datosTiendasApi = datosTiendasApi;
    }

    /**
     * @param orden {@code precio} (por defecto), {@code distancia} o {@code total}
     * @throws SolicitudNoEncontradaException si no existe o es de otro cliente (R-03)
     */
    @Transactional(readOnly = true)
    public RespuestasDeSolicitud ejecutar(UUID clienteId, UUID solicitudId, String orden) {
        var solicitud = solicitudRepository
                .buscarPorId(solicitudId)
                .filter(encontrada -> encontrada.esDe(clienteId))
                .orElseThrow(SolicitudNoEncontradaException::new);
        var respuestas = respuestaRepository.listarDe(solicitudId);
        Map<UUID, Integer> distanciasPorTienda = envioRepository.listarDe(solicitudId).stream()
                .collect(Collectors.toMap(
                        EnvioSolicitud::tiendaId, EnvioSolicitud::distanciaEnMetros, (primera, otra) -> primera));
        var conProducto = respuestas.stream()
                .filter(respuesta -> respuesta.getTipo().ofreceProducto())
                .toList();
        Map<UUID, String> nombresPorTienda =
                datosTiendasApi
                        .buscarPorIds(
                                conProducto.stream().map(r -> r.getTiendaId()).toList())
                        .stream()
                        .collect(Collectors.toMap(
                                DatosTienda::tiendaId, DatosTienda::nombre, (primero, otro) -> primero));

        var tarjetas = conProducto.stream()
                .map(respuesta -> respuesta.aTarjeta(
                        solicitud.getModalidad(), distanciasPorTienda.getOrDefault(respuesta.getTiendaId(), 0)))
                .sorted(CriterioOrden.desde(orden).comparador())
                .map(tarjeta -> aTarjetaParaCliente(tarjeta, nombresPorTienda::get))
                .toList();
        return new RespuestasDeSolicitud(
                solicitud.getId(), solicitud.getEstado().name(), solicitud.getCantidad(), respuestas.size(), tarjetas);
    }

    private static RespuestasDeSolicitud.Tarjeta aTarjetaParaCliente(
            TarjetaRespuesta tarjeta, Function<UUID, String> nombreDeTienda) {
        var nombre = nombreDeTienda.apply(tarjeta.tiendaId());
        return new RespuestasDeSolicitud.Tarjeta(
                tarjeta.respuestaId(),
                tarjeta.tiendaId(),
                nombre == null ? "Tienda" : nombre,
                tarjeta.tipo().name(),
                tarjeta.esAlternativa(),
                tarjeta.descripcionAlternativa(),
                tarjeta.precioEnPesos(),
                tarjeta.cantidadDisponible(),
                tarjeta.subtotalEnPesos(),
                tarjeta.ofreceDomicilio(),
                tarjeta.costoDomicilioEnPesos(),
                tarjeta.totalEnPesos(),
                tarjeta.distanciaEnMetros(),
                tarjeta.tiempoEstimadoEnMinutos(),
                tarjeta.fechaRespuesta(),
                tarjeta.fueElegida());
    }
}
