package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.RespuestaDeTienda;
import com.quienlotiene.solicitudes.domain.port.RespuestaRepository;
import com.quienlotiene.solicitudes.domain.port.SolicitudRepository;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-021 «Mis respuestas» (decisión 7 del README de mockups): respuestas SI y PARECIDO de la tienda en las últimas 24
 * horas con su estado ESPERANDO, ELEGIDA, NO_ELEGIDA o VENCIDA (CP-SOL-021-02).
 */
@Service
public class ListarMisRespuestas {

    static final Duration VENTANA = Duration.ofHours(24);

    private final DatosTiendasApi datosTiendasApi;
    private final RespuestaRepository respuestaRepository;
    private final SolicitudRepository solicitudRepository;
    private final Clock reloj;

    ListarMisRespuestas(
            DatosTiendasApi datosTiendasApi,
            RespuestaRepository respuestaRepository,
            SolicitudRepository solicitudRepository,
            Clock reloj) {
        this.datosTiendasApi = datosTiendasApi;
        this.respuestaRepository = respuestaRepository;
        this.solicitudRepository = solicitudRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<RespuestaDeTienda> ejecutar(UUID duenoId) {
        var tienda = datosTiendasApi.obtenerTiendaDelDueno(duenoId);
        var desde = reloj.instant().minus(VENTANA);
        return respuestaRepository.listarConProductoDeTiendaDesde(tienda.tiendaId(), desde).stream()
                .map(respuesta -> solicitudRepository
                        .buscarPorId(respuesta.getSolicitudId())
                        .map(solicitud -> RespuestasMapper.aRespuestaDeTienda(respuesta, solicitud)))
                .flatMap(Optional::stream)
                .toList();
    }
}
