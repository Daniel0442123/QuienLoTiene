package com.quienlotiene.clientes.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.clientes.application.dto.DatosDireccion;
import com.quienlotiene.clientes.domain.model.Direccion;
import com.quienlotiene.clientes.domain.model.DireccionIncompletaException;
import com.quienlotiene.clientes.domain.model.DireccionNoEncontradaException;
import com.quienlotiene.clientes.domain.model.LimiteDireccionesException;
import com.quienlotiene.clientes.domain.port.DireccionRepository;
import com.quienlotiene.shared.domain.Ubicacion;
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

@ExtendWith(MockitoExtension.class)
@Tag("HU-007")
@DisplayName("HU-007 Gestionar direcciones del cliente")
class GestionarDireccionesTest {

    private static final Instant AHORA = Instant.parse("2026-11-09T15:30:00Z");
    private static final UUID CLIENTE = UUID.randomUUID();

    @Mock
    DireccionRepository direccionRepository;

    GestionarDirecciones gestionarDirecciones;

    @BeforeEach
    void configurar() {
        gestionarDirecciones = new GestionarDirecciones(direccionRepository, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static DatosDireccion casa(Double latitud, Double longitud) {
        return new DatosDireccion(
                null, "Casa", latitud, longitud, "Calle 5 # 10-20", "Portón verde", "3001234567", null);
    }

    private static Direccion direccionDe(UUID clienteId) {
        return Direccion.registrar(
                UUID.randomUUID(), clienteId, "Casa", new Ubicacion(2.4448, -76.6147), "Calle 5", null, "300", AHORA);
    }

    @Test
    @Tag("CP-CLI-007-01")
    @DisplayName("CP-CLI-007-01 Guarda la dirección del cliente y queda disponible en su lista")
    void guardaLaDireccion() {
        var creada = gestionarDirecciones.crear(CLIENTE, casa(2.4448, -76.6147));

        var guardada = ArgumentCaptor.forClass(Direccion.class);
        verify(direccionRepository).guardarNueva(guardada.capture());
        assertThat(guardada.getValue().getClienteId()).isEqualTo(CLIENTE);
        assertThat(creada.id()).isEqualTo(guardada.getValue().getId());
        assertThat(creada.latitud()).isEqualTo(2.4448);
        assertThat(creada.fechaCreacion()).isEqualTo(AHORA);

        when(direccionRepository.listarDe(CLIENTE)).thenReturn(List.of(guardada.getValue()));
        assertThat(gestionarDirecciones.listar(CLIENTE))
                .singleElement()
                .satisfies(direccion -> assertThat(direccion.nombre()).isEqualTo("Casa"));
    }

    @Test
    @Tag("CP-CLI-007-02")
    @DisplayName("CP-CLI-007-02 Sin ubicación no se guarda")
    void noGuardaSinUbicacion() {
        assertThatThrownBy(() -> gestionarDirecciones.crear(CLIENTE, casa(null, -76.6)))
                .isInstanceOf(DireccionIncompletaException.class)
                .hasMessageContaining("ubicación");
        verify(direccionRepository, never()).guardarNueva(any());
    }

    @Test
    @DisplayName("Con 10 direcciones no deja guardar otra")
    void limitaLasDirecciones() {
        when(direccionRepository.contarDe(CLIENTE)).thenReturn(10L);

        assertThatThrownBy(() -> gestionarDirecciones.crear(CLIENTE, casa(2.4, -76.6)))
                .isInstanceOf(LimiteDireccionesException.class)
                .extracting("codigo")
                .isEqualTo("CLI-002");
    }

    @Test
    @DisplayName("Edita y borra solo las direcciones propias; la de otro cliente responde CLI-003")
    void autorizaPorObjeto() {
        var propia = direccionDe(CLIENTE);
        var ajena = direccionDe(UUID.randomUUID());
        when(direccionRepository.buscarPorId(propia.getId())).thenReturn(Optional.of(propia));
        when(direccionRepository.buscarPorId(ajena.getId())).thenReturn(Optional.of(ajena));

        var editada = gestionarDirecciones.editar(
                CLIENTE,
                propia.getId(),
                new DatosDireccion(null, "Oficina", 2.45, -76.6, "Carrera 9", null, "300", null));
        gestionarDirecciones.borrar(CLIENTE, propia.getId());

        assertThat(editada.nombre()).isEqualTo("Oficina");
        verify(direccionRepository).guardarCambios(propia);
        verify(direccionRepository).borrar(propia.getId());
        assertThatThrownBy(() -> gestionarDirecciones.borrar(CLIENTE, ajena.getId()))
                .isInstanceOf(DireccionNoEncontradaException.class)
                .extracting("codigo")
                .isEqualTo("CLI-003");
        verify(direccionRepository, never()).borrar(ajena.getId());
    }
}
