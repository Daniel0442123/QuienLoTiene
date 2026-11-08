package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.AutorizacionDatos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
interface AutorizacionDatosPersistenceMapper {

    @Mapping(target = "aceptadaEn", source = "fechaAceptacion")
    @Mapping(target = "creadoEn", source = "fechaAceptacion")
    @Mapping(target = "actualizadoEn", source = "fechaAceptacion")
    AutorizacionDatosEntity aEntity(AutorizacionDatos autorizacion);
}
