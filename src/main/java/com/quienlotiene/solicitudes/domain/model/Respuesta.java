package com.quienlotiene.solicitudes.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Lo que la tienda contesta a una solicitud (HU-019, diagrama 09 {@code solicitudes.respuesta}): SI, PARECIDO o NO.
 * Una sola por tienda y solicitud. Cuando el cliente la elige guarda el id de la orden que nació de ella (HU-024).
 */
public final class Respuesta {

    public static final long PRECIO_MAXIMO_PESOS = 100_000_000L;
    public static final int LONGITUD_MAXIMA_ALTERNATIVA = 300;
    public static final int MINUTOS_ESTIMADOS_MAXIMOS = 240;

    private final UUID id;
    private final UUID solicitudId;
    private final UUID tiendaId;
    private final UUID duenoId;
    private final TipoRespuesta tipo;
    private final Long precioEnPesos;
    private final Integer cantidadDisponible;
    private final String descripcionAlternativa;
    private final Integer tiempoEstimadoEnMinutos;
    private final Long costoDomicilioEnPesos;
    private final Instant fechaRespuesta;
    private UUID ordenId;

    private Respuesta(
            UUID id,
            UUID solicitudId,
            UUID tiendaId,
            UUID duenoId,
            TipoRespuesta tipo,
            Long precioEnPesos,
            Integer cantidadDisponible,
            String descripcionAlternativa,
            Integer tiempoEstimadoEnMinutos,
            Long costoDomicilioEnPesos,
            Instant fechaRespuesta,
            UUID ordenId) {
        this.id = id;
        this.solicitudId = solicitudId;
        this.tiendaId = tiendaId;
        this.duenoId = duenoId;
        this.tipo = tipo;
        this.precioEnPesos = precioEnPesos;
        this.cantidadDisponible = cantidadDisponible;
        this.descripcionAlternativa = descripcionAlternativa;
        this.tiempoEstimadoEnMinutos = tiempoEstimadoEnMinutos;
        this.costoDomicilioEnPesos = costoDomicilioEnPesos;
        this.fechaRespuesta = fechaRespuesta;
        this.ordenId = ordenId;
    }

    /**
     * HU-019: la tienda responde una solicitud abierta. Con NO se descartan precio, unidades, alternativa y domicilio.
     *
     * @param costoDomicilioEnPesos lo calcula tiendas con HU-010; {@code null} si no lleva domicilio a ese punto
     * @throws SolicitudCerradaException si la solicitud ya no está abierta o venció (CP-SOL-021-01)
     * @throws RespuestaIncompletaException si falta el precio o las unidades con SI o PARECIDO, o la descripción con
     *     PARECIDO (CP-SOL-019-02)
     */
    public static Respuesta responder(
            UUID id,
            Solicitud solicitud,
            UUID tiendaId,
            UUID duenoId,
            TipoRespuesta tipo,
            Long precioEnPesos,
            Integer cantidadDisponible,
            String descripcionAlternativa,
            Integer tiempoEstimadoEnMinutos,
            Long costoDomicilioEnPesos,
            Instant ahora) {
        solicitud.verificarQueRecibeRespuestas(ahora);
        if (tipo == null) {
            throw new RespuestaIncompletaException("elige Sí lo tengo, Tengo algo parecido o No lo tengo");
        }
        if (!tipo.ofreceProducto()) {
            return new Respuesta(
                    id, solicitud.getId(), tiendaId, duenoId, tipo, null, null, null, null, null, ahora, null);
        }
        if (precioEnPesos == null || precioEnPesos < 1 || precioEnPesos > PRECIO_MAXIMO_PESOS) {
            throw new RespuestaIncompletaException("escribe el precio por unidad en pesos");
        }
        if (cantidadDisponible == null || cantidadDisponible < 1) {
            throw new RespuestaIncompletaException("escribe cuántas unidades tienes");
        }
        if (cantidadDisponible > solicitud.getCantidad()) {
            throw new RespuestaIncompletaException(
                    "las unidades no pueden pasar de las " + solicitud.getCantidad() + " que pidió el cliente");
        }
        if (tiempoEstimadoEnMinutos != null
                && (tiempoEstimadoEnMinutos < 1 || tiempoEstimadoEnMinutos > MINUTOS_ESTIMADOS_MAXIMOS)) {
            throw new RespuestaIncompletaException(
                    "el tiempo estimado debe estar entre 1 y " + MINUTOS_ESTIMADOS_MAXIMOS + " minutos");
        }
        var alternativa = descripcionAlternativa == null ? null : descripcionAlternativa.strip();
        if (tipo == TipoRespuesta.PARECIDO && (alternativa == null || alternativa.isEmpty())) {
            throw new RespuestaIncompletaException("describe el producto parecido que ofreces");
        }
        if (alternativa != null && alternativa.length() > LONGITUD_MAXIMA_ALTERNATIVA) {
            throw new RespuestaIncompletaException(
                    "la descripción puede tener máximo " + LONGITUD_MAXIMA_ALTERNATIVA + " caracteres");
        }
        return new Respuesta(
                id,
                solicitud.getId(),
                tiendaId,
                duenoId,
                tipo,
                precioEnPesos,
                cantidadDisponible,
                tipo == TipoRespuesta.PARECIDO ? alternativa : null,
                tiempoEstimadoEnMinutos,
                costoDomicilioEnPesos,
                ahora,
                null);
    }

