package com.quienlotiene.mensajeria.infrastructure.web;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Cuerpo de {@code POST /api/v1/ordenes/{id}/mensajes}. Sin validación de Bean Validation a propósito: el texto vacío
 * o de más de 1000 caracteres lo rechaza el dominio ({@code Mensaje}) con MEN-003, el código del contrato, en vez de
 * la validación genérica.
 */
record EnviarMensajeRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, minLength = 1, maxLength = 1000)
        String texto) {}
