package com.quienlotiene.ordenes.application.dto;

import java.util.UUID;

/** El cliente elige una respuesta (HU-024). El cliente sale del token. */
public record CrearOrdenComando(
        UUID clienteId, UUID respuestaId, String modalidad, UUID direccionId, String medioPago) {}
