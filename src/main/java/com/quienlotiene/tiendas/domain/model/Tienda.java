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
    private String nombre;
    private String telefono;
    private HorarioAtencion horario;
    private final Ubicacion ubicacion;
    private EstadoTienda estado;
    private boolean estaAbierta;
    private final Instant fechaRegistro;
    private String motivoRechazo;
    private Instant fechaRevision;

    private Tienda(
            UUID id,
            UUID duenoId,
            UUID categoriaId,
            String nombre,
            String telefono,
            HorarioAtencion horario,
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
        this.horario = horario;
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
        validarNombreYTelefono(nombre, telefono);
        if (categoriaId == null) {
            throw new TiendaIncompletaException("falta la categoría");
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
                HorarioAtencion.sinConfigurar(),
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
            HorarioAtencion horario,
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
                horario,
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

    /**
     * HU-013 (CU-19): con el interruptor en Abierto la tienda entra en la difusión de solicitudes (si además está en su
     * horario). Cerrada deja de recibir solicitudes nuevas; sus órdenes en curso siguen.
     *
     * @throws TiendaNoAprobadaException si la tienda no está APROBADA
     */
    public void cambiarApertura(boolean abrir) {
        if (estado != EstadoTienda.APROBADA) {
            throw new TiendaNoAprobadaException();
        }
        this.estaAbierta = abrir;
    }

    /**
     * HU-009 (CU-16): el dueño cambia el nombre visible, el teléfono y el horario. La categoría y la ubicación no se
     * cambian aquí porque el administrador las revisó al aprobar la tienda.
     *
     * @throws TiendaIncompletaException si falta el nombre o el teléfono
     */
    public void actualizarPerfil(String nuevoNombre, String nuevoTelefono, HorarioAtencion nuevoHorario) {
        validarNombreYTelefono(nuevoNombre, nuevoTelefono);
        this.nombre = nuevoNombre.strip();
        this.telefono = nuevoTelefono.strip();
        this.horario = nuevoHorario == null ? HorarioAtencion.sinConfigurar() : nuevoHorario;
    }

    private static void validarNombreYTelefono(String nombre, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new TiendaIncompletaException("falta el nombre de la tienda");
        }
        if (nombre.strip().length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new TiendaIncompletaException(
                    "el nombre puede tener máximo " + LONGITUD_MAXIMA_NOMBRE + " caracteres");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new TiendaIncompletaException("falta el teléfono");
        }
    }

    private void exigirEstado(EstadoTienda estadoEsperado, String accion) {
        if (estado != estadoEsperado) {
            throw new TransicionTiendaInvalidaException(accion, estado);
        }
    }

    /**
     * Solo una tienda aprobada, con el interruptor en Abierto y dentro de su horario entra en la difusión de
     * solicitudes (CP-TIE-002-01, CP-TIE-009-01).
     */
    public boolean puedeRecibirSolicitudes(Instant ahora) {
        return estado == EstadoTienda.APROBADA && estaAbierta && horario.atiendeEn(ahora);
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

    public HorarioAtencion getHorario() {
        return horario;
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
