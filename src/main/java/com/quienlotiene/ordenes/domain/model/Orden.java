package com.quienlotiene.ordenes.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lo que nace cuando el cliente elige una respuesta (glosario: orden). Guarda el desglose del total y copias del
 * contacto del cliente y de la tienda, para no consultar otros módulos en cada pantalla.
 */
public final class Orden {

    private final UUID id;
    private final UUID solicitudId;
    private final UUID respuestaId;
    private final UUID clienteId;
    private final UUID tiendaId;
    private final UUID duenoTiendaId;
    private final String producto;
    private final int cantidad;
    private final long precioUnitarioEnPesos;
    private final long costoDomicilioEnPesos;
    private final ModalidadEntrega modalidad;
    private final MedioPagoOrden medioPago;
    private final ContactoCliente contactoCliente;
    private final DireccionDeEntrega direccionEntrega;
    private final String nombreTienda;
    private final String telefonoTienda;
    private final Instant fechaCreacion;
    private final List<CambioEstado> historial;
    private EstadoOrden estado;
    private Instant reservaVence;
    private String motivoCancelacion;
    private ParteOrden canceladaPor;

    /** HU-025: cuánto dura la reserva desde que la tienda marca LISTO_PARA_RECOGER (contrato del Sprint 3, decisión 2). */
    public static final Duration RESERVA_POR_DEFECTO = Duration.ofMinutes(30);

    /** Lo que cambia después de crear la orden, además del estado: reserva (HU-025) y cancelación (HU-029). */
    public record Seguimiento(Instant reservaVence, String motivoCancelacion, ParteOrden canceladaPor) {

        public static final Seguimiento NINGUNO = new Seguimiento(null, null, null);
    }

