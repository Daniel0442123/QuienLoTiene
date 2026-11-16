package com.quienlotiene.ordenes.application.usecase;

import com.quienlotiene.clientes.api.DireccionesApi;
import com.quienlotiene.identidad.api.ContactoUsuariosApi;
import com.quienlotiene.ordenes.api.OrdenCreadaEvent;
import com.quienlotiene.ordenes.application.dto.CrearOrdenComando;
import com.quienlotiene.ordenes.application.dto.OrdenVista;
import com.quienlotiene.ordenes.domain.model.ContactoCliente;
import com.quienlotiene.ordenes.domain.model.DireccionDeEntrega;
import com.quienlotiene.ordenes.domain.model.MedioPagoNoAceptadoException;
import com.quienlotiene.ordenes.domain.model.MedioPagoOrden;
import com.quienlotiene.ordenes.domain.model.ModalidadEntrega;
import com.quienlotiene.ordenes.domain.model.Orden;
import com.quienlotiene.ordenes.domain.model.OrdenIncompletaException;
import com.quienlotiene.ordenes.domain.port.OrdenRepository;
import com.quienlotiene.solicitudes.api.SolicitudesApi;
import com.quienlotiene.tiendas.api.DatosTiendasApi;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HU-024 Elegir respuesta y crear orden (diagrama 04 pasos 1 a 9). Todo en una transacción: si el medio de pago no se
 * acepta o la dirección está fuera de cobertura, la solicitud tampoco queda RESUELTA.
 */
@Slf4j
@Service
public class CrearOrden {

    private final SolicitudesApi solicitudesApi;
    private final DatosTiendasApi datosTiendasApi;
    private final DireccionesApi direccionesApi;
    private final ContactoUsuariosApi contactoUsuariosApi;
    private final OrdenRepository ordenRepository;
    private final ApplicationEventPublisher publicadorEventos;
    private final Clock reloj;

    CrearOrden(
            SolicitudesApi solicitudesApi,
            DatosTiendasApi datosTiendasApi,
            DireccionesApi direccionesApi,
            ContactoUsuariosApi contactoUsuariosApi,
            OrdenRepository ordenRepository,
            ApplicationEventPublisher publicadorEventos,
            Clock reloj) {
        this.solicitudesApi = solicitudesApi;
        this.datosTiendasApi = datosTiendasApi;
        this.direccionesApi = direccionesApi;
        this.contactoUsuariosApi = contactoUsuariosApi;
        this.ordenRepository = ordenRepository;
        this.publicadorEventos = publicadorEventos;
        this.reloj = reloj;
    }

    @Transactional
    public OrdenVista ejecutar(CrearOrdenComando comando) {
        var ahora = reloj.instant();
        var modalidad =
                enumDe(ModalidadEntrega.class, comando.modalidad(), "la modalidad debe ser RECOGER o DOMICILIO");
        var medioPago = enumDe(
                MedioPagoOrden.class,
                comando.medioPago(),
                "el medio de pago debe ser EFECTIVO, TRANSFERENCIA o DATAFONO");
        if (modalidad == ModalidadEntrega.DOMICILIO && comando.direccionId() == null) {
            throw new OrdenIncompletaException("elige la dirección de entrega");
        }

        var ordenId = UUID.randomUUID();
        var elegida = solicitudesApi.elegirRespuesta(comando.clienteId(), comando.respuestaId(), ordenId);
        if (!datosTiendasApi.aceptaMedioPago(elegida.tiendaId(), medioPago.name())) {
            throw new MedioPagoNoAceptadoException();
        }
        var tienda = datosTiendasApi.buscarPorIds(List.of(elegida.tiendaId())).stream()
                .findFirst()
                .orElseThrow();
        var subtotal = elegida.precioUnitarioEnPesos() * elegida.cantidad();

        DireccionDeEntrega direccion = null;
        long costoDomicilio = 0;
        var contacto = contactoUsuariosApi.obtenerContacto(comando.clienteId());
        var telefonoCliente = contacto.telefono();
        if (modalidad == ModalidadEntrega.DOMICILIO) {
            var guardada = direccionesApi.obtenerDireccionDe(comando.clienteId(), comando.direccionId());
            costoDomicilio = datosTiendasApi.calcularCostoDomicilio(
                    elegida.tiendaId(), guardada.latitud(), guardada.longitud(), subtotal);
            direccion = new DireccionDeEntrega(
                    guardada.nombre(),
                    guardada.direccionEscrita(),
                    guardada.indicaciones(),
                    guardada.latitud(),
                    guardada.longitud());
            // Quien recibe el domicilio es el teléfono de la dirección (HU-007)
            telefonoCliente = guardada.telefono();
        }

        var orden = Orden.crear(
                ordenId,
                elegida.solicitudId(),
                elegida.respuestaId(),
                comando.clienteId(),
                new Orden.TiendaDeOrden(tienda.tiendaId(), tienda.duenoId(), tienda.nombre(), tienda.telefono()),
                elegida.producto(),
                elegida.cantidad(),
                elegida.precioUnitarioEnPesos(),
                costoDomicilio,
                modalidad,
                medioPago,
                new ContactoCliente(contacto.nombre(), telefonoCliente),
                direccion,
                ahora);
        ordenRepository.guardarNueva(orden);
        publicadorEventos.publishEvent(new OrdenCreadaEvent(
                orden.getId(),
                orden.getSolicitudId(),
                orden.getClienteId(),
                orden.getDuenoTiendaId(),
                orden.getProducto(),
                orden.totalEnPesos(),
                orden.getModalidad().name()));
        log.info("Orden {} creada para la solicitud {}", orden.getId(), orden.getSolicitudId());
        return OrdenVistaMapper.aVista(orden, comando.clienteId());
    }

    private static <T extends Enum<T>> T enumDe(Class<T> tipo, String valor, String mensaje) {
        if (valor == null) {
            throw new OrdenIncompletaException(mensaje);
        }
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException valorDesconocido) {
            throw new OrdenIncompletaException(mensaje);
        }
    }
}
