package com.quienlotiene.catalogo.api;

import java.util.List;
import java.util.UUID;

/**
 * Fachada de catalogo para otros módulos: solicitudes pregunta si un producto es restringido (HU-023) y administracion
 * gestiona categorías y restringidos con su auditoría (HU-038, CU-28).
 */
public interface CatalogoApi {

    /** HU-023: {@code true} si el producto contiene una palabra restringida, sin importar mayúsculas ni tildes. */
    boolean esRestringido(String producto);

    /** HU-038: todas las categorías, activas e inactivas, ordenadas por nombre. */
    List<CategoriaAdministrada> listarCategorias();

    /**
     * HU-038: activa o desactiva una categoría (CP-CAT-038-01).
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException CAT-001 si no existe
     */
    CategoriaAdministrada cambiarActiva(UUID categoriaId, boolean activa);

    /** HU-038: lista de restringidos ordenada por palabra. */
    List<ProductoRestringido> listarRestringidos();

    /**
     * HU-038: agrega una palabra a la lista (CP-CAT-038-02).
     *
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException CAT-002 si ya está, CAT-003 si es muy corta o larga
     */
    ProductoRestringido agregarRestringido(String palabraClave);

    /**
     * HU-038: quita una palabra de la lista.
     *
     * @return la palabra quitada
     * @throws com.quienlotiene.shared.domain.ReglaNegocioException CAT-001 si no existe
     */
    ProductoRestringido quitarRestringido(UUID productoRestringidoId);
}
