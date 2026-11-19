package com.quienlotiene.reputacion.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.ordenes.api.OrdenesApi;
import com.quienlotiene.ordenes.api.ResumenDeOrden;
import com.quienlotiene.reputacion.application.dto.CalificarComando;
import com.quienlotiene.reputacion.domain.model.CalificacionNoPermitidaException;
import com.quienlotiene.reputacion.domain.model.Resena;
import com.quienlotiene.reputacion.domain.model.ResenaDuplicadaException;
import com.quienlotiene.reputacion.domain.model.ResenaNoEncontradaException;
import com.quienlotiene.reputacion.domain.model.ResumenReputacion;
import com.quienlotiene.reputacion.domain.port.ResenaRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
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
@Tag("HU-034")
@DisplayName("HU-034 Calificar tienda")
class CalificarTiendaTest {

    private static final Instant AHORA = Instant.parse("2026-11-19T15:00:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();
    private static final UUID TIENDA = UUID.randomUUID();
    private static final UUID ORDEN = UUID.randomUUID();

    @Mock
    OrdenesApi ordenesApi;

    @Mock
    ResenaRepository resenaRepository;

    private CalificarTienda calificar() {
        return new CalificarTienda(ordenesApi, resenaRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private void ordenEn(String estado, UUID quienPregunta) {
        when(ordenesApi.obtenerResumen(ORDEN, quienPregunta))
                .thenReturn(new ResumenDeOrden(
                        ORDEN, CLIENTE, TIENDA, DUENO, "Droguería Central", "Paracetamol", estado, false));
    }

    @Test
    @Tag("CP-REP-034-01")
    @DisplayName("CP-REP-034-01 El cliente califica una orden resuelta con 4 estrellas y la reseña se guarda")
    void calificaResuelta() {
        // Dado
        ordenEn("RESUELTA", CLIENTE);
        // Cuando
        var vista = calificar().ejecutar(new CalificarComando(CLIENTE, ORDEN, 4, "Llegó rápido"));
        // Entonces
        var guardada = ArgumentCaptor.forClass(Resena.class);
        verify(resenaRepository).guardar(guardada.capture());
        assertThat(guardada.getValue().tiendaId()).isEqualTo(TIENDA);
        assertThat(guardada.getValue().clienteId()).isEqualTo(CLIENTE);
        assertThat(vista.estrellas()).isEqualTo(4);
        assertThat(vista.comentario()).isEqualTo("Llegó rápido");
        assertThat(vista.fecha()).isEqualTo(AHORA);
    }

    @Test
    @Tag("CP-REP-034-02")
    @DisplayName("CP-REP-034-02 Sin orden resuelta no se califica (REP-001)")
    void sinOrdenResuelta() {
        ordenEn("ENTREGADO", CLIENTE);

        assertThatThrownBy(() -> calificar().ejecutar(new CalificarComando(CLIENTE, ORDEN, 5, null)))
                .isInstanceOf(CalificacionNoPermitidaException.class)
                .hasFieldOrPropertyWithValue("codigo", "REP-001");
        verify(resenaRepository, never()).guardar(any());
    }

    @Test
    @Tag("CP-REP-034-02")
    @DisplayName("CP-REP-034-02 La tienda de la orden no puede calificarse a sí misma")
    void laTiendaNoCalifica() {
        ordenEn("RESUELTA", DUENO);

        assertThatThrownBy(() -> calificar().ejecutar(new CalificarComando(DUENO, ORDEN, 5, null)))
                .isInstanceOf(CalificacionNoPermitidaException.class);
    }

    @Test
    @Tag("CP-REP-034-03")
    @DisplayName("CP-REP-034-03 Una orden que ya tiene reseña se rechaza (REP-002)")
    void dosVeces() {
        ordenEn("RESUELTA", CLIENTE);
        when(resenaRepository.existeParaOrden(ORDEN)).thenReturn(true);

        assertThatThrownBy(() -> calificar().ejecutar(new CalificarComando(CLIENTE, ORDEN, 3, null)))
                .isInstanceOf(ResenaDuplicadaException.class)
                .hasFieldOrPropertyWithValue("codigo", "REP-002");
        verify(resenaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Consultar la reseña: la devuelve si existe y responde REP-003 si no")
    void consultar() {
        ordenEn("RESUELTA", DUENO);
        var resena = new Resena(UUID.randomUUID(), ORDEN, TIENDA, CLIENTE, 5, null, AHORA);
        when(resenaRepository.buscarPorOrden(ORDEN)).thenReturn(Optional.of(resena), Optional.empty());

        assertThat(calificar().consultar(DUENO, ORDEN).estrellas()).isEqualTo(5);
        assertThatThrownBy(() -> calificar().consultar(DUENO, ORDEN)).isInstanceOf(ResenaNoEncontradaException.class);
    }

    @Test
    @Tag("HU-014")
    @Tag("CP-TIE-014-01")
    @DisplayName("CP-TIE-014-01 La calificación de la tienda trae promedio, total y reseñas sin datos del cliente")
    void reputacionDeTienda() {
        when(resenaRepository.resumenDe(TIENDA)).thenReturn(new ResumenReputacion(4.5, 2));
        when(resenaRepository.recientesDe(TIENDA, ConsultarReputacion.RECIENTES))
                .thenReturn(List.of(new Resena(UUID.randomUUID(), ORDEN, TIENDA, CLIENTE, 4, "Bien", AHORA)));

        var reputacion = new ConsultarReputacion(resenaRepository).ejecutar(TIENDA);

        assertThat(reputacion.promedio()).isEqualTo(4.5);
        assertThat(reputacion.totalResenas()).isEqualTo(2);
        assertThat(reputacion.recientes()).singleElement().satisfies(publica -> {
            assertThat(publica.estrellas()).isEqualTo(4);
            assertThat(publica.toString()).doesNotContain(CLIENTE.toString());
        });
    }
}
