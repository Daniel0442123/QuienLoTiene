package com.quienlotiene.catalogo.application.usecase;

import com.quienlotiene.catalogo.application.dto.CategoriaResumen;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Categorías activas para los formularios de registro de tienda (HU-002) y de publicar solicitud (HU-016), ordenadas
 * por nombre como se leen en español (las tildes no las mandan al final).
 */
@Service
public class ListarCategorias {

    private static final Locale ESPANOL = Locale.forLanguageTag("es-CO");

    private final CategoriaRepository categoriaRepository;

    ListarCategorias(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResumen> ejecutar() {
        var porNombre = Comparator.comparing(CategoriaResumen::nombre, Collator.getInstance(ESPANOL));
        return categoriaRepository.buscarActivas().stream()
                .map(categoria -> new CategoriaResumen(categoria.id(), categoria.nombre()))
                .sorted(porNombre)
                .toList();
    }
}
