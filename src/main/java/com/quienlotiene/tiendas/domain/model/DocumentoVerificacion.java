package com.quienlotiene.tiendas.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Documento guardado de una tienda (diagrama 09). La ruta apunta al almacén de archivos; solo el administrador lo
 * descarga (HU-036).
 */
public record DocumentoVerificacion(
        UUID id,
        UUID tiendaId,
        TipoDocumento tipo,
        String ruta,
        String tipoContenido,
        int tamanoEnBytes,
        Instant fechaSubida) {}
