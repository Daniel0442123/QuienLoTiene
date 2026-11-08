package com.quienlotiene.tiendas.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Lo que el administrador ve de una tienda en la bandeja de revisión. Nunca incluye el contenido de los documentos. */
public record TiendaParaRevision(
        UUID id,
        String nombre,
        UUID categoriaId,
        String telefono,
        double latitud,
        double longitud,
        String estado,
        Instant fechaRegistro,
        String motivoRechazo,
        Instant fechaRevision,
        List<DocumentoResumen> documentos) {

    public TiendaParaRevision {
        documentos = List.copyOf(documentos);
    }

    /** Datos de un documento para listarlo; el archivo se descarga aparte. */
    public record DocumentoResumen(UUID id, String tipo, String tipoContenido, int tamanoEnBytes) {}
}
