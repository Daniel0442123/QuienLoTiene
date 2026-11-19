package com.quienlotiene.reputacion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.ordenes.api.ResumenDeOrden;
import com.quienlotiene.reputacion.application.dto.ReportarComando;
import com.quienlotiene.reputacion.domain.model.CalificacionInvalidaException;
import com.quienlotiene.reputacion.domain.model.EstadoReporte;
import com.quienlotiene.reputacion.domain.model.MotivoReporte;
import com.quienlotiene.reputacion.domain.model.Reporte;
import com.quienlotiene.reputacion.domain.model.ResenaNoEncontradaException;
import com.quienlotiene.reputacion.domain.port.ReporteRepository;
import com.quienlotiene.shared.domain.ReglaNegocioException;
import com.quienlotiene.shared.domain.TipoError;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("HU-035 y HU-037 Reportes")
class ReportesTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();
    private static final UUID TIENDA = UUID.randomUUID();
    private static final UUID ORDEN = UUID.randomUUID();

    @Mock
    OrdenesApi ordenesApi;

    @Mock
    ReporteRepository reporteRepository;

    @Test
    @Tag("HU-035")
    @Tag("CP-REP-035-01")
    @DisplayName("CP-REP-035-01 El cliente reporta «Demora en la entrega» y el reporte queda ABIERTO")
    void reporta() {
        // Dado
        when(ordenesApi.obtenerResumen(ORDEN, CLIENTE))
                .thenReturn(
                        new ResumenDeOrden(ORDEN, CLIENTE, TIENDA, DUENO, "Central", "Paracetamol", "EN_CAMINO", true));
        // Cuando
        var vista = new ReportarProblema(ordenesApi, reporteRepository, RELOJ)
                .ejecutar(new ReportarComando(CLIENTE, ORDEN, "DEMORA_ENTREGA", null));
        // Entonces
        assertThat(vista.estado()).isEqualTo("ABIERTO");
        assertThat(vista.motivo()).isEqualTo("DEMORA_ENTREGA");
        var guardado = ArgumentCaptor.forClass(Reporte.class);
        verify(reporteRepository).guardarNuevo(guardado.capture());
        assertThat(guardado.getValue().getTiendaId()).isEqualTo(TIENDA);
        assertThat(guardado.getValue().autorEsCliente()).isTrue();
    }

    @Test
    @Tag("HU-035")
    @Tag("CP-REP-035-02")
    @DisplayName("CP-REP-035-02 Quien no participa en la orden recibe el 404 de ordenes y no se guarda nada")
    void ordenAjena() {
        var extrano = UUID.randomUUID();
        when(ordenesApi.obtenerResumen(ORDEN, extrano)).thenThrow(new OrdenAjena());

        assertThatThrownBy(() -> new ReportarProblema(ordenesApi, reporteRepository, RELOJ)
                        .ejecutar(new ReportarComando(extrano, ORDEN, "OTRO", "x")))
                .isInstanceOf(OrdenAjena.class);
        verify(reporteRepository, never()).guardarNuevo(any());
    }

    @Test
    @Tag("HU-035")
    @DisplayName("Un motivo desconocido se rechaza con REP-004")
    void motivoDesconocido() {
        when(ordenesApi.obtenerResumen(ORDEN, DUENO))
                .thenReturn(new ResumenDeOrden(ORDEN, CLIENTE, TIENDA, DUENO, "Central", "P", "CONFIRMADO", true));

        assertThatThrownBy(() -> new ReportarProblema(ordenesApi, reporteRepository, RELOJ)
                        .ejecutar(new ReportarComando(DUENO, ORDEN, "MALTRATO", null)))
                .isInstanceOf(CalificacionInvalidaException.class);
    }

    @Test
    @Tag("HU-037")
    @Tag("CP-ADM-037-02")
    @DisplayName("CP-ADM-037-02 El administrador resuelve un reporte abierto y ve cuántos tiene la tienda")
    void resuelve() {
        var reporte = Reporte.crear(
                UUID.randomUUID(), ORDEN, TIENDA, CLIENTE, true, MotivoReporte.DEMORA_ENTREGA, null, AHORA);
        when(reporteRepository.buscarPorId(reporte.getId())).thenReturn(Optional.of(reporte));
        when(reporteRepository.contarPorTienda(List.of(TIENDA))).thenReturn(Map.of(TIENDA, 3L));

        var vista = new AtenderReportes(reporteRepository, RELOJ).resolver(reporte.getId(), "Se habló con la tienda");

        assertThat(vista.estado()).isEqualTo("RESUELTO");
        assertThat(vista.reportadoPor()).isEqualTo("CLIENTE");
        assertThat(vista.reportesDeLaTienda()).isEqualTo(3);
        verify(reporteRepository).guardarResolucion(reporte);
    }

    @Test
    @Tag("HU-037")
    @DisplayName("La bandeja lista por estado; un estado desconocido responde REP-004 y un reporte inexistente REP-003")
    void bandeja() {
        var reporte =
                Reporte.crear(UUID.randomUUID(), ORDEN, TIENDA, DUENO, false, MotivoReporte.OTRO, "Grosero", AHORA);
        when(reporteRepository.listarPorEstado(EstadoReporte.ABIERTO)).thenReturn(List.of(reporte));
        when(reporteRepository.contarPorTienda(List.of(TIENDA))).thenReturn(Map.of(TIENDA, 1L));
        var atender = new AtenderReportes(reporteRepository, RELOJ);

        assertThat(atender.listarPorEstado("abierto"))
                .singleElement()
                .satisfies(vista -> assertThat(vista.reportadoPor()).isEqualTo("TIENDA"));
        assertThatThrownBy(() -> atender.listarPorEstado("CERRADO")).isInstanceOf(CalificacionInvalidaException.class);
        assertThatThrownBy(() -> atender.resolver(UUID.randomUUID(), "nota"))
                .isInstanceOf(ResenaNoEncontradaException.class);
    }

    private static final class OrdenAjena extends ReglaNegocioException {
        OrdenAjena() {
            super("ORD-001", "Orden no encontrada", "No existe.", TipoError.NO_ENCONTRADO);
        }
    }
}
