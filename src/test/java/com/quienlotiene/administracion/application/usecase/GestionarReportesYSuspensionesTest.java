package com.quienlotiene.administracion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.administracion.domain.model.AccionAuditada;
import com.quienlotiene.administracion.domain.model.RegistroAuditoria;
import com.quienlotiene.administracion.domain.port.AuditoriaRepository;
import com.quienlotiene.identidad.api.CuentasApi;
import com.quienlotiene.reputacion.api.ReporteParaAdministrador;
import com.quienlotiene.reputacion.api.ReportesApi;
import com.quienlotiene.tiendas.api.RevisionTiendasApi;
import com.quienlotiene.tiendas.api.TiendaParaRevision;
import com.quienlotiene.tiendas.api.TiendaSuspendida;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-037")
@DisplayName("HU-037 Gestionar reportes y suspender")
class GestionarReportesYSuspensionesTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID ADMINISTRADOR = UUID.randomUUID();
    private static final UUID TIENDA = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    ReportesApi reportesApi;

    @Mock
    RevisionTiendasApi revisionTiendasApi;

    @Mock
    CuentasApi cuentasApi;

    @Mock
    AuditoriaRepository auditoriaRepository;

    private GestionarReportesYSuspensiones gestionar() {
        return new GestionarReportesYSuspensiones(
                reportesApi, revisionTiendasApi, cuentasApi, auditoriaRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static TiendaParaRevision tienda(String estado) {
        return new TiendaParaRevision(
                TIENDA, "Central", UUID.randomUUID(), "3001112233", 2.4, -76.6, estado, AHORA, null, AHORA, List.of());
    }

    private RegistroAuditoria auditado() {
        var registro = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(auditoriaRepository).registrar(registro.capture());
        return registro.getValue();
    }

    @Test
    @Tag("CP-ADM-037-01")
    @DisplayName("CP-ADM-037-01 Suspender la tienda suspende la cuenta del dueño y queda auditado con el motivo")
    void suspende() {
        // Dado
        when(revisionTiendasApi.suspender(TIENDA)).thenReturn(new TiendaSuspendida(tienda("SUSPENDIDA"), DUENO));
        // Cuando
        var resultado = gestionar().suspenderTienda(ADMINISTRADOR, TIENDA, "Tres reportes confirmados");
        // Entonces
        assertThat(resultado.estado()).isEqualTo("SUSPENDIDA");
        verify(cuentasApi).suspender(DUENO);
        var registro = auditado();
        assertThat(registro.accion()).isEqualTo(AccionAuditada.TIENDA_SUSPENDIDA);
        assertThat(registro.detalle()).isEqualTo("Tres reportes confirmados");
        assertThat(registro.administradorId()).isEqualTo(ADMINISTRADOR);
    }

    @Test
    @DisplayName("Reactivar devuelve la tienda a APROBADA, reactiva la cuenta y queda auditado")
    void reactiva() {
        when(revisionTiendasApi.reactivar(TIENDA)).thenReturn(new TiendaSuspendida(tienda("APROBADA"), DUENO));

        assertThat(gestionar().reactivarTienda(ADMINISTRADOR, TIENDA).estado()).isEqualTo("APROBADA");
        verify(cuentasApi).reactivar(DUENO);
        assertThat(auditado().accion()).isEqualTo(AccionAuditada.TIENDA_REACTIVADA);
    }

    @Test
    @Tag("CP-ADM-037-02")
    @DisplayName("CP-ADM-037-02 Resolver un reporte queda auditado con la nota")
    void resuelveReporte() {
        var reporte = UUID.randomUUID();
        when(reportesApi.resolver(reporte, "Se habló con la tienda"))
                .thenReturn(new ReporteParaAdministrador(
                        reporte,
                        UUID.randomUUID(),
                        TIENDA,
                        "CLIENTE",
                        "DEMORA_ENTREGA",
                        null,
                        "RESUELTO",
                        AHORA,
                        "Se habló con la tienda",
                        AHORA,
                        1));
        when(reportesApi.listarPorEstado("ABIERTO")).thenReturn(List.of());

        gestionar().resolverReporte(ADMINISTRADOR, reporte, "Se habló con la tienda");

        var registro = auditado();
        assertThat(registro.accion()).isEqualTo(AccionAuditada.REPORTE_RESUELTO);
        assertThat(registro.recursoId()).isEqualTo(reporte);
        assertThat(gestionar().listarReportes("ABIERTO")).isEmpty();
    }
}
