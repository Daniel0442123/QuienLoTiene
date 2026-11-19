package com.quienlotiene.catalogo.infrastructure.web;

import org.jspecify.annotations.Nullable;

/**
 * HU-023: si el producto está en la lista de restringidos y el aviso que se muestra.
 *
 * @param aviso «Este medicamento necesita fórmula médica» (SC-011 punto 7); {@code null} si no es restringido
 */
public record RestriccionResponse(
        boolean restringido, @Nullable String aviso) {

    public static final String AVISO = "Este medicamento necesita fórmula médica";
}
