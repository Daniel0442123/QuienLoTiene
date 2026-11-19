package com.quienlotiene.notificaciones.application.dto;

import org.jspecify.annotations.Nullable;

/**
 * Lo que el frontend necesita para suscribirse (HU-031, diagrama 28 paso 2).
 *
 * @param habilitado {@code false} si el servidor no tiene claves VAPID: el frontend no pide el permiso
 * @param clavePublica la {@code applicationServerKey} en Base64 URL, o {@code null} si no está habilitado
 */
public record ConfiguracionPush(
        boolean habilitado, @Nullable String clavePublica) {}
