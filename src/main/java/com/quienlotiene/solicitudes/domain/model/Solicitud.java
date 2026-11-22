package com.quienlotiene.solicitudes.domain.model;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Lo que el cliente publica: "¿quién tiene X?" (glosario). Nace ABIERTA con radio de 3 km, puede ampliarse una vez a
 * 5 km si nadie responde (HU-022) y vence a los 30 minutos.
 */
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
    private int radioEnMetros;
    private boolean fueAmpliado;
    private final boolean esRestringida;
    private EstadoSolicitud estado;
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
            boolean fueAmpliado,
            boolean esRestringida,
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
        this.fueAmpliado = fueAmpliado;
        this.esRestringida = esRestringida;
        this.estado = estado;
        this.fechaPublicacion = fechaPublicacion;
        this.fechaVencimiento = fechaVencimiento;
    }

    /**
     * HU-016: publica una solicitud abierta con el radio inicial de búsqueda.
     *
     * @param esRestringida HU-023: el producto está en la lista de restringidos; las tiendas no le ofrecen domicilio
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
            boolean esRestringida,
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
                false,
                esRestringida,
                EstadoSolicitud.ABIERTA,
                ahora,
                ahora.plus(vigencia));
    }

    /** Vuelve a armar una solicitud guardada; solo lo usa el adaptador de persistencia. */
    public static Solicitud reconstituir(
            UUID id,
            UUID clienteId,
            UUID categoriaId,
            String producto,
            int cantidad,
            Modalidad modalidad,
            boolean esUrgente,
            Ubicacion ubicacion,
            int radioEnMetros,
            boolean fueAmpliado,
            boolean esRestringida,
            EstadoSolicitud estado,
            Instant fechaPublicacion,
            Instant fechaVencimiento) {
        return new Solicitud(
                id,
                clienteId,
                categoriaId,
                producto,
                cantidad,
                modalidad,
                esUrgente,
                ubicacion,
                radioEnMetros,
                fueAmpliado,
                esRestringida,
                estado,
                fechaPublicacion,
                fechaVencimiento);
    }

    /** HU-023: producto de la lista de restringidos (CP-SOL-023-01); no se entrega a domicilio (CP-SOL-023-02). */
    public boolean esRestringida() {
        return esRestringida;
    }

    public boolean estaVencida(Instant ahora) {
        return !ahora.isBefore(fechaVencimiento);
    }

    /** ABIERTA y sin cumplir su tiempo: recibe respuestas y se puede elegir una. */
    public boolean recibeRespuestas(Instant ahora) {
        return estado == EstadoSolicitud.ABIERTA && !estaVencida(ahora);
    }

    /**
     * @throws SolicitudCerradaException si ya no está ABIERTA o ya venció (CP-SOL-021-01)
     */
    public void verificarQueRecibeRespuestas(Instant ahora) {
        if (!recibeRespuestas(ahora)) {
            throw new SolicitudCerradaException();
        }
    }

    /**
     * HU-024: el cliente eligió una respuesta. Una sola elección por solicitud (diagrama 05).
     *
     * @throws SolicitudCerradaException si ya no está ABIERTA o ya venció
     */
    public void resolver(Instant ahora) {
        verificarQueRecibeRespuestas(ahora);
        this.estado = EstadoSolicitud.RESUELTA;
    }

    /**
     * HU-021 (CU-29): pasó su tiempo sin elección. Si ya no está ABIERTA no hace nada.
     *
     * @return {@code true} si pasó a VENCIDA
     */
    public boolean vencer(Instant ahora) {
        if (estado != EstadoSolicitud.ABIERTA || !estaVencida(ahora)) {
            return false;
        }
        this.estado = EstadoSolicitud.VENCIDA;
        return true;
    }

    /**
     * HU-022 (CU-31, diagrama 35 paso 4): nadie respondió a tiempo y la búsqueda se agranda. Solo una vez por solicitud
     * y solo mientras siga recibiendo respuestas; si no se puede, no hace nada.
     *
     * @param nuevoRadioEnMetros debe ser mayor que el radio actual
     * @return {@code true} si el radio cambió
     */
    public boolean ampliarRadio(int nuevoRadioEnMetros, Instant ahora) {
        if (fueAmpliado || !recibeRespuestas(ahora) || nuevoRadioEnMetros <= radioEnMetros) {
            return false;
        }
        this.radioEnMetros = nuevoRadioEnMetros;
        this.fueAmpliado = true;
        return true;
    }

    public boolean fueAmpliado() {
        return fueAmpliado;
    }

    public boolean esDe(UUID posibleCliente) {
        return clienteId.equals(posibleCliente);
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
