package com.quienlotiene.identidad.application.dto;

import java.time.Instant;
import java.util.UUID;

/** Resultado de registrar un cliente. */
public record ClienteRegistrado(UUID id, String nombre, String correo, String rol, Instant fechaCreacion) {}
