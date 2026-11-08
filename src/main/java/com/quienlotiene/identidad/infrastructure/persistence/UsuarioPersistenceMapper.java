package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.EstadoCuenta;
import com.quienlotiene.identidad.domain.model.Rol;
import com.quienlotiene.identidad.domain.model.Usuario;
import org.springframework.stereotype.Component;

/**
 * Convierte el {@code Usuario} del dominio en la fila de {@code identidad.usuario} y al revés. Se escribe a mano: el
 * dominio solo se crea por sus fábricas y el secreto TOTP es opcional.
 */
@Component
class UsuarioPersistenceMapper {

    UsuarioEntity aEntity(Usuario usuario) {
        return new UsuarioEntity(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo().valor(),
                usuario.getTelefono(),
                usuario.getHashContrasena(),
                usuario.getRol().name(),
                usuario.getEstado().name(),
                usuario.getSecretoTotpCifrado().orElse(null),
                usuario.getFechaCreacion(),
                usuario.getFechaCreacion());
    }

    Usuario aDominio(UsuarioEntity fila) {
        return Usuario.reconstituir(
                fila.getId(),
                fila.getNombre(),
                new Correo(fila.getCorreo()),
                fila.getTelefono(),
                fila.getHashContrasena(),
                Rol.valueOf(fila.getRol()),
                EstadoCuenta.valueOf(fila.getEstado()),
                fila.getSecretoTotp(),
                fila.getCreadoEn());
    }
}
