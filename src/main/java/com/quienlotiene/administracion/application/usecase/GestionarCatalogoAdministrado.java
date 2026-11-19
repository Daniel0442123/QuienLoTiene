package com.quienlotiene.administracion.application.usecase;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.catalogo.api.CatalogoApi;
import com.quienlotiene.catalogo.api.CategoriaAdministrada;
import com.quienlotiene.catalogo.api.ProductoRestringido;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-038 Gestionar categorías y productos restringidos (CU-28, diagrama 32). Las reglas las aplica catalogo; aquí se
 * registra la auditoría de cada cambio en la misma transacción (RNF-013).
 */
@Service
public class GestionarCatalogoAdministrado {

    private final CatalogoApi catalogoApi;
    private final AuditoriaRepository auditoriaRepository;
    private final Clock reloj;

    GestionarCatalogoAdministrado(CatalogoApi catalogoApi, AuditoriaRepository auditoriaRepository, Clock reloj) {
        this.catalogoApi = catalogoApi;
        this.auditoriaRepository = auditoriaRepository;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<CategoriaAdministrada> listarCategorias() {
        return catalogoApi.listarCategorias();
    }

    /** CP-CAT-038-01: una categoría inactiva deja de salir al publicar solicitudes; {@code true} la vuelve a mostrar. */
    @Transactional
    public CategoriaAdministrada cambiarActiva(UUID administradorId, UUID categoriaId, boolean activa) {
        var categoria = catalogoApi.cambiarActiva(categoriaId, activa);
        auditar(
                administradorId,
                activa ? AccionAuditada.CATEGORIA_ACTIVADA : AccionAuditada.CATEGORIA_DESACTIVADA,
                categoriaId,
                categoria.nombre());
        return categoria;
    }

    @Transactional(readOnly = true)
    public List<ProductoRestringido> listarRestringidos() {
        return catalogoApi.listarRestringidos();
    }

    /** CP-CAT-038-02: las solicitudes nuevas con esa palabra muestran el aviso de HU-023. */
    @Transactional
    public ProductoRestringido agregarRestringido(UUID administradorId, String palabraClave) {
        var producto = catalogoApi.agregarRestringido(palabraClave);
        auditar(administradorId, AccionAuditada.PRODUCTO_RESTRINGIDO_AGREGADO, producto.id(), producto.palabraClave());
        return producto;
    }

    @Transactional
    public void quitarRestringido(UUID administradorId, UUID productoRestringidoId) {
        var producto = catalogoApi.quitarRestringido(productoRestringidoId);
        auditar(administradorId, AccionAuditada.PRODUCTO_RESTRINGIDO_QUITADO, producto.id(), producto.palabraClave());
    }

    private void auditar(UUID administradorId, AccionAuditada accion, UUID recursoId, String detalle) {
        auditoriaRepository.registrar(
                new RegistroAuditoria(UUID.randomUUID(), administradorId, accion, recursoId, detalle, reloj.instant()));
    }
}