    /** La misma respuesta con el costo de domicilio que calculó tiendas (HU-010) a la ubicación de la solicitud. */
    public Respuesta conCostoDomicilio(Long costoEnPesos) {
        if (!tipo.ofreceProducto()) {
            return this;
        }
        return new Respuesta(
                id,
                solicitudId,
                tiendaId,
                duenoId,
                tipo,
                precioEnPesos,
                cantidadDisponible,
                descripcionAlternativa,
                tiempoEstimadoEnMinutos,
                costoEnPesos,
                fechaRespuesta,
                ordenId);
    }

    /** Vuelve a armar una respuesta guardada; solo lo usa el adaptador de persistencia. */
    public static Respuesta reconstituir(
            UUID id,
            UUID solicitudId,
            UUID tiendaId,
            UUID duenoId,
            TipoRespuesta tipo,
            Long precioEnPesos,
            Integer cantidadDisponible,
            String descripcionAlternativa,
            Integer tiempoEstimadoEnMinutos,
            Long costoDomicilioEnPesos,
            Instant fechaRespuesta,
            UUID ordenId) {
        return new Respuesta(
                id,
                solicitudId,
                tiendaId,
                duenoId,
                tipo,
                precioEnPesos,
                cantidadDisponible,
                descripcionAlternativa,
                tiempoEstimadoEnMinutos,
                costoDomicilioEnPesos,
                fechaRespuesta,
                ordenId);
    }

    /**
     * HU-024: el cliente la elige y nace la orden.
     *
     * @throws RespuestaNoElegibleException si es una respuesta NO
     */
    public void elegir(UUID ordenId) {
        if (!tipo.ofreceProducto()) {
            throw new RespuestaNoElegibleException();
        }
        this.ordenId = ordenId;
    }

    public boolean fueElegida() {
        return ordenId != null;
    }

    /** HU-021: cómo ve la tienda esta respuesta según el estado de la solicitud. */
    public EstadoRespuesta estadoSegun(EstadoSolicitud estadoSolicitud) {
        return switch (estadoSolicitud) {
            case ABIERTA -> EstadoRespuesta.ESPERANDO;
            case VENCIDA -> EstadoRespuesta.VENCIDA;
            case RESUELTA -> fueElegida() ? EstadoRespuesta.ELEGIDA : EstadoRespuesta.NO_ELEGIDA;
        };
    }

    /**
     * HU-020: tarjeta para el cliente. Total = precio × unidades + domicilio, este último solo si la solicitud es
     * DOMICILIO y la tienda lo lleva.
     *
     * @throws IllegalStateException si es una respuesta NO (no tiene tarjeta)
     */
    public TarjetaRespuesta aTarjeta(Modalidad modalidadSolicitud, int distanciaEnMetros) {
        if (!tipo.ofreceProducto()) {
            throw new IllegalStateException("Una respuesta NO no se muestra como tarjeta");
        }
        var subtotal = subtotalEnPesos();
        var domicilio = modalidadSolicitud == Modalidad.DOMICILIO ? costoDomicilioEnPesos : null;
        return new TarjetaRespuesta(
                id,
                tiendaId,
                tipo,
                descripcionAlternativa,
                precioEnPesos,
                cantidadDisponible,
                subtotal,
                costoDomicilioEnPesos,
                subtotal + (domicilio == null ? 0 : domicilio),
                distanciaEnMetros,
                tiempoEstimadoEnMinutos,
                fechaRespuesta,
                fueElegida());
    }

    /** Precio por unidad × unidades disponibles (CP-ORD-024-01); 0 para una respuesta NO. */
    public long subtotalEnPesos() {
        return tipo.ofreceProducto() ? precioEnPesos * cantidadDisponible : 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getTiendaId() {
        return tiendaId;
    }

    public UUID getDuenoId() {
        return duenoId;
    }

    public TipoRespuesta getTipo() {
        return tipo;
    }

    public Long getPrecioEnPesos() {
        return precioEnPesos;
    }

    public Integer getCantidadDisponible() {
        return cantidadDisponible;
    }

    public String getDescripcionAlternativa() {
        return descripcionAlternativa;
    }

    public Integer getTiempoEstimadoEnMinutos() {
        return tiempoEstimadoEnMinutos;
    }

    public Long getCostoDomicilioEnPesos() {
        return costoDomicilioEnPesos;
    }

    public boolean ofreceDomicilio() {
        return costoDomicilioEnPesos != null;
    }

    public Instant getFechaRespuesta() {
        return fechaRespuesta;
    }

    public UUID getOrdenId() {
        return ordenId;
    }
}
