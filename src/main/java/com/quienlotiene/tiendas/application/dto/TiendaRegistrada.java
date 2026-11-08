package com.quienlotiene.tiendas.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Resultado del registro: la tienda queda PENDIENTE de revisión. */
public record TiendaRegistrada(UUID id, String nombre, String estado, Instant fechaRegistro) {}
