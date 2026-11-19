package com.quienlotiene.catalogo.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductoRestringidoJpaRepository extends JpaRepository<ProductoRestringidoEntity, UUID> {

    boolean existsByPalabraClave(String palabraClave);
}
