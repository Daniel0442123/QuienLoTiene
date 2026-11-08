package com.quienlotiene.identidad.infrastructure.web;

import com.quienlotiene.identidad.application.dto.ClienteRegistrado;
import com.quienlotiene.identidad.application.dto.RegistrarClienteComando;
import org.mapstruct.Mapper;

/** Convierte los Request y Response HTTP del módulo identidad en comandos y resultados de los casos de uso. */
@Mapper
interface IdentidadWebMapper {

    RegistrarClienteComando aComando(RegistrarClienteRequest peticion);

    UsuarioRegistradoResponse aResponse(ClienteRegistrado clienteRegistrado);
}
