package com.quienlotiene.identidad.application.usecase;

import com.quienlotiene.identidad.api.ContactoUsuario;
import com.quienlotiene.identidad.api.ContactoUsuariosApi;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa {@link ContactoUsuariosApi}. */
@Service
class ConsultarContactoUsuario implements ContactoUsuariosApi {

    private final UsuarioRepository usuarioRepository;

    ConsultarContactoUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ContactoUsuario obtenerContacto(UUID usuarioId) {
        var usuario = usuarioRepository.buscarPorId(usuarioId).orElseThrow();
        return new ContactoUsuario(usuario.getId(), usuario.getNombre(), usuario.getTelefono());
    }
}
