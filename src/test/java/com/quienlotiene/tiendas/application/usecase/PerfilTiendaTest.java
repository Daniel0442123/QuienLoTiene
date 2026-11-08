package com.quienlotiene.tiendas.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.shared.domain.Ubicacion;
import com.quienlotiene.tiendas.application.dto.ActualizarPerfilTiendaComando;
import com.quienlotiene.tiendas.application.dto.MiTienda;
import com.quienlotiene.tiendas.domain.model.HorarioInvalidoException;
import com.quienlotiene.tiendas.domain.model.Tienda;
import com.quienlotiene.tiendas.domain.model.TiendaNoEncontradaException;
import com.quienlotiene.tiendas.domain.port.TiendaRepository;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("HU-009")
@DisplayName("HU-009 Consultar y actualizar el perfil de mi tienda")
class PerfilTiendaTest {

    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    TiendaRepository tiendaRepository;

    Tienda tienda;

    @BeforeEach
    void configurar() {
        tienda = Tienda.registrar(
                UUID.randomUUID(),
                DUENO,
                UUID.randomUUID(),
                "La 5",
                "3001234567",
                new Ubicacion(2.4448, -76.6147),
                Instant.parse("2026-11-09T15:30:00Z"));
    }

    @Test
    @Tag("CP-TIE-009-02")
    @DisplayName("CP-TIE-009-02 Guarda el teléfono y el horario nuevos de la tienda del usuario")
    void actualizaElPerfilDeLaTiendaDelUsuario() {
        // Dado
        when(tiendaRepository.buscarPorDueno(DUENO)).thenReturn(Optional.of(tienda));
        var horario = List.of(
                new MiTienda.Franja("MONDAY", LocalTime.of(8, 0), LocalTime.of(18, 0)),
                new MiTienda.Franja("SATURDAY", LocalTime.of(9, 0), LocalTime.of(13, 0)));

        // Cuando
        var miTienda = new ActualizarPerfilTienda(tiendaRepository)
                .ejecutar(new ActualizarPerfilTiendaComando(DUENO, "Droguería La 5", "3109876543", horario));

        // Entonces
        verify(tiendaRepository).guardarPerfil(tienda);
        assertThat(miTienda.telefono()).isEqualTo("3109876543");
        assertThat(miTienda.horario()).extracting(MiTienda.Franja::diaSemana).containsExactly("MONDAY", "SATURDAY");
        assertThat(miTienda.estado()).isEqualTo("PENDIENTE");
    }

    @Test
    @DisplayName("Un horario inválido no se guarda")
    void noGuardaUnHorarioInvalido() {
        when(tiendaRepository.buscarPorDueno(DUENO)).thenReturn(Optional.of(tienda));
        var actualizar = new ActualizarPerfilTienda(tiendaRepository);

        assertThatThrownBy(() -> actualizar.ejecutar(new ActualizarPerfilTiendaComando(
                        DUENO,
                        "La 5",
                        "3001234567",
                        List.of(new MiTienda.Franja("LUNES", LocalTime.NOON, LocalTime.MAX)))))
                .isInstanceOf(HorarioInvalidoException.class);
        assertThatThrownBy(() -> actualizar.ejecutar(new ActualizarPerfilTiendaComando(
                        DUENO,
                        "La 5",
                        "3001234567",
                        List.of(new MiTienda.Franja(null, LocalTime.NOON, LocalTime.MAX)))))
                .isInstanceOf(HorarioInvalidoException.class);
        verify(tiendaRepository, never()).guardarPerfil(any());
    }

    @Test
    @DisplayName("Un usuario sin tienda recibe TIE-006")
    void unUsuarioSinTiendaNoEncuentraNada() {
        when(tiendaRepository.buscarPorDueno(DUENO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarMiTienda(tiendaRepository).ejecutar(DUENO))
                .isInstanceOf(TiendaNoEncontradaException.class);
        assertThatThrownBy(() -> new ActualizarPerfilTienda(tiendaRepository)
                        .ejecutar(new ActualizarPerfilTiendaComando(DUENO, "La 5", "300", null)))
                .isInstanceOf(TiendaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Consulta la tienda del usuario con su estado")
    void consultaLaTiendaDelUsuario() {
        when(tiendaRepository.buscarPorDueno(DUENO)).thenReturn(Optional.of(tienda));

        var miTienda = new ConsultarMiTienda(tiendaRepository).ejecutar(DUENO);

        assertThat(miTienda.id()).isEqualTo(tienda.getId());
        assertThat(miTienda.estaAbierta()).isFalse();
        assertThat(miTienda.horario()).isEmpty();
    }
}
