package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte el {@code Usuario} del dominio en la fila de {@code identidad.usuario}. */
@Mapper
interface UsuarioPersistenceMapper {

    @Mapping(target = "correo", source = "correo.valor")
    @Mapping(target = "creadoEn", source = "fechaCreacion")
    @Mapping(target = "actualizadoEn", source = "fechaCreacion")
    UsuarioEntity aEntity(Usuario usuario);
}
