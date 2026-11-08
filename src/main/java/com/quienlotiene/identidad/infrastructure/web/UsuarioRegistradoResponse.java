package com.quienlotiene.identidad.infrastructure.web;

import java.time.Instant;
import java.util.UUID;

/** Respuesta de un registro exitoso. Nunca incluye el hash ni la frase. */
public record UsuarioRegistradoResponse(UUID id, String nombre, String correo, String rol, Instant fechaCreacion) {}