    private Orden(
            UUID id,
            UUID solicitudId,
            UUID respuestaId,
            UUID clienteId,
            UUID tiendaId,
            UUID duenoTiendaId,
            String producto,
            int cantidad,
            long precioUnitarioEnPesos,
            long costoDomicilioEnPesos,
            ModalidadEntrega modalidad,
            MedioPagoOrden medioPago,
            ContactoCliente contactoCliente,
            DireccionDeEntrega direccionEntrega,
            String nombreTienda,
            String telefonoTienda,
            EstadoOrden estado,
            Instant fechaCreacion,
            List<CambioEstado> historial,
            Seguimiento seguimiento) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.respuestaId = respuestaId;
        this.clienteId = clienteId;
        this.tiendaId = tiendaId;
        this.duenoTiendaId = duenoTiendaId;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitarioEnPesos = precioUnitarioEnPesos;
        this.costoDomicilioEnPesos = costoDomicilioEnPesos;
        this.modalidad = modalidad;
        this.medioPago = medioPago;
        this.contactoCliente = contactoCliente;
        this.direccionEntrega = direccionEntrega;
        this.nombreTienda = nombreTienda;
        this.telefonoTienda = telefonoTienda;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.historial = new ArrayList<>(historial);
        this.reservaVence = seguimiento.reservaVence();
        this.motivoCancelacion = seguimiento.motivoCancelacion();
        this.canceladaPor = seguimiento.canceladaPor();
    }

    /** Datos de la tienda elegida que se copian a la orden. */
    public record TiendaDeOrden(UUID tiendaId, UUID duenoId, String nombre, String telefono) {}

    /**
     * HU-024: crea la orden CONFIRMADO. Total = precio × cantidad + domicilio (CP-ORD-024-01).
     *
     * @param costoDomicilioEnPesos 0 para recoger
     * @param direccionEntrega obligatoria con DOMICILIO; se ignora para recoger
     * @throws OrdenIncompletaException si pide domicilio sin dirección
     */
    public static Orden crear(
            UUID id,
            UUID solicitudId,
            UUID respuestaId,
            UUID clienteId,
            TiendaDeOrden tienda,
            String producto,
            int cantidad,
            long precioUnitarioEnPesos,
            long costoDomicilioEnPesos,
            ModalidadEntrega modalidad,
            MedioPagoOrden medioPago,
            ContactoCliente contactoCliente,
            DireccionDeEntrega direccionEntrega,
            Instant ahora) {
        if (modalidad == ModalidadEntrega.DOMICILIO && direccionEntrega == null) {
            throw new OrdenIncompletaException("elige la dirección de entrega");
        }
        var esDomicilio = modalidad == ModalidadEntrega.DOMICILIO;
        return new Orden(
                id,
                solicitudId,
                respuestaId,
                clienteId,
                tienda.tiendaId(),
                tienda.duenoId(),
                producto,
                cantidad,
                precioUnitarioEnPesos,
                esDomicilio ? costoDomicilioEnPesos : 0,
                modalidad,
                medioPago,
                contactoCliente,
                esDomicilio ? direccionEntrega : null,
                tienda.nombre(),
                tienda.telefono(),
                EstadoOrden.CONFIRMADO,
                ahora,
                List.of(new CambioEstado(EstadoOrden.CONFIRMADO, ahora)),
                Seguimiento.NINGUNO);
    }

    /** Vuelve a armar una orden guardada; solo lo usa el adaptador de persistencia. */
    public static Orden reconstituir(
            UUID id,
            UUID solicitudId,
            UUID respuestaId,
            UUID clienteId,
            TiendaDeOrden tienda,
            String producto,
            int cantidad,
            long precioUnitarioEnPesos,
            long costoDomicilioEnPesos,
            ModalidadEntrega modalidad,
            MedioPagoOrden medioPago,
            ContactoCliente contactoCliente,
            DireccionDeEntrega direccionEntrega,
            EstadoOrden estado,
            Instant fechaCreacion,
            List<CambioEstado> historial,
            Seguimiento seguimiento) {
        return new Orden(
                id,
                solicitudId,
                respuestaId,
                clienteId,
                tienda.tiendaId(),
                tienda.duenoId(),
                producto,
                cantidad,
                precioUnitarioEnPesos,
                costoDomicilioEnPesos,
                modalidad,
                medioPago,
                contactoCliente,
                direccionEntrega,
                tienda.nombre(),
                tienda.telefono(),
                estado,
                fechaCreacion,
                historial,
                seguimiento);
    }

    /**
     * HU-027: la tienda avanza un paso (CP-ORD-027-01).
     *
     * @throws TransicionOrdenInvalidaException si no es el paso siguiente válido (CP-ORD-027-02, RNF-016)
     */
    public void avanzarPorTienda(EstadoOrden siguiente, Instant ahora) {
        avanzarPorTienda(siguiente, ahora, RESERVA_POR_DEFECTO);
    }

    /**
     * HU-027 y HU-025: igual que {@link #avanzarPorTienda(EstadoOrden, Instant)}; al pasar a LISTO_PARA_RECOGER empieza
     * la reserva de {@code duracionReserva} (CP-ORD-025-01).
     */
    public void avanzarPorTienda(EstadoOrden siguiente, Instant ahora, Duration duracionReserva) {
        if (!estado.tiendaPuedePasarA(siguiente, modalidad)) {
            throw new TransicionOrdenInvalidaException(estado, siguiente);
        }
        if (siguiente == EstadoOrden.LISTO_PARA_RECOGER) {
            this.reservaVence = ahora.plus(duracionReserva);
        }
        cambiarA(siguiente, ahora);
    }

    /**
     * HU-029: el cliente cancela mientras la orden no ha salido de la tienda (CP-ORD-029-01). Una recogida lista
     * todavía no salió, así que también se puede cancelar.
     *
     * @throws OrdenYaDespachadaException si va en camino o ya se entregó (CP-ORD-029-02)
     * @throws TransicionOrdenInvalidaException si ya terminó (RESUELTA, CANCELADO o VENCIDA)
     */
    public void cancelarPorCliente(String motivo, Instant ahora) {
        switch (estado) {
            case CONFIRMADO, PREPARANDO, LISTO_PARA_RECOGER -> cancelar(ParteOrden.CLIENTE, motivo, ahora);
            case EN_CAMINO, ENTREGADO -> throw new OrdenYaDespachadaException();
            default -> throw new TransicionOrdenInvalidaException(estado, EstadoOrden.CANCELADO);
        }
    }

    /**
     * HU-029, diagrama 15 (nota): la tienda solo cancela antes de empezar a preparar.
     *
     * @throws TransicionOrdenInvalidaException si la orden ya no está CONFIRMADO
     */
    public void cancelarPorTienda(String motivo, Instant ahora) {
        if (estado != EstadoOrden.CONFIRMADO) {
            throw new TransicionOrdenInvalidaException(estado, EstadoOrden.CANCELADO);
        }
        cancelar(ParteOrden.TIENDA, motivo, ahora);
    }

    /**
     * HU-025, CU-30: si la recogida lista pasó su reserva sin entregarse, queda VENCIDA (CP-ORD-025-01).
     *
     * @return {@code true} si venció ahora; {@code false} si no le tocaba (ya cambió o la reserva sigue vigente)
     */
    public boolean vencerReserva(Instant ahora) {
        if (estado != EstadoOrden.LISTO_PARA_RECOGER || !reservaVencidaEn(ahora)) {
            return false;
        }
        cambiarA(EstadoOrden.VENCIDA, ahora);
        return true;
    }

    private boolean reservaVencidaEn(Instant ahora) {
        return reservaVence != null && !ahora.isBefore(reservaVence);
    }

    private void cancelar(ParteOrden quien, String motivo, Instant ahora) {
        if (motivo == null || motivo.isBlank()) {
            throw new OrdenIncompletaException("escribe el motivo de la cancelación");
        }
        this.motivoCancelacion = motivo.strip();
        this.canceladaPor = quien;
        cambiarA(EstadoOrden.CANCELADO, ahora);
    }

    /**
     * HU-028: el cliente confirma que recibió el pedido; la tienda deja de ver sus datos (CP-ORD-028-01).
     *
     * @throws TransicionOrdenInvalidaException si la orden no está ENTREGADO
     */
    public void confirmarRecepcion(Instant ahora) {
        if (estado != EstadoOrden.ENTREGADO) {
            throw new TransicionOrdenInvalidaException(estado, EstadoOrden.RESUELTA);
        }
        cambiarA(EstadoOrden.RESUELTA, ahora);
    }

    private void cambiarA(EstadoOrden nuevo, Instant ahora) {
        this.estado = nuevo;
        this.historial.add(new CambioEstado(nuevo, ahora));
    }

    public long subtotalEnPesos() {
        return precioUnitarioEnPesos * cantidad;
    }

    public long totalEnPesos() {
        return subtotalEnPesos() + costoDomicilioEnPesos;
    }

    public boolean esActiva() {
        return estado.esActivo();
    }

    public boolean esDelCliente(UUID usuarioId) {
        return clienteId.equals(usuarioId);
    }

    public boolean esDeLaTienda(UUID usuarioId) {
        return duenoTiendaId.equals(usuarioId);
    }

    /** Autorización por objeto (R-03): solo el cliente o el dueño de la tienda de la orden. */
    public boolean esParte(UUID usuarioId) {
        return esDelCliente(usuarioId) || esDeLaTienda(usuarioId);
    }

    /**
     * CP-ORD-024-02 y CP-ORD-028-01: la tienda ve los datos del cliente solo mientras la orden está activa. El cliente
     * nunca los recibe aquí (son suyos).
     */
    public boolean muestraDatosDelClienteA(UUID usuarioId) {
        return esDeLaTienda(usuarioId) && esActiva();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getRespuestaId() {
        return respuestaId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getTiendaId() {
        return tiendaId;
    }

    public UUID getDuenoTiendaId() {
        return duenoTiendaId;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public long getPrecioUnitarioEnPesos() {
        return precioUnitarioEnPesos;
    }

    public long getCostoDomicilioEnPesos() {
        return costoDomicilioEnPesos;
    }

    public ModalidadEntrega getModalidad() {
        return modalidad;
    }

    public MedioPagoOrden getMedioPago() {
        return medioPago;
    }

    public ContactoCliente getContactoCliente() {
        return contactoCliente;
    }

    /** {@code null} si es para recoger. */
    public DireccionDeEntrega getDireccionEntrega() {
        return direccionEntrega;
    }

    public String getNombreTienda() {
        return nombreTienda;
    }

    public String getTelefonoTienda() {
        return telefonoTienda;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    /** {@code null} mientras la recogida no esté lista, y siempre en domicilio. */
    public Instant getReservaVence() {
        return reservaVence;
    }

    /** {@code null} si no fue cancelada. */
    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    /** {@code null} si no fue cancelada. */
    public ParteOrden getCanceladaPor() {
        return canceladaPor;
    }

    public List<CambioEstado> getHistorial() {
        return List.copyOf(historial);
    }
}
