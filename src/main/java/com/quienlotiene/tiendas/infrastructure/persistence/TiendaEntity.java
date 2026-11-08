package com.quienlotiene.tiendas.infrastructure.persistence;

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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/** Fila de {@code tiendas.tienda}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "tiendas", name = "tienda")
public class TiendaEntity {

    /** WGS 84, el sistema de coordenadas del GPS. */
    private static final int SRID_WGS84 = 4326;

    private static final GeometryFactory FABRICA_GEOMETRIAS = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    @Id
    private UUID id;

    @Column(name = "dueno_id", nullable = false, updatable = false)
    private UUID duenoId;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    private Point ubicacion;

    @Column(nullable = false, length = 12)
    private String estado;

    @Column(nullable = false)
    private boolean abierta;

    @Column(name = "motivo_rechazo", length = 300)
    private String motivoRechazo;

    @Column(name = "fecha_revision")
    private Instant fechaRevision;

    @Version
    private long version;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public TiendaEntity(
            UUID id,
            UUID duenoId,
            UUID categoriaId,
            String nombre,
            String telefono,
            double latitud,
            double longitud,
            String estado,
            boolean abierta,
            Instant creadoEn) {
        this.id = id;
        this.duenoId = duenoId;
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.telefono = telefono;
        // En JTS el orden es (x, y) = (longitud, latitud)
        this.ubicacion = FABRICA_GEOMETRIAS.createPoint(new Coordinate(longitud, latitud));
        this.estado = estado;
        this.abierta = abierta;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }

    /** Copia en la fila la decisión del administrador (HU-036). */
    void registrarRevision(String nuevoEstado, String motivo, Instant fecha) {
        this.estado = nuevoEstado;
        this.motivoRechazo = motivo;
        this.fechaRevision = fecha;
        this.actualizadoEn = fecha;
    }

    double latitud() {
        return ubicacion.getY();
    }

    double longitud() {
        return ubicacion.getX();
    }
}
