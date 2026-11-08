package com.quienlotiene.clientes.domain.model;

import com.quienlotiene.shared.domain.Ubicacion;
import java.time.Instant;
import java.util.UUID;

/**
 * Dirección de entrega del cliente (HU-007, diagrama 09 {@code clientes.direccion}): punto en el mapa, dirección
 * escrita, indicaciones y teléfono de quien recibe. La usa la orden con domicilio.
 */
public final class Direccion {

    public static final int MAXIMO_POR_CLIENTE = 10;
    public static final int LONGITUD_MAXIMA_NOMBRE = 40;
    public static final int LONGITUD_MAXIMA_TEXTO = 200;
    public static final int LONGITUD_MAXIMA_TELEFONO = 20;

    private final UUID id;
    private final UUID clienteId;
    private String nombre;
    private Ubicacion ubicacion;
    private String direccionEscrita;
    private String indicaciones;
    private String telefono;
    private final Instant fechaCreacion;

    private Direccion(
            UUID id,
            UUID clienteId,
            String nombre,
            Ubicacion ubicacion,
            String direccionEscrita,
            String indicaciones,
            String telefono,
            Instant fechaCreacion) {
        this.id = id;
        this.clienteId = clienteId;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.direccionEscrita = direccionEscrita;
        this.indicaciones = indicaciones;
        this.telefono = telefono;
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * CP-CLI-007-01: guarda "Casa" con pin, dirección escrita, indicaciones y teléfono.
     *
     * @throws DireccionIncompletaException si falta el nombre, la ubicación, la dirección escrita o el teléfono
     *     (CP-CLI-007-02)
     */
    public static Direccion registrar(
            UUID id,
            UUID clienteId,
            String nombre,
            Ubicacion ubicacion,
            String direccionEscrita,
            String indicaciones,
            String telefono,
            Instant ahora) {
        var direccion = new Direccion(id, clienteId, null, null, null, null, null, ahora);
        direccion.cambiar(nombre, ubicacion, direccionEscrita, indicaciones, telefono);
        return direccion;
    }

    /** Vuelve a armar una dirección guardada; solo lo usa el adaptador de persistencia. */
    public static Direccion reconstituir(
            UUID id,
            UUID clienteId,
            String nombre,
            Ubicacion ubicacion,
            String direccionEscrita,
            String indicaciones,
            String telefono,
            Instant fechaCreacion) {
        return new Direccion(id, clienteId, nombre, ubicacion, direccionEscrita, indicaciones, telefono, fechaCreacion);
    }

    /** Editar (CU-03): mismas reglas que al crearla. */
    public void cambiar(
            String nuevoNombre,
            Ubicacion nuevaUbicacion,
            String nuevaDireccionEscrita,
            String nuevasIndicaciones,
            String nuevoTelefono) {
        if (nuevaUbicacion == null) {
            throw new DireccionIncompletaException("marca la ubicación en el mapa");
        }
        this.nombre =
                exigirTexto(nuevoNombre, "falta el nombre de la dirección, por ejemplo Casa", LONGITUD_MAXIMA_NOMBRE);
        this.direccionEscrita = exigirTexto(nuevaDireccionEscrita, "falta la dirección escrita", LONGITUD_MAXIMA_TEXTO);
        this.telefono = exigirTexto(nuevoTelefono, "falta el teléfono de quien recibe", LONGITUD_MAXIMA_TELEFONO);
        if (nuevasIndicaciones != null && nuevasIndicaciones.strip().length() > LONGITUD_MAXIMA_TEXTO) {
            throw new DireccionIncompletaException(
                    "las indicaciones pueden tener máximo " + LONGITUD_MAXIMA_TEXTO + " caracteres");
        }
        this.indicaciones =
                nuevasIndicaciones == null || nuevasIndicaciones.isBlank() ? null : nuevasIndicaciones.strip();
        this.ubicacion = nuevaUbicacion;
    }

    public boolean esDe(UUID posibleCliente) {
        return clienteId.equals(posibleCliente);
    }

    private static String exigirTexto(String valor, String mensajeSiFalta, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new DireccionIncompletaException(mensajeSiFalta);
        }
        if (valor.strip().length() > longitudMaxima) {
            throw new DireccionIncompletaException("un dato supera los " + longitudMaxima + " caracteres");
        }
        return valor.strip();
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getNombre() {
        return nombre;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public String getDireccionEscrita() {
        return direccionEscrita;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public String getTelefono() {
        return telefono;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
