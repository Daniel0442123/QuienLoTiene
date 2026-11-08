package com.quienlotiene.tiendas.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code tiendas.documento_verificacion}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "tiendas", name = "documento_verificacion")
public class DocumentoVerificacionEntity {

    @Id
    private UUID id;

    @Column(name = "tienda_id", nullable = false, updatable = false)
    private UUID tiendaId;

    @Column(nullable = false, length = 40, updatable = false)
    private String tipo;

    @Column(nullable = false, updatable = false)
    private String ruta;

    @Column(name = "tipo_contenido", nullable = false, length = 40, updatable = false)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false, updatable = false)
    private int tamanoBytes;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public DocumentoVerificacionEntity(
            UUID id, UUID tiendaId, String tipo, String ruta, String tipoContenido, int tamanoBytes, Instant creadoEn) {
        this.id = id;
        this.tiendaId = tiendaId;
        this.tipo = tipo;
        this.ruta = ruta;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
        this.creadoEn = creadoEn;
        this.actualizadoEn = creadoEn;
    }
}
