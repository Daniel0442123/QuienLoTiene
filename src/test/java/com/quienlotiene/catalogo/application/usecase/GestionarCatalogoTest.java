package com.quienlotiene.catalogo.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.catalogo.domain.model.Categoria;
import com.quienlotiene.catalogo.domain.model.ElementoCatalogoNoEncontradoException;
import com.quienlotiene.catalogo.domain.model.PalabraRestringida;
import com.quienlotiene.catalogo.domain.model.PalabraRestringidaDuplicadaException;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import com.quienlotiene.catalogo.domain.port.PalabraRestringidaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-038")
@DisplayName("HU-038 Gestionar categorías y productos restringidos")
class GestionarCatalogoTest {

    private static final UUID FERRETERIAS = UUID.randomUUID();

    @Mock
    CategoriaRepository categoriaRepository;

    @Mock
    PalabraRestringidaRepository palabraRepository;

    GestionarCatalogo catalogo;

    @BeforeEach
    void configurar() {
        catalogo = new GestionarCatalogo(categoriaRepository, palabraRepository);
    }

    @Test
    @Tag("CP-CAT-038-01")
    @DisplayName("CP-CAT-038-01 Desactivar Ferreterías la guarda inactiva (deja de salir al publicar)")
    void desactivaCategoria() {
        // Dado
        when(categoriaRepository.buscarPorId(FERRETERIAS))
                .thenReturn(Optional.of(new Categoria(FERRETERIAS, "Ferreterías", true)));
        // Cuando
        var cambiada = catalogo.cambiarActiva(FERRETERIAS, false);
        // Entonces
        assertThat(cambiada.activa()).isFalse();
        var guardada = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).guardarActiva(guardada.capture());
        assertThat(guardada.getValue()).isEqualTo(new Categoria(FERRETERIAS, "Ferreterías", false));
    }

    @Test
    @DisplayName("Una categoría que no existe responde CAT-001; la lista trae todas en orden alfabético")
    void categoriaInexistenteYLista() {
        when(categoriaRepository.buscarPorId(any())).thenReturn(Optional.empty());
        when(categoriaRepository.buscarTodas())
                .thenReturn(List.of(
                        new Categoria(UUID.randomUUID(), "Farmacia", true),
                        new Categoria(FERRETERIAS, "Ferretería", false),
                        new Categoria(UUID.randomUUID(), "Árboles", true)));

        assertThatThrownBy(() -> catalogo.cambiarActiva(UUID.randomUUID(), true))
                .isInstanceOf(ElementoCatalogoNoEncontradoException.class)
                .hasFieldOrPropertyWithValue("codigo", "CAT-001");
        assertThat(catalogo.listarCategorias())
                .extracting(c -> c.nombre())
                .containsExactly("Árboles", "Farmacia", "Ferretería");
    }

    @Test
    @Tag("CP-CAT-038-02")
    @DisplayName("CP-CAT-038-02 Agregar «Amoxicilina» hace que las solicitudes de ese producto sean restringidas")
    void agregaRestringido() {
        // Cuando
        var agregado = catalogo.agregarRestringido("Amoxicilina");
        // Entonces
        assertThat(agregado.palabraClave()).isEqualTo("amoxicilina");
        var guardada = ArgumentCaptor.forClass(PalabraRestringida.class);
        verify(palabraRepository).guardar(guardada.capture());
        when(palabraRepository.listar()).thenReturn(List.of(guardada.getValue()));
        assertThat(catalogo.esRestringido("Amoxicilina 500 mg")).isTrue();
        assertThat(catalogo.esRestringido("Paracetamol")).isFalse();
    }

    @Test
    @DisplayName("Una palabra repetida responde CAT-002 y no se guarda")
    void duplicada() {
        when(palabraRepository.existe("amoxicilina")).thenReturn(true);

        assertThatThrownBy(() -> catalogo.agregarRestringido("AMOXICILINA"))
                .isInstanceOf(PalabraRestringidaDuplicadaException.class);
        verify(palabraRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Quitar una palabra la elimina; si no existe responde CAT-001")
    void quita() {
        var palabra = new PalabraRestringida(UUID.randomUUID(), "azitromicina");
        when(palabraRepository.buscarPorId(palabra.id())).thenReturn(Optional.of(palabra));
        when(palabraRepository.listar()).thenReturn(List.of(palabra));

        assertThat(catalogo.listarRestringidos())
                .extracting(p -> p.palabraClave())
                .containsExactly("azitromicina");
        assertThat(catalogo.quitarRestringido(palabra.id()).palabraClave()).isEqualTo("azitromicina");
        verify(palabraRepository).eliminar(palabra.id());
        assertThatThrownBy(() -> catalogo.quitarRestringido(UUID.randomUUID()))
                .isInstanceOf(ElementoCatalogoNoEncontradoException.class);
    }
}
