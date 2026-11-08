package com.quienlotiene.clientes.domain.port;

import com.quienlotiene.clientes.domain.model.Direccion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Direcciones del esquema {@code clientes}. */
public interface DireccionRepository {

    /** De la más antigua a la más reciente. */
    List<Direccion> listarDe(UUID clienteId);

    long contarDe(UUID clienteId);

    Optional<Direccion> buscarPorId(UUID direccionId);

    void guardarNueva(Direccion direccion);

    void guardarCambios(Direccion direccion);

    void borrar(UUID direccionId);
}
