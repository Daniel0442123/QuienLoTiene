package com.quienlotiene.catalogo.application.usecase;

import com.quienlotiene.catalogo.api.CatalogoApi;
import com.quienlotiene.catalogo.api.CategoriaAdministrada;
import com.quienlotiene.catalogo.api.ProductoRestringido;
import com.quienlotiene.catalogo.domain.model.Categoria;
import com.quienlotiene.catalogo.domain.model.ElementoCatalogoNoEncontradoException;
import com.quienlotiene.catalogo.domain.model.PalabraRestringida;
import com.quienlotiene.catalogo.domain.model.PalabraRestringidaDuplicadaException;
import com.quienlotiene.catalogo.domain.port.CategoriaRepository;
import com.quienlotiene.catalogo.domain.port.PalabraRestringidaRepository;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link CatalogoApi}: HU-038 Gestionar categorías y productos restringidos (CU-28, diagrama 32) y la
 * consulta de HU-023. La auditoría la registra administracion, que es quien llama las acciones de gestión.
 */
@Slf4j
@Service
class GestionarCatalogo implements CatalogoApi {

    private static final Comparator<String> EN_ESPANOL = Collator.getInstance(Locale.forLanguageTag("es-CO"))::compare;

    private final CategoriaRepository categoriaRepository;
    private final PalabraRestringidaRepository palabraRepository;

    GestionarCatalogo(CategoriaRepository categoriaRepository, PalabraRestringidaRepository palabraRepository) {
        this.categoriaRepository = categoriaRepository;
        this.palabraRepository = palabraRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esRestringido(String producto) {
        return palabraRepository.listar().stream().anyMatch(palabra -> palabra.apareceEn(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaAdministrada> listarCategorias() {
        return categoriaRepository.buscarTodas().stream()
                .sorted(Comparator.comparing(Categoria::nombre, EN_ESPANOL))
                .map(GestionarCatalogo::aAdministrada)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaAdministrada cambiarActiva(UUID categoriaId, boolean activa) {
        var categoria = categoriaRepository
                .buscarPorId(categoriaId)
                .orElseThrow(() -> new ElementoCatalogoNoEncontradoException("La categoría"));
        var cambiada = categoria.conActiva(activa);
        categoriaRepository.guardarActiva(cambiada);
        log.info("Categoría {} {}", categoriaId, activa ? "activada" : "desactivada");
        return aAdministrada(cambiada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoRestringido> listarRestringidos() {
        return palabraRepository.listar().stream()
                .sorted(Comparator.comparing(PalabraRestringida::palabraClave, EN_ESPANOL))
                .map(GestionarCatalogo::aProducto)
                .toList();
    }

    @Override
    @Transactional
    public ProductoRestringido agregarRestringido(String palabraClave) {
        var palabra = new PalabraRestringida(UUID.randomUUID(), palabraClave);
        if (palabraRepository.existe(palabra.palabraClave())) {
            throw new PalabraRestringidaDuplicadaException();
        }
        palabraRepository.guardar(palabra);
        return aProducto(palabra);
    }

    @Override
    @Transactional
    public ProductoRestringido quitarRestringido(UUID productoRestringidoId) {
        var palabra = palabraRepository
                .buscarPorId(productoRestringidoId)
                .orElseThrow(() -> new ElementoCatalogoNoEncontradoException("La palabra restringida"));
        palabraRepository.eliminar(palabra.id());
        return aProducto(palabra);
    }

    private static CategoriaAdministrada aAdministrada(Categoria categoria) {
        return new CategoriaAdministrada(categoria.id(), categoria.nombre(), categoria.activa());
    }

    private static ProductoRestringido aProducto(PalabraRestringida palabra) {
        return new ProductoRestringido(palabra.id(), palabra.palabraClave());
    }
}
