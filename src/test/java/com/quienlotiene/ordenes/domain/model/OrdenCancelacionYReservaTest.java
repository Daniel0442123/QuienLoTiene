package com.quienlotiene.ordenes.domain.model;

import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.AHORA;
import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.confirmada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("HU-025 y HU-029 Orden: reserva y cancelación")
class OrdenCancelacionYReservaTest {

    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    private static Orden enEstado(ModalidadEntrega modalidad, EstadoOrden destino) {
        var orden = confirmada(CLIENTE, DUENO, modalidad);
        var minuto = 1;
        while (orden.getEstado() != destino) {
            var siguiente = switch (orden.getEstado()) {
                case CONFIRMADO -> EstadoOrden.PREPARANDO;
                case PREPARANDO ->
                    modalidad == ModalidadEntrega.DOMICILIO ? EstadoOrden.EN_CAMINO : EstadoOrden.LISTO_PARA_RECOGER;
                case EN_CAMINO, LISTO_PARA_RECOGER -> EstadoOrden.ENTREGADO;
                default -> throw new IllegalStateException("No se llega a " + destino);
            };
            orden.avanzarPorTienda(siguiente, AHORA.plusSeconds(60L * minuto++));
        }
        return orden;
    }

    @Nested
    @Tag("HU-029")
    @DisplayName("HU-029 Cancelar orden")
    class Cancelar {

        @Test
        @Tag("CP-ORD-029-01")
        @DisplayName("CP-ORD-029-01 El cliente cancela en PREPARANDO: queda CANCELADO con motivo y la tienda deja de"
                + " ver sus datos")
        void clienteCancelaEnPreparacion() {
            // Dado
            var orden = enEstado(ModalidadEntrega.DOMICILIO, EstadoOrden.PREPARANDO);
            // Cuando
            orden.cancelarPorCliente("  Ya lo conseguí  ", AHORA.plusSeconds(600));
            // Entonces
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CANCELADO);
            assertThat(orden.getMotivoCancelacion()).isEqualTo("Ya lo conseguí");
            assertThat(orden.getCanceladaPor()).isEqualTo(ParteOrden.CLIENTE);
            assertThat(orden.esActiva()).isFalse();
            assertThat(orden.muestraDatosDelClienteA(DUENO)).isFalse();
            assertThat(orden.getHistorial())
                    .last()
                    .extracting(CambioEstado::estado)
                    .isEqualTo(EstadoOrden.CANCELADO);
        }

