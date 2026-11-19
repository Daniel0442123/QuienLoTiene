package com.quienlotiene.mensajeria.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/v1/ordenes/{id}/mensajes}. */
record EnviarMensajeRequest(@NotBlank @Size(max = 1000) String texto) {}
