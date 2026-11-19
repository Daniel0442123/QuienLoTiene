package com.quienlotiene.mensajeria.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Fila de {@code mensajeria.mensaje}. Los mensajes no se editan. */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(schema = "mensajeria", name = "mensaje")
public class MensajeEntity {

    @Id
    private UUID id;

    @Column(name = "orden_id", nullable = false, updatable = false)
    private UUID ordenId;

    @Column(name = "autor_id", nullable = false, updatable = false)
    private UUID autorId;

    @Column(name = "autor_es_cliente", nullable = false, updatable = false)
    private boolean autorEsCliente;

    @Column(nullable = false, length = 1000, updatable = false)
    private String texto;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    MensajeEntity(UUID id, UUID ordenId, UUID autorId, boolean autorEsCliente, String texto, Instant creadoEn) {
        this.id = id;
        this.ordenId = ordenId;
        this.autorId = autorId;
        this.autorEsCliente = autorEsCliente;
        this.texto = texto;
        this.creadoEn = creadoEn;
    }
}
