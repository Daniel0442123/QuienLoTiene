package com.quienlotiene.ordenes.application.usecase;

import static com.quienlotiene.ordenes.domain.model.OrdenesDePrueba.AHORA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.quienlotiene.clientes.api.DireccionEntrega;
import com.quienlotiene.clientes.api.DireccionesApi;
import com.quienlotiene.identidad.api.ContactoUsuario;
import com.quienlotiene.identidad.api.ContactoUsuariosApi;
import com.quienlotiene.ordenes.api.EstadoOrdenCambiadoEvent;
import com.quienlotiene.ordenes.api.OrdenCreadaEvent;
import com.quienlotiene.ordenes.application.dto.CrearOrdenComando;
import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.EstadoOrden;
import com.quienlotiene.ordenes.domain.model.MedioPagoNoAceptadoException;
import com.quienlotiene.ordenes.domain.model.ModalidadEntrega;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.model.OrdenIncompletaException;
import com.quienlotiene.ordenes.domain.model.OrdenNoEncontradaException;
import com.quienlotiene.ordenes.domain.model.OrdenesDePrueba;
import com.quienlotiene.ordenes.domain.model.ProductoRestringidoSinDomicilioException;
import com.quienlotiene.ordenes.domain.model.TransicionOrdenInvalidaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import com.quienlotiene.solicitudes.api.RespuestaElegida;
import com.quienlotiene.solicitudes.api.SolicitudesApi;
import com.quienlotiene.tiendas.api.DatosTienda;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("HU-024, HU-027 y HU-028 Casos de uso de órdenes")
class OrdenesCasosDeUsoTest {

    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);
    private static final UUID CLIENTE = UUID.randomUUID();
    private static final UUID DUENO = UUID.randomUUID();

    @Mock
    SolicitudesApi solicitudesApi;

    @Mock
    DatosTiendasApi datosTiendasApi;

    @Mock
    DireccionesApi direccionesApi;

    @Mock
    ContactoUsuariosApi contactoUsuariosApi;

    @Mock
    OrdenRepository ordenRepository;

    @Mock
    ApplicationEventPublisher publicadorEventos;

    private CrearOrden crearOrden() {
        return new CrearOrden(
                solicitudesApi,
                datosTiendasApi,
                direccionesApi,
                contactoUsuariosApi,
                ordenRepository,
                publicadorEventos,
                RELOJ);
    }

    private Orden guardada(ModalidadEntrega modalidad) {
        var orden = OrdenesDePrueba.confirmada(CLIENTE, DUENO, modalidad);
        when(ordenRepository.buscarPorId(orden.getId())).thenReturn(Optional.of(orden));
        return orden;
    }

    @Nested
    @Tag("HU-024")
    @DisplayName("HU-024 Crear orden")
    class Crear {

        private final UUID tiendaId = UUID.randomUUID();
        private final UUID respuestaId = UUID.randomUUID();

        private void eligeRespuesta() {
            when(solicitudesApi.elegirRespuesta(eq(CLIENTE), eq(respuestaId), any()))
                    .thenReturn(new RespuestaElegida(
                            UUID.randomUUID(), respuestaId, tiendaId, DUENO, "Paracetamol 500 mg", 2, 8_000, 4_000L));
        }

        private void tiendaYCliente() {
            when(datosTiendasApi.buscarPorIds(List.of(tiendaId)))
                    .thenReturn(List.of(new DatosTienda(tiendaId, DUENO, "Droguería Central", "3001112233")));
            when(contactoUsuariosApi.obtenerContacto(CLIENTE))
                    .thenReturn(new ContactoUsuario(CLIENTE, "Ana Pérez", "3109998877"));
        }

        @Test
        @Tag("CP-ORD-024-01")
        @DisplayName("CP-ORD-024-01 A domicilio recalcula el costo con la dirección elegida y suma 21.000")
        void creaADomicilio() {
            // Dado
            var direccionId = UUID.randomUUID();
            eligeRespuesta();
            tiendaYCliente();
            when(datosTiendasApi.aceptaMedioPago(tiendaId, "EFECTIVO")).thenReturn(true);
            when(direccionesApi.obtenerDireccionDe(CLIENTE, direccionId))
                    .thenReturn(new DireccionEntrega(
                            direccionId, "Casa", "Calle 5 # 10-20", null, "3205556677", 2.44, -76.61));
            when(datosTiendasApi.calcularCostoDomicilio(tiendaId, 2.44, -76.61, 16_000))
                    .thenReturn(5_000L);
            // Cuando
            var vista = crearOrden()
                    .ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, "DOMICILIO", direccionId, "EFECTIVO"));
            // Entonces
            assertThat(vista.totalEnPesos()).isEqualTo(21_000);
            assertThat(vista.costoDomicilioEnPesos()).isEqualTo(5_000);
            assertThat(vista.estado()).isEqualTo("CONFIRMADO");
            assertThat(vista.cliente())
                    .as("el cliente no recibe sus propios datos")
                    .isNull();
            var orden = ArgumentCaptor.forClass(Orden.class);
            verify(ordenRepository).guardarNueva(orden.capture());
            assertThat(orden.getValue().getContactoCliente().telefono()).isEqualTo("3205556677");
            assertThat(orden.getValue().getDireccionEntrega().direccionEscrita())
                    .isEqualTo("Calle 5 # 10-20");
            var evento = ArgumentCaptor.forClass(OrdenCreadaEvent.class);
            verify(publicadorEventos).publishEvent(evento.capture());
            assertThat(evento.getValue().duenoTiendaId()).isEqualTo(DUENO);
            assertThat(evento.getValue().totalEnPesos()).isEqualTo(21_000);
        }

        @Test
        @DisplayName("Para recoger usa el teléfono de la cuenta y no cobra domicilio")
        void creaParaRecoger() {
            eligeRespuesta();
            tiendaYCliente();
            when(datosTiendasApi.aceptaMedioPago(tiendaId, "TRANSFERENCIA")).thenReturn(true);

            var vista = crearOrden()
                    .ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, "RECOGER", null, "TRANSFERENCIA"));

            assertThat(vista.totalEnPesos()).isEqualTo(16_000);
            assertThat(vista.medioPago()).isEqualTo("TRANSFERENCIA");
            verifyNoInteractions(direccionesApi);
        }

        @Test
        @Tag("HU-023")
        @Tag("CP-SOL-023-02")
        @DisplayName("CP-SOL-023-02 Un producto restringido no se puede pedir a domicilio (ORD-006)")
        void restringidoSinDomicilio() {
            when(solicitudesApi.elegirRespuesta(eq(CLIENTE), eq(respuestaId), any()))
                    .thenReturn(new RespuestaElegida(
                            UUID.randomUUID(), respuestaId, tiendaId, DUENO, "Amoxicilina", 1, 9_000, null, true));

            assertThatThrownBy(() -> crearOrden()
                            .ejecutar(new CrearOrdenComando(
                                    CLIENTE, respuestaId, "DOMICILIO", UUID.randomUUID(), "EFECTIVO")))
                    .isInstanceOf(ProductoRestringidoSinDomicilioException.class)
                    .hasFieldOrPropertyWithValue("codigo", "ORD-006");
            verify(ordenRepository, never()).guardarNueva(any());
        }

        @Test
        @Tag("CP-ORD-024-04")
        @DisplayName("CP-ORD-024-04 Un medio de pago que la tienda no tiene activo rechaza la orden")
        void rechazaMedioDePago() {
            eligeRespuesta();
            when(datosTiendasApi.aceptaMedioPago(tiendaId, "DATAFONO")).thenReturn(false);

            assertThatThrownBy(() -> crearOrden()
                            .ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, "RECOGER", null, "DATAFONO")))
                    .isInstanceOf(MedioPagoNoAceptadoException.class);
            verify(ordenRepository, never()).guardarNueva(any());
        }

        @Test
        @DisplayName("Datos incompletos o desconocidos se rechazan antes de elegir la respuesta")
        void rechazaIncompleta() {
            var crear = crearOrden();
            assertThatThrownBy(() ->
                            crear.ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, "DOMICILIO", null, "EFECTIVO")))
                    .isInstanceOf(OrdenIncompletaException.class);
            assertThatThrownBy(
                            () -> crear.ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, null, null, "EFECTIVO")))
                    .isInstanceOf(OrdenIncompletaException.class);
            assertThatThrownBy(() ->
                            crear.ejecutar(new CrearOrdenComando(CLIENTE, respuestaId, "RECOGER", null, "BITCOIN")))
                    .isInstanceOf(OrdenIncompletaException.class);
            verifyNoInteractions(solicitudesApi);
        }
    }

    @Nested
    @Tag("HU-027")
    @DisplayName("HU-027 Consultar y cambiar el estado")
    class Estados {

        @Test
        @Tag("CP-ORD-027-01")
        @DisplayName("CP-ORD-027-01 La tienda pasa la orden a PREPARANDO y se avisa a las dos partes")
        void cambiaEstado() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);

            var vista = new CambiarEstadoOrden(ordenRepository, publicadorEventos, RELOJ, 30)
                    .ejecutar(DUENO, orden.getId(), "PREPARANDO");

            assertThat(vista.estado()).isEqualTo("PREPARANDO");
            assertThat(vista.cliente()).isNotNull();
            assertThat(vista.cliente().direccion().direccionEscrita()).isEqualTo("Calle 5 # 10-20");
            assertThat(vista.historial())
                    .extracting(OrdenVista.CambioEstadoOrden::estado)
                    .containsExactly("CONFIRMADO", "PREPARANDO");
            verify(ordenRepository).guardarCambioEstado(orden);
            verify(publicadorEventos)
                    .publishEvent(new EstadoOrdenCambiadoEvent(orden.getId(), CLIENTE, DUENO, "PREPARANDO"));
        }

        @Test
        @Tag("CP-ORD-027-02")
        @Tag("NF-FIA-016")
        @DisplayName("CP-ORD-027-02 Un estado inválido o desconocido se rechaza; otra tienda recibe 404")
        void rechazaCambio() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            var cambiar = new CambiarEstadoOrden(ordenRepository, publicadorEventos, RELOJ, 30);

            assertThatThrownBy(() -> cambiar.ejecutar(DUENO, orden.getId(), "ENTREGADO"))
                    .isInstanceOf(TransicionOrdenInvalidaException.class);
            assertThatThrownBy(() -> cambiar.ejecutar(DUENO, orden.getId(), "VOLANDO"))
                    .isInstanceOf(TransicionOrdenInvalidaException.class);
            assertThatThrownBy(() -> cambiar.ejecutar(UUID.randomUUID(), orden.getId(), "PREPARANDO"))
                    .isInstanceOf(OrdenNoEncontradaException.class);
            verify(publicadorEventos, never()).publishEvent(any());
        }

        @Test
        @Tag("HU-024")
        @Tag("CP-ORD-024-03")
        @DisplayName("CP-ORD-024-03 Una tienda no elegida no puede consultar la orden")
        void consultaSoloLasPartes() {
            var orden = guardada(ModalidadEntrega.RECOGER);
            var consultar = new ConsultarOrden(ordenRepository);

            assertThat(consultar.ejecutar(CLIENTE, orden.getId()).cliente()).isNull();
            var deLaTienda = consultar.ejecutar(DUENO, orden.getId());
            assertThat(deLaTienda.cliente().nombre()).isEqualTo("Ana Pérez");
            assertThat(deLaTienda.cliente().direccion()).isNull();
            assertThat(deLaTienda.tienda().nombre()).isEqualTo("Droguería Central");
            assertThatThrownBy(() -> consultar.ejecutar(UUID.randomUUID(), orden.getId()))
                    .isInstanceOf(OrdenNoEncontradaException.class);
        }

        @Test
        @DisplayName("Mis órdenes muestra primero las activas y luego las más recientes")
        void listaActivasPrimero() {
            var resuelta = OrdenesDePrueba.confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            resuelta.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
            resuelta.avanzarPorTienda(EstadoOrden.LISTO_PARA_RECOGER, AHORA);
            resuelta.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA);
            resuelta.confirmarRecepcion(AHORA);
            var activa = OrdenesDePrueba.confirmada(CLIENTE, DUENO, ModalidadEntrega.RECOGER);
            when(ordenRepository.listarDeUsuario(eq(CLIENTE), anyInt())).thenReturn(List.of(resuelta, activa));

            var mias = new ListarMisOrdenes(ordenRepository).ejecutar(CLIENTE);

            assertThat(mias).extracting(OrdenVista::id).containsExactly(activa.getId(), resuelta.getId());
        }
    }

    @Nested
    @Tag("HU-028")
    @DisplayName("HU-028 Confirmar recepción")
    class Recepcion {

        @Test
        @Tag("CP-ORD-028-01")
        @DisplayName("CP-ORD-028-01 El cliente confirma una orden ENTREGADO y queda RESUELTA")
        void confirma() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            orden.avanzarPorTienda(EstadoOrden.PREPARANDO, AHORA);
            orden.avanzarPorTienda(EstadoOrden.EN_CAMINO, AHORA);
            orden.avanzarPorTienda(EstadoOrden.ENTREGADO, AHORA);

            var vista =
                    new ConfirmarRecepcion(ordenRepository, publicadorEventos, RELOJ).ejecutar(CLIENTE, orden.getId());

            assertThat(vista.estado()).isEqualTo("RESUELTA");
            assertThat(vista.esActiva()).isFalse();
            verify(publicadorEventos)
                    .publishEvent(new EstadoOrdenCambiadoEvent(orden.getId(), CLIENTE, DUENO, "RESUELTA"));
        }

        @Test
        @Tag("CP-ORD-028-02")
        @DisplayName("CP-ORD-028-02 Otro cliente o la tienda no pueden confirmar la recepción")
        void rechazaAjena() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            var confirmar = new ConfirmarRecepcion(ordenRepository, publicadorEventos, RELOJ);

            assertThatThrownBy(() -> confirmar.ejecutar(UUID.randomUUID(), orden.getId()))
                    .isInstanceOf(OrdenNoEncontradaException.class);
            assertThatThrownBy(() -> confirmar.ejecutar(DUENO, orden.getId()))
                    .isInstanceOf(OrdenNoEncontradaException.class);
            assertThat(orden.getEstado()).isEqualTo(EstadoOrden.CONFIRMADO);
        }
    }

    @Nested
    @Tag("HU-032")
    @DisplayName("HU-032 Contacto de la otra parte")
    class Contacto {

        @Test
        @Tag("CP-MEN-032-01")
        @DisplayName("CP-MEN-032-01 El cliente recibe el teléfono de la tienda y la tienda el del cliente")
        void contactoDeLaOtraParte() {
            var orden = guardada(ModalidadEntrega.DOMICILIO);
            var consultar = new ConsultarContactoDeOrden(ordenRepository);

            var paraCliente = consultar.obtenerContactoDeLaOtraParte(orden.getId(), CLIENTE);
            var paraTienda = consultar.obtenerContactoDeLaOtraParte(orden.getId(), DUENO);

            assertThat(paraCliente.telefono()).isEqualTo("3001112233");
            assertThat(paraCliente.nombre()).isEqualTo("Droguería Central");
            assertThat(paraTienda.telefono()).isEqualTo("3109998877");
            assertThat(paraTienda.esActiva()).isTrue();
            assertThat(paraTienda.toString()).doesNotContain("3109998877");
            assertThatThrownBy(() -> consultar.obtenerContactoDeLaOtraParte(orden.getId(), UUID.randomUUID()))
                    .isInstanceOf(OrdenNoEncontradaException.class);
        }
    }
}
