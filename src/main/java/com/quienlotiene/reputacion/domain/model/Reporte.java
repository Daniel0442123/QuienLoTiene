package com.quienlotiene.reputacion.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Problema que un cliente o una tienda reporta sobre una orden (HU-035, CU-13). Lo atiende el administrador (HU-037,
 * CU-27).
 */
public final class Reporte {

    public static final int MAXIMO_DESCRIPCION = 1000;
    public static final int MINIMO_NOTA = 3;
    public static final int MAXIMO_NOTA = 500;

    private final UUID id;
    private final UUID ordenId;
    private final UUID tiendaId;
    private final UUID autorId;
    private final boolean autorEsCliente;
    private final MotivoReporte motivo;
    private final String descripcion;
    private final Instant fecha;
    private EstadoReporte estado;
    private String notaResolucion;
    private Instant fechaResolucion;

    private Reporte(
            UUID id,
            UUID ordenId,
            UUID tiendaId,
            UUID autorId,
            boolean autorEsCliente,
            MotivoReporte motivo,
            String descripcion,
            Instant fecha,
            EstadoReporte estado,
            String notaResolucion,
            Instant fechaResolucion) {
        this.id = Objects.requireNonNull(id, "id");
        this.ordenId = Objects.requireNonNull(ordenId, "ordenId");
        this.tiendaId = Objects.requireNonNull(tiendaId, "tiendaId");
        this.autorId = Objects.requireNonNull(autorId, "autorId");
        this.autorEsCliente = autorEsCliente;
        this.motivo = Objects.requireNonNull(motivo, "motivo");
        this.descripcion = descripcion;
        this.fecha = Objects.requireNonNull(fecha, "fecha");
        this.estado = Objects.requireNonNull(estado, "estado");
        this.notaResolucion = notaResolucion;
        this.fechaResolucion = fechaResolucion;
    }

    /**
     * HU-035 (CP-REP-035-01): el reporte queda ABIERTO en el panel del administrador.
     *
     * @throws CalificacionInvalidaException REP-004 si el motivo es OTRO sin descripción o la descripción es muy larga
     */
    public static Reporte crear(
            UUID id,
            UUID ordenId,
            UUID tiendaId,
            UUID autorId,
            boolean autorEsCliente,
            MotivoReporte motivo,
            String descripcion,
            Instant ahora) {
        var limpia = descripcion == null || descripcion.isBlank() ? null : descripcion.strip();
        if (motivo == null) {
            throw new CalificacionInvalidaException("elige el motivo del reporte");
        }
        if (motivo == MotivoReporte.OTRO && limpia == null) {
            throw new CalificacionInvalidaException("cuéntanos qué pasó");
        }
        if (limpia != null && limpia.length() > MAXIMO_DESCRIPCION) {
            throw new CalificacionInvalidaException("la descripción puede tener hasta 1000 caracteres");
        }
        return new Reporte(
                id,
                ordenId,
                tiendaId,
                autorId,
                autorEsCliente,
                motivo,
                limpia,
                ahora,
                EstadoReporte.ABIERTO,
                null,
                null);
    }

    /** Vuelve a armar un reporte guardado; solo lo usa el adaptador de persistencia. */
    public static Reporte reconstituir(
            UUID id,
            UUID ordenId,
            UUID tiendaId,
            UUID autorId,
            boolean autorEsCliente,
            MotivoReporte motivo,
            String descripcion,
            Instant fecha,
            EstadoReporte estado,
            String notaResolucion,
            Instant fechaResolucion) {
        return new Reporte(
                id,
                ordenId,
                tiendaId,
                autorId,
                autorEsCliente,
                motivo,
                descripcion,
                fecha,
                estado,
                notaResolucion,
                fechaResolucion);
    }

    /**
     * HU-037 (CP-ADM-037-02): el administrador lo deja RESUELTO con una nota.
     *
     * @throws ReporteYaResueltoException REP-005 si ya estaba resuelto
     * @throws CalificacionInvalidaException REP-004 si la nota es muy corta o muy larga
     */
    public void resolver(String nota, Instant ahora) {
        if (estado == EstadoReporte.RESUELTO) {
            throw new ReporteYaResueltoException();
        }
        var limpia = nota == null ? "" : nota.strip();
        if (limpia.length() < MINIMO_NOTA || limpia.length() > MAXIMO_NOTA) {
            throw new CalificacionInvalidaException("la nota debe tener entre 3 y 500 caracteres");
        }
        this.estado = EstadoReporte.RESUELTO;
        this.notaResolucion = limpia;
        this.fechaResolucion = ahora;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrdenId() {
        return ordenId;
    }

    public UUID getTiendaId() {
        return tiendaId;
    }

    public UUID getAutorId() {
        return autorId;
    }

    public boolean autorEsCliente() {
        return autorEsCliente;
    }

    public MotivoReporte getMotivo() {
        return motivo;
    }

    /** {@code null} si no escribió descripción. */
    public String getDescripcion() {
        return descripcion;
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    /** {@code null} mientras esté ABIERTO. */
    public String getNotaResolucion() {
        return notaResolucion;
    }

    /** {@code null} mientras esté ABIERTO. */
    public Instant getFechaResolucion() {
        return fechaResolucion;
    }
}
