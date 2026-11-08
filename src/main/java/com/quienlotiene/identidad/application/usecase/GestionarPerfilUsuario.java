package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.application.dto.PerfilUsuario;
import com.quienlotiene.identidad.domain.model.SesionVencidaException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** HU-007 (CU-03 pasos 14 a 16): ver y editar el nombre y el teléfono del perfil. */
@Service
public class GestionarPerfilUsuario {

    private final UsuarioRepository usuarioRepository;

    GestionarPerfilUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public PerfilUsuario consultar(UUID usuarioId) {
        return aPerfil(buscar(usuarioId));
    }

    @Transactional
    public PerfilUsuario actualizar(UUID usuarioId, String nombre, String telefono) {
        var usuario = buscar(usuarioId);
        usuario.actualizarPerfil(nombre, telefono);
        usuarioRepository.guardarPerfil(usuario);
        return aPerfil(usuario);
    }

    /** Un token válido de un usuario que ya no existe se trata como sesión vencida. */
    private Usuario buscar(UUID usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId).orElseThrow(SesionVencidaException::new);
    }

    private static PerfilUsuario aPerfil(Usuario usuario) {
        return new PerfilUsuario(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo().valor(),
                usuario.getTelefono(),
                usuario.getRol().name());
    }
}
