package com.quienlotiene.catalogo.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code catalogo.producto_restringido}. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "catalogo", name = "producto_restringido")
public class ProductoRestringidoEntity {

    @Id
    private UUID id;

    @Column(name = "palabra_clave", nullable = false, length = 80, updatable = false)
    private String palabraClave;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    ProductoRestringidoEntity(UUID id, String palabraClave, Instant ahora) {
        this.id = id;
        this.palabraClave = palabraClave;
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }
}
