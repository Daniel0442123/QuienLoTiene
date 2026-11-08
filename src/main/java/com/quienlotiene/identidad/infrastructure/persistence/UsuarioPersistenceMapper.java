package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.EstadoCuenta;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Convierte el {@code Usuario} del dominio en la fila de {@code identidad.usuario} y al revés. */
@Mapper
interface UsuarioPersistenceMapper {

    @Mapping(target = "correo", source = "correo.valor")
    @Mapping(target = "creadoEn", source = "fechaCreacion")
    @Mapping(target = "actualizadoEn", source = "fechaCreacion")
    UsuarioEntity aEntity(Usuario usuario);

    /** El dominio solo se crea por sus fábricas, así que esta conversión se escribe a mano. */
    default Usuario aDominio(UsuarioEntity fila) {
        return Usuario.reconstituir(
                fila.getId(),
                fila.getNombre(),
                new Correo(fila.getCorreo()),
                fila.getTelefono(),
                fila.getHashContrasena(),
                Rol.valueOf(fila.getRol()),
                EstadoCuenta.valueOf(fila.getEstado()),
                fila.getCreadoEn());
    }
}