        @ParameterizedTest
        @EnumSource(
                value = EstadoOrden.class,
                names = {"CONFIRMADO", "LISTO_PARA_RECOGER"})
        @DisplayName("El cliente también cancela una orden recién confirmada o una recogida lista que no ha salido")
        void clienteCancelaAntesDeSalir(EstadoOrden estado) {
            var orden = enEstado(ModalidadEntrega.RECOGER, estado);

            orden.cancelarPorCliente("Ya no lo necesito", AHORA.plusSeconds(600));

            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CANCELADO);
        }

        @Test
        @Tag("CP-ORD-029-02")
        @DisplayName("CP-ORD-029-02 En camino no se cancela: ORD-005 sugiere escribir a la tienda o reportar")
        void enCaminoNoSeCancela() {
            var orden = enEstado(ModalidadEntrega.DOMICILIO, EstadoOrden.EN_CAMINO);

            assertThatThrownBy(() -> orden.cancelarPorCliente("Demora", AHORA.plusSeconds(600)))
                    .isInstanceOf(OrdenYaDespachadaException.class)
                    .hasFieldOrPropertyWithValue("codigo", "ORD-005");
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.EN_CAMINO);
        }

        @Test
        @Tag("CP-ORD-029-02")
        @DisplayName("Entregada tampoco se cancela (ORD-005) y una ya cancelada responde ORD-002")
        void entregadaOTerminada() {
            var entregada = enEstado(ModalidadEntrega.DOMICILIO, EstadoOrden.ENTREGADO);
            var cancelada = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            cancelada.cancelarPorCliente("Me equivoqué", AHORA.plusSeconds(60));

            assertThatThrownBy(() -> entregada.cancelarPorCliente("x y z", AHORA.plusSeconds(600)))
                    .isInstanceOf(OrdenYaDespachadaException.class);
            assertThatThrownBy(() -> cancelada.cancelarPorCliente("otra vez", AHORA.plusSeconds(600)))
                    .isInstanceOf(TransicionOrdenInvalidaException.class)
                    .hasFieldOrPropertyWithValue("codigo", "ORD-002");
        }

        @Test
        @DisplayName("La tienda cancela solo en CONFIRMADO; ya en preparación responde ORD-002")
        void tiendaSoloEnConfirmado() {
            var confirmadaOrden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);
            var preparando = enEstado(ModalidadEntrega.DOMICILIO, EstadoOrden.PREPARANDO);

            confirmadaOrden.cancelarPorTienda("Se nos acabó", AHORA.plusSeconds(60));

            assertThat(confirmadaOrden.getEstado()).isEqualTo(EstadoOrden.CANCELADO);
            assertThat(confirmadaOrden.getCanceladaPor()).isEqualTo(ParteOrden.TIENDA);
            assertThatThrownBy(() -> preparando.cancelarPorTienda("Se nos acabó", AHORA.plusSeconds(600)))
                    .isInstanceOf(TransicionOrdenInvalidaException.class);
        }

        @Test
        @DisplayName("Sin motivo no se cancela (ORD-004)")
        void sinMotivo() {
            var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);

            assertThatThrownBy(() -> orden.cancelarPorCliente("   ", AHORA))
                    .isInstanceOf(OrdenIncompletaException.class);
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CONFIRMADO);
        }
    }

    @Nested
    @Tag("HU-025")
    @DisplayName("HU-025 Reservar para recogida")
    class Reserva {

        @Test
        @DisplayName("Al marcar LISTO_PARA_RECOGER empieza la reserva; a domicilio no hay reserva")
        void empiezaAlEstarLista() {
            var recoger = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            var domicilio = enEstado(ModalidadEntrega.DOMICILIO, EstadoOrden.EN_CAMINO);
            recoger.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
            assertThat(recoger.getReservaVence()).isNull();

            recoger.avanzarPorTienda(EstadoOrden.LISTO_PARA_RECOGER, AHORA.plusSeconds(300), Duration.ofMinutes(30));

            assertThat(recoger.getReservaVence())
                    .isEqualTo(AHORA.plusSeconds(300).plus(Duration.ofMinutes(30)));
            assertThat(domicilio.getReservaVence()).isNull();
        }

        @Test
        @Tag("CP-ORD-025-01")
        @DisplayName("CP-ORD-025-01 A los 30 minutos sin recoger queda VENCIDA; un segundo antes, no")
        void venceALos30Minutos() {
            // Dado
            var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
            orden.avanzarPorTienda(EstadoOrden.LISTO_PARA_RECOGER, AHORA, Duration.ofMinutes(30));
            var vence = AHORA.plus(Duration.ofMinutes(30));
            // Cuando / Entonces
            assertThat(orden.vencerReserva(vence.minusSeconds(1))).isFalse();
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.LISTO_PARA_RECOGER);
            assertThat(orden.vencerReserva(vence)).isTrue();
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.VENCIDA);
            assertThat(orden.esActiva()).isFalse();
        }

        @Test
        @Tag("CP-ORD-025-02")
        @DisplayName("CP-ORD-025-02 Recogida a tiempo: la tienda entrega, el cliente confirma, queda RESUELTA y ya no"
                + " vence")
        void recogidaATiempo() {
            var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
            orden.avanzarPorTienda(EstadoOrden.LISTO_PARA_RECOGER, AHORA, Duration.ofMinutes(30));

            orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA.plus(Duration.ofMinutes(29)));
            orden.confirmarRecepcion(AHORA.plus(Duration.ofMinutes(29)));

            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.RESUELTA);
            assertThat(orden.vencerReserva(AHORA.plus(Duration.ofHours(1)))).isFalse();
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.RESUELTA);
        }
    }
}
