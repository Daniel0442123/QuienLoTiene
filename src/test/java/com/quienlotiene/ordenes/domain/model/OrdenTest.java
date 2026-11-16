package com.quienlotiene.ordenes.domain.model;

import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.AHORA;
import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.confirmada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@DisplayName("HU-024, HU-027 y HU-028 Orden")
class OrdenTest {

    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    @Test
    @Tag("HU-024")
    @Tag("CP-ORD-024-01")
    @DisplayName("CP-ORD-024-01 2 unidades a 8.000 con domicilio de 5.000 suman 21.000 y la orden nace CONFIRMADO")
    void totalCorrecto() {
        // Dado / Cuando
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);
        // Entonces
        assertThat(orden.subtotalEnPesos()).isEqualTo(16_000);
        assertThat(orden.getCostoDomicilioEnPesos()).isEqualTo(5_000);
        assertThat(orden.totalEnPesos()).isEqualTo(21_000);
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CONFIRMADO);
        assertThat(orden.getHistorial()).containsExactly(new CambioEstado(EstadoOrden.CONFIRMADO, AHORA));
        assertThat(orden.esActiva()).isTrue();
    }

    @Test
    @Tag("HU-024")
    @DisplayName("Para recoger no cobra domicilio ni guarda dirección; a domicilio sin dirección se rechaza")
    void recogerYDomicilioSinDireccion() {
        var recoger = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);

        assertThat(recoger.getCostoDomicilioEnPesos()).isZero();
        assertThat(recoger.getDireccionEntrega()).isNull();
        assertThat(recoger.totalEnPesos()).isEqualTo(16_000);
        assertThatThrownBy(() -> Orden.crear(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        CLIENTE,
                        new Orden.TiendaDeOrden(UUID.randomUUID(), DUENO, "T", "3000000000"),
                        "x",
                        1,
                        1,
                        0,
                        ModalidadEntrega.DOMICILIO,
                        MedioPagoOrden.EFECTIVO,
                        new ContactoCliente("A", "3000000000"),
                        null,
                        AHORA))
                .isInstanceOf(OrdenIncompletaException.class);
    }

    @Test
    @Tag("HU-027")
    @Tag("CP-ORD-027-01")
    @DisplayName("CP-ORD-027-01 La tienda avanza CONFIRMADO → PREPARANDO → EN_CAMINO → ENTREGADO")
    void avanzaADomicilio() {
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);

        orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA.plusSeconds(60));
        orden.avanzarPorTienda(EstadoOrden.EN_CAMINO, AHORA.plusSeconds(120));
        orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA.plusSeconds(180));

        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.ENTREGADO);
        assertThat(orden.getHistorial())
                .extracting(CambioEstado::estado)
                .containsExactly(
                        EstadoOrden.CONFIRMADO, EstadoOrden.PREPARANDO, EstadoOrden.EN_CAMINO, EstadoOrden.ENTREGADO);
    }

    @Test
    @Tag("HU-027")
    @DisplayName("Para recoger el paso después de PREPARANDO es LISTO_PARA_RECOGER")
    void avanzaParaRecoger() {
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
        orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);

        assertThatThrownBy(() -> orden.avanzarPorTienda(EstadoOrden.EN_CAMINO, AHORA))
                .isInstanceOf(TransicionOrdenInvalidaException.class);
        orden.avanzarPorTienda(EstadoOrden.LISTO_PARA_RECOGER, AHORA);
        orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA);
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.ENTREGADO);
    }

    @Test
    @Tag("HU-027")
    @Tag("CP-ORD-027-02")
    @Tag("NF-FIA-016")
    @DisplayName("CP-ORD-027-02 La tienda no puede saltar de CONFIRMADO a ENTREGADO ni volver atrás")
    void rechazaTransicionesInvalidas() {
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);

        assertThatThrownBy(() -> orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA))
                .isInstanceOf(TransicionOrdenInvalidaException.class);
        assertThatThrownBy(() -> orden.avanzarPorTienda(EstadoOrden.RESUELTA, AHORA))
                .isInstanceOf(TransicionOrdenInvalidaException.class);
        assertThatThrownBy(() -> orden.avanzarPorTienda(EstadoOrden.CONFIRMADO, AHORA))
                .isInstanceOf(TransicionOrdenInvalidaException.class);
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CONFIRMADO);
        for (var estado :
                List.of(EstadoOrden.ENTREGADO, EstadoOrden.RESUELTA, EstadoOrden.CANCELADO, EstadoOrden.VENCIDA)) {
            assertThat(estado.tiendaPuedePasarA(EstadoOrden.RESUELTA, ModalidadEntrega.DOMICILIO))
                    .isFalse();
        }
    }

    @Test
    @Tag("HU-028")
    @Tag("CP-ORD-028-01")
    @DisplayName("CP-ORD-028-01 Al confirmar la recepción la orden queda RESUELTA y la tienda deja de ver los datos")
    void confirmaRecepcion() {
        // Dado
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);
        orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
        orden.avanzarPorTienda(EstadoOrden.EN_CAMINO, AHORA);
        orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA);
        assertThat(orden.muestraDatosDelClienteA(DUENO)).isTrue();
        // Cuando
        orden.confirmarRecepcion(AHORA.plusSeconds(600));
        // Entonces
        assertThat(orden.getEstado()).isEqualTo(EstadoOrden.RESUELTA);
        assertThat(orden.esActiva()).isFalse();
        assertThat(orden.muestraDatosDelClienteA(DUENO)).isFalse();
    }

    @Test
    @Tag("HU-028")
    @Tag("NF-FIA-016")
    @DisplayName("No se puede confirmar la recepción antes de ENTREGADO")
    void noConfirmaAntesDeEntregado() {
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);
        assertThatThrownBy(() -> orden.confirmarRecepcion(AHORA)).isInstanceOf(TransicionOrdenInvalidaException.class);
    }

    @Test
    @Tag("HU-024")
    @Tag("CP-ORD-024-02")
    @Tag("CP-ORD-024-03")
    @DisplayName("CP-ORD-024-03 Solo el cliente y la tienda elegida son parte; el cliente no recibe sus propios datos")
    void autorizacionPorObjeto() {
        var orden = confirmada(CLIENTE, DUENO, ModalidadEntrega.DOMICILIO);
        var otraTienda = UUID.randomUUID();

        assertThat(orden.esParte(CLIENTE)).isTrue();
        assertThat(orden.esParte(DUENO)).isTrue();
        assertThat(orden.esParte(otraTienda)).isFalse();
        assertThat(orden.muestraDatosDelClienteA(DUENO)).isTrue();
        assertThat(orden.muestraDatosDelClienteA(CLIENTE)).isFalse();
        assertThat(orden.getContactoCliente().toString()).doesNotContain("Ana");
        assertThat(orden.getDireccionEntrega().toString()).doesNotContain("Calle");
    }

    @Test
    @DisplayName("Reconstituir conserva estado, historial y datos copiados")
    void reconstituir() {
        var original = confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
        var copia = Orden.reconstituir(
                original.getId(),
                original.getSolicitudId(),
                original.getRespuestaId(),
                CLIENTE,
                new Orden.TiendaDeOrden(original.getTiendaId(), DUENO, "Droguería Central", "3001112233"),
                original.getProducto(),
                original.getCantidad(),
                original.getPrecioUnitarioEnPesos(),
                original.getCostoDomicilioEnPesos(),
                original.getModalidad(),
                MedioPagoOrden.DATAFONO,
                original.getContactoCliente(),
                null,
                EstadoOrden.PREPARANDO,
                original.getFechaCreacion(),
                original.getHistorial());

        assertThat(copia.getEstado()).isEqualTo(EstadoOrden.PREPARANDO);
        assertThat(copia.getMedioPago()).isEqualTo(MedioPagoOrden.DATAFONO);
        assertThat(copia.getNombreTienda()).isEqualTo("Droguería Central");
        assertThat(copia.getTelefonoTienda()).isEqualTo("3001112233");
        assertThat(copia.getHistorial()).hasSize(1);
        assertThat(EstadoOrden.PREPARANDO.textoParaUsuario()).isEqualTo("en preparación");
    }
}
