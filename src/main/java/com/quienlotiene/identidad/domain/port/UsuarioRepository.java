package com.quienlotiene.identidad.domain.port;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.Usuario;

/** Guarda y consulta las cuentas de usuario del esquema {@code identidad}. */
public interface UsuarioRepository {

    boolean existePorCorreo(Correo correo);

    /**
     * Guarda un usuario nuevo.
     *
     * @throws com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException si otra petición registró el mismo
     *     correo al mismo tiempo
     */
    void guardarNuevo(Usuario usuario);
}
