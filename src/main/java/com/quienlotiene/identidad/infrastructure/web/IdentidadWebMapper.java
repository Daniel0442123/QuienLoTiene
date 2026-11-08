package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.application.dto.SegundoFactorPendiente;
import com.quienlotiene.identidad.application.dto.SesionIniciada;
import com.quienlotiene.identidad.application.dto.VerificarSegundoFactorComando;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte los Request y Response HTTP del módulo identidad en comandos y resultados de los casos de uso. */
@Mapper
interface IdentidadWebMapper {

    RegistrarClienteComando aComando(RegistrarClienteRequest peticion);

    UsuarioRegistradoResponse aResponse(ClienteRegistrado clienteRegistrado);

    IniciarSesionComando aComando(IniciarSesionRequest peticion);

    @Mapping(target = "tipoToken", constant = "Bearer")
    @Mapping(target = "fechaExpiracion", source = "fechaExpiracionAcceso")
    @Mapping(target = "segundoFactorRequerido", constant = "false")
    SesionResponse aResponse(SesionIniciada sesion);

    @Mapping(target = "segundoFactorRequerido", constant = "true")
    SegundoFactorPendienteResponse aResponse(SegundoFactorPendiente pendiente);

    VerificarSegundoFactorComando aComando(VerificarSegundoFactorRequest peticion);
}
