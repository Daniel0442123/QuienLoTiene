package com.quienlotiene.administracion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import com.quienlotiene.tiendas.api.DocumentoDescargable;
import com.quienlotiene.tiendas.api.RevisionTiendasApi;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
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
@Tag("HU-036")
@Tag("NF-SEG-013")
@DisplayName("HU-036 Revisar tienda con auditoría (módulo administracion)")
class RevisarTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-10T09:00:00Z");
    private static final UUID ADMINISTRADOR = UUID.randomUUID();
    private static final UUID TIENDA = UUID.randomUUID();

    @Mock
    RevisionTiendasApi revisionTiendas;

    @Mock
    AuditoriaRepository auditoriaRepository;

    RevisarTienda revisarTienda;

    @BeforeEach
    void configurar() {
        revisarTienda = new RevisarTienda(revisionTiendas, auditoriaRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static TiendaParaRevision tienda(String estado, String motivo) {
        return new TiendaParaRevision(
                TIENDA, "La 5", UUID.randomUUID(), "300", 2.4, -76.6, estado, AHORA, motivo, AHORA, List.of());
    }

    private RegistroAuditoria auditoriaRegistrada() {
        var registro = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).registrar(registro.capture());
        return registro.getValue();
    }

    @Test
    @Tag("CP-ADM-036-01")
    @DisplayName("CP-ADM-036-01 La aprobación queda auditada con el administrador, la acción y la fecha")
    void auditaLaAprobacion() {
        when(revisionTiendas.aprobar(TIENDA)).thenReturn(tienda("APROBADA", null));

        var resultado = revisarTienda.aprobar(ADMINISTRADOR, TIENDA);

        assertThat(resultado.estado()).isEqualTo("APROBADA");
        var registro = auditoriaRegistrada();
        assertThat(registro.administradorId()).isEqualTo(ADMINISTRADOR);
        assertThat(registro.accion()).isEqualTo(AccionAuditada.TIENDA_APROBADA);
        assertThat(registro.recursoId()).isEqualTo(TIENDA);
        assertThat(registro.fecha()).isEqualTo(AHORA);
    }

    @Test
    @Tag("CP-ADM-036-02")
    @DisplayName("CP-ADM-036-02 El rechazo queda auditado con su motivo")
    void auditaElRechazo() {
        when(revisionTiendas.rechazar(TIENDA, "Documento ilegible"))
                .thenReturn(tienda("RECHAZADA", "Documento ilegible"));

        revisarTienda.rechazar(ADMINISTRADOR, TIENDA, "Documento ilegible");

        var registro = auditoriaRegistrada();
        assertThat(registro.accion()).isEqualTo(AccionAuditada.TIENDA_RECHAZADA);
        assertThat(registro.detalle()).isEqualTo("Documento ilegible");
    }

    @Test
    @DisplayName("Abrir un documento también queda auditado (acceso a datos de la tienda)")
    void auditaLaDescargaDeDocumentos() {
        var documentoId = UUID.randomUUID();
        when(revisionTiendas.obtenerDocumento(TIENDA, documentoId))
                .thenReturn(new DocumentoDescargable("rut.pdf", "application/pdf", new byte[] {1}));

        var documento = revisarTienda.descargarDocumento(ADMINISTRADOR, TIENDA, documentoId);

        assertThat(documento.nombreArchivo()).isEqualTo("rut.pdf");
        assertThat(auditoriaRegistrada().accion()).isEqualTo(AccionAuditada.DOCUMENTO_TIENDA_DESCARGADO);
    }

    @Test
    @DisplayName("Si tiendas rechaza la decisión, no se audita nada")
    void noAuditaUnaDecisionFallida() {
        when(revisionTiendas.aprobar(TIENDA)).thenThrow(new ReglaDePrueba());

        assertThatThrownBy(() -> revisarTienda.aprobar(ADMINISTRADOR, TIENDA)).isInstanceOf(ReglaDePrueba.class);
        verify(auditoriaRepository, never()).registrar(any());
    }

    @Test
    @DisplayName("Lista la bandeja sin auditar")
    void listaSinAuditar() {
        when(revisionTiendas.listarPorEstado("PENDIENTE")).thenReturn(List.of(tienda("PENDIENTE", null)));

        assertThat(revisarTienda.listar("PENDIENTE")).hasSize(1);
        verify(auditoriaRepository, never()).registrar(any());
    }

    private static final class ReglaDePrueba extends ReglaNegocioException {
        ReglaDePrueba() {
            super("TIE-004", "Cambio de estado no permitido", "No se puede aprobar.", TipoError.CONFLICTO);
        }
    }
}
