package com.quienlotiene.solicitudes.application.usecase;

import com.quienlotiene.solicitudes.application.dto.RespuestaDeTienda;
import com.quienlotiene.solicitudes.domain.model.Respuesta;
import com.quienlotiene.solicitudes.domain.model.Solicitud;

/** Convierte respuestas del dominio en lo que ve la tienda. */
final class RespuestasMapper {

    private RespuestasMapper() {}

    static RespuestaDeTienda aRespuestaDeTienda(Respuesta respuesta, Solicitud solicitud) {
        return new RespuestaDeTienda(
                respuesta.getId(),
                solicitud.getId(),
                solicitud.getProducto(),
                solicitud.getCantidad(),
                respuesta.getTipo().name(),
                respuesta.getPrecioEnPesos(),
                respuesta.getCantidadDisponible(),
                respuesta.getDescripcionAlternativa(),
                respuesta.getTiempoEstimadoEnMinutos(),
                respuesta.ofreceDomicilio(),
                respuesta.getCostoDomicilioEnPesos(),
                respuesta.estadoSegun(solicitud.getEstado()).name(),
                respuesta.getFechaRespuesta(),
                respuesta.getOrdenId());
    }
}
