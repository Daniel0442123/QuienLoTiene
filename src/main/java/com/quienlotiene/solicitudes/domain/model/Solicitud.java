package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Lo que el cliente publica: "¿quién tiene X?" (glosario). Nace ABIERTA y vence a los 30 minutos. */
public final class Solicitud {

    public static final int LONGITUD_MAXIMA_PRODUCTO = 150;
    public static final int CANTIDAD_MAXIMA = 999;

    private final UUID id;
    private final UUID clienteId;
    private final UUID categoriaId;
    private final String producto;
    private final int cantidad;
    private final Modalidad modalidad;
    private final boolean esUrgente;
    private final Ubicacion ubicacion;
    private final int radioEnMetros;
    private final EstadoSolicitud estado;
    private final Instant fechaPublicacion;
    private final Instant fechaVencimiento;

    private Solicitud(
            UUID id,
            UUID clienteId,
            UUID categoriaId,
            String producto,
            int cantidad,
            Modalidad modalidad,
            boolean esUrgente,
            Ubicacion ubicacion,
            int radioEnMetros,
            EstadoSolicitud estado,
            Instant fechaPublicacion,
            Instant fechaVencimiento) {
        this.id = id;
        this.clienteId = clienteId;
        this.categoriaId = categoriaId;
        this.producto = producto;
        this.cantidad = cantidad;
        this.modalidad = modalidad;
        this.esUrgente = esUrgente;
        this.ubicacion = ubicacion;
        this.radioEnMetros = radioEnMetros;
        this.estado = estado;
        this.fechaPublicacion = fechaPublicacion;
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * HU-016: publica una solicitud abierta con el radio inicial de búsqueda.
     *
     * @throws SolicitudIncompletaException si falta el producto, la categoría, la modalidad o la ubicación, o la
     *     cantidad no está entre 1 y 999 (CP-SOL-016-02)
     */
    public static Solicitud publicar(
            UUID id,
            UUID clienteId,
            UUID categoriaId,
            String producto,
            int cantidad,
            Modalidad modalidad,
            boolean esUrgente,
            Ubicacion ubicacion,
            int radioInicialEnMetros,
            Duration vigencia,
            Instant ahora) {
        if (producto == null || producto.isBlank()) {
            throw new SolicitudIncompletaException("falta el producto que buscas");
        }
        if (producto.strip().length() > LONGITUD_MAXIMA_PRODUCTO) {
            throw new SolicitudIncompletaException(
                    "el producto puede tener máximo " + LONGITUD_MAXIMA_PRODUCTO + " caracteres");
        }
        if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
            throw new SolicitudIncompletaException("la cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        if (categoriaId == null) {
            throw new SolicitudIncompletaException("falta la categoría");
        }
        if (modalidad == null) {
            throw new SolicitudIncompletaException("elige si vas a recoger o quieres domicilio");
        }
        if (ubicacion == null) {
            throw new SolicitudIncompletaException("falta tu ubicación");
        }
        return new Solicitud(
                id,
                clienteId,
                categoriaId,
                producto.strip(),
                cantidad,
                modalidad,
                esUrgente,
                ubicacion,
                radioInicialEnMetros,
                EstadoSolicitud.ABIERTA,
                ahora,
                ahora.plus(vigencia));
    }

    public boolean estaVencida(Instant ahora) {
        return !ahora.isBefore(fechaVencimiento);
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public boolean esUrgente() {
        return esUrgente;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public int getRadioEnMetros() {
        return radioEnMetros;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public Instant getFechaPublicacion() {
        return fechaPublicacion;
    }

    public Instant getFechaVencimiento() {
        return fechaVencimiento;
    }
}
