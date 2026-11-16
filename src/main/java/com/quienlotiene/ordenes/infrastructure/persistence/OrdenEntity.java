package com.quienlotiene.ordenes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code ordenes.orden}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "ordenes", name = "orden")
public class OrdenEntity {

    @Id
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, updatable = false)
    private UUID solicitudId;

    @Column(name = "respuesta_id", nullable = false, updatable = false)
    private UUID respuestaId;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(name = "dueno_tienda_id", nullable = false, updatable = false)
    private UUID duenoTiendaId;

    @Column(nullable = false, length = 300, updatable = false)
    private String producto;

    @Column(nullable = false, updatable = false)
    private int cantidad;

    @Column(name = "precio_unitario_pesos", nullable = false, updatable = false)
    private long precioUnitarioPesos;

    @Column(name = "costo_domicilio_pesos", nullable = false, updatable = false)
    private long costoDomicilioPesos;

    @Column(nullable = false, length = 10, updatable = false)
    private String modalidad;

    @Column(name = "medio_pago", nullable = false, length = 15, updatable = false)
    private String medioPago;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "cliente_nombre", nullable = false, length = 120, updatable = false)
    private String clienteNombre;

    @Column(name = "cliente_telefono", nullable = false, length = 20, updatable = false)
    private String clienteTelefono;

    @Column(name = "tienda_nombre", nullable = false, length = 120, updatable = false)
    private String tiendaNombre;

    @Column(name = "tienda_telefono", nullable = false, length = 20, updatable = false)
    private String tiendaTelefono;

    @Column(name = "direccion_nombre", length = 40, updatable = false)
    private String direccionNombre;

    @Column(name = "direccion_escrita", length = 200, updatable = false)
    private String direccionEscrita;

    @Column(name = "direccion_indicaciones", length = 200, updatable = false)
    private String direccionIndicaciones;

    @Column(name = "direccion_latitud", updatable = false)
    private Double direccionLatitud;

    @Column(name = "direccion_longitud", updatable = false)
    private Double direccionLongitud;

    @Version
    private long version;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    /** Datos que no cambian después de crear la orden. */
    record Datos(
            UUID id,
            UUID solicitudId,
            UUID respuestaId,
            UUID clienteId,
            UUID tiendaId,
            UUID duenoTiendaId,
            String producto,
            int cantidad,
            long precioUnitarioPesos,
            long costoDomicilioPesos,
            String modalidad,
            String medioPago,
            String clienteNombre,
            String clienteTelefono,
            String tiendaNombre,
            String tiendaTelefono,
            String direccionNombre,
            String direccionEscrita,
            String direccionIndicaciones,
            Double direccionLatitud,
            Double direccionLongitud) {}

    OrdenEntity(Datos datos, String estado, Instant creadoEn) {
        this.id = datos.id();
        this.solicitudId = datos.solicitudId();
        this.respuestaId = datos.respuestaId();
        this.clienteId = datos.clienteId();
        this.tiendaId = datos.tiendaId();
        this.duenoTiendaId = datos.duenoTiendaId();
        this.producto = datos.producto();
        this.cantidad = datos.cantidad();
        this.precioUnitarioPesos = datos.precioUnitarioPesos();
        this.costoDomicilioPesos = datos.costoDomicilioPesos();
        this.modalidad = datos.modalidad();
        this.medioPago = datos.medioPago();
        this.clienteNombre = datos.clienteNombre();
        this.clienteTelefono = datos.clienteTelefono();
        this.tiendaNombre = datos.tiendaNombre();
        this.tiendaTelefono = datos.tiendaTelefono();
        this.direccionNombre = datos.direccionNombre();
        this.direccionEscrita = datos.direccionEscrita();
        this.direccionIndicaciones = datos.direccionIndicaciones();
        this.direccionLatitud = datos.direccionLatitud();
        this.direccionLongitud = datos.direccionLongitud();
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    void cambiarEstado(String nuevoEstado, Instant ahora) {
        this.estado = nuevoEstado;
        this.actualizadoEn = ahora;
    }
}
