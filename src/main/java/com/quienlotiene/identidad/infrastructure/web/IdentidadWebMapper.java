package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.IniciarSesionComando;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import com.quienlotiene.identidad.application.dto.SesionIniciada;
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
    SesionResponse aResponse(SesionIniciada sesion);
}
