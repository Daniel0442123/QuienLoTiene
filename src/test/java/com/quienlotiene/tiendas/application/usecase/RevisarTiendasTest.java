package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.AlmacenArchivos;
import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.api.TiendaRevisadaEvent;
import com.quienlotiene.tiendas.domain.model.DocumentoVerificacion;
import com.quienlotiene.tiendas.domain.model.EstadoTienda;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.model.TipoDocumento;
import com.quienlotiene.tiendas.domain.model.TransicionTiendaInvalidaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@Tag("HU-036")
@DisplayName("HU-036 Revisar tiendas (módulo tiendas)")
class RevisarTiendasTest {

    private static final Instant AHORA = Instant.parse("2026-11-10T09:00:00Z");

    @Mock
    TiendaRepository tiendaRepository;

    @Mock
    AlmacenArchivos almacenArchivos;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    RevisarTiendas revisarTiendas;
    Tienda tiendaPendiente;
    DocumentoVerificacion rut;

    @BeforeEach
    void configurar() {
        revisarTiendas = new RevisarTiendas(
                tiendaRepository, almacenArchivos, publicadorEventos, Clock.fixed(AHORA, ZoneOffset.UTC));
        tiendaPendiente = Tienda.registrar(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Droguería La 5",
                "3001234567",
                new Ubicacion(2.4448, -76.6147),
                AHORA.minusSeconds(3600));
        rut = new DocumentoVerificacion(
                UUID.randomUUID(),
                tiendaPendiente.getId(),
                TipoDocumento.RUT,
                "tiendas/x/abc.pdf",
                "application/pdf",
                1200,
                AHORA.minusSeconds(3600));
    }

    @Test
    @Tag("CP-ADM-036-01")
    @DisplayName("CP-ADM-036-01 Aprueba, guarda y avisa al dueño con TiendaRevisadaEvent")
    void apruebaYAvisa() {
        // Dado
        when(tiendaRepository.buscarPorId(tiendaPendiente.getId())).thenReturn(Optional.of(tiendaPendiente));
        when(tiendaRepository.listarDocumentos(tiendaPendiente.getId())).thenReturn(List.of(rut));

        // Cuando
        var resultado = revisarTiendas.aprobar(tiendaPendiente.getId());

        // Entonces
        assertThat(resultado.estado()).isEqualTo("APROBADA");
        assertThat(resultado.fechaRevision()).isEqualTo(AHORA);
        assertThat(resultado.documentos()).hasSize(1);
        verify(tiendaRepository).guardarRevision(tiendaPendiente);
        var evento = ArgumentCaptor.forClass(TiendaRevisadaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().estado()).isEqualTo("APROBADA");
        assertThat(evento.getValue().duenoId()).isEqualTo(tiendaPendiente.getDuenoId());
    }

    @Test
    @Tag("CP-ADM-036-02")
    @DisplayName("CP-ADM-036-02 Rechaza con motivo y el evento lleva el motivo")
    void rechazaYAvisaConMotivo() {
        when(tiendaRepository.buscarPorId(tiendaPendiente.getId())).thenReturn(Optional.of(tiendaPendiente));

        var resultado = revisarTiendas.rechazar(tiendaPendiente.getId(), "Documento ilegible");

        assertThat(resultado.estado()).isEqualTo("RECHAZADA");
        assertThat(resultado.motivoRechazo()).isEqualTo("Documento ilegible");
        var evento = ArgumentCaptor.forClass(TiendaRevisadaEvent.class);
        verify(publicadorEventos).publishEvent(evento.capture());
        assertThat(evento.getValue().motivoRechazo()).isEqualTo("Documento ilegible");
    }

    @Test
    @DisplayName("Una tienda ya aprobada no se vuelve a revisar ni avisa")
    void noRevisaDosVeces() {
        tiendaPendiente.aprobar(AHORA);
        when(tiendaRepository.buscarPorId(tiendaPendiente.getId())).thenReturn(Optional.of(tiendaPendiente));

        assertThatThrownBy(() -> revisarTiendas.aprobar(tiendaPendiente.getId()))
                .isInstanceOf(TransicionTiendaInvalidaException.class);
        verify(tiendaRepository, never()).guardarRevision(any());
        verify(publicadorEventos, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Una tienda que no existe responde TIE-006")
    void rechazaUnaTiendaInexistente() {
        var idInexistente = UUID.randomUUID();
        when(tiendaRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> revisarTiendas.aprobar(idInexistente))
                .isInstanceOf(TiendaNoEncontradaException.class)
                .extracting("codigo")
                .isEqualTo("TIE-006");
    }

    @Test
    @DisplayName("Lista la bandeja por estado y entrega el documento con nombre por su tipo")
    void listaYEntregaDocumentos() {
        when(tiendaRepository.listarPorEstado(EstadoTienda.PENDIENTE)).thenReturn(List.of(tiendaPendiente));
        when(tiendaRepository.listarDocumentos(tiendaPendiente.getId())).thenReturn(List.of(rut));
        when(almacenArchivos.leer("tiendas/x/abc.pdf")).thenReturn(new byte[] {1, 2});

        assertThat(revisarTiendas.listarPorEstado("pendiente"))
                .singleElement()
                .satisfies(tienda -> assertThat(tienda.nombre()).isEqualTo("Droguería La 5"));
        var documento = revisarTiendas.obtenerDocumento(tiendaPendiente.getId(), rut.id());
        assertThat(documento.nombreArchivo()).isEqualTo("rut.pdf");
        assertThat(documento.tipoContenido()).isEqualTo("application/pdf");
        assertThat(documento.contenido()).containsExactly(1, 2);
        var otroDocumento = UUID.randomUUID();
        assertThatThrownBy(() -> revisarTiendas.obtenerDocumento(tiendaPendiente.getId(), otroDocumento))
                .isInstanceOf(TiendaNoEncontradaException.class);
    }
}
