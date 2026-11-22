package com.quienlotiene.administracion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.catalogo.api.CatalogoApi;
import com.quienlotiene.catalogo.api.CategoriaAdministrada;
import com.quienlotiene.catalogo.api.ProductoRestringido;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-038")
@DisplayName("HU-038 Auditoría de la gestión del catálogo")
class GestionarCatalogoAdministradoTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID ADMINISTRADOR = UUID.randomUUID();

    @Mock
    CatalogoApi catalogoApi;

    @Mock
    AuditoriaRepository auditoriaRepository;

    private GestionarCatalogoAdministrado gestionar() {
        return new GestionarCatalogoAdministrado(catalogoApi, auditoriaRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private RegistroAuditoria auditado() {
        var registro = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).registrar(registro.capture());
        return registro.getValue();
    }

    @Test
    @Tag("NF-SEG-013")
    @Tag("CP-CAT-038-01")
    @DisplayName("CP-CAT-038-01 Desactivar una categoría queda auditado con quién y cuándo")
    void auditaCategoria() {
        var categoria = UUID.randomUUID();
        when(catalogoApi.cambiarActiva(categoria, false))
                .thenReturn(new CategoriaAdministrada(categoria, "Ferreterías", false));

        gestionar().cambiarActiva(ADMINISTRADOR, categoria, false);

        var registro = auditado();
        assertThat(registro.accion()).isEqualTo(AccionAuditada.CATEGORIA_DESACTIVADA);
        assertThat(registro.administradorId()).isEqualTo(ADMINISTRADOR);
        assertThat(registro.recursoId()).isEqualTo(categoria);
        assertThat(registro.fecha()).isEqualTo(AHORA);
    }

    @Test
    @Tag("NF-SEG-013")
    @Tag("CP-CAT-038-02")
    @DisplayName("CP-CAT-038-02 Agregar y quitar un restringido quedan auditados")
    void auditaRestringidos() {
        var producto = new ProductoRestringido(UUID.randomUUID(), "amoxicilina");
        when(catalogoApi.agregarRestringido("Amoxicilina")).thenReturn(producto);

        assertThat(gestionar().agregarRestringido(ADMINISTRADOR, "Amoxicilina")).isEqualTo(producto);
        assertThat(auditado().accion()).isEqualTo(AccionAuditada.PRODUCTO_RESTRINGIDO_AGREGADO);
    }

    @Test
    @Tag("NF-SEG-013")
    @DisplayName("Quitar un restringido y reactivar una categoría también se auditan")
    void auditaQuitarYActivar() {
        var producto = new ProductoRestringido(UUID.randomUUID(), "amoxicilina");
        when(catalogoApi.quitarRestringido(producto.id())).thenReturn(producto);

        gestionar().quitarRestringido(ADMINISTRADOR, producto.id());

        assertThat(auditado().accion()).isEqualTo(AccionAuditada.PRODUCTO_RESTRINGIDO_QUITADO);
    }
}
