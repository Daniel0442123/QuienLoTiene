package com.quienlotiene.tiendas.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Lo que el administrador ve de una tienda en la bandeja de revisión. Nunca incluye el contenido de los documentos. */
public record TiendaParaRevision(
        UUID id,
        String nombre,
        UUID categoriaId,
        String telefono,
        double latitud,
        double longitud,

        @Schema(allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "SUSPENDIDA"})
        String estado,

        Instant fechaRegistro,
        @Nullable String motivoRechazo,
        @Nullable Instant fechaRevision,
        List<DocumentoResumen> documentos) {

    public TiendaParaRevision {
        documentos = List.copyOf(documentos);
    }

    /** Datos de un documento para listarlo; el archivo se descarga aparte. */
    public record DocumentoResumen(
            UUID id,
            @Schema(allowableValues = {"FOTO_LOCAL", "RUT"}) String tipo,
            String tipoContenido,
            int tamanoEnBytes) {}
}
