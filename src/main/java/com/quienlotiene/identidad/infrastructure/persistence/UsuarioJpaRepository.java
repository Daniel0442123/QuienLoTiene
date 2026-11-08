package com.quienlotiene.identidad.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    boolean existsByCorreo(String correo);

    Optional<UsuarioEntity> findByCorreo(String correo);
}
