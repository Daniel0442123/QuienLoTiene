package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.domain.model.MedioPago;
import com.quienlotiene.tiendas.domain.model.MediosPagoAceptados;
import com.quienlotiene.tiendas.domain.model.MediosPagoInvalidosException;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Instant;
import java.util.Arrays;
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
@Tag("HU-012")
@DisplayName("HU-012 Configurar medios de pago de mi tienda")
class ConfigurarMediosPagoTest {

    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    TiendaRepository tiendaRepository;

    Tienda tienda;
    ConfigurarMediosPago configurarMediosPago;

    @BeforeEach
    void configurar() {
        tienda = Tienda.registrar(
                UUID.randomUUID(),
                DUENO,
                UUID.randomUUID(),
                "La 5",
                "3001234567",
                new Ubicacion(2.4, -76.6),
                Instant.parse("2026-11-09T15:30:00Z"));
        when(tiendaRepository.buscarPorDueno(DUENO)).thenReturn(Optional.of(tienda));
        configurarMediosPago = new ConfigurarMediosPago(tiendaRepository);
    }

    @Test
    @Tag("CP-TIE-012-01")
    @DisplayName("CP-TIE-012-01 Guarda efectivo y transferencia para la tienda del usuario")
    void guardaLosMedios() {
        var guardados = configurarMediosPago.guardar(DUENO, List.of("TRANSFERENCIA", "EFECTIVO"));

        assertThat(guardados).containsExactly("EFECTIVO", "TRANSFERENCIA");
        var medios = ArgumentCaptor.forClass(MediosPagoAceptados.class);
        verify(tiendaRepository).guardarMediosPago(eq(tienda.getId()), medios.capture());
        assertThat(medios.getValue().acepta(MedioPago.DATAFONO)).isFalse();
    }

    @Test
    @DisplayName("Sin medios o con uno desconocido no guarda nada")
    void noGuardaMediosInvalidos() {
        assertThatThrownBy(() -> configurarMediosPago.guardar(DUENO, List.of()))
                .isInstanceOf(MediosPagoInvalidosException.class);
        assertThatThrownBy(() -> configurarMediosPago.guardar(DUENO, List.of("BITCOIN")))
                .isInstanceOf(MediosPagoInvalidosException.class);
        assertThatThrownBy(() -> configurarMediosPago.guardar(DUENO, Arrays.asList((String) null)))
                .isInstanceOf(MediosPagoInvalidosException.class);
        assertThatThrownBy(() -> configurarMediosPago.guardar(DUENO, null))
                .isInstanceOf(MediosPagoInvalidosException.class);
        verify(tiendaRepository, never()).guardarMediosPago(any(), any());
    }

    @Test
    @DisplayName("Consulta los medios guardados")
    void consultaLosMedios() {
        when(tiendaRepository.buscarMediosPago(tienda.getId()))
                .thenReturn(MediosPagoAceptados.de(List.of(MedioPago.DATAFONO)));

        assertThat(configurarMediosPago.consultar(DUENO)).containsExactly("DATAFONO");
    }
}
