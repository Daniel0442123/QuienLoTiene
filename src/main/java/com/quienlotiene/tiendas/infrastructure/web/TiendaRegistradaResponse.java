package com.quienlotiene.tiendas.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/** Respuesta del registro: la tienda queda en revisión ("PENDIENTE"). */
public record TiendaRegistradaResponse(UUID id, String nombre, String estado, Instant fechaRegistro) {}
