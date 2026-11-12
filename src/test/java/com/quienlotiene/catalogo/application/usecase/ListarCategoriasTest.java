package com.quienlotiene.catalogo.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.quienlotiene.catalogo.application.dto.CategoriaResumen;
import com.quienlotiene.catalogo.domain.model.Categoria;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-002")
@Tag("HU-016")
@DisplayName("HU-002 y HU-016 Listar las categorías para los formularios")
class ListarCategoriasTest {

    @Mock
    CategoriaRepository categoriaRepository;

    @InjectMocks
    ListarCategorias listarCategorias;

    @Test
    @DisplayName("Devuelve las categorías activas ordenadas por nombre en español")
    void ordenaPorNombreEnEspanol() {
        // Dado
        var ferreteria = new Categoria(UUID.randomUUID(), "Ferretería", true);
        var farmacia = new Categoria(UUID.randomUUID(), "Farmacia", true);
        var optica = new Categoria(UUID.randomUUID(), "Óptica", true);
        var papeleria = new Categoria(UUID.randomUUID(), "Papelería", true);
        when(categoriaRepository.buscarActivas()).thenReturn(List.of(papeleria, ferreteria, optica, farmacia));

        // Cuando
        var categorias = listarCategorias.ejecutar();

        // Entonces
        assertThat(categorias)
                .extracting(CategoriaResumen::nombre)
                .containsExactly("Farmacia", "Ferretería", "Óptica", "Papelería");
        assertThat(categorias.getFirst().id()).isEqualTo(farmacia.id());
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando no hay categorías activas")
    void sinCategoriasDevuelveListaVacia() {
        // Dado
        when(categoriaRepository.buscarActivas()).thenReturn(List.of());

        // Cuando
        var categorias = listarCategorias.ejecutar();

        // Entonces
        assertThat(categorias).isEmpty();
    }
}
