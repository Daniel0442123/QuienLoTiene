package com.quienlotiene.solicitudes.infrastructure.persistence;

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

/** Fila de {@code solicitudes.solicitud}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "solicitudes", name = "solicitud")
public class SolicitudEntity {

    /** WGS 84, el sistema de coordenadas del GPS. */
    private static final int SRID_WGS84 = 4326;

    private static final GeometryFactory FABRICA_GEOMETRIAS = new GeometryFactory(new PrecisionModel(), SRID_WGS84);

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private UUID clienteId;

    @Column(name = "categoria_id", nullable = false, updatable = false)
    private UUID categoriaId;

    @Column(nullable = false, length = 150, updatable = false)
    private String producto;

    @Column(nullable = false, updatable = false)
    private int cantidad;

    @Column(nullable = false, length = 10, updatable = false)
    private String modalidad;

    @Column(nullable = false, updatable = false)
    private boolean urgente;

    @Column(nullable = false, updatable = false, columnDefinition = "geography(Point,4326)")
    private Point ubicacion;

    @Column(name = "radio_metros", nullable = false)
    private int radioMetros;

    @Column(name = "fue_ampliado", nullable = false)
    private boolean fueAmpliado;

    @Column(nullable = false)
    private boolean restringida;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(name = "vence_en", nullable = false)
    private Instant venceEn;

    @Version
    private long version;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public SolicitudEntity(
            UUID id,
            UUID clienteId,
            UUID categoriaId,
            String producto,
            int cantidad,
            String modalidad,
            boolean urgente,
            double latitud,
            double longitud,
            int radioMetros,
            String estado,
            Instant venceEn,
            Instant creadoEn) {
        this.id = id;
        this.clienteId = clienteId;
        this.categoriaId = categoriaId;
        this.producto = producto;
        this.cantidad = cantidad;
        this.modalidad = modalidad;
        this.urgente = urgente;
        // En JTS el orden es (x, y) = (longitud, latitud)
        this.ubicacion = FABRICA_GEOMETRIAS.createPoint(new Coordinate(longitud, latitud));
        this.radioMetros = radioMetros;
        this.fueAmpliado = false;
        this.restringida = false;
        this.estado = estado;
        this.venceEn = venceEn;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
