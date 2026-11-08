package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;

/** Guarda y consulta las cuentas de usuario del esquema {@code identidad}. */
public interface UsuarioRepository {

    boolean existePorCorreo(Correo correo);

    Optional<Usuario> buscarPorCorreo(Correo correo);

    Optional<Usuario> buscarPorId(UUID id);

    /**
     * Guarda un usuario nuevo.
     *
     * @throws com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException si otra petición registró el mismo
     *     correo al mismo tiempo
     */
    void guardarNuevo(Usuario usuario);

    /** Guarda el nombre y el teléfono del perfil (HU-007). */
    void guardarPerfil(Usuario usuario);
}
