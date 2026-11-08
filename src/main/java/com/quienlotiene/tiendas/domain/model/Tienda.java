package com.quienlotiene.tiendas.domain.model;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Negocio que responde solicitudes (glosario). Nace PENDIENTE y cerrada; recibe solicitudes solo aprobada y abierta. */
public final class Tienda {

    public static final int LONGITUD_MAXIMA_NOMBRE = 120;
    public static final int LONGITUD_MINIMA_MOTIVO = 5;
    public static final int LONGITUD_MAXIMA_MOTIVO = 300;

    private final UUID id;
    private final UUID duenoId;
    private final UUID categoriaId;
    private final String nombre;
    private final String telefono;
    private final Ubicacion ubicacion;
    private EstadoTienda estado;
    private final boolean estaAbierta;
    private final Instant fechaRegistro;
    private String motivoRechazo;
    private Instant fechaRevision;

    private Tienda(
            UUID id,
            UUID duenoId,
            UUID categoriaId,
            String nombre,
            String telefono,
            Ubicacion ubicacion,
            EstadoTienda estado,
            boolean estaAbierta,
            Instant fechaRegistro,
            String motivoRechazo,
            Instant fechaRevision) {
        this.id = id;
        this.duenoId = duenoId;
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.telefono = telefono;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.estaAbierta = estaAbierta;
        this.fechaRegistro = fechaRegistro;
        this.motivoRechazo = motivoRechazo;
        this.fechaRevision = fechaRevision;
    }

    /**
     * HU-002: la tienda queda PENDIENTE de revisión y cerrada (CP-TIE-002-01).
     *
     * @throws TiendaIncompletaException si falta el nombre, la categoría, el teléfono o la ubicación
     */
    public static Tienda registrar(
            UUID id,
            UUID duenoId,
            UUID categoriaId,
            String nombre,
            String telefono,
            Ubicacion ubicacion,
            Instant ahora) {
        if (nombre == null || nombre.isBlank()) {
            throw new TiendaIncompletaException("falta el nombre de la tienda");
        }
        if (nombre.strip().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new TiendaIncompletaException(
                    "el nombre puede tener máximo " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }
        if (categoriaId == null) {
            throw new TiendaIncompletaException("falta la categoría");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new TiendaIncompletaException("falta el teléfono");
        }
        if (ubicacion == null) {
            throw new TiendaIncompletaException("falta la ubicación del local");
        }
        return new Tienda(
                id,
                duenoId,
                categoriaId,
                nombre.strip(),
                telefono.strip(),
                ubicacion,
                EstadoTienda.PENDIENTE,
                false,
                ahora,
                null,
                null);
    }

    /** Vuelve a armar una tienda guardada; solo lo usa el adaptador de persistencia. */
    public static Tienda reconstituir(
            UUID id,
            UUID duenoId,
            UUID categoriaId,
            String nombre,
            String telefono,
            Ubicacion ubicacion,
            EstadoTienda estado,
            boolean estaAbierta,
            Instant fechaRegistro,
            String motivoRechazo,
            Instant fechaRevision) {
        return new Tienda(
                id,
                duenoId,
                categoriaId,
                nombre,
                telefono,
                ubicacion,
                estado,
                estaAbierta,
                fechaRegistro,
                motivoRechazo,
                fechaRevision);
    }

    /**
     * HU-036 (CP-ADM-036-01): el administrador revisó los documentos y la tienda puede empezar a recibir solicitudes
     * cuando la abra.
     *
     * @throws TransicionTiendaInvalidaException si la tienda no está PENDIENTE
     */
    public void aprobar(Instant ahora) {
        exigirEstado(EstadoTienda.PENDIENTE, "aprobar");
        this.estado = EstadoTienda.APROBADA;
        this.motivoRechazo = null;
        this.fechaRevision = ahora;
    }

    /**
     * HU-036 (CP-ADM-036-02): la tienda queda RECHAZADA y su dueño ve el motivo.
     *
     * @throws MotivoRechazoInvalidoException si el motivo está vacío o es muy largo
     * @throws TransicionTiendaInvalidaException si la tienda no está PENDIENTE
     */
    public void rechazar(String motivo, Instant ahora) {
        if (motivo == null
                || motivo.strip().length() < LONGITUD_MINIMA_MOTIVO
                || motivo.strip().length() > LONGITUD_MAXIMA_MOTIVO) {
            throw new MotivoRechazoInvalidoException();
        }
        exigirEstado(EstadoTienda.PENDIENTE, "rechazar");
        this.estado = EstadoTienda.RECHAZADA;
        this.motivoRechazo = motivo.strip();
        this.fechaRevision = ahora;
    }

    private void exigirEstado(EstadoTienda estadoEsperado, String accion) {
        if (estado != estadoEsperado) {
            throw new TransicionTiendaInvalidaException(accion, estado);
        }
    }

    /** Solo una tienda aprobada y con el interruptor en Abierto entra en la difusión de solicitudes. */
    public boolean puedeRecibirSolicitudes() {
        return estado == EstadoTienda.APROBADA && estaAbierta;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDuenoId() {
        return duenoId;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public EstadoTienda getEstado() {
        return estado;
    }

    public boolean estaAbierta() {
        return estaAbierta;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public Optional<String> getMotivoRechazo() {
        return Optional.ofNullable(motivoRechazo);
    }

    public Optional<Instant> getFechaRevision() {
        return Optional.ofNullable(fechaRevision);
    }
}
