package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.application.dto.Domicilio;
import com.quienlotiene.tiendas.domain.model.ConfiguracionDomicilio;
import com.quienlotiene.tiendas.domain.model.DomicilioInvalidoException;
import com.quienlotiene.tiendas.domain.model.TarifaDomicilio;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Instant;
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
@Tag("HU-010")
@DisplayName("HU-010 Configurar domicilio de mi tienda")
class ConfigurarDomicilioTest {

    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    TiendaRepository tiendaRepository;

    Tienda tienda;
    ConfigurarDomicilio configurarDomicilio;

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
        configurarDomicilio = new ConfigurarDomicilio(tiendaRepository);
    }

    @Test
    @Tag("CP-TIE-010-01")
    @DisplayName("CP-TIE-010-01 Guarda domicilio con tarifa fija de 5.000 COP en un radio de 3 km")
    void guardaLaTarifaFija() {
        var guardado = configurarDomicilio.guardar(DUENO, new Domicilio(true, 3_000, null, 5_000L));

        assertThat(guardado).isEqualTo(new Domicilio(true, 3_000, "FIJA", 5_000L));
        var configuracion = ArgumentCaptor.forClass(ConfiguracionDomicilio.class);
        verify(tiendaRepository).guardarDomicilio(eq(tienda.getId()), configuracion.capture());
        assertThat(configuracion.getValue().calcularCostoEnPesos(20_000, 1_000)).isEqualTo(5_000);
    }

    @Test
    @Tag("CP-TIE-010-02")
    @DisplayName("CP-TIE-010-02 Una tarifa negativa no se guarda")
    void noGuardaUnaTarifaNegativa() {
        assertThatThrownBy(() -> configurarDomicilio.guardar(DUENO, new Domicilio(true, 3_000, null, -500L)))
                .isInstanceOf(DomicilioInvalidoException.class);
        assertThatThrownBy(() -> configurarDomicilio.guardar(DUENO, new Domicilio(true, null, null, 5_000L)))
                .isInstanceOf(DomicilioInvalidoException.class);
        verify(tiendaRepository, never()).guardarDomicilio(any(), any());
    }

    @Test
    @DisplayName("Apagar el domicilio borra radio y tarifa; consultar y simular usan la configuración guardada")
    void apagaConsultaYSimula() {
        assertThat(configurarDomicilio.guardar(DUENO, new Domicilio(false, 3_000, null, 5_000L)))
                .isEqualTo(new Domicilio(false, null, null, null));

        when(tiendaRepository.buscarDomicilio(tienda.getId()))
                .thenReturn(ConfiguracionDomicilio.conDomicilio(3_000, new TarifaDomicilio.TarifaFija(4_000)));
        assertThat(configurarDomicilio.consultar(DUENO)).isEqualTo(new Domicilio(true, 3_000, "FIJA", 4_000L));
        assertThat(configurarDomicilio.simularCostoEnPesos(DUENO, 15_000, 2_500))
                .isEqualTo(4_000);
    }
}
