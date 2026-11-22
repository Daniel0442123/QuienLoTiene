package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.IndicadoresTienda;
import com.quienlotiene.solicitudes.domain.port.ActividadDeTiendaRepository;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-015 Ver resumen de indicadores (CU-23): solicitudes recibidas, respondidas y ganadas de la tienda en los últimos 7
 * o 30 días, con los porcentajes «Respondiste» y «Te eligieron» de SC-011 punto 6. Vive en solicitudes porque los
 * conteos son de su esquema y tiendas no puede depender de solicitudes sin crear un ciclo entre módulos.
 */
@Service
public class ConsultarIndicadoresTienda {

    private final DatosTiendasApi datosTiendasApi;
    private final ActividadDeTiendaRepository actividadRepository;
    private final Clock reloj;

    ConsultarIndicadoresTienda(
            DatosTiendasApi datosTiendasApi, ActividadDeTiendaRepository actividadRepository, Clock reloj) {
        this.datosTiendasApi = datosTiendasApi;
        this.actividadRepository = actividadRepository;
        this.reloj = reloj;
    }

    /** @param periodoEnDias 7 o 30; el controlador ya lo validó */
    @Transactional(readOnly = true)
    public IndicadoresTienda ejecutar(UUID duenoId, int periodoEnDias) {
        var tienda = datosTiendasApi.obtenerTiendaDelDueno(duenoId);
        var desde = reloj.instant().minus(Duration.ofDays(periodoEnDias));
        var actividad = actividadRepository.contarDesde(tienda.tiendaId(), desde);
        return new IndicadoresTienda(
                periodoEnDias,
                actividad.recibidas(),
                actividad.respondidas(),
                actividad.respuestasConProducto(),
                actividad.ganadas(),
                actividad.porcentajeRespondiste(),
                actividad.porcentajeTeEligieron());
    }
}
