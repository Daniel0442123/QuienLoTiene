package com.quienlotiene.ordenes.application.usecase;

import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quienlotiene.ordenes.api.EstadoOrdenCambiadoEvent;
import com.quienlotiene.ordenes.domain.model.EstadoOrden;
import com.quienlotiene.ordenes.domain.model.ModalidadEntrega;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.model.OrdenYaDespachadaException;
import com.quienlotiene.ordenes.domain.model.OrdenesDePrueba;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("HU-025 y HU-029 Casos de uso de cancelar y vencer reservas")
class CancelarYVencerReservasTest {

    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    OrdenRepository ordenRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    private Orden guardada(ModalidadEntrega modalidad) {
        var orden = OrdenesDePrueba.confirmada(CLIENTE, DUENO, modalidad);
        when(ordenRepository.buscarPorId(orden.getId())).thenReturn(Optional.of(orden));
        return orden;
    }

    @Nested
    @Tag("HU-029")
    @DisplayName("HU-029 Cancelar orden")
    class Cancelar {

        private CancelarOrden cancelar() {
            return new CancelarOrden(ordenRepository, publicadorEventos, RELOJ);
        }

        @Test
        @Tag("CP-ORD-029-01")
        @DisplayName("CP-ORD-029-01 El cliente cancela; se guarda, la tienda recibe el motivo y no ve sus datos")
        void clienteCancela() {
            // Dado
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.minusSeconds(60));
            // Cuando
            var vista = cancelar().ejecutar(CLIENTE, orden.getId(), "Ya lo conseguí");
            // Entonces
            assertThat(vista.estado()).isEqualTo("CANCELADO");
            assertThat(vista.canceladaPor()).isEqualTo("CLIENTE");
            assertThat(vista.motivoCancelacion()).isEqualTo("Ya lo conseguí");
            verify(ordenRepository).guardarCambioEstado(orden);
            verify(publicadorEventos)
                    .publishEvent(
                            new EstadoOrdenCambiadoEvent(orden.getId(), CLIENTE, DUENO, "CANCELADO", "Ya lo conseguí"));
            assertThat(OrdenVistaMapper.aVista(orden, DUENO).cliente()).isNull();
        }

        @Test
        @DisplayName("La tienda cancela una orden CONFIRMADO y el aviso le llega al cliente")
        void tiendaCancela() {
            var orden = guardada(ModalidadEntrega.RECOGER);

            var vista = cancelar().ejecutar(DUENO, orden.getId(), "Se nos acabó");

            assertThat(vista.canceladaPor()).isEqualTo("TIENDA");
            verify(publicadorEventos)
                    .publishEvent(
                            new EstadoOrdenCambiadoEvent(orden.getId(), CLIENTE, DUENO, "CANCELADO", "Se nos acabó"));
        }

        @Test
        @Tag("CP-ORD-029-02")
        @DisplayName("CP-ORD-029-02 En camino se rechaza con ORD-005 y no se guarda ni avisa")
        void enCamino() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.minusSeconds(120));
            orden.avanzarPorTienda(EstadoOrden.EN_CAMINO, AHORA.minusSeconds(60));

            assertThatThrownBy(() -> cancelar().ejecutar(CLIENTE, orden.getId(), "Demora"))
                    .isInstanceOf(OrdenYaDespachadaException.class);
            verify(ordenRepository, never()).guardarCambioEstado(any());
            verify(publicadorEventos, never()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("R-03 Quien no es parte de la orden recibe ORD-001 (404)")
        void ajena() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);

            assertThatThrownBy(() -> cancelar().ejecutar(UUID.randomUUID(), orden.getId(), "No es mía"))
                    .isInstanceOf(OrdenNoEncontradaException.class);
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CONFIRMADO);
        }
    }

    @Nested
    @Tag("HU-025")
    @DisplayName("HU-025 Vencer reservas")
    class Vencer {

        @Test
        @Tag("CP-ORD-025-01")
        @DisplayName("CP-ORD-025-01 La tarea vence la recogida que pasó su reserva y avisa a los dos")
        void venceYAvisa() {
            // Dado
            var orden = OrdenesDePrueba.confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.minus(Duration.ofMinutes(40)));
            orden.avanzarPorTienda(
                    EstadoOrden.LISTO_PARA_RECOGER, AHORA.minus(Duration.ofMinutes(30)), Duration.ofMinutes(30));
            when(ordenRepository.tomarReservasVencidas(eq(AHORA), anyInt())).thenReturn(List.of(orden));
            // Cuando
            var vencidas = new VencerReservas(ordenRepository, publicadorEventos, RELOJ).ejecutar();
            // Entonces
            assertThat(vencidas).isEqualTo(1);
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.VENCIDA);
            verify(ordenRepository).guardarCambioEstado(orden);
            verify(publicadorEventos)
                    .publishEvent(new EstadoOrdenCambiadoEvent(orden.getId(), CLIENTE, DUENO, "VENCIDA"));
        }

        @Test
        @Tag("CP-ORD-025-02")
        @DisplayName("CP-ORD-025-02 Una orden que ya se entregó entre la consulta y el cambio no se vence")
        void yaEntregada() {
            var orden = OrdenesDePrueba.confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.minus(Duration.ofMinutes(40)));
            orden.avanzarPorTienda(
                    EstadoOrden.LISTO_PARA_RECOGER, AHORA.minus(Duration.ofMinutes(35)), Duration.ofMinutes(30));
            orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA.minus(Duration.ofMinutes(10)));
            when(ordenRepository.tomarReservasVencidas(eq(AHORA), anyInt())).thenReturn(List.of(orden));

            var vencidas = new VencerReservas(ordenRepository, publicadorEventos, RELOJ).ejecutar();

            assertThat(vencidas).isZero();
            verify(ordenRepository, never()).guardarCambioEstado(any());
        }
    }

    @Test
    @Tag("HU-025")
    @DisplayName("Al marcar lista, la tienda deja la reserva con los minutos configurados")
    void reservaConfigurada() {
        var orden = guardada(ModalidadEntrega.RECOGER);
        orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.minusSeconds(60));

        var vista = new CambiarEstadoOrden(ordenRepository, publicadorEventos, RELOJ, 45)
                .ejecutar(DUENO, orden.getId(), "LISTO_PARA_RECOGER");

        assertThat(vista.reservaVence()).isEqualTo(AHORA.plus(Duration.ofMinutes(45)));
    }
}
