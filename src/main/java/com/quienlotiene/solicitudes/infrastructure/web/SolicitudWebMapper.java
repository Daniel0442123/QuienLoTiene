package com.quienlotiene.solicitudes.infrastructure.web;

import com.quienlotiene.solicitudes.application.dto.PublicarSolicitudComando;
import com.quienlotiene.solicitudes.application.dto.SolicitudPublicada;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte el Request y el Response HTTP de solicitudes en el comando y el resultado del caso de uso. */
@Mapper
interface SolicitudWebMapper {

    @Mapping(target = "clienteId", source = "clienteId")
    @Mapping(target = "latitud", source = "peticion.ubicacion.latitud")
    @Mapping(target = "longitud", source = "peticion.ubicacion.longitud")
    @Mapping(target = "esUrgente", source = "peticion.esUrgente", defaultValue = "false")
    PublicarSolicitudComando aComando(PublicarSolicitudRequest peticion, UUID clienteId);

    @Mapping(target = "ubicacion.latitud", source = "latitud")
    @Mapping(target = "ubicacion.longitud", source = "longitud")
    SolicitudResponse aResponse(SolicitudPublicada solicitud);
}
