package com.quienlotiene.clientes.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/** Fila de {@code clientes.direccion}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "clientes", name = "direccion")
public class DireccionEntity {

    /** WGS 84, el sistema de coordenadas del GPS. */
    private static final int SRID_WGS84 = 4326;

    private static final GeometryFactory FABRICA_GEOMETRIAS = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(nullable = false, length = 40)
    private String nombre;

    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point ubicacion;

    @Column(name = "direccion_escrita", nullable = false, length = 200)
    private String direccionEscrita;

    @Column(length = 200)
    private String indicaciones;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public DireccionEntity(UUID id, UUID clienteId, Instant creadoEn) {
        this.id = id;
        this.clienteId = clienteId;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    void copiarDatos(
            String nuevoNombre,
            double latitud,
            double longitud,
            String nuevaDireccionEscrita,
            String nuevasIndicaciones,
            String nuevoTelefono,
            Instant fecha) {
        this.nombre = nuevoNombre;
        // En JTS el orden es (x, y) = (longitud, latitud)
        this.ubicacion = FABRICA_GEOMETRIAS.createPoint(new Coordinate(longitud, latitud));
        this.direccionEscrita = nuevaDireccionEscrita;
        this.indicaciones = nuevasIndicaciones;
        this.telefono = nuevoTelefono;
        this.actualizadoEn = fecha;
    }

    double latitud() {
        return ubicacion.getY();
    }

    double longitud() {
        return ubicacion.getX();
    }
}
