package com.quienlotiene.identidad.infrastructure.persistence;

import com.quienlotiene.identidad.domain.model.Correo;
import com.quienlotiene.identidad.domain.model.CorreoYaRegistradoException;
import com.quienlotiene.identidad.domain.model.Usuario;
import com.quienlotiene.identidad.domain.port.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Component;

/** Adaptador JPA del puerto {@link UsuarioRepository}. */
@Component
class JpaUsuarioRepository implements UsuarioRepository {

    private static final String RESTRICCION_CORREO_UNICO = "uq_usuario_correo";

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioPersistenceMapper usuarioMapper;
    private final EntityManager entityManager;
    private final Clock reloj;

    JpaUsuarioRepository(
            UsuarioJpaRepository usuarioJpaRepository,
            UsuarioPersistenceMapper usuarioMapper,
            EntityManager entityManager,
            Clock reloj) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.usuarioMapper = usuarioMapper;
        this.entityManager = entityManager;
        this.reloj = reloj;
    }

    @Override
    public boolean existePorCorreo(Correo correo) {
        return usuarioJpaRepository.existsByCorreo(correo.valor());
    }

    @Override
    public void guardarPerfil(Usuario usuario) {
        usuarioJpaRepository
                .findById(usuario.getId())
                .orElseThrow()
                .actualizarPerfil(usuario.getNombre(), usuario.getTelefono(), Instant.now(reloj));
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(Correo correo) {
        return usuarioJpaRepository.findByCorreo(correo.valor()).map(usuarioMapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioJpaRepository.findById(id).map(usuarioMapper::aDominio);
    }

    @Override
    public void guardarNuevo(Usuario usuario) {
        try {
            // persist y no save: el id lo genera la aplicación y save haría un SELECT previo para decidir si es nuevo
            entityManager.persist(usuarioMapper.aEntity(usuario));
            entityManager.flush();
        } catch (PersistenceException excepcion) {
            if (esCorreoRepetido(excepcion)) {
                throw new CorreoYaRegistradoException();
            }
            throw excepcion;
        }
    }

    private static boolean esCorreoRepetido(PersistenceException excepcion) {
        Throwable causa = excepcion;
        while (causa != null) {
            if (causa instanceof ConstraintViolationException violacion
                    && RESTRICCION_CORREO_UNICO.equalsIgnoreCase(violacion.getConstraintName())) {
                return true;
            }
            causa = causa.getCause();
        }
        return false;
    }
}
