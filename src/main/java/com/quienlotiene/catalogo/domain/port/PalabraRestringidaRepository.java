package com.quienlotiene.catalogo.domain.port;

import com.quienlotiene.catalogo.domain.model.PalabraRestringida;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Lista de productos restringidos del esquema {@code catalogo} (HU-023, HU-038). */
public interface PalabraRestringidaRepository {

    List<PalabraRestringida> listar();

    boolean existe(String palabraNormalizada);

    Optional<PalabraRestringida> buscarPorId(UUID id);

    void guardar(PalabraRestringida palabra);

    void eliminar(UUID id);
}
